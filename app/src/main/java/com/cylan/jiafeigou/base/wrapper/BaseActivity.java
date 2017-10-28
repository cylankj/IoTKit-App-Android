package com.cylan.jiafeigou.base.wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.dagger.Injectable;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.ActivityBackInterceptor;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.view.LifecycleAdapter;
import com.cylan.jiafeigou.view.PresenterAdapter;
import com.umeng.socialize.UMShareAPI;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import dagger.android.AndroidInjection;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseActivity<P extends JFGPresenter> extends AppCompatActivity implements JFGView, ActivityBackInterceptor,
        PresenterAdapter<P>, Injectable {
    protected String uuid;
    protected P presenter;
    protected LifecycleAdapter lifecycleAdapter;
    protected List<ActivityBackInterceptor> interceptors = new ArrayList<>();

    public void addActivityBackInterceptor(ActivityBackInterceptor interceptor) {
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
            this.lifecycleAdapter = (LifecycleAdapter) presenter;
        }
    }

    @Override
    public final String uuid() {
        return getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
    }

    @Override
    public boolean supportInject() {
        return true;
    }

    private final void injectDagger() {
        if (supportInject()) {
            try {
                AndroidInjection.inject(this);
            } catch (Exception e) {
                e.printStackTrace();
                AppLogger.w("Dagger 注入失败了,如果不需要 Dagger 注入,重写 supportInject 方法并返回 FALSE");
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(getResources().getDrawable(android.R.color.white));
        uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        interceptors.add(0, this);
        injectDagger();
        super.onCreate(savedInstanceState);
        if (getContentViewID() != -1) {
            setContentView(getContentViewID());
            ButterKnife.bind(this);
        } else if (getContentRootView() != null) {
            setContentView(getContentRootView());
        }
        initViewAndListener();
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (lifecycleAdapter != null) {
            lifecycleAdapter.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        IMEUtils.hide(this);
        if (lifecycleAdapter != null) {
            lifecycleAdapter.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            if (!presenter.isUnsubscribed()) {
                presenter.unsubscribe();
            }
        }
        interceptors.clear();
    }

    protected View getContentRootView() {
        return null;
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

    protected int getContentViewID() {
        return -1;
    }

    protected void initViewAndListener() {
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
        for (ActivityBackInterceptor interceptor : interceptors) {
            if (interceptor.performBackIntercept()) {
                return;
            }
        }

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean performBackIntercept() {
        return false;
    }

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        super.startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
