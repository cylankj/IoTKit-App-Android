package com.cylan.jiafeigou.view

/**
 * Created by yanzhendong on 2017/10/28.
 *
 */
@Deprecated("一个适配器,不推荐使用")
interface PresenterAdapter<in T> {

    fun setPresenter(presenter: T)

}