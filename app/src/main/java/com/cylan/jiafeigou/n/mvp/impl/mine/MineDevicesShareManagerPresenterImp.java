package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineDevicesShareManagerContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ListUtils;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public class MineDevicesShareManagerPresenterImp extends AbstractPresenter<MineDevicesShareManagerContract.View>
        implements MineDevicesShareManagerContract.Presenter {


    public MineDevicesShareManagerPresenterImp(MineDevicesShareManagerContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{cancelShareCallBack()};
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
                .subscribe(cidList -> BaseApplication.getAppComponent().getCmd().getShareList(cidList),
                        AppLogger::e);
    }

    /**
     * 获取到已分享好友的回调
     *
     * @return
     */
    @Override
    public Subscription getHasShareListCallback() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListRsp.class)
                .flatMap(new Func1<RxEvent.GetShareListRsp, Observable<ArrayList<FriendBean>>>() {
                    @Override
                    public Observable<ArrayList<FriendBean>> call(RxEvent.GetShareListRsp getShareListCallBack) {
                        ArrayList<JFGShareListInfo> list =
                                BaseApplication.getAppComponent().getSourceManager().getShareList();
                        if (ListUtils.isEmpty(list)) return Observable.just(null);
                        return Observable.just(convertData(list));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list != null && list.size() > 0) {
                        initHasShareListData(list);
                    } else {
                        getView().showNoHasShareFriendNullView();
                    }
                }, AppLogger::e);
    }

    /**
     * 将数据装换
     */
    private ArrayList<FriendBean> convertData(ArrayList<JFGShareListInfo> friendList) {
        ArrayList<FriendBean> list = new ArrayList<>();
        if (ListUtils.isEmpty(friendList)) return list;
        for (JFGShareListInfo info : friendList) {
            for (JFGFriendAccount friendBean : info.friends) {
                FriendBean tempBean = new FriendBean();
                tempBean.account = friendBean.account;
                tempBean.alias = friendBean.alias;
                tempBean.markName = friendBean.markName;
                list.add(tempBean);
            }
        }
        return list;
    }

    @Override
    public void initHasShareListData(ArrayList<FriendBean> shareDeviceFriendlist) {
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
    public void cancelShare(final String cid, final FriendBean bean) {
        if (getView() != null) {
            getView().showCancleShareProgress();
        }
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    try {
                        AppLogger.e("正在取消分享:" + bean.account);
                        BaseApplication.getAppComponent().getCmd().unShareDevice(cid, bean.account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.e("cancelShare" + throwable.getLocalizedMessage()));
    }

    /**
     * 取消分享的回调
     *
     * @return
     */
    @Override
    public Subscription cancelShareCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.UnShareDeviceCallBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handlerUnShareCallback, AppLogger::e);
    }

    /**
     * 取消分享回调的处理
     *
     * @param unshareDeviceCallBack
     */
    private void handlerUnShareCallback(RxEvent.UnShareDeviceCallBack unshareDeviceCallBack) {
        if (getView() != null) {
            getView().hideCancleShareProgress();
            getView().showUnShareResult(unshareDeviceCallBack);
        }
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION, WifiManager.NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        final String action = intent.getAction();
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
            updateConnectivityStatus(status.state);
        }
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    getView().onNetStateChanged(integer);
                }, AppLogger::e);
    }

}
