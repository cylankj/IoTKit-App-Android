package com.cylan.jiafeigou.base.wrapper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
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
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.umeng.socialize.UMShareAPI;

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
    protected AlertDialog alert;
    protected Toast mToast;

    protected ActivityComponent component;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        getWindow().setBackgroundDrawable(getResources().getDrawable(android.R.color.white));
    }

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
        if (getContentViewID() != -1) {
            setContentView(getContentViewID());
            ButterKnife.bind(this);
        } else if (getContentRootView() != null) {
            setContentView(getContentRootView());
        }
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

    protected View getContentRootView() {
        return null;
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
        if (alert != null && alert.isShowing()) {
            alert.dismiss();
            alert = null;
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
    protected void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (presenter != null) {
            presenter.onPause();
        }
    }

    @Override
    public void showLoading(int resId, Object... args) {
        LoadingDialog.showLoading(getSupportFragmentManager(), getString(resId, args));
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading(getSupportFragmentManager());
    }

    protected abstract void setActivityComponent(ActivityComponent activityComponent);

    @Override
    public AlertDialog getAlert() {
        if (alert != null) {
            alert.dismiss();
            alert = new AlertDialog.Builder(this).create();
        }
        return alert;
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


    protected int getContentViewID() {
        return -1;
    }

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
        if (alert != null && alert.isShowing()) {
            alert.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }
}
