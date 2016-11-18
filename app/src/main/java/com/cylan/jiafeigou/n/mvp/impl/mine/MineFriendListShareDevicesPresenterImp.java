package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendListShareDevicesToContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.ArrayList;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendListShareDevicesPresenterImp extends AbstractPresenter<MineFriendListShareDevicesToContract.View> implements MineFriendListShareDevicesToContract.Presenter {

    private CompositeSubscription subscription;
    private ArrayList<JFGShareListInfo> hasShareFriendList;

    public MineFriendListShareDevicesPresenterImp(MineFriendListShareDevicesToContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }else {
            subscription = new CompositeSubscription();
            subscription.add(initDeviceListData());
            subscription.add(shareDeviceCallBack());
        }
    }

    @Override
    public void stop() {
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }

    /**
     * 获取到设备列表的数据
     */
    @Override
    public Subscription initDeviceListData() {
        //Test 数据。。。。
        RxEvent.GetShareListCallBack shareDeviceList = new RxEvent.GetShareListCallBack(1,TestData());
        handlerShareDeviceListData(shareDeviceList);

        return RxBus.getCacheInstance().toObservable(RxEvent.GetShareListCallBack.class)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o != null && o instanceof RxEvent.GetShareListCallBack){
                            RxEvent.GetShareListCallBack shareDeviceList = (RxEvent.GetShareListCallBack) o;
                            handlerShareDeviceListData(shareDeviceList);
                        }
                    }
                });
    }

    /**
     * 发送分享的设备给亲友的请求
     */
    @Override
    public void sendShareToReq(ArrayList<DeviceBean> chooseList, final RelAndFriendBean friendBean) {
        if (getView() != null){
            getView().showSendReqProgress();
        }
        rx.Observable.just(chooseList)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<ArrayList<DeviceBean>>() {
                    @Override
                    public void call(ArrayList<DeviceBean> deviceBeen) {
                        for (DeviceBean bean:deviceBeen){
                            JfgCmdInsurance.getCmd().shareDevice(bean.uuid,friendBean.account);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendShareToReq",throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 是否有勾选中的设置按钮状态
     * @param list
     */
    @Override
    public void checkIsChoose(ArrayList<DeviceBean> list) {
        if (list.size() == 0){
            getView().hideFinishBtn();
        }else {
            getView().showFinishBtn();
        }
    }

    /**
     * 分享设备的回调
     * @return
     */
    @Override
    public Subscription shareDeviceCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ShareDeviceCallBack.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ShareDeviceCallBack>() {
                    @Override
                    public void call(RxEvent.ShareDeviceCallBack shareDeviceCallBack) {
                        //TODO 返回成功与否
                        if (getView() != null){
                            getView().hideSendReqProgress();
                            getView().showSendReqFinishReuslt();
                        }
                    }
                });
    }

    /**
     * 触发加载分享设备列表的数据
     */
    @Override
    public void callShareDeviceList() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        //TODO 触发请求
                    }
                });
    }

    /**
     *模拟测试数据
     */
    private ArrayList<JFGShareListInfo> TestData() {

        ArrayList<JFGShareListInfo> list = new ArrayList<>();

        for (int i = 0; i < 3; i++){
            JFGShareListInfo info = new JFGShareListInfo();
            info.cid = i+"cid";

            ArrayList<JFGFriendAccount> listNei = new ArrayList<>();

            for (int j = 0; j< 3;j++){
                JFGFriendAccount account = new JFGFriendAccount();
                account.markName = "备注名"+i+j;
                account.account = "账号"+i+j;
                account.alias = "昵称"+i+j;
                listNei.add(account);
            }
            info.friends = listNei;
            list.add(info);
        }
        return list;
    }

    /**
     * 处理请求回的列表数据
     * @param shareDeviceList
     */
    private void handlerShareDeviceListData(RxEvent.GetShareListCallBack shareDeviceList) {
        if (shareDeviceList != null && shareDeviceList.arrayList.size() != 0){
            hasShareFriendList = shareDeviceList.arrayList;
            if (getView() != null){
                getView().initRecycleView(getShareDeviceList(shareDeviceList));
            }
        }else {
            if (getView() != null){
                getView().showNoDeviceView();
            }
        }
    }
    /**
     * desc:获取到分享设备的list集合数据
     * @param shareDeviceList
     */
    private ArrayList<DeviceBean> getShareDeviceList(RxEvent.GetShareListCallBack shareDeviceList) {

        ArrayList<DeviceBean> list = new ArrayList<>();

        for (JFGShareListInfo info:shareDeviceList.arrayList){
            //TODO 数据的详细赋值
            DeviceBean bean = new DeviceBean();
            bean.alias = "相框" + info.cid;
            bean.uuid = info.cid;
            list.add(bean);
        }
        return list;
    }
}
