package com.cylan.jiafeigou.base.injector.module;

import android.content.Context;

import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.base.module.BasePresenterInjector;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.danikula.videocache.HttpProxyCacheServer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@Module
public class AppModule {
    private static BaseApplication application;

    public AppModule(BaseApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public static HttpProxyCacheServer provideHttpProxyCacheServer(@ContextLife Context context) {
        return new HttpProxyCacheServer
                .Builder(context)
                .maxCacheSize(Long.MAX_VALUE)
                .maxCacheFilesCount(Integer.MAX_VALUE)
                .fileNameGenerator(url -> {
                    String encodedPath = HttpUrl.parse(url).encodedPath();
                    AppLogger.d("HttpProxyCacheServer" + encodedPath);
                    return encodedPath;
                })
                .build();
    }

    @Provides
    @ContextLife
    @Singleton
    public static Context provideAppContext() {
        return application;
    }

    @Provides
    public static JFGPresenter provideDefaultPresenter(BasePresenterInjector injector) {
        return injector.inject(new BasePresenter() {
        });
    }
}
