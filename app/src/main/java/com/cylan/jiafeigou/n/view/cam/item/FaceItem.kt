package com.cylan.jiafeigou.n.view.cam.item

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.cylan.jiafeigou.R
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceItem : AbstractItem<FaceItem, FaceItem.FaceItemViewHolder>() {
    override fun getViewHolder(v: View): FaceItemViewHolder {
        return FaceItemViewHolder(v)
    }

    var text: String? = null
    @SuppressLint("ResourceType")
    override fun getType(): Int {
        return R.layout.item_face_list_item
    }


    override fun getLayoutRes(): Int {
        return R.layout.item_face_list_item
    }

    class FaceItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(0) as ImageView
        val text: TextView = view.findViewById(0) as TextView

    }
}