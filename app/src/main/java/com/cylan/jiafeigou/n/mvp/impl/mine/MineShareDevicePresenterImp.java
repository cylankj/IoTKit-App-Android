package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;

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
 * 创建时间：2016/9/5
 * 描述：
 */
public class MineShareDevicePresenterImp extends AbstractPresenter<MineShareDeviceContract.View> implements MineShareDeviceContract.Presenter {

    private ArrayList<JFGShareListInfo> hasShareFriendList;
    private CompositeSubscription subscription;
    private ArrayList<DeviceBean> allDevice = new ArrayList<>();

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
            subscription.add(getDeviceInfoCallBack());
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
                        if (getView() != null && deviceList != null){
                            allDevice.addAll(deviceList);
                            ArrayList<String> cidList = new ArrayList<String>();
                            for (DeviceBean bean:deviceList){
                                cidList.add(bean.uuid);
                            }
                            getDeviceInfo(cidList);
                        }else {
                            getView().showNoDeviceView();
                        }
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

    /**
     * 获取到设备已分享的亲友数
     * @param cid
     */
    @Override
    public void getDeviceInfo(ArrayList<String> cid) {
        rx.Observable.just(cid)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<ArrayList<String>>() {
                    @Override
                    public void call(ArrayList<String> cid) {
                        if (cid != null && cid.size() != 0){
                            JfgCmdInsurance.getCmd().getShareList(cid);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getDeviceInfo"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到已经分享的亲友数的回调
     * @return
     */
    @Override
    public Subscription getDeviceInfoCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListCallBack.class)
                .flatMap(new Func1<RxEvent.GetShareListCallBack, Observable<ArrayList<DeviceBean>>>() {
                    @Override
                    public Observable<ArrayList<DeviceBean>> call(RxEvent.GetShareListCallBack getShareListCallBack) {
                        if (getShareListCallBack != null && getShareListCallBack instanceof RxEvent.GetShareListCallBack){
                            if (getShareListCallBack.i == 0 && getShareListCallBack.arrayList.size() != 0){
                                //该设备以分享的亲友数赋值
                                for (int i = 0;i<allDevice.size();i++){
                                    if (allDevice.get(i).uuid.equals(getShareListCallBack.arrayList.get(i).cid)){
                                        allDevice.get(i).hasShareCount = getShareListCallBack.arrayList.get(i).friends.size();
                                    }
                                }
                                return Observable.just(allDevice);
                            }else {
                                return Observable.just(allDevice);
                            }
                        }else {
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

}
