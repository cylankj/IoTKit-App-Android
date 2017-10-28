package com.cylan.jiafeigou.base.wrapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.CallSuper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.dagger.annotation.ContextLife;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.ILoadingManager;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;
import com.cylan.jiafeigou.view.LifecycleAdapter;
import com.cylan.jiafeigou.view.PresenterAdapter;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.android.FragmentEvent;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Created by yzd on 16-12-28.
 */

public abstract class BasePresenter<View extends JFGView> implements JFGPresenter, LifecycleProvider<FragmentEvent>, LifecycleAdapter {
    protected String TAG = getClass().getName();
    @Inject
    @ContextLife
    protected Context mContext;//在不支持 inject 的时候为空,小心使用
    @Inject
    protected IDPTaskDispatcher taskDispatcher;//在不支持 inject 的时候为空,小心使用
    @Inject
    protected ILoadingManager mLoadingManager;//在不支持 inject 的时候为空,小心使用
    protected Map<LIFE_CYCLE, MapSubscription> lifeCycleCompositeSubscriptionMap;
    protected String uuid;
    protected View mView;
    protected volatile boolean subscribed = false;

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
        addSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "BasePresenter#getLoginStateSub", getLoginStateSub());
    }

    @Override
    @CallSuper
    public void unsubscribe() {
        subscribed = false;
        for (LIFE_CYCLE cycle : LIFE_CYCLE.values()) {
            unsubscribe(cycle);
        }
        mView = null;
    }

    @Override
    public boolean isUnsubscribed() {
        return subscribed;
    }

    @Nonnull
    @Override
    public Observable<FragmentEvent> lifecycle() {
        return null;
    }

    @Nonnull
    @Override
    public <T> LifecycleTransformer<T> bindToLifecycle() {
        return null;
    }

    @Nonnull
    @Override
    public <T> LifecycleTransformer<T> bindUntilEvent(@Nonnull FragmentEvent event) {
        return null;
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

    protected void unsubscribe(LIFE_CYCLE lifeCycle) {
        if (lifeCycleCompositeSubscriptionMap != null) {
            MapSubscription compositeSubscription = lifeCycleCompositeSubscriptionMap.remove(lifeCycle);
            unsubscribe(compositeSubscription);
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
        unsubscribe(LIFE_CYCLE.LIFE_CYCLE_STOP);
        if (registerTimeTick()) {
            if (timeTick != null) {
                LocalBroadcastManager.getInstance(ContextUtils.getContext()).unregisterReceiver(timeTick);
            }
        }
    }

    protected void onLoginStateChanged(RxEvent.OnlineStatusRsp loginState) {
        mView.onLoginStateChanged(loginState.state);
    }


    /**
     * 监听登录状态的变化时基本的功能,所以提取到基类中
     */
    private Subscription getLoginStateSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.OnlineStatusRsp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoginStateChanged, e -> {
                    e.printStackTrace();
                    addSubscription(LIFE_CYCLE.LIFE_CYCLE_STOP, "BasePresenter#getLoginStateSub", getLoginStateSub());//出现异常了要重现注册
                });
    }

    public Observable<IDPTaskResult> perform(IDPEntity entity) {
        return taskDispatcher.perform(entity);
    }

    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entity) {
        return taskDispatcher.perform(entity);
    }

    /**
     * 工作在非UI线程,可以简化rxjava的使用
     */
    protected void post(Runnable action) {
        HandlerThreadUtils.post(action);
    }


    public enum LIFE_CYCLE {
        LIFE_CYCLE_STOP,
        LIFE_CYCLE_DESTROY,
    }

    protected void addSubscription(LIFE_CYCLE lifeCycle, String tag, Subscription subscription) {
        if (lifeCycleCompositeSubscriptionMap == null) {
            synchronized (this) {
                if (lifeCycleCompositeSubscriptionMap == null) {
                    lifeCycleCompositeSubscriptionMap = new HashMap<>();
                }
            }
        }
        if (!TextUtils.isEmpty(tag)) {
            MapSubscription compositeSubscription = lifeCycleCompositeSubscriptionMap.get(lifeCycle);

            if (compositeSubscription == null) {
                compositeSubscription = new MapSubscription();
                lifeCycleCompositeSubscriptionMap.put(lifeCycle, compositeSubscription);
            }

            if (subscription != null) {
                compositeSubscription.add(subscription, tag);
            }
        }
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
