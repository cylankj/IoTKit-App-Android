package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.view.View;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.MineShareDeviceAdapter;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.superadapter.OnItemClickListener;
import com.cylan.superadapter.internal.SuperViewHolder;

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
 * 创建时间：2016/9/5
 * 描述：
 */
public class MineShareDevicePresenterImp extends AbstractPresenter<MineShareDeviceContract.View> implements MineShareDeviceContract.Presenter {

    private Subscription initDataSub;

    public MineShareDevicePresenterImp(MineShareDeviceContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (getView() != null){
            initData();
        }
    }

    @Override
    public void stop() {
        if (initDataSub != null && initDataSub.isUnsubscribed()){
            initDataSub.unsubscribe();
        }
    }

    @Override
    public void initData() {
        initDataSub = RxBus.getInstance().toObservable()
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
            MineShareDeviceAdapter adapter = new MineShareDeviceAdapter(getView().getContext(),getShareDeviceList(shareDeviceList),null);
            if (getView() != null){
                getView().initRecycleView(adapter);
                adapter.setOnShareClickListener(new MineShareDeviceAdapter.OnShareClickListener() {
                    @Override
                    public void onShare(SuperViewHolder holder, int viewType, int layoutPosition, DeviceBean item) {
                        getView().showShareDialog();
                    }
                });

                adapter.setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(View itemView, int viewType, int position) {
                        if (getView() != null) {
                            getView().jump2ShareDeviceMangerFragment(itemView,position,shareDeviceList.arrayList.get(position));
                        }
                    }
                });
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
    public DeviceBean getBean(int position) {
        return testData().get(position);
    }

    @Override
    public ArrayList<RelAndFriendBean> getHasShareRelAndFriendList(JFGShareListInfo info) {

        ArrayList<RelAndFriendBean> list = new ArrayList<>();

        for (JFGFriendAccount account:info.friends){
            RelAndFriendBean bean = new RelAndFriendBean();
            bean.account = account.account;
            bean.alids = account.alias;
            //TODO 具体赋值
            list.add(bean);
        }
        return list;
    }


    /**
     *desc:测试数据
     */
    private List<DeviceBean> testData() {
        List<DeviceBean> list = new ArrayList<>();
        for (int i = 0;i<3;i++){
            DeviceBean bean = new DeviceBean();
            bean.alias = "云相框"+i;
            list.add(bean);
        }
        return list;
    }
}
