package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.BaseForwardHelper;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by hunt on 16-5-23.
 */
public class HomePageListPresenterImpl extends AbstractPresenter<HomePageListContract.View>
        implements HomePageListContract.Presenter {
    private CompositeSubscription recordSub;

    public HomePageListPresenterImpl(HomePageListContract.View view) {
        super(view);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                getShareDevicesListRsp(),
                devicesUpdate(),
                internalUpdateUuidList(),
                robotDeviceDataSync(),
                JFGAccountUpdate(),
                deviceRecordStateSub(),
                deviceUnbindSub()
        };
    }

    private void initDeviceRecordState() {
        List<Device> device = DataSourceManager.getInstance().getAllDevice();
//        String uuid = null;
//        boolean apDirect = false;
        if (recordSub != null && !recordSub.isUnsubscribed()) {
            recordSub.unsubscribe();
        }
        recordSub = new CompositeSubscription();
        if (device != null) {
            for (Device device1 : device) {
                if (device1 == null) {
                    continue;//可能
                }
                if (JFGRules.isPan720(device1.pid) && JFGRules.isDeviceOnline(device1.uuid)) {//只有在线才发消息,否则没有意义
                    Subscription subscribe = BaseForwardHelper.getInstance().sendForward(device1.uuid, 13, null).subscribe(ret -> {
                        if (recordSub != null && !recordSub.isUnsubscribed()) {
                            recordSub.unsubscribe();
                        }
                    }, e -> AppLogger.e(e.getMessage()));
                    recordSub.add(subscribe);
                }
            }
        }
    }

    private Subscription deviceRecordStateSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.DeviceRecordStateChanged.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (getView() != null) {
                        getView().onRefreshDeviceList();
                    }
                });
    }

    private Subscription deviceUnbindSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceUnBindedEvent.class)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(event -> {
                    subUuidList();
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e);
                });
    }

    private Subscription getShareDevicesListRsp() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListRsp.class)
                .debounce(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.GetShareListRsp getShareListRsp) -> {
                    RxBus.getCacheInstance().post(new InternalHelp());
                    AppLogger.w("shareListRsp");
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
        return RxBus.getCacheInstance().toObservable(RxEvent.DevicesArrived.class)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> subUuidList(), throwable -> {
                    addSubscription(devicesUpdate());
                    AppLogger.e("err:" + MiscUtils.getErr(throwable));
                });
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

    private Subscription JFGAccountUpdate() {
        return RxBus.getCacheInstance().toObservable(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(jfgAccount -> {
                    getView().onAccountUpdate(jfgAccount.jfgAccount);
                }, throwable -> {
                    addSubscription(JFGAccountUpdate());
                    AppLogger.e("err:" + MiscUtils.getErr(throwable));
                });
    }

    /**
     * 粘性订阅
     *
     * @return
     */
    private void subUuidList() {
        List<Device> list = BaseApplication.getAppComponent().getSourceManager().getAllDevice();
        AppLogger.w("subUuidList?" + ListUtils.getSize(list));
        getView().onItemsRsp(new ArrayList<>(list));
        getView().onAccountUpdate(BaseApplication.getAppComponent().getSourceManager().getJFGAccount());
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDeviceDataSync() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(msg -> subUuidList(), AppLogger::e);
    }

    private Subscription internalUpdateUuidList() {
        return RxBus.getCacheInstance().toObservable(InternalHelp.class)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> subUuidList(), AppLogger::e);
    }


    @Override
    public void fetchDeviceList(boolean manually) {
        addSubscription(Schedulers.io().createWorker().schedule(() -> {
            if (manually) {
                BaseApplication.getAppComponent().getCmd().refreshDevList();
            }
        }), "refresh_manually");
        int state = BaseApplication.getAppComponent().getSourceManager().getLoginState();
        if (state != LogState.STATE_ACCOUNT_ON) {
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
            RxBus.getCacheInstance().post(new InternalHelp());
            initDeviceRecordState();
        }
        WifiInfo wifiInfo = NetUtils.getWifiManager().getConnectionInfo();
        AppLogger.w("网络变化?" + (wifiInfo == null ? null : (wifiInfo.getSupplicantState()) + "," + wifiInfo.getSSID()));
    }

    private static final class InternalHelp {
    }

}