package com.cylan.jiafeigou.activity.video;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.ToastUtil;

/**
 * Created by hebin on 2015/10/16.
 */
public class ShotPhotoAnimation {

    public ShotPhotoAnimation(Context ctx, ViewGroup rootView) {
        takePicAnim(ctx, rootView);
    }

    private void takePicAnim(final Context ctx, final ViewGroup rootView) {
        final MediaPlayer shootMP = MediaPlayer.create(ctx, R.raw.camera_click);
        shootMP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                mp.release();
            }
        });
        shootMP.start();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DensityUtil.getScreenWidth(ctx), DensityUtil.getScreenHeight(ctx));
        final ImageView img = new ImageView(ctx);
        img.setImageResource(R.color.white);
        img.setScaleType(ImageView.ScaleType.FIT_XY);
        img.setLayoutParams(params);
        rootView.addView(img);
        img.setVisibility(View.VISIBLE);
        img.clearAnimation();
        AlphaAnimation alpha = new AlphaAnimation(1.0f, 0.1f);
        alpha.setDuration(500);
        alpha.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.GONE);
                ToastUtil.showSuccessToast(ctx, ctx.getString(R.string.SAVED_PHOTOS));
                rootView.removeView(img);

            }
        });
        img.startAnimation(alpha);
    }
}
