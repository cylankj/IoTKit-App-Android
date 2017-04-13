package com.cylan.jiafeigou.base.injector.module;

import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@Module
public class CommonModule {

    @Provides
    @Singleton
    public JFGSourceManager provideSourceManager() {
        return DataSourceManager.getInstance();
    }

    @Provides
    @Singleton
    public IDBHelper provideDBHelper() {
        return BaseDBHelper.getInstance();
    }

}
