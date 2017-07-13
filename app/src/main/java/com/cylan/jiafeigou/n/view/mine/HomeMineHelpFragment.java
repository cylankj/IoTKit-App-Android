package com.cylan.jiafeigou.n.view.mine;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.CheckServerTrustedWebViewClient;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

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
@Badge(parentTag = "HomeMineFragment")
public class HomeMineHelpFragment extends IBaseFragment {

    @BindView(R.id.wv_mine_help)
    WebView mWvHelp;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.v_progress)
    ProgressBar vProgress;


    public static HomeMineHelpFragment newInstance(Bundle bundle) {
        HomeMineHelpFragment fragment = new HomeMineHelpFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferencesUtils.putBoolean(JConstant.KEY_HELP_GUIDE, false);
        RxBus.getCacheInstance().postSticky(new RxEvent.InfoUpdate());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_help, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle.containsKey(JConstant.KEY_SHOW_SUGGESTION)) {
            customToolbar.setToolbarRightTitle("");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        showWebView();
    }

    @Override
    public void onResume() {
        super.onResume();
        TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
        TreeNode node = helper.findTreeNodeByName(this.getClass().getSimpleName());
        customToolbar.showToolbarRightHint(node != null && node.getNodeCount() > 0);
    }

    /**
     * 当进度条加载完成的时候显示该webView
     */
    private void showWebView() {
        if (getView() != null) getView().post(() -> initWebView(LinkManager.getHelpWebUrl()));
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right})
    public void onClick(View view) {
        switch (view.getId()) {
            //点击退回home_mine的fragment
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            //点击进入意见反馈的页面
            case R.id.tv_toolbar_right:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_toolbar_right));
                startActivity(new Intent(getActivity(), FeedbackActivity.class));
                break;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(String url) {
        mWvHelp.getSettings().setAppCacheMaxSize(5 * 1024 * 1024); // 5MB
        mWvHelp.getSettings().setAppCachePath(getContext().getApplicationContext().getCacheDir().getAbsolutePath());
        mWvHelp.getSettings().setAllowFileAccess(true);
        mWvHelp.getSettings().setAppCacheEnabled(true);
        mWvHelp.getSettings().setJavaScriptEnabled(true);
        mWvHelp.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); // load online by default
        if (NetUtils.getJfgNetType() != 0) { // loading offline
            mWvHelp.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        try {
            mWvHelp.setWebViewClient(new CheckServerTrustedWebViewClient(ContextUtils.getContext()) {
                //                @Override
                //                public void onProgressChanged(WebView view, int newProgress) {
                //                    vProgress.setProgress(newProgress);
                //                    if (newProgress == 0) vProgress.setVisibility(View.VISIBLE);
                //                    if (newProgress == 100) vProgress.setVisibility(View.INVISIBLE);
                //                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        AppLogger.d("help:" + url);
        mWvHelp.loadUrl(url);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWvHelp.removeAllViews();
        mWvHelp.destroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
