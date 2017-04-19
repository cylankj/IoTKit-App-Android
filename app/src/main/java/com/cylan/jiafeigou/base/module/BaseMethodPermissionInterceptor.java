package com.cylan.jiafeigou.base.module;

import android.content.Context;

import com.cylan.jiafeigou.base.injector.lifecycle.ContextLife;

import javax.inject.Inject;

/**
 * Created by yanzhendong on 2017/4/19.
 */
//@Aspect
//@Singleton
public class BaseMethodPermissionInterceptor {
    private Context appContext;

    @Inject
    public BaseMethodPermissionInterceptor(@ContextLife Context appContext) {

    }

//    @Pointcut("@annotation(com.cylan.jiafeigou.base.interceptor.annotation.NeedConnectToDogWiFi)")
//    public void connectToDogWiFiPointcut() {
//
//    }
//
//    @Around("connectToDogWiFiPointcut")
//    public void isConnectToDogWiFi(ProceedingJoinPoint joinPoint) throws Throwable {
//        ConnectivityManager manager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();
////        joinPoint.proceed();
//
//    }


}
