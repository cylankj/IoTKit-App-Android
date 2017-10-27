package com.cylan.jiafeigou.base.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.ActivityBackInterceptor;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.umeng.socialize.UMShareAPI;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseActivity<P extends JFGPresenter> extends DaggerActivity implements JFGView, ActivityBackInterceptor {
    protected String uuid;
    protected P presenter;

    protected List<ActivityBackInterceptor> interceptors = new ArrayList<>();

    public void addActivityBackInterceptor(ActivityBackInterceptor interceptor) {
        interceptors.add(1, interceptor);
    }

    public void removeActivityBackInterceptor(ActivityBackInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

    @Inject
    public void setPresenter(@NonNull P presenter) {
        this.presenter = presenter;
        this.presenter.uuid(uuid);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        interceptors.add(this);
        getWindow().setBackgroundDrawable(getResources().getDrawable(android.R.color.white));
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
        presenter.subscribe();
    }

    @Override
    protected void onStop() {
        super.onStop();
        IMEUtils.hide(this);
        if (presenter instanceof BasePresenter) {
            ((BasePresenter) presenter).onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!presenter.isUnsubscribed()) {
            presenter.unsubscribe();
        }
    }

    protected View getContentRootView() {
        return null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        presenter.uuid(uuid);
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
    public Activity activity() {
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

    @Override
    public void onBackPressed() {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
