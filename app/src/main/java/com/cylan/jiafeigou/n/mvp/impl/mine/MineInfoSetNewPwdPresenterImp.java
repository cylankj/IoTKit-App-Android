package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetNewPwdContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/12/28
 * 描述：
 */
public class MineInfoSetNewPwdPresenterImp extends AbstractPresenter<MineInfoSetNewPwdContract.View> implements MineInfoSetNewPwdContract.Presenter {

    public MineInfoSetNewPwdPresenterImp(MineInfoSetNewPwdContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 注册
     *
     * @param account
     * @param pwd
     */
    @Override
    public void openLoginRegister(String account, String pwd, String token) {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            if (JConstant.PHONE_REG.matcher(account).find()) {
                                JfgCmdInsurance.getCmd().register(account, pwd, JConstant.TYPE_PHONE, "");
                            } else if (JConstant.EMAIL_REG.matcher(account).find()) {
                                JfgCmdInsurance.getCmd().register(account, pwd, JConstant.TYPE_EMAIL, "");
                            }

                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("openLoginRegister" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 注册回调
     *
     * @return
     */
    @Override
    public Subscription registerBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultRegister.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ResultRegister>() {
                    @Override
                    public void call(RxEvent.ResultRegister resultRegister) {
                        if (resultRegister != null && resultRegister instanceof RxEvent.ResultRegister) {
                            if (getView() != null) getView().registerResult(resultRegister.code);
                        }
                    }
                });
    }

    @Override
    protected Subscription[] register() {
        Subscription[] subscriptions = new Subscription[]{};
        subscriptions[0] = registerBack();
        return subscriptions;
    }
}
