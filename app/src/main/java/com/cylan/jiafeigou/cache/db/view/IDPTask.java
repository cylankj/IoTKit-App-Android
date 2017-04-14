package com.cylan.jiafeigou.cache.db.view;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;

import rx.Observable;

/**
 * Created by yanzhendong on 2017/3/1.
 */

public interface IDPTask<T extends IDPTaskResult> {

    Observable<T> performLocal();

    Observable<T> performServer();

    void setDBHelper(IDBHelper helper);

    void setSourceManager(JFGSourceManager manager);

    void setPropertyParser(IPropertyParser parser);

    void setAppCmd(AppCmd appCmd);
}
