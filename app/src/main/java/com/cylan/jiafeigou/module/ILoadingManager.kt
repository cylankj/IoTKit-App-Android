package com.cylan.jiafeigou.module

import android.support.annotation.StringRes

/**
 * Created by yanzhendong on 2017/10/27.
 */
interface ILoadingManager {


    fun hideLoading()

    fun showLoading(@StringRes resId: Int, cancelable: Boolean, vararg args: Any)
//
//    void showLoading(int resId, boolean cancelable, Object... args);
//
//    void hideLoading();

}