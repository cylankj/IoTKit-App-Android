package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.util.AttributeSet;

/**
 * Created by yanzhendong on 2017/12/20.
 * 首页白块的原因是 AppbarLayout 内部有一个 drager,这个 drager 不会引起
 * 协调者布局的调度,一般来说,这个 drager 是要比协调者布局省性能的,但是可能会出现
 * 其他依赖 AppBarLayout 的布局更新不正常的情况,所以这里直接禁用了,以保证每次
 * AppbarLayout 的位置更新都会通知到其它依赖 AppbarLayout 的 View
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
