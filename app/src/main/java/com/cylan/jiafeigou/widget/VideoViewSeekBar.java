package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by yzd on 16-12-6.
 */

public class VideoViewSeekBar extends SeekBar {
    public VideoViewSeekBar(Context context) {
        super(context);
    }

    public VideoViewSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
    }

    @Override
    public synchronized void setMax(int max) {
        super.setMax(max);
    }
}
