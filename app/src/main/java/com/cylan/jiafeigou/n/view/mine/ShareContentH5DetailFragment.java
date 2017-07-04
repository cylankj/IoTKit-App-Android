package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.databinding.FragmentShareContentH5DetailBinding;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.share.ShareManager;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.utils.WonderGlideURL;
import com.google.gson.Gson;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/31.
 */

public class ShareContentH5DetailFragment extends BaseFragment {

    private FragmentShareContentH5DetailBinding h5DetailBinding;
    private DpMsgDefine.DPShareItem shareItem;
    private Subscription subscribe;
    private Runnable delete;

    public static ShareContentH5DetailFragment newInstance(DpMsgDefine.DPShareItem shareItem, Runnable delete) {
        ShareContentH5DetailFragment fragment = new ShareContentH5DetailFragment();
        fragment.delete = delete;
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
        h5DetailBinding.headerMenuShare.setEnabled(false);
        h5DetailBinding.headerMenuDelete.setEnabled(false);
    }

    private void delete(View view) {
        new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.Tap3_ShareDevice_UnshareTips))
                .setPositiveButton(R.string.OK, (dialog, which) -> {
                    dialog.dismiss();
                    AppLogger.d("正在取消分享");
                    delete();
                })
                .setNegativeButton(R.string.CANCEL, null)
                .show();

    }

    private void delete() {
        AppLogger.e("delete");
        if (subscribe != null && subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        if (NetUtils.getNetType(getContext()) == -1) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }

        subscribe = Observable.just(shareItem)
                .map(items -> new DPEntity(null, 606, items.version, DBAction.DELETED, null))
                .observeOn(Schedulers.io())
                .flatMap(ret -> BaseApplication.getAppComponent().getTaskDispatcher().perform(ret))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    AppLogger.d("取消分享返回结果为:" + new Gson().toJson(result));
                    if (result.getResultCode() == 0) {
                        getActivity().getSupportFragmentManager().popBackStack();
                        if (delete != null) {
                            delete.run();
                        }
                    } else {
                        ToastUtil.showNegativeToast(getString(R.string.Tips_DeleteFail));
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    private void share(View view) {
        AppLogger.e("share");
        new WonderGlideURL(shareItem.toWonderItem())
                .fetchFile(filePath -> {
                    ShareManager.byWeb(getActivity())
                            .withUrl(shareItem.url)
                            .withThumb(filePath)
                            .share();
                });
    }

    @Override
    protected void onEnterAnimationFinished() {
        super.onEnterAnimationFinished();
        WebSettings settings = h5DetailBinding.shareH5WebView.getSettings();
        settings.setJavaScriptEnabled(true);
        h5DetailBinding.shareH5WebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                h5DetailBinding.headerMenuDelete.setEnabled(true);
                h5DetailBinding.headerMenuShare.setEnabled(true);
            }

        });
        h5DetailBinding.shareH5WebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                h5DetailBinding.webViewLoading.setProgress(newProgress);
                h5DetailBinding.webViewLoading.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });
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
        ViewUtils.setViewPaddingStatusBar(h5DetailBinding.headerToolbarContainer);
    }

    @Override
    public void onStop() {
        super.onStop();
        ViewUtils.clearViewPaddingStatusBar(h5DetailBinding.headerToolbarContainer);
        if (subscribe != null && subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
    }
}
