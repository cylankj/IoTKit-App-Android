package com.cylan.jiafeigou.n.view.panorama

import com.cylan.jiafeigou.base.wrapper.BasePresenter
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/9/5.
 */
open class LiveSettingPresenter @Inject constructor(view: LiveSettingContact.View) : BasePresenter<LiveSettingContact.View>(view), LiveSettingContact.Presenter {

    override fun getAccount(accountType: Int) {

    }
}