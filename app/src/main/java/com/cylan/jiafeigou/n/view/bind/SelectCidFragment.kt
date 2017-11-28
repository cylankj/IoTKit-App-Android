package com.cylan.jiafeigou.n.view.bind

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.mvp.contract.bind.SelectCidContract
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.APObserver
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.commons.utils.DiffCallback
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Created by yanzhendong on 2017/11/28.
 */
class SelectCidFragment : BaseFragment<SelectCidContract.Presenter>(), SelectCidContract.View {
    override fun onScanDogWiFiFinished(result: MutableList<APObserver.ScanResult>) {
        AppLogger.d("SelectCidFragment:onScanDogWiFiFinished:$result")
        val list = result.map { ScanItem().withScanResult(it) }
        FastAdapterDiffUtil.set(scanAdapter.itemAdapter, list, object : DiffCallback<ScanItem> {
            override fun getChangePayload(oldItem: ScanItem?, oldItemPosition: Int, newItem: ScanItem?, newItemPosition: Int) = null

            override fun areItemsTheSame(oldItem: ScanItem?, newItem: ScanItem?) = TextUtils.equals(oldItem?.scanResult?.uuid, newItem?.scanResult?.uuid)

            override fun areContentsTheSame(oldItem: ScanItem?, newItem: ScanItem?) = TextUtils.equals(oldItem?.scanResult?.uuid, newItem?.scanResult?.uuid)

        }, true)
    }

    override fun onScanDogWiFiTimeout() {
        AppLogger.d("SelectCidFragment:onScanDogWiFiTimeout")
    }

    var scanResults: MutableList<APObserver.ScanResult> = mutableListOf()
    lateinit var scanAdapter: FastItemAdapter<ScanItem>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_select_cid, container, false)
        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        scanAdapter = FastItemAdapter()
        scanAdapter.withSelectable(true)
        scanAdapter.withMultiSelect(false)
        val list = arguments.getParcelableArrayList<APObserver.ScanResult>("results")

        list?.map { ScanItem().withScanResult(it) }?.apply { scanAdapter.setNewList(this) }
    }

    @OnClick(R.id.next)
    fun nextStep() {
        AppLogger.w("nextStep")
        val intent = Intent(context, SubmitBindingInfoActivity::class.java)
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
        intent.putExtra(JConstant.KEY_BIND_DEVICE_ALIAS, "")
        startActivity(intent)
    }

    fun refreshDogWiFi() {
        scanAdapter.selectedItems.toList().getOrNull(0).apply {

            presenter.refreshDogWiFi()
        }
    }

    class ScanItem : AbstractItem<ScanItem, ScanItem.ViewHolder>() {
        var scanResult: APObserver.ScanResult? = null
        fun withScanResult(scanResult: APObserver.ScanResult): ScanItem {
            this.scanResult = scanResult
            return this
        }

        @SuppressLint("ResourceType")
        override fun getType(): Int {
            return R.layout.layout_scan_item
        }

        override fun getViewHolder(v: View): ScanItem.ViewHolder {
            return ViewHolder(v)
        }

        override fun getLayoutRes(): Int {
            return R.layout.layout_scan_item
        }

        override fun bindView(holder: ViewHolder?, payloads: MutableList<Any>?) {
            super.bindView(holder, payloads)

        }

        class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {

        }

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