package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamSettingPresenterImpl extends AbstractPresenter<CamSettingContract.View> implements
        CamSettingContract.Presenter {

    private Subscription subscription;

    private BeanCamInfo camInfoBean;

    public CamSettingPresenterImpl(CamSettingContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }

    private void wrap(DeviceBean bean) {
        BaseBean baseBean = new BaseBean();
        baseBean.alias = bean.alias;
        baseBean.pid = bean.pid;
        baseBean.shareAccount = bean.shareAccount;
        baseBean.sn = bean.sn;
        baseBean.uuid = bean.uuid;
        BeanCamInfo info = new BeanCamInfo();
        if (bean.dataList == null && BuildConfig.DEBUG) {
            throw new IllegalArgumentException("list is null");
        }
        info.convert(baseBean, bean.dataList);
        this.camInfoBean = info;
    }

    @Override
    public void fetchCamInfo(final DeviceBean bean) {
        wrap(bean);
        //查询设备列表
        unSubscribe(subscription);
        subscription = RxBus.getUiInstance().toObservableSticky(RxUiEvent.BulkDeviceList.class)
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
                            if (TextUtils.equals(wrap.baseDpDevice.uuid, bean.uuid)) {
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
                .flatMap(new Func1<DpMsgDefine.DpWrap, Observable<BeanCamInfo>>() {
                    @Override
                    public Observable<BeanCamInfo> call(DpMsgDefine.DpWrap dpWrap) {
                        BeanCamInfo info = new BeanCamInfo();
                        info.convert(dpWrap.baseDpDevice, dpWrap.baseDpMsgList);
                        AppLogger.i("BeanCamInfo: " + new Gson().toJson(info));
                        return Observable.just(info);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BeanCamInfo>() {
                    @Override
                    public void call(BeanCamInfo camInfoBean) {
                        //刷新
                        getView().onCamInfoRsp(camInfoBean);
                        if (camInfoBean.deviceBase != null
                                && !TextUtils.isEmpty(camInfoBean.deviceBase.shareAccount)) {
                            getView().isSharedDevice();
                        }
                    }
                });
        RxBus.getCacheInstance().post(new RxUiEvent.QueryBulkDevice());
    }

    @Override
    public BeanCamInfo getCamInfoBean() {
        if (camInfoBean == null)
            camInfoBean = new BeanCamInfo();
        return camInfoBean;
    }

    @Override
    public void saveCamInfoBean(BeanCamInfo camInfoBean) {
        this.camInfoBean = camInfoBean;
    }
}
