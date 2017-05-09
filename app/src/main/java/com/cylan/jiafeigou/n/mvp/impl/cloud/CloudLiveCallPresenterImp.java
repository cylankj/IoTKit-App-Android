package com.cylan.jiafeigou.n.mvp.impl.cloud;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveCallContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.CloseUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/10/19
 * 描述：
 */
public class CloudLiveCallPresenterImp extends AbstractPresenter<CloudLiveCallContract.View> implements CloudLiveCallContract.Presenter {

    private String uuid;
    private boolean isConnectOk;
    private Subscription loadProAnimSub;
    private int loadNum = 0;
    private CompositeSubscription subscription;
    private DbManager base_db;
    private String userIcon;
    private Subscription delaySub;

    public CloudLiveCallPresenterImp(CloudLiveCallContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }

    @Override
    public void start() {
        super.start();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        } else {
            subscription = new CompositeSubscription();
            subscription.add(resolutionNotifySub());
            subscription.add(videoDisconnectSub());
            subscription.add(callingResult());
            subscription.add(getAccount());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

        if (delaySub != null && !delaySub.isUnsubscribed()) {
            delaySub.unsubscribe();
        }
    }

    /**
     * 中控呼出
     */
    @Override
    public void onCloudCallConnettion() {
        showLoadProgressAnim();
        //判断网络状况
        final int net = NetUtils.getJfgNetType(ContextUtils.getContext());
        AppLogger.i("play initSubscription live " + net + " " + uuid);
        if (net == 0) {
            getView().onLiveStop(JFGRules.PlayErr.ERR_NETWORK);
            return;
        }
        try {
            if (uuid != null) {
                int ret = BaseApplication.getAppComponent().getCmd().stopPlay(uuid);
                AppLogger.e("disconnect ret: " + ret);
            }
            isConnectOk = false;
            BaseApplication.getAppComponent().getCmd().playVideo(uuid);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Subscription resolutionNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .filter((JFGMsgVideoResolution jfgMsgVideoResolution) -> {
                    boolean filter =
                            TextUtils.equals(uuid, jfgMsgVideoResolution.peer)
                                    && getView() != null;
                    if (!filter) {
                        AppLogger.e("getView(): " + (getView() != null));
                        AppLogger.e("this peer is out date: " + jfgMsgVideoResolution.peer);
                    }
                    return filter;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((JFGMsgVideoResolution resolution) -> {
                    try {
                        getView().onResolution(resolution);
                        isConnectOk = true;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    if (loadProAnimSub != null && !loadProAnimSub.isUnsubscribed()) {
                        loadProAnimSub.unsubscribe();
                    }
                    AppLogger.i("ResolutionNotifySub: " + new Gson().toJson(resolution));
                }, (Throwable throwable) -> {
                    AppLogger.e("resolution err: " + throwable.getLocalizedMessage());
                });
    }

    /**
     * 正在连接中...
     */
    public void showLoadProgressAnim() {
        loadProAnimSub = Observable.interval(500, 300, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        String[] loadContext = {".", "..", "...",};
                        getView().showLoadingView();
                        getView().setLoadingText(loadContext[loadNum++]);
                        if (loadNum == 3) {
                            loadNum = 0;
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 视频断开连接
     *
     * @return
     */
    private Subscription videoDisconnectSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .subscribeOn(Schedulers.newThread())
                .filter((JFGMsgVideoDisconn jfgMsgVideoDisconn) -> {
                    boolean notNull = getView() != null
                            && TextUtils.equals(uuid, jfgMsgVideoDisconn.remote);
                    if (!notNull) {
                        AppLogger.e("err: " + uuid);
                    } else {
                        AppLogger.i("stop for reason: " + jfgMsgVideoDisconn.code);
                    }
                    return notNull;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((JFGMsgVideoDisconn jfgMsgVideoDisconn) -> {
                    getView().onLiveStop(jfgMsgVideoDisconn.code);
                }, (Throwable throwable) -> {
                    AppLogger.e("videoDisconnectSub:" + throwable.getLocalizedMessage());
                });
    }


    @Override
    public void stopPlayVideo() {
        Observable.just(uuid)
                .subscribeOn(Schedulers.newThread())
                .subscribe((String s) -> {
                    try {
                        AppLogger.i("stopPlayVideo:" + s);
                        BaseApplication.getAppComponent().getCmd().stopPlay(s);
//                        JfgCmdInsurance.getCmd().enableCamera(false, false);
                        BaseApplication.getAppComponent().getCmd().enableRenderLocalView(false, null);
//                        JfgCmdInsurance.getCmd().enableRenderRemoteView(false, null);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    /**
     * 呼叫的结果
     *
     * @return
     */
    @Override
    public Subscription callingResult() {
        return RxBus.getCacheInstance().toObservable(RxEvent.EFamilyMsgpack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.EFamilyMsgpack>() {
                    @Override
                    public void call(RxEvent.EFamilyMsgpack eFamilyMsgpack) {
                        if (getView() != null) {
                            getView().handlerCallingReuslt(eFamilyMsgpack.msgId);
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 30s计时
     */
    @Override
    public void countTime() {
        delaySub = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .delay(30000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (getView() != null) {
                            if (!isConnectOk) {
                                getView().handlerCallingReuslt(0);
                            }
                        }
                    }
                }, AppLogger::e);
    }

    @Override
    public void saveIntoDb(CloudLiveBaseDbBean bean) {
        try {
            if (base_db == null) return;
            base_db.save(bean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getUserInfo -> {
                    if (getUserInfo != null && getUserInfo instanceof RxEvent.AccountArrived) {
                        if (getView() != null) {
                            base_db = DataBaseUtil.getInstance(getUserInfo.jfgAccount.getAccount()).dbManager;
                            userIcon = getUserInfo.jfgAccount.getPhotoUrl();
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 字符串转byte[]
     */
    @Override
    public byte[] getSerializedObject(Serializable s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(s);
        } catch (IOException e) {
            return null;
        } finally {
            CloseUtils.close(oos);
        }
        byte[] result = baos.toByteArray();
        return result;
    }

    @Override
    public boolean getIsConnectOk() {
        return isConnectOk;
    }


    @Override
    public String parseTime(long times) {
        Date time = new Date(times);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
        String dateString = formatter.format(time);
        return dateString;
    }

    @Override
    public String getUserIcon() {
        return userIcon;
    }

}
