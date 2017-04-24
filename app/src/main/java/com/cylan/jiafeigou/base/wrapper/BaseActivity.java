package com.cylan.jiafeigou.base.wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.injector.component.AppComponent;
import com.cylan.jiafeigou.base.injector.component.DaggerActivityComponent;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.engine.GlobalResetPwdSource;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.UUID;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseActivity<P extends JFGPresenter> extends AppCompatActivity implements JFGView {
    @Inject
    protected P presenter;
    @Inject
    protected JFGSourceManager sourceManager;
    @Inject
    protected AppCmd appCmd;

    protected String uuid;
    protected AlertDialog alertDialog;
    protected Toast mToast;

    protected ActivityComponent component;


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
        AppComponent appComponent = BaseApplication.getAppComponent();
        this.component = DaggerActivityComponent.builder().appComponent(appComponent).build();
        if (this.component != null) {
            setActivityComponent(this.component);
        }
        uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        if (presenter != null) {
            presenter.onSetViewUUID(uuid);
            presenter.onViewAttached(this);
        }
        initViewAndListener();
        if (presenter != null) {
            presenter.onSetContentView();//有些view需要根据一定的条件来显示不同的view,可以在这个方法中来选择
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        if (presenter != null) {
            presenter.onSetViewUUID(uuid);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.onStart();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.onStop();
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        mToast = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.onViewDetached();
            component = null;
            presenter = null;
        }
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void showLoadingMsg(String msg) {
        LoadingDialog.dismissLoading(getSupportFragmentManager());
    }

    protected abstract void setActivityComponent(ActivityComponent activityComponent);


    @Override
    public String showAlert(String title, String msg, String ok, String cancel) {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(this).create();
        }
        String handle = UUID.randomUUID().toString();
        if (!TextUtils.isEmpty(title)) alertDialog.setTitle(title);
        if (!TextUtils.isEmpty(msg)) alertDialog.setMessage(msg);
        if (!TextUtils.isEmpty(ok)) {
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, ok, (dialog, which) -> {
                presenter.onViewAction(JFGView.VIEW_ACTION_OK, handle, null);
            });
        }
        if (!TextUtils.isEmpty(cancel)) {
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancel, (dialog, which) -> {
                presenter.onViewAction(JFGView.VIEW_ACTION_CANCEL, handle, null);
            });
        }
        alertDialog.show();
        return handle;
    }

    @Override
    public void onLoginStateChanged(boolean online) {
    }

    /**
     * 默认是将viewAction转发到presenter中进行处理,子类也可以复写此方法自己处理
     */
    public void onViewAction(int action, String handler, Object extra) {
        presenter.onViewAction(action, handler, extra);
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
                presenter.onScreenRotationChanged(true);
                break;
            case Configuration.ORIENTATION_PORTRAIT:// 加入竖屏要处理的代码
                onScreenRotationChanged(false);
                presenter.onScreenRotationChanged(false);
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
        if (presenter != null) {
            boolean exit = ((BasePresenter) presenter).hasReadyForExit();
            if (exit) {
                if (shouldExit()) onPrepareToExit(super::onBackPressed);
            } else {
                showToast(getString(R.string.click_back_again_exit));
            }
        } else {
            onPrepareToExit(super::onBackPressed);
        }
    }

    @Override
    public void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(getAppContext(), "", Toast.LENGTH_SHORT);//toast会持有view对象,所以用applicationContext避免内存泄露
        }
        mToast.setText(msg);
        mToast.show();
    }

    /**
     * 退出之前做一些清理或准备工作
     */
    protected void onPrepareToExit(Action action) {
        action.actionDone();
    }

    protected boolean shouldExit() {
        return true;
    }

    @Override
    public String onResolveViewLaunchType() {
        String way = getIntent().getStringExtra(JConstant.VIEW_CALL_WAY);
        if (TextUtils.isEmpty(way)) {
            way = DelayRecordContract.View.VIEW_LAUNCH_WAY_SETTING;
        }
        return way;
    }

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        super.startActivity(intent);
    }

    protected void dismissAlert() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }
}
