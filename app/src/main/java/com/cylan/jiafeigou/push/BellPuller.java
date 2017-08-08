package com.cylan.jiafeigou.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.push.PushConstant.PUSH_TAG;

/**
 * Created by hds on 17-4-24.
 */

public class BellPuller {

    private static BellPuller instance;

    private BellPuller() {
    }

    public static BellPuller getInstance() {
        if (instance == null) {
            synchronized (BellPuller.class) {
                if (instance == null)
                    instance = new BellPuller();
            }
        }
        return instance;
    }

    /**
     * 现在从收到 push小到拉起门铃页面可能有15s,这么久.
     *
     * @param response
     * @param bundle
     */
    public void fireBellCalling(Context context, String response, Bundle bundle) {
        System.out.println(PUSH_TAG + "fireBellCalling start:" + context.getPackageName());
        BaseApplication.getAppComponent().getInitializationManager().initialization();//在这里做初始化
        JFGSourceManager sourceManager = BaseApplication.getAppComponent().getSourceManager();
        System.out.println(PUSH_TAG + "fireBellCalling end" + sourceManager.getAccount());
        AppLogger.d(PUSH_TAG + "push 当前为非登录?" + (sourceManager.getAccount() == null) + "," + response);
        if (sourceManager.getAccount() == null || TextUtils.isEmpty(sourceManager.getAccount().getAccount())) {
            //表明没有登录,这种情况比较多{1.登出,2.App正常离线,3.反正就是处于后台,系统管控着}
            Observable.just(response)
                    .subscribeOn(Schedulers.io())
                    .map(ret -> {
                        System.out.println(PUSH_TAG + "autoLogin start");
                        AutoSignIn.getInstance().autoLogin();
                        System.out.println(PUSH_TAG + "autoLogin end");
                        return ret;
                    })
                    .timeout(3, TimeUnit.SECONDS)
                    .flatMap(s -> RxBus.getCacheInstance().toObservable(RxEvent.DevicesArrived.class))
                    .subscribe(ret -> {
                        System.out.println("登录成功");
                        AppLogger.d("push,登录成功:" + new Gson().toJson(ret.devices));
                        prepareForBelling(response);
                    }, throwable -> {
                        System.out.println("登录 timeout");
                        AppLogger.e("收到门铃呼叫推送,但是登录超时?" + MiscUtils.getErr(throwable));
                        //也有可能,本地缓存了该设备.
                        prepareForBelling(response);
                    });
            return;
        }
        //App没有被清理
        prepareForBelling(response);
    }

    private void prepareForBelling(String response) {
        //[16,'500000000385','',1488012270,1]
        if (TextUtils.isEmpty(response)) {
            return;
        }

        String[] items = response.split(",");
        if (items.length != 5) {
            return;
        }

        String cid = items[1].replace("\'", "");
        long time = Long.parseLong(items[3]);
        JFGSourceManager sourceManager = BaseApplication.getAppComponent().getSourceManager();
        Device device = sourceManager.getDevice(cid);
        System.out.println(PUSH_TAG + "device," + device + "," + device.available());
        if (device == null || !device.available()) {
            AppLogger.d(PUSH_TAG + "当前列表没有这个设备");
            return;
        }
        String url = null;
        try {
            url = new JFGGlideURL(cid, time + ".jpg", device.regionType).toURL().toString();
            AppLogger.d(PUSH_TAG + "门铃截图地址:" + url);
        } catch (MalformedURLException e) {
            AppLogger.e(PUSH_TAG + "err:" + MiscUtils.getErr(e));

        }
        System.out.println(PUSH_TAG + "time," + PreferencesUtils.getInt(JConstant.KEY_NTP_INTERVAL));
        if (System.currentTimeMillis() / 1000L - PreferencesUtils.getInt(JConstant.KEY_NTP_INTERVAL) - time < 30) {
            launchBellLive(cid, url, time);
        }
    }

    private void launchBellLive(String cid, String url, long time) {
        Intent intent = new Intent(ContextUtils.getContext(), BellLiveActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, cid);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_LISTEN);
        intent.putExtra(JConstant.VIEW_CALL_WAY_EXTRA, url);
        intent.putExtra(JConstant.VIEW_CALL_WAY_TIME, time);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);//华为服务使用.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ContextUtils.getContext().startActivity(intent);
        AppLogger.e(PUSH_TAG + "收到华为推送 拉起呼叫界面:");
    }
}
