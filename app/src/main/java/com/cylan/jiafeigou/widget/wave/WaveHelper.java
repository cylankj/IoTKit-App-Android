package com.cylan.jiafeigou.widget.wave;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.annotation.UiThread;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

public class WaveHelper {
    private final static float AMPLITUDE_RATIO_MAX = 0.05f;
    private final static float AMPLITUDE_RATIO_MIN = 0.001f;
    private final static float WATER_LEVEL_MAX = 0.01f;
    private WaveView mWaveView;

    private AnimatorSet mAnimatorSet;

    public WaveHelper(WaveView waveView) {
        mWaveView = waveView;
        initAnimation();
    }

    public void start() {
        mWaveView.setShowWave(true);
        if (mAnimatorSet != null && !mAnimatorSet.isRunning()) {
            mAnimatorSet.start();
        }
    }

    /**
     * @param amplitudeRatio: [0.1,1]
     */
    @UiThread
    public void updateAmplitudeRatio(final float amplitudeRatio) {
        //最高0.08,最低0.0001
        if (amplitudeRatio < 0 || amplitudeRatio > 1)
            return;

        mWaveView.setAmplitudeRatio((AMPLITUDE_RATIO_MAX - AMPLITUDE_RATIO_MIN) * (1.0f - amplitudeRatio));
    }

    private void initAnimation() {
        List<Animator> animators = new ArrayList<>();

        // horizontal animation.
        // wave waves infinitely.
        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
                mWaveView, "waveShiftRatio", 0f, 1.0f);
        waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
        waveShiftAnim.setDuration(30000);
        waveShiftAnim.setInterpolator(new LinearInterpolator());
        animators.add(waveShiftAnim);

        // vertical animation.
        // water level increases from 0 to center of WaveView
        mWaveView.setWaterLevelRatio(WATER_LEVEL_MAX);
        //最高0.08,最低0.0001
        mWaveView.setAmplitudeRatio(AMPLITUDE_RATIO_MAX);
//        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
//                mWaveView, "waterLevelRatio", 0.01f, 0.08f);
//        waterLevelAnim.setDuration(15000);
//        waterLevelAnim.setInterpolator(new DecelerateInterpolator());
//        animators.add(waterLevelAnim);

        // amplitude animation.
        // wave grows big then grows small, repeatedly
//        ObjectAnimator amplitudeAnim = ObjectAnimator.ofFloat(
//                mWaveView, "amplitudeRatio", 0.0001f, 0.05f);
//        amplitudeAnim.setRepeatCount(ValueAnimator.INFINITE);
//        amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
//        amplitudeAnim.setDuration(5000);
//        amplitudeAnim.setInterpolator(new LinearInterpolator());
//        animators.add(amplitudeAnim);

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(animators);
    }

    public void cancel() {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet.end();
        }
    }
}
