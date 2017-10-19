package com.cylan.jiafeigou.n.view.cam

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.view.cam.item.FaceManagerItem
import com.cylan.jiafeigou.support.log.AppLogger
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.fragment_face_manager.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceManagerFragment : BaseFragment<FaceManagerContact.Presenter>(), FaceManagerContact.View {
    override fun onFaceInformationReady(data: List<DpMsgDefine.FaceInformation>) {
        var face: FaceManagerItem
        val items: MutableList<FaceManagerItem> = mutableListOf()
        for (information in data) {
            face = FaceManagerItem().withFaceInformation(information)
            items.add(face)
        }
        adapter.add(items)
        custom_toolbar.setRightEnable(adapter.itemCount > 0)
    }

    lateinit var adapter: FastItemAdapter<FaceManagerItem>

    override fun setFragmentComponent(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_face_manager, container, false)

        return view
    }

    private var personId: String? = null

    override fun onStart() {
        super.onStart()
        if (personId != null) {
            presenter.loadFacesByPersonId(personId!!)
        }
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        personId = arguments.getString("person_id")

        val layoutManager = GridLayoutManager(context, 4)
        face_manager_items.layoutManager = layoutManager
        adapter = FastItemAdapter()
        adapter.withSelectable(false)
        adapter.withMultiSelect(true)
        adapter.withSelectWithItemUpdate(true)
        adapter.withOnClickListener { v, adapter, iItem, position ->
            AppLogger.w("FaceManagerOnItemClicked:$v,$position,$iItem,$adapter")
            if (isEditMode()) {
                //TODO 选中条目
                AppLogger.w("")
                return@withOnClickListener true
            } else {
                //TODO 什么也不做了
                return@withOnClickListener false
            }
        }
        adapter.withEventHook(object : ClickEventHook<FaceManagerItem>() {

            override fun onBindMany(viewHolder: RecyclerView.ViewHolder): MutableList<View>? {
                if (viewHolder is FaceManagerItem.FaceManagerViewHolder) {
                    return mutableListOf(viewHolder.itemView, viewHolder.faceCheckBox)
                }
                return null
            }

            override fun onClick(v: View?, position: Int, fastAdapter: FastAdapter<FaceManagerItem>, item: FaceManagerItem) {
                if (!item.isSelected) {
                    val selections = fastAdapter.selections
                    if (!selections.isEmpty()) {
                        val selectedPosition = selections.iterator().next()
                        fastAdapter.deselect()
                        fastAdapter.notifyItemChanged(selectedPosition)
                    }
                    fastAdapter.select(position)
                }
            }

        })

        adapter.withOnLongClickListener { v, adapter, iItem, position ->
            AppLogger.w("FaceManagerOnItemLongClicked:$v,$adapter,$iItem,$position")
            //todo 需要弹出菜单
            showFaceManagerPopMenu(position, v)
            return@withOnLongClickListener true
        }

        face_manager_items.adapter = adapter
//        face_manager_items.addItemDecoration(GridItemDivider(resources.getDimensionPixelOffset(R.dimen.y5), 4))

        custom_toolbar.setRightAction {
            if (getString(R.string.EDIT_THEME) == custom_toolbar.tvToolbarRight.text) {
                adapter.withSelectable(true)
                custom_toolbar.setToolbarRightTitle(R.string.CANCEL)
                bottom_menu.visibility = View.VISIBLE
            } else {
                adapter.withSelectable(false)
                custom_toolbar.setToolbarRightTitle(R.string.EDIT_THEME)
                bottom_menu.visibility = View.GONE
            }
        }

        /// 默认是不可点击的,等有数据后才能点击
//        custom_toolbar.setRightEnable(false)
        custom_toolbar.setBackAction { fragmentManager.popBackStack() }

        //todo just for test

        val items: MutableList<FaceManagerItem> = mutableListOf()
        words.forEach {
            val item = FaceManagerItem()
            val information = DpMsgDefine.FaceInformation()
            information.face_name = it
            item.withFaceInformation(information)
            items.add(item)
        }
        adapter.add(items)

    }

    val words = arrayOf("普鹤骞", "田惠君", "貊怀玉", "潘鸿信", "士春柔", "阙子璇", "皇甫笑", "妍李颖", "初殷浩旷")

    private fun showFaceManagerPopMenu(position: Int, v: View?) {
        val view = View.inflate(context, R.layout.layout_face_manager_pop_alert, null)
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWindow = PopupWindow(view, view.measuredWidth, view.measuredHeight)
        popupWindow.setBackgroundDrawable(ColorDrawable(0))
        popupWindow.isOutsideTouchable = true

        view.findViewById(R.id.delete).setOnClickListener {
            AppLogger.w("面孔管理:删除")

        }

        view.findViewById(R.id.move_to).setOnClickListener {
            AppLogger.w("面孔管理:移动到")
            FaceListFragment.newInstance(DataSourceManager.getInstance().account.account, uuid)
        }
        PopupWindowCompat.showAsDropDown(popupWindow, v, 0, 0, Gravity.TOP)
    }

    private fun isEditMode(): Boolean {
        return custom_toolbar.tvToolbarRight.isEnabled
    }

    companion object {
        fun newInstance(uuid: String, personId: String): FaceManagerFragment {
            val fragment = FaceManagerFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putString("person_id", personId)
            fragment.arguments = argument
            return fragment
        }
    }

}