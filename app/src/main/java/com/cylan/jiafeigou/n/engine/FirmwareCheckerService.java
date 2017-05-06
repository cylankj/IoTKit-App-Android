package com.cylan.jiafeigou.n.engine;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.ClientUpdateManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
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
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String uuid = intent.getStringExtra(UUID_TAG);
            //更新时间
            FirmwareDescription description = null;
            try {
                String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + uuid);
                description = new Gson().fromJson(content, FirmwareDescription.class);
            } catch (Exception e) {
                description = null;
            }
            if (validateContent(description)) {
                //可以升级了.包是好的
                long time = PreferencesUtils.getLong(JConstant.KEY_FIRMWARE_CHECK_TIME + description.uuid, -1L);
                if (time != -1 && System.currentTimeMillis() - time < 24 * 3600 * 1000) return;
                //一天提示一次.
                RxBus.getCacheInstance().post(new RxEvent.FirmwareUpdate(description.uuid));
            } else {

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
        if (TextUtils.isEmpty(description.fileDir)) return false;//文件路径出错
        File file = new File(description.fileDir, description.fileName);
        if (!file.exists()) return false;//文件不存在
        //下载中,App异常了.这个条件优先级比较低,如果很就之前就下载好了呢.
        if (System.currentTimeMillis() - description.downloadUpdateTime > 2 * 1000 * 60)
            return false;
        //
        //        String localFileMd5 = FileUtils.getFileMd5(description.filePath);
//        AppLogger.d("localFileMd5:" + localFileMd5);
//        if (!TextUtils.equals(localFileMd5, description.md5)) {
//            FileUtils.deleteFile(description.filePath);
//            return false;
//        }
        return true;
    }


    private void startDownloadFirmware(FirmwareDescription desc) {
        try {
            Gson gson = new Gson();
            AppLogger.d("开始升级");
            ClientUpdateManager.getInstance().downLoadFile(desc.url, desc.fileName, desc.fileDir, new ClientUpdateManager.DownloadListener() {
                @Override
                public void start() {
                    AppLogger.d("开始下载");
                }

                @Override
                public void failed(Throwable throwable) {
                    AppLogger.d("下载失败: " + MiscUtils.getErr(throwable));
                    PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + desc.uuid);
                    PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CHECK_TIME + desc.uuid);
                }

                @Override
                public void finished(File file) {
                    AppLogger.d("下载完成");
                    desc.downloadState = JConstant.D.SUCCESS;
                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + desc.uuid, gson.toJson(desc));
                    RxBus.getCacheInstance().post(new RxEvent.FirmwareUpdate(desc.uuid));
                }

                @Override
                public void process(long currentByte, long totalByte) {
                    desc.downloadState = JConstant.D.DOWNLOADING;
                    desc.downloadUpdateTime = System.currentTimeMillis();
                    PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + desc.uuid, gson.toJson(desc));
                    Log.d("FirmwareCheckerService", "downloading: " + (float) currentByte / totalByte);
                }
            });
        } catch (Exception e) {
            AppLogger.e(MiscUtils.getErr(e));
        }
    }

    public static final class FirmwareDescription {
        //        public long lastCheckTime;//一天检查一次.
        public long downloadUpdateTime;//下载更新的时间, {如果上次是5s前+文件md5不匹配,那就证明失败了.}
        public long downloadState;//下载状态
        public String fileDir;//本地路径
        public String fileName;
        public String url;
        public String version;
        public String description;
        public String md5;
        public String uuid;

        @Override
        public String toString() {
            return "FirmwareDescription{" +
                    ", downloadUpdateTime=" + downloadUpdateTime +
                    ", downloadState=" + downloadState +
                    ", fileDir='" + fileDir + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", url='" + url + '\'' +
                    ", version='" + version + '\'' +
                    ", description='" + description + '\'' +
                    ", md5='" + md5 + '\'' +
                    ", uuid='" + uuid + '\'' +
                    '}';
        }
    }
}
