//package com.cylan.jiafeigou.n.view.cam.item
//
//import android.support.v7.widget.RecyclerView
//import android.view.View
//import com.cylan.jiafeigou.R
//import com.cylan.jiafeigou.n.mvp.model.CamMessageBean
//import com.cylan.jiafeigou.utils.MiscUtils
//import com.mikepenz.fastadapter.items.AbstractItem
//
///**
// * Created by yanzhendong on 2017/9/29.
// */
//class FaceCameraMessageItem : AbstractItem<FaceCameraMessageItem, FaceCameraMessageItem.FaceCameraViewHolder>() {
//
//    private lateinit var messageItem: CamMessageBean
//
//    private lateinit var uuid: String
//
////    private val TAG = "CamMessageListAdapter"
////    /**
////     * 图片，只有文字,加载。
////     */
////    private val MAX_TYPE = 5
////    /**
////     * 0： 正常，1:编辑
////     */
////    private var editMode: Boolean = false
////    //图片Container的总体宽度,可能有3条,可能有2条.
////    private var pic_container_width: Int//宽度是固定的，需要调整高度。
////    private val selectedMap = HashMap<Int, Int>()
////
////    private val isSharedDevice = false
////    private var summary: DpMsgDefine.DPSdcardSummary? = null
////    private var status: Boolean = false
////
////
////    fun CamMessageListAdapter(uiid: String, context: Context, items: List<CamMessageBean>, mulItemViewType: IMulItemViewType<CamMessageBean>): ??? {
////        super(context, items, mulItemViewType)
////        //这个40是根据layout中的marginStart,marginEnd距离提取出来,如果需要修改,参考这个layout
////        pic_container_width = (Resources.getSystem().displayMetrics.widthPixels - getContext().getResources().getDimension(R.dimen.x34)).toInt()
////        this.uuid = uiid
////        val device = DataSourceManager.getInstance().getDevice(uuid)
////        this.isSharedDevice = device != null && device.available() && !TextUtils.isEmpty(device.shareAccount)
////    }
////
////    /*
////     * 是否有卡,不检查卡的读写失败
////     *
////     * @return
////     */
////    private fun hasSdcard(): Boolean {
////        val status = BaseApplication.getAppComponent().sourceManager.getDevice(uuid).`$`(204, DpMsgDefine.DPSdStatus())
////
////        return this.status = status!!.hasSdcard && status.err == 0
////    }
////
////    private fun online(): Boolean {
////        val device = BaseApplication.getAppComponent().sourceManager.getDevice(uuid)
////        val net = device.`$`(201, DpMsgDefine.DPNet())
////        return net != null && net.net > 0
////    }
////
////    fun isEditMode(): Boolean {
////        return editMode
////    }
////
////    /**
////     * 翻转
////     *
////     * @param lastVisiblePosition
////     */
////    fun reverseMode(reverse: Boolean, lastVisiblePosition: Int) {
////        this.editMode = reverse
////        if (!editMode) selectedMap.clear()
////        updateItemFrom(lastVisiblePosition)
////    }
////
////    /**
////     * 全选或者反选
////     *
////     * @param mark
////     */
////    fun markAllAsSelected(mark: Boolean, lastPosition: Int) {
////        if (mark) {
////            for (i in 0..getCount() - 1) {
////                selectedMap.put(i, i)
////            }
////        } else {
////            selectedMap.clear()
////        }
////        notifyItemRangeChanged(0, getCount())
////    }
////
////    /**
////     * 收集已经选中的
////     *
////     * @return
////     */
////    fun getSelectedItems(): ArrayList<CamMessageBean> {
////        val list = ArrayList<CamMessageBean>()
////        for (index in selectedMap.keys) {
////            list.add(getItem(index))
////        }
////        return list
////    }
////
////    /**
////     * 更新部分item
////     *
////     * @param position
////     */
////    private fun updateItemFrom(position: Int) {
////        synchronized(com.cylan.jiafeigou.n.view.adapter.CamMessageListAdapter::class.java) {
////            for (i in 0..getCount() - 1) {
////                if (i <= position)
////                //没必要全部
////                    notifyItemChanged(i)
////            }
////        }
////    }
////
////    fun markItemSelected(position: Int): Boolean {
////        if (!editMode)
////            return false
////        if (selectedMap.containsKey(position)) {
////            selectedMap.remove(position)
////            notifyItemChanged(position)
////            return false
////        } else {
////            selectedMap.put(position, position)
////            notifyItemChanged(position)
////            return true
////        }
////    }
////
////
////    /**
////     * 显示直播按钮
////     *
////     * @param time
////     * @return
////     */
////
////    private fun showLiveBtn(time: Long): Boolean {
////        return System.currentTimeMillis() - time >= 30 * 60 * 1000L && hasSdcard() && !isSharedDevice
////    }
////
////    private fun showHistoryButton(bean: CamMessageBean?): Boolean {
////        if (isSharedDevice || !hasSdcard()) return false
////        if (bean != null && bean.bellCallRecord != null) {
////            return bean.bellCallRecord.isRecording == 1
////        } else if (bean != null && bean.alarmMsg != null) {
////            val device = DataSourceManager.getInstance().getDevice(uuid)
////            val pan720 = JFGRules.isPan720(device.pid)
////            return if (pan720) {
////                //                return bean.alarmMsg.isRecording == 1;//全部当成图片处理,
////                // TODO: 2017/8/4 当前查看视频不知道怎么处理
////                false
////            } else {
////                bean.alarmMsg.isRecording == 1 && System.currentTimeMillis() - bean.alarmMsg.version >= 30 * 60 * 1000L
////            }
////        }
////        return false
////    }
////
////    private fun textShowSdBtn(item: CamMessageBean): Boolean {
////        //考虑这个bean的条件.
////        // TODO: 2017/8/16  不光要看 hasSDCard 还要看 err 是否为0 #118051
////        // TODO: 2017/8/16 Android（1.1.0.534）720设备 报警中心界面 提示"检车到新的Micro SD卡，需要先初始化才能存储视频" 右下角没有查看详情 按钮
////        val device = BaseApplication.getAppComponent().sourceManager.getDevice(uuid)
////        val status = device.`$`(204, DpMsgDefine.DPSdStatus())
////        var hasSdcard = false
////        var err = -1
////        if (item.sdcardSummary != null) {
////            hasSdcard = item.sdcardSummary.hasSdcard
////            err = item.sdcardSummary.errCode
////        } else if (status != null) {
////            hasSdcard = status.hasSdcard
////            err = status.err
////        }
////
////        return hasSdcard && err != 0 && (!isSharedDevice || JFGRules.isPan720(device.pid))
////    }
////
////    /**
////     * 来自一个全局的通知消息
////     */
////    fun notifySdcardStatus(status: Boolean, position: Int) {
////        if (this.status != status) {//不一样才 notify
////            this.status = status
////            updateItemFrom(position)
////        }
////    }
////
////    fun notifyDeviceOnlineState(online: Boolean, position: Int) {
////        updateItemFrom(position)
////    }
////
////    /**
////     * sd卡状态消息
////     *
////     * @param holder
////     * @param item
////     */
////    private fun handleTextContentLayout(holder: SuperViewHolder,
////                                        item: CamMessageBean) {
////        holder.setText(R.id.tv_cam_message_item_date, getFinalTimeContentSD(item))
////        holder.setText(R.id.tv_cam_message_list_content, getFinalSdcardContent(item))
////        holder.setVisibility(R.id.tv_jump_next, if (textShowSdBtn(item)) View.VISIBLE else View.GONE)
////    }
////
////    private fun handlePicsLayout(holder: SuperViewHolder,
////                                 item: CamMessageBean) {
////        var count = 0
////        if (item.alarmMsg != null) {
////            count = if (item.alarmMsg.fileIndex < 1) 1 else MiscUtils.getCount(item.alarmMsg.fileIndex)
////        } else if (item.bellCallRecord != null && item.bellCallRecord.fileIndex != -1) {
////            count = if (item.bellCallRecord.fileIndex < 1) 1 else MiscUtils.getCount(item.bellCallRecord.fileIndex)
////        }
////        count = Math.max(count, 1)//最小为1
////        for (index in 1..count) {
////            val id = if (index == 1)
////                R.id.imgV_cam_message_pic0
////            else if (index == 2)
////                R.id.imgV_cam_message_pic1
////            else
////                R.id.imgV_cam_message_pic2
////            Glide.with(ContextUtils.getContext())
////                    .load(MiscUtils.getCamWarnUrl(uuid, item, index))
////                    .placeholder(R.drawable.wonderful_pic_place_holder)
////                    .diskCacheStrategy(DiskCacheStrategy.ALL)
////                    .centerCrop()
////                    .listener(loadListener)
////                    .into(holder.getView<View>(id) as ImageView)
////            holder.setOnClickListener(id, onClickListener)
////        }
////        holder.setText(R.id.tv_cam_message_item_date, getFinalTimeContent(item))
////        Log.d(TAG, "handlePicsLayout: " + (System.currentTimeMillis() - item.version))
////        holder.setVisibility(R.id.tv_jump_next, if (showHistoryButton(item)) View.VISIBLE else View.GONE)
////    }
////
////
////    private var onClickListener: View.OnClickListener? = null
////
////    fun setOnclickListener(onclickListener: View.OnClickListener) {
////        this.onClickListener = onclickListener
////    }
////
////    /**
////     * 时间
////     *
////     * @param bean
////     * @return
////     */
////    private fun getFinalTimeContent(bean: CamMessageBean): String {
////        val id = bean.id
////        val tContent = TimeUtils.getHH_MM(bean.version) + " "
////        if (id == DpMsgMap.ID_505_CAMERA_ALARM_MSG.toLong()) {
////            /*现在人形检测也是用的这个消息,增加了扩展字段,有人形的提示和无人形的提示有区别
////            * 1.有人形提示:检测到 XXX
////            * 2.无人形提示:有新的发现
////            * */
////            return if (bean.alarmMsg.objects != null && bean.alarmMsg.objects.size > 0) {//有检测数据
////                tContent + ContextUtils.getContext().getString(R.string.DETECTED_AI) + " " + JConstant.getAIText(bean.alarmMsg.objects)
////                //                return tContent + "检测到" + JConstant.getAIText(bean.alarmMsg.objects);
////            } else {//无检测数据
////                tContent + ContextUtils.getContext().getString(R.string.MSG_WARNING)
////            }
////
////        } else if (id == DpMsgMap.ID_401_BELL_CALL_STATE.toLong()) {
////            return tContent + if (bean.bellCallRecord.isOK == 1)ContextUtils.getContext().getString(R.string.DOOR_CALL) else ContextUtils.getContext().getString(R.string.DOOR_UNCALL)
////        }
////        return tContent
////    }
////
////
////    /**
////     * 时间
////     *
////     * @param bean
////     * @return
////     */
////    private fun getFinalTimeContentSD(bean: CamMessageBean): String {
////        val id = bean.id
////        val tContent = TimeUtils.getHH_MM(bean.version) + " "
////        return if (id == DpMsgMap.ID_505_CAMERA_ALARM_MSG.toLong() || id == DpMsgMap.ID_401_BELL_CALL_STATE.toLong()) {
////            tContent + ContextUtils.getContext().getString(R.string.MSG_WARNING)
////        } else tContent
////    }
////
////
////    /**
////     * sd卡内容
////     * 1489
////     *
////     * @param bean
////     * @return
////     */
////    private fun getFinalSdcardContent(bean: CamMessageBean): String {
////        if (bean.id != DpMsgMap.ID_222_SDCARD_SUMMARY.toLong() || bean.sdcardSummary == null)
////            return ""
////        val sdStatus = bean.sdcardSummary
////        if (!sdStatus.hasSdcard) {
////            return ContextUtils.getContext().getString(R.string.MSG_SD_OFF)
////        }
////        when (sdStatus.errCode) {
////            0 -> return ContextUtils.getContext().getString(R.string.MSG_SD_ON)
////            else -> return ContextUtils.getContext().getString(R.string.MSG_SD_ON_1)
////        }
////
////    }
////
////    fun hasFooter(): Boolean {
////        val count = getCount()
////        return count > 0 && getItem(count - 1)!!.viewType == CamMessageBean.ViewType.FOOT
////    }
////
////    private val loadListener = object : RequestListener<CamWarnGlideURL, GlideDrawable> {
////        override fun onException(e: Exception, model: CamWarnGlideURL, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
////            AppLogger.e(String.format(Locale.getDefault(), "uuid:%s,UriErr:%s,index:%s,e:%s", uuid, model.time, model.index, MiscUtils.getErr(e)))
////            return false
////        }
////
////        override fun onResourceReady(resource: GlideDrawable, model: CamWarnGlideURL, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
////            return false
////        }
////    }
////
////    fun setCurrentSDcardSummary(summary: DpMsgDefine.DPSdcardSummary) {
////        this.summary = summary
////    }
//
//
//    override fun getViewHolder(v: View?): FaceCameraViewHolder {
//        return FaceCameraViewHolder(v!!)
//    }
//
//    override fun getType(): Int {
//        if (messageItem.bellCallRecord != null) {
//            val count = if (messageItem.bellCallRecord.fileIndex < 1) 1 else MiscUtils.getCount(messageItem.bellCallRecord.fileIndex)
//            if (count == 1) return CamMessageBean.ViewType.ONE_PIC
//            if (count == 2) return CamMessageBean.ViewType.TWO_PIC
//            if (count == 3) return CamMessageBean.ViewType.THREE_PIC
//        }
//        if (messageItem.alarmMsg != null) {
//            val count = if (messageItem.alarmMsg.fileIndex < 1) 1 else MiscUtils.getCount(messageItem.alarmMsg.fileIndex)
//            if (count == 1) return CamMessageBean.ViewType.ONE_PIC
//            if (count == 2) return CamMessageBean.ViewType.TWO_PIC
//            if (count == 3) return CamMessageBean.ViewType.THREE_PIC
//        }
//        return if (messageItem.sdcardSummary != null) {
//            CamMessageBean.ViewType.TEXT
//        } else messageItem.viewType
//    }
//
//    override fun getLayoutRes(): Int {
//        return when (type) {
//            CamMessageBean.ViewType.FOOT -> R.layout.layout_item_cam_load_more
//            CamMessageBean.ViewType.TEXT -> R.layout.layout_item_cam_msg_text
//            CamMessageBean.ViewType.ONE_PIC -> R.layout.layout_item_cam_msg_1pic
//            CamMessageBean.ViewType.TWO_PIC -> R.layout.layout_item_cam_msg_2pic
//            CamMessageBean.ViewType.THREE_PIC -> R.layout.layout_item_cam_msg_3pic
//            else -> R.layout.layout_item_cam_load_more
//        }
//    }
//
////    override fun bindView(holder: FaceCameraViewHolder?, payloads: MutableList<Any>?) {
////        super.bindView(holder, payloads)
////        when (viewType) {
////            CamMessageBean.ViewType.TEXT -> handleTextContentLayout(holder, item)
////            CamMessageBean.ViewType.ONE_PIC, CamMessageBean.ViewType.TWO_PIC, CamMessageBean.ViewType.THREE_PIC -> handlePicsLayout(holder, item)
////        }
////        if (viewType == CamMessageBean.ViewType.FOOT) return
////        if (onClickListener != null) {
////            holder.setOnClickListener(R.id.lLayout_cam_msg_container, onClickListener)
////            holder.setOnClickListener(R.id.tv_cam_message_item_delete, onClickListener)
////        }
////        //设置删除可见性,共享设备不可删除消息
////        val device = DataSourceManager.getInstance().getDevice(uuid)
////        holder.setVisibility(R.id.tv_cam_message_item_delete, if (!isSharedDevice || JFGRules.isPan720(device.pid)) View.VISIBLE else View.INVISIBLE)//720 设备享有所有权限
////        holder.setOnClickListener(R.id.tv_jump_next, onClickListener)
////        holder.setVisibility(R.id.fl_item_time_line, if (isEditMode()) View.INVISIBLE else View.VISIBLE)
////        holder.setVisibility(R.id.rbtn_item_check, if (isEditMode()) View.VISIBLE else View.INVISIBLE)
////        holder.setVisibility(R.id.fLayout_cam_message_item_bottom, if (!isEditMode()) View.VISIBLE else View.INVISIBLE)
////        holder.setChecked(R.id.rbtn_item_check, isEditMode() && selectedMap.containsKey(layoutPosition))
////    }
//
//    class FaceCameraViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//
//    }
//}