package com.cylan.jiafeigou.dagger.module;

import android.content.Context;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.module.BasePropertyParser;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.DeviceInformation;
import com.cylan.jiafeigou.base.module.IHttpApi;
import com.cylan.jiafeigou.base.view.IPropertyParser;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskFactory;
import com.cylan.jiafeigou.cache.db.view.IDBHelper;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskFactory;
import com.cylan.jiafeigou.dagger.annotation.ContextLife;
import com.cylan.jiafeigou.dagger.annotation.Named;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.pty.IProperty;
import com.cylan.jiafeigou.misc.pty.PropertiesLoader;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.module.ILoadingManager;
import com.cylan.jiafeigou.module.LoadingManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PathGetter;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.gson.Gson;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yanzhendong on 2017/10/26.
 */
@Module
public abstract class CommonModule {

    @Provides
    @Singleton
    public static HttpProxyCacheServer provideHttpProxyCacheServer(@ContextLife Context context) {
        return new HttpProxyCacheServer
                .Builder(context)
                .maxCacheSize(Long.MAX_VALUE)
                .cacheDirectory(new File(JConstant.MEDIA_PATH))
                .maxCacheFilesCount(Integer.MAX_VALUE)
                .fileNameGenerator(url -> {
                    if (url != null && !url.startsWith("http://")) {
                        url = "http://www.baidu.com" + url;
                    }
                    List<String> strings = HttpUrl.parse(url).pathSegments();
                    String filePath = BasePanoramaApiHelper.getInstance().getFilePath("", strings.get(strings.size() - 1));
                    AppLogger.d("HttpProxyCacheServer" + filePath);
                    return filePath;
                })
                .build();
    }

    @Binds
    @Singleton
    @ContextLife
    public abstract Context provideApplicationContext(BaseApplication application);

    @Binds
    @Singleton
    public abstract ILoadingManager bindLoadingManager(LoadingManager loadingManager);

    @Provides
    @Singleton
    public static JFGSourceManager provideSourceManager() {
        return DataSourceManager.getInstance();
    }

    @Provides
    @Singleton
    public static IDBHelper provideDBHelper() {
        return new BaseDBHelper();
    }

    @Provides
    @Singleton
    public static IDPTaskDispatcher provideTaskDispatcher() {
        return new BaseDPTaskDispatcher();
    }

    @Provides
    @Singleton
    public static IDPTaskFactory provideTaskFactory() {
        return new BaseDPTaskFactory();
    }

    @Provides
    @Singleton
    public static IPropertyParser providePropertyParser() {
        return new BasePropertyParser();
    }

    @Provides
    @Singleton
    public static AppCmd provideAppCmd() {
        return Command.getInstance();
    }

    @Provides
    @Singleton
    @Named("VKey")
    public static String provideVKey() {
        return Security.getVKey();
    }

    @Provides
    @Singleton
    @Named("Vid")
    public static String provideVid() {
        return Security.getVId();
    }

    @Provides
    @Singleton
    @Named("ServerAddress")
    public static String provideServerAddress() {
        return OptionsImpl.getServer();
    }

    @Provides
    @Singleton
    @Named("LogPath")
    public static String provideLogPath() {
        return JConstant.WORKER_PATH;
    }

    @Provides
    @Singleton
    @Named("CrashPath")
    public static String provideCrashPath() {
        return PathGetter.createPath(JConstant.CRASH_PATH);
    }

    @Provides
    @Singleton
    public static OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    DeviceInformation information = BaseDeviceInformationFetcher.getInstance().getDeviceInformation();
                    if (information != null && information.ip != null) {
                        //动态 host
                        HttpUrl httpUrl = chain.request().url().newBuilder().host("http://" + information.ip).build();
                        request = request.newBuilder().url(httpUrl).build();
                    }
                    AppLogger.e("http请求为:" + request.toString());
                    Response proceed = chain.proceed(request);
                    ResponseBody body = proceed.body();
                    String string = body.string();
                    AppLogger.e("http 请求返回的结果:" + new Gson().toJson(string));
                    return proceed.newBuilder().body(new RealResponseBody(body.contentType().toString(), body.contentLength(), new Buffer().writeString(string, Charset.forName("UTF-8")))).build();
                })
                .connectTimeout(120, TimeUnit.SECONDS)//sd 卡格式化需要120 秒的超时
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    public static IProperty getProductProperty() {
        return new PropertiesLoader();
    }

    @Provides
    @Singleton
    public static TreeHelper getTreeHelper() {
        return new TreeHelper();
    }

    @Provides
    @Singleton
    public static IHttpApi provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder().client(okHttpClient)
                .baseUrl("http://192.168.10.2/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(IHttpApi.class);
    }
}
