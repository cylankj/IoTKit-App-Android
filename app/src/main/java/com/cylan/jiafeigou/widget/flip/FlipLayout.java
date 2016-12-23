package com.cylan.jiafeigou.widget.flip;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-12-23.
 */

public class FlipLayout extends LinearLayout {

    private FlipImageView flipImageView;
    private TextView textView;

    public FlipLayout(Context context) {
        this(context, null);
    }

    public FlipLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlipLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.flip_layout, this);
        flipImageView = (FlipImageView) view.findViewById(R.id.flip_image);
        textView = (TextView) view.findViewById(R.id.tv_flip_content);
    }

    public FlipImageView getFlipImageView() {
        return flipImageView;
    }

    public TextView getTextView() {
        return textView;
    }
}
