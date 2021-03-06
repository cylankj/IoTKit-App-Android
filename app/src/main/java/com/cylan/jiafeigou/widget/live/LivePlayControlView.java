package com.cylan.jiafeigou.widget.live;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
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

public class LivePlayControlView extends RelativeLayout implements ILiveControl, View.OnClickListener {
    /**
     * 开始默认是loading状态
     */
    private int state = PLAY_STATE_PREPARE;

    private TextView textView;
    private TextView tvHelp;
    private SimpleProgressBar simpleProgressBar;
    private ImageView imageView;
    private Action action;
    private Handler handler;

    public LivePlayControlView(Context context) {
        this(context, null);
    }

    public LivePlayControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LivePlayControlView(Context context, AttributeSet attrs, int defStyleAttr) {
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

    public int getState() {
        return state;
    }

    @Override
    public void setState(int state, CharSequence content) {
        setState(state, content, null);
    }

    @Override
    public void setState(int state, CharSequence content, String help) {
        this.state = state;
        switch (state) {
            case PLAY_STATE_PREPARE:
                toDismiss(3);
                toDismiss(1);
                textView.setVisibility(GONE);
                tvHelp.setVisibility(GONE);
                imageView.setVisibility(GONE);
                if (!simpleProgressBar.isShown())
                    simpleProgressBar.setVisibility(VISIBLE);
                simpleProgressBar.setVisibility(VISIBLE);
                simpleProgressBar.bringToFront();
                break;
            case PLAY_STATE_PLAYING:
                handler.removeCallbacksAndMessages(null);
                setVisibility(INVISIBLE);
                imageView.bringToFront();
                imageView.setVisibility(VISIBLE);
                imageView.setImageResource(R.drawable.camera_icon_pause);
                textView.setVisibility(GONE);
                tvHelp.setVisibility(GONE);
                simpleProgressBar.setVisibility(GONE);
                break;
            case PLAY_STATE_STOP:
                toDismiss(1);
                imageView.bringToFront();
                imageView.setVisibility(VISIBLE);
                imageView.setImageResource(R.drawable.camera_icon_play);
                textView.setVisibility(GONE);
                tvHelp.setVisibility(GONE);
                simpleProgressBar.setVisibility(GONE);
                break;
            case PLAY_STATE_LOADING_FAILED:
                toDismiss(1);
                imageView.setVisibility(VISIBLE);
                imageView.bringToFront();
                imageView.setImageResource(R.drawable.btn_video_retry);
                simpleProgressBar.setVisibility(GONE);
                if (!textView.isShown())
                    textView.setVisibility(VISIBLE);
                if (!TextUtils.isEmpty(content))
                    textView.setText(content);
                if (!TextUtils.isEmpty(help)) {
                    tvHelp.setVisibility(VISIBLE);
                    tvHelp.setText(help);
                }
                break;
            case PLAY_STATE_IDLE:
                setVisibility(GONE);
                break;
        }
        Log.d("setState", "setState: " + state);
    }

    /**
     * 0:3s后准备隐藏
     * 1:一直显示
     * 2:马上隐藏
     * 3:马上显示
     *
     * @param show
     */
    private void toDismiss(int show) {
        if (handler == null) {
            handler = new Handler((Message msg) -> {
                switch (msg.what) {
                    case 0:
                        handler.removeMessages(0);
                        handler.removeMessages(2);
                        if (!LivePlayControlView.this.isShown()) {
                            handler.sendEmptyMessage(3);//马上显示
                            handler.sendEmptyMessageDelayed(2, 3000);
                        } else {
                            handler.sendEmptyMessage(2);//马上隐藏
                        }
                        break;
                    case 1:
                        handler.removeMessages(0);
                        handler.removeMessages(2);
                        handler.sendEmptyMessage(3);//马上显示
                        break;
                    case 2:
                        //隐藏
                        if (LivePlayControlView.this.isShown())
                            LivePlayControlView.this.setVisibility(INVISIBLE);
                        break;
                    case 3:
                        if (!LivePlayControlView.this.isShown())
                            LivePlayControlView.this.setVisibility(VISIBLE);
                        break;
                }
                return true;
            });
        }
        handler.sendEmptyMessage(show);
    }

    @Override
    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_state:
                if (action != null) action.clickImage(v, state);
                break;
            case R.id.tv_control_content:
                if (action != null) action.clickText(v);
                break;
            case R.id.tv_control_help:
                if (action != null) action.clickHelp(v);
                break;
        }
    }
}
