package com.cylan.jiafeigou.widget.wheel;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by yzd on 17-1-17.
 */

public class WonderIndicatorView extends ViewGroup {

    private SparseArray<TextView> mItems = new SparseArray<>(32);

    private TextView mTitle;

    public WonderIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initItems() {
        TextView textView;
        mTitle = new TextView(getContext());
        mTitle.setTextSize(dp2px(getContext(), 16));
        mTitle.setTextColor(Color.parseColor("#333333"));

        for (int i = 0; i < 31; i++) {
            textView = new TextView(getContext());
            textView.setText(String.valueOf(i));
            textView.setTextSize(dp2px(getContext(), 14));
            textView.setBackgroundResource(R.drawable.wonder_circle_bg);
            textView.setTextColor(getResources().getColor(R.color.color_wonder_indicator));
            mItems.put(i, textView);
        }
    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
