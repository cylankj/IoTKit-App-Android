package com.cylan.jiafeigou.base.module;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.RandomUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpUtils.pack;
import static com.cylan.jiafeigou.dp.DpUtils.unpackData;

/**
 * Created by yanzhendong on 2017/5/11.
 */
@Singleton
public class BaseForwardHelper {

    private AppCmd appCmd = null;
    private String uuid;
    private static BaseForwardHelper instance;

    public static BaseForwardHelper getInstance() {
        return instance;
    }

    public void init(String uuid) {
        this.uuid = uuid;
    }

    @Inject
    public BaseForwardHelper() {
        instance = this;
    }

    public void setAppCmd(AppCmd appCmd) {
        this.appCmd = appCmd;
    }

    public Observable<BaseForwardHelper> getApi() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .filter(accountArrived -> accountArrived.account.isOnline())
                .first()
                .map(accountArrived -> this);
    }

    public <T> Observable<T> sendForward(int msgId, Object msg) {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .filter(accountArrived -> accountArrived.account.isOnline())
                .first()
                .observeOn(Schedulers.io())
                .map(accountArrived -> {
                    int seq = RandomUtils.getRandom(Integer.MAX_VALUE);
                    PanoramaEvent.MsgForward forward = new PanoramaEvent.MsgForward();
                    forward.dst = Collections.singletonList(uuid);
                    forward.mCaller = "";
                    forward.mCallee = "";
                    forward.mId = 20006;
                    forward.isAck = 1;
                    forward.type = msgId;
                    forward.mSeq = seq;
                    if (msg != null) {
                        forward.msg = pack(msg);
                    }
                    AppLogger.d("正在向设备发送透传消息:" + new Gson().toJson(forward) + ":" + new Gson().toJson(msg));
                    int ret = appCmd.SendForwardData(pack(forward));
                    AppLogger.e("透传消息返回值:" + ret);
                    return seq;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(PanoramaEvent.MsgForward.class).filter(rsp -> rsp.mSeq == seq).first())
                .map(this::parse);
    }

    public <T> Observable<T> sendDataPoint(int msgId) {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                JFGDPMsg dpMsg = new JFGDPMsg(msgId, 0);
                params.add(dpMsg);
                Long seq = appCmd.robotGetData(uuid, params, 20, false, 0);
                AppLogger.d("正在向服务器发送 dp消息:" + msgId);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).filter(rsp -> rsp.seq == seq))
                .first()
                .map(rsp -> parser(msgId, rsp));
    }

    private <T> T parser(int msgId, RobotoGetDataRsp rsp) {
        try {
            AppLogger.d("收到服务器的 dp 消息");
            JFGDPMsg msg = rsp.map.get(msgId).get(0);
            if (msgId == 204) {
                DpMsgDefine.DPSdStatus status = unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                PanoramaEvent.MsgSdInfoRsp infoRsp = new PanoramaEvent.MsgSdInfoRsp();
                infoRsp.sdcard_recogntion = status.err;
                infoRsp.sdIsExist = status.hasSdcard;
                infoRsp.storage = status.total;
                infoRsp.storage_used = status.used;
                return (T) infoRsp;
            } else if (msgId == 205) {
                Boolean aBoolean = unpackData(msg.packValue, boolean.class);
                PanoramaEvent.MsgPowerLineRsp powerLineRsp = new PanoramaEvent.MsgPowerLineRsp();
                powerLineRsp.powerline = aBoolean != null && aBoolean ? 1 : 0;
                return (T) powerLineRsp;
            } else if (msgId == 206) {
                Integer battery = unpackData(msg.packValue, int.class);
                PanoramaEvent.MsgBatteryRsp batteryRsp = new PanoramaEvent.MsgBatteryRsp();
                batteryRsp.battery = battery;
                return (T) batteryRsp;
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return null;
    }

    public <T> Observable<T> empty() {
        return Observable.empty();
    }

    private <T> T parse(PanoramaEvent.MsgForward forward) {
        AppLogger.d("收到服务器的透传消息:" + new Gson().toJson(forward));
        try {
            switch (forward.type) {
                case 1:       //TYPE_FILE_DOWNLOAD_REQ          = 1   下载请求
                case 2:       //TYPE_FILE_DOWNLOAD_RSP          = 2   下载响应
                case 3:       //TYPE_FILE_DELETE_REQ            = 3   删除请求
                case 4:       //TYPE_FILE_DELETE_RSP            = 4   删除响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgFileRsp.class);
                case 5:       //TYPE_FILE_LIST_REQ              = 5   列表请求
                case 6:       //TYPE_FILE_LIST_RSP              = 6   列表响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgFileListRsp.class);
                case 7:       //TYPE_TAKE_PICTURE_REQ           = 7   拍照请求
                case 8:       //TYPE_TAKE_PICTURE_RSP           = 8   拍照响应
                    PanoramaEvent.TP tp = unpackData(forward.msg, PanoramaEvent.TP.class);
                    PanoramaEvent.MsgFileRsp fileRsp = new PanoramaEvent.MsgFileRsp();
                    fileRsp.ret = tp.ret;
                    fileRsp.files = Collections.singletonList(tp.pitcure);
                    return (T) fileRsp;
                case 9:       //TYPE_VIDEO_BEGIN_REQ            = 9   开始录像请求
                case 10:      //TYPE_VIDEO_BEGIN_RSP            = 10  开始录像响应
                    return (T) new PanoramaEvent.MsgRsp(unpackData(forward.msg, int.class));
                case 11:      //TYPE_VIDEO_END_REQ              = 11  停止录像请求
                case 12:      //TYPE_VIDEO_END_RSP              = 12  停止录像响应 PanoramaEvent.TP tp = unpackData(forward.msg, PanoramaEvent.TP.class);
                    PanoramaEvent.TP vp = unpackData(forward.msg, PanoramaEvent.TP.class);
                    PanoramaEvent.MsgFileRsp fRsp = new PanoramaEvent.MsgFileRsp();
                    fRsp.ret = vp.ret;
                    fRsp.files = Collections.singletonList(vp.pitcure);
                    return (T) fRsp;
                case 13:      //TYPE_VIDEO_STATUS_REQ           = 13  查询录像状态请求
                case 14:      //TYPE_VIDEO_STATUS_RSP           = 14  查询录像状态响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgVideoStatusRsp.class);
                case 15:      //TYPE_FILE_LOGO_REQ              = 15  设置水印请求
                case 16:      //TYPE_FILE_LOGO_RSP              = 16  设置水印响应
                    return (T) new PanoramaEvent.MsgRsp(unpackData(forward.msg, int.class));
                case 17:      //TYPE_FILE_RESOLUTION_REQ        = 17  设置视频分辨率请求
                case 18:      //TYPE_FILE_RESOLUTION_RSP        = 18  视频分辨率响应
                    return (T) new PanoramaEvent.MsgRsp(unpackData(forward.msg, int.class));
                case 20:      //TYPE_FILE_GET_LOGO_RSP          = 20  查询水印响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgLogoRsp.class);
                case 21:      //TYPE_FILE_GET_RESOLUTION_REQ    = 21  查询视频分辨率请求
                case 22:      //TYPE_FILE_GET_RESOLUTION_RSP    = 22  查询视频分辨率响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgResolutionRsp.class);
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return null;
    }

}
