package com.cylan.jiafeigou.n.view.mine;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.SystemBarTintManager;

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
        initFragment();
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
