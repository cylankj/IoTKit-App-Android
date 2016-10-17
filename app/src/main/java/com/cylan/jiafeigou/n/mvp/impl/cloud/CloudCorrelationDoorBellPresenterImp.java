package com.cylan.jiafeigou.n.mvp.impl.cloud;

import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudCorrelationDoorBellContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BellInfoBean;

import java.util.ArrayList;
import java.util.Collection;
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

    public CloudCorrelationDoorBellPresenterImp(CloudCorrelationDoorBellContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        loadDoorBellData("");
    }

    @Override
    public void stop() {
        if (subscription != null){
            subscription.unsubscribe();
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
                            getView().showNoRelativeDevicesView();
                        }
                        getView().initRecycleView(bellInfoBeen);
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
}
