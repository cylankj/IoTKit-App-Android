package com.cylan.jiafeigou.n;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.SystemBarTintManager;
import com.cylan.utils.ListUtils;

import java.util.List;

/**
 * Created by cylan-hunt on 16-6-6.
 */

public class BaseFullScreenFragmentActivity extends FragmentActivity implements FragmentManager.OnBackStackChangedListener {

    SystemBarTintManager tintManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            tintManager = new SystemBarTintManager(this);
            setSystemBarTintEnable(true);
        }
        getSupportFragmentManager().addOnBackStackChangedListener(this);
    }

    protected void setSystemBarTintEnable(boolean enable) {
        if (tintManager != null)
            tintManager.setStatusBarTintEnabled(enable);
    }

    private static long time = 0;

    @Override
    public void onBackPressed() {
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

    protected boolean checkExtraFragment() {
        final int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        } else return false;
    }


    @Override
    public void onBackStackChanged() {

    }

    protected boolean popAllFragmentStack() {
        FragmentManager fm = getSupportFragmentManager();
        boolean pop = false;
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
            pop = true;
        }
        return pop;
    }


    protected void finishExt() {
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
    }
}
