package com.cylan.jiafeigou.cache.db.impl;

import com.cylan.jiafeigou.cache.db.module.tasks.DPMultiDeleteTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSingleClearTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSingleDeleteTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSingleQueryTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSingleSharedTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPUnBindDeviceTask;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPMultiTask;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.cache.db.view.IDPTask;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;

import java.util.List;


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
    public IDPTask getTask(DBAction action, boolean multi, Object initValue) {
        if (multi) {
            return getMultiTask(action).init((List<IDPEntity>) initValue);
        } else {
            return getSingleTask(action).init((IDPEntity) initValue);
        }
    }

    private IDPSingleTask getSingleTask(DBAction action) {
        IDPSingleTask result = null;
        switch (action) {
            case DELETED:
                result = new DPSingleDeleteTask();
                break;
            case SHARED:
                result = new DPSingleSharedTask();
                break;
            case QUERY:
                result = new DPSingleQueryTask();
                break;
            case CLEARED:
                result = new DPSingleClearTask();
                break;
            case UNBIND:
                result = new DPUnBindDeviceTask();
        }
        return result;
    }

    private IDPMultiTask getMultiTask(DBAction action) {
        if (DBAction.DELETED == action) {
            return new DPMultiDeleteTask();
        }
        return null;
    }

}
