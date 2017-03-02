package com.cylan.jiafeigou.cache.db.impl;

/**
 * Created by yanzhendong on 2017/3/1.
 */


import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 用于客户端和服务器之间的数据交互,query task 在无网络时将被丢弃
 * 其他 task
 */
public class BaseDPTaskDispatcher implements IDPTaskDispatcher {
    private static IDPTaskDispatcher instance;
    private boolean needSync = true;

    public static IDPTaskDispatcher getInstance() {
        if (instance == null) {
            synchronized (BaseDPTaskDispatcher.class) {
                if (instance == null) {
                    instance = new BaseDPTaskDispatcher();
                }
            }
        }
        return instance;
    }

    @Override
    public synchronized void perform() {
        Observable.just(needSync).filter(start -> start)
                .flatMap(start -> BaseDPHelper.getInstance().queryUnConfirmDpMsg(null, null))
                .observeOn(Schedulers.io())
                .flatMap(Observable::from)
                .flatMap(cache -> BaseDPTaskFactory.getInstance().getDPTask(cache).execute())
                .subscribe();
    }

    @Override
    public void markSyncNeeded() {
        needSync = true;
    }
}
