package com.cylan.jiafeigou.base.module;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.base.module.provider
 *  @文件名:   JFGObservable
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/7 11:49
 *  @描述：    TODO
 */

public interface JFGObservable<T> {

    void onRefresh(T msg);

    void update();
}
