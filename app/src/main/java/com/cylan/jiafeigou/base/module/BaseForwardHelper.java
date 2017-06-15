package com.cylan.jiafeigou.base.module;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.entity.jniCall.JFGDPMsgRet;
import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.RandomUtils;
import com.google.gson.Gson;

import org.msgpack.MessagePack;

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
    private static BaseForwardHelper instance;

    public static BaseForwardHelper getInstance() {
        return instance;
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

    public <T> Observable<T> sendForward(String uuid, int msgId, Object msg, int forwardType) {//forwardType:0 服务器透传;1:本地透传
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .filter(accountArrived -> accountArrived.account.isOnline())
                .first()
                .observeOn(Schedulers.io())
                .map(accountArrived -> {
                    int seq = RandomUtils.getRandom(Integer.MAX_VALUE);
                    try {
                        PanoramaEvent.MsgForward forward = new PanoramaEvent.MsgForward();
                        forward.dst = Collections.singletonList(TextUtils.isEmpty(uuid) ? "" : uuid);
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
                        if (forwardType == 0) {
                            int ret = appCmd.SendForwardData(pack(forward));
                            AppLogger.e("透传消息返回值:" + ret);
                        } else if (forwardType == 1) {
                            DeviceInformation information = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
                            if (information != null && information.ip != null) {
                                appCmd.sendLocalMessage(information.ip, (short) information.port, pack(forward));
                            }
                        }
                    } catch (Exception e) {
                        AppLogger.e(e.getMessage());
                    }
                    return seq;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(PanoramaEvent.MsgForward.class).filter(rsp -> rsp.mSeq == seq).first())
                .map(this::dispatcherForward);
    }

    public <T> Observable<T> sendForward(String uuid, int msgId, Object msg) {
        return sendForward(uuid, msgId, msg, 0);
    }

    public <T> Observable<T> setDataPoint(String uuid, int msgId) {
        return Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
            try {
                ArrayList<JFGDPMsg> params = new ArrayList<>();
                JFGDPMsg dpMsg = new JFGDPMsg(msgId, 0);
                params.add(dpMsg);
                Long seq = appCmd.robotSetData(uuid, params);
                AppLogger.d("正在向服务器发送 dp消息:" + msgId);
                subscriber.onNext(seq);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.SetDataRsp.class).filter(rsp -> rsp.seq == seq))
                .first()
                .map(rsp -> parserSet(msgId, rsp));
    }

    private <R> R parserSet(int msgId, RxEvent.SetDataRsp rsp) {
        try {
            AppLogger.d("收到服务器的 dp 消息");
            if (rsp.rets.size() == 0) return null;
            JFGDPMsgRet msg = rsp.rets.get(0);
            if (msgId == 218) {
                DpMsgDefine.DPSdStatus status = new DpMsgDefine.DPSdStatus();
                status.hasSdcard = msg.ret == 0;
                return (R) status;
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return null;
    }

    public <T> Observable<T> sendDataPoint(String uuid, int msgId) {
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
                .map(rsp -> parserGet(msgId, rsp));
    }

    private <T> T parserGet(int msgId, RobotoGetDataRsp rsp) {
        try {
            AppLogger.d("收到服务器的 dp 消息");
            if (rsp.map.size() == 0) return null;
            if (rsp.map.get(msgId).size() == 0) return null;
            JFGDPMsg msg = rsp.map.get(msgId).get(0);
            if (msgId == 204) {
                DpMsgDefine.DPSdStatus status = unpackData(msg.packValue, DpMsgDefine.DPSdStatus.class);
                PanoramaEvent.MsgSdInfoRsp infoRsp = new PanoramaEvent.MsgSdInfoRsp();
                infoRsp.sdcard_recogntion = status.err;
                infoRsp.sdIsExist = status.hasSdcard ? 1 : 0;
                infoRsp.storage = status.total;
                infoRsp.storage_used = status.used;
                return (T) infoRsp;
            } else if (msgId == 205) {
                AppLogger.e("当前是否有电源线:" + new MessagePack().read(msg.packValue).toString());
                Boolean aBoolean = unpackData(msg.packValue, boolean.class);
                PanoramaEvent.MsgPowerLineRsp powerLineRsp = new PanoramaEvent.MsgPowerLineRsp();
                powerLineRsp.powerline = aBoolean != null && aBoolean ? 1 : 0;
                return (T) powerLineRsp;
            } else if (msgId == 206) {
                Integer battery = unpackData(msg.packValue, int.class);
                PanoramaEvent.MsgBatteryRsp batteryRsp = new PanoramaEvent.MsgBatteryRsp();
                batteryRsp.battery = battery;
                return (T) batteryRsp;
            } else if (msgId == 218) {
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return null;
    }

    public <T> Observable<T> empty() {
        return Observable.empty();
    }

    public <T> T dispatcherForward(PanoramaEvent.MsgForward forward) {
        try {
            AppLogger.d("forward:" + new Gson().toJson(forward));
            AppLogger.d("messagePack:" + new Gson().toJson(new MessagePack().read(forward.msg).toString()));
            switch (forward.type) {
                case 1:       //TYPE_FILE_DOWNLOAD_REQ          = 1   下载请求
                case 3:       //TYPE_FILE_DELETE_REQ            = 3   删除请求
                case 4:       //TYPE_FILE_DELETE_RSP            = 4   删除响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgFileRsp.class);
                case 5:       //TYPE_FILE_LIST_REQ              = 5   列表请求
                case 6:       //TYPE_FILE_LIST_RSP              = 6   列表响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgFileListRsp.class);
                case 7:       //TYPE_TAKE_PICTURE_REQ           = 7   拍照请求
                case 8:       //TYPE_TAKE_PICTURE_RSP           = 8   拍照响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgFileRsp.class);
                case 9:       //TYPE_VIDEO_BEGIN_REQ            = 9   开始录像请求
                case 10:      //TYPE_VIDEO_BEGIN_RSP            = 10  开始录像响应
                    PanoramaEvent.MsgRsp msgRsp = new PanoramaEvent.MsgRsp(unpackData(forward.msg, int.class));
                    if (msgRsp != null && msgRsp.ret == 0) {
                        DataSourceManager.getInstance().pushDeviceState(forward.mCaller);
                    }
                    return (T) msgRsp;
                case 11:      //TYPE_VIDEO_END_REQ              = 11  停止录像请求
                case 12:      //TYPE_VIDEO_END_RSP              = 12  停止录像响应 PanoramaEvent.TP tp = unpackData(forward.msg, PanoramaEvent.TP.class);
                    PanoramaEvent.MsgFileRsp msgFileRsp = unpackData(forward.msg, PanoramaEvent.MsgFileRsp.class);
                    if (msgFileRsp != null && msgFileRsp.ret == 0) {
                        DataSourceManager.getInstance().removeDeviceState(forward.mCaller);
                    }
                    return (T) msgFileRsp;
                case 13:      //TYPE_VIDEO_STATUS_REQ           = 13  查询录像状态请求
                case 14:      //TYPE_VIDEO_STATUS_RSP           = 14  查询录像状态响应
                    PanoramaEvent.MsgVideoStatusRsp msgVideoStatusRsp = unpackData(forward.msg, PanoramaEvent.MsgVideoStatusRsp.class);
                    if (msgVideoStatusRsp != null && msgVideoStatusRsp.ret == 0) {
                        DataSourceManager.getInstance().pushDeviceState(forward.mCaller);
                    } else {
                        DataSourceManager.getInstance().removeDeviceState(forward.mCaller);
                    }
                    return (T) msgVideoStatusRsp;
                case 15:      //TYPE_FILE_LOGO_REQ              = 15  设置水印请求
                case 16:      //TYPE_FILE_LOGO_RSP              = 16  设置水印响应
                    return (T) new PanoramaEvent.MsgRsp(unpackData(forward.msg, int.class));
                case 17:      //TYPE_FILE_RESOLUTION_REQ        = 17  设置视频分辨率请求
                case 2:       //TYPE_FILE_DOWNLOAD_RSP          = 2   下载响应
                case 18:      //TYPE_FILE_RESOLUTION_RSP        = 18  视频分辨率响应
                    return (T) new PanoramaEvent.MsgRsp(unpackData(forward.msg, int.class));
                case 20:      //TYPE_FILE_GET_LOGO_RSP          = 20  查询水印响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgLogoRsp.class);
                case 21:      //TYPE_FILE_GET_RESOLUTION_REQ    = 21  查询视频分辨率请求
                case 22:      //TYPE_FILE_GET_RESOLUTION_RSP    = 22  查询视频分辨率响应
                    return (T) unpackData(forward.msg, PanoramaEvent.MsgResolutionRsp.class);
                case 52: {
                    PanoramaEvent.ReportMsgList reportMsgList = unpackData(forward.msg, PanoramaEvent.ReportMsgList.class);
                    if (reportMsgList != null) {
                        ArrayList<Long> idList = new ArrayList<>();
                        ArrayList<JFGDPMsg> msgList = new ArrayList<>();
                        for (PanoramaEvent.DpMsgForward msg : reportMsgList.msgForwards) {
                            idList.add(msg.id);
                            msgList.add(msg.msg());
                        }
                        RxEvent.DeviceSyncRsp syncData = new RxEvent.DeviceSyncRsp().setUuid(forward.mCaller, idList, msgList);
                        RxBus.getCacheInstance().post(syncData);
                    }
                    break;
                }
                case 62: {

                }
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return null;
    }

}
