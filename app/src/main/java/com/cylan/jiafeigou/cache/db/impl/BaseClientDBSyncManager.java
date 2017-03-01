package com.cylan.jiafeigou.cache.db.impl;

/**
 * Created by yanzhendong on 2017/3/1.
 */


import com.cylan.jiafeigou.cache.db.view.IClientDBSyncManager;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * 用于客户端和服务器之间同步数据
 */
public class BaseClientDBSyncManager implements IClientDBSyncManager {
    private static IClientDBSyncManager instance;
    private boolean needSync = true;

    public static IClientDBSyncManager getInstance() {
        if (instance == null) {
            synchronized (BaseClientDBSyncManager.class) {
                if (instance == null) {
                    instance = new BaseClientDBSyncManager();
                }
            }
        }
        return instance;
    }

    @Override
    public void performSyncTask() {
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
