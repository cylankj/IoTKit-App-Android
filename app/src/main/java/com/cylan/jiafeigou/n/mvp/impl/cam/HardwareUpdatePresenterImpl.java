package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
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
import com.cylan.udpMsgPack.JfgUdpMsg;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Index;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
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

    public HardwareUpdatePresenterImpl(HardwareUpdateContract.View view, RxEvent.CheckDevVersionRsp checkDevVersion) {
        super(view);
        view.setPresenter(this);
        this.checkDevVersion = checkDevVersion;
        this.simulatePercent = new SimulatePercent();
        this.simulatePercent.setOnAction(this);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                updateBack()
        };
    }

    @Override
    public UpdateFileBean creatDownLoadBean() {
        downLoadBean = new UpdateFileBean();
        downLoadBean.url = checkDevVersion.url;
        downLoadBean.version = checkDevVersion.version;
        downLoadBean.fileName = checkDevVersion.version;

        //TEST
//        UpdateFileBean downLoadBean = new UpdateFileBean();
//        downLoadBean.url = "http://yf.cylan.com.cn:82/sdk/libmedia-engine-jni-master.so";
//        downLoadBean.version = "22220000";
//        downLoadBean.fileName = "22220000";

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            downLoadBean.savePath = getView().getContext().getFilesDir().getAbsolutePath();
        } else {
            downLoadBean.savePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
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
                DownloadManagerPro.Config config = new DownloadManagerPro.Config()
                        .setContext(getView().getContext());
                DownloadManagerPro.getInstance().init(config);
                taskBuilder = new DownloadManagerPro.TaskBuilder();
                taskBuilder.setUrl(bean.url)
                        .setMaxChunks(4)
                        .setSaveName(bean.fileName)
                        .setOverwrite(true)
                        .setSdCardFolderAddress(bean.savePath)
                        .setDownloadManagerListener(listener)
                        .setAllowNetType(NetConfig.TYPE_ALL);
                int token = DownloadManagerPro.getInstance().initTask(taskBuilder);
                handler.sendMessageDelayed(handler.obtainMessage(MSG_START_DOWNLOAD, token, 0), 1000);
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
    public String getFileSize() {
        long length = 0;
        try {
            URL url = new URL(checkDevVersion.url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();//建立连接
            conn.setConnectTimeout(6 * 1000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.connect();
            if (conn.getResponseCode() == 200) {
                length = conn.getContentLength();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FormetSDcardSize(length);
    }

    @Override
    public void actionDone() {
        Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((Object integer) -> {
                    AppLogger.i("actionDone: " + integer);
                    getView().startUpdate();
                });
    }

    @Override
    public void actionPercent(int percent) {
        Observable.just(percent)
                .filter((Integer integer) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    getView().onUpdateing(integer);
                });
    }


    private class DownTemp {
        public DownTemp(double percent, long length) {
            this.percent = percent;
            this.length = length;
        }

        private double percent;
        private long length;

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
            Message msg = new Message();
            msg.what = 5;
            handler.sendMessage(msg);
        }
    };

    /**
     * desc:转换文件的大小
     *
     * @param fileS
     * @return
     */
    @Override
    public String FormetSDcardSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "0.0MB";
        } else if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }


    /**
     * 开始升级
     */
    @Override
    public void startUpdate() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new UpdatePing(downLoadBean.savePath + "/" + downLoadBean.fileName).toBytes());
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public Subscription updateBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((RxEvent.LocalUdpMsg localUdpMsg) -> {
                    //TODO 回调结果
                    MessagePack msgPack = new MessagePack();
                    try {
                        JfgUdpMsg.UdpHeader header = msgPack.read(localUdpMsg.data, JfgUdpMsg.UdpHeader.class);
                        final String headTag = header.cmd;
                        if (TextUtils.equals(headTag, "f_upgrade")) {
                            getView().handlerResult(2);
                        } else {
                            getView().handlerResult(3);
                        }
                    } catch (IOException e) {
                        AppLogger.i("unpack msgpack failed:" + e.getLocalizedMessage());
                    }
                });
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
        public String url;

        public UpdatePing(String url) {
            this.url = url;
            this.cmd = "f_upgrade";
        }
    }

    @Override
    public void stop() {
        super.stop();
        endCounting();
    }
}
