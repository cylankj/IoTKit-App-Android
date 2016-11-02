package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.ArrayList;

import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class MineShareDevicePresenterImp extends AbstractPresenter<MineShareDeviceContract.View> implements MineShareDeviceContract.Presenter {

    private Subscription initDataSub;
    private ArrayList<JFGShareListInfo> hasShareFriendList;
    private CompositeSubscription subscription;

    public MineShareDevicePresenterImp(MineShareDeviceContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }else {
            subscription = new CompositeSubscription();
            subscription.add(initData());
        }
    }

    @Override
    public void stop() {
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }

    @Override
    public Subscription initData() {
        initDataSub = RxBus.getDefault().toObservable(RxEvent.GetShareDeviceList.class)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o != null && o instanceof RxEvent.GetShareDeviceList){
                            RxEvent.GetShareDeviceList shareDeviceList = (RxEvent.GetShareDeviceList) o;
                            handlerShareDeviceListData(shareDeviceList);
                        }
                    }
                });
        RxEvent.GetShareDeviceList shareDeviceList = new RxEvent.GetShareDeviceList(1,TestData());
        handlerShareDeviceListData(shareDeviceList);
        return initDataSub;
    }

    @Override
    public JFGShareListInfo getJFGInfo(int position) {
        return hasShareFriendList.get(position);
    }

    /**
     * desc;测试的数据
     * @return
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
     * desc:处理设备分享的数据
     */
    private void handlerShareDeviceListData(final RxEvent.GetShareDeviceList shareDeviceList) {
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



    @Override
    public ArrayList<RelAndFriendBean> getHasShareRelAndFriendList(JFGShareListInfo info) {

        ArrayList<RelAndFriendBean> list = new ArrayList<>();

        for (JFGFriendAccount account:info.friends){
            RelAndFriendBean bean = new RelAndFriendBean();
            bean.account = account.account;
            bean.alias = account.alias;
            //TODO 具体赋值
            list.add(bean);
        }
        return list;
    }

}
