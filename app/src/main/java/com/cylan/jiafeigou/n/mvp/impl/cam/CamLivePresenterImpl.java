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
import com.cylan.jiafeigou.base.module.Base;
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
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MD5Util;
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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.ERR_NERWORK;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.STOP_MAUNALLY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_LIVE;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractPresenter<CamLiveContract.View>
        implements CamLiveContract.Presenter, IFeedRtcp.MonitorListener {
    private int playType = TYPE_LIVE;
    private int playState = PLAY_STATE_IDLE;
    private IData historyDataProvider;
    private int stopReason = STOP_MAUNALLY;//手动断开
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
                    }
                    return notNull;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .takeFirst(disconnect -> {
                    playState = PLAY_STATE_IDLE;
                    getView().onLiveStop(playType, disconnect.code);
                    reset();
                    AppLogger.d("reset subscription");
                    return true;
                }).subscribe(ret -> {
                }, AppLogger::e);
    }

    @Override
    public String getThumbnailKey() {
        return JConstant.MEDIA_PATH + File.separator + "." + MD5Util.lowerCaseMD5(uuid);
    }

    @Override
    public int getPlayState() {
        return playState;
    }

    @Override
    public int getPlayType() {
        return playType;
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
            prePlayType = new CamLiveContract.PrePlayType();
            prePlayType.type = TYPE_LIVE;
        }
        Log.d("updatePrePlayType", "getPrePlayType:" + prePlayType.time);
        return this.prePlayType;
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

    @Override
    public void setStopReason(int stopReason) {
        this.stopReason = stopReason;
    }

    private void reset() {
        feedRtcp.stop();
        unSubscribe(liveSubscription);
        liveSubscription = new MapSubscription();
    }

    @Override
    public void startPlayLive() {
        getView().onLivePrepare(TYPE_LIVE);
        playState = PLAY_STATE_PREPARE;
        playType = TYPE_LIVE;
        reset();
        //加入管理,如果播放失败,收到disconnect
        liveSubscription.add(videoDisconnectSub(), "videoDisconnectSub");
        liveSubscription.add(prePlay(s -> {
            try {
                int ret = BaseApplication.getAppComponent().getCmd().playVideo(uuid);
                if (ret != 0) {
                    BaseApplication.getAppComponent().getCmd().stopPlay(uuid);
                    ret = BaseApplication.getAppComponent().getCmd().playVideo(uuid);
                    AppLogger.i("play video: " + uuid + " " + ret);
                }
            } catch (JfgException e) {
                e.printStackTrace();
            }
            return null;
        }).zipWith(getInterestingOne().timeout(30, TimeUnit.SECONDS, Observable.just("timeout")
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
                    AppLogger.d("option: " + result);
                    return TextUtils.equals(result, "JFGMsgVideoResolution");
                }), (String s, Object o) -> {
            AppLogger.i("initSubscription to receive rtcp");
            //开始接收rtcp
            liveSubscription.add(rtcpNotifySub()
                    .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                    .subscribe(ret -> {
                    }, AppLogger::e), "rtcpNotifySub");
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
    private Observable<Object> rtcpNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .filter((JFGMsgVideoRtcp rtcp) -> (getView() != null))
                .onBackpressureBuffer()//防止MissingBackpressureException
                .timeout(30, TimeUnit.SECONDS, Observable.just("no rtcp call back")
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
                    updatePrePlayType(playType, rtcp.timestamp);
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
                .subscribeOn(Schedulers.newThread())
                .mergeWith(RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                        .filter(resolution -> TextUtils.equals(resolution.peer, uuid))
                        .observeOn(Schedulers.newThread())
                        .map(resolution -> {
                            setupAudio(false, false, false, false);
                            PreferencesUtils.putFloat(JConstant.KEY_UUID_RESOLUTION + uuid, (float) resolution.height / resolution.width);
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
                            playState = PLAY_STATE_PLAYING;
                            getView().onLiveStarted(playType);
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

    private void updatePrePlayType(int type, long time) {
        if (prePlayType == null) prePlayType = new CamLiveContract.PrePlayType();
        prePlayType.type = type;
        prePlayType.time = time;
        Log.d("updatePrePlayType", "updatePrePlayType:" + time);
    }

    @Override
    public void startPlayHistory(long time) {
        getView().onLivePrepare(CamLiveContract.TYPE_HISTORY);
        playType = CamLiveContract.TYPE_HISTORY;
        playState = PLAY_STATE_PREPARE;
        updatePrePlayType(playType, time);
        reset();
        //加入管理,如果播放失败,收到disconnect
        liveSubscription.add(videoDisconnectSub(), "videoDisconnectSub");
        liveSubscription.add(prePlay(s -> {
            try {
                //先停止播放{历史录像,直播都需要停止播放}
                if (playState != PLAY_STATE_IDLE) {
                    BaseApplication.getAppComponent().getCmd().stopPlay(uuid);
                    AppLogger.i("stop play history");
                }
                int ret = BaseApplication.getAppComponent().getCmd().playHistoryVideo(uuid, time / 1000L);
                if (ret != 0) {
                    BaseApplication.getAppComponent().getCmd().stopPlay(uuid);
                    ret = BaseApplication.getAppComponent().getCmd().playHistoryVideo(uuid, time / 1000L);
                }
                AppLogger.i(String.format("play history video:%s,%s ", uuid, time / 1000L) + " " + ret);
            } catch (JfgException e) {
                AppLogger.e("err:" + e.getLocalizedMessage());
            }
            return null;
        }).zipWith(getInterestingOne().timeout(30, TimeUnit.SECONDS, Observable.just("timeout")
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
                    AppLogger.d("option: " + result);
                    return TextUtils.equals(result, "JFGMsgVideoResolution");
                }), (String s, Object o) -> {
            AppLogger.i("initSubscription to receive rtcp");
            //开始接收rtcp
            liveSubscription.add(rtcpNotifySub()
                    .doOnError(throwable -> AppLogger.e("err:" + throwable.getLocalizedMessage()))
                    .subscribe(ret -> {
                    }, AppLogger::e), "rtcpNotifySub");
            return null;
        }).subscribe(objectObservable -> AppLogger.e("flow done"),
                throwable -> AppLogger.e("flow done: " + throwable.getLocalizedMessage())), "prePlay");
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
                        if (playState == PLAY_STATE_PLAYING) {
                            setupAudio(false, false, false, false);
                        }
                        BaseApplication.getAppComponent().getCmd().stopPlay(s);
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
                    AppLogger.d("live stop: " + stopReason);
                    if (getView() != null)
                        getView().onLiveStop(playType, stopReason);
                })
                .doOnError(throwable -> AppLogger.e("" + throwable.getLocalizedMessage()))
                .subscribe(ret -> {
                }, AppLogger::e);
    }

    @Override
    public void stopPlayVideo(boolean detach) {
        if (detach) {

        } else stopPlayVideo(getPlayType());
    }

    @Override
    public String getUuid() {
        return uuid;
    }

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
                    } else {
                        remoteMic = false;
                        remoteSpeaker = false;
                        localSpeaker = true;
                        localMic = false;
                    }
                    boolean result = setupAudio(localMic, localSpeaker, remoteMic, remoteSpeaker);
                    return Observable.just(result);
                });
    }

    private boolean setupAudio(boolean localMic, boolean localSpeaker, boolean remoteMic, boolean remoteSpeaker) {
        AppLogger.d(String.format(Locale.getDefault(), "localMic:%s,localSpeaker:%s,remoteMic:%s,remoteSpeaker:%s", localMic,
                localSpeaker, remoteMic, remoteSpeaker));
        BaseApplication.getAppComponent().getCmd().setAudio(false, remoteSpeaker, remoteMic);
        if (localSpeaker) {
            MediaRecorder mRecorder = null;
            try {
                mRecorder = new MediaRecorder();
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mRecorder.release();
                BaseApplication.getAppComponent().getCmd().setAudio(true, true, localMic);
                return true;
            } catch (Exception e) {
                AppLogger.d(e.getMessage());
                if (mRecorder != null) {
                    mRecorder.release();
                }
                AndroidSchedulers.mainThread().createWorker().schedule(() -> {
                            if (mView != null) mView.audioRecordPermissionDenied();
                        }
                );
                return false;
            }
        }
        AppLogger.i(String.format(Locale.getDefault(), "localMic:%s,LocalSpeaker:%s,remoteMic:%s,remoteSpeaker:%s", localMic, localSpeaker, remoteMic, remoteSpeaker));
        return true;
    }

    @Override
    public Observable<Boolean> switchMic() {
        return Observable.just(true)
                .subscribeOn(Schedulers.newThread())
                .flatMap(ret -> {
                    //当前状态
                    boolean localMic = getView().isLocalMicOn();
                    boolean localSpeaker = getView().isLocalSpeakerOn();
                    boolean remoteMic = localSpeaker;
                    boolean remoteSpeaker = localMic;//imageview 图标状态已经更新了。
                    if (!localMic) {//打开mic,全部打开
                        localMic = true;
                        localSpeaker = true;
                        remoteMic = true;
                        remoteSpeaker = true;
                    } else {//关闭mic,只需关闭远程speaker,和本地mic
                        remoteSpeaker = false;
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

    //截图:预览,弹窗,保存每日精彩
    @Override
    public void takeSnapShot(boolean forPreview) {
        AppLogger.d("take shot initSubscription");
        int w = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoWidth;
        int h = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoHeight;
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(o -> {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    PerformanceUtils.startTrace("takeCapture");
                    byte[] data = BaseApplication.getAppComponent().getCmd().screenshot(false);
                    if (data == null) {
                        if (!forPreview) getView().onTakeSnapShot(null);//弹窗
                        return null;
                    }
                    Bitmap bitmap = JfgUtils.byte2bitmap(w, h, data);
                    data = null;
                    PerformanceUtils.stopTrace("takeCapture");
                    String filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                    BitmapUtils.saveBitmap2file(bitmap, filePath);
                    MediaScannerConnection.scanFile(mView.getContext(), new String[]{filePath}, null, null);
                    if (!forPreview) getView().onTakeSnapShot(bitmap);//弹窗
                    return bitmap;
                })
                .observeOn(Schedulers.io())
                .filter(bitmap -> bitmap != null)
                .subscribe(bitmap -> {
                    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                    if (forPreview) {//预览图,和弹窗是互斥的.
                        //因为同一个url,在glide上，不会更新bitmap，等待解决，用一个token来维持
                        PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, System.currentTimeMillis() + "");
                        getView().onPreviewResourceReady(bitmap);
                    }
                    new SaveAndShare(uuid, bitmap, !forPreview).start();
                }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()), () -> AppLogger.d("take screen finish"));
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
                            if (!sdStatus.hasSdcard) updatePrePlayType(TYPE_LIVE, 0);
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
        if (mView == null) return;
        if (networkAction == null) networkAction = new NetworkAction(this);
        if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
            int type = NetUtils.getJfgNetType();
            if (type == 0) {
                networkAction.run();
            } else {
                networkAction.run();
                AppLogger.e("还需要恢复播放");
            }
        }
    }

    private NetworkAction networkAction;

    private static class NetworkAction {
        private WeakReference<CamLivePresenterImpl> presenterWeakReference;

        public NetworkAction(CamLivePresenterImpl camLivePresenter) {
            this.presenterWeakReference = new WeakReference<>(camLivePresenter);
        }

        public void run() {
            if (presenterWeakReference != null && presenterWeakReference.get() != null) {
                Observable.just("")
                        .subscribeOn(Schedulers.newThread())
                        .filter(ret -> presenterWeakReference.get().mView != null)
                        .subscribe(ret -> {
                            int net = NetUtils.getJfgNetType();
                            if (net == 0) {
                                int playType = presenterWeakReference.get().getPlayType();
                                AppLogger.i("there is no network ");
                                presenterWeakReference.get().setStopReason(ERR_NERWORK);
                                presenterWeakReference.get().stopPlayVideo(playType);
                                presenterWeakReference.get().mView.onNetworkChanged(false);
                            } else presenterWeakReference.get().mView.onNetworkChanged(true);
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
            return JConstant.MEDIA_PATH + File.separator + "." + MD5Util.lowerCaseMD5(uuid);
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
