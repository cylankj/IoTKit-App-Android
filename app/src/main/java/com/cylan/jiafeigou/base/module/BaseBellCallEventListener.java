package com.cylan.jiafeigou.base.module;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/4/14.
 */
@Singleton
public class BaseBellCallEventListener {
    private Context appContext;
    private Map<String, String> urlMap = new HashMap<>();
    private static BaseBellCallEventListener instance;
    private String caller = null;

    public static BaseBellCallEventListener getInstance() {
        if (instance == null) {
            synchronized (BaseBellCallEventListener.class) {
                if (instance == null) {
                    instance = new BaseBellCallEventListener(ContextUtils.getContext());
                }
            }
        }
        return instance;
    }

    public BaseBellCallEventListener() {
        instance = this;
    }

    @Inject
    public BaseBellCallEventListener(@ContextLife Context appContext) {
        this.appContext = appContext;
        instance = this;
    }

    public Subscription initSubscription() {
        return RxBus.getCacheInstance().toObservable(RxEvent.BellCallEvent.class)
//                .groupBy(ret -> ret.caller.cid)//通过cid来分组,但是有个bug.如果局域网的先来,公网的截图就收不到.
//                .flatMap(groupResult -> groupResult.throttleFirst(25, TimeUnit.SECONDS)
//                        .map(result -> result))
                .subscribeOn(Schedulers.io())
                .retry((i, e) -> true)
                .subscribe(this::makeNewCall, AppLogger::e);
    }

    private void makeNewCall(RxEvent.BellCallEvent callEvent) {
        //2、在直播界面查看直播时不拉起呼叫页面，在查看历史录像时拉起呼叫页面。
        if (TextUtils.equals(callEvent.caller.cid, caller)) return;
        Intent intent = new Intent(ContextUtils.getContext(), BellLiveActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
//                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
//                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, callEvent.caller.cid);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_LISTEN);
        intent.putExtra(JConstant.VIEW_CALL_WAY_TIME, callEvent.caller.time);
        urlMap.remove(callEvent.caller.cid);
        if (!callEvent.isFromLocal) {
            try {
                AppLogger.d("门铃呼叫 CID:" + callEvent.caller.cid + ",门铃呼叫时间:" + callEvent.caller.time);
                String url = new JFGGlideURL(callEvent.caller.cid, callEvent.caller.time + ".jpg", callEvent.caller.regionType).toURL().toString();
                intent.putExtra(JConstant.VIEW_CALL_WAY_EXTRA, url);
                urlMap.put(callEvent.caller.cid, url);
                AppLogger.d("门铃截图地址:" + url);
            } catch (MalformedURLException e) {
                AppLogger.e(e);
            }
        }
        appContext.startActivity(intent);
    }

    public String getUrl(String cid) {
        return urlMap.get(cid);
    }

    public void currentCaller(String caller) {
        this.caller = caller;
    }

}
