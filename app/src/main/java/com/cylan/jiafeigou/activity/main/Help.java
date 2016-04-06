package com.cylan.jiafeigou.activity.main;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.activity.video.CallOrConf;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.utils.Utils;

public class Help extends BaseActivity {

    String tag = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tag = getIntent().getStringExtra(CallOrConf.TAG);
        if (tag != null) {
            setTheme(R.style.activity_top_in);
        } else {
            setTheme(R.style.Theme_SlideTop);
        }
        setContentView(R.layout.help);
        setTitle(R.string.USE_HELP);
        if (tag != null) {
            setTheme(R.style.activity_top_in);
        } else {
            setTheme(R.style.Theme_SlideTop);
        }
        setBackBtnOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                }
        );
        WebView mWebView = (WebView) findViewById(R.id.view);
        mWebView.getSettings().setJavaScriptEnabled(true);

        String url = String.format("http://%1$s/help/%2$s.html", Constants.ADDR, Utils.getShortCountryName(this));
        mWebView.loadUrl(url);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (tag != null)
            overridePendingTransition(0, R.anim.slide_down_out);
        else
            overridePendingTransition(0, R.anim.slide_right_out);
        finish();
    }

}
