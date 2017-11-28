package com.cylan.jiafeigou.n.view.bind

import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseActivity
import com.cylan.jiafeigou.n.mvp.contract.bind.WireBindContract
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.APObserver
import com.cylan.jiafeigou.utils.ActivityUtils

/**
 * Created by yanzhendong on 2017/11/28.
 */
class WireBindActivity : BaseActivity<WireBindContract.Presenter>(),
        WireBindContract.View {

    override fun onScanDogWiFiTimeout() {
        AppLogger.w("onScanDogWiFiTimeout")
    }

    override fun onScanDogWiFiFinished(it: MutableList<APObserver.ScanResult>) {
        AppLogger.w("onScanDogWiFiFinished:$it")
        val selectCidFragment = SelectCidFragment.newInstance(ArrayList(it))
        ActivityUtils.addFragmentSlideInFromRight(supportFragmentManager, selectCidFragment, android.R.id.content)
    }

    override fun onSetContentView(): Boolean {
        setContentView(R.layout.activity_wire_bind)
        return true
    }

    @OnClick(R.id.scan)
    fun scan() {
        AppLogger.w("开始扫描了....")
        presenter.scanDogWiFi()
    }


}