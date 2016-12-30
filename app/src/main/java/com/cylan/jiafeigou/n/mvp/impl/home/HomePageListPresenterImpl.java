package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.br.TimeTickBroadcast;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.GreetBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-23.
 */
public class HomePageListPresenterImpl extends AbstractPresenter<HomePageListContract.View>
        implements HomePageListContract.Presenter {

    private TimeTickBroadcast timeTickBroadcast;

    public HomePageListPresenterImpl(HomePageListContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                getDevicesList(),
                getTimeTickEventSub(),
                getLoginRspSub(),
                subDeviceList(),
                singleDeviceSub(),
                JFGAccountUpdate()};
    }

    /**
     * 启动,就要获取设备列表
     *
     * @return
     */
    private Subscription getDevicesList() {
        return Observable.just("null")
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (!RxBus.getCacheInstance().hasStickyEvent(RxUiEvent.BulkDeviceListRsp.class)) {
                            RxBus.getCacheInstance().post(new RxUiEvent.BulkDeviceListReq());
                            Log.d(TAG, "getDevicesList getDevicesList");
                        }
                    }
                });
    }

    /**
     * 跟新单个
     *
     * @return
     */
    private Subscription singleDeviceSub() {
        return RxBus.getCacheInstance().toObservable(RxUiEvent.SingleDevice.class)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .filter(new Func1<RxUiEvent.SingleDevice, Boolean>() {
                    @Override
                    public Boolean call(RxUiEvent.SingleDevice singleDevice) {
                        boolean notNull = getView() != null && singleDevice != null && singleDevice.dpMsg != null;
                        AppLogger.i("notNull: " + notNull);
                        return notNull;
                    }
                })
                .flatMap(new Func1<RxUiEvent.SingleDevice, Observable<DeviceBean>>() {
                    @Override
                    public Observable<DeviceBean> call(RxUiEvent.SingleDevice singleDevice) {
                        AppLogger.i("get devices : " + singleDevice);
                        DeviceBean bean = new DeviceBean();
                        bean.fillData(singleDevice.dpMsg.baseDpDevice, singleDevice.dpMsg.baseDpMsgList);
                        return Observable.just(bean);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<DeviceBean, DeviceBean>() {
                    @Override
                    public DeviceBean call(DeviceBean bean) {
//                        if (bean.uuid.equals("200000000472")) {//测试分享账号显示
//                            bean.shareAccount = "what";
//                        }
                        //已经展示的列表
                        List<DeviceBean> vList = getView().getDeviceList();
                        //新列表
                        final int index = vList == null ? -1 : vList.indexOf(bean);
                        if (MiscUtils.isInRange(0, 1, index)) {
                            //更新对应的item
                            vList.set(index, bean);
                            getView().onItemUpdate(index);
                        } else {
                            //a new one
                            List<DeviceBean> newList = new ArrayList<>();
                            newList.add(bean);
                            getView().onItemsInsert(newList);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.ExceptionFun<>("singleDeviceSub"))
                .subscribe();
    }

    private Subscription getTimeTickEventSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.TimeTickEvent.class)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.TimeTickEvent>() {
                    @Override
                    public void call(RxEvent.TimeTickEvent o) {
                        //6:00 am - 17:59 pm
                        //18:00 pm-5:59 am
                        if (getView() != null) {
                            getView().onTimeTick(JFGRules.getTimeRule());
                        }
                    }
                });

    }

    private Subscription getLoginRspSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginRsp.class)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.LoginRsp>() {
                    @Override
                    public void call(RxEvent.LoginRsp o) {
                        if (getView() != null)
                            getView().onLoginState(JCache.isOnline());
                        if (JCache.getAccountCache() != null)
                            getView().onAccountUpdate(JCache.getAccountCache());
                    }
                });
    }

    private Subscription JFGAccountUpdate() {
        return RxBus.getCacheInstance().toObservableSticky(JFGAccount.class)
                .filter(new RxHelper.Filter<>(TAG + "JFGAccountUpdate", (getView() != null && JCache.isOnline())))
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<JFGAccount, Object>() {
                    @Override
                    public Object call(JFGAccount jfgAccount) {
                        getView().onAccountUpdate(jfgAccount);
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("JFGAccount"))
                .subscribe();
    }

    /**
     * 粘性订阅
     *
     * @return
     */
    private Subscription subDeviceList() {
        return RxBus.getCacheInstance().toObservableSticky(RxUiEvent.BulkDeviceListRsp.class)
                .subscribeOn(Schedulers.io())
                .filter((RxUiEvent.BulkDeviceListRsp list) -> (getView() != null && list.allDevices != null))
                .flatMap(new Func1<RxUiEvent.BulkDeviceListRsp, Observable<List<DeviceBean>>>() {
                    @Override
                    public Observable<List<DeviceBean>> call(RxUiEvent.BulkDeviceListRsp list) {
                        AppLogger.i("get devices list: " + list.allDevices);
                        List<DeviceBean> beanList = new ArrayList<>();
                        List<DpMsgDefine.DpWrap> oList = list.allDevices;
                        for (DpMsgDefine.DpWrap wrap : oList) {
                            if (wrap.baseDpDevice == null) continue;
                            DeviceBean bean = new DeviceBean();
                            bean.fillData(wrap.baseDpDevice, wrap.baseDpMsgList);
                            try {
                                Pair<Integer, BaseValue> pair = GlobalDataProxy.getInstance()
                                        .fetchUnreadCount(bean.uuid, DpMsgMap.ID_505_CAMERA_ALARM_MSG);
                                if (pair != null) bean.msgCountPair = pair;
                            } catch (JfgException e) {
                                AppLogger.e("" + e.getLocalizedMessage());
                            }
                            beanList.add(bean);
                        }
                        return Observable.just(beanList);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<List<DeviceBean>, DeviceBean>() {
                    @Override
                    public DeviceBean call(List<DeviceBean> oList) {
                        //新列表
                        ArrayList<DeviceBean> newList = new ArrayList<>();
                        for (DeviceBean bean : oList) {
                            newList.add(bean);
                        }
                        getView().onItemsInsert(null);//清空列表
                        getView().onItemsInsert(newList);
                        return null;
                    }
                })
                .retry(new RxHelper.ExceptionFun<>("subDeviceList:"))
                .subscribe();
    }


    @Override
    public void fetchGreet() {
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, GreetBean>() {
                    @Override
                    public GreetBean call(Object o) {
                        return null;
                    }
                })
                .filter(new RxHelper.Filter<>("", getView() != null && GlobalDataProxy.getInstance().getJfgAccount() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((GreetBean greetBean) -> {
                    getView().onAccountUpdate(JCache.getAccountCache());
                });
    }

    @Override
    public void fetchDeviceList(boolean manually) {
        if (!JCache.isOnline()) {
            getView().onLoginState(false);
            getView().onRefreshFinish();
        }
        Observable.just(manually)
                .subscribeOn(Schedulers.newThread())
                .map((Boolean aBoolean) -> {
                    ArrayList<DeviceBean> aList = aList();
                    if (aList != null) {
                        for (DeviceBean bean : aList)
                            try {
                                GlobalDataProxy.getInstance().fetchUnreadCount(bean.uuid, DpMsgMap.ID_505_CAMERA_ALARM_MSG);
                            } catch (JfgException e) {
                                AppLogger.e("" + e.getLocalizedMessage());
                            }
                    }
                    RxBus.getCacheInstance().post(new RxUiEvent.BulkDeviceListReq());
                    Log.d(TAG, "fetchDeviceList fetchDeviceList");
                    return null;
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object aBoolean) -> {
                    if (getView() != null) getView().onRefreshFinish();
                });
    }

    private ArrayList<DeviceBean> aList() {
        if (getView() == null) return null;
        return getView().getDeviceList();
    }

    @Override
    public void deleteItem(DeviceBean deviceBean) {

    }

    @Override
    public void registerWorker() {
        initTimeTickBroadcast();
    }

    @Override
    public void unRegisterWorker() {
        Context context = ContextUtils.getContext();
        if (timeTickBroadcast != null && context != null) {
            context.unregisterReceiver(timeTickBroadcast);
            timeTickBroadcast = null;
        }
    }

    private void initTimeTickBroadcast() {
        timeTickBroadcast = new TimeTickBroadcast();
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        getView().getContext()
                .getApplicationContext()
                .registerReceiver(timeTickBroadcast, filter);
    }

}