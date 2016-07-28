package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.SimpleProgressBar;
import com.cylan.jiafeigou.widget.wheel.SDataStack;
import com.cylan.jiafeigou.widget.wheel.SuperWheel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLivePortWheel extends FrameLayout implements SuperWheel.WheelRollListener {

    @BindView(R.id.sw_cam_live_wheel)
    SuperWheel swCamLiveWheel;
    @BindView(R.id.pb_loading_history)
    SimpleProgressBar pbLoadingHistory;
    @BindView(R.id.tv_cam_live_port_live)
    TextView tvCamLivePortLive;

//    private DataProviderImpl dataProvider;

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
    }

    /**
     * @param loading
     */
    public void loading(boolean loading) {
        pbLoadingHistory.setVisibility(loading ? VISIBLE : INVISIBLE);
    }

    public void setupHistoryData(SDataStack dataStack) {
        final long time = System.currentTimeMillis();
        swCamLiveWheel.setDataStack(dataStack);
        Log.d("performance", "CamLivePortWheel performance: " + (System.currentTimeMillis() - time));
    }

    @Override

    public void onTimeUpdate(long time) {

    }

    @Override
    public void onSettleFinish(long time) {

    }

    @OnClick(R.id.tv_cam_live_port_live)
    public void onClick() {
    }
}
