package com.cylan.jiafeigou.base.wrapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.support.annotation.CallSuper;
import android.support.v4.content.LocalBroadcastManager;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.dagger.annotation.ContextLife;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.view.LifecycleAdapter;
import com.cylan.jiafeigou.view.PresenterAdapter;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by yzd on 16-12-28.
 */

public abstract class BasePresenter<View extends JFGView> implements JFGPresenter, LifecycleAdapter {
    protected String TAG = getClass().getName();
    @Inject
    @ContextLife
    protected Context mContext;//在不支持 inject 的时候为空,小心使用
    @Inject
    protected IDPTaskDispatcher mTaskDispatcher;//在不支持 inject 的时候为空,小心使用
    protected String uuid;
    protected View mView;
    protected MapSubscription stopSubscriptions = new MapSubscription();
    protected MapSubscription destroySubscriptions = new MapSubscription();

    protected volatile boolean subscribed = false;
    protected CompositeSubscription compositeSubscription;
    protected MapSubscription refCacheMap = new MapSubscription();

    public BasePresenter(View view) {
        this.mView = view;
        setPresenter();
    }

    @Deprecated
    protected final void setPresenter() {
        if (mView instanceof PresenterAdapter) {
            ((PresenterAdapter) mView).setPresenter(this);
        }
    }

    @Override
    public final void uuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    @CallSuper
    public void subscribe() {
        subscribed = true;
        subscribeLoginState();
    }

    @Override
    @CallSuper
    public void unsubscribe() {
        subscribed = false;
//        mContext = null;
//        mTaskDispatcher = null;
//        mLoadingManager = null;
//        mView = null;
    }

    @Override
    @CallSuper
    public void destroy() {
        if (destroySubscriptions != null) {
            destroySubscriptions.unsubscribe();
            destroySubscriptions = new MapSubscription();
        }
    }

    @Override
    public boolean isSubscribed() {
        return subscribed;
    }

    protected void unsubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && !subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                }
            }
        }
    }

    @CallSuper
    public void start() {
        if (registerTimeTick()) {
            if (timeTick == null) {
                timeTick = new TimeTick(this);
            }
            LocalBroadcastManager.getInstance(ContextUtils.getContext())
                    .registerReceiver(timeTick, new IntentFilter(JConstant.KEY_TIME_TICK_));
        }
    }

    @Override
    @CallSuper
    public void pause() {

    }

    @CallSuper
    public void stop() {
        if (registerTimeTick()) {
            if (timeTick != null) {
                LocalBroadcastManager.getInstance(ContextUtils.getContext()).unregisterReceiver(timeTick);
            }
        }
        if (stopSubscriptions != null) {
            stopSubscriptions.unsubscribe();
            stopSubscriptions = new MapSubscription();
        }
    }

    protected void onLoginStateChanged(RxEvent.OnlineStatusRsp loginState) {
        mView.onLoginStateChanged(loginState.state);
    }
    protected String addStopSubscription(Subscription subscription) {
        StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
        String method = getClass().getName() + "(L:" + traceElement.getLineNumber() + "):stop:" + traceElement.getMethodName();
        AppLogger.w("addSubscription" + method);
        stopSubscriptions.add(subscription, method);
        return method;
    }

    protected <T> Observable.Transformer<T, T> applyLoading(int resId, Object... args) {
        return tObservable -> (Observable<T>) tObservable.doOnSubscribe(() -> LoadingDialog.showLoading(mView.activity(),
                mContext.getString(resId, args), false))
                .doOnTerminate(LoadingDialog::dismissLoading);
    }

    protected <T> Observable.Transformer<T, T> applyLoading(int resId, String subscription, Object... args) {
        return tObservable -> (Observable<T>) tObservable.doOnSubscribe(() -> LoadingDialog.showLoading(mView.activity(),
                mContext.getString(resId, args), true,
                dialog -> unsubscribe(subscription)))
                .doOnTerminate(LoadingDialog::dismissLoading);
    }

    protected <T> Observable.Transformer<T, T> applyLoading(String message, String subscription) {
        return tObservable -> (Observable<T>) tObservable.doOnSubscribe(() -> LoadingDialog.showLoading(mView.activity(),
                message, true,
                dialog -> unsubscribe(subscription)))
                .doOnTerminate(LoadingDialog::dismissLoading);
    }

    protected <T> Observable.Transformer<T, T> applyLoadingFocus(int resId, String subscription, Object... args) {
        return tObservable -> (Observable<T>) tObservable.doOnSubscribe(() -> LoadingDialog.showLoading(mView.activity(),
                mContext.getString(resId, args), true,
                dialog -> unsubscribe(subscription)))
                .doOnTerminate(LoadingDialog::dismissLoading);
    }


    protected void unsubscribe(String subscription) {
        destroySubscriptions.remove(subscription);
        stopSubscriptions.remove(subscription);
    }

    protected String addDestroySubscription(Subscription subscription) {
        StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
        String method = getClass().getName() + "(L:" + traceElement.getLineNumber() + "):destroy:" + traceElement.getMethodName();
        AppLogger.w("addSubscription" + method);
        destroySubscriptions.add(subscription, method);
        return method;
    }

    protected String getMethodName() {
        StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
        String method = getClass().getName() + "(L:" + traceElement.getLineNumber() + "):" + traceElement.getMethodName();
        AppLogger.w("getMethodName:" + method);
        return method;
    }

    protected String method() {
        StackTraceElement traceElement = Thread.currentThread().getStackTrace()[3];
        String method = getClass().getName() + "(L:" + traceElement.getLineNumber() + "):" + traceElement.getMethodName();
        AppLogger.w("getMethodName:" + method);
        return method;
    }

    /**
     * 监听登录状态的变化时基本的功能,所以提取到基类中
     */
    private void subscribeLoginState() {
        Subscription subscribe = RxBus.getCacheInstance().toObservableSticky(RxEvent.OnlineStatusRsp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoginStateChanged, e -> {
                    AppLogger.e(MiscUtils.getErr(e));
                    subscribeLoginState();
                });
        addDestroySubscription(subscribe);
    }

    public Observable<IDPTaskResult> perform(IDPEntity entity) {
        return mTaskDispatcher.perform(entity);
    }

    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entity) {
        return mTaskDispatcher.perform(entity);
    }

    /**
     * 工作在非UI线程,可以简化rxjava的使用
     */
    protected void post(Runnable action) {
        HandlerThreadUtils.post(action);
    }

    protected TimeTick timeTick;

    private static class TimeTick extends BroadcastReceiver {
        private WeakReference<BasePresenter> abstractPresenter;

        public TimeTick(BasePresenter abstractPresenter) {
            this.abstractPresenter = new WeakReference<>(abstractPresenter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (abstractPresenter != null && abstractPresenter.get() != null) {
                abstractPresenter.get().onTimeTick();
            }
        }
    }

    protected boolean registerTimeTick() {
        return false;
    }

    protected void onTimeTick() {
    }
}
