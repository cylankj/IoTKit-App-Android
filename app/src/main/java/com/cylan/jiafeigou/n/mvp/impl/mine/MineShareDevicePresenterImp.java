package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.view.View;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.view.adapter.MineShareDeviceAdapter;
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

    private Subscription loadDataSub;

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
        if (loadDataSub != null && loadDataSub.isUnsubscribed()){
            loadDataSub.unsubscribe();
        }
    }

    @Override
    public void initData() {
        loadDataSub = Observable.just("")
                .map(new Func1<String, List<DeviceBean>>() {
                    @Override
                    public List<DeviceBean> call(String s) {
                        //TODO 获取到设备列表的数据
                        return testData();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DeviceBean>>() {
                    @Override
                    public void call(List<DeviceBean> deviceBeen) {
                        MineShareDeviceAdapter adapter = new MineShareDeviceAdapter(getView().getContext(),deviceBeen,null);
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
                                        getView().jump2ShareDeviceMangerFragment(itemView,viewType,position);
                                    }
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public DeviceBean getBean(int position) {
        return testData().get(position);
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
