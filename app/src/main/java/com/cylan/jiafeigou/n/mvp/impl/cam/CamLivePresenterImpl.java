package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.cylan.entity.jniCall.JFGHistoryVideoErrorInfo;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.cache.SimpleCache;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.live.IFeedRtcp;
import com.cylan.jiafeigou.misc.live.LiveFrameRateMonitor;
import com.cylan.jiafeigou.misc.ver.AbstractVersion;
import com.cylan.jiafeigou.misc.ver.DeviceVersionChecker;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractFragmentPresenter;
import com.cylan.jiafeigou.push.BellPuller;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BitmapUtils;
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

import permissions.dispatcher.PermissionUtils;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_STOP;
import static com.cylan.jiafeigou.misc.JFGRules.PlayErr.ERR_NETWORK;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_LIVE;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractFragmentPresenter<CamLiveContract.View>
        implements CamLiveContract.Presenter, IFeedRtcp.MonitorListener {

    private IData historyDataProvider;
    /**
     * 只有从Idle->playing,err->playing才会设置.
     */
    private int resolutionH, resolutionW;
    /**
     * 保存当前播放的方式,eg:从播放历史视频切换到设置页面,回来之后,需要继续播放历史视频.
     */
    private CamLiveContract.LiveStream liveStream;
    /**
     * 帧率记录
     */
    private IFeedRtcp feedRtcp = new LiveFrameRateMonitor();
    private volatile boolean ignoreTimeStamp;

    public CamLivePresenterImpl(CamLiveContract.View view) {
        super(view);
        view.setPresenter(this);
        feedRtcp.setMonitorListener(this);
        if (historyDataProvider != null) historyDataProvider.clean();
        //清了吧.不需要缓存.
        History.getHistory().clearHistoryFile(uuid);
    }

    @Override
    public void start() {
        super.start();
        BaseApplication.getAppComponent().getSourceManager()
                .syncAllProperty(uuid, 204, 222);
    }

    @Override
    public void pause() {
        super.pause();
        //历史视频播放的时候，如果来了门铃。然而等待onStop，unSubscribe就非常晚。会导致这里的
        //rtcp还会继续接受数据。继续更新时间轴。
        removeSubscription("RTCPNotifySub");
    }

    private Subscription getDeviceUnBindSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeviceUnBindedEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(event -> TextUtils.equals(event.uuid, uuid))
                .subscribe(event -> {
                    if (mView != null) {
                        mView.onDeviceUnBind();
                    }
                }, e -> AppLogger.d(e.getMessage()));
    }

    /**
     * 1.需要获取设备电量并且,一天一次提醒弹窗.
     * 2.
     *
     * @return
     */
    private Subscription getBatterySub() {
        //按照
        if (JFGRules.popPowerDrainOut(getDevice().pid)) {
            Observable.just("getBatterySub")
                    .subscribeOn(Schedulers.io())
                    .filter(ret -> NetUtils.getJfgNetType() > 0)//在线
                    .filter(ret -> !JFGRules.isShareDevice(uuid))//非分享
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        Device device = getDevice();
                        Integer battery = device.$(DpMsgMap.ID_206_BATTERY, 0);
                        DpMsgDefine.DPNet net = device.$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
                        if (battery <= 20 && net.net > 0) {//电量低
                            DBOption.DeviceOption option = device.option(DBOption.DeviceOption.class);
                            if (option != null && option.lastLowBatteryTime < TimeUtils.getTodayStartTime()) {//新的一天
                                option.lastLowBatteryTime = System.currentTimeMillis();
                                device.setOption(option);
                                BaseApplication.getAppComponent().getSourceManager().updateDevice(device);
                                mView.onBatteryDrainOut();
                            }
//                            mView.onBatteryDrainOut();
                        }
                    }, AppLogger::e);
            AppLogger.d("getBatterySub:" + JFGRules.popPowerDrainOutLevel(getDevice().pid));
        }
        return null;
    }

    private Subscription checkNewVersionRsp() {
        Subscription subscription = RxBus.getCacheInstance().toObservable(AbstractVersion.BinVersion.class)
                .filter(ret -> mView != null && mView.isAdded() && TextUtils.equals(ret.getCid(), uuid))
                .retry()
                .subscribe(version -> {
                    version.setLastShowTime(System.currentTimeMillis());
                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(version));
                    mView.showFirmwareDialog();
                }, AppLogger::e);
        AbstractVersion<AbstractVersion.BinVersion> version = new DeviceVersionChecker();
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        version.setPortrait(new AbstractVersion.Portrait().setCid(uuid).setPid(device.pid));
        version.setShowCondition(() -> {
            DpMsgDefine.DPNet dpNet = getDevice().$(201, new DpMsgDefine.DPNet());
            //设备离线就不需要弹出来
            if (!JFGRules.isDeviceOnline(dpNet)) {
                return false;
            }
            //局域网弹出
            if (!MiscUtils.isDeviceInWLAN(uuid)) return false;
            return true;
        });
        version.startCheck();
        return subscription;
    }

    /**
     * 视频断开连接
     * 只需要开始播放后注册
     *
     * @return
     */
    private Subscription videoDisconnectSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoDisconn.class)
                .subscribeOn(Schedulers.io())
                .filter(ret -> mView != null)
                .map(ret -> {
                    AppLogger.i("stop for reason: " + ret.code);
                    stopPlayVideo(ret.code).subscribe(result -> {
                    }, AppLogger::e);
                    return ret;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    BellPuller.getInstance().currentCaller(null);
                    updateLiveStream(getLiveStream().type, -1, PLAY_STATE_IDLE);
                    getView().onLiveStop(getLiveStream().type, ret.code);
//                    feedRtcp.stop();
                    AppLogger.d("reset subscription");
                }, AppLogger::e);
    }

    @Override
    public String getThumbnailKey() {
        return PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid);
    }

    @Override
    public int getPlayState() {
        return getLiveStream().playState;
    }

    @Override
    public int getPlayType() {
        return getLiveStream().type;
    }


    /**
     * 一天一天地查询
     * 可以用来查询数据库
     */

    @Override
    public Observable<IData> assembleTheDay() {
        long timeEnd = Integer.MAX_VALUE;
        AppLogger.d("historyFile:timeEnd?" + 0);
        return BaseApplication.getAppComponent().getDBHelper().loadHistoryFile(uuid, 0, timeEnd)
                .subscribeOn(Schedulers.io())
                .flatMap(historyFiles -> {
                    AppLogger.d("load hisFile List: " + ListUtils.getSize(historyFiles));
                    if (!ListUtils.isEmpty(historyFiles)) {
                        if (historyDataProvider == null)
                            historyDataProvider = DataExt.getInstance();
                        historyDataProvider.flattenData(new ArrayList<>(historyFiles), JFGRules.getDeviceTimezone(getDevice()));
                    }
                    //本地没有啊,需要从服务器获取.
                    if (ListUtils.isEmpty(historyFiles))
                        fetchHistoryDataList();
                    return Observable.just(historyDataProvider);
                });
    }

    @Override
    public CamLiveContract.LiveStream getLiveStream() {
        if (liveStream == null) {
            this.liveStream = new CamLiveContract.LiveStream();
        }
        return this.liveStream;
    }

    @Override
    public void updateLiveStream(CamLiveContract.LiveStream prePlayType) {
        this.liveStream = prePlayType;
    }


    @Override
    public float getVideoPortHeightRatio() {
        AppLogger.d("获取分辨率?");
        float cache = PreferencesUtils.getFloat(JConstant.KEY_UUID_RESOLUTION + uuid, 0.0f);
        if (cache == 0.0f) cache = JFGRules.getDefaultPortHeightRatio(getDevice().pid);
        return PreferencesUtils.getFloat(JConstant.KEY_UUID_RESOLUTION + uuid, cache);
    }

    @Override
    public void saveHotSeatState() {
        getHotSeatStateMaintainer().saveRestore();
    }

    @Override
    public void restoreHotSeatState() {
        getHotSeatStateMaintainer().restore();
    }

    public void drawTheDay() {
        if (historyDataProvider == null) {
            historyDataProvider = DataExt.getInstance();
        }
        Subscription subscription = assembleTheDay()
                .subscribeOn(Schedulers.io())
                .delay(1, TimeUnit.SECONDS)
                .filter(ret -> ret != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    mView.onHistoryDataRsp(historyDataProvider);
                    AppLogger.d("历史录像wheel准备好,断开直播");
                    //放在这里不好,有 bug 单:#116924
                    //Android（1.0.0.490）没获取到历史视频时。从消息界面，点击查看视频（11:00历史视频）；跳转至直播界面播放历史视频，
                    // 不是消息界面时间上的历史视频，而是历史视频时间上最早一天的历史视频

//                    startPlayHistory(historyDataProvider.getFlattenMinTime());
                }, AppLogger::e);
        addSubscription(subscription, "hisFlat");
    }

    @Override
    public boolean fetchHistoryDataList() {
//        test();
        if (getLiveStream().playState == PLAY_STATE_PLAYING || getLiveStream().playState == PLAY_STATE_PREPARE) {
            stopPlayVideo(PLAY_STATE_STOP).subscribe(ret -> {
            }, AppLogger::e);
            AppLogger.d("获取历史录像,先断开直播,或者历史录像");
        }
        if (ListUtils.isEmpty(History.getHistory().getDateList(uuid))) {
            if (historyDataProvider != null) historyDataProvider.clean();
        }
        if (historyDataProvider != null && historyDataProvider.getDataCount() > 0) {
            AppLogger.d("有历史录像了.");
            RxBus.getCacheInstance().post(new RxEvent.HistoryBack(false));
            return false;
        }
        Subscription subscription = BaseApplication.getAppComponent().getSourceManager().queryHistory(uuid)
                .subscribeOn(Schedulers.io())
                .filter(ret -> {
                    AppLogger.d("get history?" + ret);
                    return ret;
                })
                .flatMap(integer -> RxBus.getCacheInstance().toObservable(RxEvent.JFGHistoryVideoParseRsp.class)
                        .filter(rsp -> TextUtils.equals(rsp.uuid, uuid))
                        .filter(rsp -> ListUtils.getSize(rsp.historyFiles) > 0)//>0
                        .timeout(30, TimeUnit.SECONDS)
                        .flatMap(rsp -> makeTimeDelayForList()))
                .delay(200, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    mView.onHistoryLoadFinished();
                    RxBus.getCacheInstance().post(new RxEvent.HistoryBack(false));
                }, AppLogger::e);
        removeSubscription("getHistoryList");
        addSubscription(subscription, "getHistoryList");
        return true;
    }

    /**
     * 对于大内存的设备,历史录像是好几条逐条rsp.但是不知道什么时候结束,不知道有几条.
     * <p>
     * 只需要初始化一天的就可以啦.丢throwable就是为了让订阅链断开
     */
    private Observable<Boolean> makeTimeDelayForList() {
        drawTheDay();
        return Observable.just("")
                .subscribeOn(Schedulers.io())
                .flatMap(list -> {
                    //更新日历
                    ArrayList<Long> dateList = History.getHistory().getDateList(uuid);
                    AppLogger.d("历史录像日历更新,天数: " + ListUtils.getSize(dateList));
                    return Observable.just(true);
                });
    }

    @Override
    public boolean isShareDevice() {
        return JFGRules.isShareDevice(uuid);
    }

    @Override
    public boolean isDeviceStandby() {
        DpMsgDefine.DPStandby standby = getDevice().$(508, new DpMsgDefine.DPStandby());
        return standby.standby;
    }

    @Override
    public void startPlay() {
        if (mView == null || !mView.isUserVisible()) return;
        if (getLiveStream().playState == PLAY_STATE_PREPARE) {
            AppLogger.d("已经loading");
            mView.onLivePrepare(getLiveStream().type);
            return;
        }
        mView.onLivePrepare(getLiveStream().type);
        boolean sdkOnlineStatus = BaseApplication.getAppComponent().getSourceManager().isOnline();
        if (!sdkOnlineStatus) {
            String routeMac = NetUtils.getRouterMacAddress();
            String deviceMac = getDevice().$(202, "");
            boolean AP = !TextUtils.isEmpty(routeMac) && TextUtils.equals(deviceMac, routeMac);
            AppLogger.d("直连Ap?" + AP);
        }
        addSubscription(beforePlayObservable(s -> {
            try {
                int ret;
                boolean switchInterface = false;
                // 历史播放中，需要停止,不能保证上次是播放的是历史还是直播，所以直接断开。
                if (getLiveStream().playState == PLAY_STATE_PLAYING) {
                    BaseApplication.getAppComponent().getCmd().stopPlay(uuid);  // 先停止播放
                    switchInterface = true;
                }
                // TODO: 2017/9/2 记录开始播放时间,在开始播放的最初几秒内禁止 Rtcp回调
                getLiveStream().playStartTime = System.currentTimeMillis() / 1000;
//                getLiveStream().playStartTime = System.currentTimeMillis();
                if (getDevice().getPid() == 42) {
                    String json = " {\n" +
                            "    \"audio\": {\n" +
                            "      \"codec\": \"g729\"\n" +
                            "    }\n" +
                            "  }";
                    ret = BaseApplication.getAppComponent().getCmd().playVideo(uuid, json);
                } else {
                    ret = BaseApplication.getAppComponent().getCmd().playVideo(uuid);
                }

                AppLogger.d("play video ret :" + ret + "," + switchInterface);

                // TODO: 2017/7/12 判断当前是否需要拦截呼叫事件 针对所有的门铃产品
//                if (JFGRules.isBell(getDevice().pid)) {
                BellPuller.getInstance().currentCaller(uuid);//查看直播时禁止呼叫
//                }
                getHotSeatStateMaintainer().saveRestore();
                AppLogger.i("play video: " + uuid + " " + ret);
            } catch (JfgException e) {
                e.printStackTrace();
            }
            updateLiveStream(getLiveStream().type, -1, PLAY_STATE_PREPARE);
            getView().onLivePrepare(TYPE_LIVE);
            return null;
        }).subscribe(objectObservable -> {
            AppLogger.d("播放流程走通 done,不在这个环节获取历史视频");
        }, AppLogger::e), "beforePlayObservable");
    }

    /**
     * 错误码
     */
    private Subscription errCodeSub() {
        return RxBus.getCacheInstance().toObservable(JFGHistoryVideoErrorInfo.class)
                .subscribeOn(Schedulers.io())
                .filter(ret -> ret != null && ret.code != 0)
                .subscribe(result -> {
                    stopPlayVideo(result.code).subscribe(what -> {
                    }, AppLogger::e);
                    removeTimeoutSub();
                }, AppLogger::e);
    }


    /**
     * 一旦有rtcp消息回来,就表明直播通了,需要通知更新UI.
     * 这个逻辑只需要第一个消息,然后就断开
     *
     * @return
     */
    private Subscription getFirstRTCPNotification() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .filter((JFGMsgVideoRtcp rtcp) -> (getView() != null) && rtcp.frameRate > 0)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    throw new RxEvent.HelperBreaker("Sweat");
                }, throwable -> {
                    if (throwable instanceof RxEvent.HelperBreaker) {
                        AppLogger.d("收到RTCP通知:" + resolutionH);
                        //需要收到发送一个Resolution
                        getHotSeatStateMaintainer().restore();
                        if (resolutionH != 0 && resolutionW != 0) {
                            //因为直播转历史录像,不会再有JFGMsgVideoResolution回调.
                            JFGMsgVideoResolution resolution = new JFGMsgVideoResolution();
                            resolution.peer = uuid;
                            resolution.height = resolutionH;
                            resolution.width = resolutionW;
                            RxBus.getCacheInstance().post(resolution);
                        }
                    }
                });
    }

    /**
     * Rtcp和resolution的回调,
     * 只有resolution回调之后,才能设置{@link JfgAppCmd#enableRenderLocalView(boolean, View)} (View)}
     * 正常播放回调
     * 10s没有视频,直接断开
     *
     * @return
     */
    private Subscription RTCPNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .subscribeOn(Schedulers.io())
                //断网就不要feed.
                .filter(rtcp -> getView() != null && NetUtils.getJfgNetType() != 0)
                .map(rtcp -> {
                    removeTimeoutSub();
                    feedRtcp.feed(rtcp);
                    updateLiveStream(getLiveStream().type, rtcp.timestamp, PLAY_STATE_PLAYING);
                    return rtcp;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rtcp -> {
                    try {
                        // TODO: 2017/9/2 startPlay 后的三秒内禁止更新时间,只允许更新流量 ,避免时间轴跳动,只针对于历史录像
                        if (rtcp.timestamp - getLiveStream().playStartTime < 30) {// TODO: 2017/9/4 rtcp 回调的时间是秒
                            ignoreTimeStamp = false;
                        }
                        getView().onRtcp(rtcp, ignoreTimeStamp);
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, AppLogger::e);
    }

    private Boolean isDampOver() {
        return null;
    }

    private Subscription timeoutSub() {
        return Observable.just("timeout")
                .subscribeOn(Schedulers.newThread())
                .delay(30, TimeUnit.SECONDS)
                .doOnError(ret -> AppLogger.e("30s 超时了"))
                .subscribe(ret -> {
                    //需要发送超时
                    stopPlayVideo(JFGRules.PlayErr.ERR_NOT_FLOW).subscribe(what -> {
                    }, AppLogger::e);
                    BellPuller.getInstance().currentCaller(null);
                }, AppLogger::e);
    }

    /**
     * 只有新建立的播放,才有这个回调.
     *
     * @return
     */
    private Subscription resolutionSub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoResolution.class)
                .timeout(30, TimeUnit.SECONDS)
                .filter(resolution -> TextUtils.equals(resolution.peer, uuid))
                .observeOn(Schedulers.io())
                .map(resolution -> {
                    addSubscription(RTCPNotifySub(), "RTCPNotifySub");
                    removeTimeoutSub();
                    PreferencesUtils.putFloat(JConstant.KEY_UUID_RESOLUTION + uuid, (float) resolution.height / resolution.width);

                    //注册监听耳机
                    registerHeadSetObservable();
                    //正向,抛异常
                    resolutionH = resolution.height;
                    resolutionW = resolution.width;
                    throw new RxEvent.HelperBreaker(resolution);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                }, throwable -> {
                    if (throwable instanceof RxEvent.HelperBreaker) {
                        Object o = ((RxEvent.HelperBreaker) throwable).object;
                        if (o instanceof JFGMsgVideoResolution) {
                            JFGMsgVideoResolution resolution = (JFGMsgVideoResolution) o;
                            AppLogger.i("ResolutionNotifySub: " + new Gson().toJson(resolution) + "," + Thread.currentThread().getName());
                            try {
                                getView().onResolution(resolution);
                                //保存分辨率
                                PreferencesUtils.putFloat(JConstant.KEY_UUID_RESOLUTION + uuid, (float) resolution.height / resolution.width);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                            updateLiveStream(-1, -1, PLAY_STATE_PLAYING);
                            getView().onLiveStarted(getLiveStream().type);
                            getHotSeatStateMaintainer().restore();
                        }
                    }
                });
    }

    /**
     * 1.检查网络
     * 2.开始播放
     *
     * @return
     */
    private Observable<String> beforePlayObservable(Func1<String, String> func1) {
        return Observable.just("")
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(o -> {
                    DpMsgDefine.DPStandby dpStandby = getDevice().$(508, new DpMsgDefine.DPStandby());
                    if (dpStandby.standby) return false;//待机模式
                    if (NetUtils.getJfgNetType() == 0) {
                        //客户端断网了
                        stopPlayVideo(ERR_NETWORK)
                                .observeOn(AndroidSchedulers.mainThread())
                                .filter(ret -> mView != null)
                                .subscribe(ret -> mView.onLiveStop(getLiveStream().type, ERR_NETWORK), AppLogger::e);
                        AppLogger.i("stop play  video for err network");
                        return false;
                    }
                    return true;
                })
                .map(ret -> {
                    //加入管理,如果播放失败,收到disconnect

                    feedRtcp.stop();
                    addSubscription(videoDisconnectSub(), "videoDisconnectSub");
                    addSubscription(errCodeSub(), "errCodeSub");
                    //挪到resolutionSub里面
//                    addSubscription(RTCPNotifySub(), "RTCPNotifySub");
                    addSubscription(resolutionSub(), "resolutionSub");
                    addSubscription(timeoutSub(), "timeoutSub");
                    addSubscription(getFirstRTCPNotification(), "getFirstRTCPNotification");
                    return "";
                })
                .observeOn(Schedulers.io())
                .map(func1);
    }

    private void removeTimeoutSub() {
        removeSubscription("timeoutSub");
    }

    private void updateLiveStream(int type, long time, int state) {
        if (liveStream == null) liveStream = new CamLiveContract.LiveStream();
        if (type != -1)
            liveStream.type = type;
        if (time != -1 && time != 0)
            liveStream.time = time;
        liveStream.playState = state;
        Log.d("updateLiveStream", "updateLiveStream:" + liveStream);
    }
/**
 * 设备是否在线
 */
//    private Observable<Boolean> checkDevice() {
//        return Observable.just(uuid)
//                .subscribeOn(Schedulers.io())
//                .flatMap(new Func1<String, Observable<Boolean>>() {
//                    @Override
//                    public Observable<Boolean> call(String account) {
//                        return null;
//                    }
//                })
//                .filter();
//    }

    /**
     * 网络
     *
     * @return
     */
    private Observable<Boolean> checkNetwork() {
        return Observable.just(uuid)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String s) {
                        DpMsgDefine.DPNet net = getDevice().$(201, new DpMsgDefine.DPNet());
                        if (!JFGRules.isDeviceOnline(net)) {
                            updateLiveStream(TYPE_HISTORY, -1, PLAY_STATE_IDLE);
                            mView.onLiveStop(TYPE_HISTORY, JFGRules.PlayErr.ERR_DEVICE_OFFLINE);
                        }
                        return null;
                    }
                });
    }

    @Override
    public void startPlayHistory(long t) {
        //保证得到s System.currentTimeMillis() / t == 0 的条件范围可能有点小
        if (t == 0) {
            t = 1;
            if (BuildConfig.DEBUG) throw new IllegalArgumentException("怎么会有这种情况发生");
        }
        if (t == getLiveStream().playStartTime) {
            return;//多次调用了,过滤掉即可
        }
        final long time = System.currentTimeMillis() / t > 100 ? t : t / 1000;
        // TODO: 2017/9/2 记录开始播放时间,在开始播放的最初几秒内禁止 Rtcp回调
        ignoreTimeStamp = true;
        getLiveStream().playStartTime = time;
        getView().onLivePrepare(TYPE_HISTORY);
        DpMsgDefine.DPNet net = getDevice().$(201, new DpMsgDefine.DPNet());
        if (!JFGRules.isDeviceOnline(net)) {
            updateLiveStream(TYPE_HISTORY, -1, PLAY_STATE_IDLE);
            mView.onLiveStop(TYPE_HISTORY, JFGRules.PlayErr.ERR_DEVICE_OFFLINE);
            return;
        }
        addSubscription(beforePlayObservable(s -> {
            try {
                int ret;
                getLiveStream().time = time;
                getHotSeatStateMaintainer().saveRestore();
                if (getLiveStream().playState != PLAY_STATE_PLAYING) {
//                    BaseApplication.getAppComponent().getCmd().playVideo(uuid);
//                    AppLogger.i(" stop video .first......");
                }


                ret = BaseApplication.getAppComponent().getCmd().playHistoryVideo(uuid, time);

                //说明现在是在查看历史录像了,泽允许进行门铃呼叫
                BellPuller.getInstance().currentCaller(null);
                updateLiveStream(TYPE_HISTORY, time, PLAY_STATE_PREPARE);
                AppLogger.i("play history video: " + uuid + " time:" + JfgUtils.date2String(JfgUtils.DetailedDateFormat, TimeUtils.wrapToLong(time)) + " ret:" + ret);
            } catch (JfgException e) {
                AppLogger.e("err:" + e.getLocalizedMessage());
            }
            return null;
        }).subscribe(ret -> {
        }, AppLogger::e), "playHistory");
    }

    @Override
    public Observable<Boolean> stopPlayVideo(int reasonOrState) {
        AppLogger.d("pre play state: " + liveStream);
        if (liveStream == null || liveStream.playState == PLAY_STATE_IDLE
                || liveStream.playState == PLAY_STATE_STOP)
            return Observable.just(false);
        if (getLiveStream().playState == PLAY_STATE_PLAYING) {
            //暂停播放了，还需要截图
            takeSnapShot(false);
        }
        resolutionW = resolutionH = 0;
        return Observable.just(uuid)
                .subscribeOn(Schedulers.io())
                .flatMap((String s) -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().stopPlay(s);
                        getHotSeatStateMaintainer().reset();
                        updateLiveStream(getLiveStream().type, -1, reasonOrState);
                        BellPuller.getInstance().currentCaller(null);
                        AppLogger.i("stopPlayVideo:" + s);
                    } catch (JfgException e) {
                        AppLogger.e("stop play err: " + e.getLocalizedMessage());
                    }
                    AppLogger.d("live stop: " + reasonOrState);
                    if (getView() != null)
                        getView().onLiveStop(getLiveStream().type, reasonOrState);
                    return Observable.just(true);
                })
                .doOnError(throwable -> AppLogger.e("stop play err" + throwable.getLocalizedMessage()));
    }

    @Override
    public Observable<Boolean> stopPlayVideo(boolean detach) {
        return stopPlayVideo(PLAY_STATE_STOP);
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
    public void switchSpeaker() {
        getHotSeatStateMaintainer().switchSpeaker();

    }

    public HotSeatStateMaintainer getHotSeatStateMaintainer() {
        if (hotSeatStateMaintainer == null)
            hotSeatStateMaintainer = new HotSeatStateMaintainer(mView, this);
        return hotSeatStateMaintainer;
    }

    /**
     * localMic 0-->1{全部打开}
     * localMic 1-->0{}
     * 查看 doc/mic_speaker设置.md
     *
     * @return
     */
    @Override
    public void switchMic() {
        getHotSeatStateMaintainer().switchMic();
    }

    /**
     * 高清,标清,自动模式切换
     *
     * @param mode
     * @return
     */
    @Override
    public Observable<Boolean> switchStreamMode(int mode) {
        return Observable.just(mode)
                .subscribeOn(Schedulers.io())
                .flatMap(integer -> {
                    DpMsgDefine.DPPrimary<Integer> dpPrimary = new DpMsgDefine.DPPrimary<>(integer);
                    try {
                        AppLogger.e("还需要发送局域网消息");
                        return Observable.just(BaseApplication.getAppComponent().getSourceManager().updateValue(uuid, dpPrimary, 513));
                    } catch (IllegalAccessException e) {
                        return Observable.just(false);
                    }
                });
    }

    //forPopWindow true:手动截图:弹窗,保存每日精彩
    //forPopWindow false:直播断开,退出界面.
    @Override
    public void takeSnapShot(boolean forPopWindow) {
        AppLogger.d("take shot initSubscription");
        Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(new TakeSnapShootLogicHelper(uuid, forPopWindow, mView))
                .observeOn(Schedulers.io())
                .subscribe(pair -> {
                }, AppLogger::e, () -> AppLogger.d("take screen finish"));
    }


    @Override
    public void saveAlarmFlag(boolean flag) {
        Log.d("saveAlarmFlag", "saveAlarmFlag: " + flag);
    }

    @Override
    public void saveAndShareBitmap(Bitmap bitmap) {
        AppLogger.d("take shot initSubscription");
        Observable.just(bitmap)
                .subscribeOn(Schedulers.io())
                .map(new TakeSnapShootHelper(uuid, true, mView))
                .observeOn(Schedulers.io())
                .subscribe(pair -> {
                }, AppLogger::e, () -> AppLogger.d("take screen finish"));
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
        return new Subscription[]{getBatterySub(),
                robotDataSync(),
                getDeviceUnBindSub(),
                checkNewVersionRsp(),
                sdcardFormatSub()};
    }

    /**
     * sd卡被格式化
     *
     * @return
     */
    private Subscription sdcardFormatSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class)
                .subscribeOn(Schedulers.io())
                .filter(ret -> mView != null && TextUtils.equals(ret.uuid, uuid))
                .map(ret -> ret.rets)
                .flatMap(Observable::from)
                .filter(msg -> msg.id == 218)
                .map(msg -> {
                    if (msg.ret == 0) {
                        if (historyDataProvider != null) historyDataProvider.clean();
                        History.getHistory().clearHistoryFile(uuid);
                        AppLogger.d("清空历史录像");
                    }
                    return msg;
                })
                .subscribe(ret -> {
                }, AppLogger::e);
    }

//    private Subscription streamModeUpdate(){
//
//        Subscription subscription =
//    }

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
                        if (msg.id == 222) {
                            DpMsgDefine.DPSdcardSummary sdStatus = DpUtils.unpackData(msg.packValue, DpMsgDefine.DPSdcardSummary.class);
                            if (sdStatus == null) sdStatus = new DpMsgDefine.DPSdcardSummary();
                            if (!sdStatus.hasSdcard || sdStatus.errCode != 0)
                                updateLiveStream(TYPE_LIVE, 0, -1);
                        } else if (msg.id == DpMsgMap.ID_206_BATTERY) {
                            if (JFGRules.popPowerDrainOut(getDevice().pid)) {
                                Integer battery = DpUtils.unpackDataWithoutThrow(msg.packValue, Integer.class, 0);
                                if (battery != null && battery <= 20 && getDevice().$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet()).net > 0) {
                                    mView.onBatteryDrainOut();
                                }
                            }
                        }
                        mView.onDeviceInfoChanged(msg);
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
        if (slow) {
            saveHotSeatState();
        } else restoreHotSeatState();
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
        private int preNetType = 0;
        private WeakReference<CamLivePresenterImpl> presenterWeakReference;

        public NetworkAction(CamLivePresenterImpl camLivePresenter) {
            preNetType = NetUtils.getJfgNetType();
            this.presenterWeakReference = new WeakReference<>(camLivePresenter);
        }

        public void run() {
            if (presenterWeakReference != null && presenterWeakReference.get() != null) {
                Observable.just("")
                        .subscribeOn(Schedulers.io())
                        .filter(ret -> presenterWeakReference.get().mView != null)
                        .subscribe(ret -> {
                            int net = NetUtils.getJfgNetType();
                            if (preNetType == net) return;
                            preNetType = net;
                            if (net == 0) {
                                AppLogger.i("网络中断");
                                presenterWeakReference.get().mView.onNetworkChanged(false);
                            } else {
                                presenterWeakReference.get().mView.onNetworkChanged(true);
                                AppLogger.d("网络恢复");
                            }
                        }, AppLogger::e);
            }
        }
    }

    private static class TakeSnapShootHelper extends TakeSnapShootLogicHelper {

        TakeSnapShootHelper(String uuid, boolean forPopWindow, CamLiveContract.View v) {
            super(uuid, forPopWindow, v);
        }

        @Override
        public Pair<Bitmap, String> call(Object o) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            PerformanceUtils.startTrace("takeCapture");
            Bitmap bitmap = (Bitmap) o;
            if (weakReference.get() != null)
                weakReference.get().onTakeSnapShot(bitmap);//弹窗
            final String fileName = "." + uuid + System.currentTimeMillis();
            final String filePath = JConstant.MEDIA_PATH + File.separator + fileName;
            removeLastPreview();
            SimpleCache.getInstance().addCache(filePath, bitmap);
            PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, filePath);
            //需要删除之前的一条记录.
            BitmapUtils.saveBitmap2file(bitmap, filePath);
            MiscUtils.insertImage(JConstant.MEDIA_PATH, fileName);
            shareSnapshot(true, filePath);//最后一步处理分享
            return new Pair<>(bitmap, filePath);
        }
    }

    /**
     * 静态内部类
     */
    private static class TakeSnapShootLogicHelper implements Func1<Object, Pair<Bitmap, String>> {

        WeakReference<CamLiveContract.View> weakReference;
        boolean forPopWindow;
        String uuid;

        TakeSnapShootLogicHelper(String uuid, boolean forPopWindow, CamLiveContract.View v) {
            weakReference = new WeakReference<>(v);
            this.forPopWindow = forPopWindow;
            this.uuid = uuid;
        }

        private void doNext() {

        }

        @Override
        public Pair<Bitmap, String> call(Object o) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            PerformanceUtils.startTrace("takeCapture");
            byte[] data = BaseApplication.getAppComponent().getCmd().screenshot(false);
            if (data == null) {
                if (forPopWindow && weakReference.get() != null)
                    weakReference.get().onTakeSnapShot(null);//弹窗
                AppLogger.e("截图失败,data为空");

                // TODO: 2017/8/29 做一次尝试用 VideoView 截图

                return null;
            }
            int w = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoWidth;
            int h = ((JfgAppCmd) BaseApplication.getAppComponent().getCmd()).videoHeight;
            Bitmap bitmap = JfgUtils.byte2bitmap(w, h, data);
            if (forPopWindow && weakReference.get() != null)
                weakReference.get().onTakeSnapShot(bitmap);//弹窗
            PerformanceUtils.stopTrace("takeCapture");
            String filePath;
            String fileName = "";
            if (forPopWindow) {
                fileName = System.currentTimeMillis() + ".png";
                filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
            } else {
                filePath = JConstant.MEDIA_PATH + File.separator + "." + uuid + System.currentTimeMillis();
                removeLastPreview();
//                SimpleCache.getInstance().addCache(filePath, bitmap);
                PreferencesUtils.putString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid, filePath);
                //需要删除之前的一条记录.
            }
            BitmapUtils.saveBitmap2file(bitmap, filePath);
            if (forPopWindow) {//添加到相册
                MiscUtils.insertImage(JConstant.MEDIA_PATH, fileName);
            }
//            MediaScannerConnection.scanFile(ContextUtils.getContext(), new String[]{filePath}, null, null);
            if (weakReference.get() != null && !forPopWindow)
                weakReference.get().onPreviewResourceReady(bitmap);
            shareSnapshot(forPopWindow, filePath);//最后一步处理分享
            return new Pair<>(bitmap, filePath);
        }

        protected void removeLastPreview() {
            final String pre = PreferencesUtils.getString(JConstant.KEY_UUID_PREVIEW_THUMBNAIL_TOKEN + uuid);
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
            }
            String[] file = new File(JConstant.MEDIA_PATH).list((dir, name) -> !TextUtils.isEmpty(name) && name.contains(uuid));
            if (file.length == 0) return;
            Observable.just(file)
                    .subscribeOn(Schedulers.io())
                    .subscribe(ret -> {
                        for (String fileName : ret)
                            FileUtils.deleteFile(JConstant.MEDIA_PATH + File.separator + fileName);
                    }, AppLogger::e);
        }

        /**
         * 保存和分享,这是一个后台任务,用一个静态类,避免持有引用
         *
         * @param needShare
         */
        protected void shareSnapshot(boolean needShare, String localPath) {
            Observable.just(localPath)
                    .subscribeOn(Schedulers.io())
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

    private HotSeatStateMaintainer hotSeatStateMaintainer;

    public static final class HotSeatStateMaintainer {
        private static final String TAG = "HotSeatStateMaintainer";
        private WeakReference<CamLiveContract.View> viewWeakReference;
        private WeakReference<CamLiveContract.Presenter> presenterWeakReference;

        public HotSeatStateMaintainer(CamLiveContract.View view,
                                      CamLiveContract.Presenter presenter) {
            viewWeakReference = new WeakReference<>(view);
            presenterWeakReference = new WeakReference<>(presenter);
        }

        private volatile boolean micOn;
        private volatile boolean speakerOn;
        private boolean captureOn = true;

        private boolean filterRestore = true;

        /**
         * 停止播放了.
         */
        public void reset() {
            micOn = speakerOn = captureOn = false;
            if (viewWeakReference.get() != null)
                viewWeakReference.get().switchHotSeat(false, false,
                        false, false,
                        false, false);
            disableAudio();
        }

        /**
         *
         */
        private void disableAudio() {
            dump("disableAudio");
            setupLocalAudio(false, false);
//            Observable.just("reset")
//                    .subscribeOn(Schedulers.io())
//                    .subscribe(ret -> {
//                        if (presenterWeakReference.get() != null) {
//                            setupLocalAudio(false, false, false, false);
//                        }
//                    }, AppLogger::e);
        }

        private void dump(String tag) {
            Log.d(TAG, tag + ",micOn:" + micOn + ",speakerOn:" + speakerOn + ",captureOn:" + captureOn);
        }

        /**
         * 恢复三个按钮的状态.
         */
        public void restore() {
            filterRestore = true;
            if (viewWeakReference.get() != null && presenterWeakReference.get() != null) {
                int playType = presenterWeakReference.get().getPlayType();
                Observable.just("restoreAudio")
                        .subscribeOn(Schedulers.io())
                        .subscribe(ret -> {
                                    if (playType == TYPE_HISTORY)
                                        micOn = false;
                                    //设置客户端声音
                                    boolean result = setupLocalAudio(micOn, micOn || speakerOn);
                                    if (result) {
                                        //设置设备的声音
                                        setupRemoteAudio(micOn, micOn || speakerOn);
                                        //设置成功
                                    } else {
                                        micOn = false;
                                        setupRemoteAudio(micOn, micOn || speakerOn);
                                    }
                                    dump("restore?" + result);
                                    if (result || (!micOn && !speakerOn)) {
                                        viewWeakReference.get().switchHotSeat(micOn || speakerOn,
                                                !micOn,
                                                micOn,
                                                playType == TYPE_LIVE,
                                                captureOn, true);
                                    }
                                    if (!result) {
                                        viewWeakReference.get().onAudioPermissionCheck();
                                    }
                                },
                                AppLogger::e);
            }
        }

        /**
         * 由于开始loading了.需要保存当前状态.
         */
        public void saveRestore() {
            filterRestore = false;
            if (viewWeakReference.get() != null && presenterWeakReference.get() != null) {
                viewWeakReference.get().switchHotSeat(false, false, false, false, false, false);
                dump("saveRestore");
                disableAudio();
            }
        }

        public void switchMic() {
            if (presenterWeakReference.get() != null && viewWeakReference.get() != null) {
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .flatMap(ret -> {
                            //当前状态,remoteSpeaker = localMic ,remoteMic=localSpeaker
                            micOn = !micOn;

                            //设置客户端声音
                            boolean result = setupLocalAudio(micOn, micOn || speakerOn);
                            if (result) {
                                //设置设备的声音
                                setupRemoteAudio(micOn, micOn || speakerOn);
                                //设置成功
//                                speakerOn = tmpSpeaker;
                            } else {
                                micOn = false;
                                setupRemoteAudio(micOn, micOn || speakerOn);
                            }
                            return Observable.just(result);
                        })
                        .filter(ret -> ret)
                        .filter(ret -> viewWeakReference.get() != null)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ret -> {
                                    viewWeakReference.get().switchHotSeat(micOn || speakerOn, !micOn/*presenterWeakReference.get().getPlayType() == TYPE_LIVE*/,
                                            micOn,
                                            presenterWeakReference.get().getPlayType() == TYPE_LIVE,
                                            captureOn, true);
                                    dump("switchMic");
                                },
                                AppLogger::e);
            }
        }

        public void switchSpeaker() {
            if (presenterWeakReference.get() != null && viewWeakReference.get() != null) {
                Observable.just(true)
                        .subscribeOn(Schedulers.io())
                        .flatMap(ret -> {
                            //操作speaker的时候,本地的mic是关闭的.
                            speakerOn = !speakerOn;
                            boolean result = setupLocalAudio(micOn, speakerOn);
                            if (result) {
                                //设置设备的声音
                                setupRemoteAudio(micOn, speakerOn);
                                //说明已经有权限,并且设置成功
                                dump("switchSpeaker");
                                viewWeakReference.get().switchHotSeat(speakerOn, !micOn/*presenterWeakReference.get().getPlayType() == TYPE_LIVE*/,
                                        micOn,
                                        presenterWeakReference.get().getPlayType() == TYPE_LIVE,
                                        captureOn, true);
                            }
                            return Observable.just(result);
                        })
                        .filter(ret -> ret && viewWeakReference.get() != null)
                        .subscribe(ret -> {
                                },
                                AppLogger::e);
            }
        }


        // 参数与实际调用不一定是正确的 // modify
        private void setupRemoteAudio(boolean mic, boolean speaker) {
            if (presenterWeakReference.get().getLiveStream().playState == PLAY_STATE_PLAYING) {
                BaseApplication.getAppComponent().getCmd().setAudio(false, speaker, mic);
                AppLogger.d(String.format(Locale.getDefault(), "remoteMic:%s,remoteSpeaker:%s", speaker, mic));
                AppLogger.d("切换远程:mic:" + speaker + ",speaker:" + mic);
            }
        }

        private boolean setupLocalAudio(boolean localMic, boolean localSpeaker) {
            AppLogger.d(String.format(Locale.getDefault(), "localMic:%s,localSpeaker:%s", localMic, localSpeaker));
            MediaRecorder mRecorder = null;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//这是为了兼容魅族4.4的权限
                try {
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.release();
                } catch (Exception e) {
                    AppLogger.d(e.getMessage());
                    if (mRecorder != null) {
                        mRecorder.release();
                    }
                    AndroidSchedulers.mainThread().createWorker().schedule(() -> {
                        if (viewWeakReference == null || viewWeakReference.get() == null) return;
                        if (viewWeakReference.get().isUserVisible())
                            viewWeakReference.get().audioRecordPermissionDenied();
                    });
                    return false;
                }
            } else {
                if (!PermissionUtils.hasSelfPermissions(viewWeakReference.get().getContext(), Manifest.permission.RECORD_AUDIO)) {
                    return false;
                }
            }
            AppLogger.d("切换本地:mic:" + localMic + ",speaker:" + localSpeaker);
            // 有视频直播中才能操作。
            if (presenterWeakReference.get().getLiveStream().playState == PLAY_STATE_PLAYING) {
                BaseApplication.getAppComponent().getCmd().setAudio(true, localMic, localSpeaker);
            }
            if (presenterWeakReference.get().isEarpiecePlug()) {
                presenterWeakReference.get().switchEarpiece(true);
            }
            return true;
        }
    }
}
