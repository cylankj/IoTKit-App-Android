package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;

/**
 * Created by yanzhendong on 2017/12/20.
 */

public class NoDragAppbarBehavior extends AppBarLayout.Behavior {

    public NoDragAppbarBehavior(Context context, AttributeSet attributeSet) {
        setDragCallback(new DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
    }
}
