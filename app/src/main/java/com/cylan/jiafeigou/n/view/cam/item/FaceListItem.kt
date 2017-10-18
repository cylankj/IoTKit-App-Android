package com.cylan.jiafeigou.n.view.cam.item

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.bumptech.glide.Glide
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.support.photoselect.CircleImageView
import com.github.promeg.pinyinhelper.Pinyin
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Created by yanzhendong on 2017/10/12.
 */
class FaceListItem : AbstractItem<FaceListItem, FaceListItem.FaceListViewHolder>() {
    var faceInformation: DpMsgDefine.FaceInformation? = null
    fun withFaceInformation(faceInformation: DpMsgDefine.FaceInformation): FaceListItem {
        this.faceInformation = faceInformation
        return this
    }

    override fun getViewHolder(v: View): FaceListViewHolder {
        return FaceListViewHolder(v)
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_face_list_item
    }

    @SuppressLint("ResourceType")
    override fun getType(): Int {
        return R.layout.layout_face_list_item
    }

    override fun bindView(holder: FaceListViewHolder, payloads: MutableList<Any>?) {
        super.bindView(holder, payloads)
        holder.radio.isChecked = isSelected

        Glide
                .with(holder.itemView.context)
                .load(faceInformation?.image_url)
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .into(holder.icon)

        holder.name.text = faceInformation?.face_name ?: "小明"
        var adapter: FastAdapter<FaceListItem> = holder.itemView.getTag(R.id.fastadapter_item_adapter) as FastAdapter<FaceListItem>
        val position = holder.adapterPosition
        val pinyin = Pinyin.toPinyin(faceInformation?.face_name?.get(0) ?: ' ')
        when (position) {
            0 -> {

            }

        }
        if (position > 0) {
            val item = adapter.getItem(position - 1)
            val name1 = item.faceInformation?.face_name
            val name2 = faceInformation?.face_name

            val pinyin = Pinyin.toPinyin(name1?.getOrElse(0) { '\u0000' }!!)
            val pinyin1 = Pinyin.toPinyin(name2?.getOrElse(0) { '\u0000' }!!)

            if (!TextUtils.equals(pinyin, pinyin1)) {
                holder.header.text = pinyin1
            } else {
                holder.header.text = null
            }
        }
    }


    class FaceListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var header: TextView = view.findViewById(R.id.header) as TextView
        var icon: CircleImageView = view.findViewById(R.id.icon) as CircleImageView
        var name: TextView = view.findViewById(R.id.name) as TextView
        var radio: RadioButton = view.findViewById(R.id.radio) as RadioButton
    }
}