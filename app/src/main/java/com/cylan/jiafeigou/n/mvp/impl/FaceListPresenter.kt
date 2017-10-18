package com.cylan.jiafeigou.n.mvp.impl

import com.cylan.jiafeigou.base.wrapper.BasePresenter
import com.cylan.jiafeigou.n.view.cam.FaceListContact

/**
 * Created by yanzhendong on 2017/10/16.
 */
class FaceListPresenter : BasePresenter<FaceListContact.View>(), FaceListContact.Presenter {
    override fun loadPersonItems(account: String, uuid: String) {

    }
}