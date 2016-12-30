package com.cylan.jiafeigou.base;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.UUID;

import butterknife.ButterKnife;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseActivity<P extends JFGPresenter> extends AppCompatActivity implements JFGView {
    protected P mPresenter;

    private static AlertDialog mAlertDialog;

    private static Toast sToast;

    @Override
    public Context getAppContext() {
        return getApplicationContext();
    }

    @Override
    public Activity getActivityContext() {
        return this;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewID());
        ButterKnife.bind(this);
        if (mPresenter == null) {
            mPresenter = onCreatePresenter();

        }
        initViewAndListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mPresenter != null) {
            mPresenter.onViewAttached(this);
            mPresenter.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.onStop();
            mPresenter.onViewDetached();
        }
    }

    protected abstract P onCreatePresenter();

    @Override
    public void showLoading() {
        showLoadingMsg(getResources().getString(R.string.LOADING));
    }

    @Override
    public void showLoadingMsg(String msg) {
        LoadingDialog.dismissLoading(getSupportFragmentManager());
    }

    @Override
    public String showAlert(String title, String msg, String ok, String cancel) {
        if (mAlertDialog == null) {
            mAlertDialog = new AlertDialog.Builder(this).create();
        }
        String handle = UUID.randomUUID().toString();
        if (!TextUtils.isEmpty(title)) mAlertDialog.setTitle(title);
        if (!TextUtils.isEmpty(msg)) mAlertDialog.setMessage(msg);
        if (!TextUtils.isEmpty(ok)) {
            mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, ok, (dialog, which) -> {
                mPresenter.onViewAction(JFGView.VIEW_ACTION_OK, handle, null);
            });
        }
        if (!TextUtils.isEmpty(cancel)) {
            mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, (dialog, which) -> {
                mPresenter.onViewAction(JFGView.VIEW_ACTION_CANCEL, handle, null);
            });
        }
        mAlertDialog.show();
        return handle;
    }

    protected abstract int getContentViewID();

    protected void initViewAndListener() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final int orientation = this.getResources().getConfiguration().orientation;
        switch (orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:// 加入横屏要处理的代码
                onScreenRotationChanged(true);
                mPresenter.onScreenRotationChanged(true);
                break;
            case Configuration.ORIENTATION_PORTRAIT:// 加入竖屏要处理的代码
                onScreenRotationChanged(false);
                mPresenter.onScreenRotationChanged(false);
                break;
        }
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        //do nothing
    }

    @Override
    public void onBackPressed() {
        int orientation = getRequestedOrientation();
        if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }

        boolean exit = ((BasePresenter) mPresenter).hasReadyForExit();
        if (exit) {
            if (shouldExit()) onPrepareToExit(super::onBackPressed);
        } else {
            showToast(getString(R.string.click_back_again_exit));
        }
    }

    @Override
    public void showToast(String msg) {
        if (sToast == null) {
            sToast = Toast.makeText(getAppContext(), "", Toast.LENGTH_SHORT);//toast会持有view对象,所以用applicationContext避免内存泄露
        }
        sToast.setText(msg);
        sToast.show();
    }

    /**
     * 退出之前做一些清理或准备工作
     */
    protected void onPrepareToExit(Action action) {
    }

    protected boolean shouldExit() {
        return true;
    }
}
