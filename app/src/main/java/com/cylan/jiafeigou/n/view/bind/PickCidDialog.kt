package com.cylan.jiafeigou.n.view.bind

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import butterknife.ButterKnife
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.APObserver
import com.cylan.jiafeigou.utils.DensityUtils
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.commons.utils.DiffCallback
import com.mikepenz.fastadapter.commons.utils.FastAdapterDiffUtil
import com.mikepenz.fastadapter.items.AbstractItem
import com.nineoldandroids.animation.ValueAnimator
import kotlinx.android.synthetic.main.layout_fragment_dialog_wifi_list.*
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * Created by yanzhendong on 2017/11/28.
 */
class PickCidDialog : DialogFragment() {
    private var maxWidth: Int = 0
    private var layoutHeightAnimation: ValueAnimator? = null
    private var resultList: MutableList<ScanItem> = mutableListOf()
    lateinit var scanAdapter: FastItemAdapter<ScanItem>
    private var selectedCid: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.l_style_dialog)
        isCancelable = true
        maxWidth = (DensityUtils.getScreenWidth() * 0.78f).toInt()
    }

    override fun onResume() {
        super.onResume()
        dialog.window.setLayout(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.layout_fragment_dialog_wifi_list, container, false)
        ButterKnife.bind(this, view)
        return view
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        list_title.text = context.getString(R.string.WIRED_SELECT_DEVICE_CID)
        scanAdapter = FastItemAdapter()
        scanAdapter.withSelectable(true)
        scanAdapter.withMultiSelect(false)
        scanAdapter.withSelectWithItemUpdate(true)
        scanAdapter.itemAdapter.withComparator { item1, item2 ->
            when {
                item1.isSelected -> -1
                item2.isSelected -> 1
                else -> 0
            }
        }
        scanAdapter.withOnClickListener { _, _, item, position ->
            AppLogger.d("cid picker dialog item clicked,item:$item,position:$position")
            scanAdapter.select(position)
            pickerCallback?.onPicker(item?.scanResult)
            dismiss()
            return@withOnClickListener true
        }
        rv_wifi_list.adapter = scanAdapter
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv_wifi_list.layoutManager = layoutManager
        val list = arguments.getParcelableArrayList<APObserver.ScanResult>("results")
        selectedCid = arguments.getString("selected_cid")
        updateList(list)
    }

    override fun onPause() {
        super.onPause()
        cancelAnimation()
    }


    private fun cancelAnimation() {
        if (layoutHeightAnimation != null && layoutHeightAnimation!!.isRunning) {
            layoutHeightAnimation!!.cancel()
        }
    }

    private fun updateList(result: List<APObserver.ScanResult>?) {
        result?.map { ScanItem().withScanResult(it).withSetSelected(TextUtils.equals(it.uuid, selectedCid)) }?.apply {
            resultList.clear()
            resultList.addAll(this)
        }

        FastAdapterDiffUtil.set(scanAdapter.itemAdapter, resultList, object : DiffCallback<ScanItem> {
            override fun getChangePayload(oldItem: ScanItem?, oldItemPosition: Int, newItem: ScanItem?, newItemPosition: Int) = null

            override fun areItemsTheSame(oldItem: ScanItem?, newItem: ScanItem?) = TextUtils.equals(oldItem?.scanResult?.uuid, newItem?.scanResult?.uuid)

            override fun areContentsTheSame(oldItem: ScanItem?, newItem: ScanItem?) = TextUtils.equals(oldItem?.scanResult?.uuid, newItem?.scanResult?.uuid)

        }, true)
        rv_wifi_list.layoutManager.scrollToPosition(0)
    }

    private var subscribe: Subscription? = null
    var pickerCallback: PickerCallback? = null

    interface PickerCallback {
        fun onPicker(scanResult: APObserver.ScanResult?)
    }

    @OnClick(R.id.refresh)
    fun refreshDogWiFi() {
        AppLogger.d("refreshDogWiFi")
        subscribe?.unsubscribe()
        subscribe = APObserver.scanDogWiFi()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { refresh_switcher.displayedChild = 1 }
                .doOnTerminate { refresh_switcher.displayedChild = 0 }
                .subscribe({ updateList(it) }) {
                    refresh_switcher.displayedChild = 0
                    it.printStackTrace()
                    AppLogger.e(it)
                }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        subscribe?.unsubscribe()
        pickerCallback = null
    }

    class ScanItem : AbstractItem<ScanItem, ScanItem.ViewHolder>() {
        var scanResult: APObserver.ScanResult? = null
        fun withScanResult(scanResult: APObserver.ScanResult): ScanItem {
            this.scanResult = scanResult
            return this
        }

        @SuppressLint("ResourceType")
        override fun getType(): Int {
            return R.layout.layout_pick_wifi_item
        }

        override fun getViewHolder(v: View): ScanItem.ViewHolder {
            return ViewHolder(v)
        }

        override fun getLayoutRes(): Int {
            return R.layout.layout_pick_wifi_item
        }

        override fun bindView(holder: ViewHolder, payloads: MutableList<Any>?) {
            super.bindView(holder, payloads)
            holder.item_ssid.text = scanResult?.uuid
            holder.item_check.isChecked = isSelected
        }

        class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
            var item_check: RadioButton = itemview.findViewById(R.id.rbtn_item_check) as RadioButton
            var item_ssid: TextView = itemview.findViewById(R.id.tv_item_ssid) as TextView
        }

    }

    companion object {
        fun newInstance(list: MutableList<APObserver.ScanResult>, selectedCid: String?): PickCidDialog {
            val fragment = PickCidDialog()
            val argument = Bundle()
            argument.putParcelableArrayList("results", ArrayList(list))
            argument.putString("selected_cid", selectedCid)
            fragment.arguments = argument
            return fragment
        }
    }
}