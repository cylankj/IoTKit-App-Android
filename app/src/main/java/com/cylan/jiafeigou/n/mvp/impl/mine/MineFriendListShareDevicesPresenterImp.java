package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendListShareDevicesToContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.MineShareDeviceBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
    private ArrayList<JFGShareListInfo> hasShareFriendList;
    private Subscription sendShareToReqSub;

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
        }
    }

    @Override
    public void stop() {
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
        if (sendShareToReqSub != null && !sendShareToReqSub.isUnsubscribed()){
            sendShareToReqSub.unsubscribe();
        }
    }

    @Override
    public ArrayList<MineShareDeviceBean> getDeviceData() {
        ArrayList<MineShareDeviceBean> list = new ArrayList<>();
        MineShareDeviceBean mineShareDeviceBean = new MineShareDeviceBean();
        mineShareDeviceBean.setCheck(true);
        mineShareDeviceBean.setDeviceName("智能摄像头");
        mineShareDeviceBean.setIconUrl("");
        mineShareDeviceBean.setShareNumber(2);
        list.add(mineShareDeviceBean);
        return list;
    }

    /**
     * 获取到设备列表的数据
     */
    @Override
    public Subscription initDeviceListData() {

        //Test 数据。。。。
        RxEvent.GetShareDeviceList shareDeviceList = new RxEvent.GetShareDeviceList(1,TestData());
        handlerShareDeviceListData(shareDeviceList);

        return RxBus.getDefault().toObservable(RxEvent.GetShareDeviceList.class)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o != null && o instanceof RxEvent.GetShareDeviceList){
                            RxEvent.GetShareDeviceList shareDeviceList = (RxEvent.GetShareDeviceList) o;
                            handlerShareDeviceListData(shareDeviceList);
                        }
                    }
                });
    }

    /**
     * 发送分享的设备给亲友的请求
     */
    @Override
    public void sendShareToReq(ArrayList<DeviceBean> chooseList) {
        if (getView() != null){
            getView().showSendReqProgress();
        }
        sendShareToReqSub = Observable.just(chooseList)
                .map(new Func1<ArrayList<DeviceBean>, Integer>() {
                    @Override
                    public Integer call(ArrayList<DeviceBean> deviceBeen) {
                        //TODO SDK发送分享请求
                        return null;
                    }
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        getView().hideSendReqProgress();
                        getView().showSendReqFinishReuslt();
                    }
                });
    }

    @Override
    public void checkIsChoose(ArrayList<DeviceBean> list) {
        if (list.size() == 0){
            getView().hideFinishBtn();
        }else {
            getView().showFinishBtn();
        }
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
    private void handlerShareDeviceListData(RxEvent.GetShareDeviceList shareDeviceList) {
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
    private ArrayList<DeviceBean> getShareDeviceList(RxEvent.GetShareDeviceList shareDeviceList) {

        ArrayList<DeviceBean> list = new ArrayList<>();

        for (JFGShareListInfo info:shareDeviceList.arrayList){
            //TODO 数据的详细赋值
            DeviceBean bean = new DeviceBean();
            bean.alias = "相框" + info.cid;
            bean.cid = info.cid;
            list.add(bean);
        }
        return list;
    }
}
