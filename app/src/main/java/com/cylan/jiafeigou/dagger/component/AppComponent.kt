package com.cylan.jiafeigou.dagger.component

import android.content.Context
import com.cylan.jiafeigou.base.module.IHttpApi
import com.cylan.jiafeigou.dagger.annotation.ContextLife
import com.cylan.jiafeigou.dagger.annotation.Named
import com.cylan.jiafeigou.dagger.module.ActivityModule
import com.cylan.jiafeigou.dagger.module.CommonModule
import com.cylan.jiafeigou.dagger.module.FragmentModule
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.support.badge.TreeHelper
import com.danikula.videocache.HttpProxyCacheServer
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import okhttp3.OkHttpClient
import javax.inject.Singleton


/**
 * Created by yanzhendong on 2017/10/26.
 */
@Singleton
@Component(modules = arrayOf(
        AndroidSupportInjectionModule::class,
        ActivityModule::class,
        FragmentModule::class,
        CommonModule::class
))
interface AppComponent : AndroidInjector<BaseApplication> {

    override fun inject(baseApplication: BaseApplication)
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: BaseApplication): AppComponent.Builder

        fun build(): AppComponent
    }

    @ContextLife()
    fun getAppContext(): Context

    fun getHttpProxyCacheServer(): HttpProxyCacheServer

    fun getOkHttpClient(): OkHttpClient

    @Named("LogPath")
    fun getLogPath(): String

    fun getTreeHelper(): TreeHelper

    fun getHttpApi(): IHttpApi
}