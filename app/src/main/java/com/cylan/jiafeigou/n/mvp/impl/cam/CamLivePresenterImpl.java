package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.entity.jniCall.JFGHistoryVideo;
import com.cylan.entity.jniCall.JFGMsgVideoDisconn;
import com.cylan.entity.jniCall.JFGMsgVideoResolution;
import com.cylan.entity.jniCall.JFGMsgVideoRtcp;
import com.cylan.entity.jniCall.JFGVideo;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.BaseValue;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.HistoryDateFlatten;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.widget.wheel.ex.DataExt;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.utils.BitmapUtil;
import com.cylan.utils.NetUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePresenterImpl extends AbstractPresenter<CamLiveContract.View> implements CamLiveContract.Presenter {
    //    private DeviceBean bean;
//    private BeanCamInfo beanCamInfo;
    private boolean isRtcpSignal;
    private int playType = CamLiveContract.TYPE_LIVE;
    private boolean speakerFlag, micFlag;
    private int[] videoResolution = {0, 0};
    private int playState = PLAY_STATE_IDLE;
    private ArrayList<JFGVideo> simpleCache = new ArrayList<>();
    private HistoryDateFlatten historyDateFlatten = new HistoryDateFlatten();
    private IData historyDataProvider;
    private String uuid;
    /**
     * 帧率记录
     */
    private List<Integer> frameRateList = new ArrayList<>();

    public CamLivePresenterImpl(CamLiveContract.View view, String uuid) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
    }

    /**
     * Rtcp和resolution的回调,
     * 只有resolution回调之后,才能设置{@link JfgAppCmd#setRenderLocalView(View)}
     * 正常播放回调
     * 10s没有视频,直接断开
     *
     * @return
     */
    private Subscription rtcpNotifySub() {
        return RxBus.getCacheInstance().toObservable(JFGMsgVideoRtcp.class)
                .filter((JFGMsgVideoRtcp rtcp) -> (getView() != null && isRtcpSignal))
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((JFGMsgVideoRtcp rtcp) -> {
                    frameRateList.add(rtcp.frameRate);
                    if (frameRateList.size() == 11) {
                        frameRateList.remove(0);//移除最前沿的一个
                        boolean isBad = MiscUtils.isBad(frameRateList, 2, 10);
                        if (isBad) {
                            frameRateList.clear();
                            AppLogger.e("is bad net work");
                            playState = PLAY_STATE_IDLE;
                            getView().onLiveStop(playType,
                                    JFGRules.PlayErr.ERR_LOW_FRAME_RATE);
                            //暂停播放
                            stopPlayVideo(playType);
                        }
                    }
                    getView().onRtcp(rtcp);
//                    Log.d(TAG, "rtcp: " + new Gson().toJson(rtcp));
                }, (Throwable throwable) -> {
                    AppLogger.e("rtcp err: " + throwable.getLocalizedMessage());
                });
    }

    /**
     * 分辨率回调
     *
     * @return
     */
    private Subscription resolutionNotifySub() {
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
                    isRtcpSignal = true;
                    videoResolution[0] = resolution.width;
                    videoResolution[1] = resolution.height;
                    try {
                        getView().onResolution(resolution);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    getView().onLiveStarted(playType);
                    playState = PLAY_STATE_PLAYING;
                    AppLogger.i("ResolutionNotifySub: " + new Gson().toJson(resolution));
                }, (Throwable throwable) -> {
                    AppLogger.e("resolution err: " + throwable.getLocalizedMessage());
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
                    playState = PLAY_STATE_IDLE;
                    getView().onLiveStop(playType, jfgMsgVideoDisconn.code);
                }, (Throwable throwable) -> {
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
                .observeOn(Schedulers.newThread())
                .subscribe((Object dataStack) -> {
                    //获取设备历史录像
                    if (!TextUtils.isEmpty(uuid)) {
                        RxEvent.JFGHistoryVideoReq req = new RxEvent.JFGHistoryVideoReq();
                        req.uuid = uuid;
                        RxBus.getCacheInstance().post(req);
                        //不直接使用这个接口,因为在videoList的数据结构中没有uuid标签,只能使用请求的seq来判断.
                        //所有把它统一放到History类中管理.
                        //JfgCmdInsurance.getCmd().getVideoList(uuid);
                        AppLogger.i("getVideoList");
                    }
                });
    }

    @Override
    public boolean isShareDevice() {
        JFGDevice device = GlobalDataProxy.getInstance().fetch(uuid);
        return device != null && !TextUtils.isEmpty(device.shareAccount);
    }

    @Override
    public void startPlayVideo(int type) {
        getView().onLivePrepare(type);
        playState = PLAY_STATE_PREPARE;
        playType = CamLiveContract.TYPE_LIVE;
        Observable.just(uuid)
                .subscribeOn(Schedulers.newThread())
                .filter((String s) -> {
                    //判断网络状况
                    final int net = NetUtils.getJfgNetType(getView().getContext());
                    AppLogger.i("play start live " + net + " " + s);
                    if (net == 0) {
                        Observable.just(null)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((Object o) -> {
                                    playState = PLAY_STATE_IDLE;
                                    getView().onLiveStop(playType, JFGRules.PlayErr.ERR_NERWORK);
                                });
                        return false;
                    }
                    return !TextUtils.isEmpty(s);
                })
                .subscribe((String s) -> {
                    try {
                        JfgCmdInsurance.getCmd().playVideo(s);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    AppLogger.i("play video");
                });
    }

    @Override
    public void startPlayHistory(long time) {
        playType = CamLiveContract.TYPE_HISTORY;
        playState = PLAY_STATE_PREPARE;
        if (NetUtils.getJfgNetType(getView().getContext()) == 0) {
            //断网了
            stopPlayVideo(getPlayType());
            playState = PLAY_STATE_IDLE;
            getView().onLiveStop(getPlayType(), JFGRules.PlayErr.ERR_NERWORK);
            return;
        }
        Observable.just(time)
                .subscribeOn(Schedulers.newThread())
                .subscribe((Long aLong) -> {
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
                }, (Throwable throwable) -> {
                    AppLogger.e("err:" + throwable.getLocalizedMessage());
                });
    }

    @Override
    public void stopPlayVideo(int type) {
        Observable.just(uuid)
                .subscribeOn(Schedulers.newThread())
                .subscribe((String s) -> {
                    try {
                        AppLogger.i("stopPlayVideo:" + s);
                        JfgCmdInsurance.getCmd().stopPlay(s);
                        playType = CamLiveContract.TYPE_NONE;
                        playState = PLAY_STATE_IDLE;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void switchSpeakerMic(final boolean local, final boolean speakerFlag,
                                 final boolean micFlag) {
        this.speakerFlag = speakerFlag;
        this.micFlag = micFlag;
        Observable.just(true)
                .subscribeOn(Schedulers.newThread())
                .subscribe((Boolean aBoolean) -> {
                    JfgCmdInsurance.getCmd().setAudio(local, speakerFlag, micFlag);
                    AppLogger.i(String.format(Locale.getDefault(), "local:%s,speaker:%s,mic:%s", local, speakerFlag, micFlag));
                });
    }

    @Override
    public void takeSnapShot() {
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe((Object o) -> {
                    byte[] data = JfgCmdInsurance.getCmd().screenshot(false);
                    Bitmap bitmap = BitmapUtil.byte2bitmap(videoResolution[0], videoResolution[1], data);
                    String filePath = JConstant.MEDIA_PATH + File.separator + System.currentTimeMillis() + ".png";
                    BitmapUtil.saveBitmap2file(bitmap, filePath);
                    snapshotResult(bitmap != null);
                }, (Throwable throwable) -> {
                    AppLogger.e("takeSnapshot: " + throwable.getLocalizedMessage());
                });
    }

    private void snapshotResult(boolean bitmap) {
        Log.d("takeSnapShot", "takeSnapShot: " + (bitmap));
        Observable.just(bitmap)
                .filter((Boolean bit) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Boolean b) -> {
                    getView().onTakeSnapShot(b != null && ((boolean) b));
                });
    }

    @Override
    public boolean getSpeakerFlag() {
        return speakerFlag;
    }

    @Override
    public boolean getMicFlag() {
        return micFlag;
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
        DpMsgDefine.DPNet net = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_201_NET, null);
        return net != null && JFGRules.isDeviceOnline(net)
                && NetUtils.getJfgNetType(getView().getContext()) != 0;
    }

    @Override
    public void updateInfoReq(Object value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    BaseValue baseValue = new BaseValue();
                    baseValue.setId(id);
                    baseValue.setVersion(System.currentTimeMillis());
                    baseValue.setValue(o);
                    GlobalDataProxy.getInstance().update(uuid, baseValue, true);
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                rtcpNotifySub(),
                resolutionNotifySub(),
                videoDisconnectSub(),
                robotDataSync(),
                historyDataListSub()};
    }

    /**
     * 接受历史录像数据
     *
     * @return
     */
    private Subscription historyDataListSub() {
        return RxBus.getCacheInstance().toObservable(JFGHistoryVideo.class)
                .subscribeOn(Schedulers.computation())
                .map((JFGHistoryVideo jfgHistoryVideo) -> {
                    long time = System.currentTimeMillis();
                    simpleCache.addAll(jfgHistoryVideo.list);
                    simpleCache = new ArrayList<>(new HashSet<>(simpleCache));
                    Collections.sort(simpleCache);
                    if (simpleCache.size() == 0)
                        return null;
                    AppLogger.d(String.format("performance:%s", (System.currentTimeMillis() - time)));
                    AppLogger.i("historyDataListSub:" + new Gson().toJson(jfgHistoryVideo));
                    IData data = new DataExt();
                    data.flattenData(simpleCache);
                    historyDateFlatten.flat(simpleCache);
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
        return RxBus.getCacheInstance().toObservable(RxEvent.DataPoolUpdate.class)
                .filter((RxEvent.DataPoolUpdate jfgRobotSyncData) -> (
                        getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .map((RxEvent.DataPoolUpdate update) -> {
                    if (update.id == DpMsgMap.ID_508_CAMERA_STANDBY_FLAG) {
                        boolean flag = MiscUtils.cast(update.value.getValue(), false);
                        getView().onDeviceStandBy(flag);
                    }
                    return null;
                })
                .retry(new RxHelper.RxException<>("robotDataSync"))
                .subscribe();
    }
}
