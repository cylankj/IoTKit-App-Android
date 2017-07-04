package com.cylan.jiafeigou.base.module;

import java.util.List;

import rx.Observable;

/**
 * Created by hds on 17-7-3.
 */

public interface IManager<T, TASK> {

    Observable<List<T>> getNewList();

    Observable<Iterable<T>> saveToCache(List<T> arrayList);

    Observable<Void> deleteCache(List<T> arrayList);

    Observable<Void> deleteAllCache();

    TASK getTask(long key);

    void submitTask(TASK task);

}