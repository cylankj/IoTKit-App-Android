package com.cylan.jiafeigou.widget.live;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.SimpleProgressBar;

import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_IDLE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_LOADING_FAILED;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PLAYING;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_PREPARE;
import static com.cylan.jiafeigou.misc.JConstant.PLAY_STATE_STOP;

/**
 * Created by cylan-hunt on 16-12-8.
 */

public class LiveControlView extends RelativeLayout implements ILiveControl, View.OnClickListener {
    /**
     * 开始默认是loading状态
     */
    private int state = PLAY_STATE_STOP;

    private TextView textView;
    private TextView tvHelp;
    private SimpleProgressBar simpleProgressBar;
    private ImageView imageView;
    private Action action;

    public LiveControlView(Context context) {
        this(context, null);
    }

    public LiveControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.live_play_control_layout_content, this, true);
        textView = (TextView) view.findViewById(R.id.tv_control_content);
        tvHelp = (TextView) view.findViewById(R.id.tv_control_help);
        simpleProgressBar = (SimpleProgressBar) view.findViewById(R.id.sp_control_loading);
        imageView = (ImageView) view.findViewById(R.id.img_state);
        imageView.setOnClickListener(this);
        tvHelp.setOnClickListener(this);
        textView.setOnClickListener(this);
    }

    private static final String TAG = "LiveControlView";

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void setState(int state, CharSequence content) {
        setState(state, content, null);
    }

    public void changeToLoading(boolean show) {
        setAlpha(show ? 1 : getAlpha());
        setVisibility(show ? VISIBLE : GONE);
        bringToFront();
        tvHelp.setVisibility(GONE);
        imageView.setVisibility(GONE);
        textView.setVisibility(GONE);
        simpleProgressBar.setVisibility(VISIBLE);
    }

    public void showLastLoading() {
        setVisibility(VISIBLE);
        bringToFront();
        imageView.setVisibility(GONE);
        tvHelp.setVisibility(GONE);
        simpleProgressBar.setVisibility(VISIBLE);
        if (!TextUtils.isEmpty(textView.getText())) {
            textView.setVisibility(VISIBLE);
        }
    }

    public void changeToLoading(boolean show, String content, String help) {
        setVisibility(show ? VISIBLE : GONE);
        setAlpha(show ? 1 : getAlpha());
        simpleProgressBar.setVisibility(VISIBLE);
        bringToFront();
        imageView.setVisibility(GONE);
        if (!TextUtils.isEmpty(content)) {
            textView.setVisibility(VISIBLE);
            textView.setText(content);
        }
        if (!TextUtils.isEmpty(help)) {
            tvHelp.setVisibility(VISIBLE);
            tvHelp.setText(help);
        }
    }

    public void showLastLoadingError() {
        setVisibility(VISIBLE);
        bringToFront();
        imageView.setVisibility(VISIBLE);
        imageView.setImageResource(R.drawable.btn_video_retry);
        simpleProgressBar.setVisibility(GONE);
        if (!TextUtils.isEmpty(textView.getText())) {
            textView.setVisibility(VISIBLE);
        }
        if (!TextUtils.isEmpty(tvHelp.getText())) {
            tvHelp.setVisibility(VISIBLE);
        }
    }

    public void changeToLoadingError(boolean show, String content, String help) {
        setVisibility(show ? VISIBLE : GONE);
        setAlpha(show ? 1 : getAlpha());
        imageView.setVisibility(VISIBLE);
        bringToFront();
        imageView.setImageResource(R.drawable.btn_video_retry);
        simpleProgressBar.setVisibility(GONE);
        textView.setVisibility(VISIBLE);
        if (!TextUtils.isEmpty(content)) {
            textView.setText(content);
        }
        if (!TextUtils.isEmpty(help)) {
            tvHelp.setVisibility(VISIBLE);
            tvHelp.setText(help);
        }
    }

    public void changeToPlaying(boolean show) {
        setVisibility(show ? VISIBLE : GONE);
        setAlpha(show ? 1 : getAlpha());
        imageView.setVisibility(VISIBLE);
        bringToFront();
        imageView.setImageResource(R.drawable.camera_icon_play);
        textView.setVisibility(GONE);
        tvHelp.setVisibility(GONE);
        simpleProgressBar.setVisibility(GONE);
    }

    public void changeToPause(boolean show) {
        setVisibility(show ? VISIBLE : GONE);
        setAlpha(show ? 1 : getAlpha());
        imageView.setVisibility(VISIBLE);
        bringToFront();
        imageView.setImageResource(R.drawable.camera_icon_pause);
        textView.setVisibility(GONE);
        tvHelp.setVisibility(GONE);
        simpleProgressBar.setVisibility(GONE);
    }

    public void hideLoading() {
        setVisibility(INVISIBLE);
        bringToFront();
        simpleProgressBar.setVisibility(GONE);
        imageView.setVisibility(GONE);
        textView.setVisibility(GONE);
        tvHelp.setVisibility(GONE);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void show() {
        setAlpha(1);
        setVisibility(VISIBLE);
        bringToFront();
    }

    public void showOrHide(boolean show) {
        if (show) {
            show();
        } else {
            hide();
        }
    }

    @Override
    public void setState(int state, CharSequence content, String help) {
        if (this.state == state) {
            return;
        }
        this.state = state;
        switch (state) {
            case PLAY_STATE_PREPARE:
                setVisibility(VISIBLE);
                tvHelp.setVisibility(GONE);
                if (!TextUtils.isEmpty(content)) {
                    textView.setVisibility(VISIBLE);
                    textView.setText(content);
                } else {
                    textView.setVisibility(GONE);
                }

                simpleProgressBar.setVisibility(VISIBLE);
                simpleProgressBar.bringToFront();
                break;
            case PLAY_STATE_PLAYING:
                setVisibility(VISIBLE);
                imageView.bringToFront();
                imageView.setImageResource(R.drawable.camera_icon_pause);
                textView.setVisibility(GONE);
                tvHelp.setVisibility(GONE);
                simpleProgressBar.setVisibility(GONE);
                break;
            case PLAY_STATE_STOP:
                imageView.bringToFront();
                imageView.setImageResource(R.drawable.camera_icon_play);
                textView.setVisibility(GONE);
                tvHelp.setVisibility(GONE);
                simpleProgressBar.setVisibility(GONE);
                break;
            case PLAY_STATE_LOADING_FAILED:
                setVisibility(VISIBLE);
                imageView.setVisibility(VISIBLE);
                imageView.bringToFront();
                imageView.setImageResource(R.drawable.btn_video_retry);
                simpleProgressBar.setVisibility(GONE);
                textView.setVisibility(VISIBLE);
                if (!TextUtils.isEmpty(content)) {
                    textView.setText(content);
                }
                if (!TextUtils.isEmpty(help)) {
                    tvHelp.setVisibility(VISIBLE);
                    tvHelp.setText(help);
                }
                break;
            case PLAY_STATE_IDLE: {
                setVisibility(GONE);
            }
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
                if (action != null) {
                    action.clickImage(v, state);
                }
                break;
            case R.id.tv_control_content:
                if (action != null) {
                    action.clickText(v);
                }
                break;
            case R.id.tv_control_help:
                if (action != null) {
                    action.clickHelp(v);
                }
                break;
        }
    }


}
