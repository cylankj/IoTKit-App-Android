package com.cylan.jiafeigou.n.view.cam.item

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import com.cylan.jiafeigou.R
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Created by yanzhendong on 2017/10/18.
 */
class FaceListHeaderItem : AbstractItem<FaceListHeaderItem, FaceListHeaderItem.FaceListHeaderViewHolder>() {
    override fun getViewHolder(v: View): FaceListHeaderViewHolder {
        return FaceListHeaderViewHolder(v)
    }

    @SuppressLint("ResourceType")
    override fun getType(): Int {
        return R.layout.layout_face_list_header
    }

    override fun getLayoutRes(): Int {
        return R.layout.layout_face_list_header
    }

    override fun bindView(holder: FaceListHeaderViewHolder?, payloads: MutableList<Any>?) {
        super.bindView(holder, payloads)
    }


    class FaceListHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }
}