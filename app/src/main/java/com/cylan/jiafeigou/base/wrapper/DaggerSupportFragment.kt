package com.cylan.jiafeigou.base.wrapper

import android.content.Context
import android.support.v4.app.Fragment
import com.cylan.jiafeigou.dagger.Injectable
import com.cylan.jiafeigou.support.log.AppLogger
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import java.lang.Exception
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/10/26.
 */
abstract class DaggerSupportFragment : Fragment(), HasSupportFragmentInjector, Injectable {

    @Inject
    lateinit var supportFragmentAndroidInjector: DispatchingAndroidInjector<Fragment>


    override fun supportFragmentInjector(): AndroidInjector<Fragment>? {
        return supportFragmentAndroidInjector
    }

    override fun useDaggerInject(): Boolean = true

    override fun onAttach(context: Context?) {
        if (useDaggerInject()) {
            try {
                AndroidSupportInjection.inject(this)
            } catch (e: Exception) {
                e.printStackTrace()
                AppLogger.w("Dagger 注入失败了,如果不需要 Dagger 注入,重写 useDaggerInject 方法并返回 FALSE")
            }
        }
        super.onAttach(context)
    }
}