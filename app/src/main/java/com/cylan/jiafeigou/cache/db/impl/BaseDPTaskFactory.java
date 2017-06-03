package com.cylan.jiafeigou.cache.db.impl;

import com.cylan.jiafeigou.cache.db.module.tasks.DPCamDateQueryTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPCamMultiQueryTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPMultiDeleteTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSimpleMultiQueryTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSingleClearTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSingleDeleteTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSingleQueryTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSingleShareH5Task;
import com.cylan.jiafeigou.cache.db.module.tasks.DPSingleSharedTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPUnBindDeviceTask;
import com.cylan.jiafeigou.cache.db.module.tasks.DPUpdateTask;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPMultiTask;
import com.cylan.jiafeigou.cache.db.view.IDPSingleTask;
import com.cylan.jiafeigou.cache.db.view.IDPTask;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.List;


/**
 * Created by yanzhendong on 2017/3/1.
 */

public class BaseDPTaskFactory implements IDPTaskFactory {
    @Override
    public IDPTask getTask(DBAction action, boolean multi, Object initValue) {
        try {
            if (multi) {
                return getMultiTask(action).init((List<IDPEntity>) initValue);
            } else {
                return getSingleTask(action).init((IDPEntity) initValue);
            }
        } catch (Exception e) {
            AppLogger.e(e.getMessage());
        }
        return null;
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
                break;
            case CAM_DATE_QUERY:
                result = new DPCamDateQueryTask();
                break;
            case SHARE_H5:
                result = new DPSingleShareH5Task();
        }
        return result;
    }

    private IDPMultiTask getMultiTask(DBAction action) {
        if (DBAction.DELETED == action) {
            return new DPMultiDeleteTask();
        } else if (DBAction.CAM_MULTI_QUERY == action) {
            return new DPCamMultiQueryTask();
        } else if (DBAction.MULTI_UPDATE == action) {
            return new DPUpdateTask();
        } else if (DBAction.SIMPLE_MULTI_QUERY == action) {
            return new DPSimpleMultiQueryTask();
        }
        return null;
    }

}
