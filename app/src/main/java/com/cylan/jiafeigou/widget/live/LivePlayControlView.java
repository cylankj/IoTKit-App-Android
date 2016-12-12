package com.cylan.jiafeigou.widget.live;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.SimpleProgressBar;

/**
 * Created by cylan-hunt on 16-12-8.
 */

public class LivePlayControlView extends LinearLayout implements ILiveControl, View.OnClickListener {
    /**
     * 开始默认是loading状态
     */
    private int state = STATE_LOADING;

    private TextView textView;
    private SimpleProgressBar simpleProgressBar;
    private ImageView imageView;
    private Action action;

    public LivePlayControlView(Context context) {
        this(context, null);
    }

    public LivePlayControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LivePlayControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.live_play_control_layout_content, this, true);
        textView = (TextView) view.findViewById(R.id.tv_control_content);
        simpleProgressBar = (SimpleProgressBar) view.findViewById(R.id.sp_control_loading);
        imageView = (ImageView) view.findViewById(R.id.img_state);
        imageView.setOnClickListener(this);
        textView.setOnClickListener(this);
    }

    @Override
    public void setState(int state, CharSequence content) {
        this.state = state;
        switch (state) {
            case STATE_LOADING:
                textView.setVisibility(GONE);
                imageView.setVisibility(GONE);
                if (!simpleProgressBar.isShown())
                    simpleProgressBar.setVisibility(VISIBLE);
                simpleProgressBar.setVisibility(VISIBLE);
                break;
            case STATE_PLAYING:
                if (!imageView.isShown())
                    imageView.setVisibility(VISIBLE);
                imageView.setImageResource(R.drawable.btn_video_playing);
                textView.setVisibility(GONE);
                simpleProgressBar.setVisibility(GONE);
                break;
            case STATE_STOP:
                if (!imageView.isShown())
                    imageView.setVisibility(VISIBLE);
                imageView.setImageResource(R.drawable.btn_video_stop);
                textView.setVisibility(GONE);
                simpleProgressBar.setVisibility(GONE);
                break;
            case STATE_LOADING_FAILED:
                if (!imageView.isShown())
                    imageView.setVisibility(VISIBLE);
                imageView.setImageResource(R.drawable.btn_video_retry);
                simpleProgressBar.setVisibility(GONE);
                if (!textView.isShown())
                    textView.setVisibility(VISIBLE);
                textView.setText(content);
                break;
            case STATE_SHOWING_OR_HIDING:
                break;
        }
        Log.d("setState", "setState: " + state);
    }

    @Override
    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_state:
                if (action != null) action.clickImage(state);
                break;
            case R.id.tv_control_content:
                if (action != null) action.clickText();
                break;
        }
    }
}
