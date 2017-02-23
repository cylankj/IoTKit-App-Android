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
    private Handler handler;

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

    public int getState() {
        return state;
    }

    @Override
    public void setState(int state, CharSequence content) {
        this.state = state;
        switch (state) {
            case STATE_LOADING:
                toDismiss(3);
                toDismiss(1);
                textView.setVisibility(GONE);
                imageView.setVisibility(GONE);
                if (!simpleProgressBar.isShown())
                    simpleProgressBar.setVisibility(VISIBLE);
                simpleProgressBar.setVisibility(VISIBLE);
                break;
            case STATE_PLAYING:
                toDismiss(0);
                if (!imageView.isShown())
                    imageView.setVisibility(VISIBLE);
                imageView.setImageResource(R.drawable.camera_icon_pause);
                textView.setVisibility(GONE);
                simpleProgressBar.setVisibility(GONE);
                break;
            case STATE_STOP:
                toDismiss(1);
                if (!imageView.isShown())
                    imageView.setVisibility(VISIBLE);
                imageView.setImageResource(R.drawable.camera_icon_play);
                textView.setVisibility(GONE);
                simpleProgressBar.setVisibility(GONE);
                break;
            case STATE_LOADING_FAILED:
                toDismiss(1);
                if (!imageView.isShown())
                    imageView.setVisibility(VISIBLE);
                imageView.setImageResource(R.drawable.btn_video_retry);
                simpleProgressBar.setVisibility(GONE);
                if (!textView.isShown())
                    textView.setVisibility(VISIBLE);
                if (!TextUtils.isEmpty(content))
                    textView.setText(content);
                break;
            case STATE_IDLE:
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
                if (action != null) action.clickImage(state);
                break;
            case R.id.tv_control_content:
                if (action != null) action.clickText();
                break;
        }
    }
}
