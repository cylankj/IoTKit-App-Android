package com.cylan.jiafeigou.base.module;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yanzhendong on 2017/5/8.
 */

public class BaseHttpApiHelper {
    private Retrofit retrofit;
    private IHttpApi httpApi;
    private static final BaseHttpApiHelper instance = new BaseHttpApiHelper();

    public static BaseHttpApiHelper getInstance() {
        return instance;
    }

    public IHttpApi getHttpApi(String baseUrl) {
        if (retrofit == null || !TextUtils.equals(baseUrl, retrofit.baseUrl().url().toString())) {
            synchronized (this) {
                if (retrofit == null || !TextUtils.equals(baseUrl, retrofit.baseUrl().url().toString())) {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(new Interceptor() {
                                @Override
                                public Response intercept(Chain chain) throws IOException {
                                    Request request = chain.request();
                                    Response proceed = chain.proceed(request);
                                    return proceed;
                                }
                            })
                            .build();
                    retrofit = new Retrofit.Builder()
                            .baseUrl(baseUrl)
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .addConverterFactory(ImageFileConverterFactory.create())
                            .client(client)
                            .build();
                    httpApi = retrofit.create(IHttpApi.class);
                }
            }
        }
        return httpApi;
    }
}
