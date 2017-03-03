package com.cylan.jiafeigou.n;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.NotifyManager;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.view.misc.SystemUiHider;
import com.cylan.jiafeigou.n.view.splash.BeforeLoginFragment;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.widget.SystemBarTintManager;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.cylan.jiafeigou.misc.INotify.KEY_NEED_EMPTY_NOTIFICATION;

/**
 * Created by cylan-hunt on 16-6-6.
 */

public class BaseFullScreenFragmentActivity<T extends BasePresenter> extends AppCompatActivity {

    protected T basePresenter;
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
    protected void onStart() {
        super.onStart();
        if (basePresenter != null)
            basePresenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (basePresenter != null)
            basePresenter.stop();
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
            showSystemUI();
//            attrs.flags ^= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                attrs.flags ^= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
//            }
        } else {
            hideSystemUI();
//            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                attrs.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
//            }
        }
        getWindow().setAttributes(attrs);
        checkSystemHider();
        showSystemBar(port, delay);
        //状态栏的背景色
        setSystemBarTintEnable(port);
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    protected void setSystemBarTintEnable(boolean enable) {
        if (tintManager != null)
            tintManager.setStatusBarTintEnabled(enable);
    }

    private static long time = 0;

    @Override
    public void onBackPressed() {

//        if (isBeforLog()) {
//            if (System.currentTimeMillis() - time < 1500) {
//                finish();
//            } else {
//                time = System.currentTimeMillis();
//                Toast.makeText(getApplicationContext(), String.format(getString(R.string.click_back_again_exit),
//                        getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
//            }
//            return;
//        }

        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        if (theLastActivity()) {
            if (System.currentTimeMillis() - time < 1500) {
                super.onBackPressed();
            } else {
                time = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), String.format(getString(R.string.click_back_again_exit),
                        getString(R.string.app_name)), Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onBackPressed();
        }
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

    protected boolean isBeforLog() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> list = fm.getFragments();

        if (list == null) {
            return false;
        }

        if (list.size() == 1 && list.get(0) instanceof BeforeLoginFragment) {
            return true;
        } else {
            return false;
        }
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
