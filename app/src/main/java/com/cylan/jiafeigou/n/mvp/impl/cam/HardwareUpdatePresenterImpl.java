package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.cam.HardwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.UpdateFileBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.download.core.DownloadManagerPro;
import com.cylan.jiafeigou.support.download.net.NetConfig;
import com.cylan.jiafeigou.support.download.report.listener.DownloadManagerListener;
import com.cylan.jiafeigou.support.download.report.listener.FailReason;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.ConnectivityStatus;
import com.cylan.jiafeigou.support.network.ReactiveNetwork;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class HardwareUpdatePresenterImpl extends AbstractPresenter<HardwareUpdateContract.View> implements HardwareUpdateContract.Presenter, SimulatePercent.OnAction {

    private RxEvent.CheckDevVersionRsp checkDevVersion;
    private DownloadManagerPro.TaskBuilder taskBuilder;
    private UpdateFileBean downLoadBean;

    private SimulatePercent simulatePercent;
    //    private DownloadManagerPro.Config config;
    private String uuid;
    private int updateTime;
    private int updatePingTime;
    private Network network;
    private int firmwareUpdateState = 0;//-1失败,0初始,1.升级中,2.升级成功

    public HardwareUpdatePresenterImpl(HardwareUpdateContract.View view, String uuid, RxEvent.CheckDevVersionRsp checkDevVersion) {
        super(view);
        view.setPresenter(this);
        this.uuid = uuid;
        this.checkDevVersion = checkDevVersion;
        this.simulatePercent = new SimulatePercent();
        this.simulatePercent.setOnAction(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                upgradePingBack(),
                updateBack()
        };
    }

    @Override
    public void start() {
        super.start();
        registerNetworkMonitor();
    }

    @Override
    public UpdateFileBean creatDownLoadBean() {
        downLoadBean = new UpdateFileBean();
        downLoadBean.url = checkDevVersion.url;
        downLoadBean.version = checkDevVersion.version;
        downLoadBean.fileName = checkDevVersion.version;

        downLoadBean.savePath = getView().getContext().getApplicationContext().getFilesDir().getAbsolutePath();
        AppLogger.d("initSavePath:" + downLoadBean.savePath);
/*        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            downLoadBean.savePath = getView().getContext().getFilesDir().getAbsolutePath();
        } else {
            downLoadBean.savePath = JConstant.MISC_PATH;
        }*/
        return downLoadBean;
    }

    @Override
    public void startDownload(Parcelable parcelable) {
        if (parcelable != null && parcelable instanceof UpdateFileBean) {
            UpdateFileBean bean = (UpdateFileBean) parcelable;
            initSomething(bean);
        }
    }

    private void initSomething(UpdateFileBean bean) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                taskBuilder = new DownloadManagerPro.TaskBuilder();
                taskBuilder.setUrl(bean.url)
                        .setMaxChunks(4)
                        .setSaveName(bean.fileName)
                        .setOverwrite(true)
                        .setSdCardFolderAddress(bean.savePath)
                        .setDownloadManagerListener(listener)
                        .setAllowNetType(NetConfig.TYPE_ALL);
                try {
                    int token = DownloadManagerPro.getInstance().initTask(taskBuilder);
                    handler.sendMessageDelayed(handler.obtainMessage(MSG_START_DOWNLOAD, token, 0), 1000);
                } catch (JfgException e) {
                    AppLogger.d("err: " + e.getLocalizedMessage());
                }
            }
        }).start();
    }

    private static final int MSG_START_DOWNLOAD = 1;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_DOWNLOAD:
                    Log.d(this.getClass().getSimpleName(), "handler: " + msg.arg1);
                    DownloadManagerPro.getInstance().startDownload(msg.arg1);
                    break;
                case 2:
                    DownTemp obj = (DownTemp) msg.obj;
                    getView().onDownloading(obj.percent, obj.length);
                    break;
                case 3:
                    getView().onDownloadFinish();
                    break;

                case 4:
                    getView().onDownloadStart();
                    break;

                case 5:
                    getView().onDownloadErr(1);
                    break;
            }
            return true;
        }
    });

    @Override
    public void stopDownload() {

    }

    @Override
    public void getFileSize(UpdateFileBean bean) {
        addSubscription(Observable.just("url")
                .subscribeOn(Schedulers.newThread())
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        long length = 0;
                        try {
                            if (TextUtils.isEmpty(checkDevVersion.url)) return Observable.just("");
//                    URL url = new URL("http://yf.cylan.com.cn:82/sdk/libmedia-engine-jni-master.so");
                            URL url = new URL(checkDevVersion.url);
                            URLConnection conn = url.openConnection();//建立连接
                            String headerField = conn.getHeaderField(6);
                            length = conn.getContentLength();
                            AppLogger.d("file name:" + headerField);
                            AppLogger.d("file_length:" + length);
                            //先从本地获取看看是否已下载
                            String localUrl = getView().getContext().getApplicationContext().getFilesDir().getAbsolutePath() + "/" + bean.fileName + ".bin";
//                            String localUrl = bean.savePath+"/"+ bean.fileName + ".bin";
                            File file = new File(localUrl);
                            AppLogger.d("local_url:" + file.getAbsolutePath());
                            AppLogger.d("file_length:" + getFileSize(file));
                            AppLogger.d("file_exit:" + file.exists());
                            if (file.exists()) {
                                if (!NetUtils.isNetworkAvailable(ContextUtils.getContext())) {
                                    return Observable.just("");
                                }
                                //包是否完整
                                if (getFileSize(file) == length) {
                                    return Observable.just("");
                                } else {
                                    boolean delete = file.delete();
                                    AppLogger.d("update_file_del:" + delete);
                                }
                            }
                            return Observable.just(MiscUtils.FormatSdCardSize(length));
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Observable.just("");
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    if (!TextUtils.isEmpty(s) && getView() != null) {
                        getView().initFileSize(s);
                    }
                }, AppLogger::e));
    }

    @Override
    public void actionDone() {
        Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((Object integer) -> {
                    AppLogger.i("actionDone: " + integer);
                    getView().beginUpdate();
                }, AppLogger::e);
    }

    @Override
    public void actionPercent(int percent) {
        Observable.just(percent)
                .filter((Integer integer) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().onUpdateing(integer);
                }, AppLogger::e);
    }


    private class DownTemp {
        public DownTemp() {
        }

        public DownTemp(double percent, long length) {
            this.percent = percent;
            this.length = length;
        }

        public double percent;
        public long length;

    }

    private DownloadManagerListener listener = new DownloadManagerListener() {
        @Override
        public void onDownloadStarted(long taskId) {
            Message msg = new Message();
            msg.what = 4;
            handler.sendMessage(msg);
        }

        @Override
        public void onDownloadPaused(long taskId) {

        }

        @Override
        public void onDownloadProcess(long taskId, double percent, long downloadedLength) {
            DownTemp temp = new DownTemp(percent, downloadedLength);
            Message msg = new Message();
            msg.what = 2;
            msg.obj = temp;
            handler.sendMessage(msg);
        }

        @Override
        public void onDownloadFinished(long taskId) {
            Message msg = new Message();
            msg.what = 3;
            handler.sendMessage(msg);
        }

        @Override
        public void onDownloadRebuildStart(long taskId) {

        }

        @Override
        public void onDownloadRebuildFinished(long taskId) {

        }

        @Override
        public void onDownloadCompleted(long taskId) {

        }

        @Override
        public void onFailedReason(long taskId, FailReason reason) {
            AppLogger.d("download_fail:" + reason.toString());
            Message msg = new Message();
            msg.what = 5;
            handler.sendMessage(msg);
        }
    };

    /**
     * 开始升级
     */
    @Override
    public void startUpdate(String Ip, short port, String cid) {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        int ipAddress = 0;
                        WifiManager mWifi = (WifiManager) ContextUtils.getContext().getSystemService(Context.WIFI_SERVICE);
                        if (mWifi.isWifiEnabled()) {
                            WifiInfo wifiInfo = mWifi.getConnectionInfo();
                            ipAddress = wifiInfo.getIpAddress();
                        }
                        String ip = intToIp(ipAddress);
                        String localUrl = "http://" + ip + ":8765/" + downLoadBean.fileName + ".bin";
//                        String localUrl = downLoadBean.savePath+"/"+ downLoadBean.fileName + ".bin";
                        AppLogger.d("localUrl2:" + localUrl);
                        firmwareUpdateState = 1;
                        if (TextUtils.equals(cid, uuid)) {
                            updateTime = BaseApplication.getAppComponent().getCmd().sendLocalMessage(Ip, port, new UpdatePing(localUrl, uuid, ip, 8765).toBytes());
                            AppLogger.d("beginUpdate2:" + updateTime);
                        }
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    @Override
    public Subscription updateBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.LocalUdpMsg localUdpMsg) -> {
                    //回调结果
                    AppLogger.d("endUpdate:" + localUdpMsg.time);
                    MessagePack msgPack = new MessagePack();
                    try {
                        JfgUdpMsg.UdpHeader header = msgPack.read(localUdpMsg.data, JfgUdpMsg.UdpHeader.class);
                        final String headTag = header.cmd;
                        AppLogger.d("udp_cmd:" + headTag);
                        if (TextUtils.equals(headTag, "f_ack")) {
                            firmwareUpdateState = 2;
                            endCounting();
                            getView().handlerResult(2);
                            AppLogger.d("f_upgrade:succ");
                        } else {
//                          getView().handlerResult(3);
                            firmwareUpdateState = -1;
                        }
                    } catch (IOException e) {
                        AppLogger.i("unpack msgpack failed:" + e.getLocalizedMessage());
                    }
                }, AppLogger::e);
    }

    @Override
    public void startCounting() {
        if (simulatePercent != null)
            simulatePercent.start();
    }

    @Override
    public void endCounting() {
        if (simulatePercent != null)
            simulatePercent.stop();
    }


    @org.msgpack.annotation.Message
    public static class UpdatePing extends JfgUdpMsg.UdpHeader {
        @Index(1)
        public String cid;
        @Index(2)
        public String ip;
        @Index(3)
        public int port;
        @Index(4)
        public String url;

        public UpdatePing(String url, String cid, String ip, int port) {
            this.url = url;
            this.cmd = "f_upgrade";
            this.ip = ip;
            this.port = port;
            this.cid = cid;
        }
    }

    @Override
    public void stop() {
        super.stop();
        endCounting();
        unregisterNetworkMonitor();
    }

    /**
     * 获取文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    private long getFileSize(File file) {
        long size = 0;
        try {
            if (file.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(file);
                size = fis.available();
                AppLogger.d("getF:" + size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    @Override
    public void myDownLoad(String fileUrl, String fileName) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 区别3g狗文件后缀
                    String file = fileName + ".bin";
                    Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                    if (device != null) {
                        if (JFGRules.is3GCam(device.pid)) {
                            file = fileName + ".apk";
                        }
                    }

                    FileOutputStream fileOutputStream = getView().getContext().getApplicationContext().openFileOutput(file, Context.MODE_WORLD_WRITEABLE);
                    URL url = new URL(fileUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //获取文件长度
                    int fileLength = conn.getContentLength();

                    AppLogger.d("binLength:" + fileLength + " fileUrl:" + fileUrl);

                    InputStream input = null;
                    input = conn.getInputStream();
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    int total = 0;
                    while ((len = input.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                        total += len;
                        Message message = new Message();
                        message.what = 1;
                        DownTemp temp = new DownTemp();
                        temp.length = fileLength;
                        temp.percent = total * 1.0 / fileLength;
                        message.obj = temp;
                        myHandler.sendMessage(message);
                        AppLogger.d("myDown:" + total * 1.0 / fileLength);
                        AppLogger.d("myDownLen:" + total);
                    }
                    input.close();
                    fileOutputStream.close();
                    fileOutputStream.flush();
                    AppLogger.d("myDown:下完了");
                    myHandler.sendEmptyMessage(0);
                } catch (IOException e) {
                    myHandler.sendEmptyMessage(2);
                    AppLogger.d("myDown:" + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:         //下载中
                    DownTemp obj = (DownTemp) msg.obj;
                    getView().onDownloading(obj.percent, obj.length);
                    AppLogger.d("progress" + obj.length);
                    break;
                case 2:         //下载失败
                    getView().onDownloadErr(1);
                    break;
                case 0:         //下载成功
                    AppLogger.d("开始升级...");
                    String[] strings = getView().getContext().getApplicationContext().fileList();
                    for (String s : strings) {
                        AppLogger.d("file_name:" + s);
                    }
                    getView().onDownloadFinish();
                    break;
            }
        }
    };

    @Override
    public void upgradePing() {
        getView().showPingLoading();
        rx.Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        updatePingTime = BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                        AppLogger.d("beginPing2:" + updatePingTime);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    @Override
    public Subscription upgradePingBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.LocalUdpMsg localUdpMsg) -> {
                    //回调结果
                    AppLogger.d("endPing:" + localUdpMsg.time);
                    MessagePack msgPack = new MessagePack();
                    try {
                        JfgUdpMsg.FPingAck fPingAck = msgPack.read(localUdpMsg.data, JfgUdpMsg.FPingAck.class);
                        if (fPingAck != null) {
                            final String headTag = fPingAck.cmd;
                            AppLogger.d("udp_cmd:" + headTag + ":" + fPingAck.version);
                            if (TextUtils.equals(headTag, "f_ping_ack")) {
                                getView().hidePingLoading();
                                startUpdate(localUdpMsg.ip, localUdpMsg.port, fPingAck.cid);
                                startCounting();
                                AppLogger.d("f_ping:succ:" + fPingAck.cid);
                            } else {
                                //设备无响应
                                getView().deviceNoRsp();
                            }
                        }
                    } catch (IOException e) {
                        AppLogger.i("unpack msgpack failed:" + e.getLocalizedMessage());
                    }
                }, AppLogger::e);
    }


    public void registerNetworkMonitor() {
        try {
            if (network == null) {
                network = new Network();
                final IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                ContextUtils.getContext().registerReceiver(network, filter);
            }
        } catch (Exception e) {
            AppLogger.e("registerNetworkMonitor" + e.getLocalizedMessage());
        }
    }

    public void unregisterNetworkMonitor() {
        if (network != null) {
            ContextUtils.getContext().unregisterReceiver(network);
            network = null;
        }
    }

    /**
     * 监听网络状态
     */
    private class Network extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityStatus status = ReactiveNetwork.getConnectivityStatus(context);
                updateConnectivityStatus(status.state);
            }
        }
    }

    /**
     * 连接状态变化
     */
    private void updateConnectivityStatus(int network) {
        Observable.just(network)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        if (integer == -1 && firmwareUpdateState == 1) {
                            endCounting();
                            getView().handlerResult(3);
                        }
                    }
                }, AppLogger::e);
    }

}
