package com.cylan.jiafeigou.n.mvp;

/**
 * Created by hunt on 16-5-14.
 */

import android.support.annotation.NonNull;

import com.cylan.jiafeigou.cache.db.module.Device;

import rx.Subscription;

/**
 * 提供两个最基本的接口,对应Activity,Fragment的生命周期.用于注册或者反注册某些
 * 任务
 */
public interface BasePresenter {

    @NonNull
    Device getDevice();

    String getUuid();

    void start();

    void stop();

    void addSubscription(String tag, Subscription s);
}
