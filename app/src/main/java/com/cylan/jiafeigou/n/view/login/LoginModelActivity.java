package com.cylan.jiafeigou.n.view.login;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.test.TestFragment;

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
        TestFragment fragment = TestFragment.newInstance(null,"");
        getSupportFragmentManager().beginTransaction().
                add(R.id.fLayout_login_model_container, fragment, "login")
                .setCustomAnimations(R.anim.slide_in_down,R.anim.slide_down_out)
                .commit();
    }

}
