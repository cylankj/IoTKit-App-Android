package com.cylan.jiafeigou.n.engine;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.File;

import rx.Observable;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class FirmwareCheckerService extends IntentService {

    private static final String UUID_TAG = "CID";

    public FirmwareCheckerService() {
        super("FirmwareCheckerService");
    }


    public static void checkVersion(String uuid) {
        Context context = ContextUtils.getContext();
        Intent intent = new Intent(context, FirmwareCheckerService.class);
        intent.putExtra(UUID_TAG, uuid);
        context.startService(new Intent());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                String uuid = intent.getStringExtra(UUID_TAG);
                String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT);
                FirmwareDescription description = new Gson().fromJson(content, FirmwareDescription.class);
                if (validateContent(description)) {
                    //可以升级了.包是好的
                } else {
                    //发送请求
                    Observable.just("go")
                            .map(s -> {
                                Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                                try {
                                    String version = device.$(DpMsgMap.ID_207_DEVICE_VERSION, "0");
                                    return BaseApplication.getAppComponent().getCmd().checkDevVersion(device.pid, uuid, version);
                                } catch (Exception e) {
                                    AppLogger.e("checkNewHardWare:" + e.getLocalizedMessage());
                                    return -1L;
                                }
                            })
                            .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RxEvent.CheckDevVersionRsp.class)
                                    .filter(checkDevVersionRsp -> checkDevVersionRsp.seq == aLong))
                            .subscribe(result -> {
                                AppLogger.d("开始下载固件?");
                            }, AppLogger::e);
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 24小时的规则,只有下载成功的前提下生效
     *
     * @param description
     * @return
     */
    private boolean validateContent(FirmwareDescription description) {
        if (description == null) return false;//没有下载过
        if (description.downloadState == JConstant.D.FAILED) return false;//下载失败
        if (System.currentTimeMillis() - description.downloadUpdateTime > 10 * 1000) return false;//
        if (TextUtils.isEmpty(description.filePath)) return false;//文件路径出错
        File file = new File(description.filePath);
        if (!file.exists()) return false;//文件不存在
        if (System.currentTimeMillis() - description.downloadUpdateTime < 24 * 3600 * 1000L)
            return false;//一天检查一次
//        String localFileMd5 = FileUtils.getFileMd5(description.filePath);
//        AppLogger.d("localFileMd5:" + localFileMd5);
//        if (!TextUtils.equals(localFileMd5, description.md5)) {
//            FileUtils.deleteFile(description.filePath);
//            return false;
//        }
        return true;
    }

    public static final class FirmwareDescription {
        public long lastCheckTime;//一天检查一次.
        public long downloadUpdateTime;//下载更新的时间, {如果上次是5s前+文件md5不匹配,那就证明失败了.}
        public long downloadState;//下载状态
        public String filePath;//本地路径
        public String url;
        public String version;
        public String description;
        public String md5;
    }
}
