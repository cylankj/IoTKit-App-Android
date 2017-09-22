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
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.push.PushConstant.PUSH_TAG;

/**
 * Created by hds on 17-4-24.
 */
public class BellPuller {

    private static BellPuller instance;
    private String caller;

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
        if (!BaseApplication.getAppComponent().getInitializationManager().isHasInitFinished()) {
            BaseApplication.getAppComponent().getInitializationManager().initAppCmd();
            BaseApplication.getAppComponent().getInitializationManager().initialization();//在这里做初始化
        }
        JFGSourceManager sourceManager = BaseApplication.getAppComponent().getSourceManager();
        System.out.println(PUSH_TAG + "fireBellCalling end" + sourceManager.getAccount());
        final boolean isBellCall = isBellCall(response);
        System.out.println(PUSH_TAG + "push 当前为非登录?" + (sourceManager.getAccount() == null) + "," + response + "," + isBellCall);
        if (!isBellCall) return;
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
                        System.out.println("push,登录成功:" + new Gson().toJson(ret.devices));
                        prepareForBelling(response);
                    }, throwable -> {
                        System.out.println("登录 timeout");
                        System.out.println("收到门铃呼叫推送,但是登录超时?" + MiscUtils.getErr(throwable));
                        //也有可能,本地缓存了该设备.
                        prepareForBelling(response);
                    });
            return;
        }
        //App没有被清理
        prepareForBelling(response);
    }

    private boolean isBellCall(String response) {
        //[16,'500000000385','',1488012270,1]
        if (TextUtils.isEmpty(response)) {
            return false;
        }

        String[] items = response.split(",");
        if (items.length == 0) {
            return false;
        }
        try {
            long msgId = Long.parseLong(items[0].replaceAll("\\D", ""));
            return msgId == 2516;
        } catch (Exception e) {
            return false;
        }
    }

    private void prepareForBelling(String response) {
        //[16,'500000000385','',1488012270,1]


        String[] items = response.split(",");
        String cid = items[1].replace("\'", "");
        long time = Long.parseLong(items[3]);
        JFGSourceManager sourceManager = BaseApplication.getAppComponent().getSourceManager();
        Device device = sourceManager.getDevice(cid);
        System.out.println(PUSH_TAG + "device," + device + "," + device.available());
        if (device == null || !device.available()) {
            System.out.println(PUSH_TAG + "当前列表没有这个设备");
            return;
        }
        String url = items[4].replace("\'", "");
        launchBellLive(cid, url, time);
    }

    public void launchBellLive(String cid, String url, long time) {

        urlMap.remove(cid);
        if (url != null) {
            urlMap.put(cid, url);
        }

        // TODO: 2017/8/30 直播中不允许新的门铃呼叫,这里直接忽略掉
        if (TextUtils.equals(cid, caller)) {
            System.out.println(PUSH_TAG + "直播中不允许新的门铃呼叫,这里直接忽略掉");
            return;
        }

        Long callTime = callTimeMap.get(cid);
        if (callTime != null && Math.abs(time - callTime) < 30) {
            // TODO: 2017/8/30 说明两次呼叫在30秒以内,应该正常情况下一个cid 两次呼叫不可能在
            //30秒以内,则这个呼叫很有可能是 通过其他渠道推送过来的则直接忽略掉即可
            System.out.println(PUSH_TAG + "通过其他渠道推送过来的则直接忽略掉即可");
            return;
        }

        callTimeMap.put(cid, time);

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
        intent.putExtra(JConstant.IS_IN_BACKGROUND, BaseApplication.isBackground());
        ContextUtils.getContext().startActivity(intent);
        System.out.println(PUSH_TAG + "收到华为推送 拉起呼叫界面:");
    }


    private Map<String, String> urlMap = new HashMap<>();

    private Map<String, Long> callTimeMap = new HashMap<>();

    public String getUrl(String cid) {
        return urlMap.get(cid);
    }

    private boolean allowNewBellCall = true;

    public boolean isAllowNewBellCall() {
        return allowNewBellCall;
    }

    public void setAllowNewBellCall(boolean allowNewBellCall) {
        this.allowNewBellCall = allowNewBellCall;
    }

    public void currentCaller(String caller) {
        this.caller = caller;
    }
}
