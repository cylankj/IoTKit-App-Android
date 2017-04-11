package com.cylan.jiafeigou.n.view.mine;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;

import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v7.view.menu.MenuView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;

import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;

import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/8 11:26
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineHelpFragment extends Fragment {

    @BindView(R.id.wv_mine_help)
    WebView mWvHelp;

    private HomeMineHelpSuggestionFragment homeMineHelpSuggestionFragment;

    public static HomeMineHelpFragment newInstance(Bundle bundle) {
        HomeMineHelpFragment fragment = new HomeMineHelpFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeMineHelpSuggestionFragment = HomeMineHelpSuggestionFragment.newInstance(new Bundle());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_help, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showWebView();
    }

    /**
     * 当进度条加载完成的时候显示该webView
     */
    private void showWebView() {
        String agreementUrl = getString(R.string.help_url);
        if (agreementUrl.contains("–")) {
            agreementUrl = agreementUrl.replace("–", "-");
        }
/*        WebSettings settings = mWvHelp.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setBlockNetworkImage(false);
        settings.setSavePassword(false);
        String cacheDirPath = getContext().getFilesDir().getAbsolutePath()+"/webview";
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setAppCachePath(cacheDirPath);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        mWvHelp.removeJavascriptInterface("searchBoxJavaBridge_");
        mWvHelp.removeJavascriptInterface("accessibilityTraversal");
        mWvHelp.removeJavascriptInterface("accessibility");
        WebSettings webseting = mWvHelp.getSettings();
        webseting.setJavaScriptEnabled(true);
        mWvHelp.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });
        AppLogger.d("url:" + agreementUrl);
        mWvHelp.loadUrl(agreementUrl);*/
        initWebView(agreementUrl);
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right})
    public void onClick(View view) {
        switch (view.getId()) {
            //点击退回home_mine的fragment
            case R.id.tv_toolbar_icon:
                getFragmentManager().popBackStack();
                break;
            //点击进入意见反馈的页面
            case R.id.tv_toolbar_right:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_toolbar_right));
                AppLogger.e("tv_mine_help_suggestion");
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, homeMineHelpSuggestionFragment, "homeMineHelpSuggestionFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
                break;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String url) {
        mWvHelp.getSettings().setJavaScriptEnabled(true);
        mWvHelp.getSettings().setDefaultTextEncodingName("utf-8");
        mWvHelp.getSettings().setAllowFileAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWvHelp.getSettings().setMixedContentMode(
                    WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        // 设置 缓存模式
        if (NetUtils.isNetworkAvailable(ContextUtils.getContext())) {
            mWvHelp.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            mWvHelp.getSettings().setCacheMode(
                    WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        // webView.getSettings().setBlockNetworkImage(true);// 把图片加载放在最后来加载渲染
        mWvHelp.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 支持多窗口
        mWvHelp.getSettings().setSupportMultipleWindows(true);
        // 开启 DOM storage API 功能
        mWvHelp.getSettings().setDomStorageEnabled(true);
        // 开启 Application Caches 功能
        String cacheDirPath = getContext().getApplicationContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        mWvHelp.getSettings().setAppCachePath(cacheDirPath);
        mWvHelp.getSettings().setAppCacheMaxSize(1024*1024*8);
        mWvHelp.getSettings().setDatabaseEnabled(true);
        mWvHelp.getSettings().setAppCacheEnabled(true);
        onLoad(url);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    public void onLoad(String url) {
        try {
            mWvHelp.setWebViewClient(new WebViewClient() {
                @Override
                public void onLoadResource(WebView view, String url) {
                    super.onLoadResource(view, url);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView webview,
                                                        String url) {
                    webview.loadUrl(url);
                    return true;
                }

                @Override
                public void onReceivedSslError(WebView view,
                                               SslErrorHandler handler, SslError error) {
                    handler.proceed();  //接受所有证书
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                }

                @Override
                public void onReceivedError(WebView view, int errorCode,
                                            String description, String failingUrl) {

                }
            });
            mWvHelp.loadUrl(url);
        } catch (Exception e) {
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWvHelp.removeAllViews();
        mWvHelp.destroy();
    }
}
