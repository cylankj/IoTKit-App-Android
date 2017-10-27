package com.cylan.jiafeigou.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.NotifyManager;
import com.cylan.jiafeigou.n.view.misc.SystemUiHider;
import com.cylan.jiafeigou.widget.SystemBarTintManager;

import java.lang.ref.WeakReference;

import static com.cylan.jiafeigou.misc.INotify.KEY_NEED_EMPTY_NOTIFICATION;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseFullScreenActivity<P extends JFGPresenter> extends BaseActivity<P> {

    private SystemBarTintManager tintManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
        overridePendingTransition(getOverridePendingTransition()[0], getOverridePendingTransition()[1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            tintManager = new SystemBarTintManager(this);
            setSystemBarTintEnable(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(KEY_NEED_EMPTY_NOTIFICATION)) {
            //需要发送一个空notification
            NotifyManager.getNotifyManager()
                    .sendNotify(NotifyManager
                            .getNotifyManager()
                            .sendDefaultEmptyNotify());
        }
    }

    protected int[] getOverridePendingTransition() {
        return new int[]{R.anim.slide_in_right, R.anim.slide_out_left};
    }

    protected void setSystemBarTintEnable(boolean enable) {
        if (tintManager != null) {
            tintManager.setStatusBarTintEnabled(enable);
        }
    }

    /**
     * 带有出场动画的
     */
    protected void finishExt() {
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
    }
}
