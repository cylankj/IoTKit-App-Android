package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetNameContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineInfoSetNamePresenterImpl extends AbstractPresenter<MineInfoSetNameContract.View> implements MineInfoSetNameContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private JFGAccount jfgAccount;

    public MineInfoSetNamePresenterImpl(MineInfoSetNameContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 发送保存昵称的请求
     */
    @Override
    public void saveName(final String newAlias) {
        if (getView() != null) {
            getView().showSendHint();
        }
        rx.Observable.just(newAlias)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String newAliasAccount) {
                        jfgAccount.resetFlag();
                        jfgAccount.setAlias(newAliasAccount);
                        try {
                            JfgCmdInsurance.getCmd().setAccount(jfgAccount);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("saveName" + throwable.getLocalizedMessage());
                    }
                });

    }

    @Override
    public boolean isEditEmpty(String string) {
        return TextUtils.isEmpty(string) ? true : false;
    }

    /**
     * 修改昵称之后的回调
     *
     * @return
     */
    @Override
    public Subscription saveAliasCallBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getView() != null) {
                            jfgAccount = getUserInfo.jfgAccount;
                            getView().handlerResult(getUserInfo);
                        }
                    }
                });
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(saveAliasCallBack());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }
}
