package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

import static android.view.animation.Animation.INFINITE;

/**
 * Created by hds on 17-7-11.
 */

public class AddBtnView extends FrameLayout {

    public AddBtnView(@NonNull Context context) {
        this(context, null);
    }

    public AddBtnView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddBtnView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ImageView im = new ImageView(context);
        FrameLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        im.setImageResource(R.drawable.btn_common_add);
        addView(im, lp);
//        startAnim();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(height, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (findViewById("0".hashCode()) != null) {
            ViewGroup.LayoutParams lp = findViewById("0".hashCode()).getLayoutParams();
            lp.height = getWidth();
            lp.width = getWidth();
            updateViewLayout(findViewById("0".hashCode()), lp);
            updateViewLayout(findViewById("1".hashCode()), lp);
        }
    }

    public void startAnim() {
        View v = new View(getContext());
        v.setId("0".hashCode());
        v.setBackground(getResources().getDrawable(R.drawable.white_circle));
        addView(v, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        AnimatorSet set0 = prepareAnim(v);
        set0.setDuration(1500);
        list.add(set0);

        View v1 = new View(getContext());
        v1.setId("1".hashCode());
        v1.setBackground(getResources().getDrawable(R.drawable.white_circle));
        addView(v1, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        v1.setVisibility(VISIBLE);
        AnimatorSet set1 = prepareAnim(v1);
        set1.setStartDelay(200);
        set1.setDuration(1500);
        list.add(set1);
        set0.start();
        set1.start();
    }

    private AnimatorSet prepareAnim(View v) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(v,
                "scaleX", 0.6f, 0.9f, 0.6f);
        animatorX.setRepeatCount(INFINITE);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(v,
                "scaleY", 0.6f, 0.9f, 0.6f);
        animatorY.setRepeatCount(INFINITE);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(v,
                "alpha", 0.0f, 1.0f, 0.5f, 0.0f);
        alpha.setRepeatCount(INFINITE);
        set.playTogether(animatorX, animatorY, alpha);
        return set;
    }

    private List<AnimatorSet> list = new ArrayList<>();

    public void dismissAnim() {
        if (list != null) {
            for (AnimatorSet set : list) {
                if (set != null) set.cancel();
            }
        }
        View v = findViewById("0".hashCode());
        if (v != null) {
            v.setVisibility(INVISIBLE);
            removeView(v);
        }
        v = findViewById("1".hashCode());
        if (v != null) {
            v.setVisibility(INVISIBLE);
            removeView(v);
        }
    }
}
