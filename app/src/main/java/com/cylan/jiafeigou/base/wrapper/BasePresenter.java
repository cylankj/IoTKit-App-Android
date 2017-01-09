package com.cylan.jiafeigou.base.wrapper;

import android.support.annotation.CallSuper;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.module.JFGDevice;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.base.view.PropertyView;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.provider.DataSourceManager;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.utils.HandlerThreadUtils;

import java.util.ArrayList;
import java.util.Set;

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

    protected JFGSourceManager mSourceManager;

    protected CompositeSubscription mSubscriptions;
    private LongSparseArray<ResponseParser> mResponseParserMap = new LongSparseArray<>(32);

    protected V mView;
    protected ArrayList<Long> mRequestSeqs = new ArrayList<>(32);

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
        mSourceManager = DataSourceManager.getInstance();
        mView = (V) view;
    }

    @Override
    @CallSuper
    public void onStart() {
        onRegisterSubscription(mSubscriptions = new CompositeSubscription());
        onRegisterResponseParser();
    }

    @CallSuper
    protected void onRegisterSubscription(CompositeSubscription subscriptions) {
        subscriptions.add(getDeviceSyncSub());
        subscriptions.add(getLoginStateSub());
        subscriptions.add(getQueryDataRspSub());
    }

    @CallSuper
    protected void onRegisterResponseParser() {
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
        mSourceManager = null;
    }

    protected void onLoginStateChanged(RxEvent.LoginRsp loginState) {
        mView.onLoginStateChanged(loginState.state);
    }


    /**
     * 监听登录状态的变化时基本的功能,所以提取到基类中
     */
    private Subscription getLoginStateSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.LoginRsp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onLoginStateChanged, Throwable::printStackTrace);
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
                    if (mView != null && mView instanceof PropertyView) {
                        ((PropertyView) mView).onShowProperty(mSourceManager.getJFGDevice(mUUID));
                    }
                    onDeviceSync();
                }, Throwable::printStackTrace);
    }

    protected void onDeviceSync() {
    }

    /**
     * 监听请求数据的响应是基本功能,提取到基类
     */
    private Subscription getQueryDataRspSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetDataResponse.class)
                .subscribeOn(Schedulers.io())
                .filter(response -> mRequestSeqs.remove(response.seq))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                            ResponseParser parser = mResponseParserMap.get(response.msgId);
                            if (parser != null) {
                                Object value = mSourceManager.getValue(mUUID, response.msgId, response.seq);
                                if (JFGDevice.isSetType(response.msgId) && value != null) {
                                    Set<DataPoint> set = (Set<DataPoint>) value;
                                    parser.onParseResponse(set.toArray(new DataPoint[set.size()]));
                                } else {
                                    parser.onParseResponse((DataPoint) value);
                                }
                            }
                        }
                        , Throwable::printStackTrace);
    }


    public boolean hasReadyForExit() {
        return true;
//        long clickedTime = SystemClock.uptimeMillis();

//        long duration = clickedTime - mLastClickedTime;
//        mLastClickedTime = clickedTime;
//        return duration < 1500;
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

    protected void postDelay(Runnable action, long delay) {
        HandlerThreadUtils.postDelay(action, delay);
    }

    protected void registerResponseParser(int msg, ResponseParser parser) {
        mResponseParserMap.put(msg, parser);
    }

    public interface ResponseParser {
        /**
         * @param response 可能为BaseValue或者HashSet<BaseValue> 取决于消息类型,需要自己强转
         */
        void onParseResponse(DataPoint... response);
    }

}
