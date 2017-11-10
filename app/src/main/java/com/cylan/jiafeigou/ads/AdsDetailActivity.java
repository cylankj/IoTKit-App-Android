package com.cylan.jiafeigou.ads;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.share.ShareManager;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AdsDetailActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.w_ads_content)
    WebView wAdsContent;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.v_progress)
    ProgressBar vProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_detail);
        ButterKnife.bind(this);
        final AdsStrategy.AdsDescription description = getIntent().getParcelableExtra(JConstant.KEY_ADD_DESC + JFGRules.getLanguageType());
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
        wAdsContent.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                vProgress.setProgress(newProgress);
                if (newProgress == 0) {
                    vProgress.setVisibility(View.VISIBLE);
                }
                if (newProgress == 100) {
                    vProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
        wAdsContent.loadUrl(description.tagUrl);
        try {
            BaseApplication.getAppComponent().getCmd().countADClick(description.showCount,
                    PackageUtils.getAppVersionName(ContextUtils.getContext()), description.tagUrl);
        } catch (JfgException e) {
            if (BuildConfig.DEBUG) {
                throw new IllegalArgumentException("错了");
            }
        }
        customToolbar.setBackAction(v -> onClick());
        customToolbar.getTvToolbarRight().setOnClickListener(v -> {
            AppLogger.d("分享");
            GlideApp.with(this)
                    .downloadOnly()
                    .load(description.url)
                    .onlyRetrieveFromCache(true)
                    .into(new SimpleTarget<File>() {
                        @Override
                        public void onResourceReady(File resource, Transition<? super File> transition) {
                            ShareManager.byWeb(AdsDetailActivity.this)
                                    .withUrl(description.tagUrl)
                                    .withDescription(description.url)
                                    .withThumb(resource.getAbsolutePath())
                                    .share();
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            ShareManager.byWeb(AdsDetailActivity.this)
                                    .withUrl(description.tagUrl)
                                    .withDescription(description.url)
                                    .share();
                        }
                    });
            if (customToolbar.getTvToolbarRight() != null) {
                ViewUtils.setDrawablePadding(customToolbar.getTvToolbarRight(), R.drawable.details_icon_share, 0);
            }
//            Intent intent = new Intent(getContext(), ShareMediaActivity.class);
//            intent.putExtra(ShareConstant.SHARE_CONTENT, ShareConstant.SHARE_CONTENT_WEB_URL);
//            intent.putExtra(ShareConstant.SHARE_CONTENT_WEB_URL_EXTRA_THUMB_PATH, description.tagUrl);
//            intent.putExtra(ShareConstant.SHARE_CONTENT_WEB_URL_EXTRA_LINK_URL, description.tagUrl);
//            startActivity(intent);
        });
    }

    /**
     * 结束广告页面
     */
    public void onClick() {
        Intent intent = new Intent(this, NewHomeActivity.class);
        if (BaseApplication.getAppComponent().getSourceManager().getLoginState() == LogState.STATE_ACCOUNT_ON) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            //没有登录,跳转到登录页面.
            setResult(JConstant.CODE_AD_FINISH);
        }
        finish();
    }

    @Override
    public boolean performBackIntercept(boolean willExit) {
        onClick();
        return true;
    }
}
