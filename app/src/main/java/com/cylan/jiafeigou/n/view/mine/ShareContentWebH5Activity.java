package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
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

import java.io.File;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/31.
 */

public class ShareContentWebH5Activity extends BaseActivity {

    private FragmentShareContentH5DetailBinding h5DetailBinding;
    private DpMsgDefine.DPShareItem shareItem;
    private Subscription subscribe;

    public static Intent getLaunchIntent(Context context, DpMsgDefine.DPShareItem shareItem) {
        Intent intent = new Intent(context, ShareContentWebH5Activity.class);
        intent.putExtra("shareItem", shareItem);
        return intent;
    }

    @Override
    protected View getContentRootView() {
        h5DetailBinding = FragmentShareContentH5DetailBinding.inflate(getLayoutInflater());
        return h5DetailBinding.getRoot();
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        h5DetailBinding.headerMenuBack.setOnClickListener(v -> onBackPressed());
        h5DetailBinding.headerMenuShare.setOnClickListener(this::share);
        h5DetailBinding.headerMenuDelete.setOnClickListener(this::delete);
        h5DetailBinding.headerMenuShare.setEnabled(false);
        h5DetailBinding.headerMenuDelete.setEnabled(false);
    }

    private void delete(View view) {
        new AlertDialog.Builder(this)
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
        if (subscribe != null && !subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        if (NetUtils.getNetType(this) == -1) {
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
                        finish();
                    } else {
                        ToastUtil.showNegativeToast(getString(R.string.Tips_DeleteFail));
                    }
                }, e -> {
                    AppLogger.e(e.getMessage());
                });
    }

    private void share(View view) {
        AppLogger.e("share");
        Glide.with(this)
                .load(new WonderGlideURL(shareItem.toWonderItem()))
                .downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        ShareManager.byWeb(ShareContentWebH5Activity.this)
                                .withUrl(shareItem.url)
                                .withThumb(resource.getAbsolutePath())
                                .share();
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        ShareManager.byWeb(ShareContentWebH5Activity.this)
                                .withUrl(shareItem.url)
                                .share();
                    }
                });
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
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
        shareItem = getIntent().getParcelableExtra("shareItem");
        if (shareItem != null) {
            h5DetailBinding.shareH5WebView.loadUrl(shareItem.url);
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
        if (subscribe != null && !subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
    }

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {

    }
}
