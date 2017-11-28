package com.cylan.jiafeigou.base.wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.dagger.Injectable;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.ActivityBackInterceptor;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.view.LifecycleAdapter;
import com.cylan.jiafeigou.view.PresenterAdapter;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.RxLifecycle;
import com.trello.rxlifecycle.android.FragmentEvent;
import com.trello.rxlifecycle.android.RxLifecycleAndroid;
import com.umeng.socialize.UMShareAPI;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.AndroidInjection;
import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseActivity<P extends JFGPresenter> extends AppCompatActivity implements JFGView, ActivityBackInterceptor,
        PresenterAdapter<P>, Injectable, LifecycleProvider<FragmentEvent> {
    protected String uuid;
    protected P presenter;
    protected LifecycleAdapter lifecycleAdapter;
    protected List<ActivityBackInterceptor> interceptors = new ArrayList<>();
    protected BehaviorSubject<FragmentEvent> lifecycleSubject;
    private Unbinder unbinder;
    protected boolean mInitCalled = false;

    public void addActivityBackInterceptor(ActivityBackInterceptor interceptor) {
        //先确保之前没有这个 interceptor ,防止重复 add
        interceptors.remove(interceptor);
        interceptors.add(1, interceptor);
    }

    public void removeActivityBackInterceptor(ActivityBackInterceptor interceptor) {
        interceptors.remove(interceptor);
    }


    @Inject
    public final void setPresenter(@NonNull P presenter) {
        this.presenter = presenter;
        this.presenter.uuid(uuid);
        if (presenter instanceof LifecycleAdapter) {
            lifecycleAdapter = (LifecycleAdapter) presenter;
        }
    }

    @Override
    public final String uuid() {
        return getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
    }

    @Override
    public boolean useDaggerInject() {
        return true;
    }

    @Override
    public boolean useButterKnifeInject() {
        return true;
    }

    private final void injectDagger() {
        if (useDaggerInject()) {
            try {
                AndroidInjection.inject(this);
            } catch (Exception e) {
                e.printStackTrace();
                AppLogger.w("Dagger 注入失败了,如果不需要 Dagger 注入,重写 useDaggerInject 方法并返回 FALSE");
            }
        }
    }

    private final void injectButterKnife() {
        if (useButterKnifeInject()) {
            unbinder = ButterKnife.bind(this);
        }
    }

    @Nonnull
    @Override
    public final Observable<FragmentEvent> lifecycle() {
        return lifecycleSubject.asObservable();
    }

    @Nonnull
    @Override
    public <T> LifecycleTransformer<T> bindUntilEvent(@Nonnull FragmentEvent event) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, event);
    }

    @Nonnull
    @Override
    public <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycleAndroid.bindFragment(lifecycleSubject);
    }

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(getResources().getDrawable(android.R.color.white));
        lifecycleSubject = BehaviorSubject.create();
        uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        interceptors.add(0, this);
        injectDagger();
        super.onCreate(savedInstanceState);
        if (onSetContentView()) {
            injectButterKnife();
            initViewAndListener();
        }
        lifecycleSubject.onNext(FragmentEvent.CREATE);
    }

    protected boolean onSetContentView() {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.subscribe();
        }
        if (lifecycleAdapter != null) {
            lifecycleAdapter.start();
        }
        lifecycleSubject.onNext(FragmentEvent.START);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lifecycleSubject.onNext(FragmentEvent.RESUME);
    }

    @Override
    protected void onPause() {
        lifecycleSubject.onNext(FragmentEvent.PAUSE);
        super.onPause();
        if (lifecycleAdapter != null) {
            lifecycleAdapter.pause();
        }
    }

    @Override
    protected void onStop() {
        lifecycleSubject.onNext(FragmentEvent.STOP);
        super.onStop();
        IMEUtils.hide(this);
        if (lifecycleAdapter != null) {
            lifecycleAdapter.stop();
        }
    }

    @Override
    protected void onDestroy() {
        lifecycleSubject.onNext(FragmentEvent.DESTROY);
        super.onDestroy();
        if (presenter != null) {
            if (presenter.isSubscribed()) {
                presenter.unsubscribe();
            }
        }
        if (lifecycleAdapter != null) {
            lifecycleAdapter.destroy();
        }
//        if (unbinder != null) {
//            unbinder.unbind();
//        }
//        interceptors.clear();
//        presenter = null;
//        unbinder = null;
//        lifecycleSubject = null;
//        lifecycleAdapter = null;
//        mInitCalled = false;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        if (presenter != null) {
            presenter.uuid(uuid);
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onLoginStateChanged(boolean online) {
    }

    @CallSuper
    protected void initViewAndListener() {
        mInitCalled = true;
    }

    @Override
    public final Activity activity() {
        return this;
    }

    @Override
    public final Context getContext() {
        return this;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final int orientation = this.getResources().getConfiguration().orientation;
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:// 加入横屏要处理的代码
                onScreenRotationChanged(true);
                break;
            case Configuration.ORIENTATION_PORTRAIT:// 加入竖屏要处理的代码
                onScreenRotationChanged(false);
                break;
        }
    }

    public void onScreenRotationChanged(boolean land) {
        //do nothing
    }

    /**
     * 如果需要拦截 activity 的 back 事件,请使用performBackIntercept
     */
    @Override
    public final void onBackPressed() {
        boolean willExit = getSupportFragmentManager().getBackStackEntryCount() == 0;
        boolean hasConsumed = false;
        for (ActivityBackInterceptor interceptor : interceptors) {
            try {
                if (interceptor.performBackIntercept(willExit)) {
                    hasConsumed = true;
                }
                //如果出现异常直接捕获就行了
            } catch (Exception e) {
                e.printStackTrace();
                AppLogger.e(MiscUtils.getErr(e));
            }
        }
        //每个类都可以接收到 back 事件,但需要子类自己判断是否需要消费 back 事件
        if (hasConsumed) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean performBackIntercept(boolean willExit) {
        return false;
    }

    @Override
    public void startActivity(Intent intent) {
        if (!TextUtils.isEmpty(uuid)) {//避免写入空值
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        }
        super.startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
