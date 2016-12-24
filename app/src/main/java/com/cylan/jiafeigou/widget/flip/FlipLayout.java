package com.cylan.jiafeigou.widget.flip;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-12-23.
 */

public class FlipLayout extends LinearLayout implements ISafeStateSetter,
        FlipImageView.OnFlipListener {

    private FlipImageView flipImageView;
    private TextView textView;
    private IClicker clicker;

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
        initFlipLayout();
    }

    /**
     * 安全防护
     */
    private void initFlipLayout() {
        //偷懒,这些都应该封装在FlipLayout内部的.
        //设置切换动画
        flipImageView.setRotationXEnabled(true);
        flipImageView.setDuration(200);
        flipImageView.setInterpolator(new DecelerateInterpolator());
        flipImageView.setOnFlipListener(this);
        //大区域
        setOnClickListener((View v) -> flipImageView.performClick());
    }

    @Override
    public void setState(boolean state) {
        textView.setText(state ? getContext().getString(R.string.SECURE) : "");
        if (!flipImageView.isFlipped() && state) {
            flipImageView.setFlipped(true);
        }
    }

    @Override
    public void setVisibility(boolean show) {
        setVisibility(show ? VISIBLE : INVISIBLE);
    }

    public void setClicker(IClicker clicker) {
        this.clicker = clicker;
    }

    @Override
    public void onClick(FlipImageView view) {
        if (clicker != null) {
            clicker.click(view);
        }
        Log.d("onClick", "onClick");
    }

    @Override
    public void onFlipStart(FlipImageView view) {

    }

    @Override
    public void onFlipEnd(FlipImageView view) {

    }
}