package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;

/**
 * Created by HeBin on 2015/5/27.
 */
public class AddDeviceLayout extends RelativeLayout {


    private ImageView mAdd;
    private ImageView mAddBg;
    private boolean isClick;
    private boolean isFirst = true;
    private AnimationSet mCircleAnimation;
    private RotateAnimation mRotateAnimation;


    public AddDeviceLayout(Context context) {
        this(context, null);
    }

    public AddDeviceLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddDeviceLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.layout_adddevice_btn, this);
        initView();
    }


    private void initView() {
        mAddBg = (ImageView) getChildAt(0);
        mAdd = (ImageView) getChildAt(1);
    }

    public void startCircle() {
        mAddBg.setImageResource(R.drawable.add_circle);
        mCircleAnimation = new AnimationSet(true);
        ScaleAnimation scale = new ScaleAnimation(Animation.RELATIVE_TO_SELF, 1.5f, Animation.RELATIVE_TO_SELF, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setRepeatMode(Animation.RESTART);
        scale.setRepeatCount(-1);
        mCircleAnimation.addAnimation(scale);

        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.1f);
        alpha.setRepeatMode(Animation.RESTART);
        alpha.setRepeatCount(-1);
        mCircleAnimation.addAnimation(alpha);
        mCircleAnimation.setDuration(800);
        mAddBg.startAnimation(mCircleAnimation);
    }

    private void AddButtonRotateAnimation(float fromDegrees, float toDegrees, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue,
                                          long dus) {

        mRotateAnimation = new RotateAnimation(fromDegrees, toDegrees, pivotXType, pivotXValue, pivotYType, pivotYValue);
        mRotateAnimation.setStartOffset(0);
        mRotateAnimation.setDuration(dus);
        mRotateAnimation.setFillAfter(true);

        mAdd.startAnimation(mRotateAnimation);
    }


    public void setOnClickListener(OnClickListener l) {
        mAdd.setOnClickListener(l);
        if (isFirst) {
            isFirst = false;
            return;
        }
        if (isClick) {
            AddButtonRotateAnimation(0, 45f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, 250);
        } else {
            AddButtonRotateAnimation(45f, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, 250);
        }

        isClick = !isClick;

    }

    public void stopCircle() {
        mAddBg.clearAnimation();
        mAddBg.setImageResource(0);
    }

}
