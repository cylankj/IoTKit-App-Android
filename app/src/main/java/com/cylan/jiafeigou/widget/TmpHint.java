package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;

/**
 * Created by hds on 17-6-28.
 */

public class TmpHint extends LinearLayout {
    public TmpHint(Context context) {
        this(context, null);
    }

    public TmpHint(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TmpHint(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.tmp_hint_layout, this);

    }

    public void showHint(boolean show) {
    }
}
