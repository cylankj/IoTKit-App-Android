package com.cylan.jiafeigou.n.view.cam.item

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Created by yanzhendong on 2017/10/16.
 */
class FaceManagerItem : AbstractItem<FaceManagerItem, FaceManagerItem.FaceManagerViewHolder>() {

    var faceInformation: DpMsgDefine.FaceInformation? = null

    fun withFaceInformation(faceInformation: DpMsgDefine.FaceInformation): FaceManagerItem {
        this.faceInformation = faceInformation
        return this
    }

    override fun getViewHolder(v: View): FaceManagerViewHolder {
        return FaceManagerViewHolder(v)
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_face_manager_item
    }

    @SuppressLint("ResourceType")
    override fun getType(): Int {
        return R.layout.layout_face_manager_item
    }

    override fun bindView(holder: FaceManagerViewHolder, payloads: MutableList<Any>?) {
        super.bindView(holder, payloads)
        Glide.with(holder.itemView.context)
                .load(faceInformation?.source_image_url)
                .into(holder.faceIcon)
        holder.faceCheckBox.isChecked = isSelected

    }


    class FaceManagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.face)
        lateinit var faceIcon: ImageView
        @BindView(R.id.checkbox)
        lateinit var faceCheckBox: CheckBox

        init {
            ButterKnife.bind(this, itemView)
        }


    }
}