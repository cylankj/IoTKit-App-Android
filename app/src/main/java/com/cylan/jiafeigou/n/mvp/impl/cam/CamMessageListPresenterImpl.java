package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.utils.RandomUtils;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class CamMessageListPresenterImpl extends AbstractPresenter<CamMessageListContract.View>
        implements CamMessageListContract.Presenter {


    Subscription subscription;

    public CamMessageListPresenterImpl(CamMessageListContract.View view) {
        super(view);
        view.setPresenter(this);
    }


    private ArrayList<CamMessageBean> testData() {
        ArrayList<CamMessageBean> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CamMessageBean bean = new CamMessageBean();
            bean.viewType = RandomUtils.getRandom(4);
            bean.time = System.currentTimeMillis() - RandomUtils.getRandom(24 * 3600);
            list.add(bean);
        }
        return list;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }

    @Override
    public void fetchMessageList() {
        subscription = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, ArrayList<CamMessageBean>>() {
                    @Override
                    public ArrayList<CamMessageBean> call(Object o) {
                        return testData();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<CamMessageBean>>() {
                    @Override
                    public void call(ArrayList<CamMessageBean> beanArrayList) {
                        getView().onMessageListRsp(beanArrayList);
                    }
                });
    }
}
