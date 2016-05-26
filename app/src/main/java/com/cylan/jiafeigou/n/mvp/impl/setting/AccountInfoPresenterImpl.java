package com.cylan.jiafeigou.n.mvp.impl.setting;

import com.cylan.jiafeigou.n.mvp.contract.setting.AccountInfoContract;

import java.lang.ref.WeakReference;

/**
 * Created by hunt on 16-5-26.
 */

public class AccountInfoPresenterImpl implements AccountInfoContract.Presenter {

    private WeakReference<AccountInfoContract.View> weakReference;

    public AccountInfoPresenterImpl(AccountInfoContract.View view) {
        weakReference = new WeakReference<>(view);
        view.setPresenter(this);

    }

    @Override
    public void start() {
        if (getView() != null)
            getView().initBackStackChangeListener();
    }

    @Override
    public void stop() {

    }

    @Override
    public AccountInfoContract.View getView() {
        return weakReference != null ? weakReference.get() : null;
    }
}
