package com.cylan.jiafeigou.n.engine;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.ClientUpdateManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - setDevice intent actions, extra parameters and static
 * helper methods.
 * 登陆成功后，需要刷新一些缓存，数据，都在这里做。
 */
public class AfterLoginService extends IntentService {

    private static final String TAG = "KEY";
    /**
     * 保存账号密码，登陆成功后保存。
     */
    public static final String ACTION_SAVE_ACCOUNT = "action_save_account";
    public static final String ACTION_GET_ACCOUNT = "action_get_account";
    public static final String ACTION_SYN_OFFLINE_REQ = "action_offline_req";

    public static final String ACTION_CHECK_VERSION = "action_check_version";


    public AfterLoginService() {
        super("AfterLoginService");
    }

    public static void startSaveAccountAction(Context context) {
        Intent intent = new Intent(context, AfterLoginService.class);
        intent.putExtra(TAG, ACTION_SAVE_ACCOUNT);
        context.startService(intent);
    }

    public static void startGetAccountAction(Context context) {
        Intent intent = new Intent(context, AfterLoginService.class);
        intent.putExtra(TAG, ACTION_GET_ACCOUNT);
        context.startService(intent);
    }

    /**
     * 恢复离线时候,加入请求队列的消息
     */
    public static void resumeOfflineRequest() {
        Intent intent = new Intent(ContextUtils.getContext(), AfterLoginService.class);
        intent.putExtra(TAG, ACTION_SYN_OFFLINE_REQ);
        ContextUtils.getContext().startService(intent);
    }

    /**
     * 恢复离线时候,加入请求队列的消息
     */
    public static void resumeTryCheckVersion() {
        Intent intent = new Intent(ContextUtils.getContext(), AfterLoginService.class);
        intent.putExtra(TAG, ACTION_CHECK_VERSION);
        ContextUtils.getContext().startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getStringExtra(TAG);
            AppLogger.i("AfterLoginService: " + action + ",looper: " + (Looper.myLooper() == Looper.getMainLooper()));
            if (TextUtils.equals(action, ACTION_SAVE_ACCOUNT)) {
            } else if (TextUtils.equals(action, ACTION_GET_ACCOUNT)) {
                BaseApplication.getAppComponent().getCmd().getAccount();
            } else if (TextUtils.equals(action, ACTION_SYN_OFFLINE_REQ)) {
                Observable.just("go and do something")
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(s -> {
                            try {
                                String content = PreferencesUtils.getString(JConstant.BINDING_DEVICE);
                                UdpConstant.UdpDevicePortrait portrait = new Gson().fromJson(content, UdpConstant.UdpDevicePortrait.class);
                                if (portrait != null)
                                    BaseApplication.getAppComponent().getCmd().bindDevice(portrait.uuid, portrait.bindCode, portrait.mac, portrait.bindFlag);
                            } catch (Exception e) {
                                AppLogger.d("err: " + e.getLocalizedMessage());
                            }
                        }, throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()));
            } else if (TextUtils.equals(action, ACTION_CHECK_VERSION)) {
                Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                AppLogger.d("尝试检查版本");
                Observable.just("check_version")
                        .subscribeOn(Schedulers.newThread())
                        .flatMap(s -> {
                            int req = -1;
                            try {
                                String vid = PackageUtils.getMetaString(ContextUtils.getContext(), "vId");
                                req = BaseApplication.getAppComponent().getCmd().checkClientVersion(vid);
                            } catch (JfgException e) {
                                AppLogger.e("check_version failed:" + MiscUtils.getErr(e));
                            }
                            return Observable.just(req);
                        })
                        .filter(ret -> ret >= 0)
                        .flatMap(integer -> RxBus.getCacheInstance().toObservable(RxEvent.ClientCheckVersion.class)
                                .flatMap(clientCheckVersion -> {
                                    AppLogger.d("check_version result: " + clientCheckVersion);
//                                    clientCheckVersion.result = "VRJz6f";
                                    if (TextUtils.isEmpty(clientCheckVersion.result))
                                        return Observable.just(false);
                                    String finalUrl = JConstant.assembleUrl(clientCheckVersion.result, getApplicationContext().getPackageName());
                                    Request.Builder requestBuilder = new Request.Builder().url(finalUrl);
                                    requestBuilder.method("GET", null);
                                    OkHttpClient client = new OkHttpClient();
                                    client.newCall(requestBuilder.build())
                                            .enqueue(new Callback() {
                                                @Override
                                                public void onFailure(Call call, IOException e) {
                                                    AppLogger.e("check_version what the hell?" + MiscUtils.getErr(e));
                                                }

                                                @Override
                                                public void onResponse(Call call, Response response) throws IOException {
                                                    String result = response.body().string();
                                                    AppLogger.d("check_version result: " + result);
                                                    try {
                                                        JSONObject jsonObject = new JSONObject(result);
                                                        final String url = jsonObject.getString("url");
                                                        final String versionName = jsonObject.getString("version");
                                                        final String shortVersion = jsonObject.getString("shortversion");
                                                        final String desc = jsonObject.getString("desc");
                                                        ClientUpdateManager.getInstance().enqueue(url, versionName, shortVersion, desc, clientCheckVersion.forceUpgrade);
                                                    } catch (JSONException e) {
                                                        AppLogger.e(MiscUtils.getErr(e));
                                                    }
                                                }
                                            });
                                    return Observable.just(true);
                                }))
                        .subscribe(ret -> {
                        }, AppLogger::e);
            }
        }
    }

}