package com.cylan.jiafeigou.n.mvp.impl.bell;

import android.text.TextUtils;

import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanBellInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class BellSettingPresenterImpl extends AbstractPresenter<BellSettingContract.View>
        implements BellSettingContract.Presenter {
    private BeanBellInfo beanBellInfo;
    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public BellSettingPresenterImpl(BellSettingContract.View view, DeviceBean bean) {
        super(view);
        view.setPresenter(this);
        fillData(bean);
    }

    private void fillData(DeviceBean bean) {
        beanBellInfo = new BeanBellInfo();
        BaseBean baseBean = new BaseBean();
        baseBean.alias = bean.alias;
        baseBean.pid = bean.pid;
        baseBean.uuid = bean.uuid;
        baseBean.sn = bean.sn;
        beanBellInfo.convert(baseBean, bean.dataList);
    }


    @Override
    public void start() {
        unSubscribe(compositeSubscription);
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(onBellInfoSubscription());
        compositeSubscription.add(onLoginStateSubscription());
        compositeSubscription.add(unbindDevSub());
    }

    /**
     * 门铃解绑
     *
     * @return
     */
    private Subscription unbindDevSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.UnBindDeviceEvent.class)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<RxEvent.UnBindDeviceEvent, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.UnBindDeviceEvent unBindDeviceEvent) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.UnBindDeviceEvent, Object>() {
                    @Override
                    public Object call(RxEvent.UnBindDeviceEvent unBindDeviceEvent) {
                        getView().unbindDeviceRsp(unBindDeviceEvent.jfgResult.code);
                        if (unBindDeviceEvent.jfgResult.code == 0) {
                            //清理这个订阅
                            RxBus.getCacheInstance().removeStickyEvent(RxEvent.UnBindDeviceEvent.class);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("unbindDevSub"))
                .subscribe();
    }

    private Subscription onBellInfoSubscription() {
        //查询设备列表
        return RxBus.getUiInstance().toObservableSticky(RxUiEvent.BulkDeviceList.class)
                .subscribeOn(Schedulers.computation())
                .filter(new Func1<RxUiEvent.BulkDeviceList, Boolean>() {
                    @Override
                    public Boolean call(RxUiEvent.BulkDeviceList list) {
                        return getView() != null && list != null && list.allDevices != null;
                    }
                })
                .flatMap(new Func1<RxUiEvent.BulkDeviceList, Observable<DpMsgDefine.DpWrap>>() {
                    @Override
                    public Observable<DpMsgDefine.DpWrap> call(RxUiEvent.BulkDeviceList list) {
                        for (DpMsgDefine.DpWrap wrap : list.allDevices) {
                            if (beanBellInfo.deviceBase == null || wrap.baseDpDevice == null)
                                continue;
                            if (TextUtils.equals(wrap.baseDpDevice.uuid,
                                    beanBellInfo.deviceBase.uuid)) {
                                return Observable.just(wrap);
                            }
                        }
                        return null;
                    }
                })
                .filter(new Func1<DpMsgDefine.DpWrap, Boolean>() {
                    @Override
                    public Boolean call(DpMsgDefine.DpWrap dpWrap) {
                        return dpWrap != null && dpWrap.baseDpDevice != null;
                    }
                })
                .flatMap(new Func1<DpMsgDefine.DpWrap, Observable<BeanBellInfo>>() {
                    @Override
                    public Observable<BeanBellInfo> call(DpMsgDefine.DpWrap dpWrap) {
                        BeanBellInfo info = new BeanBellInfo();
                        info.convert(dpWrap.baseDpDevice, dpWrap.baseDpMsgList);
                        beanBellInfo = info;
                        AppLogger.i("BeanCamInfo: " + new Gson().toJson(info));
                        return Observable.just(info);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BeanBellInfo>() {
                    @Override
                    public void call(BeanBellInfo beanBellInfo) {
                        //刷新
                        getView().onSettingInfoRsp(beanBellInfo);
                    }
                });
    }

    /**
     * 查询登陆状态
     *
     * @return
     */
    private Subscription onLoginStateSubscription() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.LoginRsp>() {
                    @Override
                    public void call(RxEvent.LoginRsp o) {
                        if (getView() != null)
                            getView().onLoginState(o.state);
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }


    @Override
    public BeanBellInfo getBellInfo() {
        return beanBellInfo;
    }

    @Override
    public void unbindDevice() {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        String uuid = beanBellInfo.deviceBase.uuid;
                        RxEvent.JFGDeviceDeletion deletion = new RxEvent.JFGDeviceDeletion();
                        deletion.uuid = uuid;
                        RxBus.getCacheInstance().post(deletion);
                        JfgCmdInsurance.getCmd().unBindDevice(uuid);
                        AppLogger.i("unbind uuid: " + uuid);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("delete uuid failed: " + throwable.getLocalizedMessage());
                    }
                });
    }
}
