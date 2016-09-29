package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-9-29.
 */

public class GuideIndicatorLayout extends LinearLayout {


    public GuideIndicatorLayout(Context context) {
        this(context, null);
    }

    public GuideIndicatorLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GuideIndicatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.guide_indicator_layout, this, true);
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public void setFocusedIndex(int index) {
        if (index < 0 || index > getChildCount())
            return;
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).setEnabled(true);
        }
        View v = getChildAt(index);
        Log.d("GuideIndicatorLayout", "GuideIndicatorLayout: " + v);
        if (v != null) {
            v.setEnabled(false);
        }
    }
}
