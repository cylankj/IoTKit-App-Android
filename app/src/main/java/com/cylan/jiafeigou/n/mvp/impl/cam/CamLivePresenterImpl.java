package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.base.module.Base;
import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.utils.JfgUtils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.ERR_NETWORK;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_LIVE;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractPresenter<CamLiveContract.View>
        implements CamLiveContract.Presenter, IFeedRtcp.MonitorListener {
    private IData historyDataProvider;
    private MapSubscription liveSubscription = new MapSubscription();
    /**
     * 保存当前播放的方式,eg:从播放历史视频切换到设置页面,回来之后,需要继续播放历史视频.
     */
    private CamLiveContract.PrePlayType prePlayType;
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
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .subscribeOn(Schedulers.newThread())
                .filter((JFGMsgVideoDisconn jfgMsgVideoDisconn) -> {
                    boolean notNull = getView() != null;
                    if (!notNull) {
                        AppLogger.e("err: " + uuid + " remote:" + jfgMsgVideoDisconn.remote);
                    } else {
                        AppLogger.i("stop for reason: " + jfgMsgVideoDisconn.code);
                        try {
                            BaseApplication.getAppComponent().getCmd().stopPlay(uuid);
                            AppLogger.d("停止播放");
                        } catch (JfgException e) {

                        }
                    }
                    return notNull;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .takeFirst(disconnect -> {
                    updatePrePlayType(-1, -1, PLAY_STATE_IDLE);
                    getView().onLiveStop(getPrePlayType().type, disconnect.code);
                    reset();
                    AppLogger.d("reset subscription");
                    return true;
                }).subscribe(ret -> {
                }, AppLogger::e);
    }

    @Override
    public String getThumbnailKey() {
        return PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid);
    }

    @Override
    public int getPlayState() {
        return getPrePlayType().playState;
    }

    @Override
    public int getPlayType() {
        return getPrePlayType().type;
    }


    /**
     * 一天一天地查询
     *
     * @param timeStartInDay:可以用来查询数据库
     */

    @Override
    public Observable<IData> assembleTheDay(long timeStartInDay) {
        long timeEnd = timeStartInDay + 24 * 3600 - 1;
        AppLogger.d("historyFile:timeEnd?" + timeStartInDay);
        return BaseApplication.getAppComponent().getDBHelper().loadHistoryFile(uuid, timeStartInDay, timeEnd)
                .subscribeOn(Schedulers.io())
                .flatMap(historyFiles -> {
                    AppLogger.d("load hisFile List: " + ListUtils.getSize(historyFiles));
                    historyDataProvider.flattenData(new ArrayList<>(historyFiles));
                    return Observable.just(historyDataProvider);
                });
    }

    @Override
    public CamLiveContract.PrePlayType getPrePlayType() {
        if (prePlayType == null) {
            this.prePlayType = new CamLiveContract.PrePlayType();
        }
        return this.prePlayType;
    }

    @Override
    public void updatePrePlayType(CamLiveContract.PrePlayType prePlayType) {
        this.prePlayType = prePlayType;
    }

    @Override
    public float getVideoPortHeightRatio() {
        AppLogger.d("获取分辨率?");
        float cache = PreferencesUtils.getFloat(JConstant.KEY_UUID_RESOLUTION + uuid, 0.0f);
        if (cache == 0.0f) cache = JFGRules.getDefaultPortHeightRatio(0);
        return PreferencesUtils.getFloat(JConstant.KEY_UUID_RESOLUTION + uuid, cache);
    }

    public void assembleTheDay(ArrayList<HistoryFile> files) {
        if (historyDataProvider == null) {
            historyDataProvider = new DataExt();
        }
        if (historyDataProvider.getDataCount() == 0) {
            Subscription subscription = assembleTheDay(TimeUtils.getSpecificDayStartTime(files.get(0).getTime() * 1000L) / 1000L)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        mView.onHistoryDataRsp(historyDataProvider);
                        AppLogger.d("历史录像wheel准备好");
                    }, AppLogger::e);
            addSubscription(subscription, "hisFlat");
        }
    }

    private void test() {
        Observable.just("go")
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        DpMsgDefine.V3DateListReq req = new DpMsgDefine.V3DateListReq();
                        req.beginTime = (int) (TimeUtils.getTodayStartTime() / 1000);
                        req.limit = 30;
                        req.asc = false;//向后
                        byte[] data = DpUtils.pack(req);
                        Base.ForewordSedHeader header = new Base.ForewordSedHeader();
                        header.mSeq = System.currentTimeMillis();
                        header.mCaller = uuid;
//                        header.mCaller = uuid;
                        header.mId = 20006;
                        header.cidArray = new String[]{uuid};
                        header.isAck = 1;//需要相应
                        header.msgId = 2000;
                        header.msgByte = data;
                        int ret = BaseApplication.getAppComponent().getCmd().SendForwardData(DpUtils.pack(header));
                        AppLogger.d("send foreword: " + ret);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("err:" + MiscUtils.getErr(throwable));
                    }
                });
    }

    //    @Override
    public void fetchHistoryDataList() {
//        test();
        if (historyDataProvider != null && historyDataProvider.getDataCount() > 0) {
            AppLogger.d("有历史录像了.");
            return;
        }
        Subscription subscription = BaseApplication.getAppComponent().getSourceManager().queryHistory(uuid)
                .subscribeOn(Schedulers.newThread())
                .filter(ret -> {
                    AppLogger.d("get history?" + ret);
                    return ret;
                })
                .timeout(30, TimeUnit.SECONDS)
                .flatMap(integer -> RxBus.getCacheInstance().toObservable(RxEvent.JFGHistoryVideoParseRsp.class)
                        .filter(rsp -> TextUtils.equals(rsp.uuid, uuid))
                        .filter(rsp -> ListUtils.getSize(rsp.historyFiles) > 0)//>0
                        .map(rsp -> {
                            //只需要初始化一天的就可以啦.
                            assembleTheDay(rsp.historyFiles);
                            return null;
                        })
                        .filter(result -> mView != null)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .map(longs -> {
                            //更新日历
                            ArrayList<Long> dateList = BaseApplication.getAppComponent().getSourceManager().getHisDateList(uuid);
                            mView.onHistoryDateListUpdate(dateList);
                            AppLogger.d("历史录像日历更新,天数: " + ListUtils.getSize(dateList));
                            return null;
                        }))
                .subscribe(ret -> {
                }, AppLogger::e);
        addSubscription(subscription, "getHistoryList");
    }

    @Override
    public boolean isShareDevice() {
        return JFGRules.isShareDevice(uuid);
    }

//    @Override
//    public void setStopReason(int stopReason) {
//        this.stopReason = stopReason;
//    }

    private void reset() {
        feedRtcp.stop();
        unSubscribe(liveSubscription);
        liveSubscription = new MapSubscription();
    }

    @Override
    public void startPlay() {
        if (mView == null || !mView.isUserVisible()) return;
        if (getPrePlayType().playState == PLAY_STATE_PREPARE) {
            AppLogger.d("已经loading");
            mView.onLivePrepare(getPrePlayType().type);
            return;
        }
        CamLiveContract.PrePlayType prePlayType = getPrePlayType();
        if (prePlayType.type == TYPE_LIVE) {
        } else if (prePlayType.type == TYPE_HISTORY) {
            startPlayHistory(prePlayType.time);
            return;
        } else return;
        DpMsgDefine.DPNet net = getDevice().$(201, new DpMsgDefine.DPNet());
        if (!JFGRules.isDeviceOnline(net)) {
            updatePrePlayType(TYPE_LIVE, -1, PLAY_STATE_IDLE);
            mView.onLiveStop(TYPE_LIVE, JFGRules.PlayErr.ERR_DEVICE_OFFLINE);
            return;
        }
        updatePrePlayType(TYPE_LIVE, -1, PLAY_STATE_PREPARE);
        getView().onLivePrepare(TYPE_LIVE);
        reset();
        //加入管理,如果播放失败,收到disconnect
        liveSubscription.add(videoDisconnectSub(), "videoDisconnectSub");
        liveSubscription.add(prePlay(s -> {
            try {
                int ret = BaseApplication.getAppComponent().getCmd().playVideo(uuid);
                if (ret != 0) {
                    BaseApplication.getAppComponent().getCmd().stopPlay(uuid);
                    ret = BaseApplication.getAppComponent().getCmd().playVideo(uuid);
                }
                AppLogger.i("play video: " + uuid + " " + ret);
            } catch (JfgException e) {
                e.printStackTrace();
            }
            return null;
        }).zipWith(getInterestingOne().timeout(30, TimeUnit.SECONDS, Observable.just("timeout")
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    AppLogger.e("play video :" + s);
                    //暂停播放
                    stopPlayVideo(JFGRules.PlayErr.ERR_NOT_FLOW).subscribe(ret -> {
                    }, AppLogger::e);
                    return s;
                }))
                //filter getInterestingOne()
                .filter(result -> {
                    AppLogger.d("option: " + result);
                    return TextUtils.equals(result, "JFGMsgVideoResolution");
                }), (String s, Object o) -> {
            AppLogger.i("initSubscription to receive rtcp");
            //开始接收rtcp
            liveSubscription.add(rtcpNotifySub(), "rtcpNotifySub");
            return null;
        }).subscribe(objectObservable -> {
                    AppLogger.d("播放流程走通 done");
                    if (historyDataProvider == null || historyDataProvider.getDataCount() == 0) {
                        fetchHistoryDataList();//播放成功后,才拉取历史录像
                    }
                },
                throwable -> AppLogger.e("flow done: " + throwable.getLocalizedMessage())), "prePlay");
    }

    /**
     * Rtcp和resolution的回调,
     * 只有resolution回调之后,才能设置{@link JfgAppCmd#enableRenderLocalView(boolean, View)} (View)}
     * 正常播放回调
     * 10s没有视频,直接断开
     *
     * @return
     */
    private Subscription rtcpNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .filter((JFGMsgVideoRtcp rtcp) -> (getView() != null))
                .onBackpressureBuffer()//防止MissingBackpressureException
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .map(rtcp -> {
                    feedRtcp.feed(rtcp);
                    updatePrePlayType(getPrePlayType().type, rtcp.timestamp, PLAY_STATE_PLAYING);
                    return rtcp;
                })
                .doOnError(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        //暂停播放
                        stopPlayVideo(JFGRules.PlayErr.ERR_NOT_FLOW).subscribe(ret -> {
                        }, AppLogger::e);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rtcp -> {
                    try {
                        getView().onRtcp(rtcp);
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
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
                .subscribeOn(Schedulers.newThread())
                .mergeWith(RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                        .filter(resolution -> TextUtils.equals(resolution.peer, uuid))
                        .observeOn(Schedulers.newThread())
                        .map(resolution -> {
                            setupAudio(false, false, false, false);
                            PreferencesUtils.putFloat(JConstant.KEY_UUID_RESOLUTION + uuid, (float) resolution.height / resolution.width);
                            //注册监听耳机
                            registerHeadSetObservable();
                            return resolution;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(resolution -> {
                            AppLogger.i("ResolutionNotifySub: " + new Gson().toJson(resolution) + "," + Thread.currentThread().getName());
                            try {
                                getView().onResolution(resolution);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                            updatePrePlayType(-1, -1, PLAY_STATE_PLAYING);
                            getView().onLiveStarted(getPrePlayType().type);
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
                    if (NetUtils.getJfgNetType() == 0) {
                        //断网了
                        stopPlayVideo(ERR_NETWORK).subscribe(ret -> {
                        }, AppLogger::e);
                        AppLogger.i("stop play  video for err network");
                        return false;
                    }
                    return true;
                })
                .subscribeOn(Schedulers.io())
                .map(func1);
    }

    private void updatePrePlayType(int type, long time, int state) {
        if (prePlayType == null) prePlayType = new CamLiveContract.PrePlayType();
        if (type != -1)
            prePlayType.type = type;
        if (time != -1)
            prePlayType.time = time;
        prePlayType.playState = state;
        Log.d("updatePrePlayType", "updatePrePlayType:" + prePlayType);
    }

    @Override
    public void startPlayHistory(long t) {
        //保证得到s System.currentTimeMillis() / t == 0 的条件范围可能有点小
        if (t == 0) {
            t = 1;
            if (BuildConfig.DEBUG) throw new IllegalArgumentException("怎么会有这种情况发生");
        }
        final long time = System.currentTimeMillis() / t > 100 ? t : t / 1000;
        getView().onLivePrepare(TYPE_HISTORY);
        updatePrePlayType(TYPE_HISTORY, time, PLAY_STATE_PREPARE);
        DpMsgDefine.DPNet net = getDevice().$(201, new DpMsgDefine.DPNet());
        if (!JFGRules.isDeviceOnline(net)) {
            updatePrePlayType(TYPE_HISTORY, -1, PLAY_STATE_IDLE);
            mView.onLiveStop(TYPE_HISTORY, JFGRules.PlayErr.ERR_DEVICE_OFFLINE);
            return;
        }
        reset();
        //加入管理,如果播放失败,收到disconnect
        liveSubscription.add(videoDisconnectSub(), "videoDisconnectSub");
        liveSubscription.add(prePlay(s -> {
            try {
                //先停止播放{历史录像,直播都需要停止播放}
                if (getPrePlayType().playState != PLAY_STATE_IDLE) {
                    BaseApplication.getAppComponent().getCmd().stopPlay(uuid);
                    AppLogger.i("stop play history");
                }
                int ret = BaseApplication.getAppComponent().getCmd().playHistoryVideo(uuid, time);
                if (ret != 0) {
                    BaseApplication.getAppComponent().getCmd().stopPlay(uuid);
                    ret = BaseApplication.getAppComponent().getCmd().playHistoryVideo(uuid, time);
                }
                AppLogger.i(String.format("play history video:%s,%s ", uuid, time) + " " + ret);
            } catch (JfgException e) {
                AppLogger.e("err:" + e.getLocalizedMessage());
            }
            return null;
        }).zipWith(getInterestingOne().timeout(30, TimeUnit.SECONDS, Observable.just("timeout")
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    AppLogger.e("play history video :" + s);
                    //暂停播放
                    stopPlayVideo(JFGRules.PlayErr.ERR_NOT_FLOW).subscribe(ret -> {
                    }, AppLogger::e);
                    return s;
                }))
                //filter getInterestingOne()
                .filter(result -> {
                    AppLogger.d("option: " + result);
                    return TextUtils.equals(result, "JFGMsgVideoResolution");
                }), (String s, Object o) -> {
            AppLogger.i("initSubscription to receive rtcp");
            //开始接收rtcp
            liveSubscription.add(rtcpNotifySub(), "rtcpNotifySub");
            return null;
        }).subscribe(objectObservable -> AppLogger.e("flow done"),
                throwable -> AppLogger.e("flow done: " + throwable.getLocalizedMessage())), "prePlay");
    }

    @Override
    public Observable<Boolean> stopPlayVideo(int reasonOrState) {
        AppLogger.d("pre play state: " + prePlayType);
        if (getPrePlayType().playState == PLAY_STATE_PLAYING) {
            //暂停播放了，还需要截图
            takeSnapShot(false);
        }
        reset();
        return Observable.just(uuid)
                .subscribeOn(Schedulers.newThread())
                .flatMap((String s) -> {
                    try {
//                        if (getPrePlayType().playState == PLAY_STATE_PLAYING) {
                        setupAudio(false, false, false, false);
//                        }
                        BaseApplication.getAppComponent().getCmd().stopPlay(s);
                        updatePrePlayType(-1, -1, reasonOrState);
                        AppLogger.i("stopPlayVideo:" + s);
                    } catch (JfgException e) {
                        AppLogger.e("stop play err: " + e.getLocalizedMessage());
                    }
                    AppLogger.d("live stop: " + reasonOrState);
                    if (getView() != null)
                        getView().onLiveStop(getPrePlayType().type, reasonOrState);
                    return Observable.just(true);
                })
                .doOnError(throwable -> AppLogger.e("" + throwable.getLocalizedMessage()));
    }

    @Override
    public Observable<Boolean> stopPlayVideo(boolean detach) {
        return stopPlayVideo(PLAY_STATE_IDLE);
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    /**
     * 查看 doc/mic_speaker设置.md
     * localSpeaker 0->1{ }
     *
     * @return
     */
    @Override
    public Observable<Boolean> switchSpeaker() {
        return Observable.just(true)
                .subscribeOn(Schedulers.newThread())
                .flatMap(ret -> {
                    //这些都表示当前状态
                    boolean localMic = getView().isLocalMicOn();
                    boolean localSpeaker = getView().isLocalSpeakerOn();
                    boolean remoteMic = getView().isLocalSpeakerOn();
                    boolean remoteSpeaker = getView().isLocalMicOn();//imageview 图标状态已经更新了。
                    if (localSpeaker) {
                        //下一步,
                        localSpeaker = false;
                        remoteMic = false;
                    } else {
                        remoteMic = true;
                        remoteSpeaker = localMic;
                        localSpeaker = true;
                    }
                    boolean result = setupAudio(localMic, localSpeaker, remoteMic, remoteSpeaker);
                    return Observable.just(result);
                });
    }

    private boolean setupAudio(boolean localMic, boolean localSpeaker, boolean remoteMic, boolean remoteSpeaker) {
        AppLogger.d(String.format(Locale.getDefault(), "localMic:%s,localSpeaker:%s,remoteMic:%s,remoteSpeaker:%s", localMic,
                localSpeaker, remoteMic, remoteSpeaker));
        //local:false远程  true本地
        BaseApplication.getAppComponent().getCmd().setAudio(false, remoteMic, remoteSpeaker);
//        if (localSpeaker) {
        MediaRecorder mRecorder = null;
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.release();
            BaseApplication.getAppComponent().getCmd().setAudio(true, remoteMic, localSpeaker);
            if (isEarpiecePlug()) {
                Observable.just("webRtcJava层的设置影响了耳机")
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(ret -> switchEarpiece(true), AppLogger::e);
            }
            return true;
        } catch (Exception e) {
            AppLogger.d(e.getMessage());
            if (mRecorder != null) {
                mRecorder.release();
            }
            if (!localMic && !localSpeaker && !remoteMic && !remoteSpeaker) {
            } else {
                AndroidSchedulers.mainThread().createWorker().schedule(() -> {
                    if (mView != null && mView.isUserVisible()) mView.audioRecordPermissionDenied();
                });
            }
            return false;
//            }
        }
    }

    /**
     * localMic 0-->1{全部打开}
     * localMic 1-->0{}
     * 查看 doc/mic_speaker设置.md
     *
     * @return
     */
    @Override
    public Observable<Boolean> switchMic() {
        return Observable.just(true)
                .subscribeOn(Schedulers.newThread())
                .flatMap(ret -> {
                    //当前状态
                    boolean localMic = getView().isLocalMicOn();
                    boolean localSpeaker = getView().isLocalSpeakerOn();
                    boolean remoteSpeaker = false;//imageview 图标状态已经更新了。
                    boolean remoteMic = false;
                    if (!localMic) {//打开mic,全部打开
                        localMic = true;
                        localSpeaker = true;
                        remoteMic = true;
                        remoteSpeaker = true;
                    } else {//关闭mic,只需关闭远程speaker,和本地mic
                        remoteMic = localSpeaker;
                        localMic = false;
                    }
                    boolean result = setupAudio(localMic, localSpeaker, remoteMic, remoteSpeaker);
                    return Observable.just(result);
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
        unSubscribe(liveSubscription);
    }

    //forPopWindow true:手动截图:弹窗,保存每日精彩
    //forPopWindow false:直播断开,退出界面.
    @Override
    public void takeSnapShot(boolean forPopWindow) {
        AppLogger.d("take shot initSubscription");
        int w = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoWidth;
        int h = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoHeight;
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(o -> {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    PerformanceUtils.startTrace("takeCapture");
                    byte[] data = BaseApplication.getAppComponent().getCmd().screenshot(false);
                    if (data == null) {
                        if (forPopWindow) getView().onTakeSnapShot(null);//弹窗
                        AppLogger.e("截图失败,data为空");
                        return null;
                    }
                    Bitmap bitmap = JfgUtils.byte2bitmap(w, h, data);
                    if (forPopWindow) getView().onTakeSnapShot(bitmap);//弹窗
                    data = null;
                    PerformanceUtils.stopTrace("takeCapture");
                    String filePath;
                    if (forPopWindow) {
                        filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                    } else {
                        filePath = JConstant.MEDIA_PATH + File.separator + "." + uuid + System.currentTimeMillis();
                        removeLastPreview();
                        SimpleCache.getInstance().addCache(filePath, bitmap);
                        PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, filePath);
                        //需要删除之前的一条记录.
                    }
                    BitmapUtils.saveBitmap2file(bitmap, filePath);
                    if (forPopWindow)//添加到相册
                        MediaScannerConnection.scanFile(ContextUtils.getContext(), new String[]{filePath}, null, null);
                    return bitmap;
                })
                .observeOn(Schedulers.io())
                .filter(bitmap -> bitmap != null)
                .subscribe(bitmap -> {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    if (!forPopWindow) {//预览图,和弹窗是互斥的.
                        //因为同一个url,在glide上，不会更新bitmap，等待解决，用一个token来维持
                        getView().onPreviewResourceReady(bitmap);
                    }
                    new SaveAndShare(uuid, bitmap, forPopWindow).start();
                }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()), () -> AppLogger.d("take screen finish"));
    }

    private void removeLastPreview() {
        final String pre = PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid);
        if (TextUtils.isEmpty(pre)) return;
        try {
            if (SimpleCache.getInstance().getPreviewKeyList() != null) {
                List<String> list = new ArrayList<>(SimpleCache.getInstance().getPreviewKeyList());
                for (String key : list) {
                    if (!TextUtils.isEmpty(key) && key.contains(uuid)) {
                        SimpleCache.getInstance().removeCache(key);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "" + e);
        }
        Observable.just("go")
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> FileUtils.deleteFile(pre), AppLogger::e);
    }

    @Override
    public void saveAlarmFlag(boolean flag) {
        Log.d("saveAlarmFlag", "saveAlarmFlag: " + flag);
    }

    @Override
    public ArrayList<Long> getFlattenDateList() {
        return BaseApplication.getAppComponent().getSourceManager().getHisDateList(uuid);
    }

    @Override
    public IData getHistoryDataProvider() {
        return historyDataProvider;
    }

    @Override
    public boolean needShowHistoryWheelView() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPNet net = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        DpMsgDefine.DPSdStatus sdStatus = device.$(DpMsgMap.ID_204_SDCARD_STORAGE, new DpMsgDefine.DPSdStatus());
        boolean show = JFGRules.isDeviceOnline(net)
                && NetUtils.getJfgNetType(getView().getContext()) != 0
                && TextUtils.isEmpty(device.shareAccount)
                && sdStatus.hasSdcard && sdStatus.err == 0
                && historyDataProvider != null && historyDataProvider.getDataCount() > 0;
        AppLogger.i("show: " + show);
        return show;
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }


    @Override
    protected Subscription[] register() {
        return new Subscription[]{robotDataSync(), checkNewHardWareBack()};
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private Subscription robotDataSync() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter((RxEvent.DeviceSyncRsp jfgRobotSyncData) -> (
                        jfgRobotSyncData.dpList != null &&
                                getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .flatMap(deviceSyncRsp -> {
                    AppLogger.d("update dp List: " + ListUtils.getSize(deviceSyncRsp.dpList));
                    return Observable.from(deviceSyncRsp.dpList);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(msg -> {
                    try {
                        getView().onDeviceInfoChanged(msg);
                        if (msg.id == 222) {
                            DpMsgDefine.DPSdcardSummary sdStatus = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
                            if (sdStatus == null) sdStatus = new DpMsgDefine.DPSdcardSummary();
                            if (!sdStatus.hasSdcard || sdStatus.errCode != 0)
                                updatePrePlayType(TYPE_LIVE, 0, -1);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .retry(new RxHelper.RxException<>("robotDataSync"))
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .subscribe(ret -> {
                }, throwable -> AppLogger.e(MiscUtils.getErr(throwable)));
    }

    @Override
    public void onFrameFailed() {
        AppLogger.e("is bad net work");
        //暂停播放
        stopPlayVideo(JFGRules.PlayErr.ERR_LOW_FRAME_RATE).subscribe(ret -> {
        }, AppLogger::e);

    }

    @Override
    public void onFrameRate(boolean slow) {
        AppLogger.e("is bad net work show loading?" + slow);
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
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION,
                NETWORK_STATE_CHANGED_ACTION};
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        String action = intent.getAction();
        if (mView == null) return;
        if (networkAction == null) networkAction = new NetworkAction(this);
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)
                || TextUtils.equals(action, NETWORK_STATE_CHANGED_ACTION)) {
            networkAction.run();
        }
    }

    private NetworkAction networkAction;

    private static class NetworkAction {
        private int preState = 0;
        private WeakReference<CamLivePresenterImpl> presenterWeakReference;

        public NetworkAction(CamLivePresenterImpl camLivePresenter) {
            preState = NetUtils.getJfgNetType();
            this.presenterWeakReference = new WeakReference<>(camLivePresenter);
        }

        public void run() {
            if (presenterWeakReference != null && presenterWeakReference.get() != null) {
                Observable.just("")
                        .subscribeOn(Schedulers.newThread())
                        .filter(ret -> presenterWeakReference.get().mView != null)
                        .subscribe(ret -> {
                            int net = NetUtils.getJfgNetType();
                            if (preState == net) return;
                            preState = net;
                            if (net == 0) {
                                AppLogger.i("网络中断");
//                                presenterWeakReference.get().stopPlayVideo(ERR_NETWORK).subscribe(r -> {
//                                }, AppLogger::e);
                                presenterWeakReference.get().mView.onNetworkChanged(false);
                            } else {
                                presenterWeakReference.get().mView.onNetworkChanged(true);
                                AppLogger.d("网络恢复");
                                //此处的reason 不需要填 ERR_NETWORK,因为下一步需要恢复播放loading
//                                presenterWeakReference.get().startPlay();
//                                presenterWeakReference.get().stopPlayVideo(PLAY_STATE_NET_CHANGED)
//                                        .subscribeOn(Schedulers.newThread())
//                                        .subscribe(result -> {
//                                            presenterWeakReference.get().startPlay();
//                                        }, AppLogger::e);
                            }
                        }, AppLogger::e);
            }
        }
    }

    /**
     * 每天检测一次是否有新固件
     */
    @Override
    public void checkNewHardWare() {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    if (TimeUtils.isToday(PreferencesUtils.getLong(JConstant.CHECK_HARDWARE_TIME, 0))) {
                        return;
                    }
                    Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                    try {
                        String version = device.$(DpMsgMap.ID_207_DEVICE_VERSION, "0");
                        BaseApplication.getAppComponent().getCmd().checkDevVersion(device.pid, uuid, version);
                    } catch (Exception e) {
                        AppLogger.e("checkNewHardWare:" + e.getLocalizedMessage());
                    }
                }, throwable -> AppLogger.e(MiscUtils.getErr(throwable)));
    }

    @Override
    public Subscription checkNewHardWareBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckDevVersionRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.CheckDevVersionRsp checkDevVersionRsp) -> {
                    if (checkDevVersionRsp != null && checkDevVersionRsp.hasNew) {
                        getView().hardwareResult(checkDevVersionRsp);
                        PreferencesUtils.putLong(JConstant.CHECK_HARDWARE_TIME, System.currentTimeMillis());
                    }
                }, throwable -> AppLogger.e(MiscUtils.getErr(throwable)));
    }

    private static class SaveAndShare extends Thread {
        private String uuid;
        private Bitmap bitmap;
        private boolean needShare;

        public SaveAndShare(String uuid, Bitmap bitmap, boolean needShare) {
            this.uuid = uuid;
            this.bitmap = bitmap;
            this.needShare = needShare;
        }

        @Override
        public void run() {
            shareSnapshot(this.needShare, this.bitmap);
        }

        private String getThumbnailKey() {
            return PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid);
        }

        /**
         * 保存和分享,这是一个后台任务,用一个静态类,避免持有引用
         *
         * @param needShare
         * @param bitmap
         */
        private void shareSnapshot(boolean needShare, Bitmap bitmap) {
            Observable.just(bitmap)
                    .subscribeOn(Schedulers.io())
                    .map(result -> {
                        String filePath = needShare ? getThumbnailKey() : JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".jpg";
                        BitmapUtils.saveBitmap2file(result, filePath);
                        return filePath;
                    })
                    .filter(path -> {
                        AppLogger.d("to collect bitmap is null? " + (TextUtils.isEmpty(path)));
                        return path != null && needShare;
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(filePath -> {
                        long time = System.currentTimeMillis();
                        AppLogger.d("save bitmap to disk performance:" + (System.currentTimeMillis() - time));
                        DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
                        item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
                        item.cid = uuid;
                        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                        item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
                        item.fileName = time / 1000 + ".jpg";
                        item.time = (int) (time / 1000);
                        IDPEntity entity = new DPEntity()
                                .setUuid(uuid)
                                .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                                .setVersion(System.currentTimeMillis())
                                .setAccount(BaseApplication.getAppComponent().getSourceManager().getAccount().getAccount())
                                .setAction(DBAction.SHARED)
                                .setOption(new DBOption.SingleSharedOption(1, 1, filePath))
                                .setBytes(item.toBytes());
                        BaseApplication.getAppComponent().getTaskDispatcher().perform(entity)
                                .subscribeOn(Schedulers.io())
                                .subscribe(ret -> {
                                }, AppLogger::e);
                        AppLogger.d("take shot step collect ");
                    }, throwable -> AppLogger.e("shareSnapshot:" + throwable.getLocalizedMessage()));
        }
    }
}
