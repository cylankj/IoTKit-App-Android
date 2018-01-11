package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.video.History;
import com.cylan.jiafeigou.module.CameraLiveHelper;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract;
import com.cylan.jiafeigou.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by cylan-hunt on 16-12-23.
 */

public class LiveTimeLayout extends FrameLayout implements LiveTimeSetter {

    private TextView textView;
    private int liveType;
    private long liveTime;
    private SimpleDateFormat dateFormat;


    public LiveTimeLayout(Context context) {
        this(context, null);
    }

    public LiveTimeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveTimeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View v = LayoutInflater.from(context).inflate(R.layout.layout_live_time, this);
        textView = (TextView) v.findViewById(R.id.tv_live_time);
        dateFormat = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
    }


    private String getTime(long time) {
        return dateFormat.format(new Date(TimeUtils.wrapToLong(time)));
//        return History.parseLiveTime(TimeUtils.wrapToLong(time));
    }

    private String getRealLiveTime(long time) {
        return History.parseLiveRealTime(time);
    }


    @Override
    public void setContent(int liveType, long liveTime) {
        if (!isShown()) {
            setVisibility(VISIBLE);
        }
        if (!textView.isShown()) {
            textView.setVisibility(View.VISIBLE);
        }
        this.liveType = liveType;
        if (liveType == CamLiveContract.TYPE_HISTORY) {
            this.liveTime = liveTime;
            String content = String.format(getContext().getString(R.string.Tap1_Camera_Playback) + "|%s", getTime(liveTime == 0 ? System.currentTimeMillis() : CameraLiveHelper.LongTimestamp(liveTime)));
            textView.setText(content);
        } else if (liveType == CamLiveContract.TYPE_LIVE) {
            String content = String.format(getContext().getString(R.string.Tap1_Camera_VideoLive) + "|%s", getRealLiveTime(System.currentTimeMillis()));
            textView.setText(content);
        }
    }

    @Override
    public void setTimeZone(TimeZone timeZone) {
        //这个只能在当前播放的历史录像和当前设备时区一致的情况下才会正确,
        //如果录像时是一个时区,然后后来改变了设备时区,那么时间也是不对的,
        //最好是在 rtcp 时带上设备的时区才是最稳妥的
        dateFormat.setTimeZone(timeZone);
    }
}

