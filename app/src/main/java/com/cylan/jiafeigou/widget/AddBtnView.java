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

import java.util.HashMap;
import java.util.Map;

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
        View v = new View(context);
        v.setId("0".hashCode());
        v.setBackground(getResources().getDrawable(R.drawable.white_circle));
        addView(v, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        View v1 = new View(context);
        v1.setId("1".hashCode());
        v1.setBackground(getResources().getDrawable(R.drawable.white_circle));
        addView(v1, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ImageView im = new ImageView(context);
        FrameLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        im.setImageResource(R.drawable.btn_common_add);
        addView(im, lp);
        startAnim();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(height, height);
    }

    public void startAnim() {
        View v = findViewById("0".hashCode());
        AnimatorSet set0 = prepareAnim(v);
        set0.setDuration(800);
        map.put(v, set0);
        v = findViewById("1".hashCode());
        AnimatorSet set1 = prepareAnim(v);
        set1.setStartDelay(200);
        set1.setDuration(800);
        map.put(v, set1);
        set0.start();
        set1.start();
    }

    private AnimatorSet prepareAnim(View v) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(v,
                "scaleX", 0.8f, 1.0f, 0.8f);
        animatorX.setRepeatMode(INFINITE);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(v,
                "scaleY", 0.8f, 1.0f, 0.8f);
        animatorY.setRepeatMode(INFINITE);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(v,
                "alpha", 1.0f, 0.5f, 1.0f);
        alpha.setRepeatMode(INFINITE);
        set.playTogether(animatorX, animatorY, alpha);
        return set;
    }

    private Map<View, AnimatorSet> map = new HashMap<>();

    public void dismissAnim() {

    }
}
