package com.cylan.jiafeigou.widget

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by yanzhendong on 2017/10/19.
 */
class GridItemDivider(var space: Int, var spanCount: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View?, parent: RecyclerView, state: RecyclerView.State?) {
        val position = parent.getChildLayoutPosition(view)
        outRect.right = space
        outRect.top = space
        //由于每行都只有3个，所以第一个都是3的倍数，把左边距设为0
        if (position % spanCount == 0) {
            outRect.left = space
        }
    }
}