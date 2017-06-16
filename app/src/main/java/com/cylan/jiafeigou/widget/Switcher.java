package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

/**
 * Created by hds on 17-6-15.
 */

public class Switcher extends LinearLayout {

    private TextView viewFirst;
    private TextView viewSecond;
    private TextView viewThird;

    public Switcher(Context context) {
        super(context);
    }

    public Switcher(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Switcher(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        viewFirst = (TextView) getChildAt(0);
        viewSecond = (TextView) getChildAt(1);
        viewThird = (TextView) getChildAt(2);
        viewFirst.setOnClickListener(v -> {
            YoYo.with(Techniques.SlideOutRight)
                    .duration(200)
                    .playOn(viewSecond);
            YoYo.with(Techniques.FadeOut)
                    .duration(200)
                    .playOn(viewSecond);

            YoYo.with(Techniques.SlideOutRight)
                    .duration(200)
                    .playOn(viewFirst);
            YoYo.with(Techniques.FadeOut)
                    .duration(200)
                    .playOn(viewFirst);
            setMode(getContext().getString(R.string.Tap1_Camera_Video_HD));
            if (switcherListener != null) switcherListener.switcher(v);
        });
        viewSecond.setOnClickListener(v -> {
            YoYo.with(Techniques.SlideOutRight)
                    .duration(200)
                    .playOn(viewSecond);
            YoYo.with(Techniques.FadeOut)
                    .duration(200)
                    .playOn(viewSecond);
            YoYo.with(Techniques.SlideOutRight)
                    .duration(200)
                    .playOn(viewFirst);
            YoYo.with(Techniques.FadeOut)
                    .duration(200)
                    .playOn(viewFirst);
            setMode(getContext().getString(R.string.Tap1_Camera_Video_SD));
            if (switcherListener != null) switcherListener.switcher(v);
        });
        viewThird.setOnClickListener(v -> {
            if (!viewFirst.isShown()) {
                viewFirst.setVisibility(VISIBLE);
                viewFirst.setAlpha(0.0f);
            }
            if (!viewSecond.isShown()) {
                viewSecond.setVisibility(VISIBLE);
                viewSecond.setAlpha(0.0f);
            }
            if (viewFirst.getAlpha() == 1.0) {
                YoYo.with(Techniques.SlideOutRight)
                        .interpolate(new DecelerateInterpolator())
                        .duration(200)
                        .playOn(viewSecond);
                YoYo.with(Techniques.FadeOut)
                        .duration(200)
                        .playOn(viewSecond);
                YoYo.with(Techniques.SlideOutRight)
                        .interpolate(new DecelerateInterpolator())
                        .duration(200)
                        .playOn(viewFirst);
                YoYo.with(Techniques.FadeOut)
                        .duration(200)
                        .playOn(viewFirst);
            } else {
                YoYo.with(Techniques.SlideInRight)
                        .interpolate(new DecelerateInterpolator())
                        .duration(200)
                        .playOn(viewSecond);
                YoYo.with(Techniques.FadeIn)
                        .duration(200)
                        .playOn(viewSecond);
                YoYo.with(Techniques.SlideInRight)
                        .interpolate(new DecelerateInterpolator())
                        .duration(200)
                        .playOn(viewFirst);
                YoYo.with(Techniques.FadeIn)
                        .duration(200)
                        .playOn(viewFirst);
            }
            if (switcherListener != null) switcherListener.switcher(v);
        });
    }

    private SwitcherListener switcherListener;

    public void setSwitcherListener(SwitcherListener switcherListener) {
        this.switcherListener = switcherListener;
    }

    public interface SwitcherListener {
        void switcher(View view);
    }

    public void setMode(String mode) {
        viewThird.setText(mode);
    }
}
