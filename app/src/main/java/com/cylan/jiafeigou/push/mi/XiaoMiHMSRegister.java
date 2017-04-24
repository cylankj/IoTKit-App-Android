package com.cylan.jiafeigou.push.mi;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.cylan.jiafeigou.push.IPushRegister;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * Created by hds on 17-4-22.
 * 1、为了打开客户端的日志，便于在开发过程中调试，需要自定义一个 Application。
 * 并将自定义的 application 注册在 AndroidManifest.xml 文件中。<br/>
 * 2、为了提高 push 的注册率，您可以在 Application 的 onCreate 中初始化 push。你也可以根据需要，在其他地方初始化 push。
 *
 * @author wangkuiwei
 */
public class XiaoMiHMSRegister extends IntentService implements IPushRegister {


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p>
     * Used to name the worker thread, important only for debugging.
     */
    public XiaoMiHMSRegister() {
        super("XiaoMiHMSRegister");
    }


    @Override
    public void registerPMS(Context context) {

    }

    @Override
    public void releasePMS() {

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String AppId = PackageUtils.getMetaString(getApplicationContext(), "XiaoMiAppId");
        String AppKey = PackageUtils.getMetaString(getApplicationContext(), "XiaoMiAppKey");
        //简单粗暴,只要一行代码即可
        // 注册push服务，注册成功后会向xxxMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        MiPushClient.registerPush(this, AppId, AppKey);
    }
}
