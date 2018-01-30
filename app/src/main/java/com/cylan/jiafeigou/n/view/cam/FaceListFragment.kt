package com.cylan.jiafeigou.n.view.cam

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.view.cam.item.FaceListHeaderItem
import com.cylan.jiafeigou.n.view.cam.item.FaceListItem
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.NetUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.github.promeg.pinyinhelper.Pinyin
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.fragment_facelist.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceListFragment : BaseFragment<FaceListContact.Presenter>(), FaceListContact.View {
    override fun onMoveFaceError() {
        ToastUtil.showToast(getString(R.string.SETTINGS_FAILED))
    }

    override fun onAuthorizationError() {
        ToastUtil.showToast(getString(R.string.SETTINGS_FAILED))
    }

    private var faceId: String? = null
    private var hasRequested: Boolean = false

    override fun onVisitorInformationReady(visitors: List<DpMsgDefine.Visitor>?) {
        visitors?.map {
            FaceListItem().withVisitorInformation(it)
        }?.apply {
                    adapter.setNewList(this)
                    when {
                        adapter.adapterItemCount == 0 -> {
                            empty_view.visibility = View.VISIBLE
                            headerAdapter.clear()
                        }
                        adapter.adapterItemCount > 0 -> {
                            empty_view.visibility = View.GONE
                            if (headerAdapter.adapterItemCount == 0) {
                                headerAdapter.add(FaceListHeaderItem())
                            }
                        }
                    }

                }
    }

    override fun onMoveFaceToPersonSuccess(personId: String) {
        AppLogger.w("移动面孔成功了")
        ToastUtil.showToast(getString(R.string.PWD_OK_2))
        fragmentManager?.popBackStack()

        if (targetFragment != null) {
            val intent = Intent()
            intent.putExtra("person_id", personId)
            targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
        }

        resultCallback?.invoke(personId, "todo:还不知道要传多少个参数", "todo:还不知道要传多少个参数")
    }

    override fun onFaceNotExistError() {
        ToastUtil.showToast(getString(R.string.SETTINGS_FAILED))
    }

    override fun onFaceInformationReady(data: List<DpMsgDefine.FaceInformation>) {
        AppLogger.w("onFaceInformationReady")
    }

    lateinit var adapter: FastItemAdapter<FaceListItem>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_facelist, container, false)
        return view
    }

    lateinit var layoutManager: LinearLayoutManager

    private lateinit var headerAdapter: ItemAdapter<FaceListHeaderItem>

    override fun initViewAndListener() {
        super.initViewAndListener()
        account = arguments?.getString("account")
        faceId = arguments?.getString("face_id")
        when (arguments?.getInt("type", TYPE_ADD_TO)) {
            TYPE_ADD_TO -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_IDENTIFY_ADD_BTN)
            }
            TYPE_MOVE_TO -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_FACE_MOVE)
            }
            else -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_IDENTIFY_ADD_BTN)
            }
        }

        adapter = FastItemAdapter()

        headerAdapter = ItemAdapter()
        headerAdapter.withUseIdDistributor(true)

        (adapter as FastItemAdapter<IItem<*, *>>).addAdapter(0, headerAdapter as ItemAdapter<IItem<*, *>>)

        adapter.withUseIdDistributor(true)
        adapter.withSelectable(true)
        adapter.withMultiSelect(false)
        adapter.withAllowDeselection(false)
        (adapter as FastAdapter<IItem<*, *>>).withSelectionListener { _, _ ->
            custom_toolbar.setRightEnable(adapter.selections.size > 0)
        }

        custom_toolbar.setRightEnable(false)
        adapter.itemAdapter.withComparator { item1, item2 ->
            val char1 = getPinYinLatter(item1.visitor?.personName)
            val char2 = getPinYinLatter(item2.visitor?.personName)
            val i = char1.compareTo(char2, true)

            return@withComparator when {
                i == 0 -> i
                char1 == "#" -> 1
                char2 == "#" -> -1
                else -> i
            }
        }

        adapter.withEventHook(object : ClickEventHook<FaceListItem>() {

            override fun onBindMany(viewHolder: RecyclerView.ViewHolder): MutableList<View>? {
                if (viewHolder is FaceListItem.FaceListViewHolder) {
                    return mutableListOf(viewHolder.itemView, viewHolder.radio)
                }
                return null
            }

            override fun onClick(v: View?, position: Int, fastAdapter: FastAdapter<FaceListItem>, item: FaceListItem) {
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



        layoutManager = LinearLayoutManager(context)
        face_list_items.adapter = adapter
        face_list_items.layoutManager = layoutManager

        custom_toolbar.setBackAction { fragmentManager?.popBackStack() }
        custom_toolbar.setRightAction { moveFaceTo() }

    }

    private fun getPinYinLatter(text: String?): String {
        val char = text?.getOrNull(0) ?: '#'
        val toPinyin = Pinyin.toPinyin(char)[0].toString()
        return if ("[a-z,A-Z]".toRegex().matches(toPinyin)) {
            toPinyin.toUpperCase()
        } else {
            "#"
        }
    }

    private fun moveFaceTo() {
        val selections = adapter.selectedItems
        if (selections != null && selections.size > 0) {
            val item = selections.elementAt(0)
            when {
                item.visitor?.personId == null -> {
                    AppLogger.w("PersonId is null")
                    ToastUtil.showToast(getString(R.string.SETTINGS_FAILED))
                }
                faceId == null -> {
                    AppLogger.w("faceId is null")
                    ToastUtil.showToast(getString(R.string.SETTINGS_FAILED))
                }
                NetUtils.getNetType(context) == -1 -> {
                    ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1))
                }
                else -> {
                    AppLogger.w("moveFaceToPerson with person id:${item.visitor?.personId},face id:$faceId")
                    presenter.moveFaceToPerson(item.visitor!!.personId!!, faceId!!)
                }
            }
        } else {
            AppLogger.w("Empty To Do")
        }
    }

    override fun onStart() {
        super.onStart()
//        presenter.loadPersonItems(account!!, uuid)
        if (!hasRequested) {
            hasRequested = true
            presenter.loadPersonItem2()
        }
    }

    var resultCallback: ((a: Any, b: Any, c: Any) -> Unit)? = null

    private var account: String? = null

    companion object {
        const val TYPE_ADD_TO = 1
        const val TYPE_MOVE_TO = 2
        fun newInstance(account: String, uuid: String, faceId: String, type: Int = TYPE_ADD_TO): FaceListFragment {
            val fragment = FaceListFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putInt("type", type)
            argument.putString("account", account)
            argument.putString("face_id", faceId)
            fragment.arguments = argument
            return fragment
        }
    }


}