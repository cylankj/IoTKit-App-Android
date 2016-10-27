package com.cylan.jiafeigou.n.mvp.impl.bell;

import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BellInfoBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-8-3.
 */
public class BellSettingPresenterImpl extends AbstractPresenter<BellSettingContract.View>
        implements BellSettingContract.Presenter {
    public BellSettingPresenterImpl(BellSettingContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    @Override
    public void start() {
        compositeSubscription.add(onBellInfoSubscription());
        compositeSubscription.add(onLogStateSubscription());
    }

    private Subscription onBellInfoSubscription() {
        return Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, BellInfoBean>() {
                    @Override
                    public BellInfoBean call(Object o) {
                        BellInfoBean bean = new BellInfoBean();
                        bean.nickName = "智能门铃";
                        bean.ssid = "xxx";
                        return bean;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BellInfoBean>() {
                    @Override
                    public void call(BellInfoBean bellInfoBean) {
                        if (getView() != null) {
                            getView().onSettingInfoRsp(bellInfoBean);
                        }
                    }
                });
    }

    /**
     * 查询登陆状态
     *
     * @return
     */
    private Subscription onLogStateSubscription() {
        return RxBus.getInstance().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (o != null && o instanceof RxEvent.LoginRsp) {
                            if (getView() != null)
                                getView().onLoginState(((RxEvent.LoginRsp) o).state);
                        }
                    }
                });
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    @Override
    public void sendActivityResult(RxEvent.ActivityResult result) {
        if (RxBus.getInstance().hasObservers())
            RxBus.getInstance().send(result);
    }
}
