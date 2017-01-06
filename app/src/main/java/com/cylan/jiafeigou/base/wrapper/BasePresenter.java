package com.cylan.jiafeigou.base.wrapper;

import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.utils.HandlerThreadUtils;

import java.util.ArrayList;
import java.util.Map;

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

    protected CompositeSubscription mSubscriptions;
    protected LongSparseArray<ResponseParser> mResponseParserMap = new LongSparseArray<>(32);

    protected V mView;
    protected ArrayList<Long> mRequestSeqs = new ArrayList<>(32);

    private static long mLastClickedTime;

    protected void unSubscribe(Subscription... subscriptions) {
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
        mView = (V) view;
    }

    @Override
    @CallSuper
    public void onStart() {
        onRegisterSubscription(mSubscriptions = new CompositeSubscription());
    }

    @CallSuper
    protected void onRegisterSubscription(CompositeSubscription subscriptions) {
        subscriptions.add(getDeviceSyncSub());
        subscriptions.add(getLoginStateSub());
        subscriptions.add(getQueryDataRspSub());
    }

    /**
     * 如果不需要在onStop中进行反注册,可以重写这个方法,然后在自定义的地方反注册
     */
    protected void onUnRegisterSubscription() {
        unSubscribe(mSubscriptions);
        mRequestSeqs.clear();//清空请求队列
    }

    @Override
    @CallSuper
    public void onStop() {
        onUnRegisterSubscription();
    }

    @Override
    public void onViewDetached() {
        mView = null;
    }

    protected void onLoginStateChanged(RxEvent.LoginRsp loginState) {
    }

    protected Subscription getLoginStateSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.LoginRsp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoginStateChanged, Throwable::printStackTrace);
    }


    protected Subscription getDeviceSyncSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JFGRobotSyncData.class)
                .subscribeOn(Schedulers.io())
                .filter(robotSyncData -> TextUtils.equals(onResolveViewIdentify(), robotSyncData.identity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    for (JFGDPMsg msg : response.dataList) {
                        ResponseParser parser = mResponseParserMap.get(msg.id);
                        if (parser != null) {
                            parser.onParseResponse(response.identity, msg);
                        }
                    }
                }, Throwable::printStackTrace);
    }

    private Subscription getQueryDataRspSub() {
        return RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .filter(response -> mRequestSeqs.remove(response.seq))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    for (Map.Entry<Integer, ArrayList<JFGDPMsg>> entry : response.map.entrySet()) {
                        ResponseParser parser = mResponseParserMap.get(entry.getKey());
                        if (parser != null) {
                            parser.onParseResponse(response.identity, (JFGDPMsg[]) entry.getValue().toArray());
                        }
                    }
                }, Throwable::printStackTrace);
    }


    public boolean hasReadyForExit() {
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

    protected void registerResponseParser(int msg, ResponseParser parser) {
        mResponseParserMap.put(msg, parser);
    }

    public interface ResponseParser {
        void onParseResponse(String identity, JFGDPMsg... value);
    }
}
