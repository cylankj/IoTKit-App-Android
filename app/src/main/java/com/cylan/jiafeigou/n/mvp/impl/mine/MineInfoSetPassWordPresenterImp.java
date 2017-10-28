package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetPassWordContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/20
 * 描述：
 */
public class MineInfoSetPassWordPresenterImp extends AbstractPresenter<MineInfoSetPassWordContract.View> implements MineInfoSetPassWordContract.Presenter {

    private CompositeSubscription subscription;

    public MineInfoSetPassWordPresenterImp(MineInfoSetPassWordContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        } else {
            subscription = new CompositeSubscription();
            subscription.add(changePwdBack());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    @Override
    public boolean checkOldPassword(String inputPass) {
        //TEST
        String oldPass = "111111";
        return inputPass.equals(oldPass);
    }

    @Override
    public boolean checkNewPassword(String oldPass, String newPass) {
        return oldPass.equals(newPass);
    }

    @Override
    public boolean checkNewPasswordLength(String newPass) {
        return (newPass.length() < 6 || newPass.length() > ContextUtils.getContext().getResources().getInteger(R.integer.max_password_length));
    }

    /**
     * 发送修改密码请求
     *
     * @param account
     */
    @Override
    public void sendChangePassReq(final String account, final String oldPass, final String newPass) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.io())
                .subscribe(s -> {
                    try {
                        int i = BaseApplication.getAppComponent().getCmd().changePassword(account, oldPass, newPass);
                        AppLogger.d("sendChangePassReq:" + i);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    /**
     * 修改密码的回调
     *
     * @return
     */
    @Override
    public Subscription changePwdBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ChangePwdBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(changePwdBack -> {
                    if (changePwdBack != null) {
                        if (getView() != null) {
                            getView().changePwdResult(changePwdBack.jfgResult);
                        }
                    }
                }, AppLogger::e);
    }

}
