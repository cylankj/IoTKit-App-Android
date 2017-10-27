package com.cylan.jiafeigou.dagger.component

import android.content.Context
import com.cylan.jfgapp.interfases.AppCmd
import com.cylan.jiafeigou.base.module.*
import com.cylan.jiafeigou.base.view.IPropertyParser
import com.cylan.jiafeigou.base.view.JFGSourceManager
import com.cylan.jiafeigou.cache.db.view.IDBHelper
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher
import com.cylan.jiafeigou.dagger.annotation.ContextLife
import com.cylan.jiafeigou.dagger.annotation.Named
import com.cylan.jiafeigou.dagger.module.ActivityModule
import com.cylan.jiafeigou.dagger.module.CommonModule
import com.cylan.jiafeigou.dagger.module.FragmentModule
import com.cylan.jiafeigou.misc.pty.IProperty
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

    fun getSourceManager(): JFGSourceManager

    fun getDBHelper(): IDBHelper


    fun getTaskDispatcher(): IDPTaskDispatcher

    fun getPropertyParser(): IPropertyParser

    fun getBasePresenterInjector(): BasePresenterInjector

    fun getInitializationManager(): BaseInitializationManager

    fun getCmd(): AppCmd

    fun getHttpProxyCacheServer(): HttpProxyCacheServer

    fun getHttpApiHelper(): BasePanoramaApiHelper

    fun getDeviceInformationFetcher(): BaseDeviceInformationFetcher

    fun getOkHttpClient(): OkHttpClient

    fun getProductProperty(): IProperty

    @Named("LogPath")
    fun getLogPath(): String

    fun getTreeHelper(): TreeHelper

    fun getHttpApi(): IHttpApi

}