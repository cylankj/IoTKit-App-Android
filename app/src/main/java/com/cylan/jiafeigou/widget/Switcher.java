package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            slideOut();
            int index = content2Index(viewFirst.getText().toString());
            viewFirst.setText(viewThird.getText());
            viewThird.setText(index2Content(index));
            if (switcherListener != null) switcherListener.switcher(v, index);
        });
        viewSecond.setOnClickListener(v -> {
            slideOut();
            int index = content2Index(viewSecond.getText().toString());
            viewSecond.setText(viewThird.getText());
            viewThird.setText(index2Content(index));
            if (switcherListener != null) switcherListener.switcher(v, index);
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
            removeCallbacks(autoSlideOut);
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
                postDelayed(autoSlideOut, 3000);
            }
            if (switcherListener != null)
                switcherListener.switcher(v, content2Index(viewThird.getText()));
        });
    }

    private Runnable autoSlideOut = this::slideOut;

    private void slideOut() {
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
    }

    private SwitcherListener switcherListener;

    public void setSwitcherListener(SwitcherListener switcherListener) {
        this.switcherListener = switcherListener;
    }

    public interface SwitcherListener {
        void switcher(View view, int mode);
    }

    private static final Integer[] sId = {R.string.Tap1_Camera_Video_Auto,
            R.string.Tap1_Camera_Video_SD,
            R.string.Tap1_Camera_Video_HD};

    private int content2Index(CharSequence content) {
        if (TextUtils.equals(getResources().getString(R.string.Tap1_Camera_Video_Auto), content))
            return 0;
        if (TextUtils.equals(getResources().getString(R.string.Tap1_Camera_Video_SD), content))
            return 1;
        return 2;
    }

    private String index2Content(int mode) {
        return getResources().getString(sId[mode]);
    }

    public void setMode(int mode) {
        List<Integer> list = new ArrayList<>(Arrays.asList(sId));
        list.remove(mode);
        viewThird.setText(getResources().getString(sId[mode]));
        viewFirst.setText(getResources().getString(list.get(0)));
        viewSecond.setText(getResources().getString(list.get(1)));
    }
}
