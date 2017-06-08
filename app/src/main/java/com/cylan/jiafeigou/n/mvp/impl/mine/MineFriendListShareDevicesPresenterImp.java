package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendListShareDevicesToContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendListShareDevicesPresenterImp extends AbstractPresenter<MineFriendListShareDevicesToContract.View> implements MineFriendListShareDevicesToContract.Presenter {

    private ArrayList<JFGShareListInfo> hasShareFriendList = new ArrayList<>();
    private ArrayList<DeviceBean> allDevice = new ArrayList<>();
    private ArrayList<RxEvent.ShareDeviceCallBack> callBackList = new ArrayList<>();
    private int totalFriend;
    private String relAndFriendBean;

    public MineFriendListShareDevicesPresenterImp(String relAndFriendBean, MineFriendListShareDevicesToContract.View view) {
        super(view);
        view.setPresenter(this);
        this.relAndFriendBean = relAndFriendBean;
    }


    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                initDeviceListData(),
                getDeviceInfoCallBack(),
                shareDeviceCallBack()
        };
    }

    /**
     * 获取到设备列表的数据
     */
    @Override
    public Subscription initDeviceListData() {
        return Observable.just(getShareDeviceList())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<DeviceBean>>() {
                    @Override
                    public void call(ArrayList<DeviceBean> deviceList) {
                        if (getView() != null && deviceList != null && deviceList.size() != 0) {
                            allDevice.clear();
                            ArrayList<String> cidList = new ArrayList<String>();
                            for (DeviceBean bean : deviceList) {
                                if (TextUtils.isEmpty(bean.shareAccount)) {
                                    cidList.add(bean.uuid);
                                    allDevice.add(bean);
                                }
                            }
                            getDeviceInfo(cidList);
                        } else {
                            getView().hideLoadingDialog();
                            getView().showNoDeviceView();
                        }
                    }
                }, AppLogger::e);
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
                .filter(ret -> ListUtils.getSize(ret) > 0)
                .subscribe(cidList -> {
                    BaseApplication.getAppComponent().getCmd().getShareList(cidList);
                }, AppLogger::e);
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
                                BaseApplication.getAppComponent().getCmd().shareDevice(bean.uuid, friendBean.account);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, AppLogger::e);
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

                        if (shareDeviceCallBack != null) {
                            callBackList.add(shareDeviceCallBack);
                        }

                        if (callBackList.size() == totalFriend) {
                            if (getView() != null) {
                                getView().hideSendReqProgress();
                                getView().showSendReqFinishReuslt(callBackList);
                            }
                        }
                    }
                }, AppLogger::e);
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
     */
    private ArrayList<DeviceBean> getShareDeviceList() {

        ArrayList<DeviceBean> list = new ArrayList<>();
        List<Device> devices = BaseApplication.getAppComponent().getSourceManager().getAllDevice();
        for (Device info : devices) {
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
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListRsp.class)
                .flatMap(new Func1<RxEvent.GetShareListRsp, Observable<ArrayList<DeviceBean>>>() {
                    @Override
                    public Observable<ArrayList<DeviceBean>> call(RxEvent.GetShareListRsp getShareListCallBack) {
                        ArrayList<JFGShareListInfo> list =
                                BaseApplication.getAppComponent().getSourceManager().getShareList();
                        if (ListUtils.getSize(list) > 0) {
                            //每个设备已分享的亲友集合
                            hasShareFriendList.clear();
                            hasShareFriendList.addAll(list);
                            //该设备以分享的亲友数赋值
                            for (int i = allDevice.size() - 1; i >= 0; i--) {
                                if (allDevice.get(i).uuid.equals(list.get(i).cid)) {
                                    allDevice.get(i).hasShareCount = list.get(i).friends.size();
                                    for (int j = list.get(i).friends.size() - 1; j >= 0; j--) {
                                        if (list.get(i).friends.get(j).account.equals(relAndFriendBean)) {
                                            allDevice.remove(allDevice.get(i));
                                        }
                                    }
                                }
                            }
                            return Observable.just(allDevice);
                        } else {
                            return Observable.just(allDevice);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handlerShareDeviceListData, AppLogger::e);
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        final String action = intent.getAction();
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
            updateConnectivityStatus(status.state);
        }
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{
                WifiManager.NETWORK_STATE_CHANGED_ACTION,
                ConnectivityManager.CONNECTIVITY_ACTION
        };
    }


    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> getView().onNetStateChanged(integer),
                        AppLogger::e);
    }


}
