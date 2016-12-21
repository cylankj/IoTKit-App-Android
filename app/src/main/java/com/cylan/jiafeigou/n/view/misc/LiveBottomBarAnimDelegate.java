package com.cylan.jiafeigou.n.view.misc;

/**
 * Created by cylan-hunt on 16-7-13.
 */

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.lang.ref.WeakReference;


//
// 安全防护   直播|5/16 12:30   全屏
//

/**
 * 这一个块的 show 和hide动画
 */
public class LiveBottomBarAnimDelegate {
    private WeakReference<ViewGroup> weakReference;
    private WeakReference<Activity> activityWeakReference;
    private View vProtection;
    private View vLiveTime;
    private View vZoomFullScreen;

    public LiveBottomBarAnimDelegate(Activity activity, ViewGroup view) {
        weakReference = new WeakReference<>(view);
        activityWeakReference = new WeakReference<>(activity);
        initView();
    }

    private void initView() {
        if (weakReference == null || weakReference.get() == null)
            return;
        vProtection = weakReference.get().findViewById(R.id.lLayout_protection);
        vLiveTime = weakReference.get().findViewById(R.id.lLayout_live_time);
        vZoomFullScreen = weakReference.get().findViewById(R.id.imgV_cam_zoom_to_full_screen);
        //设置全屏点击
        vZoomFullScreen.setOnClickListener((View v) -> {
            if (activityWeakReference == null
                    || activityWeakReference.get() == null)
                return;
            ViewUtils.setRequestedOrientation(activityWeakReference.get(),
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        });
        vLiveTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    /**
     * 显示热区
     *
     * @param show
     */
    public void showLiveTimeRect(boolean show) {
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

    }

    /**
     * 更新播放时间
     *
     * @param time
     */
    public void setLiveTime(long time) {

    }
}