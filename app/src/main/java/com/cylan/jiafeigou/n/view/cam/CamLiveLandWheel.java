//package com.cylan.jiafeigou.n.view.cam;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//
//import com.cylan.jiafeigou.R;
//import com.cylan.jiafeigou.widget.wheel.SDataStack;
//import com.cylan.jiafeigou.widget.wheel.SuperWheel;
//import com.cylan.jiafeigou.widget.wheel.ex.IData;
//import com.cylan.jiafeigou.widget.wheel.ex.SuperWheelExt;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//
///**
// * Created by cylan-hunt on 16-7-27.
// */
//public class CamLiveLandWheel extends FrameLayout implements SuperWheelExt.WheelRollListener {
//    @BindView(R.id.tv_cam_live_land_date_pop)
//    TextView tvCamLiveLandDatePop;
//    @BindView(R.id.sw_cam_live_wheel)
//    SuperWheelExt swCamLiveWheel;
//    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M/dd HH:mm", Locale.getDefault());
//
//    public CamLiveLandWheel(Context context) {
//        this(context, null);
//    }
//
//    public CamLiveLandWheel(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public CamLiveLandWheel(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        View view = LayoutInflater.from(context).inflate(R.layout.layout_cam_land_live_wheel, this, true);
//        ButterKnife.bind(view);
//        swCamLiveWheel.setWheelRollListener(this);
//    }
//
//    public void setupHistoryData(IData dataStack) {
//        final long time = System.currentTimeMillis();
//        swCamLiveWheel.setDataProvider(dataStack);
//        Log.d("performance", "CamLivePortWheel performance: " + (System.currentTimeMillis() - time));
//    }
//
//    @Override
//    public void onTimeUpdate(final long time, int state) {
//        if (tvCamLiveLandDatePop != null) {
//            tvCamLiveLandDatePop.post(new Runnable() {
//                @Override
//                public void run() {
//                    tvCamLiveLandDatePop.setText(simpleDateFormat.format(new Date(time)));
//                }
//            });
//        }
//    }
//
//
//    private WheelUpdateListener wheelUpdateListener;
//
//    public void setWheelUpdateListener(WheelUpdateListener wheelUpdateListener) {
//        this.wheelUpdateListener = wheelUpdateListener;
//    }
//
//    public interface WheelUpdateListener {
//        void onSettleFinish();
//    }
//}
