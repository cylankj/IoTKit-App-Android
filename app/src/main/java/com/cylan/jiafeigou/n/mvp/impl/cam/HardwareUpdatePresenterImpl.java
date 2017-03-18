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
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;

import org.msgpack.MessagePack;
import org.msgpack.annotation.Index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
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
    private DownloadManagerPro.Config config;

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
//        downLoadBean.version = "3330000";
//        downLoadBean.fileName = "3330000";

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            downLoadBean.savePath = getView().getContext().getFilesDir().getAbsolutePath();
        } else {
            downLoadBean.savePath = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator;
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
                config = new DownloadManagerPro.Config()
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
                    config = null;
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
                    if (TextUtils.isEmpty(checkDevVersion.url))return Observable.just("");
//                    URL url = new URL("http://yf.cylan.com.cn:82/sdk/libmedia-engine-jni-master.so");
                    URL url = new URL(checkDevVersion.url);
                    URLConnection conn = url.openConnection();//建立连接
                    String headerField = conn.getHeaderField(6);
                    length = conn.getContentLength();
                    AppLogger.d("file name:"+headerField);
                    AppLogger.d("file_length:"+length);
                    //先从本地获取看看是否已下载
                    String localUrl = "/mnt/sdcard/"+Environment.getExternalStorageDirectory().getAbsolutePath()+ "/" + bean.fileName+".bin";
                    File file = new File(localUrl);
                    AppLogger.d("local_url:"+file.getAbsolutePath());
                    AppLogger.d("file_length:"+getFileSize(file));
                    AppLogger.d("file_exit:"+file.exists());
                    if (file.exists() && getFileSize(file) == length){
                        return Observable.just("");
                    }
                    return Observable.just(MiscUtils.FormetSDcardSize(length));
                } catch (Exception e) {
                    e.printStackTrace();
                    return Observable.just("");
                }
            }
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s->{
            if (!TextUtils.isEmpty(s) && getView() != null){
                getView().initFileSize(s);
            }
        }));
    }

    @Override
    public void actionDone() {
        Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe((Object integer) -> {
                    AppLogger.i("actionDone: " + integer);
                    getView().beginUpdate();
                });
    }

    @Override
    public void actionPercent(int percent) {
        Observable.just(percent)
                .filter((Integer integer) -> (getView() != null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((Integer integer) -> {
                    AppLogger.d("updataing:"+integer);
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
            AppLogger.d("download_fail:"+reason.toString());
            Message msg = new Message();
            msg.what = 5;
            handler.sendMessage(msg);
        }
    };


    /**
     * 开始升级
     */
    @Override
    public void startUpdate() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        String localUrl = "/mnt/sdcard"+downLoadBean.savePath + "/" + downLoadBean.fileName+".bin";
                        AppLogger.d("localUrl:"+localUrl);
                        int req = JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new UpdatePing(localUrl).toBytes());
                        AppLogger.d("beginUpdate:"+req);
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
                        AppLogger.d("udp_cmd:"+headTag);
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
        @Index(2)
        public String cid;

        public UpdatePing(String url) {
            this.url = url;
            this.cmd = "f_upgrade";
//            this.cid = cid;
        }
    }

    @Override
    public void stop() {
        super.stop();
        endCounting();
    }

    /**
     * 获取文件大小
     * @param file
     * @return
     * @throws Exception
     */
    private long getFileSize(File file){
        long size = 0;
        try {
            if (file.exists()){
                FileInputStream fis = null;
                fis = new FileInputStream(file);
                size = fis.available();
                AppLogger.d("getF:"+size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }
}
