package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.widget.GridLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.support.log.AppLogger
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import kotlinx.android.synthetic.main.fragment_face_manager.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceManagerFragment : BaseFragment<JFGPresenter<*>>() {

    lateinit var adapter: FastItemAdapter<*>

    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_face_manager, container, false)

        return view
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        val layoutManager = GridLayoutManager(context, 4)
        face_manager_items.layoutManager = layoutManager
        adapter.withOnClickListener { v, adapter, iItem, position ->
            AppLogger.w("FaceManagerOnItemClicked:$v,$position,$iItem,$adapter")
            if (isEditMode()) {
                //TODO 选中条目
            } else {
                //TODO 什么也不做了
            }
            return@withOnClickListener true
        }
        adapter.withOnLongClickListener { v, adapter, iItem, position ->
            AppLogger.w("FaceManagerOnItemLongClicked:$v,$adapter,$iItem,$position")
            //todo 需要弹出菜单
            showFaceManagerPopMenu(position, v)
            return@withOnLongClickListener true
        }

        face_manager_items.adapter = adapter

    }

    private var popupWindow: PopupWindow? = null

    private fun showFaceManagerPopMenu(position: Int, v: View?) {
        if (popupWindow == null) {
            val view = View.inflate(context, R.layout.layout_face_delete_pop_alert, null)
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            popupWindow = PopupWindow(view, view.measuredWidth, view.measuredHeight)
        }
        PopupWindowCompat.showAsDropDown(popupWindow, v, 0, 0, Gravity.TOP)
    }

    private fun isEditMode(): Boolean {
        return true
    }

}