package com.cylan.jiafeigou.base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.WindowManager;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.misc.NotifyManager;
import com.cylan.jiafeigou.n.view.misc.SystemUiHider;
import com.cylan.jiafeigou.widget.SystemBarTintManager;
import com.cylan.jiafeigou.utils.ListUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.cylan.jiafeigou.misc.INotify.KEY_NEED_EMPTY_NOTIFICATION;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseFullScreenActivity<P extends JFGPresenter> extends BaseActivity<P> {

    private SystemBarTintManager tintManager;
    private WeakReference<SystemUiHider> systemUiHiderWeakReference;

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

    /**
     * 用于隐藏系统状态栏
     */
    private void checkSystemHider() {
        if (systemUiHiderWeakReference == null
                || systemUiHiderWeakReference.get() == null) {
            systemUiHiderWeakReference = new WeakReference<>(new SystemUiHider(getWindow().getDecorView(), true));
        }
    }

    private void showSystemBar(boolean show, final long delayTime) {
        systemUiHiderWeakReference.get().setSupportAutoHide(!show);
        if (show) {
            systemUiHiderWeakReference.get().show();
        } else {
            systemUiHiderWeakReference.get().delayedHide(delayTime);
        }
    }

    /**
     * 处理statusBar和NavigationBar
     *
     * @param port
     */
    protected void handleSystemBar(boolean port, final long delay) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (port) {
            attrs.flags ^= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                attrs.flags ^= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            }
        } else {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            }
        }
        getWindow().setAttributes(attrs);
        checkSystemHider();
        showSystemBar(port, delay);
        //状态栏的背景色
        setSystemBarTintEnable(port);
    }

    protected void setSystemBarTintEnable(boolean enable) {
        if (tintManager != null)
            tintManager.setStatusBarTintEnabled(enable);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected boolean theLastActivity() {
        return true;
    }

    /**
     * 检查是否有子{@link Fragment}
     *
     * @return
     */
    protected boolean checkExtraChildFragment() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> list = fm.getFragments();
        if (ListUtils.isEmpty(list))
            return false;
        for (Fragment frag : list) {
            if (frag != null && frag.isVisible()) {
                FragmentManager childFm = frag.getChildFragmentManager();
                if (childFm != null && childFm.getBackStackEntryCount() > 0) {
//                    childFm.popBackStack();
                    childFm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return
     */
    protected boolean checkExtraFragment() {
        final int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        } else return false;
    }

    /**
     * 弹出所有 {@link Fragment}
     *
     * @return
     */
    protected boolean popAllFragmentStack() {
        FragmentManager fm = getSupportFragmentManager();
        boolean pop = false;
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
            pop = true;
        }
        return pop;
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
