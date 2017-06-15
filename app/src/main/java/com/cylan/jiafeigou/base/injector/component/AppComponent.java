package com.cylan.jiafeigou.base.injector.component;

import android.content.Context;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.base.injector.module.AppModule;
import com.cylan.jiafeigou.base.injector.module.CommonModule;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.BaseInitializationManager;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.BasePresenterInjector;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.misc.pty.IProperty;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.danikula.videocache.HttpProxyCacheServer;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import okhttp3.OkHttpClient;

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

    BasePanoramaApiHelper getHttpApiHelper();

    BaseDeviceInformationFetcher getDeviceInformationFetcher();

    OkHttpClient getOkHttpClient();

    IProperty getProductProperty();

    @Named("LogPath")
    String getLogPath();

    TreeHelper getTreeHelper();
}
