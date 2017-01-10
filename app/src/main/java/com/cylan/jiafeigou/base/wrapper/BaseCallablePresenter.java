package com.cylan.jiafeigou.base.wrapper;

import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.text.TextUtils;

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
import com.cylan.jiafeigou.utils.ContextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yzd on 16-12-30.
 */

public abstract class BaseCallablePresenter<V extends CallableView> extends BaseViewablePresenter<V> implements CallablePresenter {
    protected Caller mCaller;

    @Override
    @CallSuper
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getCallAnswerObserverSub());
    }


    @Override
    protected String onResolveViewIdentify() {
        return mCaller == null ? "" : mCaller.caller + "";
    }

    protected Subscription getCallAnswerObserverSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CallAnswerd.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callAnswer -> {
                    callAnswerInOther();
                }, Throwable::printStackTrace);
    }

    public void pickup() {
        mView.onViewer();
        waitForPicture(mCaller.picture, this::startViewer);
    }

    protected void callAnswerInOther() {
        mHasResolution = true;
        mView.onCallAnswerInOther();
    }

    public void newCall(Caller caller) {
        mCaller = caller;
        switch (mView.onResolveViewLaunchType()) {
            case JConstant.VIEW_CALL_WAY_LISTEN:
                if (TextUtils.equals(mInViewCallWay, JConstant.VIEW_CALL_WAY_VIEWER)) {
                    return;//当主动查看门铃时忽略门铃呼叫
                }
                mIsSpeakerOn = true;//接听门铃默认打开麦克风

                if (!TextUtils.isEmpty(mInViewIdentify)) {
                    mView.onNewCallWhenInLive(mCaller.caller);
                } else {
                    mView.onListen();
                    waitForPicture(mCaller.picture, () -> {
                        if (mView != null) mView.onPreviewPicture(mCaller.picture);
                    });
                }
                break;
            case JConstant.VIEW_CALL_WAY_VIEWER:
                mIsSpeakerOn = false;//主动查看门铃默认关闭麦克风
                startViewer();
                break;
        }

        setSpeaker(mIsSpeakerOn);

        mView.onSpeaker(mIsSpeakerOn);
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
