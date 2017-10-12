package com.cylan.jiafeigou.n.view.cam.item

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.support.photoselect.CircleImageView
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Created by yanzhendong on 2017/10/12.
 */
class FaceListItem : AbstractItem<FaceListItem, FaceListItem.FaceListViewHolder>() {
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

    }


    class FaceListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var header: TextView = view.findViewById(R.id.header) as TextView
        var icon: CircleImageView = view.findViewById(R.id.icon) as CircleImageView
        var name: TextView = view.findViewById(R.id.name) as TextView
        var radio: RadioButton = view.findViewById(R.id.radio) as RadioButton
    }
}