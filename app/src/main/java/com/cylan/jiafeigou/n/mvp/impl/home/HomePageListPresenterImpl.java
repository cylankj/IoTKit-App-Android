package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.module.BaseForwardHelper;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.ApFilter;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.HomePageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
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
import rx.subscriptions.CompositeSubscription;

/**
 * Created by hunt on 16-5-23.
 */
public class HomePageListPresenterImpl extends AbstractPresenter<HomePageListContract.View>
        implements HomePageListContract.Presenter {
    private CompositeSubscription recordSub;

    public HomePageListPresenterImpl(HomePageListContract.View view) {
        super(view);
        view.setPresenter(this);
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
                checkNetSub(),
                deviceRecordStateSub()
        };
    }

    private void initDeviceRecordState() {
        List<Device> device = DataSourceManager.getInstance().getAllDevice();
        String uuid = null;
        boolean apDirect = false;
        if (recordSub != null && recordSub.isUnsubscribed()) {
            recordSub.unsubscribe();
        }
        recordSub = new CompositeSubscription();
        if (device != null) {
            for (Device device1 : device) {
                if (device1 == null) continue;//可能
                apDirect = JFGRules.isAPDirect(device1.uuid, device1.$(DpMsgMap.ID_202_MAC, ""));
                if (apDirect) {
                    uuid = device1.uuid;
                    break;
                }
            }
            if (apDirect) {
                BasePanoramaApiHelper.getInstance().init(uuid);
                Subscription subscribe = BasePanoramaApiHelper.getInstance().getRecStatus().subscribe(ret -> {
                    if (recordSub != null && recordSub.isUnsubscribed()) {
                        recordSub.unsubscribe();
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
                recordSub.add(subscribe);
            } else {
                for (Device device1 : device) {
                    if (device1 == null) continue;//可能
                    if (JFGRules.isPan720(device1.pid)) {
                        Subscription subscribe = BaseForwardHelper.getInstance().sendForward(device1.uuid, 13, null).subscribe(ret -> {
                            if (recordSub != null && recordSub.isUnsubscribed()) {
                                recordSub.unsubscribe();
                            }
                        }, e -> {
                            AppLogger.e(e.getMessage());
                        });
                        recordSub.add(subscribe);
                    }
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

    private Subscription checkNetSub() {
        return Observable.interval(4, TimeUnit.SECONDS)
                .observeOn(Schedulers.newThread())
                .map(aLong -> {
                    //优先check online
                    return NetUtils.isPublicNetwork() || ApFilter.isApNet();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .subscribe(ret ->
                        mView.onNetworkChanged(ret), AppLogger::e);
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
                .subscribe(ret -> {
                }, throwable -> {
                    addSubscription(devicesUpdate());
                    AppLogger.e("err:" + MiscUtils.getErr(throwable));
                });
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
                .subscribe(ret -> {
                }, throwable -> {
                    addSubscription(devicesUpdate1());
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
                .subscribe(ret -> {
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
        Log.d("subUuidList", "subUuidList?" + ListUtils.getSize(list));
        getView().onItemsRsp(list);
        getView().onAccountUpdate(BaseApplication.getAppComponent().getSourceManager().getJFGAccount());
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
                .subscribe(msg -> {
                    try {
                        //刷新就对了
                        RxBus.getCacheInstance().post(new InternalHelp());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    private Subscription internalUpdateUuidList() {
        return RxBus.getCacheInstance().toObservable(InternalHelp.class)
                .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .map(o -> {
                    subUuidList();
                    AppLogger.d("get list");
                    return null;
                })
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
//                .doOnCompleted(this::subUuidList)
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(refreshSub);
    }

    private Subscription refreshSub;

    @Override
    public void fetchDeviceList(boolean manually) {
        int state = BaseApplication.getAppComponent().getSourceManager().getLoginState();
        if (state != LogState.STATE_ACCOUNT_ON) {
            getView().onLoginState(false);
        }
        if (refreshSub != null && !refreshSub.isUnsubscribed())
            return;
        refreshSub = Observable.just(manually)
                .subscribeOn(Schedulers.newThread())
                .delay(1, TimeUnit.SECONDS)
                .map((Boolean aBoolean) -> {
                    if (manually)
                        BaseApplication.getAppComponent().getCmd().refreshDevList();
                    BaseApplication.getAppComponent().getSourceManager().syncAllDevicePropertyManually();
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
        addSubscription(Observable.just("go")
                .subscribeOn(Schedulers.newThread())
                .delay(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .subscribe(ret -> mView.onRefreshFinish(),
                        throwable -> mView.onRefreshFinish()), "30s_timeout");
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{WifiManager.NETWORK_STATE_CHANGED_ACTION, ConnectivityManager.CONNECTIVITY_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            updateConnectInfo(null);
        } else if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            RxBus.getCacheInstance().post(new InternalHelp());
            initDeviceRecordState();
        }
    }

    /**
     * 可能连上其他非 'DOG-xxx'
     *
     * @param networkInfo
     */
    private void updateConnectInfo(NetworkInfo networkInfo) {
        Observable.just(networkInfo)
                .subscribeOn(Schedulers.newThread())
                .map(aLong -> {
                    return NetUtils.isPublicNetwork() || ApFilter.isApNet();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(v -> getView() != null)
                .subscribe(ret -> getView().onNetworkChanged(ret), AppLogger::e);
    }

    private static final class InternalHelp {
    }

}