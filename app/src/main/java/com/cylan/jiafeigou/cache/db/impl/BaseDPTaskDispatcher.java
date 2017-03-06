package com.cylan.jiafeigou.cache.db.impl;

/**
 * Created by yanzhendong on 2017/3/1.
 */


import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * 用于客户端和服务器之间的数据交互,query task 在无网络时将被丢弃
 * 其他 task
 */
public class BaseDPTaskDispatcher implements IDPTaskDispatcher {
    private static IDPTaskDispatcher instance;
    private IDPTaskFactory mTaskFactory;

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

    public BaseDPTaskDispatcher() {
        mTaskFactory = BaseDPTaskFactory.getInstance();
    }

    @Override
    public synchronized void perform() {
        BaseDBHelper.getInstance().queryUnConfirmDpMsg(null, null)
                .observeOn(Schedulers.io())
                .flatMap(Observable::from)
                .subscribe(new Subscriber<DPEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onStart() {
                        request(1);
                    }

                    @Override
                    public void onNext(DPEntity entity) {
                        BaseDPTaskFactory.getInstance().getTask(entity.action(), false, entity).performServer().subscribe(result -> request(1), e -> request(1));
                    }
                });
    }

    @Override
    public Observable<IDPTaskResult> perform(IDPEntity entity) {
        return Observable.just(mTaskFactory.getTask(entity.action(), false, entity))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(task -> DataSourceManager.getInstance().isOnline()
                        ? task.performLocal().observeOn(Schedulers.io()).flatMap(result -> task.performServer())
                        : task.performLocal().observeOn(Schedulers.io())
                );
    }

    @Override
    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entities) {
        return Observable.just(mTaskFactory.getTask(entities.get(0).action(), true, entities))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(task -> DataSourceManager.getInstance().isOnline()
                        ? task.performLocal().observeOn(Schedulers.io()).flatMap(result -> task.performServer())
                        : task.performLocal().observeOn(Schedulers.io())
                );
    }
}
