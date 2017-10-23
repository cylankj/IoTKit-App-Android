package com.cylan.jiafeigou.n.view.cam


import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
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
import com.cylan.jiafeigou.n.view.cam.item.FaceItem
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.ListUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.cylan.jiafeigou.widget.WrapContentViewPager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter


/**
 * A simple [Fragment] subclass.
 * Use the [VisitorListFragmentV2.newInstance] factory method to
 * create an instance of this fragment.
 */
class VisitorListFragmentV2 : IBaseFragment<VisitorListContract.Presenter>(),
        VisitorListContract.View {
    lateinit var onVisitorListCallback: OnVisitorListCallback

    lateinit var faceAdapter: FaceAdapter
    lateinit var cViewPager: WrapContentViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        basePresenter = BaseVisitorPresenter(this)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_visitor_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cViewPager = view.findViewById(R.id.vp_default) as WrapContentViewPager
        faceAdapter = FaceAdapter(childFragmentManager)
        faceAdapter.uuid = uuid
        cViewPager.adapter = faceAdapter
        faceAdapter.ensurePreloadHeaderItem()

        faceAdapter.itemClickListener = object : ItemClickListener {
            override fun itemClick(globalPosition: Int, position: Int, pageIndex: Int) {
//                ToastUtil.showToast("点击了？" + globalPosition)
                onVisitorListCallback?.onItemClick(globalPosition)
            }
        }
        cViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                onVisitorListCallback?.onPageScroll(position,
                        ListUtils.getSize(FaceItemsProvider.get.items))
            }
        })
    }

    override fun onVisitorListReady(visitorList: DpMsgDefine.VisitorList?) {
        assembleFaceList(visitorList!!.dataList)
        onVisitorListCallback?.onPageScroll(cViewPager.currentItem,
                ListUtils.getSize(FaceItemsProvider.get.items))
    }

    override fun onDetach() {
        super.onDetach()
        FaceItemsProvider.get.items?.clear()
    }

    companion object {
        fun newInstance(uuid: String): VisitorListFragmentV2 {
            val fragment = VisitorListFragmentV2()
            val args = Bundle()
            args.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = args
            return fragment
        }
    }

    private fun assembleFaceList(dataList: List<DpMsgDefine.Visitor>?) {
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
        faceAdapter.populateItems(list)
        if (ListUtils.isEmpty(list)) {
            return
        }
        faceAdapter.populateItems(list)
    }

    interface OnVisitorListCallback {
        /**
         * gPosition: global position
         */
        fun onItemClick(gPosition: Int)

        fun onVisitorListReady(visitorList: DpMsgDefine.VisitorList?)
        fun onPageScroll(currentItem: Int, total: Int)
    }


    interface ItemClickListener {

        fun itemClick(globalPosition: Int, position: Int, pageIndex: Int)
    }

}// Required empty public constructor

class FaceFastItemAdapter : ItemAdapter<FaceItem>()

class FaceAdapter(private var fm: FragmentManager?) : FragmentPagerAdapter(fm) {

    lateinit var uuid: String
    lateinit var itemClickListener: VisitorListFragmentV2.ItemClickListener

    var preClickPage: Int = 0
    var preClickPosition: Int = 0

    override fun getCount(): Int {
        val totalCount = ListUtils.getSize(FaceItemsProvider.get.items)
        val cnt = totalCount / 6 + if (totalCount % 6 == 0) 0 else 1
        Log.d("cnt", "cnt:$cnt,$totalCount")
        return cnt
    }

    fun populateItems(items: ArrayList<FaceItem>) {
        FaceItemsProvider.get.populateItems(items)
        notifyDataSetChanged()
    }

    private fun updateClickItem(position: Int, pageIndex: Int) {
        if (preClickPage == pageIndex) return
        val list = fm?.fragments
        if (list != null) {
            val cnt = ListUtils.getSize(list)
            Log.d("cnt", "cnt prePageIndex:$preClickPage,$preClickPosition")
            (0..cnt - 1).filter { it == preClickPage }.forEach {
                (list[it] as FaceFragment).adapter?.deselect(preClickPosition)
            }
        }
        preClickPage = pageIndex
        preClickPosition = position
    }

    override fun getItem(position: Int): Fragment {
        val f = FaceFragment.newInstance(position, uuid)
        f.itemClickListener = object : VisitorListFragmentV2.ItemClickListener {
            override fun itemClick(globalPosition: Int, position: Int, pageIndex: Int) {
                updateClickItem(position, pageIndex)
                itemClickListener?.itemClick(globalPosition, position, pageIndex)
            }
        }
        return f
    }

    fun ensurePreloadHeaderItem() {
        FaceItemsProvider.get.ensurePreloadHeaderItem()
        notifyDataSetChanged()
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        val list = fm?.fragments
        list?.map { it as FaceFragment }?.forEach { it.populateItems() }
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
//        super.destroyItem(container, position, `object`)
    }
}


class FaceFragment : Fragment() {

    lateinit var visitorAdapter: FaceFastItemAdapter
    lateinit var rvList: RecyclerView
    lateinit var uuid: String
    var pageIndex: Int = 0
    lateinit var itemClickListener: VisitorListFragmentV2.ItemClickListener
    public lateinit var adapter: FastAdapter<FaceItem>

    companion object {
        fun newInstance(pageIndex: Int, uuid: String): FaceFragment {
            val f = FaceFragment()
            val b = Bundle()
            b.putInt("pageIndex", pageIndex)
            b.putString("uuid", uuid)
            f.arguments = b
            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.uuid = arguments.getString("uuid")
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.message_face_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvList = view.findViewById(R.id.message_face_page_item) as RecyclerView
        adapter = FastAdapter<FaceItem>()
        visitorAdapter = FaceFastItemAdapter()
        rvList.layoutManager = GridLayoutManager(context, 3)
        rvList.adapter = visitorAdapter.wrap(adapter)
        adapter.withSelectable(true)
        adapter.withMultiSelect(false)
        adapter.withSelectWithItemUpdate(true)
        adapter.withAllowDeselection(false)
        rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, v: View, parent: RecyclerView, state: RecyclerView.State?) {
                if (parent.getChildLayoutPosition(v) % 3 == 1) {
                    val pixelOffset = getContext().resources.getDimensionPixelOffset(R.dimen.y18)
                    outRect.left = pixelOffset
                    outRect.right = pixelOffset
                }
            }
        })
        adapter.withOnClickListener { _, _, item, position ->
            val globalPosition = pageIndex * 6 + position
            itemClickListener?.itemClick(globalPosition, position, pageIndex)
            true
        }
        adapter.withOnLongClickListener { _v, _, _, _p ->
            showHeaderFacePopMenu(_p, _v as ImageView, adapter.getItem(_p).faceType)
            true
        }
        populateItems()
    }

    fun populateItems() {
        pageIndex = arguments.getInt("pageIndex")
        val totalCnt = ListUtils.getSize(FaceItemsProvider.get.items)
        val list = FaceItemsProvider.get.items.subList(6 * pageIndex,
                6 * pageIndex + Math.min(6, totalCnt - 6 * pageIndex))
        Log.d("cnt", "cnt,,," + ListUtils.getSize(list) + ",pageIndex:" + pageIndex)
        visitorAdapter.clear()
        visitorAdapter.add(list)
        for (i in list) {
            Log.d("cnt", "index:" + pageIndex + "," + i.isSelected)
        }
    }

    private fun showHeaderFacePopMenu(position: Int, faceItem: ImageView, faceType: Int) {
//        AppLogger.w("showHeaderFacePopMenu:$position,item:$faceItem")
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
                val item = visitorAdapter.getItem(position)
                        as FaceItem
                showDeleteFaceAlert(item)
            }

        contentView.findViewById(R.id.detect).setOnClickListener { v ->
            // TODO: 2017/10/9 识别操作
            AppLogger.w("将识别面孔")
            popupWindow.dismiss()
            faceItem.isDrawingCacheEnabled = true
            val image = faceItem.drawingCache
            showDetectFaceAlert("")
        }

        contentView.findViewById(R.id.viewer).setOnClickListener { v ->
            AppLogger.w("将查看面孔详细信息")
            popupWindow.dismiss()
            val item = visitorAdapter?.getItem(position)
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

    private fun showDetectFaceAlert(faceId: String) {
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
}

class FaceItemsProvider private constructor() {

    var items = ArrayList<FaceItem>()

    private object Holder {
        val INSTANCE = FaceItemsProvider()
    }

    companion object {
        val get: FaceItemsProvider by lazy { Holder.INSTANCE }
    }

    fun populateItems(items: ArrayList<FaceItem>) {
        checkEmpty()
        ensurePreloadHeaderItem()
        this.items.addAll(items)
    }

    fun checkEmpty() {
        if (items == null)
            items = ArrayList()
    }

    fun ensurePreloadHeaderItem() {
        if (!(hasPreloadFaceItems())) {
            val list = ArrayList<FaceItem>()
            val allFace = FaceItem()
            allFace.withSetSelected(true)
            allFace.faceType = FaceItem.FACE_TYPE_ALL
            list.add(allFace)
            val strangerFace = FaceItem()
            strangerFace.faceType = FaceItem.FACE_TYPE_STRANGER
            list.add(strangerFace)
            checkEmpty()
            items.addAll(list)
        }
    }

    fun hasPreloadFaceItems(): Boolean {
        if (ListUtils.getSize(items) < 2) return false
        return items[0].faceType == FaceItem.FACE_TYPE_ALL
                && items[1].faceType == FaceItem.FACE_TYPE_STRANGER
    }

}
