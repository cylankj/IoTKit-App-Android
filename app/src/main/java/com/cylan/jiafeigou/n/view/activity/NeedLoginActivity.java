package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.view.login.LoginFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;

/**
 * Created by cylan-hunt on 16-7-6.
 */
public class NeedLoginActivity<T extends BasePresenter> extends BaseFullScreenFragmentActivity<T> {

    private LoginFragment loginFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void signInFirst(Bundle extra) {
        if (extra == null)
            extra = new Bundle();
        extra.putInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID, android.R.id.content);
        extra.putInt(JConstant.KEY_SHOW_LOGIN_FRAGMENT, 1);
        loginFragment = LoginFragment.newInstance(extra);
        loginFragment.setArguments(extra);
        if (getSupportFragmentManager().findFragmentByTag(loginFragment.getClass().getSimpleName()) != null)
            return;
        View v = findViewById(R.id.rLayout_login);
        if (v != null) {
            try {
                AppLogger.e("loginFragment already added");
                getSupportFragmentManager().beginTransaction().show(loginFragment)
                        .commitAllowingStateLoss();
                return;
            } catch (Exception e) {
            }
        }
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                loginFragment, android.R.id.content, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (loginFragment != null) {
            loginFragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
