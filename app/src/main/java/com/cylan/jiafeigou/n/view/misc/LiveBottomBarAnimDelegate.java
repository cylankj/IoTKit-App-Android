package com.cylan.jiafeigou.n.view.misc;

/**
 * Created by cylan-hunt on 16-7-13.
 */

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.FlipImageView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


//
// 安全防护   直播|5/16 12:30   全屏
//

/**
 * 这一个块的 show 和hide动画
 */
public class LiveBottomBarAnimDelegate implements FlipImageView.OnFlipListener {
    private WeakReference<ViewGroup> weakReference;
    private WeakReference<Activity> activityWeakReference;
    private TextView vLiveTime;
    private FlipImageView flipImageView;
    private TextView tvProtection;
    private CamLiveContract.Presenter presenter;

    public LiveBottomBarAnimDelegate(Activity activity, ViewGroup view, CamLiveContract.Presenter presenter) {
        weakReference = new WeakReference<>(view);
        activityWeakReference = new WeakReference<>(activity);
        this.presenter = presenter;
        initView();
    }

    private void initView() {
        if (weakReference == null || weakReference.get() == null)
            return;
        View vProtection = weakReference.get().findViewById(R.id.lLayout_protection);
        tvProtection = (TextView) weakReference.get().findViewById(R.id.tv_cam_live_protection);
        flipImageView = (FlipImageView) vProtection.findViewById(R.id.flip_image);
        //设置切换动画
        flipImageView.setRotationXEnabled(true);
        flipImageView.setDuration(200);
        flipImageView.setInterpolator(new DecelerateInterpolator());
        flipImageView.setOnFlipListener(this);
        //初始状态
        if (presenter.getCamInfo() != null) {
            if (!presenter.getCamInfo().cameraAlarmFlag) {
                flipImageView.setFlipped(true);
            }
        }
        //大区域
        vProtection.setOnClickListener((View v) -> {
            flipImageView.performClick();
        });
        vLiveTime = (TextView) weakReference.get().findViewById(R.id.tv_live_time);
        View vZoomFullScreen = weakReference.get().findViewById(R.id.imgV_cam_zoom_to_full_screen);
        //设置全屏点击
        vZoomFullScreen.setOnClickListener((View v) -> {
            if (activityWeakReference == null
                    || activityWeakReference.get() == null)
                return;
            ViewUtils.setRequestedOrientation(activityWeakReference.get(),
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        vLiveTime.setOnClickListener((View v) -> {
            if (liveTimeRectListener != null) liveTimeRectListener.click();
        });
    }

    /**
     * 显示热区
     *
     * @param show
     */
    public void showLiveTimeRect(boolean show) {
        if (!check())
            return;
        if (weakReference != null && weakReference.get() != null) {
            vLiveTime.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 设置安全防护状态
     *
     * @param state
     */
    public void setProtectionState(boolean state) {
        if (!check())
            return;
        if (flipImageView != null) {
            boolean isFlipped = flipImageView.isFlipped();//切换到背面
            if ((isFlipped && state) || (!state && !isFlipped)) flipImageView.performClick();
            tvProtection.setText(state ? tvProtection.getContext().getString(R.string.SECURE) : "");
        }
    }

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());

    /**
     * 更新播放时间
     *
     * @param state 直播|历史录像|空闲  1:直播 2:录像
     * @param time
     */
    public void setLiveTime(int state, long time) {
        if (!check() || state == 0)
            return;
        String content = String.format(vLiveTime.getContext().getString(
                state == 1 ? R.string.Tap1_Camera_VideoLive : R.string.Tap1_Camera_Playback)
                + "|%s", simpleDateFormat.format(new Date(time)));
        if (!vLiveTime.isShown()) vLiveTime.setVisibility(View.VISIBLE);
        vLiveTime.setText(content);
    }


    private boolean check() {
        return weakReference != null && weakReference.get() != null && activityWeakReference != null && activityWeakReference.get() != null;
    }

    @Override
    public void onClick(FlipImageView view) {
        presenter.saveAlarmFlag(!view.isFlipped());
    }

    @Override
    public void onFlipStart(FlipImageView view) {

    }

    @Override
    public void onFlipEnd(FlipImageView view) {

    }

    private LiveTimeRectListener liveTimeRectListener;

    public void setLiveTimeRectListener(LiveTimeRectListener liveTimeRectListener) {
        this.liveTimeRectListener = liveTimeRectListener;
    }

    public interface LiveTimeRectListener {
        void click();
    }
}