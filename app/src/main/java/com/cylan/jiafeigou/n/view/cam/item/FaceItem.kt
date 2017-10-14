package com.cylan.jiafeigou.n.view.cam.item

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.bumptech.glide.Glide
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean
import com.cylan.jiafeigou.support.photoselect.CircleImageView
import com.cylan.jiafeigou.utils.MiscUtils
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceItem : AbstractItem<FaceItem, FaceItem.FaceItemViewHolder>() {
    var faceinformation: DpMsgDefine.FaceInformation? = null
    var message: CamMessageBean? = null
    var uuid: String? = null

    companion object {
        const val FACE_TYPE_ALL = -1
        const val FACE_TYPE_STRANGER = 0
        const val FACE_TYPE_ACQUAINTANCE = 1
    }

    override fun getViewHolder(v: View): FaceItemViewHolder {
        return FaceItemViewHolder(v)
    }

    var faceType: Int = 0 //熟人或者陌生人


    @SuppressLint("ResourceType")
    override fun getType(): Int {
        return R.layout.item_face_selection
    }

    fun withMessage(message: CamMessageBean): FaceItem {
        this.message = message
        return this
    }

    fun withUuid(uuid: String): FaceItem {
        this.uuid = uuid
        return this
    }


    override fun getLayoutRes(): Int {
        return R.layout.item_face_selection
    }

    override fun bindView(holder: FaceItemViewHolder, payloads: MutableList<Any>?) {
        super.bindView(holder, payloads)
        //todo 全部是默认图,陌生人是组合图片,需要特殊处理
        when (faceType) {
            FACE_TYPE_ALL -> {
                //todo UI图导入
                holder.text.text = holder.itemView.context.getText(R.string.MESSAGES_FILTER_ALL)
                holder.icon.setImageResource(R.drawable.news_icon_all_selector)
            }
            FACE_TYPE_STRANGER -> {
                //todo 多图片合成
                //http://img.taopic.com/uploads/allimg/120727/201995-120HG1030762.jpg
                Glide.with(holder.itemView.context)
                        .load(MiscUtils.getCamWarnUrl(uuid, message, 1))
                        .placeholder(R.drawable.news_icon_stranger)
                        .error(R.drawable.news_icon_stranger)
//                        .bitmapTransform(AvatarTransform(holder.itemView.context, message!!.alarmMsg!!.face_id))
                        .into(holder.icon)
                holder.text.text = holder.itemView.context.getText(R.string.MESSAGES_FILTER_STRANGER)
                holder.icon.isDisableCircularTransformation = true
                holder.icon.showHint(true)
            }
        //todo 可能会有猫狗车辆行人,这些都是预制的图片,需要判断
            else -> {
                holder.text.text = faceinformation?.face_name
                Glide.with(holder.itemView.context)
                        .load(faceinformation?.source_image_url)
                        .into(holder.icon)
            }
        }

    }


    class FaceItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: CircleImageView = view.findViewById(R.id.img_item_face_selection) as CircleImageView
        val text: TextView = view.findViewById(R.id.text_item_face_selection) as TextView

    }
}