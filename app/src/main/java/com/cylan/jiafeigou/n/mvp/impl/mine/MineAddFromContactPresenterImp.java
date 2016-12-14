package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.rx.RxBus;

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

    private CompositeSubscription compositeSubscription;

    public MineAddFromContactPresenterImp(MineAddFromContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getAccountAlids());
            compositeSubscription.add(checkAccountCallBack());
        }
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    @Override
    public void sendRequest(final String account, final String mesg) {
        rx.Observable.just(account,mesg)
            .subscribeOn(Schedulers.newThread())
            .subscribe(new Action1<String>() {
                @Override
                public void call(String s) {
                    try {
                        JfgCmdInsurance.getCmd().addFriend(account,mesg);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    AppLogger.e("sendRequest"+throwable.getLocalizedMessage());
                }
            });

    }

    /**
     * 获取到账号昵称
     * @return
     */
    @Override
    public Subscription getAccountAlids() {
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
    public String getUserAlias() {
        return userAlids;
    }

    /**
     * 检测账号
     * @param account
     */
    @Override
    public void checkAccount(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            JfgCmdInsurance.getCmd().checkFriendAccount(s);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("checkoutAccount"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 检测账号的回调
     * @return
     */
    @Override
    public Subscription checkAccountCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null && checkAccountCallback instanceof RxEvent.CheckAccountCallback){
                            if (getView() != null){
                                getView().showResultDialog(checkAccountCallback);
                            }
                        }
                    }
                });
    }

}
