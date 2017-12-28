package com.cylan.jiafeigou.n.view.cam


import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import butterknife.ButterKnife
import butterknife.OnClick
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.module.DataSourceManager
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper
import com.cylan.jiafeigou.cache.db.module.KeyValue
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.base.IBaseFragment
import com.cylan.jiafeigou.n.mvp.contract.cam.VisitorListContract
import com.cylan.jiafeigou.n.mvp.impl.cam.BaseVisitorPresenter
import com.cylan.jiafeigou.n.view.cam.item.FaceItem
import com.cylan.jiafeigou.n.view.cam.item.LoadMoreItem
import com.cylan.jiafeigou.server.cache.longHash
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.ActivityUtils
import com.cylan.jiafeigou.utils.NetUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.cylan.jiafeigou.utils.ViewUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
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
                presenter.fetchStrangerVisitorList(0)
            }
            2 -> {
                //熟人
                presenter.fetchVisitorList(0)
            }
        }

    }

    override fun onDeleteFaceError() {
        AppLogger.w("删除面孔消息失败了")
        ToastUtil.showToast(getString(R.string.Tips_DeleteFail))

    }


    override fun onVisitsTimeRsp(faceId: String, cnt: Int, type: Int) {
        setFaceVisitsCounts(faceId, cnt, type)
    }


    var visitorListener: VisitorListener? = null

    private lateinit var faceAdapter: FaceAdapter
    private var footerAdapter: ItemAdapter<LoadMoreItem> = ItemAdapter.items<LoadMoreItem>()


    private val allFace = FaceItem().withSetSelected(true).withFaceType(FaceItem.FACE_TYPE_ALL)
    private val strangerFace = FaceItem().withFaceType(FaceItem.FACE_TYPE_STRANGER)
    private val moreItem = LoadMoreItem()
    private val preloadItems = listOf(allFace, strangerFace)

    private val visitorItems = mutableListOf<FaceItem>().apply {
        addAll(preloadItems)
    }

    private val strangerItems = mutableListOf<FaceItem>()
    private var isLoadCache = false
    private var visitorCountMap = mutableMapOf<String, Int>()
    @Volatile
    private var currentPosition: Int = 0

    private var isExpanded = false

    private var isLoadingFinished = true

    private class FaceAdapter(var isNormalView: Boolean) : FastItemAdapter<FaceItem>()

    private lateinit var gridLayoutManager: GridLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = BaseVisitorPresenter(this)
    }

    private fun restoreCache() {

        val keyValueDao = BaseDBHelper.getInstance().daoSession.keyValueDao
        keyValueDao.loadByRowId("${VisitorListFragmentV2::javaClass.name}:$uuid:faceAdapter:dateItems".longHash())

//        val boxFor = BaseApplication.getBoxStore().boxFor(KeyValueStringItem::class)
        val valueItem = keyValueDao.loadByRowId("${VisitorListFragmentV2::javaClass.name}:$uuid:faceAdapter:dateItems".longHash())// boxFor["${VisitorListFragmentV2::javaClass.name}:$uuid:faceAdapter:dateItems".longHash()]
        val valueItem1 = keyValueDao.loadByRowId("${VisitorListFragmentV2::javaClass.name}:$uuid:faceStrangerAdapter:dateItems".longHash())//boxFor["${VisitorListFragmentV2::javaClass.name}:$uuid:faceStrangerAdapter:dateItems".longHash()]
        valueItem?.value?.apply {
            val item = Gson().fromJson<List<FaceItem>>(this, object : TypeToken<List<FaceItem>>() {}.type)
            item.forEach { it.withSetSelected(false) }
            onVisitorListReady(item.toMutableList(), 0)
        }
        valueItem1?.value?.apply {
            val item1 = Gson().fromJson<List<FaceItem>>(this, object : TypeToken<List<FaceItem>>() {}.type)
            item1.forEach { it.withSetSelected(false) }
            strangerItems.addAll(item1)
        }
    }

    override fun onPause() {
        super.onPause()
        saveCache()
    }

    private fun makeContentView(isNormalView: Boolean) {
        if (isNormalView && faceAdapter.isNormalView) {
            currentPosition = 0
            faceAdapter.set(visitorItems)
            faceAdapter.select(0)
        } else if (!isNormalView && !faceAdapter.isNormalView) {
            currentPosition = 0
            faceAdapter.set(strangerItems)
            faceAdapter.select(0)
        }
        resizeContentHeight()
    }

    private fun resizeContentHeight() {
        val layoutParams = view!!.layoutParams
        val oldHeight = layoutParams.height
        layoutParams.height = when {
            faceAdapter.adapterItemCount > 3 || isExpanded -> {
                //进入消息界面，当头像区域显示有两排时才显示右侧的更多控件。实际结果：一排也显示了更多的控件
                more_text.visibility = View.VISIBLE
                ViewGroup.LayoutParams.MATCH_PARENT
            }
            faceAdapter.adapterItemCount > 0 -> {
                //进入消息界面，当头像区域显示有两排时才显示右侧的更多控件。实际结果：一排也显示了更多的控件
                more_text.visibility = View.INVISIBLE
                resources.getDimensionPixelSize(R.dimen.y160)
            }
            else -> {
                //进入消息界面，当头像区域显示有两排时才显示右侧的更多控件。实际结果：一排也显示了更多的控件
                more_text.visibility = View.INVISIBLE
                resources.getDimensionPixelSize(R.dimen.y0)
            }
        }
        if (oldHeight != layoutParams.height) {
            view!!.layoutParams = layoutParams
        }
    }


    private fun saveCache() {
        val keyValueDao = BaseDBHelper.getInstance().daoSession.keyValueDao;
//        val boxFor = BaseApplication.getBoxStore().boxFor(KeyValueStringItem::class)
        val gson = Gson()
        visitorItems.drop(2).apply {
            keyValueDao.insertOrReplace(KeyValue("${VisitorListFragmentV2::javaClass.name}:$uuid:faceAdapter:dateItems".longHash(), gson.toJson(this)))
//            boxFor.put(KeyValueStringItem("${VisitorListFragmentV2::javaClass.name}:$uuid:faceAdapter:dateItems".longHash(), gson.toJson(this)))
        }
        keyValueDao.insertOrReplace(KeyValue("${VisitorListFragmentV2::javaClass.name}:$uuid:faceStrangerAdapter:dateItems".longHash(), gson.toJson(strangerItems)))
//        boxFor.put(KeyValueStringItem("${VisitorListFragmentV2::javaClass.name}:$uuid:faceStrangerAdapter:dateItems".longHash(), gson.toJson(strangerItems)))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_visitor_list, container, false)
    }

    fun canScrollVertically(direction: Int) = face_header.canScrollVertically(direction)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        faceAdapter = FaceAdapter(true)
        gridLayoutManager = GridLayoutManager(context, 3)
        face_header.layoutManager = gridLayoutManager
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {

            override fun getSpanSize(position: Int): Int {
                val item1 = (faceAdapter as FastItemAdapter<IItem<*, *>>).getItem(position)
                return if (item1 is LoadMoreItem) {
                    gridLayoutManager.spanCount
                } else {
                    1
                }
            }
        }
        faceAdapter.setNewList(visitorItems)
        (faceAdapter as FastItemAdapter<IItem<*, *>>).addAdapter(1, footerAdapter as ItemAdapter<IItem<*, *>>)
        face_header.adapter = faceAdapter
//        face_header.addOnScrollListener(object : EndlessRecyclerOnScrollListener(footerAdapter) {
//            override fun onLoadMore(currentPage: Int) {
//                AppLogger.w("loadMore")
//                onLoadMore()
//            }
//        })
        face_header.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()
                if (visibleItemPosition >= 0) {
                    if (dy > 0) { //check for scroll down
                        val visibleItemCount = gridLayoutManager.childCount
                        val totalItemCount = gridLayoutManager.itemCount
                        if (visibleItemCount + visibleItemPosition >= totalItemCount && isLoadingFinished && isExpanded) {
                            Log.d("tag", "tag.....load more")
                            isLoadingFinished = false
                            face_header.post {
                                if (isExpanded) {
                                    if (footerAdapter.adapterItemCount == 0)
                                        footerAdapter.add(moreItem)
                                }
                                onLoadMore()
                            }

                        }
                    }
                }
            }
        })
        faceAdapter.withOnClickListener { v, adapter, item, position ->
            when (item.getFaceType()) {
                FaceItem.FACE_TYPE_ALL -> {
                    currentPosition = 0
                    presenter.fetchVisitorList(0)
                    cam_message_indicator_watcher_text.visibility = View.VISIBLE
                    presenter.fetchVisitsCount("", FILTER_TYPE_ALL)
                    visitorListener?.onLoadItemInformation(item.getFaceType(), "")
                }
                FaceItem.FACE_TYPE_STRANGER -> {
                    currentPosition = 0
                    cam_message_indicator_watcher_text.visibility = View.GONE
                    faceAdapter.isNormalView = false
//                    makeContentView(false)
                    faceAdapter.set(strangerItems)
                    faceAdapter.select(0)
                    resizeContentHeight()
//                    strangerItems.getOrNull(currentPosition)?.strangerVisitor?.faceId?.apply {
                    visitorListener?.onLoadItemInformation(FaceItem.FACE_TYPE_STRANGER, strangerItems.getOrNull(0)?.strangerVisitor?.faceId ?: "")
//                    }
                    presenter.fetchStrangerVisitorList(0)

                }
                FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                    val faceId = if (item.getFaceType() == FaceItem.FACE_TYPE_ACQUAINTANCE) item.visitor?.personId else item.strangerVisitor?.faceId
                    AppLogger.w("主列表的 faceId?personId")
                    cam_message_indicator_watcher_text.visibility = View.VISIBLE

                    presenter.fetchVisitsCount(faceId!!, FILTER_TYPE_ACQUAINTANCE)
                    visitorListener?.onLoadItemInformation(item.getFaceType(), faceId)
                    setExpanded(false)
                }
                FaceItem.FACE_TYPE_STRANGER_SUB -> {
                    val faceId = if (item.getFaceType() == FaceItem.FACE_TYPE_STRANGER_SUB) item.strangerVisitor?.faceId else item.visitor?.personId
                    AppLogger.w("主列表的 faceId?personId")
                    cam_message_indicator_watcher_text.visibility = View.VISIBLE
                    presenter.fetchVisitsCount(faceId!!, FILTER_TYPE_STRANGER)
                    visitorListener?.onLoadItemInformation(item.getFaceType(), faceId)
                    setExpanded(false)
                }
            }
            return@withOnClickListener true
        }

        faceAdapter.withOnLongClickListener { v, adapter, item, position ->
            if (true) {//目前屏蔽掉长按事件
                return@withOnLongClickListener true
            }
            //            visitorListener?.onLoadItemInformation(item)
            if (item.getFaceType() != FaceItem.FACE_TYPE_ALL && item.getFaceType() != FaceItem.FACE_TYPE_STRANGER) {
                showHeaderFacePopMenu(item, position, v, item.getFaceType())
            }
            return@withOnLongClickListener true
        }
        faceAdapter.withSelectionListener { item, selected ->
            if (selected) {
                currentPosition = faceAdapter.getPosition(item)
            }
        }
        faceAdapter.withSelectable(true)
        faceAdapter.withMultiSelect(false)
//        adapter.withSelectOnLongClick(true)
        faceAdapter.withSelectWithItemUpdate(true)
        faceAdapter.withAllowDeselection(false)



        if (!isLoadCache && NetUtils.getNetType(context) == -1) {
            isLoadCache = true
            restoreCache()
        } else {
            refreshContent()
        }
    }

    private val moreRunnable = Runnable {
        if (faceAdapter.isNormalView) {
            visitorItems.lastOrNull()?.apply {
                if (getFaceType() == FaceItem.FACE_TYPE_ACQUAINTANCE) {
                    presenter.fetchVisitorList(visitor?.lastTime ?: 0)
                }
            }
        } else {
            strangerItems.lastOrNull()?.apply {
                presenter.fetchStrangerVisitorList(strangerVisitor?.lastTime ?: 0)
            }
        }
    }

    fun onLoadMore() {
        Log.d("VisitorListFragmentV2", "onLoadMore")
        face_header.removeCallbacks(moreRunnable)
        face_header.postDelayed(moreRunnable, 500)
    }

    @OnClick(R.id.more_text)
    fun clickedExpandArrow() {
        Log.d("VisitorFragment", "clickedExpandArrow")
        isExpanded = !isExpanded
        setExpanded(isExpanded)
    }

    private fun setExpanded(expanded: Boolean) {
        this.isExpanded = expanded
        visitorListener?.onExpanded(isExpanded)
        resizeContentHeight()
        if (isExpanded) {
            more_text.setText(R.string.FACE_COLLAPSE)
            ViewUtils.setDrawablePadding(more_text, R.drawable.icon_putaway, 2)

        } else {
            more_text.setText(R.string.Tap1_Menu_More)
            ViewUtils.setDrawablePadding(more_text, R.drawable.icon_expand, 2)
            footerAdapter.clear()
            faceAdapter.selections.firstOrNull()?.apply {
                gridLayoutManager.scrollToPositionWithOffset(this, 0)
            }
        }
    }

    private fun makeVisitorCount(count: Int, forAllVisitor: Boolean) {
        if (forAllVisitor) {
            val string = SpannableString(getString(R.string.MESSAGES_FACE_VISIT_SUM, count.toString()))
            val matcher = "\\d+".toPattern().matcher(string)
            if (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val span = ForegroundColorSpan(Color.parseColor("#4B9Fd5"))
                string.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                string.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            face_header.post { cam_message_indicator_watcher_text.text = string }
        } else {
            val string = SpannableString(getString(R.string.MESSAGES_FACE_VISIT_TIMES, count.toString()))
            val matcher = "\\d+".toPattern().matcher(string)
            if (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val span = ForegroundColorSpan(Color.parseColor("#4B9Fd5"))
                string.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            if (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()
                val span = ForegroundColorSpan(Color.parseColor("#4B9Fd5"))
                string.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                string.setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
            }
            cam_message_indicator_watcher_text.post { cam_message_indicator_watcher_text.text = string }
        }
    }

    private fun setFaceVisitsCounts(faceId: String, count: Int, type: Int) {
        AppLogger.w("获取来访数: id:$faceId,count:$count,type:$type")
        cam_message_indicator_watcher_text.visibility = View.VISIBLE
        visitorCountMap[faceId] = count
        when (type) {
            FILTER_TYPE_ALL -> {
                if (TextUtils.isEmpty(faceId)) {
                    makeVisitorCount(count, true)
                }
            }
            FILTER_TYPE_STRANGER -> {
                val faceId1 = strangerItems.getOrNull(currentPosition)?.strangerVisitor?.faceId
                AppLogger.w("actual face id:$faceId1")
                if (TextUtils.equals(faceId, faceId1)) {
                    makeVisitorCount(count, false)
                } else {
                    AppLogger.w("来访次数丢失了!!!!!!!")
                }
            }
            FILTER_TYPE_ACQUAINTANCE -> {
                val personId = visitorItems.getOrNull(currentPosition)?.visitor?.personId
                AppLogger.w("actual person id:$personId")
                if (/*adapter.isNormalVisitor&&*/ TextUtils.equals(faceId, personId)) {
                    makeVisitorCount(count, false)
                } else {
                    AppLogger.w("来访次数丢失了!!!!!!")
                }
            }
        }
    }

    override fun onVisitorListReady(visitorList: MutableList<FaceItem>, version: Long) {
        AppLogger.e("访客数据已经就绪")
        if (version == 0L) {
            visitorItems.clear()
            visitorItems.addAll(preloadItems)
            visitorItems.addAll(visitorList)
            faceAdapter.set(visitorItems)
            faceAdapter.select(currentPosition)
        } else {
            //append
            visitorItems.addAll(visitorList)
            if (!faceAdapter.isNormalView) {
                faceAdapter.set(visitorItems)
            } else {
                faceAdapter.add(visitorList)
            }
        }
        resizeContentHeight()
//        makeContentView(true)
        visitorListener?.onVisitorReady(visitorList)
        val id = visitorItems.getOrNull(currentPosition)?.visitor?.personId ?: ""
        presenter.fetchVisitsCount(id, FILTER_TYPE_ALL)
        if (!isExpanded) {
            val faceItem = visitorItems[currentPosition]
            visitorListener?.onLoadItemInformation(faceItem.getFaceType(), faceItem.visitor?.personId ?: "")
        }
        face_header.postDelayed({
            footerAdapter.clear()
            isLoadingFinished = true
        }, 500)
    }

    private fun decideShowFooter() {
        footerAdapter.clear()
        if (isExpanded) {
            footerAdapter.add(moreItem)
        }
        isLoadingFinished = true
    }

    open fun exitStranger() {
        faceAdapter.isNormalView = true
        currentPosition = 0
        faceAdapter.set(visitorItems)
        faceAdapter.select(currentPosition)
        gridLayoutManager.scrollToPosition(0)
        resizeContentHeight()
//        makeContentView(true)
        makeVisitorCount(0, true)
        presenter.fetchVisitsCount("", FILTER_TYPE_ALL)
        if (!isExpanded) {
            val faceItem = visitorItems[currentPosition]
            visitorListener?.onLoadItemInformation(faceItem.getFaceType(), faceItem.visitor?.personId ?: "")
        }
    }

    override fun onStrangerVisitorListReady(visitorList: MutableList<FaceItem>, version: Long) {
        AppLogger.e("陌生人列表已就绪")
        if (version == 0L) {
            strangerItems.clear()
            strangerItems.addAll(visitorList)
            faceAdapter.set(strangerItems)
            faceAdapter.select(currentPosition)
        } else {
            //append
            strangerItems.addAll(visitorList)
            if (faceAdapter.isNormalView) {
                faceAdapter.set(strangerItems)
            } else {
                faceAdapter.add(visitorList)
            }
        }
        resizeContentHeight()
//        strangerItems.addAll(visitorList)
//        makeContentView(false)
        strangerItems.getOrNull(currentPosition)?.apply {
            presenter.fetchVisitsCount(strangerVisitor?.faceId!!, FILTER_TYPE_STRANGER)
            if (!isExpanded) {
                val faceItem = strangerItems[currentPosition]
                visitorListener?.onLoadItemInformation(faceItem.getFaceType(), faceItem.strangerVisitor?.faceId ?: "")
            }
        }
        visitorListener?.onStrangerVisitorReady(visitorList)
        isLoadingFinished = true
        face_header.postDelayed({
            footerAdapter.clear()
            isLoadingFinished = true
        }, 500)
    }

    open fun refreshContent() {
        if (faceAdapter.isNormalView) {
            face_header.post {
                currentPosition = 0
                presenter?.fetchVisitorList(0)
            }
        } else {
            face_header.post {
                currentPosition = 0
                presenter?.fetchStrangerVisitorList(0)
            }
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
                contentView.findViewById<View>(R.id.detect).visibility = View.GONE
            }
            FaceItem.FACE_TYPE_STRANGER, FaceItem.FACE_TYPE_STRANGER_SUB -> {
                contentView.findViewById<View>(R.id.viewer).visibility = View.GONE
            }
        }
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWindow = PopupWindow(contentView, contentView.measuredWidth, contentView.measuredHeight)
        popupWindow.setBackgroundDrawable(ColorDrawable(0))
        popupWindow.isOutsideTouchable = true
        contentView.findViewById<View>(R.id.delete).setOnClickListener { v ->
            // TODO: 2017/10/9 删除操作
            AppLogger.w("将删除面孔")
            popupWindow.dismiss()
            showDeleteFaceAlert(item)
        }

        contentView.findViewById<View>(R.id.detect).setOnClickListener { v ->
            // TODO: 2017/10/9 识别操作
            AppLogger.w("将识别面孔")
            popupWindow.dismiss()
            showDetectFaceAlert(item.strangerVisitor)
        }

        contentView.findViewById<View>(R.id.viewer).setOnClickListener { _ ->
            AppLogger.w("将查看面孔详细信息")
            popupWindow.dismiss()

            if (item != null) {
                val fragment = FaceInformationFragment.newInstance(uuid, item.visitor)
                ActivityUtils.addFragmentSlideInFromRight(activity?.supportFragmentManager, fragment, android.R.id.content)
            } else {
                // TODO: 2017/10/16 为什么会出现这种情况?
            }
        }
//        popupWindow.showAsDropDown(faceItem.findViewById(R.id.img_item_face_selection))
        val anchor = faceItem.findViewById<View>(R.id.img_item_face_selection)
//        showAsDropDown(popupWindow, anchor, 0, 0)
//        var position = IntArray(2)
//        anchor.getLocationOnScreen(position)
//        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, position[0], position[1] + anchor.measuredHeight)
        PopupWindowCompat.showAsDropDown(popupWindow, anchor, 0, 0, Gravity.NO_GRAVITY)
    }

    private fun showDetectFaceAlert(strangerVisitor: DpMsgDefine.StrangerVisitor?) {
        val dialog = AlertDialog.Builder(context!!)
                .setView(R.layout.layout_face_detect_pop_alert)
                .show()

        dialog.findViewById<View>(R.id.detect_cancel)!!.setOnClickListener { v -> dialog.dismiss() }

        dialog.findViewById<View>(R.id.detect_ok)!!.setOnClickListener { v ->
            val addTo = dialog.findViewById<RadioButton>(R.id.detect_add_to) as RadioButton?
            val newFace = dialog.findViewById<RadioButton>(R.id.detect_new_face) as RadioButton?
            if (addTo!!.isChecked) {
                val fragment = FaceListFragment.newInstance(DataSourceManager.getInstance().account.account,
                        uuid, strangerVisitor?.faceId ?: "", FaceListFragment.TYPE_ADD_TO)
                fragment.resultCallback = { o, o2, o3 ->
                    presenter.fetchStrangerVisitorList(0)

                }// TODO: 2017/10/10 移动到面孔的结果回调
                ActivityUtils.addFragmentSlideInFromRight(activity?.supportFragmentManager, fragment, android.R.id.content)
            } else if (newFace!!.isChecked) {
                val fragment = CreateNewFaceFragment.newInstance(uuid, strangerVisitor)
                fragment.resultCallback = {
                    //todo 返回创建的personID
                    presenter.fetchStrangerVisitorList(0)
                }
                ActivityUtils.addFragmentSlideInFromRight(activity?.supportFragmentManager, fragment, android.R.id.content)
            }
            dialog.dismiss()
        }
    }

    private fun showDeleteFaceAlert(item: FaceItem) {
        val dialog = AlertDialog.Builder(context!!)
                .setView(R.layout.layout_face_delete_pop_alert)
                .show()
        dialog.findViewById<View>(R.id.delete_cancel)!!.setOnClickListener { v1 ->
            // TODO: 2017/10/9 取消了 什么也不做
            dialog.dismiss()

        }

        dialog.findViewById<View>(R.id.delete_ok)!!.setOnClickListener { v ->
            val radioGroup = dialog.findViewById<RadioGroup>(R.id.delete_radio)
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
            }
            dialog.dismiss()
        }

    }

    interface VisitorListener {
        fun onLoadItemInformation(faceType: Int, personOrFaceId: String)
        fun onStrangerVisitorReady(visitorList: MutableList<FaceItem>)
        fun onVisitorReady(visitorList: MutableList<FaceItem>)
        fun onExpanded(expanded: Boolean)
    }

}

