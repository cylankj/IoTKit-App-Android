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
    var visitor: DpMsgDefine.Visitor? = null
    fun withFaceInformation(faceInformation: DpMsgDefine.FaceInformation): FaceListItem {
        this.faceInformation = faceInformation
        return this
    }

    fun withVisitorInformation(visitor: DpMsgDefine.Visitor): FaceListItem {
        this.visitor = visitor
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

        //todo 基于 FaceInformation
        //imageUrl 怎么定义的一个 visitor 下有多个人脸
        var imageUrl: String = ""
        var personName: String = visitor?.personName ?: "小明啊"
        Glide
                .with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .into(holder.icon)

        holder.name.text = personName
        var adapter: FastAdapter<*> = holder.itemView.getTag(R.id.fastadapter_item_adapter) as FastAdapter<*>
        val adapter1 = adapter.getAdapter(holder.adapterPosition)
        val position = adapter1!!.getAdapterPosition(identifier)
        val pinyin = getPinYinLatter(personName)// Pinyin.toPinyin(faceInformation?.face_name?.get(0) ?: ' ')

        when {
            position == 0 -> {
                holder.header.text = pinyin
            }
            position > 0 -> {
                val item1 = adapter1.getAdapterItem(position - 1) as FaceListItem
                var lastPersonName = item1.visitor?.personName
                val pinyin1 = getPinYinLatter(lastPersonName)// Pinyin.toPinyin(item1.faceInformation?.face_name?.get(0) ?: ' ')
                if (!TextUtils.equals(pinyin1, pinyin)) {
                    holder.header.text = pinyin
                } else {
                    holder.header.text = null
                }
            }
        }
    }

    private fun getPinYinLatter(text: String?): String {
        val char = text?.get(0) ?: '#'
        return Pinyin.toPinyin(if (Pinyin.isChinese(char)) char else '#')?.get(0).toString()
    }


    class FaceListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var header: TextView = view.findViewById(R.id.header) as TextView
        var icon: CircleImageView = view.findViewById(R.id.icon) as CircleImageView
        var name: TextView = view.findViewById(R.id.name) as TextView
        var radio: RadioButton = view.findViewById(R.id.radio) as RadioButton
    }
}