package com.cylan.jiafeigou.n.view.cam


import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.IBaseFragment
import com.cylan.jiafeigou.n.mvp.contract.cam.VisitorListContract
import com.cylan.jiafeigou.n.mvp.impl.cam.BaseVisitorPresenter
import com.cylan.jiafeigou.n.view.adapter.CamMessageFaceAdapter
import com.cylan.jiafeigou.n.view.cam.item.FaceItem
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.ListUtils
import com.cylan.jiafeigou.widget.WrapContentViewPager
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [VisitorListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitorListFragment : IBaseFragment<VisitorListContract.Presenter>(),
        VisitorListContract.View {


    lateinit var onVisitorListCallback: OnVisitorListCallback
    lateinit var visitorAdapter: CamMessageFaceAdapter
    lateinit var vpCamMessageHeaderFaces: WrapContentViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        basePresenter = BaseVisitorPresenter(this)
    }

    lateinit var container: ViewGroup

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        this.container = container!!
        return inflater!!.inflate(R.layout.fragment_visitor_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.post { Log.d("TAg", "tag: " + container?.height + "," + container?.measuredHeight) }
        vpCamMessageHeaderFaces = view.findViewById(R.id.vp_default) as WrapContentViewPager
        visitorAdapter = CamMessageFaceAdapter()
        visitorAdapter.setOnFaceItemClickListener(object : CamMessageFaceAdapter.FaceItemEventListener {
            override fun onFaceItemClicked(positionInPage: Int, parent: View, icon: ImageView) {
                val wPage = vpCamMessageHeaderFaces.currentItem
                val globalPosition = wPage * 6 + positionInPage
                AppLogger.w("onFaceItemClicked:$wPage,$positionInPage,$globalPosition")
                val item = visitorAdapter.getGlobalItem(wPage, positionInPage) as FaceItem
                onVisitorListCallback.onItemClick(item, globalPosition, null)
            }

            override fun onFaceItemLongClicked(positionInPage: Int, parent: View, icon: ImageView, faceType: Int) {
                val wPage = vpCamMessageHeaderFaces.currentItem
                val globalPosition = vpCamMessageHeaderFaces.currentItem * 6 + positionInPage
                AppLogger.w("onFaceItemClicked:$wPage,$positionInPage,$globalPosition")
                showHeaderFacePopMenu(wPage, positionInPage, parent, icon, faceType)
            }

            override fun getCurrentItem(): Int {
                return vpCamMessageHeaderFaces.currentItem
            }
        })
        vpCamMessageHeaderFaces.adapter = visitorAdapter
        vpCamMessageHeaderFaces.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                onVisitorListCallback?.onPageScroll(position, visitorAdapter.totalCount)
            }
        })
        ensurePreloadHeaderItem()
    }

    override fun onVisitorListReady(visitorList: DpMsgDefine.VisitorList?) {
        onVisitorListCallback.onVisitorListReady(visitorList)
        AppLogger.d("数据回来了")
        assembleFaceList(visitorList!!.dataList, 0)
    }

    private fun ensurePreloadHeaderItem() {
        if (!(visitorAdapter.hasPreloadFaceItems())) {
            val list = ArrayList<FaceItem>()
            val allFace = FaceItem()
            allFace.withSetSelected(true)
            allFace.faceType = FaceItem.FACE_TYPE_ALL
            list.add(allFace)
            val strangerFace = FaceItem()
            strangerFace.faceType = FaceItem.FACE_TYPE_STRANGER
            list.add(strangerFace)
            visitorAdapter.setPreloadFaceItems(list)
        }
        if (onVisitorListCallback != null) {
            onVisitorListCallback.onPageScroll(0, 2)
        }
    }

    fun itemFocused(itemIndex: Int) {
        when (itemIndex) {
            0 -> {

            }

            1 -> {

            }
        }
    }

    private fun showHeaderFacePopMenu(whichPage: Int, position: Int, parent: View, faceItem: ImageView, faceType: Int) {
        AppLogger.w("showHeaderFacePopMenu:$position,item:$faceItem")
        val view = View.inflate(context, R.layout.layout_face_page_pop_menu, null)
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWindow = PopupWindow(view, view.measuredWidth, view.measuredHeight)
        popupWindow.setBackgroundDrawable(ColorDrawable(0))
        popupWindow.isOutsideTouchable = true

        val contentView = popupWindow.contentView

        // TODO: 2017/10/9 查看和识别二选一 ,需要判断,并且只有人才有查看识别二选一
        if (faceType == FaceItem.FACE_TYPE_STRANGER)

            contentView.findViewById(R.id.delete).setOnClickListener { v ->
                // TODO: 2017/10/9 删除操作
                AppLogger.w("将删除面孔")
                popupWindow.dismiss()
                val item = visitorAdapter?.getGlobalItem(whichPage, position)
                        as FaceItem
                showDeleteFaceAlert(item)
            }

        contentView.findViewById(R.id.detect).setOnClickListener { v ->
            // TODO: 2017/10/9 识别操作
            AppLogger.w("将识别面孔")
            popupWindow.dismiss()
            faceItem.isDrawingCacheEnabled = true
            val image = faceItem.drawingCache
            showDetectFaceAlert("", image)
        }

        contentView.findViewById(R.id.viewer).setOnClickListener { v ->
            AppLogger.w("将查看面孔详细信息")
            popupWindow.dismiss()
            val item = visitorAdapter?.getGlobalItem(whichPage, position)
            if (item != null) {
                val fragment = FaceInformationFragment.newInstance(uuid, item.faceinformation!!.face_id,
                        item.faceinformation!!.face_name, item.faceinformation!!.person_id)
                ActivityUtils.addFragmentSlideInFromRight(activity.supportFragmentManager, fragment, android.R.id.content)
            } else {
                // TODO: 2017/10/16 为什么会出现这种情况?
            }
        }
        PopupWindowCompat.showAsDropDown(popupWindow, faceItem, 0, 0, Gravity.START)
    }

    private fun showDetectFaceAlert(faceId: String, picture: Bitmap) {
        val dialog = AlertDialog.Builder(context)
                .setView(R.layout.layout_face_detect_pop_alert)
                .show()

        dialog.findViewById(R.id.detect_cancel)!!.setOnClickListener { v -> dialog.dismiss() }

        dialog.findViewById(R.id.detect_ok)!!.setOnClickListener { v ->
            val addTo = dialog.findViewById(R.id.detect_add_to) as RadioButton?
            val newFace = dialog.findViewById(R.id.detect_new_face) as RadioButton?
            if (addTo!!.isChecked) {
                val fragment = FaceListFragment.newInstance(DataSourceManager.getInstance().account.account,
                        uuid, "", FaceListFragment.TYPE_ADD_TO)
                fragment.resultCallback = { o, o2, o3 ->

                    null
                }// TODO: 2017/10/10 移动到面孔的结果回调
                ActivityUtils.addFragmentSlideInFromRight(activity.supportFragmentManager, fragment, android.R.id.content)
            } else if (newFace!!.isChecked) {
                val fragment = CreateNewFaceFragment.newInstance(uuid, faceId)
                fragment.resultCallback = { ret -> null }
                ActivityUtils.addFragmentSlideInFromRight(activity.supportFragmentManager, fragment, android.R.id.content)
            }
            dialog.dismiss()
        }
    }

    private fun showDeleteFaceAlert(item: FaceItem) {
        val dialog = AlertDialog.Builder(context)
                .setView(R.layout.layout_face_delete_pop_alert)
                .show()
        dialog.findViewById(R.id.delete_cancel)!!.setOnClickListener { v1 ->
            // TODO: 2017/10/9 取消了 什么也不做
            dialog.dismiss()

        }

        dialog.findViewById(R.id.delete_ok)!!.setOnClickListener { v ->
            val radioGroup = dialog.findViewById(R.id.delete_radio) as RadioGroup?
            val radioButtonId = radioGroup!!.checkedRadioButtonId
            if (radioButtonId == R.id.delete_only_face) {
                AppLogger.w("only face")
                val faceInformation = item.faceinformation
                if (faceInformation != null) {
//                    basePresenter.deleteFace(faceInformation.face_id, null, null)
                }
            } else if (radioButtonId == R.id.delete_face_and_message) {
                AppLogger.w("face and message")
            } else {
                // 什么也没选
            }
            dialog.dismiss()
        }

    }

    companion object {
        fun newInstance(uuid: String): VisitorListFragment {
            val fragment = VisitorListFragment()
            val args = Bundle()
            args.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = args
            return fragment
        }
    }

    private fun assembleFaceList(dataList: List<DpMsgDefine.Visitor>?, guessTotal: Int) {
        val list = ArrayList<FaceItem>()
        if (dataList != null) {
            for (visitor in dataList) {
                val allFace = FaceItem()
                allFace.faceType = FaceItem.FACE_TYPE_ACQUAINTANCE
                allFace.visitor = visitor
                list.add(allFace)
            }
        }
        for (i in 1..5) {
            val strangerFace = FaceItem()
            strangerFace.faceType = FaceItem.FACE_TYPE_ACQUAINTANCE
            list.add(strangerFace)
        }
        //need remove duplicated items
        visitorAdapter!!.appendFaceItems(list)
        if (ListUtils.isEmpty(list)) {
            return
        }
        visitorAdapter?.appendFaceItems(list)
        if (onVisitorListCallback != null) {
            onVisitorListCallback.onPageScroll(vpCamMessageHeaderFaces.currentItem, visitorAdapter.totalCount)
        }
    }

    interface OnVisitorListCallback {
        /**
         * gPosition: global position
         */
        fun onItemClick(type: FaceItem?, gPosition: Int, dataList: ArrayList<String>?)

        fun onVisitorListReady(visitorList: DpMsgDefine.VisitorList?)
        fun onPageScroll(currentItem: Int, total: Int)
    }

}// Required empty public constructor
