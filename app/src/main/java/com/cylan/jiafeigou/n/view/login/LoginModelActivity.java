package com.cylan.jiafeigou.n.view.login;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;

/**
 * Created by lxh on 16-6-12.
 */

public class LoginModelActivity extends BaseFullScreenFragmentActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_model);

        showLoginFragment();
    }

    private void showLoginFragment() {
        LoginFragment fragment = LoginFragment.newInstance(null);
        getSupportFragmentManager().beginTransaction().
                add(R.id.fLayout_login_model_container, fragment, "login").commit();
    }

}
