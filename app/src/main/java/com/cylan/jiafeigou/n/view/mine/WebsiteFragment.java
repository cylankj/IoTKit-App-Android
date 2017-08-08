package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.CheckServerTrustedWebViewClient;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 作者：zsl
 * 创建时间：2017/2/17
 * 描述：
 */
public class WebsiteFragment extends Fragment {

    @BindView(R.id.wv_website)
    WebView wvWebsite;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    public static WebsiteFragment getInstance(Bundle bundle) {
        WebsiteFragment fragment = new WebsiteFragment();
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customToolbar.setBackAction((View v) -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        enterWeb();
    }

    private void enterWeb() {
        wvWebsite.setVisibility(View.VISIBLE);
        String agreementUrl = getString(R.string.show_web);
        if (!agreementUrl.startsWith("http://")) {
            agreementUrl = "http://" + agreementUrl;
        }
        AppLogger.d("官网:" + agreementUrl);
        WebSettings settings = wvWebsite.getSettings();
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptEnabled(true);
        settings.setSavePassword(false);
        wvWebsite.removeJavascriptInterface("searchBoxJavaBridge_");
        wvWebsite.removeJavascriptInterface("accessibilityTraversal");
        wvWebsite.removeJavascriptInterface("accessibility");
        try {
            wvWebsite.setWebViewClient(new CheckServerTrustedWebViewClient(ContextUtils.getContext()) {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }

            });
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        wvWebsite.loadUrl(agreementUrl);
    }

}
