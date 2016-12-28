package com.cylan.jiafeigou.base;

import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.utils.HandlerThreadUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BasePresenter<V extends JFGView> implements JFGPresenter<V> {
    protected String TAG = getClass().getName();

    protected static JFGSourceManager sSourceManager;
    private static CompositeSubscription sSubscriptions;
    private static BasePresenter sInstance;
    protected V mView;

    protected boolean mResolutionRetryFinished = false;

    /**
     * 这里的feature注册采用延迟注册方式,即在view第一次需要的时候进行注册,
     * 注册是全局性的,一次注册所有的view共用
     */
    private int mViewRequestFeatures;//view需要的feature
    private static int mHasRegisterFeatures;//当前已经注册的feature
    private static long mLastClickedTime;

    public static final int FEATURE_NO_FEATURES = 0;
    public static final int FEATURE_LOGIN_STATE = 0X1;
    public static final int FEATURE_VIDEO_RESOLUTION = 0X1 << 1;
    public static final int FEATURE_VIDEO_DISCONNECT = 0X1 << 2;
    public static final int FEATURE_VIDEO_FLOW_RSP = 0X1 << 3;
    public static final int FEATURE_DOUBLE_CLICK_EXIT = 0X1 << 4;

    static {
        //在APP处于后台时反注册所有已注册的Observer
        RxBus.getCacheInstance()
                .toObservable(RxEvent.AppHideEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(appHideEvent -> {
                    unSubscribe(sSubscriptions);
                    mHasRegisterFeatures = FEATURE_NO_FEATURES;
                    sSubscriptions = null;
                });
    }


    protected static void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && !subscription.isUnsubscribed())
                    subscription.unsubscribe();
            }
        }
    }

    public void onViewAttached(V view) {
        sInstance = this;
        mView = view;
    }

    @Override
    @CallSuper
    public void onStart() {
        mViewRequestFeatures = onResolveViewFeatures();
        onRegisterObserver();
    }

    @Override
    @CallSuper
    public void onStop() {
        onUnRegisterObserver();
    }

    @Override
    public void onViewDetached() {
        mView = null;
        sInstance = null;
    }

    protected void requestFeatures(int features) {
        mViewRequestFeatures |= features;
        onRegisterObserver();
    }

    protected void onLoginStateChanged(RxEvent.LoginRsp loginState) {
    }

    protected void onVideoResolution(JFGMsgVideoResolution resolution) {
    }

    protected void onVideoFlowRsp(JFGMsgVideoRtcp flowRsp) {
    }

    protected void onVideoDisconnected(JFGMsgVideoDisconn disconnect) {
    }

    /**
     * 在这个方法返回view需要得到的feature,基类会自动管理生命周期并回调相关接口
     */
    protected int onResolveViewFeatures() {
        return FEATURE_NO_FEATURES;
    }

    private void onRegisterObserver() {
        if (sSubscriptions == null) {
            sSubscriptions = new CompositeSubscription();
        }
        if ((mViewRequestFeatures & FEATURE_LOGIN_STATE) == FEATURE_LOGIN_STATE) {//需要监听登录状态变化
            requestLoginStateFeature();
        }

        if ((mViewRequestFeatures & FEATURE_VIDEO_RESOLUTION) == FEATURE_VIDEO_RESOLUTION) {//需要监听设备分辨率消息
            requestResolutionFeature();
        }

        if ((mViewRequestFeatures & FEATURE_VIDEO_DISCONNECT) == FEATURE_VIDEO_DISCONNECT) {//需要监听直播视频断开消息
            requestVideoDisconnectFeature();
        }

        if ((mViewRequestFeatures & FEATURE_VIDEO_FLOW_RSP) == FEATURE_VIDEO_FLOW_RSP) {//需要监听直播视频流量消息
            requestVideoFlowRspFeature();
        }

        if ((mViewRequestFeatures & FEATURE_DOUBLE_CLICK_EXIT) == FEATURE_DOUBLE_CLICK_EXIT) {//需要监听直播视频流量消息
            mHasRegisterFeatures |= FEATURE_DOUBLE_CLICK_EXIT;
        }
    }


    private void onUnRegisterObserver() {
        mViewRequestFeatures = FEATURE_NO_FEATURES;
        mResolutionRetryFinished = true;
    }

    protected void requestResolutionFeature() {
        if ((mHasRegisterFeatures & FEATURE_VIDEO_RESOLUTION) == 0) {//代表还没有注册这个feature
            sInstance.mViewRequestFeatures |= FEATURE_VIDEO_RESOLUTION;
            mHasRegisterFeatures |= FEATURE_VIDEO_RESOLUTION;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                    .subscribeOn(Schedulers.io())
                    .filter(resolutionRsp -> sInstance != null && TextUtils.equals(sInstance.onResolveViewIdentify(), resolutionRsp.peer))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(videoResolutionRsp -> {
                        if ((sInstance.mViewRequestFeatures & FEATURE_VIDEO_RESOLUTION) == FEATURE_VIDEO_RESOLUTION) {
                            sInstance.mResolutionRetryFinished = true;
                            sInstance.onVideoResolution(videoResolutionRsp);
                        }
                    });
            sSubscriptions.add(subscribe);
        }
    }

    protected void requestLoginStateFeature() {
        if ((mHasRegisterFeatures & FEATURE_LOGIN_STATE) == 0) {//代表还没有注册这个feature
            sInstance.mViewRequestFeatures |= FEATURE_LOGIN_STATE;
            mHasRegisterFeatures |= FEATURE_LOGIN_STATE;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.LoginRsp.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(loginRsp -> {
                        if (sInstance != null && (sInstance.mViewRequestFeatures & FEATURE_LOGIN_STATE) == FEATURE_LOGIN_STATE) {
                            sInstance.onLoginStateChanged(loginRsp);
                        }
                    });
            sSubscriptions.add(subscribe);
        }
    }

    protected void requestVideoDisconnectFeature() {
        if ((mHasRegisterFeatures & FEATURE_VIDEO_DISCONNECT) == 0) {//代表还没有注册这个feature
            sInstance.mViewRequestFeatures |= FEATURE_VIDEO_DISCONNECT;
            mHasRegisterFeatures |= FEATURE_VIDEO_DISCONNECT;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                    .subscribeOn(Schedulers.io())
                    .filter(disconnectRsp -> sInstance != null && TextUtils.equals(sInstance.onResolveViewIdentify(), disconnectRsp.remote))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(disconnectRsp -> {
                        if (sInstance != null && (sInstance.mViewRequestFeatures & FEATURE_VIDEO_DISCONNECT) == FEATURE_VIDEO_DISCONNECT) {
                            sInstance.onVideoDisconnected(disconnectRsp);
                        }
                    });
            sSubscriptions.add(subscribe);
        }
    }

    protected void requestVideoFlowRspFeature() {
        if ((mHasRegisterFeatures & FEATURE_VIDEO_FLOW_RSP) == 0) {//代表还没有注册这个feature
            sInstance.mViewRequestFeatures |= FEATURE_VIDEO_FLOW_RSP;
            mHasRegisterFeatures |= FEATURE_VIDEO_FLOW_RSP;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(flowRsp -> {
                        if (sInstance != null && (sInstance.mViewRequestFeatures & FEATURE_VIDEO_FLOW_RSP) == FEATURE_VIDEO_FLOW_RSP) {
                            sInstance.onVideoFlowRsp(flowRsp);
                        }
                    });
            sSubscriptions.add(subscribe);
        }
    }

    public boolean hasReadyForExit() {
        if ((mHasRegisterFeatures & FEATURE_DOUBLE_CLICK_EXIT) == 0) {
            return true;
        }
        long clickedTime = SystemClock.uptimeMillis();
        long duration = clickedTime - mLastClickedTime;
        mLastClickedTime = clickedTime;
        return duration < 1500;
    }

    /**
     * 获取代表当前view的cid有些feature需要这个cid属性来进行过滤
     */
    protected String onResolveViewIdentify() {
        return "This Method Should Be Override If The View Should Use The Identify To Filter";
    }

    protected void onStartResolutionRetry() {
        if (!mResolutionRetryFinished) {
            HandlerThreadUtils.postDelay(() -> {
                if (mResolutionRetryFinished) return;
                try {
                    JfgCmdInsurance.getCmd().stopPlay(onResolveViewIdentify());
                    SystemClock.sleep(1000);
                    JfgCmdInsurance.getCmd().playVideo(onResolveViewIdentify());
                    onStartResolutionRetry();
                } catch (JfgException e) {
                    e.printStackTrace();
                }
            }, 15);
        }
    }

    @Override
    public void onViewAction(int action, Object extra) {
        //do nothing,but wrap it
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
        //do nothing
    }

    protected void post(Runnable action) {
        HandlerThreadUtils.post(action);
    }

    protected void postDelay(Runnable action, long delay) {
        HandlerThreadUtils.postDelay(action, delay);
    }

}
