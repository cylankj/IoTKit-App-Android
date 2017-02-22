package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cylan.jiafeigou.R;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：zsl
 * 创建时间：2017/2/17
 * 描述：
 */
public class WebsiteFragement extends Fragment {

    @BindView(R.id.wv_website)
    WebView wvWebsite;

    public static WebsiteFragement getInstance(Bundle bundle) {
        WebsiteFragement fragment = new WebsiteFragement();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_website, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        enterWeb();
    }

    private void enterWeb() {
        wvWebsite.setVisibility(View.VISIBLE);
        String agreementUrl;
        Locale locale = getContext().getResources().getConfiguration().locale;
        final String c = locale.toString();

        if (c.contains("zh")) {
            agreementUrl = "http://www.jfgou.com/";
        } else {
            agreementUrl = "http://www.cleverdog.com.cn/";
        }
        WebSettings settings = wvWebsite.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);
        wvWebsite.removeJavascriptInterface("searchBoxJavaBridge_");
        wvWebsite.removeJavascriptInterface("accessibilityTraversal");
        wvWebsite.removeJavascriptInterface("accessibility");
        wvWebsite.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        wvWebsite.loadUrl(agreementUrl);
    }

}
