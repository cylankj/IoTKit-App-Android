package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendListShareDevicesToContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
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
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendListShareDevicesPresenterImp extends AbstractPresenter<MineFriendListShareDevicesToContract.View> implements MineFriendListShareDevicesToContract.Presenter {

    private CompositeSubscription subscription;
    private ArrayList<JFGShareListInfo> hasShareFriendList = new ArrayList<>();
    private ArrayList<DeviceBean> allDevice = new ArrayList<>();
    private ArrayList<RxEvent.ShareDeviceCallBack> callBackList = new ArrayList<>();
    private int totalFriend;
    private String relAndFriendBean;
    private Network network;

    public MineFriendListShareDevicesPresenterImp(String relAndFriendBean,MineFriendListShareDevicesToContract.View view) {
        super(view);
        view.setPresenter(this);
        this.relAndFriendBean = relAndFriendBean;
    }

    @Override
    public void start() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        } else {
            subscription = new CompositeSubscription();
            subscription.add(initDeviceListData());
            subscription.add(getDeviceInfoCallBack());
            subscription.add(shareDeviceCallBack());
        }
        registerNetworkMonitor();
    }

    @Override
    public void stop() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        unregisterNetworkMonitor();
    }

    /**
     * 获取到设备列表的数据
     */
    @Override
    public Subscription initDeviceListData() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.DeviceList.class)
                .flatMap(new Func1<RxEvent.DeviceList, Observable<ArrayList<DeviceBean>>>() {
                    @Override
                    public Observable<ArrayList<DeviceBean>> call(RxEvent.DeviceList deviceList) {
                        if (deviceList == null || deviceList.jfgDevices == null) {
                            return null;
                        }
                        return Observable.just(getShareDeviceList(deviceList));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<DeviceBean>>() {
                    @Override
                    public void call(ArrayList<DeviceBean> deviceList) {
                        if (getView() != null && deviceList != null) {
                            allDevice.clear();
                            allDevice.addAll(deviceList);
                            ArrayList<String> cidList = new ArrayList<String>();
                            for (DeviceBean bean : deviceList) {
                                cidList.add(bean.uuid);
                            }
                            getDeviceInfo(cidList);
                        } else {
                            getView().hideLoadingDialog();
                            getView().showNoDeviceView();
                        }
                    }
                });
    }

    /**
     * 获取到设备已分享的亲友数
     *
     * @param cid
     */
    @Override
    public void getDeviceInfo(ArrayList<String> cid) {
        rx.Observable.just(cid)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<ArrayList<String>>() {
                    @Override
                    public void call(ArrayList<String> cid) {
                        if (cid != null && cid.size() != 0) {
                            JfgCmdInsurance.getCmd().getShareList(cid);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getDeviceInfo" + throwable.getLocalizedMessage());
                    }
                });
    }


    /**
     * 发送分享设备给亲友的请求
     */
    @Override
    public void sendShareToReq(ArrayList<DeviceBean> chooseList, final RelAndFriendBean friendBean) {
        totalFriend = chooseList.size();
        if (getView() != null) {
            getView().showSendReqProgress();
        }
        rx.Observable.just(chooseList)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<ArrayList<DeviceBean>>() {
                    @Override
                    public void call(ArrayList<DeviceBean> deviceBeen) {
                        for (DeviceBean bean : deviceBeen) {
                            try {
                                JfgCmdInsurance.getCmd().shareDevice(bean.uuid, friendBean.account);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e(throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 是否有勾选中的设置按钮状态
     *
     * @param list
     */
    @Override
    public void checkIsChoose(ArrayList<DeviceBean> list) {
        if (list.size() == 0) {
            getView().hideFinishBtn();
        } else {
            getView().showFinishBtn();
        }
    }

    /**
     * 分享设备的回调
     *
     * @return
     */
    @Override
    public Subscription shareDeviceCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ShareDeviceCallBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ShareDeviceCallBack>() {
                    @Override
                    public void call(RxEvent.ShareDeviceCallBack shareDeviceCallBack) {

                        if (shareDeviceCallBack != null && shareDeviceCallBack instanceof RxEvent.ShareDeviceCallBack) {
                            callBackList.add(shareDeviceCallBack);
                        }

                        if (callBackList.size() == totalFriend) {
                            if (getView() != null) {
                                getView().hideSendReqProgress();
                                getView().showSendReqFinishReuslt(callBackList);
                            }
                        }
                    }
                });
    }

    /**
     * desc:处理设备分享的数据
     */
    private void handlerShareDeviceListData(ArrayList<DeviceBean> shareDeviceList) {
        if (shareDeviceList != null && shareDeviceList.size() != 0) {
            if (getView() != null) {
                getView().hideNoDeviceView();
                getView().initRecycleView(shareDeviceList);
            }
        } else {
            if (getView() != null) {
                getView().hideLoadingDialog();
                getView().showNoDeviceView();
            }
        }
    }

    /**
     * desc:获取到分享设备的list集合数据
     *
     * @param shareDeviceList
     */
    private ArrayList<DeviceBean> getShareDeviceList(RxEvent.DeviceList shareDeviceList) {

        ArrayList<DeviceBean> list = new ArrayList<>();

        for (JFGDevice info : shareDeviceList.jfgDevices) {
            DeviceBean bean = new DeviceBean();
            bean.alias = info.alias;
            bean.pid = info.pid;
            bean.uuid = info.uuid;
            bean.shareAccount = info.shareAccount;
            bean.sn = info.sn;
            list.add(bean);
        }
        return list;
    }


    /**
     * 获取到已经分享的亲友数的回调
     *
     * @return
     */
    @Override
    public Subscription getDeviceInfoCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListCallBack.class)
                .flatMap(new Func1<RxEvent.GetShareListCallBack, Observable<ArrayList<DeviceBean>>>() {
                    @Override
                    public Observable<ArrayList<DeviceBean>> call(RxEvent.GetShareListCallBack getShareListCallBack) {
                        if (getShareListCallBack != null && getShareListCallBack instanceof RxEvent.GetShareListCallBack) {
                            if (getShareListCallBack.i == 0 && getShareListCallBack.arrayList.size() != 0) {
                                //每个设备已分享的亲友集合
                                hasShareFriendList.clear();
                                hasShareFriendList.addAll(getShareListCallBack.arrayList);
                                //该设备以分享的亲友数赋值
                                for (int i = allDevice.size()-1; i >= 0 ; i--) {
                                    if (allDevice.get(i).uuid.equals(getShareListCallBack.arrayList.get(i).cid)) {
                                        allDevice.get(i).hasShareCount = getShareListCallBack.arrayList.get(i).friends.size();
                                        for (int j = getShareListCallBack.arrayList.get(i).friends.size()-1;j >= 0;j--){
                                            if (getShareListCallBack.arrayList.get(i).friends.get(j).account.equals(relAndFriendBean)){
                                                allDevice.remove(allDevice.get(i));
                                            }
                                        }
                                    }
                                }
                                return Observable.just(allDevice);
                            } else {
                                return Observable.just(allDevice);
                            }
                        } else {
                            return Observable.just(allDevice);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<DeviceBean>>() {
                    @Override
                    public void call(ArrayList<DeviceBean> deviceBeen) {
                        handlerShareDeviceListData(deviceBeen);
                    }
                });
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
            AppLogger.e("registerNetworkMonitor"+e.getLocalizedMessage());
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
                });
    }


}
