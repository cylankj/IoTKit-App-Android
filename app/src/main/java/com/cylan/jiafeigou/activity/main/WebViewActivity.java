package com.cylan.jiafeigou.activity.main;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.utils.PreferenceUtil;

public class WebViewActivity extends BaseActivity implements OnClickListener {

    public static final String URL = "url";
    public static final String TITLE = "title";
    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fg_comment_webview);
        String url = getIntent().getStringExtra(URL);
        String title = getIntent().getStringExtra(TITLE);
        setTitle(title);
        View view = findViewById(R.id.titlebar1);
        ((TextView) view.findViewById(R.id.title)).setText(title);
        view.findViewById(R.id.back).setOnClickListener(this);

        if (PreferenceUtil.getIsLogout(WebViewActivity.this)) {
            view.setVisibility(View.VISIBLE);
            setBaseTitlebarVisbitly(false);
        } else {
            view.setVisibility(View.GONE);
            setBaseTitlebarVisbitly(true);
        }

        mWebView = (WebView) findViewById(R.id.webview);

        WebSettings settings = mWebView.getSettings();
        settings.setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(false);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);
                return true;
            }
        });

        mWebView.loadUrl(url);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;

            default:
                break;
        }

    }

}