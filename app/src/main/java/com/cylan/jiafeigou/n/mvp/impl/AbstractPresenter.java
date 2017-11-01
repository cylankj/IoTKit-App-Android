package com.cylan.jiafeigou.n.mvp.impl;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.support.headset.HeadsetObserver;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.NetMonitor;
import com.cylan.jiafeigou.support.network.NetworkCallback;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.view.SubscriptionAdapter;
import com.trello.rxlifecycle.LifecycleProvider;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 一个基本模型的Presenter
 * Created by cylan-hunt on 16-6-30.
 */
public abstract class AbstractPresenter<T extends JFGView> extends BasePresenter<T> implements SubscriptionAdapter,
        NetworkCallback, HeadsetObserver.HeadsetListener {
    protected CompositeSubscription compositeSubscription;
    protected MapSubscription refCacheMap = new MapSubscription();
    protected HeadsetObserver headsetObserver;
    protected AudioManager audioManager;

    public AbstractPresenter(T view) {
        super(view);
        mView = view;
        this.uuid = mView.uuid();
        setSubscriptionManager(BaseApplication.getAppComponent().getSubscriptionManager());
        if (mView instanceof LifecycleProvider) {
            attachToLifecycle((LifecycleProvider) mView);
        }
    }

    @Override
    public void addSubscription(String tag, Subscription s) {
        addSubscription(s, tag);
    }

//    public AbstractPresenter(T view, String uuid) {
//        super(view);
//        mView = view;
//        this.uuid = mView.uuid();
//        setSubscriptionManager(BaseApplication.getAppComponent().getSubscriptionManager());
//    }


    @Override
    public void pause() {
        super.pause();
    }

    public T getView() {
        return mView;
    }

    protected void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && !subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                }
            }
        }
    }

    public AudioManager getAudioManager() {
        if (audioManager == null) {
            audioManager = (AudioManager) ContextUtils.getContext().getSystemService(Context.AUDIO_SERVICE);
        }
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
        super.start();
        if (compositeSubscription != null) {
            compositeSubscription.unsubscribe();
        }
        compositeSubscription = new CompositeSubscription();
        if (refCacheMap != null) {
            refCacheMap.unsubscribe();
        }
        refCacheMap = new MapSubscription();
        Subscription[] subs = register();
        if (subs != null) {
            for (Subscription s : subs) {
                if (s != null) {
                    compositeSubscription.add(s);
                }
            }
        }
        AppLogger.w(TAG + ": register: " + compositeSubscription.isUnsubscribed() + ",:" + refCacheMap.isUnsubscribed());
        String[] action = registerNetworkAction();
        if (action != null && action.length > 0) {
            NetMonitor.getNetMonitor().registerNet(this, action);
            AppLogger.w("register network true");
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
        if (compositeSubscription == null) {
            compositeSubscription = new CompositeSubscription();
        }
        if (refCacheMap == null) {
            refCacheMap = new MapSubscription();
        }
        if (subscription != null) {
            compositeSubscription.add(subscription);
        }
    }

    protected void addSubscription(Subscription subscription, String tag) {
        if (subscription != null) {
            refCacheMap.add(subscription, tag);
        }
    }

    @Override
    public boolean unSubscribe(String tag) {
        refCacheMap.remove(tag);
        return true;
    }

    public boolean containsSubscription(final String tag) {
        return refCacheMap.hasSubscription(tag);
    }

    public void unSubscribeAllTag() {
        refCacheMap.clear();
    }

    @CallSuper
    @Override
    public void stop() {
        super.stop();
        Log.d("stop", "stop: " + this.getClass().getSimpleName());
        unSubscribe(refCacheMap);
        unSubscribe(compositeSubscription);
        if (compositeSubscription != null) {
            compositeSubscription.clear();
        }
        if (refCacheMap != null) {
            refCacheMap.clear();
        }
        if (mSubscriptionManager != null) {
            mSubscriptionManager.unbind(this);
        }
        if (subscriptions != null) {
            subscriptions.clear();
        }
        NetMonitor.getNetMonitor().unregister(this);
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
        if (headsetObserver == null) {
            headsetObserver = HeadsetObserver.getHeadsetObserver();
        }
        headsetObserver.addObserver(this);
        AppLogger.d("wetRtcJava层干扰了耳机的设置 注册监听耳机:" + TAG);
        AppLogger.d("wetRtcJava层干扰了耳机的设置 需要在打开speaker后,延时重新设置:" + TAG);
    }

    public boolean isEarpiecePlug() {
        if (headsetObserver == null) {
            headsetObserver = HeadsetObserver.getHeadsetObserver();
        }
        return headsetObserver.isHeadsetOn();
    }

    public void switchEarpiece(boolean enable) {
        getAudioManager().setMode(enable ? AudioManager.MODE_CURRENT : AudioManager.MODE_IN_CALL);
        getAudioManager().setSpeakerphoneOn(!enable);
    }

    protected void unRegisterHeadSetObservable() {
        if (headsetObserver == null) {
            return;
        }
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
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                audioManager.abandonAudioFocus(afChangeListener);
            }
        }
    };

    /**
     * a static task
     */
    protected static <T, R> void enqueueTask(T source, Func1<T, R> func1) {
        Observable.just(source)
                .map(func1)
                .compose(applySchedulers())
                .subscribe(ret -> {
                }, AppLogger::e);
    }

    final static Observable.Transformer<Object, Object> schedulersTransformer =
            observable -> observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

    @SuppressWarnings("unchecked")
    static <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) schedulersTransformer;
    }

}
