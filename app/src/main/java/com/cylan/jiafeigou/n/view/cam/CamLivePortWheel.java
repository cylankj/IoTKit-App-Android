package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.wheel.ex.IData;
import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePortWheel extends FrameLayout implements SuperWheelExt.WheelRollListener {

    @BindView(R.id.sw_cam_live_wheel)
    SuperWheelExt swCamLiveWheel;
    @BindView(R.id.tv_cam_live_port_live)
    TextView tvCamLivePortLive;
    private IData iDataProvider;

    public CamLivePortWheel(Context context) {
        this(context, null);
    }

    public CamLivePortWheel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CamLivePortWheel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cam_port_live_wheel, this, true);
        ButterKnife.bind(view);
        swCamLiveWheel.setWheelRollListener(this);
//        swCamLiveWheel.setOnTouchListener(new OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int mask = event.getAction() & MotionEventCompat.ACTION_MASK;
//                // Always take care of the touch gesture being complete.
//                if (viewHandleListener != null)
//                    viewHandleListener.handleTouch(mask);
//                return true;
//            }
//        });
    }

    public IData getDataProvider() {
        return iDataProvider;
    }

    public void setupHistoryData(IData dataProvider) {
        final long time = System.currentTimeMillis();
        swCamLiveWheel.setDataProvider(dataProvider);
        Log.d("performance", "CamLivePortWheel performance: " + (System.currentTimeMillis() - time));
    }


    @OnClick(R.id.tv_cam_live_port_live)
    public void onClick() {

    }

    @Override
    public void onTimeUpdate(long time, int state) {

    }

    private ViewHandleListener viewHandleListener;

    public void setViewHandleListener(ViewHandleListener viewHandleListener) {
        this.viewHandleListener = viewHandleListener;
    }

    public interface ViewHandleListener {
        void handleTouch(int action);
    }
}
