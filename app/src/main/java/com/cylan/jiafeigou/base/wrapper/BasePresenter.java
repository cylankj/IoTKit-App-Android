package com.cylan.jiafeigou.base.wrapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.CallSuper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.HandlerThreadUtils;

import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;


/**
 * Created by yzd on 16-12-28.
 */

public abstract class BasePresenter<V extends JFGView> implements JFGPresenter<V> {
    protected String TAG = getClass().getName();
    protected String mUUID;
    protected JFGSourceManager sourceManager;
    protected IDPTaskDispatcher taskDispatcher;
    protected AppCmd appCmd;
    private CompositeSubscription compositeSubscription;

    protected V mView;

    protected void unSubscribe(Subscription... subscriptions) {
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                if (subscription != null && !subscription.isUnsubscribed()) {
                    subscription.unsubscribe();
                }
            }
        }
    }

    public void setSourceManager(JFGSourceManager manager) {
        this.sourceManager = manager;
    }

    public void setTaskDispatcher(IDPTaskDispatcher taskDispatcher) {
        this.taskDispatcher = taskDispatcher;
    }

    public void setAppCmd(AppCmd appCmd) {
        this.appCmd = appCmd;
    }

    @Override
    public void onViewAttached(V view) {
        mView = view;
        onRegisterResponseParser();
    }

    @Override
    @CallSuper
    public void onStart() {
        onRegisterSubscription();
        if (registerTimeTick()) {
            if (timeTick == null) timeTick = new TimeTick(this);
            LocalBroadcastManager.getInstance(ContextUtils.getContext())
                    .registerReceiver(timeTick, new IntentFilter(JConstant.KEY_TIME_TICK_));
        }
    }

    @CallSuper
    protected void onRegisterSubscription() {
        registerSubscription(
                getDeviceSyncSub(),
                getLoginStateSub(),
                getDeleteDataRspSub()
        );
    }

    @CallSuper
    protected void onRegisterResponseParser() {
    }

    /**
     * 如果不需要在onStop中进行反注册,可以重写这个方法,然后在自定义的地方反注册
     */
    protected void onUnRegisterSubscription() {
        unSubscribe(compositeSubscription);
        compositeSubscription = null;
    }

    @Override
    @CallSuper
    public void onStop() {
        onUnRegisterSubscription();
        if (registerTimeTick()) {
            if (timeTick != null)
                LocalBroadcastManager.getInstance(ContextUtils.getContext()).unregisterReceiver(timeTick);
        }
    }

    @Override
    public void onViewDetached() {
        mView = null;
        sourceManager = null;
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
                    registerSubscription(getLoginStateSub());//出现异常了要重现注册
                });
    }

    /**
     * 监听设备同步消息是基本功能,所以提取到基类中
     */
    private Subscription getDeviceSyncSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .subscribeOn(Schedulers.io())
                .filter(rsp -> accept(rsp.uuid))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    onDeviceSync();
                }, e -> {
                    e.printStackTrace();//打印错误日志以便排错
                    registerSubscription(getDeviceSyncSub());//基类不能崩,否则一些功能异常
                });
    }

    protected void onDeviceSync() {
    }

    private Subscription getDeleteDataRspSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteDataRsp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteDataRsp -> {

                }, e -> {
                    e.printStackTrace();
                    registerSubscription(getDeleteDataRspSub());
                });
    }

    public boolean hasReadyForExit() {
        return true;
    }

    public Observable<IDPTaskResult> perform(IDPEntity entity) {
        return taskDispatcher.perform(entity);
    }

    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entity) {
        return taskDispatcher.perform(entity);
    }

    /**
     * 获取代表当前view的cid有些feature需要这个cid属性来进行过滤
     *
     * @deprecated 现在view会自动设置uuid到presenter中, 因此这个方法也就意义不大了
     */
    @Deprecated
    protected String onResolveViewIdentify() {
        return "This Method Should Be Override If The View Should Use The Identify To Filter";
    }

    @Override
    public void onSetViewUUID(String uuid) {
        mUUID = uuid;
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
     * 用户判断当前uuid是否是自己需要的
     */
    protected boolean accept(String uuid) {
        return TextUtils.equals(mUUID, uuid);
    }

    /**
     * 工作在非UI线程,可以简化rxjava的使用
     */
    protected void post(Runnable action) {
        HandlerThreadUtils.post(action);
    }


    protected void registerSubscription(Subscription... subscriptions) {
        if (compositeSubscription == null) {
            synchronized (this) {
                if (compositeSubscription == null) {
                    compositeSubscription = new CompositeSubscription();
                }
            }
        }
        if (subscriptions != null) {
            for (Subscription subscription : subscriptions) {
                compositeSubscription.add(subscription);
            }
        }
    }

    private TimeTick timeTick;

    private static class TimeTick extends BroadcastReceiver {
        private WeakReference<BasePresenter> abstractPresenter;

        public TimeTick(BasePresenter abstractPresenter) {
            this.abstractPresenter = new WeakReference<>(abstractPresenter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (abstractPresenter != null && abstractPresenter.get() != null)
                abstractPresenter.get().onTimeTick();
        }
    }

    protected boolean registerTimeTick() {
        return false;
    }

    protected void onTimeTick() {
    }
}
