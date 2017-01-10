package com.cylan.jiafeigou.n.mvp.impl.cloud;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveCallContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.utils.NetUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;

/**
 * 作者：zsl
 * 创建时间：2016/10/19
 * 描述：
 */
public class CloudLiveCallPresenterImp extends AbstractPresenter<CloudLiveCallContract.View> implements CloudLiveCallContract.Presenter {

    private String uuid;
    private boolean isInLive;
    private Subscription loadProAnimSub;
    private int loadNum = 0;
    private CompositeSubscription subscription;

    public CloudLiveCallPresenterImp(CloudLiveCallContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }

    @Override
    public void start() {
        super.start();
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }else {
            subscription = new CompositeSubscription();
            subscription.add(resolutionNotifySub());
            subscription.add(videoDisconnectSub());
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }

    /**
     * 中控呼出
     */
    @Override
    public void onCloudCallOut() {
        showLoadProgressAnim();
        //判断网络状况
        final int net = NetUtils.getJfgNetType(ContextUtils.getContext());
        AppLogger.i("play start live " + net + " " + uuid);
        if (net == 0) {
            getView().onLiveStop(JFGRules.PlayErr.ERR_NERWORK);
            return;
        }
        try {
            if (uuid != null) JfgCmdInsurance.getCmd().stopPlay(uuid);
            isInLive = true;
            JfgCmdInsurance.getCmd().playVideo(uuid);
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
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                        if (loadProAnimSub != null && !loadProAnimSub.isUnsubscribed()){
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
                });
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
                        JfgCmdInsurance.getCmd().stopPlay(s);
                        JfgCmdInsurance.getCmd().removeRenderLocalView();
                        JfgCmdInsurance.getCmd().removeRenderRemoteView();
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                });
    }

}
