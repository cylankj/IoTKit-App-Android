package com.cylan.jiafeigou.n.engine;

import android.content.Intent;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by yzd on 16-12-23.
 */

public class GlobalBellCallSource {

    private static GlobalBellCallSource sInstance;
    private CompositeSubscription mSubscription;

    public static GlobalBellCallSource getInstance() {
        if (sInstance == null) {
            sInstance = new GlobalBellCallSource();
        }
        return sInstance;
    }

    public void register() {
        if (mSubscription == null) {
            mSubscription = new CompositeSubscription();
        }
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.BellCallEvent.class).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::launchBellLive);
        mSubscription.add(subscribe);


    }

    private void launchBellLive(RxEvent.BellCallEvent bellCallEvent) {
        Intent intent = new Intent(ContextUtils.getContext(), BellLiveActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, bellCallEvent.caller.cid);
        intent.putExtra(JConstant.VIEW_CALL_WAY, JConstant.VIEW_CALL_WAY_LISTEN);
//        intent.putExtra(JConstant.VIEW_CALL_WAY_EXTRA, bellCallEvent.caller.url);
        intent.putExtra(JConstant.VIEW_CALL_WAY_TIME, bellCallEvent.caller.time);
        ContextUtils.getContext().startActivity(intent);

    }

    public void unRegister() {
        if (mSubscription != null && mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }


}
