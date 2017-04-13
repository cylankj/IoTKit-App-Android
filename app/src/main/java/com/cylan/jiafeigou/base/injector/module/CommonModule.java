package com.cylan.jiafeigou.base.injector.module;

import com.cylan.jiafeigou.base.module.BasePresenterInjector;
import com.cylan.jiafeigou.base.module.BasePropertyParser;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskFactory;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;

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
        return new DataSourceManager();
    }

    @Provides
    @Singleton
    public IDBHelper provideDBHelper() {
        return new BaseDBHelper();
    }

    @Provides
    @Singleton
    public IDPTaskDispatcher provideTaskDispatcher() {
        return new BaseDPTaskDispatcher();
    }

    @Provides
    @Singleton
    public IDPTaskFactory provideTaskFactory() {
        return new BaseDPTaskFactory();
    }

    @Provides
    @Singleton
    public IPropertyParser providePropertyParser() {
        return new BasePropertyParser();
    }

    @Provides
    @Singleton
    public BasePresenterInjector provideBasePresenterInject(JFGSourceManager manager, IDPTaskDispatcher dispatcher) {
        return new BasePresenterInjector(manager, dispatcher);
    }

}
