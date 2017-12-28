package com.cylan.jiafeigou.base.wrapper;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.request.FutureTarget;
import com.cylan.jiafeigou.base.view.CallablePresenter;
import com.cylan.jiafeigou.base.view.CallableView;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.BellerSupervisor;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Created by yzd on 16-12-30.
 */

public abstract class BaseCallablePresenter<V extends CallableView> extends BaseViewablePresenter<V> implements CallablePresenter {
    protected Caller mCaller;
    protected Caller mHolderCaller;
    protected boolean mIsInViewerMode = false;

    public BaseCallablePresenter(V view) {
        super(view);
    }


    @Override
    protected String getViewHandler() {
        return mCaller == null ? null : mCaller.caller;
    }

    @Override
    public void pickup() {
        AppLogger.d("正在接听");
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


    @Override
    public void newCall(Caller caller) {
        //直播中的门铃呼叫
//                                                mView.onNewCallWhenInLive(mHolderCaller.caller);
//说明不是自己接听的
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.CallResponse.class)
                .mergeWith(
                        Observable.just(mHolderCaller = caller)
                                .observeOn(AndroidSchedulers.mainThread())
                                .filter(who -> !mIsInViewerMode)
                                .flatMap(who -> {
//                                    switch (mView.onResolveViewLaunchType()) {
//                                        case JConstant.VIEW_CALL_WAY_LISTEN:
                                    if (mCaller != null && mHolderCaller != null) {//直播中的门铃呼叫
//                                                mView.onNewCallWhenInLive(mHolderCaller.caller);
                                    } else if (mHolderCaller != null) {
                                        mView.onListen();
                                        AppLogger.d("收到门铃呼叫");
                                    }
//                                            break;
//                                        case JConstant.VIEW_CALL_WAY_VIEWER:
//                                            mCaller = mHolderCaller;
//                                            mHolderCaller = null;
//                                            startViewer();
//                                            break;
//                                    }
                                    return RxBus.getCacheInstance().toObservable(RxEvent.CallResponse.class);
                                })
                )
                .first()
                .timeout(JFGRules.getCallTimeOut(sourceManager.getDevice(uuid)), TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(answer -> {
                    if (!answer.self) {//说明不是自己接听的
                        AppLogger.d("门铃在其他端接听了");
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
                        mHolderCaller = null;
                        AppLogger.w("门铃呼叫超时了!!!");
                        mView.onNewCallTimeOut();
                    }
                    AppLogger.e(e.getMessage());
                });
        addDestroySubscription(subscribe);
    }

    @Override
    public void stop() {
        super.stop();
        mIsInViewerMode = false;
    }

    @Override
    public void loadPreview(String url) {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                File file = null;
                String picture = null;
                while (file == null && !subscriber.isUnsubscribed()) {
                    try {
                        picture = BellerSupervisor.getBellerPicture(uuid);
                        if (TextUtils.isEmpty(picture)) {
                            SystemClock.sleep(1000);
                            continue;
                        }
                        FutureTarget<File> fileFutureTarget = GlideApp.with(mView.activity())
                                .downloadOnly()
                                .load(picture)
                                .submit();
                        Log.d("LoadPreview", "load preview:" + picture);
                        file = fileFutureTarget.get();
                        removeLastPreview();
                        String filePath = JConstant.MEDIA_PATH + File.separator + "." + uuid + System.currentTimeMillis();
                        PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, filePath);
                        FileUtils.copyFile(file, new File(filePath));
                        AppLogger.w("截图文件地址:" + filePath);
                    } catch (Exception e) {
                        Log.d("LoadPreview", "load preview error:" + picture + ",retrying");
                        SystemClock.sleep(1000);
                    }
                }
                subscriber.onNext(picture);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .takeUntil(RxBus.getCacheInstance().toObservable(Notify.class))
                .first()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (mView != null) {
                            Log.d("LoadPreview", "finished" + s);
                            mView.onShowVideoPreviewPicture(s);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e("load picture error:" + throwable.getMessage());
                    }
                });
        addDestroySubscription(subscribe);
    }

    public static class Notify {
        public boolean success;

        public Notify(boolean success) {
            this.success = success;
        }
    }
}
