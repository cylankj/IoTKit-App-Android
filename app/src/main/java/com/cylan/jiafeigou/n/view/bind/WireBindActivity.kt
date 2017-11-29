package com.cylan.jiafeigou.n.view.bind

import android.view.View
import com.cylan.jiafeigou.n.mvp.contract.bind.WireBindContract
import com.cylan.jiafeigou.n.mvp.impl.bind.WireBindPresenter
import com.cylan.jiafeigou.n.view.activity.BindAnimationActivity
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.APObserver
import com.cylan.jiafeigou.utils.ActivityUtils
import javax.inject.Inject

/**
 * Created by yanzhendong on 2017/11/28.
 */
class WireBindActivity : BindAnimationActivity(),
        WireBindContract.View {
    override fun useDaggerInject(): Boolean {
        return true
    }

    @Inject lateinit var mPresenter: WireBindPresenter
    override fun onScanDogWiFiTimeout() {
        AppLogger.w("onScanDogWiFiTimeout")
    }

    override fun onScanDogWiFiFinished(it: MutableList<APObserver.ScanResult>) {
        AppLogger.w("onScanDogWiFiFinished:$it")
        val selectCidFragment = SelectCidFragment.newInstance(ArrayList(it))
        ActivityUtils.addFragmentSlideInFromRight(supportFragmentManager, selectCidFragment, android.R.id.content)
    }

    override fun onClick(view: View) {
        mPresenter.scanDogWiFi()
    }

}