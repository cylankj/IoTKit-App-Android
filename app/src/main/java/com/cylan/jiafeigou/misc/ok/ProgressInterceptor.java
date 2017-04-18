package com.cylan.jiafeigou.misc.ok;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by hds on 17-4-18.
 */

public class ProgressInterceptor implements Interceptor {
    // private ProgressListener progressListener;


  /* public ProgressInterceptor(ProgressListener progressListener) { this.progressListener = progressListener; }*/

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder().body(new ProgressResponseBody(originalResponse.body())).build();
    }
}
