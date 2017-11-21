package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.n.mvp.contract.cam.MonitorAreaSettingContact

/**
 * Created by yanzhendong on 2017/11/21.
 */
class MonitorAreaSetting : BaseFragment<MonitorAreaSettingContact.Presenter>()
        , MonitorAreaSettingContact.View {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_monitor_area_setting, container, false)
        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()

    }

}