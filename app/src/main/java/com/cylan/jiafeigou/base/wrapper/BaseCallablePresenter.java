package com.cylan.jiafeigou.base.wrapper;

import android.os.SystemClock;
import android.support.annotation.CallSuper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.base.view.CallablePresenter;
import com.cylan.jiafeigou.base.view.CallableView;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by yzd on 16-12-30.
 */

public abstract class BaseCallablePresenter<V extends CallableView> extends BaseViewablePresenter<V> implements CallablePresenter {
    protected Caller mCaller;
    protected Caller mHolderCaller;
    protected boolean mIsInViewerMode = false;

    @Override
    @CallSuper
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
    }

    @Override
    protected void onRegisterResponseParser() {
        super.onRegisterResponseParser();
    }

    @Override
    protected String onResolveViewIdentify() {
        return mCaller == null ? null : mCaller.caller;
    }

    public void pickup() {
        AppLogger.e("正在接听");
        if (mHolderCaller != null) {
            mCaller = mHolderCaller;
            mHolderCaller = null;
            startViewer();
        }
    }

    @Override
    protected void setViewHandler(String handler) {
        if (handler == null && mCaller != null) {
            mCaller = null;
        }
    }

    public void newCall(Caller caller) {
        Subscription subscription = Observable.just(mHolderCaller = caller)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(who -> !mIsInViewerMode)
                .flatMap(who -> {
                    switch (mView.onResolveViewLaunchType()) {
                        case JConstant.VIEW_CALL_WAY_LISTEN:
                            mIsInViewerMode = false;
                            if (mCaller != null && mHolderCaller != null) {//直播中的门铃呼叫
                                mView.onNewCallWhenInLive(mHolderCaller.caller);
                            } else if (mHolderCaller != null) {
                                mView.onListen();
                                AppLogger.e("收到门铃呼叫");
                                waitForPicture(mHolderCaller.picture, () -> {
                                    if (mView != null)
                                        mView.onPreviewPicture(mHolderCaller.picture);
                                });
                            }
                            break;
                        case JConstant.VIEW_CALL_WAY_VIEWER:
                            mCaller = mHolderCaller;
                            mHolderCaller = null;
                            mIsInViewerMode = true;
                            startViewer();
                            break;
                    }
                    return RxBus.getCacheInstance().toObservable(RxEvent.CallAnswered.class)
                            .timeout(30, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()); //三十秒超时时间,如果无人接听的话
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(answer -> {
                    AppLogger.e("门铃在其他端接听了");
                    if (!answer.self) {//说明不是自己接听的
                        if (mCaller == null) {
                            mView.onCallAnswerInOther();
                            mView.onDismiss();
                        } else {
                            mHolderCaller = null;
                            mView.onCallAnswerInOther();
                        }
                    }
                }, e -> {
                    if (e instanceof TimeoutException) {
                        if (mCaller == null) {//没有正在查看的直播,且当前直播接听超时,则直接关闭退出
                            mView.onNewCallTimeOut();
                            mView.onDismiss();
                        } else if (mHolderCaller != null) {
                            mHolderCaller = null;
                            mView.onNewCallTimeOut();
                        }
                    }
                });
        registerSubscription(subscription);
    }

    @Override
    public void onStop() {
        super.onStop();
        mIsInViewerMode = false;
    }

    protected void waitForPicture(String url, JFGView.Action action) {
        Glide.with(ContextUtils.getContext()).load(url).
                listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        SystemClock.sleep(200);
                        waitForPicture(url, action);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (action != null) action.actionDone();
                        return false;
                    }
                }).preload();
    }
}
