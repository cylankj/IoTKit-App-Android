package com.cylan.jiafeigou.n.presenter;

/**
 * Created by hunt on 16-5-14.
 */

/**
 * 提供两个最基本的接口,对应Activity,Fragment的生命周期.用于注册或者反注册某些
 * 任务
 */
public interface BasePresenter {

    void start();

    void stop();
}
