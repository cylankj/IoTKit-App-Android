package com.cylan.jiafeigou.base.injector.component;

import android.content.Context;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.base.injector.module.AppModule;
import com.cylan.jiafeigou.base.injector.module.CommonModule;
import com.cylan.jiafeigou.base.module.BaseInitializationManager;
import com.cylan.jiafeigou.base.module.BasePresenterInjector;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.danikula.videocache.HttpProxyCacheServer;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@Singleton
@Component(modules = {AppModule.class, CommonModule.class})
public interface AppComponent {

    @ContextLife()
    Context getAppContext();

    JFGPresenter getDefaultPresenter();

    JFGSourceManager getSourceManager();

    IDBHelper getDBHelper();

    IDPTaskDispatcher getTaskDispatcher();

    IPropertyParser getPropertyParser();

    BasePresenterInjector getBasePresenterInjector();

    BaseInitializationManager getInitializationManager();

    AppCmd getCmd();

    HttpProxyCacheServer getHttpProxyCacheServer();


}
