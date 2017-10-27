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
import com.cylan.jiafeigou.widget.WrapContentViewPager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [VisitorListFragmentV2.newInstance] factory method to
 * create an instance of this fragment.
 */
open class VisitorListFragmentV2 : IBaseFragment<VisitorListContract.Presenter>(),
        VisitorListContract.View {

    override fun onVisitsTimeRsp(faceId: String, cnt: Int) {
        onVisitorListCallback?.onVisitorTimes(cnt)
    }

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
        faceAdapter = FaceAdapter(childFragmentManager, isV2())
        faceAdapter.uuid = uuid
        cViewPager.adapter = faceAdapter

        faceAdapter.itemClickListener = object : ItemClickListener {
            override fun itemClick(item: FaceItem, globalPosition: Int, position: Int, pageIndex: Int) {
                onVisitorListCallback?.onItemClick(globalPosition)
                if (globalPosition > 1 || !isV2()) {//前面两个
                    val faceId = if (isV2()) item.visitor?.personId else item.strangerVisitor?.faceId
                    AppLogger.d("主列表的 faceId?personId")
                    basePresenter.fetchVisitsCount(faceId!!)
                }
            }
        }
        cViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                onVisitorListCallback?.onPageScroll(position,
                        ListUtils.getSize(provideData()) as Int)
            }
        })
        FaceItemsProvider.get.ensurePreloadHeaderItem()
        faceAdapter.populateItems(provideData())
    }

    open fun fetchStrangerVisitorList() {
        if (basePresenter != null) {
            basePresenter.fetchStrangerVisitorList()
        }
    }

    open fun isV2(): Boolean {
        return true
    }

    override fun onVisitorListReady(visitorList: DpMsgDefine.VisitorList?) {
        assembleFaceList(visitorList!!.dataList)
        onVisitorListCallback?.onPageScroll(cViewPager.currentItem,
                ListUtils.getSize(FaceItemsProvider.get.visitorItems))
    }

    override fun onVisitorListReady(visitorList: DpMsgDefine.StrangerVisitorList?) {
        AppLogger.d("陌生人列表")
        val listCnt = ListUtils.getSize(visitorList?.strangerVisitors)
        if (listCnt == 0) {
            return
        }
        var list = ArrayList<FaceItem>()
        for (i in 0..listCnt - 1) {
            val strangerFace = FaceItem()
            strangerFace.withFaceType(FaceItem.FACE_TYPE_STRANGER_SUB)
            strangerFace.withStrangerVisitor(visitorList!!.strangerVisitors[i])
            strangerFace.withSetSelected(false)
            list.add(strangerFace)
        }
        FaceItemsProvider.get.populateStrangerItems(list)
    }


    override fun onDetach() {
        super.onDetach()
        if (cleanData()) {
            FaceItemsProvider.get.visitorItems?.clear()
        }
    }

    open fun cleanData(): Boolean {
        return true
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
                allFace.withFaceType(FaceItem.FACE_TYPE_ACQUAINTANCE)
                allFace.withVisitor(visitor)
                list.add(allFace)
            }
        }
        //need remove duplicated visitorItems
        if (ListUtils.isEmpty(list)) {
            return
        }
        FaceItemsProvider.get.populateItems(list)
        faceAdapter.populateItems(provideData())
    }

    open fun provideData(): ArrayList<FaceItem> {
        return FaceItemsProvider.get.visitorItems
    }

    interface OnVisitorListCallback {
        /**
         * gPosition: global position
         */
        fun onItemClick(gPosition: Int)

        fun onVisitorListReady()
        fun onPageScroll(currentItem: Int, total: Int)

        fun onVisitorTimes(times: Int)
    }


    interface ItemClickListener {

        fun itemClick(item: FaceItem, globalPosition: Int, position: Int, pageIndex: Int)
    }

}// Required empty public constructor

class FaceFastItemAdapter : ItemAdapter<FaceItem>()

class FaceAdapter(private var fm: FragmentManager?, private var isV2: Boolean) : FragmentPagerAdapter(fm) {

    lateinit var uuid: String
    lateinit var itemClickListener: VisitorListFragmentV2.ItemClickListener

    var preClickPage: Int = 0
    var preClickPosition: Int = 0

    var dataItems = ArrayList<FaceItem>()

    override fun getCount(): Int {
        val totalCount = ListUtils.getSize(dataItems)
        val cnt = totalCount / JConstant.FACE_CNT_IN_PAGE + if (totalCount % JConstant.FACE_CNT_IN_PAGE == 0) 0 else 1
        Log.d("cnt", "cnt:$cnt,$totalCount")
        return cnt
    }

    fun populateItems(dataItems: ArrayList<FaceItem>) {
        this.dataItems = dataItems
        notifyDataSetChanged()
    }

    private fun updateClickItem(position: Int, pageIndex: Int) {
        if (preClickPage == pageIndex) {
            preClickPosition = position//同一个page,自动刷新。
            return
        }
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
        val f = FaceFragment.newInstance(position, uuid, isV2)
        f.itemClickListener = object : VisitorListFragmentV2.ItemClickListener {
            override fun itemClick(item: FaceItem, globalPosition: Int, position: Int, pageIndex: Int) {
                updateClickItem(position, pageIndex)
                itemClickListener?.itemClick(item, globalPosition, position, pageIndex)
            }
        }
        return f
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

    var visitorAdapter = FaceFastItemAdapter()
    lateinit var rvList: RecyclerView
    lateinit var uuid: String
    var pageIndex: Int = 0
    lateinit var itemClickListener: VisitorListFragmentV2.ItemClickListener
    var adapter = FastAdapter<FaceItem>()

    companion object {
        fun newInstance(pageIndex: Int, uuid: String, isV2: Boolean): FaceFragment {
            val f = FaceFragment()
            val b = Bundle()
            b.putInt("pageIndex", pageIndex)
            b.putString("uuid", uuid)
            b.putBoolean("isV2", isV2)
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
        rvList.layoutManager = GridLayoutManager(context, 3)
        rvList.adapter = visitorAdapter.wrap(adapter)
        adapter.withSelectable(true)
        adapter.withMultiSelect(false)
        adapter.withSelectWithItemUpdate(true)
        adapter.withAllowDeselection(false)
        rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, v: View, parent: RecyclerView, state: RecyclerView.State?) {
                if (parent.getChildLayoutPosition(v) % 3 == 1) {
                    val pixelOffset = context.resources.getDimensionPixelOffset(R.dimen.y18)
                    outRect.left = pixelOffset
                    outRect.right = pixelOffset
                }
            }
        })
        adapter.withOnClickListener { _, _, item, position ->
            val globalPosition = pageIndex * JConstant.FACE_CNT_IN_PAGE + position
            itemClickListener?.itemClick(visitorAdapter.getItem(position),
                    globalPosition, position, pageIndex)
            true
        }
        adapter.withOnLongClickListener { _v, _, _, _p ->
            val globalPosition = pageIndex * JConstant.FACE_CNT_IN_PAGE + _p
            if (globalPosition > 1 || !arguments.getBoolean("isV2")) {
                showHeaderFacePopMenu(globalPosition, _p, _v, adapter.getItem(_p).getFaceType())
            }
            true
        }
        populateItems()
    }

    fun populateItems() {
        pageIndex = arguments.getInt("pageIndex")
        val list = if (arguments.getBoolean("isV2"))
            FaceItemsProvider.get.visitorItems else FaceItemsProvider.get.strangerItems
        val totalCnt = ListUtils.getSize(list)
        if (totalCnt == 0) {
            return
        }
        val subList = list.subList(JConstant.FACE_CNT_IN_PAGE * pageIndex,
                JConstant.FACE_CNT_IN_PAGE * pageIndex + Math.min(JConstant.FACE_CNT_IN_PAGE, totalCnt - JConstant.FACE_CNT_IN_PAGE * pageIndex))
        Log.d("cnt", "cnt,,," + ListUtils.getSize(subList) + ",pageIndex:" + pageIndex)
        visitorAdapter.clear()
        visitorAdapter.add(subList)
    }

    private fun showHeaderFacePopMenu(gPosition: Int, position: Int, faceItem: View, faceType: Int) {
//        AppLogger.w("showHeaderFacePopMenu:$position,item:$faceItem")
        val view = View.inflate(context, R.layout.layout_face_page_pop_menu, null)
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWindow = PopupWindow(view, view.measuredWidth, view.measuredHeight)
        popupWindow.setBackgroundDrawable(ColorDrawable(0))
        popupWindow.isOutsideTouchable = true

        val contentView = popupWindow.contentView
        contentView.findViewById(R.id.detect).visibility =
                if (faceType == FaceItem.FACE_TYPE_ACQUAINTANCE) View.GONE else View.VISIBLE
        // TODO: 2017/10/9 查看和识别二选一 ,需要判断,并且只有人才有查看识别二选一
        if (faceType == FaceItem.FACE_TYPE_ACQUAINTANCE)

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
//            faceItem.isDrawingCacheEnabled = true
//            val image = faceItem.drawingCache
            showDetectFaceAlert(adapter.getItem(position).strangerVisitor?.faceId ?: "")
        }

        contentView.findViewById(R.id.viewer).setOnClickListener { _ ->
            AppLogger.w("将查看面孔详细信息")
            popupWindow.dismiss()
            val item = visitorAdapter?.getItem(position)
            if (item != null) {
                val fragment = FaceInformationFragment.newInstance(uuid,
                        item.visitor?.faceIdList?.get(0) ?: "", item.visitor?.personName ?: "",
                        item.visitor?.personId ?: "")
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
                        uuid, faceId, FaceListFragment.TYPE_ADD_TO)
                fragment.resultCallback = { o, o2, o3 ->

                }// TODO: 2017/10/10 移动到面孔的结果回调
                ActivityUtils.addFragmentSlideInFromRight(activity.supportFragmentManager, fragment, android.R.id.content)
            } else if (newFace!!.isChecked) {
                val fragment = CreateNewFaceFragment.newInstance(uuid, faceId)
                fragment.resultCallback = {
                    //todo 返回创建的personID
                }
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
                //TODO
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
    //熟人
    var visitorItems = ArrayList<FaceItem>()
    //陌生人
    var strangerItems = ArrayList<FaceItem>()

    private object Holder {
        val INSTANCE = FaceItemsProvider()
    }

    companion object {
        val get: FaceItemsProvider by lazy { Holder.INSTANCE }
    }

    fun populateItems(visitorItems: ArrayList<FaceItem>) {
        checkEmpty()
        ensurePreloadHeaderItem()
        if (ListUtils.isEmpty(visitorItems)) return
        this.visitorItems.addAll(visitorItems)
        this.visitorItems = ArrayList<FaceItem>(TreeSet(this.visitorItems))
        Collections.sort(this.visitorItems)
    }

    fun populateStrangerItems(strangerItems: ArrayList<FaceItem>) {
        if (this.strangerItems == null)
            this.strangerItems = ArrayList()
        if (ListUtils.isEmpty(strangerItems)) return
        this.strangerItems.addAll(strangerItems)
        this.strangerItems = ArrayList(TreeSet(this.strangerItems))
        Collections.sort(this.strangerItems)
    }

    fun checkEmpty() {
        if (visitorItems == null)
            visitorItems = ArrayList()
    }

    fun ensurePreloadHeaderItem() {
        if (!(hasPreloadFaceItems())) {
            val list = ArrayList<FaceItem>()
            val allFace = FaceItem()
            allFace.withSetSelected(true)
            allFace.withFaceType(FaceItem.FACE_TYPE_ALL)
            list.add(allFace)
            val strangerFace = FaceItem()
            strangerFace.withFaceType(FaceItem.FACE_TYPE_STRANGER)
            list.add(strangerFace)
            checkEmpty()
            visitorItems.addAll(list)
        }
    }

    fun hasPreloadFaceItems(): Boolean {
        if (ListUtils.getSize(visitorItems) < 2) return false
        return visitorItems[0].getFaceType() == FaceItem.FACE_TYPE_ALL
                && visitorItems[1].getFaceType() == FaceItem.FACE_TYPE_STRANGER
    }

}
