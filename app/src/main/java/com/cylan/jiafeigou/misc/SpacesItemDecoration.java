package com.cylan.jiafeigou.misc;

/**
 * Created by cylan-hunt on 16-8-6.
 */

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by hunt on 15-12-8.
 */
public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private Rect space;

    public SpacesItemDecoration(Rect space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        outRect.left = space.left;
        outRect.right = space.right;
        outRect.bottom = space.bottom;
        outRect.top = space.top;
    }
}
