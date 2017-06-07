package com.cylan.jiafeigou.n.task;

import android.util.Log;

import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.utils.MiscUtils;

import java.util.List;

import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 系统消息读数
 * Created by hds on 17-6-7.
 */

public class SystemMsgTask implements Action1<Object> {
    @Override
    public void call(Object o) {
        List<IDPEntity> idpEntities = new MiscUtils.DPEntityBuilder()
                .add(DBAction.SIMPLE_MULTI_QUERY, "", 1101L, 0, true)
                .add(DBAction.SIMPLE_MULTI_QUERY, "", 1103L, 0, true)
                .add(DBAction.SIMPLE_MULTI_QUERY, "", 1104L, 0, true)
                .build();
        BaseApplication.getAppComponent().getTaskDispatcher().perform(idpEntities)
                .subscribeOn(Schedulers.newThread())
                .subscribe(baseDPTaskResult -> {
                    Log.d("SystemMsgTask", "SystemMsgTask" + baseDPTaskResult);
                });
    }
}
