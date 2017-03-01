package com.cylan.jiafeigou.cache.db.impl;

import com.cylan.jiafeigou.cache.db.module.DBAction;
import com.cylan.jiafeigou.cache.db.module.tasks.DPDeleteTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSharedTask;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTask;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public class BaseDPTaskFactory implements IDPTaskFactory {

    private static IDPTaskFactory instance;

    public static IDPTaskFactory getInstance() {
        if (instance == null) {
            synchronized (BaseDPTaskFactory.class) {
                if (instance == null) {
                    instance = new BaseDPTaskFactory();
                }
            }
        }
        return instance;
    }

    @Override
    public IDPTask getDPTask(IDPEntity cache) {
        if (DBAction.DELETED.name().equals(cache.getTag())) {
            return new DPDeleteTask().init(cache);
        } else if (DBAction.SHARED.name().equals(cache.getTag())) {
            return new DPSharedTask().init(cache);
        }
        return null;
    }
}
