package com.cylan.jiafeigou.n.view.cam


import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
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
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.android.synthetic.main.fragment_visitor_list.*
import java.util.*


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
    lateinit var strangerAdapter: FaceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = BaseVisitorPresenter(this)
    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_visitor_list, container, false)
    }

    private lateinit var layoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        faceAdapter = FaceAdapter(true)
        faceAdapter.uuid = uuid
        strangerAdapter = FaceAdapter(false)
        strangerAdapter.uuid = uuid
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        face_header.layoutManager = layoutManager
        face_header.adapter = faceAdapter
        val itemClickListener: ItemClickListener = object : ItemClickListener {
            override fun itemClick(item: FaceItem, globalPosition: Int, position: Int, pageIndex: Int) {
                (face_header.adapter as FaceAdapter?)?.updateClickItem(globalPosition)
                when (item.getFaceType()) {
                    FaceItem.FACE_TYPE_ALL -> {
                        presenter.fetchVisitorList()
                        cam_message_indicator_watcher_text.visibility = View.GONE
                    }
                    FaceItem.FACE_TYPE_STRANGER -> {
                        cam_message_indicator_watcher_text.visibility = View.GONE
                        presenter.fetchStrangerVisitorList()
                    }
                    FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                        val adapter = face_header.adapter as FaceAdapter?
                        val faceId = if (adapter?.isNormalVisitor == true) item.visitor?.personId else item.strangerVisitor?.faceId
                        AppLogger.d("主列表的 faceId?personId")
                        cam_message_indicator_watcher_text.visibility = View.VISIBLE
                        presenter.fetchVisitsCount(faceId!!)
                    }
                    FaceItem.FACE_TYPE_STRANGER_SUB -> {
                        val adapter = face_header.adapter as FaceAdapter?
                        val faceId = if (adapter?.isNormalVisitor == true) item.visitor?.personId else item.strangerVisitor?.faceId
                        AppLogger.d("主列表的 faceId?personId")
                        cam_message_indicator_watcher_text.visibility = View.VISIBLE
                        presenter.fetchVisitsCount(faceId!!)
                    }
                }
            }

            override fun itemLongClick(globalPosition: Int, _p: Int, _v: View, faceType: Int, pageIndex: Int) {
                val adapter = face_header.adapter as FaceAdapter?
                if (adapter != null) {
                    adapter.updateClickItem(globalPosition)
                    val faceItem = adapter.dataItems[globalPosition]
                    showHeaderFacePopMenu(faceItem, _p, _v, faceType)
                }
            }

        }
        faceAdapter.itemClickListener = itemClickListener
        strangerAdapter.itemClickListener = itemClickListener

//        vp_default.enableScrollListener = EViewPager.EnableScrollListener { false }
        cam_message_indicator_holder.visibility = View.VISIBLE
        val count = (face_header.adapter as FaceAdapter).getItemCount()
        val position = layoutManager.findFirstCompletelyVisibleItemPosition();
        setFaceHeaderPageIndicator(position, count)

        PagerSnapHelper().attachToRecyclerView(face_header)
        face_header.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val itemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                    val count = (face_header.adapter as FaceAdapter).getItemSize()
                    setFaceHeaderPageIndicator(itemPosition, count)
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        presenter.fetchVisitorList()
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

    override fun onVisitorListReady(visitorList: DpMsgDefine.VisitorList?) {
        visitorList?.dataList?.map {
            val allFace = FaceItem()
            allFace.withFaceType(FaceItem.FACE_TYPE_ACQUAINTANCE)
            allFace.withVisitor(it)
        }?.apply {
            faceAdapter.populateItems(this)
        }
        cam_message_indicator_holder.visibility = View.VISIBLE
        setFaceHeaderPageIndicator(layoutManager.findFirstCompletelyVisibleItemPosition(), (face_header.adapter as FaceAdapter).getItemSize())
    }

    open fun exitStranger() {
        face_header.swapAdapter(faceAdapter, true)
        presenter.fetchVisitorList()
    }

    override fun onVisitorListReady(visitorList: DpMsgDefine.StrangerVisitorList?) {
        AppLogger.d("陌生人列表")
        val listCnt = ListUtils.getSize(visitorList?.strangerVisitors)
        var list = ArrayList<FaceItem>()
        for (i in 0 until listCnt) {
            val strangerFace = FaceItem()
            strangerFace.withFaceType(FaceItem.FACE_TYPE_STRANGER_SUB)
            strangerFace.withStrangerVisitor(visitorList!!.strangerVisitors[i])
            strangerFace.withSetSelected(false)
            list.add(strangerFace)
        }
        strangerAdapter.populateItems(list)
        face_header.swapAdapter(strangerAdapter, true)
        cam_message_indicator_holder.visibility = View.VISIBLE
        setFaceHeaderPageIndicator(layoutManager.findFirstCompletelyVisibleItemPosition(), ListUtils.getSize(list))
        onVisitorListCallback?.onStrangeListReady()
    }

    open fun refreshContent() {
        val adapter = face_header.adapter as FaceAdapter?
        if (adapter?.isNormalVisitor == true) {
            presenter.fetchVisitorList()
        } else {
            presenter.fetchStrangerVisitorList()
        }
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

    interface OnVisitorListCallback {
        /**
         * gPosition: global position
         */
        fun onItemClick(item: FaceItem)

        fun onVisitorListReady()
        fun onPageScroll(currentItem: Int, total: Int)

        fun onVisitorTimes(times: Int)
        fun onStrangeListReady()
    }

    private fun showHeaderFacePopMenu(item: FaceItem, position: Int, faceItem: View, faceType: Int) {
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
            showDeleteFaceAlert(item)
        }

        contentView.findViewById(R.id.detect).setOnClickListener { v ->
            // TODO: 2017/10/9 识别操作
            AppLogger.w("将识别面孔")
            popupWindow.dismiss()
            showDetectFaceAlert(item.strangerVisitor?.faceId ?: "", item.strangerVisitor?.image_url ?: "")
        }

        contentView.findViewById(R.id.viewer).setOnClickListener { _ ->
            AppLogger.w("将查看面孔详细信息")
            popupWindow.dismiss()

            if (item != null) {
                val fragment = FaceInformationFragment.newInstance(uuid,
                        item.visitor?.detailList?.getOrNull(0)?.imgUrl ?: "",
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
class ViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
    val rvList: RecyclerView = itemview.findViewById(R.id.message_face_page_item) as RecyclerView
    val visitorAdapter = FaceFastItemAdapter()
    val adapter = FastAdapter<FaceItem>()
    fun bindItem(pageIndex: Int, isNormalVisitor: Boolean, items: List<FaceItem>, itemClickListener: VisitorListFragmentV2.ItemClickListener) {
        adapter.withOnClickListener { _, _, item, position ->
            val globalPosition = pageIndex * JConstant.FACE_CNT_IN_PAGE + position
            itemClickListener?.itemClick(visitorAdapter.getItem(position),
                    globalPosition, position, pageIndex)
            true
        }
        adapter.withOnLongClickListener { _v, _, _, _p ->
            val globalPosition = pageIndex * JConstant.FACE_CNT_IN_PAGE + _p
            if (globalPosition > 1 || !isNormalVisitor) {
                itemClickListener?.itemLongClick(globalPosition, _p, _v, adapter.getItem(_p).getFaceType(), pageIndex)
            }
            true
        }
        visitorAdapter.set(items)
        adapter.notifyDataSetChanged()
    }

    init {
        rvList.layoutManager = GridLayoutManager(itemview.context, 3)
        rvList.adapter = visitorAdapter.wrap(adapter)
        adapter.withSelectable(true)
        adapter.withMultiSelect(false)
        adapter.withSelectWithItemUpdate(true)
        adapter.withAllowDeselection(false)
        rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, v: View, parent: RecyclerView, state: RecyclerView.State?) {
                if (parent.getChildLayoutPosition(v) % 3 == 1) {
                    val pixelOffset = itemview.context.resources.getDimensionPixelOffset(R.dimen.y18)
                    outRect.left = pixelOffset
                    outRect.right = pixelOffset
                }
            }
        })
    }
}

class FaceAdapter(var isNormalVisitor: Boolean) : RecyclerView.Adapter<ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val start = JConstant.FACE_CNT_IN_PAGE * position
        val end = Math.min(dataItems.size, start + JConstant.FACE_CNT_IN_PAGE)
        AppLogger.e("start:$start,end:$end")
        val list = (JConstant.FACE_CNT_IN_PAGE * position until Math.min(dataItems.size, start + JConstant.FACE_CNT_IN_PAGE)).map { dataItems[it] }
        holder.bindItem(position, isNormalVisitor, list, itemClickListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflate = LayoutInflater.from(parent.context).inflate(R.layout.message_face_page, parent, false)
        val viewHolder = ViewHolder(inflate)
        return viewHolder
    }


    lateinit var uuid: String
    lateinit var itemClickListener: VisitorListFragmentV2.ItemClickListener

    var preloadItems = mutableListOf<FaceItem>()
    var dataItems = mutableListOf<FaceItem>()

    init {
        val allFace = FaceItem()
        allFace.withSetSelected(true)
        allFace.withFaceType(FaceItem.FACE_TYPE_ALL)
        preloadItems.add(allFace)

        val strangerFace = FaceItem()
        strangerFace.withFaceType(FaceItem.FACE_TYPE_STRANGER)
        preloadItems.add(strangerFace)
        if (isNormalVisitor) {
            dataItems.addAll(preloadItems)
        }
    }

    override fun getItemCount(): Int {
        return JConstant.getPageCnt(ListUtils.getSize(dataItems))
    }

    fun getItemSize(): Int {
        return dataItems?.size ?: 0
    }

    fun populateItems(dataItems: List<FaceItem>) {
        this.dataItems.clear()
        if (isNormalVisitor) {
            this.dataItems.addAll(preloadItems)
        }
        this.dataItems.addAll(dataItems)
        notifyDataSetChanged()
    }

    fun updateClickItem(position: Int) {
        dataItems.forEachIndexed { index, faceItem ->
            if (index != position) {
                faceItem.withSetSelected(false)
            }
        }
        notifyDataSetChanged()
    }
}

//
//class FaceFragment : Fragment() {
//
//    var visitorAdapter = FaceFastItemAdapter(this, pageIndex)
//    lateinit var rvList: RecyclerView
//    lateinit var uuid: String
//    var pageIndex: Int = 0
//    lateinit var itemClickListener: VisitorListFragmentV2.ItemClickListener
//    var adapter = FastAdapter<FaceItem>()
//
//    companion object {
//        fun newInstance(pageIndex: Int, uuid: String, isNormalVisitor: Boolean): FaceFragment {
//            val f = FaceFragment()
//            val b = Bundle()
//            b.putInt("pageIndex", pageIndex)
//            b.putString("uuid", uuid)
//            b.putBoolean("isNormalVisitor", isNormalVisitor)
//            f.arguments = b
//            return f
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        this.uuid = arguments.getString("uuid")
//    }
//
//
//    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        // Inflate the layout for this fragment
//        return inflater!!.inflate(R.layout.message_face_page, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        rvList = view.findViewById(R.id.message_face_page_item) as RecyclerView
//        rvList.layoutManager = GridLayoutManager(context, 3)
//        rvList.adapter = visitorAdapter.wrap(adapter)
//        adapter.withSelectable(true)
//        adapter.withMultiSelect(false)
//        adapter.withSelectWithItemUpdate(true)
//        adapter.withAllowDeselection(false)
//        rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
//            override fun getItemOffsets(outRect: Rect, v: View, parent: RecyclerView, state: RecyclerView.State?) {
//                if (parent.getChildLayoutPosition(v) % 3 == 1) {
//                    val pixelOffset = context.resources.getDimensionPixelOffset(R.dimen.y18)
//                    outRect.left = pixelOffset
//                    outRect.right = pixelOffset
//                }
//            }
//        })
//        adapter.withOnClickListener { _, _, item, position ->
//            val globalPosition = pageIndex * JConstant.FACE_CNT_IN_PAGE + position
//            itemClickListener?.itemClick(visitorAdapter.getItem(position),
//                    globalPosition, position, pageIndex)
//            true
//        }
//        adapter.withOnLongClickListener { _v, _, _, _p ->
//            val globalPosition = pageIndex * JConstant.FACE_CNT_IN_PAGE + _p
//            if (globalPosition > 1 || !arguments.getBoolean("isNormalVisitor")) {
//                itemClickListener?.itemLongClick(globalPosition, _p, _v, adapter.getItem(_p).getFaceType(), pageIndex)
//
//            }
//            true
//        }
//        populateItems()
//    }
//
//    fun populateItems() {
//        pageIndex = arguments.getInt("pageIndex")
//        val list = if (arguments.getBoolean("isNormalVisitor"))
//            FaceItemsProvider.get.visitorItems else FaceItemsProvider.get.strangerItems
//        val totalCnt = ListUtils.getSize(list)
//        if (totalCnt == 0) {
//            return
//        }
//        if (pageIndex >= JConstant.getPageCnt(totalCnt)) {
//            //viewPager中的fragment会被缓存
//            Log.d("cnt", "bad")
//            return
//        }
//        val lastIndex = JConstant.FACE_CNT_IN_PAGE * pageIndex + Math.min(JConstant.FACE_CNT_IN_PAGE, totalCnt - JConstant.FACE_CNT_IN_PAGE * pageIndex)
//        Log.d("cnt", "pre cnt,,," + ListUtils.getSize(list) + ",pageIndex:" + pageIndex + ",lastIndex:" + lastIndex)
//        val subList = list.subList(JConstant.FACE_CNT_IN_PAGE * pageIndex, lastIndex)
//        Log.d("cnt", "cnt,,," + ListUtils.getSize(subList) + ",pageIndex:" + pageIndex)
//        visitorAdapter.clear()
//        visitorAdapter.add(subList)
//    }
//
//
//}

//class FaceItemsProvider private constructor() {
//    //熟人
//    @Deprecated("outdate")
//    var visitorItems = ArrayList<FaceItem>()
//    //陌生人
//    @Deprecated("outdate")
//    var strangerItems = ArrayList<FaceItem>()
//
//    private object Holder {
//        val INSTANCE = FaceItemsProvider()
//    }
//
//    companion object {
//        val get: FaceItemsProvider by lazy { Holder.INSTANCE }
//    }
//
//    fun populateItems(visitorItems: ArrayList<FaceItem>) {
//        checkEmpty()
//        ensurePreloadHeaderItem()
//        if (ListUtils.isEmpty(visitorItems)) return
//        //保留前面两个,
//        val cnt = ListUtils.getSize(this.visitorItems)
//        var tmpList = if (cnt > 2) this.visitorItems.subList(2, cnt) else
//            ArrayList()
//        tmpList.addAll(visitorItems)
//        tmpList = ArrayList(TreeSet(tmpList))
//        Collections.sort(tmpList)
//        val finalList = getPreloadItems()
//        finalList.addAll(tmpList)
//        this.visitorItems = finalList
//        Log.d("visitorItems", "visitorItems:" + ListUtils.getSize(this.visitorItems))
//    }
//
//    private fun getPreloadItems(): ArrayList<FaceItem> {
//        ensurePreloadHeaderItem()
//        return ArrayList(this.visitorItems.subList(0, 2))
//    }
//
//    fun populateStrangerItems(strangerItems: ArrayList<FaceItem>) {
//        if (this.strangerItems == null)
//            this.strangerItems = ArrayList()
//        if (ListUtils.isEmpty(strangerItems)) return
//        this.strangerItems.addAll(strangerItems)
//        this.strangerItems = ArrayList(TreeSet(this.strangerItems))
//        Collections.sort(this.strangerItems)
//    }
//
//    private fun checkEmpty() {
//        if (visitorItems == null)
//            visitorItems = ArrayList()
//    }
//
//    fun ensurePreloadHeaderItem() {
//        if (!(hasPreloadFaceItems())) {
//            checkEmpty()
//            val allFace = FaceItem()
//            allFace.withSetSelected(true)
//            allFace.withFaceType(FaceItem.FACE_TYPE_ALL)
//            visitorItems.add(0, allFace)
//            val strangerFace = FaceItem()
//            strangerFace.withFaceType(FaceItem.FACE_TYPE_STRANGER)
//            visitorItems.add(1, strangerFace)
//        }
//    }
//
//    private fun hasPreloadFaceItems(): Boolean {
//        if (ListUtils.getSize(visitorItems) < 2) return false
//        return visitorItems[0].getFaceType() == FaceItem.FACE_TYPE_ALL
//                && visitorItems[1].getFaceType() == FaceItem.FACE_TYPE_STRANGER
//    }
//
//}
