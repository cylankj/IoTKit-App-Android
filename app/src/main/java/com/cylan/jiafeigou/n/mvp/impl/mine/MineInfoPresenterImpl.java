package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;

import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MineInfoPresenterImpl extends AbstractPresenter<MineInfoContract.View> implements MineInfoContract.Presenter {


    public MineInfoPresenterImpl(MineInfoContract.View view, Context context) {
        super(view);
        view.setPresenter(this);
    }

    public void monitorPersonInformation() {
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(accountArrived -> {
                    if (accountArrived != null) {
                        getView().initPersonalInformation(accountArrived.account);
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
        addSubscription(subscribe);
    }

    /**
     * 判断是否是三方登录
     *
     * @return
     */
    @Override
    public boolean checkOpenLogin() {
        return BaseApplication.getAppComponent().getSourceManager().getAccount().getLoginType() >= 3;
    }

}
