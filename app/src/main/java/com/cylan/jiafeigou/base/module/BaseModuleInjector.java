package com.cylan.jiafeigou.base.module;

import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by yanzhendong on 2017/4/13.
 */
@Singleton
public class BaseModuleInjector {
    @Inject
    public BaseModuleInjector(JFGSourceManager manager,
                              IDBHelper helper,
                              IPropertyParser parser,
                              IDPTaskFactory factory,
                              IDPTaskDispatcher dispatcher) {

        manager.setPropertyParser(parser);
        manager.setDBHelper(helper);
        helper.setDataSourceManager(manager);
        helper.setPropertyParser(parser);
        dispatcher.setPropertyParser(parser);
        dispatcher.setDBHelper(helper);
        dispatcher.setSourceManager(manager);
        dispatcher.setTaskFactory(factory);
    }
}
