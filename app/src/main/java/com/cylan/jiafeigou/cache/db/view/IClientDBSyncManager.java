package com.cylan.jiafeigou.cache.db.view;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IClientDBSyncManager {
    void performSyncTask();

    void markSyncNeeded();
}
