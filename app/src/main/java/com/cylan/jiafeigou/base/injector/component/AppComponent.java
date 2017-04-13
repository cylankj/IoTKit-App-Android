package com.cylan.jiafeigou.base.injector.component;

import android.content.Context;

import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.base.injector.module.AppModule;
import com.cylan.jiafeigou.base.injector.module.CommonModule;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;

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

}
