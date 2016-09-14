package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.cylan.jiafeigou.R;

/**
 * 作者：zsl
 * 创建时间：2016/8/31
 * 描述：
 */
public class SuggestionChatContainerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestionchat_container);
        //initTitlebar();
        initFragment();
    }

    private void initTitlebar() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }


    private void initFragment() {
        getSupportFragmentManager().beginTransaction()
                //.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                //, R.anim.slide_in_left, R.anim.slide_out_right)
                .add(R.id.fl_container, new SuggestionChatFragment(), "suggestionChatFragment")
                //.addToBackStack("mineHelpFragment")
                .commit();
    }

}
