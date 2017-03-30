package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.br.TimeTickBroadcast;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ListUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
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
        return new Subscription[]{
                getShareDevicesListRsp(),
                devicesUpdate(),
                internalUpdateUuidList(),
                devicesUpdate1(),
                robotDeviceDataSync(),
                JFGAccountUpdate(),
                unreadCountUpdate(),
        };
    }

    private Subscription unreadCountUpdate() {
        return RxBus.getCacheInstance().toObservable(RxEvent.UnreadCount.class)
                .onBackpressureBuffer()
                .map(unreadCount -> {
                    RxBus.getCacheInstance().post(new InternalHelp());
                    return null;
                })
                .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                .subscribe();
    }

    private Subscription getShareDevicesListRsp() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListRsp.class)
                .subscribeOn(Schedulers.newThread())
                .last()
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new RxHelper.Filter<>("getShareDevicesListRsp:", getView() != null))
                .subscribe((RxEvent.GetShareListRsp getShareListRsp) -> {
                    RxBus.getCacheInstance().post(new InternalHelp());
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
    private Subscription devicesUpdate() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .filter((RobotoGetDataRsp data) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .map(update -> {
                    RxBus.getCacheInstance().post(new InternalHelp());
                    AppLogger.d("data pool update: " + update);
                    return null;
                })
                .retry(new RxHelper.RxException<>("devicesUpdate"))
                .subscribe();
    }

    private Subscription devicesUpdate1() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceListRsp.class)
                .filter((RxEvent.DeviceListRsp data) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .map(update -> {
                    RxBus.getCacheInstance().post(new InternalHelp());
                    AppLogger.d("data pool update: " + update);
                    return null;
                })
                .retry(new RxHelper.RxException<>("devicesUpdate"))
                .subscribe();
    }

    @Override
    protected boolean registerTimeTick() {
        return true;
    }

    @Override
    protected void onTimeTick() {
        if (mView != null) {
            Observable.just("timeTick")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        //6:00 am - 17:59 pm
                        //18:00 pm-5:59 am
                        if (getView() != null) {
                            getView().onTimeTick(JFGRules.getTimeRule());
                            AppLogger.i("time tick");
                        }
                    }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
        }
    }

    private Subscription JFGAccountUpdate() {
        return RxBus.getCacheInstance().toObservable(JFGAccount.class)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(jfgAccount -> {
                    getView().onAccountUpdate(jfgAccount);
                    RxBus.getCacheInstance().post(new InternalHelp());
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
        getView().onItemsInsert(DataSourceManager.getInstance().getAllJFGDevice());
        getView().onAccountUpdate(DataSourceManager.getInstance().getJFGAccount());
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDeviceDataSync() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .subscribeOn(Schedulers.newThread())
                .filter(jfgRobotSyncData -> (ListUtils.getSize(jfgRobotSyncData.dpList) > 0 && getView() != null))
                .flatMap(ret -> Observable.from(ret.dpList))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .subscribe(msg -> {
                    try {
                        if (msg.id == 508 //待机
                                || msg.id == 201//网络
                                || msg.id == 501 //安全防护
                                || msg.id == 223) {//3g
                            RxBus.getCacheInstance().post(new InternalHelp());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private Subscription internalUpdateUuidList() {
        return RxBus.getCacheInstance().toObservable(InternalHelp.class)
                .observeOn(Schedulers.newThread())
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(o -> {
                    subUuidList();
                    AppLogger.d("get list");
                    return null;
                })
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .doOnCompleted(this::subUuidList)
                .subscribe();
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(refreshSub);
    }

    private Subscription refreshSub;

    @Override
    public void fetchDeviceList(boolean manually) {
        int state = DataSourceManager.getInstance().getLoginState();
        if (state != LogState.STATE_ACCOUNT_ON) {
            getView().onLoginState(false);
        }
        if (refreshSub != null && !refreshSub.isUnsubscribed())
            return;
        refreshSub = Observable.just(manually)
                .subscribeOn(Schedulers.newThread())
                .map((Boolean aBoolean) -> {
                    DataSourceManager.getInstance().syncDeviceUnreadCount();
                    if (manually)
                        DataSourceManager.getInstance().syncAllJFGDevicePropertyManually();
                    AppLogger.i("fetchDeviceList: " + aBoolean);
                    return aBoolean;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(aBoolean -> {
                    RxBus.getCacheInstance().post(new InternalHelp());
                    return aBoolean;
                })
                .filter(aBoolean -> aBoolean)//手动刷新，需要停止刷新
                .observeOn(Schedulers.newThread())
                .delay(3, TimeUnit.SECONDS)
                .filter(aBoolean -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Object aBoolean) -> getView().onRefreshFinish(),
                        throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{WifiManager.NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            updateConnectInfo(info);
        }
    }

    /**
     * 可能连上其他非 'DOG-xxx'
     *
     * @param networkInfo
     */
    private void updateConnectInfo(NetworkInfo networkInfo) {
        Observable.just(networkInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(v -> getView() != null)
                .subscribe((NetworkInfo info) -> getView().onNetworkChanged(info != null && info.isConnected()));
    }
    //    @Override
//    public void unBindDevReq(String uuid) {
//        addSubscription(Observable.just(null)
//                .subscribeOn(Schedulers.newThread())
//                .map((Object o) -> {
//                    boolean result = DataSourceManager.getInstance().delRemoteJFGDevice(uuid);
//                    AppLogger.i("unbind uuid: " + uuid + " " + result);
//                    return null;
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .zipWith(RxBus.getCacheInstance().toObservable(RxEvent.UnBindDeviceEvent.class)
//                                .subscribeOn(Schedulers.newThread())
//                                .timeout(3000, TimeUnit.MILLISECONDS, Observable.just("unbind timeout")
//                                        .subscribeOn(AndroidSchedulers.mainThread())
//                                        .map(s -> {
////                                            getView().unBindDeviceRsp(-1);
//                                            return null;
//                                        }))
//                                .filter(s -> getView() != null)
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .filter(unbindEvent -> {
//                                    if (unbindEvent.jfgResult.code != 0)
//                                        getView().unBindDeviceRsp(unbindEvent.jfgResult.code);//失败
//                                    return unbindEvent.jfgResult.code == 0;
//                                }),
//                        (Object o, RxEvent.UnBindDeviceEvent unbindEvent) -> {
//                            getView().unBindDeviceRsp(0);//成功
//                            DataSourceManager.getInstance().delLocalJFGDevice(uuid);
//                            return null;
//                        })
//                .subscribe());
//    }

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

    private static final class InternalHelp {
    }

}