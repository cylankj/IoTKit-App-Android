package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
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
                sdcardStatusSub(),
                JFGAccountUpdate()};
    }

    private Subscription getShareDevicesListRsp() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new RxHelper.Filter<>("getShareDevicesListRsp:", getView() != null))
                .subscribe((RxEvent.GetShareListRsp getShareListRsp) -> {
                    getView().onItemsInsert(getView().getUuidList());
                    AppLogger.i("shareListRsp");
                }, (Throwable throwable) -> {
                    AppLogger.e("" + throwable.getLocalizedMessage());
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
                            DpMsgDefine.DPSdStatus sdStatus = update.value.getValue();
                        } else if (update.id == DpMsgMap.ID_222_SDCARD_SUMMARY) {
                            DpMsgDefine.DPSdcardSummary sdcardSummary = update.value.getValue();
                        } else if (update.id == DpMsgMap.ID_201_NET) {
                            DpMsgDefine.DPNet net = update.value.getValue();
                        }
                        AppLogger.d("data pool update: " + update);
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
                        AppLogger.i("time tick");
                    }
                });

    }

    private Subscription getLoginRspSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LoginRsp.class)
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.LoginRsp o) -> {
                    JFGAccount account = GlobalDataProxy.getInstance().getJfgAccount();
                    if (account != null && !TextUtils.isEmpty(account.getAccount()))
                        getView().onAccountUpdate(GlobalDataProxy.getInstance().getJfgAccount());
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
        List<JFGDevice> deviceList = GlobalDataProxy.getInstance().fetchAll();
        if (deviceList != null) {
            ArrayList<String> uuidList = new ArrayList<>();
            for (JFGDevice device : deviceList) {
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
                .filter(new RxHelper.Filter<>("", getView() != null && GlobalDataProxy.getInstance().getJfgAccount() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((GreetBean greetBean) -> {
                    getView().onAccountUpdate(GlobalDataProxy.getInstance().getJfgAccount());
                });
    }

    @Override
    public void fetchDeviceList(boolean manually) {
        int state = GlobalDataProxy.getInstance().getLoginState();
        if (state != LogState.STATE_ACCOUNT_ON) {
            getView().onLoginState(false);
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
                    if (aBoolean) {
                        JfgCmdInsurance.getCmd().getShareList(getView().getUuidList());
                    }
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