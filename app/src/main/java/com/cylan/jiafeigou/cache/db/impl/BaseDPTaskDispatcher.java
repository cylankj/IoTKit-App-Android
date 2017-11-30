package com.cylan.jiafeigou.cache.db.impl;

/**
 * Created by yanzhendong on 2017/3/1.
 */


import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTask;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * 用于客户端和服务器之间的数据交互
 *
 * @deprecated 没有灵活性 ,直接使用 appCmd
 */
public class BaseDPTaskDispatcher implements IDPTaskDispatcher {

    private static BaseDPTaskDispatcher instance;

    public static BaseDPTaskDispatcher getInstance() {
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
                        IDPTask<IDPTaskResult> task = BaseDPTaskFactory.getInstance().getTask(entity.action(), false, entity);
                        if (task != null) {
                            task.performServer().subscribe(result -> request(1), e -> {
                                AppLogger.e(e.getMessage());
                                e.printStackTrace();
                            });
                        } else {
                            request(1);
                        }
                    }
                });
    }

    @Override
    public Observable<IDPTaskResult> perform(IDPEntity entity) {
        return Observable.just(BaseDPTaskFactory.getInstance().getTask(entity.action(), false, entity))
                .filter(task -> task != null)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(task -> DataSourceManager.getInstance().isOnline()
                        ? task.performLocal().observeOn(Schedulers.io()).flatMap(ret -> task.performServer())
                        : task.performLocal().observeOn(Schedulers.io())
                );
    }

    @Override
    public Observable<IDPTaskResult> perform(List<? extends IDPEntity> entities) {
        if (DataSourceManager.getInstance().getAccount() == null) {
            return Observable.just(BaseDPTaskResult.SUCCESS);
        }
        return Observable.just(BaseDPTaskFactory.getInstance().getTask(entities.get(0).action(), true, entities))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .filter(task -> task != null)
                .flatMap(task -> DataSourceManager.getInstance().isOnline()
                        ? task.performServer()
                        : task.performLocal().subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                );
    }
}


