package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.br.TimeTickBroadcast;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.GreetBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.google.gson.Gson;

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
        registerWorker();
    }

    @Override
    protected Subscription[] register() {
        subUuidList();
        return new Subscription[]{
                getTimeTickEventSub(),
                getLoginRspSub(),
                getShareDevicesListRsp(),
                devicesUpdate(),
                devicesUpdate1(),
                JFGAccountUpdate()};
    }

    private Subscription getShareDevicesListRsp() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListRsp.class)
                .subscribeOn(Schedulers.newThread())
                .delay(500, TimeUnit.MILLISECONDS)//
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new RxHelper.Filter<>("getShareDevicesListRsp:", getView() != null))
                .subscribe((RxEvent.GetShareListRsp getShareListRsp) -> {
                    getView().onItemsInsert(getUuidList());
                    AppLogger.i("shareListRsp");
                }, (Throwable throwable) -> {
                    AppLogger.e("" + throwable.getLocalizedMessage());
                });
    }

    private ArrayList<String> getUuidList() {
        List<JFGDPDevice> devices = DataSourceManager.getInstance().getAllJFGDevice();
        ArrayList<String> arrayList = new ArrayList<>(devices == null ? 0 : devices.size());
        for (JFGDPDevice device : devices) {
            arrayList.add(device.uuid);
        }
        return arrayList;
    }

    /**
     * sd卡状态更新
     *
     * @return
     */
    private Subscription devicesUpdate() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ParseResponseCompleted.class)
                .filter((RxEvent.ParseResponseCompleted data) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.ParseResponseCompleted, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.ParseResponseCompleted update) {
                        subUuidList();
                        AppLogger.d("data pool update: " + update);
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("devicesUpdate"))
                .subscribe();
    }

    private Subscription devicesUpdate1() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceListRsp.class)
                .filter((RxEvent.DeviceListRsp data) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.DeviceListRsp, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.DeviceListRsp update) {
                        subUuidList();
                        AppLogger.d("data pool update: " + update);
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("devicesUpdate"))
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
                        AppLogger.i("time tick");
                    }
                });
    }

    private Subscription getLoginRspSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.OnlineStatusRsp.class)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.OnlineStatusRsp o) -> {
                    JFGAccount account = DataSourceManager.getInstance().getJFGAccount();
                    if (account != null && !TextUtils.isEmpty(account.getAccount()))
                        getView().onAccountUpdate(DataSourceManager.getInstance().getJFGAccount());
                });
    }

    private Subscription JFGAccountUpdate() {
        return RxBus.getCacheInstance().toObservable(JFGAccount.class)
                .observeOn(AndroidSchedulers.mainThread())
                .map((JFGAccount jfgAccount) -> {
                    Log.d("CYLAN_TAG", "onAccountUpdate rsp:" + new Gson().toJson(jfgAccount));
                    getView().onAccountUpdate(jfgAccount);
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(o -> {
                    Log.d("CYLAN_TAG", "JFGAccount rsp:");
                    subUuidList();
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
    private void subUuidList() {
        List<JFGDPDevice> deviceList = DataSourceManager.getInstance().getAllJFGDevice();
        if (deviceList != null) {
            ArrayList<String> uuidList = new ArrayList<>();
            for (JFGDPDevice device : deviceList) {
                uuidList.add(device.uuid);
            }
            getView().onItemsInsert(uuidList);
        }
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
                .filter(new RxHelper.Filter<>("", getView() != null && DataSourceManager.getInstance().getJFGAccount() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((GreetBean greetBean) -> {
                    getView().onAccountUpdate(DataSourceManager.getInstance().getJFGAccount());
                });
    }

    @Override
    public void fetchDeviceList(boolean manually) {
        int state = DataSourceManager.getInstance().getLoginState();
        if (state != LogState.STATE_ACCOUNT_ON) {
            getView().onLoginState(false);
        }
        Observable.just(manually)
                .subscribeOn(Schedulers.newThread())
                .map((Boolean aBoolean) -> {
                    DataSourceManager.getInstance().syncAllJFGDeviceProperty();
                    AppLogger.i("fetchDeviceList: " + aBoolean);
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object aBoolean) -> {
                    subUuidList();
                }, throwable -> {
                    AppLogger.e("err: " + throwable.getLocalizedMessage());
                });
    }

    @Override
    public void deleteItem(String uuid) {
        Observable.just(uuid)
                .subscribeOn(Schedulers.io())
                .subscribe((String o) -> {
                    boolean result = DataSourceManager.getInstance().delJFGDevice(uuid);
                    AppLogger.i("unbind uuid: " + uuid + " " + result);
                }, (Throwable throwable) -> {
                    AppLogger.e("delete uuid failed: " + throwable.getLocalizedMessage());
                });
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