package com.cylan.jiafeigou.base.wrapper;

import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.utils.HandlerThreadUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BasePresenter<V extends JFGView> implements JFGPresenter {
    protected String TAG = getClass().getName();

    protected String mUUID;

    protected static JFGSourceManager sSourceManager;
    private static CompositeSubscription sSubscriptions;

    protected CompositeSubscription mSubscriptions;

    private static ArrayList<BasePresenter> sPresenters = new ArrayList<>(32);

    protected V mView;
    protected ArrayList<Long> mRequestSeqs = new ArrayList<>(32);
    protected boolean mHasResolution = false;
    protected boolean isActive;

    protected long mLastDeviceRtcpTime;//以后会移动到更具体的类里面去
    protected long mLastResolutionRetryTime;
    /**
     * 这里的feature注册采用延迟注册方式,即在view第一次需要的时候进行注册,
     * 注册是全局性的,一次注册所有的view共用,因此适合注册一些比较基础的feature
     */
    private long mViewRequestFeatures;//view需要的feature
    private long mViewHiddenFeature;//在view隐藏时仍然需要激活的feature
    private static long mHasRegisterFeatures;//当前已经注册的feature
    private static long mLastClickedTime;


    public static final long FEATURE_NO_FEATURES = 0;
    public static final long FEATURE_LOGIN_STATE = 0X1;
    public static final long FEATURE_VIDEO_RESOLUTION = 0X1 << 1;
    public static final long FEATURE_VIDEO_DISCONNECT = 0X1 << 2;
    public static final long FEATURE_VIDEO_FLOW_RSP = 0X1 << 3;
    public static final long FEATURE_DOUBLE_CLICK_EXIT = 0X1 << 4;
    public static final long FEATURE_DEVICE_BATTERY_STATE = 0X1 << 5;

    public static final long FEATURE_DEVICE_SYNC_DATA = 0X1 << 6;//基本feature
    public static final long FEATURE_DEVICE_GET_DATA_RSP = 0X1 << 7;//基本feature
    public static final long FEATURE_OBSERVER_WHEN_HIDDEN = 0X1 << 8;//在view不可见时仍然接收消息

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
                }, Throwable::printStackTrace);
    }


    protected static void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && !subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                }
            }
        }
    }

    @Override
    public void onViewAttached(JFGView view) {
        if (!sPresenters.contains(this)) {
            sPresenters.add(this);
        }
        mView = (V) view;
    }

    @Override
    @CallSuper
    public void onStart() {
        isActive = true;
        mViewRequestFeatures = onResolveViewFeatures();
        mViewHiddenFeature = onResolveViewHiddenFeatureMask();
        onRegisterObserver();
        onRegisterSubscription(mSubscriptions = new CompositeSubscription());
    }

    @CallSuper
    protected void onRegisterSubscription(CompositeSubscription subscriptions) {
        subscriptions.add(onStartTaskSchedule());
    }

    /**
     * 如果不需要在onStop中进行反注册,可以重写这个方法,然后在自定义的地方反注册
     */
    protected void onUnRegisterSubscription() {
        unSubscribe(mSubscriptions);
    }

    @Override
    @CallSuper
    public void onStop() {
        isActive = false;
        onUnRegisterObserver();
        onUnRegisterSubscription();
    }

    @Override
    public void onViewDetached() {
        if ((mViewRequestFeatures & FEATURE_OBSERVER_WHEN_HIDDEN) != FEATURE_OBSERVER_WHEN_HIDDEN) {
            sPresenters.remove(this);
        }
        mView = null;
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
    protected long onResolveViewFeatures() {
        return FEATURE_NO_FEATURES;
    }

    protected long onResolveViewHiddenFeatureMask() {
        return FEATURE_NO_FEATURES;
    }


    /**
     * 全局注册方式,推荐使用onRegisterSubscription的方式注册
     */
    @CallSuper
    @Deprecated
    protected void onRegisterObserver() {
        if (sSubscriptions == null) {
            sSubscriptions = new CompositeSubscription();
        }

        requestDeviceSyncObserverFeature();//监听设备同步消息,这个是基础feature,不需要注册
        requestGetDataRspObserverFeature();//监听请求数据响应的消息,这个是基础feature,不需要注册

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

        if ((mViewRequestFeatures & FEATURE_DEVICE_BATTERY_STATE) == FEATURE_DEVICE_BATTERY_STATE) {//需要监听设备电量变化
            mHasRegisterFeatures |= FEATURE_DEVICE_BATTERY_STATE;
        }
    }

    private void onUnRegisterObserver() {
        mViewRequestFeatures = FEATURE_NO_FEATURES;
        mHasResolution = true;
        mRequestSeqs.clear();
    }

    private static boolean accept(String identify) {
        for (BasePresenter presenter : sPresenters) {
            if (TextUtils.equals(presenter.onResolveViewIdentify(), identify)) {
                return true;
            }
        }
        return false;
    }

    protected void requestResolutionFeature() {
        if ((mHasRegisterFeatures & FEATURE_VIDEO_RESOLUTION) == 0) {//代表还没有注册这个feature
            mHasRegisterFeatures |= FEATURE_VIDEO_RESOLUTION;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(videoResolutionRsp -> {
                        for (BasePresenter presenter : sPresenters) {
                            if ((presenter.mViewRequestFeatures & FEATURE_VIDEO_RESOLUTION) == FEATURE_VIDEO_RESOLUTION) {
                                if ((presenter.mViewHiddenFeature & FEATURE_VIDEO_RESOLUTION) == FEATURE_VIDEO_RESOLUTION || presenter.isActive) {
                                    if (TextUtils.equals(presenter.onResolveViewIdentify(), videoResolutionRsp.peer)) {
                                        presenter.mHasResolution = true;
                                        presenter.onVideoResolution(videoResolutionRsp);
                                    }
                                }
                            }
                        }
                    }, Throwable::printStackTrace);
            sSubscriptions.add(subscribe);
        }
    }

    protected void requestLoginStateFeature() {
        if ((mHasRegisterFeatures & FEATURE_LOGIN_STATE) == 0) {//代表还没有注册这个feature
            mHasRegisterFeatures |= FEATURE_LOGIN_STATE;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.LoginRsp.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(loginRsp -> {
                        for (BasePresenter presenter : sPresenters) {
                            if ((presenter.mViewRequestFeatures & FEATURE_LOGIN_STATE) == FEATURE_LOGIN_STATE) {
                                if ((presenter.mViewHiddenFeature & FEATURE_LOGIN_STATE) == FEATURE_LOGIN_STATE || presenter.isActive) {
                                    presenter.onLoginStateChanged(loginRsp);
                                }
                            }
                        }
                    }, Throwable::printStackTrace);
            sSubscriptions.add(subscribe);
        }
    }

    protected void requestVideoDisconnectFeature() {
        if ((mHasRegisterFeatures & FEATURE_VIDEO_DISCONNECT) == 0) {//代表还没有注册这个feature
            mHasRegisterFeatures |= FEATURE_VIDEO_DISCONNECT;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(disconnectRsp -> {
                        for (BasePresenter presenter : sPresenters) {
                            if ((presenter.mViewRequestFeatures & FEATURE_VIDEO_DISCONNECT) == FEATURE_VIDEO_DISCONNECT) {
                                if ((presenter.mViewHiddenFeature & FEATURE_VIDEO_DISCONNECT) == FEATURE_VIDEO_DISCONNECT || presenter.isActive) {
                                    if (TextUtils.equals(presenter.onResolveViewIdentify(), disconnectRsp.remote)) {
                                        presenter.onVideoDisconnected(disconnectRsp);
                                    }
                                }
                            }
                        }
                    }, Throwable::printStackTrace);
            sSubscriptions.add(subscribe);
        }
    }

    protected void requestVideoFlowRspFeature() {
        if ((mHasRegisterFeatures & FEATURE_VIDEO_FLOW_RSP) == 0) {//代表还没有注册这个feature
            mHasRegisterFeatures |= FEATURE_VIDEO_FLOW_RSP;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(flowRsp -> {
                        for (BasePresenter presenter : sPresenters) {
                            if ((presenter.mViewRequestFeatures & FEATURE_VIDEO_FLOW_RSP) == FEATURE_VIDEO_FLOW_RSP) {
                                if ((presenter.mViewHiddenFeature & FEATURE_VIDEO_FLOW_RSP) == FEATURE_VIDEO_FLOW_RSP || presenter.isActive) {
                                    presenter.mLastDeviceRtcpTime = SystemClock.uptimeMillis();
                                    presenter.onVideoFlowRsp(flowRsp);
                                }
                            }
                        }
                    }, Throwable::printStackTrace);
            sSubscriptions.add(subscribe);
        }
    }

    private void requestDeviceSyncObserverFeature() {
        mViewRequestFeatures |= FEATURE_DEVICE_SYNC_DATA;
        if ((mHasRegisterFeatures & FEATURE_DEVICE_SYNC_DATA) == 0) {//代表还没有注册这个feature
            mHasRegisterFeatures |= FEATURE_DEVICE_SYNC_DATA;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.JFGRobotSyncData.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(rsp -> {
                        for (BasePresenter presenter : sPresenters) {
                            if ((presenter.mViewHiddenFeature & FEATURE_DEVICE_SYNC_DATA) == FEATURE_DEVICE_SYNC_DATA || presenter.isActive) {
                                presenter.onResolveDeviceSyncData(rsp);
                            }
                        }
                    }, Throwable::printStackTrace);
            sSubscriptions.add(subscribe);
        }
    }

    protected void onResolveDeviceSyncData(RxEvent.JFGRobotSyncData robotSyncData) {
        if (TextUtils.equals(onResolveViewIdentify(), robotSyncData.identity)) {
            try {
                for (JFGDPMsg msg : robotSyncData.dataList) {
                    if (msg.id == DpMsgMap.ID_206_BATTERY &&
                            (mViewRequestFeatures & FEATURE_DEVICE_BATTERY_STATE) == FEATURE_DEVICE_BATTERY_STATE) {//解析设备电量变化
                        DpMsgDefine.MsgBattery battery = DpUtils.unpackData(msg.packValue, DpMsgDefine.MsgBattery.class);
                        onDeviceBatteryStateChanged(battery);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestGetDataRspObserverFeature() {
        mViewRequestFeatures |= FEATURE_DEVICE_GET_DATA_RSP;
        if ((mHasRegisterFeatures & FEATURE_DEVICE_GET_DATA_RSP) == 0) {
            mHasRegisterFeatures |= FEATURE_DEVICE_GET_DATA_RSP;
            Subscription subscribe = RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(response -> {
                        for (BasePresenter presenter : sPresenters) {
                            if ((presenter.mViewHiddenFeature & FEATURE_DEVICE_GET_DATA_RSP) == FEATURE_DEVICE_GET_DATA_RSP || presenter.isActive) {
                                if (presenter.mRequestSeqs.remove(response.seq)) {
                                    for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : response.map.entrySet()) {
                                        presenter.onResolveGetDataResponse(response.identity, entry.getKey(), entry.getValue());
                                    }
                                    presenter.onResolveGetDataResponseCompleted();
                                }
                            }
                        }
                    }, Throwable::printStackTrace);
            sSubscriptions.add(subscribe);
        }
    }

    protected void onResolveGetDataResponseCompleted() {
    }

    /**
     * 在UI线程调用
     */
    protected void onResolveGetDataResponse(String identity, Integer key, ArrayList<JFGDPMsg> value) {
    }

    protected void onDeviceBatteryStateChanged(DpMsgDefine.MsgBattery battery) {
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

    @Override
    public void onSetViewUUID(String uuid) {
        mUUID = uuid;
    }

    protected Subscription onStartTaskSchedule() {//定时调用的timer
        return Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(count -> {
//                    for (BasePresenter presenter : sPresenters) {
//                        if ((presenter.mViewRequestFeatures & FEATURE_VIDEO_RESOLUTION) == FEATURE_VIDEO_RESOLUTION && !presenter.mHasResolution) {
//                            presenter.onResolutionTask();
//                        }
//                        if ((presenter.mViewRequestFeatures & FEATURE_VIDEO_FLOW_RSP) == FEATURE_VIDEO_FLOW_RSP && presenter.mHasResolution) {
//                            presenter.onFlowRtcpTask();
//                        }
//                    }

                }, Throwable::printStackTrace);
    }

    protected void listenResolution() {
        mLastResolutionRetryTime = SystemClock.uptimeMillis();
    }

    protected void onResolutionTask() {
        if (SystemClock.uptimeMillis() - mLastResolutionRetryTime < 15000) {
            //每15秒检查一下resolution
            return;
        }
        mLastResolutionRetryTime = SystemClock.uptimeMillis();
        try {
            JfgCmdInsurance.getCmd().stopPlay(onResolveViewIdentify());
            SystemClock.sleep(1000);
            JfgCmdInsurance.getCmd().playVideo(onResolveViewIdentify());
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    protected void onFlowRtcpTask() {
        long interval = SystemClock.uptimeMillis() - mLastDeviceRtcpTime;
        if (interval > 5000) {
            onVideoDisconnected(null);
        }
    }


    @Override
    public void onViewAction(int action, String handle, Object extra) {
    }

    @Override
    public void onScreenRotationChanged(boolean land) {
    }

    @Override
    public void onSetContentView() {
    }

    /**
     * 工作在非UI线程,可以简化rxjava的使用
     */
    protected void post(Runnable action) {
        HandlerThreadUtils.post(action);
    }

    protected void postDelay(Runnable action, long delay) {
        HandlerThreadUtils.postDelay(action, delay);
    }
}
