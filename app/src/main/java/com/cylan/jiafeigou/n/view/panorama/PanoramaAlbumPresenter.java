package com.cylan.jiafeigou.n.view.panorama;

import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.base.module.PanoramaEvent;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.module.DownloadFile;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.model.PAlbumBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.RandomUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.socket.JfgSocket;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.base.module.PanoramaEvent.MIDRobotForwardDataV2;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_FILE_DOWNLOAD_REQ;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_FILE_DOWNLOAD_RSP;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_FILE_LIST_REQ;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_FILE_LIST_RSP;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_FIRST_FILE_REQ;
import static com.cylan.jiafeigou.base.module.PanoramaEvent.TYPE_FIRST_FILE_RSP;

/**
 * Created by yanzhendong on 2017/3/13.
 */

public class PanoramaAlbumPresenter extends BasePresenter<PanoramaAlbumContact.View> implements
        PanoramaAlbumContact.Presenter, JfgSocket.JFGSocketCallBack {

    private long socketPointer = -1;
    private boolean hasConnected;

    @Override
    public void onStart() {
        super.onStart();
        if (socketPointer == -1)
            socketPointer = JfgSocket.InitSocket(this);
        AppLogger.d("start: " + socketPointer);
        makeTCPBridge();
    }

    @Override
    public void onStop() {
        super.onStop();
        PanAlbumDataManager.getInstance().setDownloading(false);
        AppLogger.d("onStop: " + socketPointer);
        try {
            if (socketPointer != -1) {
                socketPointer = -1;
                JfgSocket.Disconnect(socketPointer);
            }
        } catch (Exception e) {
            Log.d("err: ", "PanoramaAlbumPresenter: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void OnConnected() {
        AppLogger.d("socked connected");
        startGetFirstItem();
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault());

    public void startSyncAlbumList(int time, int limit) {
        PanoramaEvent.MsgFileListReq req = new PanoramaEvent.MsgFileListReq();
        req.beginTime = time;
        req.endTime = req.beginTime + 24 * 3600;
        req.limit = limit;
        byte[] data = fill(new PanoramaEvent.RawReqMsg(), MIDRobotForwardDataV2, TYPE_FILE_LIST_REQ, DpUtils.pack(req));
        boolean send = JfgSocket.SendMsgpackBuff(socketPointer, data);
        AppLogger.d("send ret:" + send + " time:" + dateFormat.format(new Date(time * 1000L)));
    }

    /**
     * 得到文件
     */
    private void startGetFirstItem() {
        PanoramaEvent.MsgFirstFileInListReq req = new PanoramaEvent.MsgFirstFileInListReq();
        byte[] data = fill(new PanoramaEvent.RawReqMsg(), MIDRobotForwardDataV2,
                TYPE_FIRST_FILE_REQ, DpUtils.pack(req));
        boolean send = JfgSocket.SendMsgpackBuff(socketPointer, data);
        AppLogger.d("send ret:" + send + "请求第一条文件时间");
    }

    protected byte[] fill(PanoramaEvent.RawReqMsg rawReqMsg, int msgId, int type, byte[] msg) {
        rawReqMsg.mSeq = RandomUtils.getRandom(Integer.MAX_VALUE);
        rawReqMsg.dst = Collections.singletonList(mUUID);
        rawReqMsg.mCaller = "";
        rawReqMsg.mCallee = "";
        rawReqMsg.mId = msgId;
        rawReqMsg.isAck = 1;
        rawReqMsg.type = type;
        rawReqMsg.msg = msg;
        return DpUtils.pack(rawReqMsg);
    }

    @Override
    public void OnDisconnected() {
        socketPointer = -1;
        hasConnected = false;
        AppLogger.d("OnDisconnected ");
        PanAlbumDataManager.getInstance().setDownloading(false);
    }

    private void notifyNoFile() {
        Observable.just("no file")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    ArrayList<PAlbumBean> arrayList = mView.getList();
                    mView.onDelete(arrayList);
                }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
    }

    private static final String KEY_FIRST_FILE_TIME = "firstFileTime";

    private void handleFirstFileTime(int time) {
        if (time <= 0) {
            AppLogger.d("设备没有文件:" + time);
            notifyNoFile();
            return;
        }
        int firstTime = PreferencesUtils.getInt(KEY_FIRST_FILE_TIME + mUUID, 0);
        AppLogger.d("firstTime in cache: " + firstTime + " " + time);
        if (time > firstTime) {
            if (firstTime != 0)
                AppLogger.d("设备刷新了，或者更换sd卡了。:" + dateFormat.format(new Date(firstTime * 1000L)));
            PreferencesUtils.putInt(KEY_FIRST_FILE_TIME + mUUID, time);
        } else {
            AppLogger.d("设备刷新了，或者更换sd卡了。:" + dateFormat.format(new Date(firstTime * 1000L)));
        }
        startSyncAlbumList(time, 20);
    }

    @Override
    public void OnMsgpackBuff(byte[] data) {
        AppLogger.d("socket buffer:" + data.length);
        try {
            PanoramaEvent.RawRspMsg header = DpUtils.unpackData(data, PanoramaEvent.RawRspMsg.class);
            if (header == null) return;
            switch (header.type) {
                case TYPE_FIRST_FILE_RSP:
                    //设备中列表的第一条数据的时间。
                    Integer startTime = DpUtils.unpackData(header.msg, Integer.class);
                    handleFirstFileTime(startTime == null ? -1 : startTime);
                    break;
                case TYPE_FILE_LIST_RSP:
                    PanoramaEvent.MsgFileListRsp fileListRsp = DpUtils.unpackData(header.msg, PanoramaEvent.MsgFileListRsp.class);
                    if (fileListRsp != null && fileListRsp.array != null)
                        RxBus.getCacheInstance().post(new FileListRspEvent(fileListRsp.array));
                    break;
                case TYPE_FILE_DOWNLOAD_RSP://文件区块响应
                    Log.d("PanoramaAlbumPresenter", "文件下载中....");
                    RxBus.getCacheInstance().post(new DownFileRsp(header.msg));
                    break;
            }
            AppLogger.d("msgId: " + header + ",type:" + header.type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static final class DownFileRsp {
        byte[] data;

        public DownFileRsp(byte[] data) {
            this.data = data;
        }
    }

    public static final class FileListRspEvent {
        public PanoramaEvent.MsgFile[] array;

        public FileListRspEvent(PanoramaEvent.MsgFile[] array) {
            this.array = array;
        }
    }

    private Subscription fileDownloadRsp() {
        return RxBus.getCacheInstance().toObservable(DownFileRsp.class)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .filter(aFile -> aFile.data != null && aFile.data.length > 0)
                .map(downFileRsp -> {
                    try {
                        PanoramaEvent.MsgFileDownloadRsp rsp = DpUtils.unpackData(downFileRsp.data, PanoramaEvent.MsgFileDownloadRsp.class);
                        if (rsp == null || rsp.isInValid()) {
                            AppLogger.e("file download err: " + rsp);
                            return null;
                        }
                        int updateRet = PanAlbumDataManager.getInstance().updateDownloadFile(rsp.fileName, rsp.offset);
                        if (updateRet == 1) {
                            AppLogger.d("文件下载完成...:" + rsp);
                            goonDownload();
                            return null;
                        }
                        byte[] fileMd5 = PanAlbumDataManager.getInstance().getFileMd5(rsp.fileName);
                        if (fileMd5 == null || fileMd5.length == 0) {
                            AppLogger.e("file md5 is null: " + rsp);
                            return null;
                        }
                        PanoramaEvent.MsgFileDownloadReq req = new PanoramaEvent.MsgFileDownloadReq();
                        req.begin = rsp.offset;
                        req.fileName = rsp.fileName;
                        req.md5 = fileMd5;
                        byte[] data = fill(new PanoramaEvent.RawReqMsg(),
                                MIDRobotForwardDataV2,
                                TYPE_FILE_DOWNLOAD_REQ,
                                DpUtils.pack(req));
                        boolean ret = JfgSocket.SendMsgpackBuff(socketPointer, data);
                        AppLogger.d("文件下载中 ret: " + ret + " goto write to file: " + req);
                    } catch (Exception e) {
                        AppLogger.d("file Download err: " + e);
                    }
                    return null;
                })
                .doOnError(throwable -> AppLogger.e("downloadFile err: " + throwable.getLocalizedMessage()))
                .subscribe();
    }

    /**
     * 开始下载
     */
    private void goonDownload() {
        if (PanAlbumDataManager.getInstance().isDownloading()) {
            AppLogger.d("已经在下载");
            return;
        }
        //判断当前下载状态
        DownloadFile file = PanAlbumDataManager.getInstance().getNextPreparedDownloadFile();
        if (file == null) {
            AppLogger.e("err: no file need download");
            return;
        }
        PanoramaEvent.MsgFileDownloadReq req = new PanoramaEvent.MsgFileDownloadReq();
        req.begin = file.offset;
        req.offset = req.begin + 64;
        req.fileName = file.fileName;
        req.md5 = file.md5;
        byte[] data = fill(new PanoramaEvent.RawReqMsg(),
                MIDRobotForwardDataV2,
                TYPE_FILE_DOWNLOAD_REQ,
                DpUtils.pack(req));
        boolean ret = JfgSocket.SendMsgpackBuff(socketPointer, data);
        Log.d("PanoramaAlbumPresenter", "尝试开始下载:" + ret + " " + req);
    }

    private Subscription fileListRspSubscription() {
        return RxBus.getCacheInstance().toObservable(FileListRspEvent.class)
                .subscribeOn(Schedulers.io())
                .map(fileListRspEvent -> {
                    if (fileListRspEvent.array == null || fileListRspEvent.array.length == 0)
                        return null;
                    for (PanoramaEvent.MsgFile file : fileListRspEvent.array) {
                        if (TextUtils.isEmpty(file.fileName)) {
                            AppLogger.e("fileName is empty: " + file);
                            continue;
                        }
                        PanAlbumDataManager.getInstance().putFile(file.fileName, file.md5, file.fileSize);
                        Log.d("PanoramaAlbumPresenter", "file: " + file);
                    }
                    goonDownload();
                    return null;
                })
                .map(msgFiles -> {
                    ArrayList<PAlbumBean> viewList = mView.getList();
                    return convert(PanAlbumDataManager.getInstance().getAllFileList());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    mView.onAppend(list);
                    AppLogger.d("fileList： " + list.size());
                }, throwable -> {
                    AppLogger.e("err: " + throwable.getLocalizedMessage());
                    registerSubscription(fileListRspSubscription());
                });
    }

    private ArrayList<PAlbumBean> convert(List<DownloadFile> list) {
        ArrayList<PAlbumBean> arrayList = new ArrayList<>(list.size());
        Collections.sort(list);
        Log.d("PanoramaAlbumPresenter", "sort:" + list);
        ArrayList<String> dateList = new ArrayList<>();
        for (DownloadFile file : list) {
            String date = TimeUtils.getDayString(file.getTimeStamp() * 1000L);
            PAlbumBean bean = new PAlbumBean();
            bean.timeInDate = file.getTimeStamp() * 1000L;
            if (!dateList.contains(date)) {
                dateList.add(date);
                bean.isDate = true;
                arrayList.add(bean);
            } else {
                arrayList.add(bean);
            }
        }
        return arrayList;
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
        registerSubscription(getNetWorkChangedSub());
        registerSubscription(fileDownloadRsp());
        registerSubscription(fileListRspSubscription());
    }

    private Observable<Boolean> checkConnection() {
        return Observable.just("checkConnection")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    final WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
                    if (info == null || !JFGRules.isCylanDevice(info.getSSID())) {
                        AppLogger.i("checkConnection: " + info);
//                        mView.showAlert()
                        return false;
                    }
                    return true;
                });
    }

    private void makeTCPBridge() {
        checkConnection()
                .filter(aBoolean -> aBoolean)
                .map(s -> {
                    try {
                        AppLogger.d("正在发送 FPing 消息");
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.d("连接 socket 出现错误");
                    }
                    return null;
                })
                .timeout(3, TimeUnit.SECONDS)
                .zipWith(RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .map(new Func1<RxEvent.LocalUdpMsg, Boolean>() {
                            @Override
                            public Boolean call(RxEvent.LocalUdpMsg msg) {
                                try {
                                    JfgUdpMsg.UdpHeader header = DpUtils.unpackData(msg.data, JfgUdpMsg.UdpHeader.class);
                                    Log.d("PanoramaAlbumPresenter", "header: " + new Gson().toJson(header));
                                    if (header == null || !TextUtils.equals(header.cmd, "f_ping_ack"))
                                        return false;
                                    JfgUdpMsg.FPingAck pingAck = DpUtils.unpackData(msg.data, JfgUdpMsg.FPingAck.class);
                                    Log.d("PanoramaAlbumPresenter", "pingAck: " + new Gson().toJson(pingAck));
                                    if (pingAck == null || !TextUtils.equals(pingAck.cid, mUUID))
                                        return false;
                                    //得到 fping响应
                                    synchronized (this) {
                                        if (!hasConnected) {
                                            hasConnected = true;
                                            AppLogger.d("获取到设备 IP 地址:" + msg.ip + ",port:" + msg.port);
                                            JfgSocket.Connect(socketPointer, msg.ip, msg.port, true);
                                            return true;
                                        }
                                    }
                                } catch (Exception e) {
                                    AppLogger.e("err: " + e.getLocalizedMessage());
                                    return false;
                                }
                                return false;
                            }
                        }), (Object o, Boolean aBoolean) -> aBoolean)
                .filter(aBoolean -> aBoolean)
                .doOnError(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        AppLogger.e("f_ping timeout :");
                    }
                })
                .subscribe(o -> AppLogger.d("good"),
                        throwable -> AppLogger.e("f_ping err :" + throwable.getLocalizedMessage()),
                        () -> AppLogger.d("make tcp finished"));
    }

    private Subscription getNetWorkChangedSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (event.mobile != null && event.mobile.isConnected()) {
                        //移动网络,提醒用户注意流量
                        AppLogger.d("mobile network connected");
                    } else if (event.wifi != null && event.wifi.isConnected()) {
                        //wifi 网络,关闭流量提醒
                        AppLogger.d("wifi network connected");
                    }
                });

    }


    @Override
    public void fresh(boolean asc) {
        ArrayList<PAlbumBean> list = mView.getList();
        int startTime = 0;
        if (list == null || list.size() == 0) {
        } else {
            startTime = (int) (list.get(0).timeInDate / 1000);
        }
        startSyncAlbumList(startTime, 20);
    }

    @Override
    public void downloadFile(String fileName) {
        DownloadFile file = PanAlbumDataManager.getInstance().getDownloadFile(fileName);
        if (file == null) {
            AppLogger.e("文件损坏");
            return;
        }
    }
}
