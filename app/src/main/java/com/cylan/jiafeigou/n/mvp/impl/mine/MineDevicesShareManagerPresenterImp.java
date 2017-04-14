package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineDevicesShareManagerContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public class MineDevicesShareManagerPresenterImp extends AbstractPresenter<MineDevicesShareManagerContract.View>
        implements MineDevicesShareManagerContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private ArrayList<RelAndFriendBean> hasShareFriend;
    private Network network;

    public MineDevicesShareManagerPresenterImp(MineDevicesShareManagerContract.View view, ArrayList<RelAndFriendBean> hasShareFriend) {
        super(view);
        view.setPresenter(this);
        this.hasShareFriend = hasShareFriend;
    }

    @Override
    public void start() {
        initHasShareListData(hasShareFriend);
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
//          compositeSubscription.add(getHasShareListCallback());
            compositeSubscription.add(cancleShareCallBack());
        }
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        unregisterNetworkMonitor();
    }

    /**
     * 获取已分享的好友列表
     *
     * @param cid
     */
    @Override
    public void getHasShareList(String cid) {
        ArrayList<String> deviceCid = new ArrayList<>();
        deviceCid.add(cid);
        rx.Observable.just(deviceCid)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<ArrayList<String>>() {
                    @Override
                    public void call(ArrayList<String> cid) {
                        BaseApplication.getAppComponent().getCmd().getShareList(cid);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getHasShareList" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到已分享好友的回调
     *
     * @return
     */
    @Override
    public Subscription getHasShareListCallback() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListCallBack.class)
                .flatMap(new Func1<RxEvent.GetShareListCallBack, Observable<ArrayList<RelAndFriendBean>>>() {
                    @Override
                    public Observable<ArrayList<RelAndFriendBean>> call(RxEvent.GetShareListCallBack getShareListCallBack) {
                        if (getShareListCallBack != null && getShareListCallBack instanceof RxEvent.GetShareListCallBack) {
                            return Observable.just(converData(getShareListCallBack.arrayList.get(0).friends));
                        } else {
                            return Observable.just(null);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<RelAndFriendBean>>() {
                    @Override
                    public void call(ArrayList<RelAndFriendBean> list) {
                        if (list != null && list.size() > 0) {
                            initHasShareListData(list);
                        } else {
                            getView().showNoHasShareFriendNullView();
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 将数据装换
     */
    private ArrayList<RelAndFriendBean> converData(ArrayList<JFGFriendAccount> friendList) {
        ArrayList<RelAndFriendBean> list = new ArrayList<>();
        for (JFGFriendAccount friendBean : friendList) {
            RelAndFriendBean tempBean = new RelAndFriendBean();
            tempBean.account = friendBean.account;
            tempBean.alias = friendBean.alias;
            tempBean.markName = friendBean.markName;
            list.add(tempBean);
        }
        return list;
    }

    @Override
    public void initHasShareListData(ArrayList<RelAndFriendBean> shareDeviceFriendlist) {
        if (getView() != null && shareDeviceFriendlist != null && shareDeviceFriendlist.size() != 0) {
            getView().showHasShareListTitle();
            getView().initHasShareFriendRecyView(shareDeviceFriendlist);
        } else {
            getView().hideHasShareListTitle();
            getView().showNoHasShareFriendNullView();
        }
    }

    /**
     * 取消分享设备
     *
     * @param cid
     * @param bean
     */
    @Override
    public void cancleShare(final String cid, final RelAndFriendBean bean) {
        if (getView() != null) {
            getView().showCancleShareProgress();
        }
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            BaseApplication.getAppComponent().getCmd().unShareDevice(cid, bean.account);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("cancleShare" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 取消分享的回调
     *
     * @return
     */
    @Override
    public Subscription cancleShareCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.UnshareDeviceCallBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.UnshareDeviceCallBack>() {
                    @Override
                    public void call(RxEvent.UnshareDeviceCallBack unshareDeviceCallBack) {
                        if (unshareDeviceCallBack != null && unshareDeviceCallBack instanceof RxEvent.UnshareDeviceCallBack) {
                            handlerUnShareCallback(unshareDeviceCallBack);
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 取消分享回调的处理
     *
     * @param unshareDeviceCallBack
     */
    private void handlerUnShareCallback(RxEvent.UnshareDeviceCallBack unshareDeviceCallBack) {
        if (getView() != null) {
            getView().hideCancleShareProgress();
            getView().showUnShareResult(unshareDeviceCallBack);
        }
    }

    @Override
    public void registerNetworkMonitor() {
        try {
            if (network == null) {
                network = new Network();
                final IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                ContextUtils.getContext().registerReceiver(network, filter);
            }
        } catch (Exception e) {
            AppLogger.e("registerNetworkMonitor" + e.getLocalizedMessage());
        }
    }

    @Override
    public void unregisterNetworkMonitor() {
        if (network != null) {
            ContextUtils.getContext().unregisterReceiver(network);
            network = null;
        }
    }

    /**
     * 监听网络状态
     */
    private class Network extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
                updateConnectivityStatus(status.state);
            }
        }
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        getView().onNetStateChanged(integer);
                    }
                }, AppLogger::e);
    }

}
