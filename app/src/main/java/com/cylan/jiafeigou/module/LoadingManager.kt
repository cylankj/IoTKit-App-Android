package com.cylan.jiafeigou.module

import android.content.Context
import com.cylan.jiafeigou.dagger.annotation.ContextLife
import com.cylan.jiafeigou.widget.LoadingDialog
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by yanzhendong on 2017/10/27.
 */
@Singleton
class LoadingManager @Inject constructor() : ILoadingManager {
    @Inject
    @ContextLife
    lateinit var mContext: Context

    override fun hideLoading() {
        LoadingDialog.dismissLoading()
    }

    override fun showLoading(resId: Int, cancelable: Boolean, vararg args: Any) {
        LoadingDialog.showLoading(mContext, mContext.getString(resId, *args), cancelable)
    }
}