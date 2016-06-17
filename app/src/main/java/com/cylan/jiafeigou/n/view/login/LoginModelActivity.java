package com.cylan.jiafeigou.n.view.login;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.utils.ToastUtil;

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
        LoginModel1Fragment fragment = LoginModel1Fragment.newInstance(null);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_down_in, R.anim.slide_down_out).
                add(R.id.fLayout_login_model_container, fragment).commit();
    }

    private static long time = 0;


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if (System.currentTimeMillis() - time < 1500) {
        } else {
            time = System.currentTimeMillis();
            ToastUtil.showToast(this,
                    String.format(getString(R.string.click_back_again_exit),
                            getString(R.string.app_name)));
        }
    }


    private boolean checkExtraFragment() {
        final int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        } else return false;
    }
}
