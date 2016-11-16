package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class MineShareDevicePresenterImp extends AbstractPresenter<MineShareDeviceContract.View> implements MineShareDeviceContract.Presenter {

    private ArrayList<JFGShareListInfo> hasShareFriendList;
    private CompositeSubscription subscription;

    public MineShareDevicePresenterImp(MineShareDeviceContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        } else {
            subscription = new CompositeSubscription();
            subscription.add(initData());
        }
    }

    @Override
    public void stop() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    public Subscription initData() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.DeviceList.class)
                .flatMap(new Func1<RxEvent.DeviceList, Observable<ArrayList<DeviceBean>>>() {
                    @Override
                    public Observable<ArrayList<DeviceBean>> call(RxEvent.DeviceList deviceList) {
                        if (deviceList == null || deviceList.jfgDevices == null){
                            return null;
                        }
                        return Observable.just(getShareDeviceList(deviceList));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<DeviceBean>>() {
                    @Override
                    public void call(ArrayList<DeviceBean> deviceList) {
                        handlerShareDeviceListData(deviceList);
                    }
                });
    }

    @Override
    public JFGShareListInfo getJFGInfo(int position) {
        return hasShareFriendList.get(position);
    }

    /**
     * desc:处理设备分享的数据
     */
    private void handlerShareDeviceListData(ArrayList<DeviceBean> shareDeviceList) {
        if (shareDeviceList != null && shareDeviceList.size() != 0) {
            //hasShareFriendList = shareDeviceList.jfgDevices;
            if (getView() != null) {
                getView().initRecycleView(shareDeviceList);
            }
        } else {
            if (getView() != null) {
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


    @Override
    public ArrayList<RelAndFriendBean> getHasShareRelAndFriendList(JFGShareListInfo info) {

        ArrayList<RelAndFriendBean> list = new ArrayList<>();

        for (JFGFriendAccount account : info.friends) {
            RelAndFriendBean bean = new RelAndFriendBean();
            bean.account = account.account;
            bean.alias = account.alias;
            //TODO 具体赋值
            list.add(bean);
        }
        return list;
    }


}
