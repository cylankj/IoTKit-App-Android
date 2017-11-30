package com.cylan.jiafeigou.n.view.bind

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.misc.bind.UdpConstant
import com.cylan.jiafeigou.n.mvp.contract.bind.SelectCidContract
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.APObserver
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_select_cid.*

/**
 * Created by yanzhendong on 2017/11/28.
 */
class SelectCidFragment : BaseFragment<SelectCidContract.Presenter>(),
        SelectCidContract.View, PickCidDialog.PickerCallback {
    override fun onSendDogConfigFinished() {
        AppLogger.w("onSendDogConfigFinished")
        next_step.viewZoomSmall(null)
        val intent = Intent(context, SubmitBindingInfoActivity::class.java)
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, selectedScanResult?.uuid)
        intent.putExtra(JConstant.KEY_BIND_BACK_ACTIVITY, BindDeviceActivity::class.java.name)
        intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.DOG_CAMERA_NAME))
        startActivity(intent)
        activity.finish()
    }

    override fun onSendDogConfigError() {
        AppLogger.w("onSendDogConfigError")
    }

    override fun onPicker(scanResult: APObserver.ScanResult?) {
        AppLogger.d("onPicker:$scanResult")
        this.selectedScanResult = scanResult
        selectedScanResult?.apply {
            device_cid.text = this.uuid
            device_cid.isEnabled = true
        }
    }


    override fun onScanDogWiFiFinished(result: MutableList<APObserver.ScanResult>) {
        AppLogger.d("SelectCidFragment:onScanDogWiFiFinished:$result")

    }

    override fun onScanDogWiFiTimeout() {
        AppLogger.d("SelectCidFragment:onScanDogWiFiTimeout")
    }

    var scanResults: MutableList<APObserver.ScanResult> = mutableListOf()
    private var selectedScanResult: APObserver.ScanResult? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_select_cid, container, false)
        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()

        val list = arguments.getParcelableArrayList<APObserver.ScanResult>("results")
        scanResults.clear()
        scanResults.addAll(list)
        selectedScanResult = list?.getOrNull(0)
        selectedScanResult?.apply {
            device_cid.text = this.uuid
            device_cid.isEnabled = true
        }
        custom_toolbar.setBackAction { back() }
    }

    @OnClick(R.id.next_step)
    fun nextStep() {
        AppLogger.w("nextStep")
        selectedScanResult?.apply {
            val devicePortrait = UdpConstant.UdpDevicePortrait()
            devicePortrait.mac = mac
            devicePortrait.net = net
            devicePortrait.pid = os
            devicePortrait.version = version
            devicePortrait.uuid = uuid
            devicePortrait.bindFlag = 0//不强绑
            PreferencesUtils.putString(JConstant.BINDING_DEVICE, Gson().toJson(devicePortrait))
            presenter.sendDogConfig(this)
        }
    }

    fun back() {
        AppLogger.w("back")
        fragmentManager.popBackStack()
    }

    @OnClick(R.id.device_cid)
    fun showPickerDialog() {
        val dialog = PickCidDialog.newInstance(scanResults, selectedScanResult?.uuid)
        dialog.pickerCallback = this
        dialog.show(fragmentManager, PickCidDialog::class.java.name)
    }

    companion object {
        @JvmStatic
        fun newInstance(results: ArrayList<APObserver.ScanResult>): SelectCidFragment {
            val fragment = SelectCidFragment()
            val argument = Bundle()
            argument.putParcelableArrayList("results", results)
            fragment.arguments = argument
            return fragment
        }
    }
}