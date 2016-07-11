package com.cylan.jiafeigou.n.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.impl.LoginPresenterImpl;
import com.cylan.jiafeigou.n.view.login.LoginFragment;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.ActivityUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public class NeedLoginActivity extends BaseFullScreenFragmentActivity {

    private WeakReference<LoginFragment> loginFragmentWeakReference;
    private WeakReference<LoginPresenterImpl> loginPresenterWeakReference;
    CompositeSubscription _subscriptions;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();
        registerRxBug();
    }

    private void registerRxBug() {
        //注册1
        _subscriptions = new CompositeSubscription();
        _subscriptions
                .add(RxBus.getInstance().toObservable()
//                        .debounce(2000, TimeUnit.MILLISECONDS)//2s内只发生一次
                        .subscribe(new Action1<Object>() {
                            @Override
                            public void call(Object event) {
                                if (event instanceof RxEvent.NeedLoginEvent) {
                                    signInFirst(((RxEvent.NeedLoginEvent) event).bundle);
                                }
                            }
                        }));
    }

    private void signInFirst(Bundle extra) {
        if (extra == null)
            extra = new Bundle();
        extra.putInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID, android.R.id.content);
        extra.putInt(JConstant.KEY_FRAGMENT_ACTION_1, 1);
        LoginFragment fragment = null;
        if (loginFragmentWeakReference != null && loginFragmentWeakReference.get() != null) {
            fragment = loginFragmentWeakReference.get();
        } else {
            fragment = LoginFragment.newInstance(extra);
            loginFragmentWeakReference = new WeakReference<>(fragment);
        }
        if (loginPresenterWeakReference != null && loginPresenterWeakReference.get() != null) {
            fragment.setPresenter(loginPresenterWeakReference.get());
        } else loginPresenterWeakReference = new WeakReference<>(new LoginPresenterImpl(fragment));
        if (getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName()) != null)
            return;
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                fragment, android.R.id.content, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (_subscriptions != null && !_subscriptions.isUnsubscribed())
            _subscriptions.unsubscribe();
    }
}
