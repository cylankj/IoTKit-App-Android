package com.cylan.jiafeigou.widget.sticky;

import android.content.Context;
import android.view.View;

/**
 * Created by hunt on 16-6-1.
 */

public class StickyHeaderTarget extends StickyHeader {

    protected StickyHeaderTarget(Context context, View header, int minHeightHeader, HeaderAnimator headerAnimator) {
        super(context, header, minHeightHeader, headerAnimator);
    }

    @Override
    protected View getScrollingView() {
        return null;
    }

}