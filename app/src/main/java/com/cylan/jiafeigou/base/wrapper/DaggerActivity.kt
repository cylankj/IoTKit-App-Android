package com.cylan.jiafeigou.base.wrapper

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.cylan.jiafeigou.dagger.Injectable
import com.cylan.jiafeigou.support.log.AppLogger
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import dagger.android.support.HasSupportFragmentInjector
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/10/26.
 */
abstract class DaggerActivity : AppCompatActivity(), HasFragmentInjector, HasSupportFragmentInjector, Injectable {
    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var frameworkFragmentInjector: DispatchingAndroidInjector<android.app.Fragment>

    override fun fragmentInjector(): AndroidInjector<android.app.Fragment> {
        return frameworkFragmentInjector
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return supportFragmentInjector
    }

    override fun useDaggerInject(): Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        if (useDaggerInject()) {
            try {
                AndroidInjection.inject(this)
            } catch (e: Exception) {
                e.printStackTrace()
                AppLogger.w("Dagger 注入失败了,如果不需要 Dagger 注入,重写 useDaggerInject 方法并返回 FALSE")
            }
        }
        super.onCreate(savedInstanceState)
    }
}