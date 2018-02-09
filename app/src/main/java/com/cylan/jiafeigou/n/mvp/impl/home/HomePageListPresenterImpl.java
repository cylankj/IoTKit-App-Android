package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.BaseForwardHelper;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.LoginHelper;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.List;
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

    public HomePageListPresenterImpl(HomePageListContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
        getShareDevicesListRsp();
        devicesUpdate();
        robotDeviceDataSync();
        JFGAccountUpdate();
        deviceRecordStateSub();
        deviceUnbindSub();
        timeCheckerSub();
        monitorRefreshEvent();
    }

    private void monitorRefreshEvent() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RequiredRefreshEvent.class)
                .throttleLast(1000, TimeUnit.MILLISECONDS)
                .retry()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(requiredRefreshEvent -> {
                    subUuidList();
                }, error -> {
                    AppLogger.e(MiscUtils.getErr(error));
                    monitorRefreshEvent();
                });
        addStopSubscription(subscribe);
    }

    private static class RequiredRefreshEvent {
        public static RequiredRefreshEvent INSTANCE = new RequiredRefreshEvent();
    }

    private void timeCheckerSub() {
        //这个 timeChecker 是定时任务,用来定时更新主页的消息时间的,因为如果刚刚来了一条消息,我们把他标记为刚刚
        //过了好久没有收到同步消息,这时如果没有定时任务,主页还会显示刚刚,这就不正确了,所以加入了定时任务,来定时
        //更新主页的时间
        Subscription subscribe = Observable.interval(5, TimeUnit.MINUTES)
                .retry()
                .subscribe(aLong -> RxBus.getCacheInstance().post(RequiredRefreshEvent.INSTANCE), throwable -> {
                    timeCheckerSub();
                });
        addStopSubscription(subscribe);
    }

    private void initDeviceRecordState() {
        List<Device> device = DataSourceManager.getInstance().getAllDevice();
        if (device != null) {
            for (Device device1 : device) {
                if (device1 == null) {
                    continue;//可能
                }
                if (JFGRules.isPan720(device1.pid) && JFGRules.isDeviceOnline(device1.uuid)) {//只有在线才发消息,否则没有意义
                    Subscription subscribe = BaseForwardHelper.getInstance().sendForward(device1.uuid, 13, null).subscribe(ret -> {
                    }, e -> AppLogger.e(e.getMessage()));
                    addSubscription("deviceRecordMonitor", subscribe);
                }
            }
        }
    }

    private void deviceRecordStateSub() {
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.DeviceRecordStateChanged.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (getView() != null) {
                        getView().onRefreshDeviceList();
                    }
                }, error -> {
                    AppLogger.e(MiscUtils.getErr(error));
                });
        addStopSubscription(subscribe);
    }

    private void deviceUnbindSub() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DeviceUnBindedEvent.class)
                .retry()
                .subscribe(event -> RxBus.getCacheInstance().post(RequiredRefreshEvent.INSTANCE), e -> {
                    e.printStackTrace();
                    AppLogger.e(e);
                    deviceUnbindSub();
                });
        addStopSubscription(subscribe);
    }

    private void getShareDevicesListRsp() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.GetShareListRsp.class)
                .subscribe((RxEvent.GetShareListRsp getShareListRsp) -> {
                    RxBus.getCacheInstance().post(RequiredRefreshEvent.INSTANCE);
                    AppLogger.w("shareListRsp");
                }, (Throwable throwable) -> {
                    AppLogger.e("" + throwable.getLocalizedMessage());
                });
        addStopSubscription(subscribe);
    }

    /**
     * sd卡状态更新
     *
     * @return
     */
    private void devicesUpdate() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DevicesArrived.class)
                .subscribe(ret -> RxBus.getCacheInstance().post(RequiredRefreshEvent.INSTANCE), throwable -> {
                    devicesUpdate();
                    AppLogger.e("err:" + MiscUtils.getErr(throwable));
                });
        addStopSubscription(subscribe);
    }

    @Override
    protected boolean registerTimeTick() {
        return true;
    }

    @Override
    protected void onTimeTick() {
        if (mView != null) {
            getView().onTimeTick(JFGRules.getTimeRule());
        }
    }

    private void JFGAccountUpdate() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jfgAccount -> {
                    getView().onAccountUpdate(jfgAccount.jfgAccount);
                }, throwable -> {
                    JFGAccountUpdate();
                    AppLogger.e("err:" + MiscUtils.getErr(throwable));
                });
        addStopSubscription(subscribe);
    }

    /**
     * 粘性订阅
     *
     * @return
     */


    private void subUuidList() {
        List<Device> list = DataSourceManager.getInstance().getAllDevice();
        AppLogger.w("subUuidList?" + ListUtils.getSize(list));
        getView().onItemsRsp(list);
        getView().onAccountUpdate(DataSourceManager.getInstance().getJFGAccount());
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private void robotDeviceDataSync() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> RxBus.getCacheInstance().post(RequiredRefreshEvent.INSTANCE), error -> {
                    AppLogger.e(MiscUtils.getErr(error));
                    robotDeviceDataSync();
                });
        addStopSubscription(subscribe);
    }

    @Override
    public void fetchDeviceList(boolean manually) {
        addSubscription(Schedulers.io().createWorker().schedule(() -> {
            if (manually) {
                Command.getInstance().refreshDevList();
            }
        }), "refresh_manually");
        if (!LoginHelper.isLoginSuccessful()) {
            getView().onLoginState(false);
        }
        addSubscription(AndroidSchedulers.mainThread().createWorker().schedule(() -> {
            mView.onRefreshFinish();
        }, 30, TimeUnit.SECONDS), "refresh_delay");
    }

    @Override
    public void refreshDevices() {
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{WifiManager.NETWORK_STATE_CHANGED_ACTION, ConnectivityManager.CONNECTIVITY_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        getView().onNetworkChanged(NetUtils.getJfgNetType(context) > 0);
        if (TextUtils.equals(action, WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//            updateConnectInfo(null);
        } else if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            RxBus.getCacheInstance().post(RequiredRefreshEvent.INSTANCE);
            initDeviceRecordState();
        }
        WifiInfo wifiInfo = NetUtils.getWifiManager().getConnectionInfo();
        AppLogger.w("网络变化?" + (wifiInfo == null ? null : (wifiInfo.getSupplicantState()) + "," + wifiInfo.getSSID()));
    }
}