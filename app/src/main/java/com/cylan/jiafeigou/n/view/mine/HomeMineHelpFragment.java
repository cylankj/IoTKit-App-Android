package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.lang.ref.WeakReference;

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
//        String agreementUrl= "https://yf.jfgou.com:8081/helps/zh-rCN.html";
        String agreementUrl = getString(R.string.help_url);
        if (agreementUrl.contains("–")) {
            agreementUrl = agreementUrl.replace("–", "-");
        }
        WebSettings settings = mWvHelp.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setBlockNetworkImage(false);
        settings.setSavePassword(false);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setAppCacheEnabled(true);
        mWvHelp.removeJavascriptInterface("searchBoxJavaBridge_");
        mWvHelp.removeJavascriptInterface("accessibilityTraversal");
        mWvHelp.removeJavascriptInterface("accessibility");
        mWvHelp.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                handler.proceed();  //接受所有证书
            }
        });
        AppLogger.d("url:" + agreementUrl);
        mWvHelp.loadUrl(agreementUrl);
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
}
