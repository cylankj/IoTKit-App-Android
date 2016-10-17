package com.cylan.jiafeigou.n.mvp.impl.cloud;

import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudCorrelationDoorBellContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BellInfoBean;
import com.cylan.jiafeigou.n.view.adapter.RelationDoorBellAdapter;
import com.cylan.jiafeigou.n.view.adapter.UnRelationDoorBellAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;
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
 * 创建时间：2016/9/29
 * 描述：
 */
public class CloudCorrelationDoorBellPresenterImp extends AbstractPresenter<CloudCorrelationDoorBellContract.View> implements CloudCorrelationDoorBellContract.Presenter{

    private Subscription subscription;
    private Subscription unRelativeSub;
    public List<BellInfoBean> unRelativieList;

    public int notifyFlag = 1;

    public CloudCorrelationDoorBellPresenterImp(CloudCorrelationDoorBellContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        loadDoorBellData("");
        loadUnRelaiveDoorBellData("");
    }

    @Override
    public void stop() {
        if (subscription != null){
            subscription.unsubscribe();
        }

        if (unRelativeSub != null){
            unRelativeSub.unsubscribe();
        }
    }

    @Override
    public void loadDoorBellData(String url) {

        subscription = Observable.just(url)
                .map(new Func1<String, List<BellInfoBean>>() {
                    @Override
                    public List<BellInfoBean> call(String url) {
                        //TODO 开启网络访问服务器
                        List<BellInfoBean> list = new ArrayList<BellInfoBean>();
                        list.addAll(TestData());
                        return list;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<BellInfoBean>>() {
                    @Override
                    public void call(List<BellInfoBean> bellInfoBeen) {
                        if (bellInfoBeen.size()== 0){
                            getView().showNoRelativeDevicesView(notifyFlag);
                        }
                        getView().initRelativeRecycleView(bellInfoBeen);
                        getView().setOnRelaItemClickListener(new RelativeItemListener());
                    }
                });
    }

    @Override
    public void loadUnRelaiveDoorBellData(String url) {

        unRelativeSub = Observable.just(url)
                .map(new Func1<String, List<BellInfoBean>>() {
                    @Override
                    public List<BellInfoBean> call(String s) {
                        //TODO 开启网络访问服务器
                        List<BellInfoBean> list = new ArrayList<BellInfoBean>();
                        list.addAll(TestData());
                        return list;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<BellInfoBean>>() {
                    @Override
                    public void call(List<BellInfoBean> list) {
                        if (unRelativieList == null){
                            unRelativieList = new ArrayList<BellInfoBean>();
                        }
                        unRelativieList.addAll(list);
                        if (list.size()== 0){
                            getView().showNoUnRelativeDevicesView(notifyFlag);
                        }
                        getView().initUnRelativeRecycleView(unRelativieList);
                        getView().setOnUnRelItemClickListener(new UnRelativeItemListener());
                    }
                });
    }

    /**
     *
     * desc:测试数据
     */
    private List<BellInfoBean> TestData() {
        List<BellInfoBean> list = new ArrayList<>();
        for (int i = 0; i< 3;i++){
            BellInfoBean bean = new BellInfoBean();
            bean.nickName = "门铃"+i;
            bean.ssid ="序列号"+i;
            list.add(bean);
        }
        return list;
    }

    /**
     * desc:关联按钮监听器
     */
    private class UnRelativeItemListener implements UnRelationDoorBellAdapter.OnRelativeClickListener {
        @Override
        public void relativeClick(SuperViewHolder holder, int viewType, int layoutPosition, BellInfoBean item) {
            notifyFlag = 1;
            getView().notifyUnRelativeRecycle(holder,viewType,layoutPosition,item,notifyFlag);
            getView().notifyRelativeRecycle(holder,viewType,layoutPosition,item,notifyFlag);
        }
    }

    /**
     * desc:取消关联按钮监听
     */
    private class RelativeItemListener implements RelationDoorBellAdapter.OnUnRelaItemClickListener{
        @Override
        public void unRelativeClick(SuperViewHolder holder, int viewType, int layoutPosition, BellInfoBean item) {
            notifyFlag = 2;
            getView().notifyRelativeRecycle(holder,viewType,layoutPosition,item,notifyFlag);
            getView().notifyUnRelativeRecycle(holder,viewType,layoutPosition,item,notifyFlag);
        }
    }
}
