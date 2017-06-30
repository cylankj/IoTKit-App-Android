package com.cylan.jiafeigou.base.module;

import android.content.Context;
import android.content.Intent;

import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

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

    @Inject
    public BaseBellCallEventListener(@ContextLife Context appContext) {
        this.appContext = appContext;
    }

    public Subscription initSubscription() {
        return RxBus.getCacheInstance().toObservable(RxEvent.BellCallEvent.class)
                .groupBy(ret -> ret.caller.cid)//通过cid来分组,但是有个bug.如果局域网的先来,公网的截图就收不到.
                .flatMap(groupResult -> groupResult.throttleFirst(25, TimeUnit.SECONDS)
                        .map(result -> result))
                .subscribeOn(Schedulers.io())
                .retry((i, e) -> true)
                .subscribe(this::makeNewCall, AppLogger::e);
    }

    private void makeNewCall(RxEvent.BellCallEvent callEvent) {
        Intent intent = new Intent(ContextUtils.getContext(), BellLiveActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, callEvent.caller.cid);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_LISTEN);
        intent.putExtra(JConstant.VIEW_CALL_WAY_TIME, callEvent.caller.time);
        if (!callEvent.isFromLocal) {
            try {
                AppLogger.d("门铃呼叫 CID:" + callEvent.caller.cid + ",门铃呼叫时间:" + callEvent.caller.time);
                String url = new JFGGlideURL(callEvent.caller.cid, callEvent.caller.time + ".jpg", callEvent.caller.regionType).toURL().toString();
                intent.putExtra(JConstant.VIEW_CALL_WAY_EXTRA, url);
                AppLogger.d("门铃截图地址:" + url);
            } catch (MalformedURLException e) {
                AppLogger.e(e);
            }
        }
        appContext.startActivity(intent);
    }
}
