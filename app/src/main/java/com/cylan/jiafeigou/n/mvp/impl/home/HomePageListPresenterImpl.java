package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.br.TimeTickBroadcast;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.GreetBean;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
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
                subUuidList(),
                sdcardStatusSub(),
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
                .subscribe((String s) -> {
                    if (!RxBus.getCacheInstance().hasStickyEvent(RxUiEvent.BulkDeviceListRsp.class)) {
                        RxBus.getCacheInstance().post(new RxUiEvent.BulkUUidListReq());
                        Log.d(TAG, "getDevicesList getDevicesList");
                    }
                });
    }

    /**
     * sd卡状态更新
     *
     * @return
     */
    private Subscription sdcardStatusSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DataPoolUpdate.class)
                .filter((RxEvent.DataPoolUpdate data) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.DataPoolUpdate, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.DataPoolUpdate update) {
                        if (update.id == DpMsgMap.ID_204_SDCARD_STORAGE) {
                            DpMsgDefine.DPSdStatus sdStatus = (DpMsgDefine.DPSdStatus) update.value.getValue();
                        } else if (update.id == DpMsgMap.ID_222_SDCARD_SUMMARY) {
                            DpMsgDefine.DPSdcardSummary sdcardSummary = (DpMsgDefine.DPSdcardSummary) update.value.getValue();
                        } else if (update.id == DpMsgMap.ID_201_NET) {
                            DpMsgDefine.DPNet net = (DpMsgDefine.DPNet) update.value.getValue();
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("sdcardStatusSub"))
                .subscribe();
    }

    private Subscription getTimeTickEventSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.TimeTickEvent.class)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.TimeTickEvent o) -> {
                    //6:00 am - 17:59 pm
                    //18:00 pm-5:59 am
                    if (getView() != null) {
                        getView().onTimeTick(JFGRules.getTimeRule());

                    }
                });

    }

    private Subscription getLoginRspSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginRsp.class)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.LoginRsp o) -> {
                    if (getView() != null)
                        getView().onLoginState(JCache.isOnline());
                    if (JCache.getAccountCache() != null)
                        getView().onAccountUpdate(JCache.getAccountCache());
                });
    }

    private Subscription JFGAccountUpdate() {
        return RxBus.getCacheInstance().toObservableSticky(JFGAccount.class)
                .filter(new RxHelper.Filter<>(TAG + "JFGAccountUpdate", (getView() != null && JCache.isOnline())))
                .observeOn(AndroidSchedulers.mainThread())
                .map((JFGAccount jfgAccount) -> {
                    getView().onAccountUpdate(jfgAccount);
                    return null;
                })
                .retry(new RxHelper.RxException<>("JFGAccount"))
                .subscribe();
    }

    /**
     * 粘性订阅
     *
     * @return
     */
    private Subscription subUuidList() {
        List<JFGDevice> deviceList = GlobalDataProxy.getInstance().fetchAll();
        if (deviceList != null) {
            ArrayList<String> uuidList = new ArrayList<>();
            for (JFGDevice device : deviceList) {
                uuidList.add(device.uuid);
            }
            getView().onItemsInsert(uuidList);
        }
        return RxBus.getCacheInstance().toObservableSticky(RxUiEvent.BulkUUidListRsp.class)
                .subscribeOn(Schedulers.io())
                .filter((RxUiEvent.BulkUUidListRsp list) -> (getView() != null && list.allList != null))
                .flatMap(new Func1<RxUiEvent.BulkUUidListRsp, Observable<List<String>>>() {
                    @Override
                    public Observable<List<String>> call(RxUiEvent.BulkUUidListRsp list) {
                        AppLogger.i("get devices list: " + list.allList);
                        return Observable.just(list.allList);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<List<String>, String>() {
                    @Override
                    public String call(List<String> oList) {
                        //新列表
                        getView().onItemsInsert(null);//清空列表
                        getView().onItemsInsert(oList);
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
        if (!DataSourceManager.getInstance().isOnline()) {
            getView().onLoginState(false);
            getView().onRefreshFinish();
        }
        Observable.just(manually)
                .subscribeOn(Schedulers.newThread())
                .map((Boolean aBoolean) -> {
                    ArrayList<String> aList = aList();
                    if (aList != null) {
                        for (String uuid : aList)
                            try {
                                GlobalDataProxy.getInstance().fetchUnreadCount(uuid, DpMsgMap.ID_505_CAMERA_ALARM_MSG);
                            } catch (JfgException e) {
                                AppLogger.e("" + e.getLocalizedMessage());
                            }
                    }
                    RxBus.getCacheInstance().post(new RxUiEvent.BulkUUidListReq());
                    Log.d(TAG, "fetchDeviceList fetchDeviceList");
                    return null;
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object aBoolean) -> {
                    if (getView() != null) getView().onRefreshFinish();
                });
    }

    private ArrayList<String> aList() {
        if (getView() == null) return null;
        return getView().getUuidList();
    }

    @Override
    public void deleteItem(String uuid) {

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