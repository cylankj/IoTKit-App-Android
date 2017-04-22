package com.cylan.jiafeigou.push;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.huawei.hms.api.ConnectionResult;
import com.huawei.hms.api.HuaweiApiClient;
import com.huawei.hms.support.api.client.PendingResult;
import com.huawei.hms.support.api.client.ResultCallback;
import com.huawei.hms.support.api.hwid.HuaweiId;
import com.huawei.hms.support.api.hwid.SignOutResult;
import com.huawei.hms.support.api.push.HuaweiPush;

import static com.cylan.jiafeigou.push.PushConstant.PUSH_TAG;

/**
 * Created by hds on 17-4-22.
 */

public class HMSRegister extends IntentService implements HuaweiApiClient.OnConnectionFailedListener,
        HuaweiApiClient.ConnectionCallbacks, IPushRegister {
    private HuaweiApiClient client;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * Used to name the worker thread, important only for debugging.
     */
    public HMSRegister() {
        super("HMSRegister");
    }

    @Override
    public void onConnected() {
        AppLogger.d(PUSH_TAG + "华为推送连接成功");
        HuaweiPush.HuaweiPushApi.getToken(client).setResultCallback(result -> {
//此处不需要handle  token,token会在 HuaweiPushReceiver#onToken回调
//            AppLogger.d(PUSH_TAG + "token:" + result.getTokenRes().getToken());
        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        AppLogger.d(PUSH_TAG + "onConnectionSuspended" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        if (result.getErrorCode() == 1) {
            AppLogger.d(PUSH_TAG + "未安装华为推送服务");
        }
        AppLogger.d(PUSH_TAG + "华为推送连接失败:" + result.getErrorCode());
    }

    @Override
    public void registerPMS(Context context) {

    }

    @Override
    public void releasePMS() {
        if (client != null && client.isConnected()) {
            client.disconnect();
            HuaweiId.HuaweiIdApi.signOut(client);
            PendingResult<SignOutResult> signOutResult = HuaweiId.HuaweiIdApi.signOut(client);
            signOutResult.setResultCallback(new ResultCallback<SignOutResult>() {
                @Override
                public void onResult(SignOutResult result) {
                // TODO: 登出结果,处理result.getStatus()
                }
            });
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        AppLogger.d(PUSH_TAG + "正在初始化华为推送SDK");
        client = new HuaweiApiClient.Builder(getApplicationContext())
                .addApi(HuaweiPush.PUSH_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();
    }
}
