package com.cylan.jiafeigou.n.engine;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class FirmwareCheckerService extends Service {

    private static final String UUID_TAG = "CID";
    private static final String FORCE_TAG = "FORCE";

    private static Map<String, Long> antiHighFrequentCheck = new HashMap<>();
    private MapSubscription mapSubscription = new MapSubscription();

    public FirmwareCheckerService() {
    }

    /**
     * 一分钟内制作一次检查,不管force标志
     *
     * @param uuid
     * @return
     */
    private static boolean check(String uuid) {
        Long time = antiHighFrequentCheck.get(uuid);
        if (time == null || System.currentTimeMillis() - time > 5 * 60 * 1000) {
            antiHighFrequentCheck.put(uuid, System.currentTimeMillis());
            AppLogger.e("记得改成一分钟");
            return true;
        }
        return false;
    }

    public static void checkVersion(String uuid) {
//        if (!check(uuid)) return;
        Context context = ContextUtils.getContext();
        Intent intent = new Intent(context, FirmwareCheckerService.class);
        intent.putExtra(UUID_TAG, uuid);
        context.startService(intent);
    }

    public static void checkVersion(String uuid, boolean force) {
        if (!check(uuid)) return;
        Context context = ContextUtils.getContext();
        Intent intent = new Intent(context, FirmwareCheckerService.class);
        intent.putExtra(UUID_TAG, uuid);
        intent.putExtra(FORCE_TAG, force);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            int net = NetUtils.getJfgNetType();
            //客户端无网络
            if (net == 0) {
                tryStopSelf();
                return;
            }
            String uuid = intent.getStringExtra(UUID_TAG);
            Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
            if (intent.hasExtra(FORCE_TAG) && intent.getBooleanExtra(FORCE_TAG, false)) {
                //不管任何条件,都检查升级
                handleCheckFlow(uuid, device.$(207, ""), device.pid);
                return;
            }
            AppLogger.d("开始检查升级:" + uuid);
            //分享设备不显示
            if (JFGRules.isShareDevice(uuid)) {
                tryStopSelf();
                return;
            }
            //全景设备不显示
            if (JFGRules.isPanoramicCam(device.pid)) {
                tryStopSelf();
                return;
            }
//            DpMsgDefine.DPNet dpNet = device.$(201, new DpMsgDefine.DPNet());
//            //设备离线就不要检查了
//            if (!JFGRules.isDeviceOnline(dpNet)) {
//                tryStopSelf();
//                return;
//            }
//            String localSSid = NetUtils.getNetName(ContextUtils.getContext());
//            String remoteSSid = dpNet.ssid;
//            //原型上说,局域网才弹框.
//            //客户端和设备相同的网络才去检查.因为检查是很快的.
//            if (!TextUtils.equals(localSSid, remoteSSid)) {
//                tryStopSelf();
//                return;
//            }
            handleCheckFlow(uuid, device.$(207, ""), device.pid);
        }
    }

    private void tryStopSelf() {
        if (mapSubscription != null && !mapSubscription.hasSubscriptions()) {
            stopSelf();
        }
    }

    private void handleCheckFlow(final String uuid, String currentVersion, int pid) {
        mapSubscription.remove(uuid);
        Subscription s = Observable.just("go")
                .subscribeOn(Schedulers.newThread())
                .timeout(5, TimeUnit.SECONDS)
                .flatMap(what -> {
                    long seq;
                    try {
                        seq = BaseApplication.getAppComponent().getCmd().checkDevVersion(pid, uuid, currentVersion);
                    } catch (Exception e) {
                        AppLogger.e("checkNewHardWare:" + e.getLocalizedMessage());
                        seq = -1L;
                    }
                    return Observable.just(seq);
                })
                .flatMap(aLong -> RxBus.getCacheInstance().toObservable(RxEvent.CheckVersionRsp.class)
                        .subscribeOn(Schedulers.newThread())
                        .filter(ret -> ret != null && TextUtils.equals(uuid, ret.uuid))
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
                        ret.fileDir = JConstant.ROOT_DIR;
                        ret.hasNew = true;
                        ret.fileName = "." + uuid + MiscUtils.getFileNameWithoutExn(ret.url);
                        ret.uuid = uuid;
                        ret.preKey = JConstant.KEY_FIRMWARE_CONTENT + uuid;
                        PreferencesUtils.putString(JConstant.KEY_FIRMWARE_CONTENT + uuid, new Gson().toJson(ret));

                        RxBus.getCacheInstance().post(new RxEvent.FirmwareUpdateRsp(uuid));
                        AppLogger.d("检查到有新固件:" + uuid);
                        return ret;
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
