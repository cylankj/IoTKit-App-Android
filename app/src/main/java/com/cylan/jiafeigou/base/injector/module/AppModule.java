package com.cylan.jiafeigou.base.injector.module;

import android.content.Context;

import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.BasePresenterInjector;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.danikula.videocache.HttpProxyCacheServer;

import java.io.File;
import java.util.List;

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
                .cacheDirectory(new File(JConstant.MEDIA_PATH))
                .maxCacheFilesCount(Integer.MAX_VALUE)
                .fileNameGenerator(url -> {
                    if (url != null && !url.startsWith("http://"))
                        url = "http://www.baidu.com" + url;
                    List<String> strings = HttpUrl.parse(url).pathSegments();
                    String filePath = BasePanoramaApiHelper.getInstance().getFilePath(strings.get(strings.size() - 1));
                    AppLogger.d("HttpProxyCacheServer" + filePath);
                    return filePath;
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
