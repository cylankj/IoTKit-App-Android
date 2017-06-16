package com.cylan.jiafeigou.base.injector.module;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.base.module.BaseAppCallBackHolder;
import com.cylan.jiafeigou.base.module.BaseDeviceInformationFetcher;
import com.cylan.jiafeigou.base.module.BasePresenterInjector;
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
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.pty.IProperty;
import com.cylan.jiafeigou.misc.pty.PropertiesLoader;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.toolsfinal.io.Charsets;
import com.cylan.jiafeigou.utils.PathGetter;
import com.google.gson.Gson;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@Module
public class CommonModule {

    @Provides
    @Singleton
    public static JFGSourceManager provideSourceManager() {
        return new DataSourceManager();
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
    public static BasePresenterInjector provideBasePresenterInject(JFGSourceManager manager, IDPTaskDispatcher dispatcher, AppCmd appCmd) {
        return new BasePresenterInjector(manager, dispatcher, appCmd);
    }

    @Provides
    @Singleton
    public static BaseAppCallBackHolder provideAppCallBackHolder() {
        return new BaseAppCallBackHolder();
    }

    @Provides
    @Singleton
    public static AppCmd provideAppCmd() {
        return JfgAppCmd.getInstance();
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
                    String string = proceed.body().string();
                    AppLogger.e("http 请求返回的结果:" + new Gson().toJson(string));
                    return proceed.newBuilder().body(new RealResponseBody(proceed.headers(), new Buffer().writeString(string, Charsets.UTF_8))).build();
                })
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
