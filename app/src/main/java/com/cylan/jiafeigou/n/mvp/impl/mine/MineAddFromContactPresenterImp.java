package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineAddFromContactPresenterImp extends AbstractPresenter<MineAddFromContactContract.View> implements MineAddFromContactContract.Presenter {

    private String userAlids = "";

    private Subscription sendRequestSub;
    private CompositeSubscription compositeSubscription;

    public MineAddFromContactPresenterImp(MineAddFromContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }

        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(getAcocountAlids());
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    @Override
    public void sendRequest(final String account, final String mesg) {
        rx.Observable.just(account,mesg)
            .delay(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .subscribe(new Action1<String>() {
                @Override
                public void call(String s) {
                    JfgCmdInsurance.getCmd().addFriend(account,mesg);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    AppLogger.e("sendRequest"+throwable.getLocalizedMessage());
                }
            });

    }

    @Override
    public Subscription getAcocountAlids() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo){
                            if (getView() != null)getView().initEditText(getUserInfo.jfgAccount.getAlias());
                            userAlids = getUserInfo.jfgAccount.getAlias();
                        }
                    }
                });
    }

    /**
     * 获取到用户的昵称
     * @return
     */
    @Override
    public String getUserAlis() {
        return userAlids;
    }
}
