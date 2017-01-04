package com.cylan.jiafeigou.n.engine;

import android.content.Intent;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yzd on 16-12-23.
 */

public class GlobalBellCallSource {

    private static GlobalBellCallSource sInstance;
    private Subscription mSubscription;


    private RxEvent.BellCallEvent mHolderBellCall;//正在等待的门铃消息，这时要判断是否可以开启门铃页面

    public static GlobalBellCallSource getInstance() {
        if (sInstance == null) {
            sInstance = new GlobalBellCallSource();
        }
        return sInstance;
    }

    public void register() {
        mSubscription = RxBus.getCacheInstance().toObservable(RxEvent.BellCallEvent.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bellCallEvent -> {
                    mHolderBellCall = bellCallEvent;
                    launchBellLive();
                });
    }

    private void launchBellLive() {
        Log.e("ABC", "launchBellLive: " + mHolderBellCall.caller.cid);
        Intent intent = new Intent(ContextUtils.getContext(), BellLiveActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_LISTEN);
        intent.putExtra(JConstant.VIEW_CALL_WAY_EXTRA, mHolderBellCall.caller);
        ContextUtils.getContext().startActivity(intent);

    }

    private void handlerBellInLive() {
        Log.e("ABC", "handlerBellInLive: ");
    }

    public void unRegister() {
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }


    public interface R {
        void r();
    }

    /**
     * 兼容性处理，以后会删除
     */
    private void waitBellPictureReady(String url, R r) {
        Glide.with(ContextUtils.getContext()).load(url).
                listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        waitBellPictureReady(url, r);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        r.r();
                        return false;
                    }
                }).preload();


    }
}
