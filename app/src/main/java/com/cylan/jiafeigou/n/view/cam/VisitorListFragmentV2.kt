package com.cylan.jiafeigou.n.view.cam


import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.PopupWindowCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
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
import com.cylan.jiafeigou.utils.NetUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
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
        setFaceVisitsCounts(faceId, cnt, type)
    }


    var visitorListener: VisitorListener? = null

    private lateinit var faceAdapter: FaceAdapter
    private val allFace = FaceItem().withSetSelected(true).withFaceType(FaceItem.FACE_TYPE_ALL)
    private val strangerFace = FaceItem().withFaceType(FaceItem.FACE_TYPE_STRANGER)
    private val preloadItems = listOf(allFace, strangerFace)
    private val visitorItems = mutableListOf<FaceItem>().apply {
        addAll(preloadItems)
    }

    private val strangerItems = mutableListOf<FaceItem>()
    private var isLoadCache = false
    //    private var currentItem: FaceItem? = null
    @Volatile
    private var currentPosition: Int = 0

    private var isExpanded = false

    private var allVisitorCount: Int = 0

    private class FaceAdapter(var isNormalView: Boolean) : FastItemAdapter<FaceItem>()

    private lateinit var gridLayoutManager: GridLayoutManager
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
            strangerItems.addAll(item1)
        }
    }

    override fun onPause() {
        super.onPause()
        saveCache()
    }

    private fun makeContentView(items: MutableList<FaceItem>, isNormalView: Boolean) {
        if (isNormalView) {
            visitorItems.addAll(items)
            faceAdapter.isNormalView = true
            faceAdapter.setNewList(visitorItems)
            faceAdapter.select(0)

//            cam_message_indicator_watcher_text.visibility = View.VISIBLE
            currentPosition = 0
            presenter.fetchVisitsCount("", FILTER_TYPE_ALL)
            visitorListener?.onVisitorReady(items)
            visitorListener?.onLoadItemInformation(visitorItems[currentPosition])
        } else {
            faceAdapter.isNormalView = false
            faceAdapter.set(strangerItems)
            faceAdapter.select(0)
            visitorListener?.onStrangerVisitorReady(items)

//            cam_message_indicator_watcher_text.visibility = View.VISIBLE
            currentPosition = 0
            strangerItems.getOrNull(0)?.apply {
                presenter.fetchVisitsCount(strangerVisitor?.faceId!!, FILTER_TYPE_STRANGER)
                visitorListener?.onLoadItemInformation(this)
            }
        }
    }


    private fun saveCache() {
        val boxFor = BaseApplication.getBoxStore().boxFor(KeyValueStringItem::class)
        val gson = Gson()
        visitorItems.drop(2).apply {
            boxFor.put(KeyValueStringItem("${VisitorListFragmentV2::javaClass.name}:$uuid:faceAdapter:dateItems".longHash(), gson.toJson(this)))
        }
        boxFor.put(KeyValueStringItem("${VisitorListFragmentV2::javaClass.name}:$uuid:faceStrangerAdapter:dateItems".longHash(), gson.toJson(strangerItems)))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_visitor_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
        faceAdapter = FaceAdapter(true)
        refresh_layout.isEnabled = false
        refresh_layout.setColorSchemeColors(resources.getColor(R.color.color_36BDFF))
        faceAdapter.setNewList(visitorItems)
        gridLayoutManager = GridLayoutManager(context, 3)
        face_header.layoutManager = gridLayoutManager
        face_header.adapter = faceAdapter
        faceAdapter.withOnClickListener { v, adapter, item, position ->
            visitorListener?.onLoadItemInformation(item)
            when (item.getFaceType()) {
                FaceItem.FACE_TYPE_ALL -> {
                    presenter.fetchVisitorList()
                    cam_message_indicator_watcher_text.visibility = View.VISIBLE
                    presenter.fetchVisitsCount("", FILTER_TYPE_ALL)
                    makeVisitorCount(allVisitorCount, true)
                }
                FaceItem.FACE_TYPE_STRANGER -> {
                    cam_message_indicator_watcher_text.visibility = View.GONE
                    faceAdapter.setNewList(strangerItems)
                    presenter.fetchStrangerVisitorList()

                }
                FaceItem.FACE_TYPE_ACQUAINTANCE -> {
                    val faceId = if (item.getFaceType() == FaceItem.FACE_TYPE_ACQUAINTANCE) item.visitor?.personId else item.strangerVisitor?.faceId
                    AppLogger.w("主列表的 faceId?personId")
                    cam_message_indicator_watcher_text.visibility = View.VISIBLE
                    presenter.fetchVisitsCount(faceId!!, FILTER_TYPE_ACQUAINTANCE)
                }
                FaceItem.FACE_TYPE_STRANGER_SUB -> {
                    val faceId = if (item.getFaceType() == FaceItem.FACE_TYPE_STRANGER_SUB) item.strangerVisitor?.faceId else item.visitor?.personId
                    AppLogger.w("主列表的 faceId?personId")
                    cam_message_indicator_watcher_text.visibility = View.VISIBLE
                    presenter.fetchVisitsCount(faceId!!, FILTER_TYPE_STRANGER)
                }
            }
            return@withOnClickListener true
        }

        faceAdapter.withOnLongClickListener { v, adapter, item, position ->

            //            visitorListener?.onLoadItemInformation(item)
            if (item.getFaceType() != FaceItem.FACE_TYPE_ALL && item.getFaceType() != FaceItem.FACE_TYPE_STRANGER) {
                showHeaderFacePopMenu(item, position, v, item.getFaceType())
            }
            return@withOnLongClickListener true
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

    @OnClick(R.id.expand_arrow)
    fun clickedExpandArrow() {
        Log.d("VisitorFragment", "clickedExpandArrow")
        isExpanded = !isExpanded
        refresh_layout.isRefreshing = false
        refresh_layout.isEnabled = isExpanded
        visitorListener?.onExpanded(isExpanded)
        expand_arrow.setImageResource(if (isExpanded) R.drawable.btn_unfolded else R.drawable.btn_put_away)
        if (!isExpanded) {
            faceAdapter.selections.firstOrNull()?.apply {
                gridLayoutManager.scrollToPosition(this)
            }
        }
    }

    private fun makeVisitorCount(count: Int, forAllVisitor: Boolean) {
        if (forAllVisitor) {
            allVisitorCount = count
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

    override fun onVisitorListReady(visitorList: MutableList<FaceItem>) {
        AppLogger.e("访客数据已经就绪")
        visitorItems.clear()
        visitorItems.addAll(preloadItems)
        makeContentView(visitorList, true)
    }

    open fun exitStranger() {
        makeContentView(visitorItems, true)
        faceAdapter.isNormalView = true
        faceAdapter.setNewList(visitorItems)
        faceAdapter.select(0)
        presenter.fetchVisitorList()
        makeVisitorCount(allVisitorCount, true)
    }

    override fun onStrangerVisitorListReady(visitorList: MutableList<FaceItem>) {
        AppLogger.e("陌生人列表已就绪")
        strangerItems.clear()
        makeContentView(visitorList, false)
    }

    open fun refreshContent() {
        if (faceAdapter.isNormalView) {
            face_header.post { presenter?.fetchVisitorList() }
        } else {
            face_header.post { presenter?.fetchStrangerVisitorList() }
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
        var position = IntArray(2)
        anchor.getLocationOnScreen(position)
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, position[0], position[1] + anchor.measuredHeight)
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
                    presenter.fetchStrangerVisitorList()

                }// TODO: 2017/10/10 移动到面孔的结果回调
                ActivityUtils.addFragmentSlideInFromRight(activity?.supportFragmentManager, fragment, android.R.id.content)
            } else if (newFace!!.isChecked) {
                val fragment = CreateNewFaceFragment.newInstance(uuid, strangerVisitor)
                fragment.resultCallback = {
                    //todo 返回创建的personID
                    presenter.fetchStrangerVisitorList()
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
        fun onLoadItemInformation(item: FaceItem)
        fun onStrangerVisitorReady(visitorList: MutableList<FaceItem>)
        fun onVisitorReady(visitorList: MutableList<FaceItem>)
        fun onExpanded(expanded: Boolean)
    }

}

