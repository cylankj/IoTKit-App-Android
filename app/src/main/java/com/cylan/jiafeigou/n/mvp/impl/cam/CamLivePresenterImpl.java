package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.misc.Convertor;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.utils.NetUtils;
import com.google.gson.Gson;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractPresenter<CamLiveContract.View> implements CamLiveContract.Presenter {
    private static final String TAG = "CamLivePresenterImpl";
    private DeviceBean bean;
    private BeanCamInfo beanCamInfo;
    private CompositeSubscription compositeSubscription;
    private boolean isRtcpSignal;

    public CamLivePresenterImpl(CamLiveContract.View view, DeviceBean bean) {
        super(view);
        view.setPresenter(this);
        this.bean = bean;
        this.beanCamInfo = Convertor.convert(bean);
    }

    /**
     * Rtcp和resolution的回调,
     * 只有resolution回调之后,才能设置{@link JfgAppCmd#setRenderLocalView(View)}
     * 正常播放回调
     *
     * @return
     */
    private Subscription RtcpNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<JFGMsgVideoRtcp, Boolean>() {
                    @Override
                    public Boolean call(JFGMsgVideoRtcp rtcp) {
                        return getView() != null && isRtcpSignal;
                    }
                })
                .subscribe(new Action1<JFGMsgVideoRtcp>() {
                    @Override
                    public void call(JFGMsgVideoRtcp rtcp) {
                        getView().onRtcp(rtcp);
                        Log.d(TAG, "rtcp: " + new Gson().toJson(rtcp));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("rtcp err: " + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 分辨率回调
     *
     * @return
     */
    private Subscription ResolutionNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .filter(new Func1<JFGMsgVideoResolution, Boolean>() {
                    @Override
                    public Boolean call(JFGMsgVideoResolution jfgMsgVideoResolution) {
                        boolean filter = getCamInfo() != null
                                && getCamInfo().deviceBase != null
                                && TextUtils.equals(getCamInfo().deviceBase.uuid, jfgMsgVideoResolution.peer)
                                && getView() != null;
                        if (!filter) {
                            AppLogger.e("getView(): " + (getView() != null));
                            AppLogger.e("this peer is out date: " + jfgMsgVideoResolution.peer);
                        }
                        return filter;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoResolution>() {
                    @Override
                    public void call(JFGMsgVideoResolution resolution) {
                        isRtcpSignal = true;
                        getView().onResolution(resolution);
                        AppLogger.i("ResolutionNotifySub: " + new Gson().toJson(resolution));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("resolution err: " + throwable.getLocalizedMessage());
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
                .filter(new Func1<JFGMsgVideoDisconn, Boolean>() {
                    @Override
                    public Boolean call(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
                        boolean notNull = getView() != null
                                && getCamInfo().deviceBase != null
                                && TextUtils.equals(getCamInfo().deviceBase.uuid, jfgMsgVideoDisconn.remote);
                        if (!notNull) {
                            AppLogger.e("err: " + getCamInfo());
                        }
                        return notNull;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGMsgVideoDisconn>() {
                    @Override
                    public void call(JFGMsgVideoDisconn jfgMsgVideoDisconn) {
                        getView().onFailed(jfgMsgVideoDisconn.code);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("videoDisconnectSub:" + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public void fetchHistoryData() {
        Observable.just(null)
                .observeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object dataStack) {
                        //获取设备历史录像
                        if (getCamInfo().deviceBase != null && getCamInfo().deviceBase.uuid != null) {
                            JfgCmdInsurance.getCmd().getVideoList(getCamInfo().deviceBase.uuid);
                            AppLogger.i("getVideoList");
                        }
                    }
                });
    }

    @Override
    public void startPlayVideo() {
        Observable.just(getCamInfo().deviceBase.uuid)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        //判断网络状况
                        final int net = NetUtils.getJfgNetType(getView().getContext());
                        AppLogger.i("play env state: " + net + " " + s);
                        if (net == 0) {
                            getView().onFailed(JFGRules.PlayErr.ERR_NERWORK);
                            return false;
                        }
                        return !TextUtils.isEmpty(s);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        JfgCmdInsurance.getCmd().playVideo(s);
                        AppLogger.i("play video");
                    }
                });
    }

    @Override
    public void stopPlayVideo() {
        Observable.just(getCamInfo().deviceBase.uuid)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        //判断网络状况
                        AppLogger.i("stop play env state:" + s);
                        return !TextUtils.isEmpty(s);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        JfgCmdInsurance.getCmd().stopPlay(s);
                    }
                });
    }

    @Override
    public BeanCamInfo getCamInfo() {
        if (this.beanCamInfo == null)
            this.beanCamInfo = Convertor.convert(this.bean);
        return beanCamInfo;
    }

    @Override
    public void start() {
        unSubscribe(compositeSubscription);
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(RtcpNotifySub());
        compositeSubscription.add(ResolutionNotifySub());
        compositeSubscription.add(videoDisconnectSub());
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
        compositeSubscription = null;
    }
}
