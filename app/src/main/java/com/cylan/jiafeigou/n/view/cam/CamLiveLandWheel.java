package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.wheel.DataProviderImpl;
import com.cylan.jiafeigou.widget.wheel.SDataStack;
import com.cylan.jiafeigou.widget.wheel.SuperWheel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamLiveLandWheel extends FrameLayout implements SuperWheel.WheelRollListener {
    @BindView(R.id.tv_cam_live_land_date_pop)
    TextView tvCamLiveLandDatePop;
    @BindView(R.id.sw_cam_live_wheel)
    SuperWheel swCamLiveWheel;
    DataProviderImpl dataProvider;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M/dd HH:mm", Locale.getDefault());

    public CamLiveLandWheel(Context context) {
        this(context, null);
    }

    public CamLiveLandWheel(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CamLiveLandWheel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_cam_land_live_wheel, this, true);
        ButterKnife.bind(view);
        swCamLiveWheel.setWheelRollListener(this);
    }

    public void setupHistoryData(long[] timeSet) {
        final long time = System.currentTimeMillis();
        if (timeSet == null || timeSet.length == 0)
            return;
        if (dataProvider == null) {
            dataProvider = new DataProviderImpl();
        }
        dataProvider.setHistoryTimeSet(timeSet);
        SDataStack dataStack = dataProvider.initTimeLine();
        swCamLiveWheel.setDataStack(dataStack);
        final long performance = System.currentTimeMillis() - time;
        if (performance > 2 && BuildConfig.DEBUG)
            Toast.makeText(getContext(), "wowowo: " + performance, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimeUpdate(final long time) {
        if (tvCamLiveLandDatePop != null) {
            tvCamLiveLandDatePop.post(new Runnable() {
                @Override
                public void run() {
                    tvCamLiveLandDatePop.setText(simpleDateFormat.format(new Date(time)));
                }
            });
        }
    }

    @Override
    public void onSettleFinish(final long time) {
        if (tvCamLiveLandDatePop != null) {
            tvCamLiveLandDatePop.post(new Runnable() {
                @Override
                public void run() {
                    tvCamLiveLandDatePop.setText(simpleDateFormat.format(new Date(time)));
                }
            });
        }
        if (wheelUpdateListener != null)
            wheelUpdateListener.onSettleFinish();
    }

    private WheelUpdateListener wheelUpdateListener;

    public void setWheelUpdateListener(WheelUpdateListener wheelUpdateListener) {
        this.wheelUpdateListener = wheelUpdateListener;
    }

    public interface WheelUpdateListener {
        void onSettleFinish();
    }
}
