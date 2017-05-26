package com.cylan.jiafeigou.ads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AdsDetailActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.w_ads_content)
    WebView wAdsContent;
    @BindView(R.id.back)
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_detail);
        ButterKnife.bind(this);
        AdsStrategy.AdsDescription description = getIntent().getParcelableExtra(JConstant.KEY_ADD_DESC);
        if (description == null) {
            throw new IllegalArgumentException("错了");
        }
        wAdsContent.setVisibility(View.VISIBLE);
        WebSettings settings = wAdsContent.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);
        wAdsContent.removeJavascriptInterface("searchBoxJavaBridge_");
        wAdsContent.removeJavascriptInterface("accessibilityTraversal");
        wAdsContent.removeJavascriptInterface("accessibility");
        wAdsContent.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        wAdsContent.loadUrl(description.tagUrl);
        description.showCount++;
        PreferencesUtils.putString(JConstant.KEY_ADD_DESC, new Gson().toJson(description));
        try {
            BaseApplication.getAppComponent().getCmd().countADClick(description.showCount,
                    PackageUtils.getAppVersionName(ContextUtils.getContext()), description.tagUrl);
        } catch (JfgException e) {
            if (BuildConfig.DEBUG) throw new IllegalArgumentException("错了");
        }
    }

    @OnClick(R.id.back)
    public void onClick() {
        Intent intent = new Intent(this, NewHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishExt();
    }

    @Override
    public void onBackPressed() {
        onClick();
    }
}
