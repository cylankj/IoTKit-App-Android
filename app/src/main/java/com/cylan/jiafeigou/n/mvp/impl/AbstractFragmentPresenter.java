package com.cylan.jiafeigou.n.mvp.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.BaseFragmentView;
import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.support.headset.HeadsetObserver;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.NetMonitor;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.lang.ref.WeakReference;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * 一个基本模型的Presenter
 * Created by cylan-hunt on 16-6-30.
 */
public abstract class AbstractFragmentPresenter<T extends BaseFragmentView> implements BasePresenter,
        NetMonitor.NetworkCallback, HeadsetObserver.HeadsetListener {

    protected final String TAG = this.getClass().getSimpleName();
    protected T mView;//弱引用会被强制释放,我们的view需要我们手动释放,不适合弱引用
    protected String uuid;
    private CompositeSubscription compositeSubscription;
    private MapSubscription refCacheMap = new MapSubscription();
    private TimeTick timeTick;
    private HeadsetObserver headsetObserver;
    private AudioManager audioManager;

    public AbstractFragmentPresenter(T view) {
        mView = view;
        this.uuid = mView.getUuid();
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    protected boolean check() {
        return mView != null && mView.isAdded();
    }

    public T getView() {
        return mView;
    }

    protected void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && !subscription.isUnsubscribed())
                    subscription.unsubscribe();
            }
        }
    }

    public AudioManager getAudioManager() {
        if (audioManager == null)
            audioManager = (AudioManager) ContextUtils.getContext().getSystemService(Context.AUDIO_SERVICE);
        return audioManager;
    }

    /**
     * 注册监听,系统时间广播
     *
     * @return
     */
    protected boolean registerTimeTick() {
        return false;
    }

    @CallSuper
    @Override
    public void start() {
        if (compositeSubscription != null) compositeSubscription.unsubscribe();
        compositeSubscription = new CompositeSubscription();
        if (refCacheMap != null) refCacheMap.unsubscribe();
        refCacheMap = new MapSubscription();
        Subscription[] subs = register();
        if (subs != null) {
            for (Subscription s : subs)
                if (s != null)
                    compositeSubscription.add(s);
        }
        AppLogger.d(TAG + ": register: " + compositeSubscription.isUnsubscribed() + ",:" + refCacheMap.isUnsubscribed());
        String[] action = registerNetworkAction();
        if (action != null && action.length > 0) {
            NetMonitor.getNetMonitor().registerNet(this, action);
            AppLogger.d("register network true");
        }
        if (registerTimeTick()) {
            if (timeTick == null) timeTick = new TimeTick(this);
            LocalBroadcastManager.getInstance(ContextUtils.getContext())
                    .registerReceiver(timeTick, new IntentFilter(JConstant.KEY_TIME_TICK_));
        }
    }

    /**
     * 注册网络广播
     *
     * @return
     */
    protected String[] registerNetworkAction() {
        return null;
    }

    protected void addSubscription(Subscription subscription) {
        if (subscription != null)
            compositeSubscription.add(subscription);
    }

    protected void addSubscription(Subscription subscription, String tag) {
        if (subscription != null)
            refCacheMap.add(subscription, tag);
    }

    protected boolean unSubscribe(String tag) {
        refCacheMap.remove(tag);
        return true;
    }

    @CallSuper
    @Override
    public void stop() {
        Log.d("stop", "stop: " + this.getClass().getSimpleName());
        unSubscribe(refCacheMap);
        unSubscribe(compositeSubscription);
        if (compositeSubscription != null) compositeSubscription.clear();
        if (refCacheMap != null) refCacheMap.clear();
        NetMonitor.getNetMonitor().unregister(this);
        if (registerTimeTick()) {
            if (timeTick != null)
                LocalBroadcastManager.getInstance(ContextUtils.getContext()).unregisterReceiver(timeTick);
        }
        unRegisterHeadSetObservable();
        abandonAudioFocus();
    }

    protected Subscription[] register() {
        return null;
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {

    }

    protected void onTimeTick() {
    }

    private static class TimeTick extends BroadcastReceiver {
        private WeakReference<AbstractFragmentPresenter> abstractPresenter;

        public TimeTick(AbstractFragmentPresenter abstractPresenter) {
            this.abstractPresenter = new WeakReference<>(abstractPresenter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (abstractPresenter != null && abstractPresenter.get() != null)
                abstractPresenter.get().onTimeTick();
        }
    }

    @NonNull
    @Override
    public Device getDevice() {
        return BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
    }

    @Override
    public void onHeadSetPlugIn(boolean plugIn) {
        AppLogger.d("耳机接入?:" + plugIn);
        switchEarpiece(plugIn);
    }

    protected void registerHeadSetObservable() {
        if (headsetObserver == null) headsetObserver = HeadsetObserver.getHeadsetObserver();
        headsetObserver.addObserver(this);
        AppLogger.d("wetRtcJava层干扰了耳机的设置 注册监听耳机:" + TAG);
        AppLogger.d("wetRtcJava层干扰了耳机的设置 需要在打开speaker后,延时重新设置:" + TAG);
    }

    protected boolean isEarpiecePlug() {
        if (headsetObserver == null) headsetObserver = HeadsetObserver.getHeadsetObserver();
        return headsetObserver.isHeadsetOn();
    }

    protected void switchEarpiece(boolean enable) {
        getAudioManager().setMode(enable ? AudioManager.MODE_CURRENT : AudioManager.MODE_IN_CALL);
        getAudioManager().setSpeakerphoneOn(!enable);
    }

    protected void unRegisterHeadSetObservable() {
        if (headsetObserver == null) return;
        headsetObserver.removeObserver(this);
        AppLogger.d("反注册注册监听耳机:" + TAG);
    }

    protected void gainAudioFocus() {
        getAudioManager().requestAudioFocus(afChangeListener,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN);
    }

    /**
     * 反注册
     */
    protected void abandonAudioFocus() {
        getAudioManager().abandonAudioFocus(afChangeListener);
    }

    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                audioManager.abandonAudioFocus(afChangeListener);
            }
        }
    };
}
