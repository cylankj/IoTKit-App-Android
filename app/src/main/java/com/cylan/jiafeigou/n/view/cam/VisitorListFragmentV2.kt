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
import com.cylan.jiafeigou.utils.ToastUtil
import com.cylan.jiafeigou.widget.page.EViewPager
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.fragment_visitor_list.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 * Use the [VisitorListFragmentV2.newInstance] factory method to
 * create an instance of this fragment.
 */
open class VisitorListFragmentV2 : IBaseFragment<VisitorListContract.Presenter>(),
        VisitorListContract.View {
    override fun onDeleteFaceSuccess(type: Int, delMsg: Int) {
        AppLogger.w("删除面孔消息成功了")
        ToastUtil.showToast("语言包:删除面孔成功了!")
        when (type) {
            1 -> {
                //陌生人
                presenter.fetchStrangerVisitorList()
            }
            2 -> {
                //熟人
                presenter.fetchVisitorList()
            }
        }
    }

    override fun onDeleteFaceError() {
        AppLogger.w("删除面孔消息失败了")
        ToastUtil.showToast(getString(R.string.Tips_DeleteFail))

    }


    override fun onVisitsTimeRsp(faceId: String, cnt: Int) {
        setFaceVisitsCounts(cnt)
    }

    lateinit var onVisitorListCallback: OnVisitorListCallback

    lateinit var faceAdapter: FaceAdapter
    lateinit var cViewPager: EViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = BaseVisitorPresenter(this)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_visitor_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cViewPager = view.findViewById(R.id.vp_default) as EViewPager
        faceAdapter = FaceAdapter(childFragmentManager, isNormalVisitor())
        faceAdapter.uuid = uuid
        cViewPager.adapter = faceAdapter

        faceAdapter.itemClickListener = object : ItemClickListener {
            override fun itemLongClick(globalPosition: Int, _p: Int, _v: View, faceType: Int, pageIndex: Int) {
                faceAdapter.updateClickItem(_p, pageIndex)
                showHeaderFacePopMenu(globalPosition, _p, _v, faceType)
            }

            override fun itemClick(item: FaceItem, globalPosition: Int, position: Int, pageIndex: Int) {
                onVisitorListCallback?.onItemClick(globalPosition)
                val next = item.getFaceType() != FaceItem.FACE_TYPE_STRANGER &&
                        item.getFaceType() != FaceItem.FACE_TYPE_ALL
                if (next || !isNormalVisitor()) {//前面两个
                    val faceId = if (isNormalVisitor()) item.visitor?.personId else item.strangerVisitor?.faceId
                    AppLogger.d("主列表的 faceId?personId")
                    cam_message_indicator_watcher_text.visibility = View.VISIBLE
                    presenter.fetchVisitsCount(faceId!!)
                } else {
                    cam_message_indicator_watcher_text.visibility = View.GONE
                }
            }
        }
        cViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                setFaceHeaderPageIndicator(position, ListUtils.getSize(provideData()))
            }
        })
        cViewPager.enableScrollListener = EViewPager.EnableScrollListener { false }
        if (isNormalVisitor()) {
            FaceItemsProvider.get.ensurePreloadHeaderItem()
            faceAdapter.populateItems(provideData())
            cam_message_indicator_holder.visibility = View.VISIBLE
            setFaceHeaderPageIndicator(cViewPager.currentItem, ListUtils.getSize(provideData()))
        }
    }

    private fun setFaceHeaderPageIndicator(currentItem: Int, total: Int) {
        cam_message_indicator_page_text.text = String.format("%s/%s", currentItem + 1, total / 6 + if (total % 6 == 0) 0 else 1)
        cam_message_indicator_page_text.visibility = if (total > 3) View.VISIBLE else View.GONE
    }

    private fun setFaceVisitsCounts(count: Int) {
        if (cam_message_indicator_watcher_text.visibility != View.VISIBLE) {
            cam_message_indicator_watcher_text.visibility = View.VISIBLE
        }
        cam_message_indicator_watcher_text.text = getString(R.string.MESSAGES_FACE_VISIT_TIMES, count.toString())
    }

    open fun fetchStrangerVisitorList() {
        if (presenter != null) {
            presenter.fetchStrangerVisitorList()
        }
    }

    override fun isNormalVisitor(): Boolean {
        return true
    }


    override fun onVisitorListReady(visitorList: DpMsgDefine.VisitorList?) {
        FaceItemsProvider.get.visitorItems.clear()
        assembleFaceList(visitorList!!.dataList)
        cam_message_indicator_holder.visibility = View.VISIBLE
        setFaceHeaderPageIndicator(cViewPager.currentItem, ListUtils.getSize(provideData()))
    }

    override fun onVisitorListReady(visitorList: DpMsgDefine.StrangerVisitorList?) {
        AppLogger.d("陌生人列表")
        FaceItemsProvider.get.strangerItems.clear()
        val listCnt = ListUtils.getSize(visitorList?.strangerVisitors)
        if (listCnt == 0) {
            return
        }
        var list = ArrayList<FaceItem>()
        for (i in 0 until listCnt) {
            val strangerFace = FaceItem()
            strangerFace.withFaceType(FaceItem.FACE_TYPE_STRANGER_SUB)
            strangerFace.withStrangerVisitor(visitorList!!.strangerVisitors[i])
            strangerFace.withSetSelected(false)
            list.add(strangerFace)
        }
        FaceItemsProvider.get.populateStrangerItems(list)
        faceAdapter.populateItems(FaceItemsProvider.get.strangerItems)
        cam_message_indicator_holder.visibility = View.VISIBLE
        setFaceHeaderPageIndicator(cViewPager.currentItem, ListUtils.getSize(provideData()))
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
        //need remove duplicated visitorItems
        if (ListUtils.isEmpty(dataList)) {
            return
        }
        val list = ArrayList<FaceItem>()
        if (dataList != null) {
            for (visitor in dataList) {
                val allFace = FaceItem()
                allFace.withFaceType(FaceItem.FACE_TYPE_ACQUAINTANCE)
                allFace.withVisitor(visitor)
                list.add(allFace)
            }
        }
        FaceItemsProvider.get.populateItems(list)
        val uiList = ArrayList(faceAdapter.dataItems)
        val mayBeList = ArrayList(provideData())
        mayBeList.removeAll(uiList)
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

    private fun showHeaderFacePopMenu(gPosition: Int, position: Int, faceItem: View, faceType: Int) {
//        AppLogger.w("showHeaderFacePopMenu:$position,item:$faceItem")
        val view = View.inflate(context, R.layout.layout_face_page_pop_menu, null)

        // TODO: 2017/10/9 查看和识别二选一 ,需要判断,并且只有人才有查看识别二选一
        when (faceType) {
            FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                view.findViewById(R.id.detect).visibility = View.GONE
            }
            FaceItem.FACE_TYPE_STRANGER, FaceItem.FACE_TYPE_STRANGER_SUB -> {
                view.findViewById(R.id.viewer).visibility = View.GONE
            }
        }


        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWindow = PopupWindow(view, view.measuredWidth, view.measuredHeight)
        popupWindow.setBackgroundDrawable(ColorDrawable(0))
        popupWindow.isOutsideTouchable = true

        val contentView = popupWindow.contentView

        contentView.findViewById(R.id.delete).setOnClickListener { v ->
            // TODO: 2017/10/9 删除操作
            AppLogger.w("将删除面孔")
            popupWindow.dismiss()
            when (faceType) {
                FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                    val item = FaceItemsProvider.get.visitorItems[position]
                    showDeleteFaceAlert(item)
                }
                FaceItem.FACE_TYPE_STRANGER_SUB -> {
                    val item = FaceItemsProvider.get.strangerItems[position]
                    showDeleteFaceAlert(item)
                }
            }
        }

        contentView.findViewById(R.id.detect).setOnClickListener { v ->
            // TODO: 2017/10/9 识别操作
            AppLogger.w("将识别面孔")
            popupWindow.dismiss()
            val item = FaceItemsProvider.get.strangerItems[position]
            showDetectFaceAlert(item.strangerVisitor?.faceId ?: "", item.strangerVisitor?.image_url ?: "")
        }

        contentView.findViewById(R.id.viewer).setOnClickListener { _ ->
            AppLogger.w("将查看面孔详细信息")
            popupWindow.dismiss()

            val item = FaceItemsProvider.get.visitorItems[position]
            if (item != null) {
                val fragment = FaceInformationFragment.newInstance(uuid,
                        item.visitor?.detailList?.get(0)?.imgUrl ?: "",
                        item.visitor?.personName ?: "",
                        item.visitor?.personId ?: "")
                ActivityUtils.addFragmentSlideInFromRight(activity.supportFragmentManager, fragment, android.R.id.content)
            } else {
                // TODO: 2017/10/16 为什么会出现这种情况?
            }
        }
        PopupWindowCompat.showAsDropDown(popupWindow, faceItem, 0, 0, Gravity.START)
    }

    private fun showDetectFaceAlert(faceId: String, imageUrl: String) {
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
                val fragment = CreateNewFaceFragment.newInstance(uuid, faceId, imageUrl)
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
                when (item.getFaceType()) {
                    FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                        presenter.deleteFace(2, item.visitor?.personId!!, 0)
                    }
                    FaceItem.FACE_TYPE_STRANGER_SUB -> {
                        presenter.deleteFace(1, item.strangerVisitor?.faceId!!, 0)

                    }
                }

            } else if (radioButtonId == R.id.delete_face_and_message) {
                AppLogger.w("face and message")
                when (item.getFaceType()) {
                    FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                        presenter.deleteFace(2, item.visitor?.personId!!, 1)
                    }
                    FaceItem.FACE_TYPE_STRANGER_SUB -> {
                        presenter.deleteFace(1, item.strangerVisitor?.faceId!!, 1)
                    }
                }
            } else {
                // 什么也没选
            }
            dialog.dismiss()
        }

    }

    interface ItemClickListener {

        fun itemClick(item: FaceItem, globalPosition: Int, position: Int, pageIndex: Int)
        fun itemLongClick(globalPosition: Int, _p: Int, _v: View, faceType: Int, pageIndex: Int)
    }

}// Required empty public constructor

class FaceFastItemAdapter : ItemAdapter<FaceItem>()

class FaceAdapter(private var fm: FragmentManager?, private var isNormalVisitor: Boolean) : FragmentPagerAdapter(fm) {

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

    fun updateClickItem(position: Int, pageIndex: Int) {
        if (preClickPage == pageIndex) {
            preClickPosition = position//同一个page,自动刷新。
            return
        }
        val list = fm?.fragments
        if (list != null) {
            val cnt = ListUtils.getSize(list)
            Log.d("cnt", "cnt prePageIndex:$preClickPage,$preClickPosition")
            (0 until cnt).filter { it == preClickPage }.forEach {
                (list[it] as FaceFragment).adapter?.deselect(preClickPosition)
            }
        }
        preClickPage = pageIndex
        preClickPosition = position
    }

    override fun getItem(position: Int): Fragment {
        val f = FaceFragment.newInstance(position, uuid, isNormalVisitor)
        if (itemClickListener != null) {
            f.itemClickListener = itemClickListener
        }
//        f.itemClickListener = object : VisitorListFragmentV2.ItemClickListener {
//            override fun itemLongClick(globalPosition: Int, _p: Int, _v: View, faceType: Int, pageIndex: Int) {
//
//            }
//
//            override fun itemClick(item: FaceItem, globalPosition: Int, position: Int, pageIndex: Int) {
//
//            }
//        }
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
        fun newInstance(pageIndex: Int, uuid: String, isNormalVisitor: Boolean): FaceFragment {
            val f = FaceFragment()
            val b = Bundle()
            b.putInt("pageIndex", pageIndex)
            b.putString("uuid", uuid)
            b.putBoolean("isNormalVisitor", isNormalVisitor)
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
            if (globalPosition > 1 || !arguments.getBoolean("isNormalVisitor")) {
                itemClickListener?.itemLongClick(globalPosition, _p, _v, adapter.getItem(_p).getFaceType(), pageIndex)

            }
            true
        }
        populateItems()
    }

    fun populateItems() {
        pageIndex = arguments.getInt("pageIndex")
        val list = if (arguments.getBoolean("isNormalVisitor"))
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


}

class FaceItemsProvider private constructor() {
    //熟人
    @Deprecated("outdate")
    var visitorItems = ArrayList<FaceItem>()
    //陌生人
    @Deprecated("outdate")
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
        //保留前面两个,
        val cnt = ListUtils.getSize(this.visitorItems)
        var tmpList = if (cnt > 2) this.visitorItems.subList(2, cnt) else
            ArrayList()
        tmpList.addAll(visitorItems)
        tmpList = ArrayList(TreeSet(tmpList))
        Collections.sort(tmpList)
        val finalList = getPreloadItems()
        finalList.addAll(tmpList)
        this.visitorItems = finalList
        Log.d("visitorItems", "visitorItems:" + ListUtils.getSize(this.visitorItems))
    }

    private fun getPreloadItems(): ArrayList<FaceItem> {
        ensurePreloadHeaderItem()
        return ArrayList(this.visitorItems.subList(0, 2))
    }

    fun populateStrangerItems(strangerItems: ArrayList<FaceItem>) {
        if (this.strangerItems == null)
            this.strangerItems = ArrayList()
        if (ListUtils.isEmpty(strangerItems)) return
        this.strangerItems.addAll(strangerItems)
        this.strangerItems = ArrayList(TreeSet(this.strangerItems))
        Collections.sort(this.strangerItems)
    }

    private fun checkEmpty() {
        if (visitorItems == null)
            visitorItems = ArrayList()
    }

    fun ensurePreloadHeaderItem() {
        if (!(hasPreloadFaceItems())) {
            checkEmpty()
            val allFace = FaceItem()
            allFace.withSetSelected(true)
            allFace.withFaceType(FaceItem.FACE_TYPE_ALL)
            visitorItems.add(0, allFace)
            val strangerFace = FaceItem()
            strangerFace.withFaceType(FaceItem.FACE_TYPE_STRANGER)
            visitorItems.add(1, strangerFace)
        }
    }

    private fun hasPreloadFaceItems(): Boolean {
        if (ListUtils.getSize(visitorItems) < 2) return false
        return visitorItems[0].getFaceType() == FaceItem.FACE_TYPE_ALL
                && visitorItems[1].getFaceType() == FaceItem.FACE_TYPE_STRANGER
    }

}
