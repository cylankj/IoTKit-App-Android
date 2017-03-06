package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.HistoryDateFlatten;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.ERR_NERWORK;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.STOP_MAUNALLY;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractPresenter<CamLiveContract.View>
        implements CamLiveContract.Presenter, IFeedRtcp.MonitorListener {
    private int playType = CamLiveContract.TYPE_LIVE;
    private int[] videoResolution = {0, 0};
    private int playState = PLAY_STATE_IDLE;
    private HistoryDateFlatten historyDateFlatten = new HistoryDateFlatten();
    private IData historyDataProvider;
    private int stopReason = STOP_MAUNALLY;//手动断开
    private CompositeSubscription liveSubscription;
    /**
     * 帧率记录
     */
    private IFeedRtcp feedRtcp = new LiveFrameRateMonitor();

    public CamLivePresenterImpl(CamLiveContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
        feedRtcp.setMonitorListener(this);
    }


    /**
     * 视频断开连接
     * 只需要开始播放后注册
     *
     * @return
     */
    private Subscription videoDisconnectSub() {
        return Observable.create(subscriber -> {
            subscriber.onNext(null);
            subscriber.onCompleted();
            //只要JFGMsgVideoDisconn返回一次 满足条件的对象,videoDisconnectSub()这个链条就会被unsubscribe,
            //即使后面,再有JFGMsgVideoDisconn对象,下面这个zipWith也不会被执行,所以不会有内存泄露
        }).zipWith(RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
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
                        .takeFirst(disconn -> {
                            playState = PLAY_STATE_IDLE;
                            getView().onLiveStop(playType, disconn.code);
                            reset();
                            AppLogger.d("reset subscription");
                            return true;
                        }),
                new Func2<Object, JFGMsgVideoDisconn, Object>() {
                    @Override
                    public Object call(Object o, JFGMsgVideoDisconn disconn) {
                        AppLogger.i("jfgMsgVideoDisconn finish:");
                        return null;
                    }
                }).subscribe(o -> AppLogger.i("jfgMsgVideoDisconn finish:"),
                (Throwable throwable) -> {
                    AppLogger.e("videoDisconnectSub:" + throwable.getLocalizedMessage());
                });
    }

    @Override
    public int getPlayState() {
        return playState;
    }

    @Override
    public int getPlayType() {
        return playType;
    }

    @Override
    public void fetchHistoryDataList() {
        Observable.just(null)
                .filter(o -> !JFGRules.isShareDevice(uuid))//过滤分享设备
                .observeOn(Schedulers.newThread())
                .subscribe((Object dataStack) -> {
                    //获取设备历史录像
                    //不直接使用这个接口,因为在videoList的数据结构中没有uuid标签,只能使用请求的seq来判断.
                    //所有把它统一放到History类中管理.
                    DataSourceManager.getInstance().queryHistory(uuid);
                    AppLogger.i("getVideoList");
                });
    }

    @Override
    public boolean isShareDevice() {
        return JFGRules.isShareDevice(uuid);
    }

    @Override
    public void setStopReason(int stopReason) {
        this.stopReason = stopReason;
    }

    //    private Subscription playFlowSub;
    private void reset() {
        feedRtcp.stop();
        unSubscribe(liveSubscription);
        liveSubscription = new CompositeSubscription();
    }

    @Override
    public void startPlayVideo(int type) {
        getView().onLivePrepare(type);
        playState = PLAY_STATE_PREPARE;
        playType = CamLiveContract.TYPE_LIVE;
        reset();
        //加入管理,如果播放失败,收到disconnect
        liveSubscription.add(videoDisconnectSub());
        liveSubscription.add(prePlay(s -> {
            try {
                JfgCmdInsurance.getCmd().playVideo(uuid);
                AppLogger.i("play video: " + uuid);
            } catch (JfgException e) {
                e.printStackTrace();
            }
            return null;
        }).zipWith(getInterestingOne().timeout(10, TimeUnit.SECONDS, Observable.just("timeout")
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    AppLogger.e("play video :" + s);
                    //暂停播放
                    setStopReason(JFGRules.PlayErr.ERR_NOT_FLOW);
                    stopPlayVideo(playType);
                    return s;
                }))
                //filter getInterestingOne()
                .filter(result -> {
                    AppLogger.d("action: " + result);
                    return TextUtils.equals(result, "JFGMsgVideoResolution");
                }), (String s, Object o) -> {
            AppLogger.i("start to receive rtcp");
            //开始接收rtcp
            liveSubscription.add(rtcpNotifySub()
                    .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                    .subscribe());
            return null;
        }).subscribe(objectObservable -> AppLogger.e("flow done"),
                throwable -> AppLogger.e("flow done: " + throwable.getLocalizedMessage())));
    }

    /**
     * Rtcp和resolution的回调,
     * 只有resolution回调之后,才能设置{@link JfgAppCmd#enableRenderLocalView(boolean, View)} (View)}
     * 正常播放回调
     * 10s没有视频,直接断开
     *
     * @return
     */
    private Observable<Object> rtcpNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .filter((JFGMsgVideoRtcp rtcp) -> (getView() != null))
                .onBackpressureBuffer()//防止MissingBackpressureException
                .timeout(10, TimeUnit.SECONDS, Observable.just("no rtcp call back")
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .map(s -> {
                            //暂停播放
                            setStopReason(JFGRules.PlayErr.ERR_NOT_FLOW);
                            stopPlayVideo(playType);
                            AppLogger.e(s);
                            return null;
                        }))
                .subscribeOn(Schedulers.newThread())
                .map(rtcp -> {
                    feedRtcp.feed(rtcp);
                    return rtcp;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map((JFGMsgVideoRtcp rtcp) -> {
                    try {
                        getView().onRtcp(rtcp);
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                    return null;
                });
    }

    /**
     * disconnect 或者 分辨率
     *
     * @return
     */
    private Observable<String> getInterestingOne() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .filter(disconnect -> (TextUtils.equals(disconnect.remote, uuid)))
                .map((JFGMsgVideoDisconn disconn) -> {
                    AppLogger.e("disconnected: " + new Gson().toJson(disconn));
                    return "JFGMsgVideoDisconn";
                })
                .subscribeOn(Schedulers.io())
                .mergeWith(RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                        .filter(resolution -> TextUtils.equals(resolution.peer, uuid))
                        .map(resolution -> {
                            JfgCmdInsurance.getCmd().setAudio(false, false, false);
                            JfgCmdInsurance.getCmd().setAudio(true, false, false);
                            AppLogger.d("set default mic n speaker flag");
                            return resolution;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(resolution -> {
                            AppLogger.i("ResolutionNotifySub: " + new Gson().toJson(resolution) + "," + Thread.currentThread().getName());
                            videoResolution[0] = resolution.width;
                            videoResolution[1] = resolution.height;
                            try {
                                getView().onResolution(resolution);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                            getView().onLiveStarted(playType);
                            playState = PLAY_STATE_PLAYING;
                            return "JFGMsgVideoResolution";
                        }))
                .first();
    }

    /**
     * 1.检查网络
     * 2.开始播放
     *
     * @return
     */
    private Observable<String> prePlay(Func1<String, String> func1) {
        return Observable.just("")
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(o -> {
                    if (NetUtils.getJfgNetType(getView().getContext()) == 0) {
                        //断网了
                        setStopReason(ERR_NERWORK);
                        stopPlayVideo(getPlayType());
                        AppLogger.i("stop play  video for err network");
                        return false;
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .map(func1);
    }

    @Override
    public void startPlayHistory(long time) {
        getView().onLivePrepare(CamLiveContract.TYPE_HISTORY);
        playType = CamLiveContract.TYPE_HISTORY;
        playState = PLAY_STATE_PREPARE;
        reset();
        //加入管理,如果播放失败,收到disconnect
        liveSubscription.add(videoDisconnectSub());
        liveSubscription.add(prePlay(s -> {
            try {
                //先停止播放{历史录像,直播都需要停止播放}
                if (playState != PLAY_STATE_IDLE) {
                    JfgCmdInsurance.getCmd().stopPlay(uuid);
                    AppLogger.i("stop play history");
                }
                JfgCmdInsurance.getCmd().playHistoryVideo(uuid, time / 1000L);
                AppLogger.i(String.format("play history video:%s,%s ", uuid, time / 1000L));
            } catch (JfgException e) {
                AppLogger.e("err:" + e.getLocalizedMessage());
            }
            return null;
        }).zipWith(getInterestingOne().timeout(10, TimeUnit.SECONDS, Observable.just("timeout")
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    AppLogger.e("play history video :" + s);
                    //暂停播放
                    setStopReason(JFGRules.PlayErr.ERR_NOT_FLOW);
                    stopPlayVideo(playType);
                    return s;
                }))
                //filter getInterestingOne()
                .filter(result -> {
                    AppLogger.d("action: " + result);
                    return TextUtils.equals(result, "JFGMsgVideoResolution");
                }), (String s, Object o) -> {
            AppLogger.i("start to receive rtcp");
            //开始接收rtcp
            liveSubscription.add(rtcpNotifySub()
                    .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                    .subscribe());
            return null;
        }).subscribe(objectObservable -> AppLogger.e("flow done"),
                throwable -> AppLogger.e("flow done: " + throwable.getLocalizedMessage())));
    }

    @Override
    public void stopPlayVideo(int type) {
        AppLogger.d("pre play state: " + playState);
        if (playState == PLAY_STATE_PLAYING) {
            //暂停播放了，还需要截图
            takeSnapShot(true);
        }
        reset();
        Observable.just(uuid)
                .subscribeOn(Schedulers.newThread())
                .map((String s) -> {
                    try {
                        JfgCmdInsurance.getCmd().stopPlay(s);
                        playType = CamLiveContract.TYPE_NONE;
                        playState = PLAY_STATE_IDLE;
                        AppLogger.i("stopPlayVideo:" + s);
                    } catch (JfgException e) {
                        AppLogger.e("stop play err: " + e.getLocalizedMessage());
                    }
                    return null;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(() -> {
                    getView().onLiveStop(playType, stopReason);
                    AppLogger.d("live stop: " + stopReason);
                })
                .doOnError(throwable -> AppLogger.e("" + throwable.getLocalizedMessage()))
                .subscribe();
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void switchSpeaker() {
        Observable.just(true)
                .subscribeOn(Schedulers.newThread())
                .subscribe((Boolean aBoolean) -> {
                    boolean localMic = getView().isLocalMicOn();
                    boolean localSpeaker = getView().isLocalSpeakerOn();
                    boolean remoteMic = getView().isLocalSpeakerOn();
                    boolean remoteSpeaker = getView().isLocalMicOn();//imageview 图标状态已经更新了。
                    if (localSpeaker) {

                    } else {
                        remoteMic = false;
                        remoteSpeaker = false;
                        localSpeaker = false;
                        localMic = false;
                    }
                    JfgCmdInsurance.getCmd().setAudio(false, remoteSpeaker, remoteMic);
                    JfgCmdInsurance.getCmd().setAudio(true, localSpeaker, localMic);
                    AppLogger.i(String.format(Locale.getDefault(), "localMic:%s,LocalSpeaker:%s,remoteMic:%s,remoteSpeaker:%s", localMic, localSpeaker, remoteMic, remoteSpeaker));
                });
    }

    @Override
    public void switchMic() {
        Observable.just(true)
                .subscribeOn(Schedulers.newThread())
                .subscribe((Boolean aBoolean) -> {
                    boolean localMic = getView().isLocalMicOn();
                    boolean localSpeaker = getView().isLocalSpeakerOn();
                    boolean remoteMic = localSpeaker;
                    boolean remoteSpeaker = localMic;//imageview 图标状态已经更新了。
                    if (localMic) {
                        localSpeaker = true;
                        remoteMic = true;
                        remoteSpeaker = true;
                    } else {
                        remoteSpeaker = false;
                    }
                    JfgCmdInsurance.getCmd().setAudio(false, remoteSpeaker, remoteMic);
                    JfgCmdInsurance.getCmd().setAudio(true, localSpeaker, localMic);
                    AppLogger.i(String.format(Locale.getDefault(), "localMic:%s,LocalSpeaker:%s,remoteMic:%s,remoteSpeaker:%s", localMic, localSpeaker, remoteMic, remoteSpeaker));
                });
    }

    @Override
    public int getLocalMicSpeakerBit() {
        if (getView() == null) return 0;
        int mic = getView().isLocalMicOn() ? 2 : 0;
        int speaker = getView().isLocalSpeakerOn() ? 1 : 0;
        return mic + speaker;
    }

    @Override
    public void stop() {
        super.stop();
        reset();
    }


    @Override
    public void takeSnapShot(boolean forPreview) {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(o -> {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    long time = System.currentTimeMillis();
                    byte[] data = JfgCmdInsurance.getCmd().screenshot(false);
                    if (data == null) {
                        AppLogger.e("直播黑屏，没有数据");
                        return null;
                    }
                    Bitmap bitmap = BitmapUtils.byte2bitmap(videoResolution[0], videoResolution[1], data);
                    AppLogger.i("capture take shot performance: " + (System.currentTimeMillis() - time));
                    return bitmap;
                })
                .filter(bitmap -> bitmap != null)
                .subscribeOn(Schedulers.io())
                .subscribe((Bitmap bitmap) -> {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    String filePath;
                    long time = System.currentTimeMillis();
                    if (forPreview) {
                        filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + "_";
                    } else {
                        snapshotResult(bitmap);
                        filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                        BitmapUtils.saveBitmap2file(bitmap, filePath);
                    }
                    AppLogger.i("save take shot performance: " + (System.currentTimeMillis() - time));
                }, throwable -> AppLogger.e("takeSnapshot: " + throwable.getLocalizedMessage()));
    }

    private void snapshotResult(Bitmap bitmap) {
        Log.d("takeSnapShot", "takeSnapShot: " + (bitmap));
        Observable.just(bitmap)
                .filter((Bitmap bit) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Bitmap b) -> getView().onTakeSnapShot(b),
                        throwable -> AppLogger.e("snapshotResult:" + throwable.getLocalizedMessage()));
    }


    @Override
    public void saveAlarmFlag(boolean flag) {
        Log.d("saveAlarmFlag", "saveAlarmFlag: " + flag);
    }

    @Override
    public Map<Long, Long> getFlattenDateMap() {
        return historyDateFlatten.getFlattenMap();
    }

    @Override
    public IData getHistoryDataProvider() {
        return historyDataProvider;
    }

    @Override
    public boolean needShowHistoryWheelView() {
        DpMsgDefine.DPNet net = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_201_NET);
        JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(uuid);
        DpMsgDefine.DPSdStatus sdStatus = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_204_SDCARD_STORAGE);
        boolean show = JFGRules.isDeviceOnline(net)
                && NetUtils.getJfgNetType(getView().getContext()) != 0
                && device != null && TextUtils.isEmpty(device.shareAccount)
                && sdStatus.hasSdcard && sdStatus.err == 0;
        AppLogger.i("show: " + show);
        return show;
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        DataSourceManager.getInstance().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }


    @Override
    public void startCountForDismissPop() {
        addSubscription(Observable.just("count_5_s")
                .subscribeOn(Schedulers.newThread())
                .delay(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(s -> getView() != null)
                .subscribe(s -> getView().countdownFinish(),
                        throwable -> AppLogger.e("countdown finish")));
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                robotDataSync(),
                historyDataListSub()};
    }

    /**
     * 接受历史录像数据
     *
     * @return
     */
    private Subscription historyDataListSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JFGHistoryVideoParseRsp.class)
                .filter(historyList -> TextUtils.equals(uuid, historyList.uuid))//过滤uuid
                .subscribeOn(Schedulers.computation())
                .map((RxEvent.JFGHistoryVideoParseRsp jfgHistoryVideo) -> {
                    long time = System.currentTimeMillis();
                    ArrayList<JFGVideo> finalList = DataSourceManager.getInstance().getHistoryList(uuid);
                    if (finalList == null || finalList.size() == 0)
                        return null;
                    Collections.sort(finalList);
                    AppLogger.d(String.format("performance:%s", (System.currentTimeMillis() - time)));
                    AppLogger.i("performance:" + new Gson().toJson(jfgHistoryVideo));
                    IData data = new DataExt();
                    data.flattenData(finalList);
                    historyDateFlatten.flat(finalList);
                    return historyDataProvider = data;
                })
                .filter((IData dataStack) -> (getView() != null && dataStack != null))
                .observeOn(AndroidSchedulers.mainThread())
                .map((IData dataStack) -> {
                    getView().onHistoryDataRsp(dataStack);
                    return null;
                })
                .retry(new RxHelper.ExceptionFun<>("historyDataListSub"))
                .subscribe();
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDataSync() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter((RxEvent.DeviceSyncRsp jfgRobotSyncData) -> (
                        getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .flatMap(new Func1<RxEvent.DeviceSyncRsp, Observable<Long>>() {
                    @Override
                    public Observable<Long> call(RxEvent.DeviceSyncRsp deviceSyncRsp) {
                        AppLogger.d("updateList: " + deviceSyncRsp.idList);
                        return Observable.from(deviceSyncRsp.idList);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map((Long id) -> {
                    getView().onDeviceInfoChanged(id);
                    return null;
                })
                .retry(new RxHelper.RxException<>("robotDataSync"))
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .subscribe();
    }

    @Override
    public void onFrameFailed() {
        AppLogger.e("is bad net work");
        //暂停播放
        setStopReason(JFGRules.PlayErr.ERR_LOW_FRAME_RATE);
        stopPlayVideo(playType);
    }

    @Override
    public void onFrameRate(boolean slow) {
        Observable.just(slow)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(slowFrameRate -> {
                    getView().shouldWaitFor(slow);
                }, throwable -> {
                    AppLogger.e("err: " + throwable.getLocalizedMessage());
                });
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            int type = NetUtils.getNetType(context);
            if (type == -1) {
                AppLogger.i("there is no network ");
                setStopReason(ERR_NERWORK);
                stopPlayVideo(getPlayType());
            }
        }
    }
}
