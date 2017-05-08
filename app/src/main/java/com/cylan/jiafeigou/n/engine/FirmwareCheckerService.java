package com.cylan.jiafeigou.n.engine;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class FirmwareCheckerService extends Service {

    private static final String UUID_TAG = "CID";

    private MapSubscription mapSubscription = new MapSubscription();

    public FirmwareCheckerService() {
    }


    public static void checkVersion(String uuid) {
        Context context = ContextUtils.getContext();
        Intent intent = new Intent(context, FirmwareCheckerService.class);
        intent.putExtra(UUID_TAG, uuid);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String uuid = intent.getStringExtra(UUID_TAG);
            mapSubscription.remove(uuid);
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
            AppLogger.d("开始检查升级:" + uuid);
            if (JFGRules.isShareDevice(uuid)) return;//分享设备不显示
            if (JFGRules.isPanoramicCam(device.pid)) return;//全景设备不显示
            Subscription s = Observable.just("go")
                    .subscribeOn(Schedulers.newThread())
                    .timeout(5, TimeUnit.SECONDS)
                    .flatMap(what -> {
                        long seq;
                        try {
                            String version = device.$(DpMsgMap.ID_207_DEVICE_VERSION, "0");
                            seq = BaseApplication.getAppComponent().getCmd().checkDevVersion(device.pid, uuid, version);
                        } catch (Exception e) {
                            AppLogger.e("checkNewHardWare:" + e.getLocalizedMessage());
                            seq = -1L;
                        }
                        return Observable.just(seq);
                    })
                    .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RxEvent.CheckDevVersionRsp.class)
                            .subscribeOn(Schedulers.newThread())
                            .filter(ret -> {
                                if (!ret.hasNew) {
                                    PreferencesUtils.remove(JConstant.KEY_FIRMWARE_CONTENT + uuid);
                                }
                                return ret.hasNew;
                            }))
                    .map(ret -> {
                        try {
                            Request request = new Request.Builder()
                                    .url(ret.url)
                                    .build();
                            Response response = new OkHttpClient().newCall(request).execute();
                            ret.fileSize = response.body().contentLength();
                            ret.fileDir = JConstant.MISC_PATH;
                            ret.hasNew = true;
                            ret.fileName = "." + uuid;
                            ret.uuid = uuid;
                            PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(ret));
                            long checkTime = PreferencesUtils.getLong(JConstant.KEY_FIRMWARE_CHECK_TIME + uuid, -1);
                            if (checkTime == -1 || System.currentTimeMillis() - checkTime > 24 * 3600 * 1000L) {
                                PreferencesUtils.putLong(JConstant.KEY_FIRMWARE_CHECK_TIME + uuid, System.currentTimeMillis());
                                RxBus.getCacheInstance().post(new RxEvent.FirmwareUpdateRsp(uuid));
                                AppLogger.d("检查到有新固件:" + uuid);
                                return ret;
                            }
                            return null;
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                    }, throwable -> {
                        if (throwable instanceof TimeoutException) {
                            mapSubscription.remove(uuid);
                            if (!mapSubscription.hasSubscriptions()) {
                                stopSelf();
                                AppLogger.e("停止service");
                            }
                        }
                    });
            mapSubscription.add(s, uuid);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapSubscription.unsubscribe();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
