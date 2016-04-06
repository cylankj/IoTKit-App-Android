package com.cylan.jiafeigou.activity.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.DensityUtil;

public class AddDeviceDialog extends Dialog implements OnClickListener, DialogInterface.OnShowListener {

    private View mView;
    private TextView mAddVideoView;
    private TextView mAddDoorBellView;
    private TextView mAddEfamily;
    private ImageView mAddView;
    private static final int DURATION_200 = 200;
    private static final int DURATION_250 = 250;

    public AddDeviceDialog(Context context) {
        super(context, R.style.add_page_dialog);
        mView = LayoutInflater.from(context).inflate(R.layout.layout_adddevice, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DensityUtil.getScreenWidth(context), DensityUtil.getScreenHeight(context));
        mView.setLayoutParams(params);
        mAddVideoView = (TextView) mView.findViewById(R.id.video);
        mAddDoorBellView = (TextView) mView.findViewById(R.id.doorbell);
        mAddEfamily = (TextView) mView.findViewById(R.id.efamily);
        mAddView = (ImageView) mView.findViewById(R.id.btn_add);

//        int viewWidth = getViewWidth(mAddVideoView);

//        RelativeLayout.LayoutParams mBtnParams = (RelativeLayout.LayoutParams) mAddVideoView.getLayoutParams();
//        mBtnParams.setMargins(DensityUtil.getScreenWidth(context) / 3 - (viewWidth / 2), 0, 0, 0);
//        mAddVideoView.setLayoutParams(mBtnParams);
//
//        RelativeLayout.LayoutParams mBtnParams1 = (RelativeLayout.LayoutParams) mAddDoorBellView.getLayoutParams();
//        mBtnParams1.setMargins(0, 0, DensityUtil.getScreenWidth(context) / 3 - (viewWidth / 2), 0);
//        mAddDoorBellView.setLayoutParams(mBtnParams1);

        mAddVideoView.setOnClickListener((View.OnClickListener) context);
        mAddVideoView.setOnTouchListener(new MyOnTouchListener());

        mAddDoorBellView.setOnClickListener((View.OnClickListener) context);
        mAddDoorBellView.setOnTouchListener(new MyOnTouchListener());

        mAddEfamily.setOnClickListener((View.OnClickListener) context);
        mAddEfamily.setOnTouchListener(new MyOnTouchListener());

        mAddView.setOnClickListener(this);

        setOnShowListener(this);

        setContentView(mView);
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.width = display.getWidth(); // set width
        this.getWindow().setAttributes(lp);

    }


    public void setAddCameraListener(View.OnClickListener listener) {
        mAddVideoView.setOnClickListener(listener);
    }

    public void setAddDoorBellListener(View.OnClickListener listener) {
        mAddDoorBellView.setOnClickListener(listener);
    }

    public void setmAddEfamilyListener(View.OnClickListener listener) {
        mAddEfamily.setOnClickListener(listener);
    }


    private void onDismiss() {

        AddButtonRotateAnimation(mAddView, 45f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, DURATION_200);
        ObjectAnimator mAlphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.1f);
        ObjectAnimator mTranAnimator1 = ObjectAnimator.ofFloat(mAddVideoView, "translationY", 0, -(mView.getHeight() / 2 + mAddVideoView.getHeight()));
        ObjectAnimator mTranAnimator2 = ObjectAnimator.ofFloat(mAddDoorBellView, "translationY", 0, -(mView.getHeight() / 2 + mAddDoorBellView.getHeight()));
        ObjectAnimator mTranAnimator3 = ObjectAnimator.ofFloat(mAddEfamily, "translationY", 0, -(mView.getHeight() / 2 + mAddEfamily.getHeight()));
        AnimatorSet set = new AnimatorSet();
        set.playTogether(mAlphaAnimator, mTranAnimator1, mTranAnimator2, mTranAnimator3);
        set.setDuration(DURATION_200);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dismiss();
            }
        });
        set.start();
    }


    private void onShow() {
        AddButtonRotateAnimation(mAddView, 0, 45f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f, DURATION_250);
        ObjectAnimator.ofFloat(this, "alpha", 0.1f, 1.0f).setDuration(DURATION_250).start();
        ObjectAnimator mTranAnimator1 = ObjectAnimator.ofFloat(mAddVideoView, "translationY", mView.getHeight() / 2, 0).setDuration(DURATION_250);
        mTranAnimator1.start();
        mTranAnimator1.setInterpolator(new OvershootInterpolator(0.9f));
        ObjectAnimator mTranAnimator2 = ObjectAnimator.ofFloat(mAddDoorBellView, "translationY", mView.getHeight() / 2, 0).setDuration(DURATION_250);
        mTranAnimator2.start();
        mTranAnimator2.setInterpolator(new OvershootInterpolator(0.9f));
        ObjectAnimator mTranAnimator3 = ObjectAnimator.ofFloat(mAddEfamily, "translationY", mView.getHeight() / 2, 0).setDuration(DURATION_250);
        mTranAnimator3.start();
        mTranAnimator3.setInterpolator(new OvershootInterpolator(0.9f));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                if (isShowing()) {
                    disDialog();
                } else {
                    showMyDialog();
                }
                break;
        }
    }


    public void showMyDialog() {
        show();
    }

    public void disDialog() {
        onDismiss();
    }

    @Override
    public void onShow(DialogInterface dialog) {
        onShow();
    }

    private int getViewWidth(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return view.getMeasuredWidth();
    }

    private void AddButtonRotateAnimation(View view, float fromDegrees, float toDegrees, int pivotXType, float pivotXValue, int pivotYType, float pivotYValue,
                                          long dus) {

        RotateAnimation mAnimation = new RotateAnimation(fromDegrees, toDegrees, pivotXType, pivotXValue, pivotYType, pivotYValue);// fromDegrees,

        mAnimation.setStartOffset(0);
        mAnimation.setDuration(dus);
        mAnimation.setFillAfter(true);

        view.startAnimation(mAnimation);
    }

}

