package com.cylan.jiafeigou.base.injector.module;

import android.content.Context;

import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.danikula.videocache.HttpProxyCacheServer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@Module
public class AppModule {
    private BaseApplication application;

    public AppModule(BaseApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public HttpProxyCacheServer provideHttpProxyCacheServer(@ContextLife Context context) {
        return new HttpProxyCacheServer.Builder(context).maxCacheSize(Long.MAX_VALUE).maxCacheFilesCount(Integer.MAX_VALUE).build();
    }

    @Provides
    @ContextLife
    @Singleton
    public Context provideAppContext() {
        return application;
    }

    @Provides
    public JFGPresenter provideDefaultPresenter() {
        return new BasePresenter() {
        };
    }
}
