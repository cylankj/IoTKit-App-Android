package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by hds on 17-6-28.
 */

public class TmpHint extends LinearLayout {

    TextView tv;

    public TmpHint(Context context) {
        this(context, null);
    }

    public TmpHint(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TmpHint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.tmp_hint_layout, this);
        tv = (TextView) view.findViewById(R.id.tv_content);
    }

    public void showHint(boolean show) {
        findViewById(R.id.v_hint)
                .setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void setText(String resId) {
        tv.setText(resId);
    }

    public void setTextSize(int unit, float size) {
        tv.setTextSize(unit, size);
    }

    public void setTypeface(Typeface tf, int style) {
        tv.setTypeface(tf, style);
    }
}
