package com.cylan.jiafeigou.support.splash;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.support.login.LoginActivity;

/**
 * Created by yanzhendong on 2017/7/5.
 */

public class SplashActivity extends BaseActivity<SplashContact.Presenter> implements SplashContact.View {



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.initPermissions();
    }

    @Override
    public void onEnterLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);

        startActivity(intent);
        finish();//不在回退栈
    }

    @Override
    public void onExitApp() {

    }
}
