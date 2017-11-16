package com.cylan.jiafeigou.n.view.cam


import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
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
import com.cylan.jiafeigou.n.base.BaseApplication
import com.cylan.jiafeigou.n.base.IBaseFragment
import com.cylan.jiafeigou.n.mvp.contract.cam.VisitorListContract
import com.cylan.jiafeigou.n.mvp.impl.cam.BaseVisitorPresenter
import com.cylan.jiafeigou.n.view.cam.item.FaceItem
import com.cylan.jiafeigou.server.cache.KeyValueStringItem
import com.cylan.jiafeigou.server.cache.longHash
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.ListUtils
import com.cylan.jiafeigou.utils.NetUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.cylan.jiafeigou.widget.page.EViewPager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.fragment_visitor_list.*


/**
 * A simple [Fragment] subclass.
 * Use the [VisitorListFragmentV2.newInstance] factory method to
 * create an instance of this fragment.
 */
open class VisitorListFragmentV2 : IBaseFragment<VisitorListContract.Presenter>(),
        VisitorListContract.View {

    override fun onDeleteFaceSuccess(type: Int, delMsg: Int) {
        AppLogger.w("删除面孔消息成功了")
        ToastUtil.showToast(getString(R.string.DELETED_SUC))
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


    override fun onVisitsTimeRsp(faceId: String, cnt: Int, type: Int) {
        setFaceVisitsCounts(faceId, cnt)
    }


    var visitorListener: VisitorListener? = null

    private lateinit var faceAdapter: FaceAdapter
    private lateinit var strangerAdapter: FaceAdapter
    private var isLoadCache = false
    private var currentItem: FaceItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = BaseVisitorPresenter(this)
    }

    private fun restoreCache() {
        val boxFor = BaseApplication.getBoxStore().boxFor(KeyValueStringItem::class)
        val valueItem = boxFor["${VisitorListFragmentV2::javaClass.name}:$uuid:faceAdapter:dateItems".longHash()]
        val valueItem1 = boxFor["${VisitorListFragmentV2::javaClass.name}:$uuid:faceStrangerAdapter:dateItems".longHash()]
        valueItem?.value?.apply {
            val item = Gson().fromJson<List<FaceItem>>(this, object : TypeToken<List<FaceItem>>() {}.type)
            item.forEach { it.withSetSelected(false) }
            onVisitorListReady(item.toMutableList())
        }
        valueItem1?.value?.apply {
            val item1 = Gson().fromJson<List<FaceItem>>(this, object : TypeToken<List<FaceItem>>() {}.type)
            item1.forEach { it.withSetSelected(false) }
            strangerAdapter.populateItems(item1)
        }
    }

    override fun onPause() {
        super.onPause()
        saveCache()
    }

    private fun saveCache() {
        val boxFor = BaseApplication.getBoxStore().boxFor(KeyValueStringItem::class)
        val gson = Gson()
        faceAdapter.dataItems.drop(2).apply {
            boxFor.put(KeyValueStringItem("${VisitorListFragmentV2::javaClass.name}:$uuid:faceAdapter:dateItems".longHash(), gson.toJson(this)))
        }
        boxFor.put(KeyValueStringItem("${VisitorListFragmentV2::javaClass.name}:$uuid:faceStrangerAdapter:dateItems".longHash(), gson.toJson(strangerAdapter.dataItems)))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_visitor_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        faceAdapter = FaceAdapter(true)
        faceAdapter.uuid = uuid
        strangerAdapter = FaceAdapter(false)
        strangerAdapter.uuid = uuid
//        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        vp_default.adapter = faceAdapter
        val itemClickListener: ItemClickListener = object : ItemClickListener {
            override fun itemClick(item: FaceItem, globalPosition: Int, position: Int, pageIndex: Int) {
                val adapter = vp_default.adapter as FaceAdapter?
                val faceItem = adapter?.dataItems?.get(globalPosition)
                visitorListener?.onLoadItemInformation(item)
                currentItem = faceItem
                (vp_default.adapter as FaceAdapter?)?.updateClickItem(globalPosition)
                when (item.getFaceType()) {
                    FaceItem.FACE_TYPE_ALL -> {
                        presenter.fetchVisitorList()
                        cam_message_indicator_watcher_text.visibility = View.VISIBLE
                        presenter.fetchVisitsCount("", FILTER_TYPE_ALL)
                    }
                    FaceItem.FACE_TYPE_STRANGER -> {
                        cam_message_indicator_watcher_text.visibility = View.GONE
                        vp_default.adapter = strangerAdapter
                        setFaceHeaderPageIndicator(vp_default.currentItem, strangerAdapter?.dataItems?.size)
                        presenter.fetchStrangerVisitorList()
                    }
                    FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                        val faceId = if (adapter?.isNormalVisitor == true) item.visitor?.personId else item.strangerVisitor?.faceId
                        AppLogger.w("主列表的 faceId?personId")
                        cam_message_indicator_watcher_text.visibility = View.VISIBLE
                        presenter.fetchVisitsCount(faceId!!, FILTER_TYPE_ACQUAINTANCE)
                    }
                    FaceItem.FACE_TYPE_STRANGER_SUB -> {
                        val faceId = if (adapter?.isNormalVisitor == true) item.visitor?.personId else item.strangerVisitor?.faceId
                        AppLogger.w("主列表的 faceId?personId")
                        cam_message_indicator_watcher_text.visibility = View.VISIBLE
                        presenter.fetchVisitsCount(faceId!!, FILTER_TYPE_STRANGER)
                    }
                }
            }

            override fun itemLongClick(globalPosition: Int, _p: Int, _v: View, faceType: Int, pageIndex: Int) {
                val adapter = vp_default.adapter as FaceAdapter?
                if (adapter != null) {
                    val faceItem = adapter.dataItems[globalPosition]
                    visitorListener?.onLoadItemInformation(faceItem)
                    currentItem = faceItem
                    showHeaderFacePopMenu(faceItem, _p, _v, faceType)
                    adapter.updateClickItem(globalPosition)
                }
            }

        }
        faceAdapter.itemClickListener = itemClickListener
        strangerAdapter.itemClickListener = itemClickListener

        vp_default.enableScrollListener = EViewPager.EnableScrollListener { false }
        cam_message_indicator_holder.visibility = View.VISIBLE
        val count = (vp_default.adapter as FaceAdapter).getItemSize()
//        val position = layoutManager.findFirstCompletelyVisibleItemPosition();
        setFaceHeaderPageIndicator(vp_default.currentItem, count)
        vp_default.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                val itemSize = (vp_default.adapter as FaceAdapter).getItemSize()
                setFaceHeaderPageIndicator(position, itemSize)
            }

        })

        if (!isLoadCache && NetUtils.getNetType(context) == -1) {
            isLoadCache = true
            restoreCache()
        } else {
            refreshContent()
        }
    }

    private fun setFaceHeaderPageIndicator(currentItem: Int, total: Int) {
        cam_message_indicator_holder.visibility = if (total > 0) View.VISIBLE else View.GONE
        cam_message_indicator_page_text.text = String.format("%s/%s", currentItem + 1, total / 6 + if (total % 6 == 0) 0 else 1)
        cam_message_indicator_page_text.visibility = if (total > 3) View.VISIBLE else View.GONE
    }

    private fun setFaceVisitsCounts(faceId: String, count: Int) {
        cam_message_indicator_holder.visibility = View.VISIBLE
        cam_message_indicator_watcher_text.visibility = View.VISIBLE
        val faceType = currentItem?.getFaceType() ?: FaceItem.FACE_TYPE_EMPTY
        when (faceType) {
            FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                if (TextUtils.equals(faceId, currentItem?.visitor?.personId)) {
                    cam_message_indicator_watcher_text.text = getString(R.string.MESSAGES_FACE_VISIT_TIMES, count.toString())
                }
            }
            FaceItem.FACE_TYPE_STRANGER_SUB -> {
                if (TextUtils.equals(faceId, currentItem?.strangerVisitor?.faceId)) {
                    cam_message_indicator_watcher_text.text = getString(R.string.MESSAGES_FACE_VISIT_TIMES, count.toString())
                }
            }
            FaceItem.FACE_TYPE_ALL -> {
                if (TextUtils.isEmpty(faceId)) {
                    cam_message_indicator_watcher_text.text = String.format("今天来访%s人", count)
                }
            }
        }
    }

    override fun onVisitorListReady(visitorList: MutableList<FaceItem>) {
        if (!(vp_default.adapter as FaceAdapter).isNormalVisitor) {
            vp_default.adapter = faceAdapter
        }

        faceAdapter.populateItems(visitorList)
        faceAdapter.updateClickItem(0)
        cam_message_indicator_watcher_text.visibility = View.VISIBLE
        setFaceHeaderPageIndicator(vp_default.currentItem, (vp_default.adapter as FaceAdapter).getItemSize())

        val newPos = Math.min(vp_default.currentItem * 6, faceAdapter.dataItems.size - 1)
        currentItem = faceAdapter.dataItems[newPos]
        presenter.fetchVisitsCount("", FILTER_TYPE_ALL)
        visitorListener?.onVisitorReady(visitorList)
        visitorListener?.onLoadItemInformation(currentItem!!)
    }

    open fun exitStranger() {
//        face_header.ad(faceAdapter, true)
        vp_default.adapter = faceAdapter
        faceAdapter.updateClickItem(0)
        presenter.fetchVisitorList()
        cam_message_indicator_holder.visibility = View.VISIBLE
        cam_message_indicator_watcher_text.text = ""
        cam_message_indicator_watcher_text.visibility = View.GONE
    }

    override fun onStrangerVisitorListReady(visitorList: MutableList<FaceItem>) {
        AppLogger.d("陌生人列表")
        if ((vp_default.adapter as FaceAdapter).isNormalVisitor) {
            vp_default.adapter = strangerAdapter
        }
        strangerAdapter.populateItems(visitorList)
        strangerAdapter.updateClickItem(0)
//        vp_default.swapAdapter(strangerAdapter, true)
        setFaceHeaderPageIndicator(vp_default.currentItem, (vp_default.adapter as FaceAdapter).getItemSize())
        visitorListener?.onStrangerVisitorReady(visitorList)
        if (strangerAdapter.dataItems.size > 0) {
            val newPos = Math.min(vp_default.currentItem * 6, strangerAdapter.dataItems.size - 1)
            currentItem = strangerAdapter.dataItems[newPos]
            cam_message_indicator_watcher_text.visibility = View.VISIBLE
            presenter.fetchVisitsCount(currentItem?.strangerVisitor?.faceId!!, FILTER_TYPE_STRANGER)
            visitorListener?.onLoadItemInformation(currentItem!!)
        }
    }

    open fun refreshContent() {
        val adapter = vp_default?.adapter as FaceAdapter?
        if (adapter?.isNormalVisitor == true) {
            presenter?.fetchVisitorList()
        } else {
            presenter?.fetchStrangerVisitorList()
        }
    }

    open fun disable(disable: Boolean) {
        if (disable) {
            cover_layer.visibility = View.VISIBLE
        } else {
            cover_layer.visibility = View.INVISIBLE
        }
    }

    companion object {
        const val FILTER_TYPE_ALL = 5
        const val FILTER_TYPE_STRANGER = 1
        const val FILTER_TYPE_ACQUAINTANCE = 2
        fun newInstance(uuid: String): VisitorListFragmentV2 {
            val fragment = VisitorListFragmentV2()
            val args = Bundle()
            args.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = args
            return fragment
        }
    }

    private fun showHeaderFacePopMenu(item: FaceItem, position: Int, faceItem: View, faceType: Int) {
//        AppLogger.w("showHeaderFacePopMenu:$position,item:$faceItem")
        val contentView = View.inflate(context, R.layout.layout_face_page_pop_menu, null)
        // TODO: 2017/10/9 查看和识别二选一 ,需要判断,并且只有人才有查看识别二选一
        when (faceType) {
            FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                contentView.findViewById(R.id.detect).visibility = View.GONE
            }
            FaceItem.FACE_TYPE_STRANGER, FaceItem.FACE_TYPE_STRANGER_SUB -> {
                contentView.findViewById(R.id.viewer).visibility = View.GONE
            }
        }


        val popupWindow = PopupWindow(context)
        popupWindow.setBackgroundDrawable(ColorDrawable(0))
        popupWindow.isOutsideTouchable = true
        popupWindow.contentView = contentView
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
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
            showDetectFaceAlert(item.strangerVisitor)
        }

        contentView.findViewById(R.id.viewer).setOnClickListener { _ ->
            AppLogger.w("将查看面孔详细信息")
            popupWindow.dismiss()

            if (item != null) {
                val fragment = FaceInformationFragment.newInstance(uuid, item.visitor)
                ActivityUtils.addFragmentSlideInFromRight(activity.supportFragmentManager, fragment, android.R.id.content)
            } else {
                // TODO: 2017/10/16 为什么会出现这种情况?
            }
        }
//        popupWindow.showAsDropDown(faceItem.findViewById(R.id.img_item_face_selection))
        val anchor = faceItem.findViewById(R.id.img_item_face_selection)
//        showAsDropDown(popupWindow, anchor, 0, 0)
        var position = IntArray(2)
        anchor.getLocationOnScreen(position)
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, position[0], position[1] + anchor.measuredHeight)
        PopupWindowCompat.showAsDropDown(popupWindow, anchor, 0, 0, Gravity.NO_GRAVITY)
    }

    private fun showDetectFaceAlert(strangerVisitor: DpMsgDefine.StrangerVisitor?) {
        val dialog = AlertDialog.Builder(context)
                .setView(R.layout.layout_face_detect_pop_alert)
                .show()

        dialog.findViewById(R.id.detect_cancel)!!.setOnClickListener { v -> dialog.dismiss() }

        dialog.findViewById(R.id.detect_ok)!!.setOnClickListener { v ->
            val addTo = dialog.findViewById(R.id.detect_add_to) as RadioButton?
            val newFace = dialog.findViewById(R.id.detect_new_face) as RadioButton?
            if (addTo!!.isChecked) {
                val fragment = FaceListFragment.newInstance(DataSourceManager.getInstance().account.account,
                        uuid, strangerVisitor?.faceId ?: "", FaceListFragment.TYPE_ADD_TO)
                fragment.resultCallback = { o, o2, o3 ->
                    presenter.fetchStrangerVisitorList()

                }// TODO: 2017/10/10 移动到面孔的结果回调
                ActivityUtils.addFragmentSlideInFromRight(activity.supportFragmentManager, fragment, android.R.id.content)
            } else if (newFace!!.isChecked) {
                val fragment = CreateNewFaceFragment.newInstance(uuid, strangerVisitor)
                fragment.resultCallback = {
                    //todo 返回创建的personID
                    presenter.fetchStrangerVisitorList()
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

    interface VisitorListener {
        fun onLoadItemInformation(item: FaceItem)
        fun onStrangerVisitorReady(visitorList: MutableList<FaceItem>)
        fun onVisitorReady(visitorList: MutableList<FaceItem>)
    }

}// Required empty public constructor

class FaceFastItemAdapter : ItemAdapter<FaceItem>()
class ViewHolder(val itemview: View) {
    val rvList: RecyclerView = itemview.findViewById(R.id.message_face_page_item) as RecyclerView
    val visitorAdapter = FaceFastItemAdapter()
    val adapter = FastAdapter<FaceItem>()
    var itemClickListener: VisitorListFragmentV2.ItemClickListener? = null
    private var isNormalVisitor: Boolean = true
    private var items: List<FaceItem>? = null
    private var pageIndex: Int = 0

    fun bindItem(pageIndex: Int, isNormalVisitor: Boolean, items: List<FaceItem>, itemClickListener: VisitorListFragmentV2.ItemClickListener) {
        this.itemClickListener = itemClickListener
        this.isNormalVisitor = isNormalVisitor
        this.items = items
        this.pageIndex = pageIndex
        visitorAdapter.setNewList(items)
        adapter.notifyDataSetChanged()
    }

    init {
        rvList.layoutManager = GridLayoutManager(itemview.context, 3)
        rvList.adapter = visitorAdapter.wrap(adapter)
        adapter.withSelectable(true)
        adapter.withMultiSelect(false)
        adapter.withSelectOnLongClick(true)
        adapter.withSelectWithItemUpdate(true)
        adapter.withAllowDeselection(false)
        rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, v: View, parent: RecyclerView, state: RecyclerView.State?) {
//                if (parent.getChildLayoutPosition(v) % 3 == 1) {
//                    val pixelOffset = itemview.context.resources.getDimensionPixelOffset(R.dimen.y18)
//                    outRect.left = pixelOffset
//                    outRect.right = pixelOffset
//                }
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
            if (globalPosition > 1 || !isNormalVisitor) {
                itemClickListener?.itemLongClick(globalPosition, _p, _v, adapter.getItem(_p).getFaceType(), pageIndex)
            }
            false
        }
    }
}

class FaceAdapter(var isNormalVisitor: Boolean) : PagerAdapter() {
    override fun isViewFromObject(view: View?, viewHolder: Any?): Boolean {
        return (viewHolder as? ViewHolder)?.itemview == view
    }

    private val cachedItems = mutableListOf<ViewHolder>()


    override fun getCount(): Int {
        return JConstant.getPageCnt(ListUtils.getSize(dataItems))
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val viewHolder = if (cachedItems.size > 0) {
            cachedItems.removeAt(0)
        } else {
            val inflate = LayoutInflater.from(container.context).inflate(R.layout.message_face_page, container, false)
            ViewHolder(inflate)
        }
        val start = JConstant.FACE_CNT_IN_PAGE * position
        val end = Math.min(dataItems.size, start + JConstant.FACE_CNT_IN_PAGE)
        AppLogger.e("start:$start,end:$end")
        val list = (JConstant.FACE_CNT_IN_PAGE * position until Math.min(dataItems.size, start + JConstant.FACE_CNT_IN_PAGE)).map { dataItems[it] }
        viewHolder.bindItem(position, isNormalVisitor, list, itemClickListener)
        container.addView(viewHolder.itemview)
        return viewHolder
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()

    }

    override fun destroyItem(container: ViewGroup, position: Int, viewHolder: Any?) {
        container.removeView((viewHolder as ViewHolder).itemview)
        cachedItems.add(viewHolder)
    }

    override fun getItemPosition(`object`: Any?): Int {
        return POSITION_NONE
    }


    lateinit var uuid: String
    lateinit var itemClickListener: VisitorListFragmentV2.ItemClickListener

    private var preloadItems = mutableListOf<FaceItem>()
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
            } else {
                faceItem.withSetSelected(true)
            }
        }
        notifyDataSetChanged()
    }
}
