package com.cylan.jiafeigou.n.view.mine;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;

import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.databinding.FragmentShareContentH5DetailBinding;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.share.ShareConstant;
import com.cylan.jiafeigou.support.share.ShareMediaActivity;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.utils.WonderGlideURL;

/**
 * Created by yanzhendong on 2017/5/31.
 */

public class ShareContentH5DetailFragment extends BaseFragment {

    private FragmentShareContentH5DetailBinding h5DetailBinding;
    private DpMsgDefine.DPShareItem shareItem;

    public static ShareContentH5DetailFragment newInstance(DpMsgDefine.DPShareItem shareItem) {
        ShareContentH5DetailFragment fragment = new ShareContentH5DetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("shareItem", shareItem);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected void setFragmentComponent(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        h5DetailBinding = FragmentShareContentH5DetailBinding.inflate(inflater);
        return h5DetailBinding.getRoot();
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        h5DetailBinding.headerMenuBack.setOnClickListener(v -> getActivity().onBackPressed());
        h5DetailBinding.headerMenuShare.setOnClickListener(this::share);
        h5DetailBinding.headerMenuDelete.setOnClickListener(this::delete);
    }

    private void delete(View view) {
        AppLogger.e("delete");
    }

    private void share(View view) {
        AppLogger.e("share");
        new WonderGlideURL(shareItem.toWonderItem())
                .fetchFile(filePath -> {
                    Intent intent = new Intent(getContext(), ShareMediaActivity.class);
                    intent.putExtra(ShareConstant.SHARE_CONTENT, ShareConstant.SHARE_CONTENT_WEB_URL);
                    intent.putExtra(ShareConstant.SHARE_CONTENT_WEB_URL_EXTRA_LINK_URL, shareItem.url);
                    intent.putExtra(ShareConstant.SHARE_CONTENT_WEB_URL_EXTRA_THUMB_PATH, filePath);
                    startActivity(intent);
                });
    }

    @Override
    protected void onEnterAnimationFinished() {
        super.onEnterAnimationFinished();
        WebSettings settings = h5DetailBinding.shareH5WebView.getSettings();
        settings.setJavaScriptEnabled(true);
        Bundle arguments = getArguments();
        if (arguments != null) {
            shareItem = arguments.getParcelable("shareItem");
            if (shareItem != null) {
                h5DetailBinding.shareH5WebView.loadUrl(shareItem.url);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewUtils.setViewPaddingStatusBar(h5DetailBinding.headerToolbar);
    }

    @Override
    public void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(h5DetailBinding.headerToolbar);
    }
}
