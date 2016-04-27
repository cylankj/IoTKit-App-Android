package com.cylan.jiafeigou.activity.efamily.main;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.support.DswLog;
import com.cylan.jiafeigou.activity.efamily.audio.RecorderManager;
import com.cylan.jiafeigou.entity.msg.EfamlMsg;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.AudioAmplitudeView;
import com.tencent.stat.StatService;

import java.io.File;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-09
 * Time: 15:03
 */

public class BottomMenu extends RelativeLayout implements View.OnClickListener, View.OnTouchListener {


    private static final int POLL_INTERVAL = 50;
    private static final String TAG = "BottomMenu";

    private Context mContext;
    private View mMainLayout;
    private View mMenuLayout;
    private ImageView mToolsBarView;

    private View mWordLayout;
    private View mWord;
    private ImageView mStartView;
    private ImageView mBackView;
    private View mTopBar;
    private AudioAmplitudeView mAudioAmplitudeView1;
    private AudioAmplitudeView mAudioAmplitudeView2;
    private Chronometer mTimelengthView;
    private TextView mInfoView;

    private TextView mWordView;
    private TextView mSharePIcView;
    private TextView mFacetimeView;

    //    private TimeMeter mRecordTime;
    private RecorderManager mRecorderManager;
    private BottomMenuListener mListener;

    private String cid;
    private String tmpFilePath;

    private boolean isVibrator;

    private Runnable mRecordWorker = null;

    private BottomMenuOnTouch bottomMenuOnTouch;


    public BottomMenu(Context context) {
        super(context);
        this.mContext = context;
    }

    public BottomMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView();
    }

    private void initView() {
        mMainLayout = findViewById(R.id.main_layout);
        mWordLayout = findViewById(R.id.word_layout);
        mMenuLayout = findViewById(R.id.menu);
        mWordLayout.setVisibility(View.GONE);
        mWordView = (TextView) findViewById(R.id.word);
        mWordView.setOnClickListener(this);
        mSharePIcView = (TextView) findViewById(R.id.share_pic);
        mSharePIcView.setOnClickListener(this);
        mFacetimeView = (TextView) findViewById(R.id.facetime);
        mFacetimeView.setOnClickListener(this);
        mStartView = (ImageView) findViewById(R.id.start_word);
        mStartView.setOnTouchListener(this);
        mToolsBarView = (ImageView) findViewById(R.id.toolsbar);
        mToolsBarView.setOnClickListener(this);
        mBackView = (ImageView) findViewById(R.id.back);
        mBackView.setOnClickListener(this);
        mWord = findViewById(R.id.word_pressed);
        mTopBar = findViewById(R.id.topbar);
        mTopBar.setOnClickListener(this);
        mAudioAmplitudeView1 = (AudioAmplitudeView) findViewById(R.id.amplitude1);
        mAudioAmplitudeView2 = (AudioAmplitudeView) findViewById(R.id.amplitude2);
        mTimelengthView = (Chronometer) findViewById(R.id.timelength);
        mInfoView = (TextView) findViewById(R.id.info);
        initRecordBtn();

    }


    public void dismiss() {
        if (mMainLayout.getVisibility() == View.VISIBLE) {
            if (mMenuLayout.getVisibility() == View.VISIBLE) {
                mMenuLayout.setVisibility(View.GONE);
                findViewById(R.id.obligate_view).setVisibility(GONE);
                mToolsBarView.setImageResource(R.drawable.bg_efamily_toolbar);
                if (mListener != null) mListener.dismiss();
            }
        } else {
            if (mWord.getVisibility() == View.VISIBLE) {
                mWord.setVisibility(View.GONE);
                if (mListener != null) mListener.dismiss();
                mWordLayout.setVisibility(View.GONE);
                mMenuLayout.setVisibility(View.GONE);
                mMainLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public void show() {
        if (mMainLayout.getVisibility() == View.VISIBLE) {
            if (mMenuLayout.getVisibility() == View.GONE) {
                mMenuLayout.setVisibility(View.VISIBLE);
                findViewById(R.id.obligate_view).setVisibility(VISIBLE);
                mToolsBarView.setImageResource(R.drawable.bg_efamily_toolbar_down);
                if (mListener != null) mListener.show();
            }
        } else {
            if (mWord.getVisibility() == View.GONE) {
                mWord.setVisibility(View.VISIBLE);
                if (mListener != null) mListener.show();
            }
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.toolsbar) {
            if (mMenuLayout.getVisibility() == View.VISIBLE) {
                mMenuLayout.setVisibility(View.GONE);
                findViewById(R.id.obligate_view).setVisibility(GONE);
                mToolsBarView.setImageResource(R.drawable.bg_efamily_toolbar);
            } else {
                mMenuLayout.setVisibility(View.VISIBLE);
                findViewById(R.id.obligate_view).setVisibility(VISIBLE);
                mToolsBarView.setImageResource(R.drawable.bg_efamily_toolbar_down);
                if (mListener != null) mListener.show();
            }
        } else if (id == R.id.word) {
            mMainLayout.setVisibility(View.GONE);
            findViewById(R.id.obligate_view).setVisibility(GONE);
            mWordLayout.setVisibility(View.VISIBLE);
            mWord.setVisibility(View.VISIBLE);
            StatService.trackCustomEvent(mContext, TAG, mContext.getString(R.string.EFAMILY_MENU_VOICEMS));
        } else if (id == R.id.back) {
            mMainLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.obligate_view).setVisibility(VISIBLE);
            mToolsBarView.setImageResource(R.drawable.bg_efamily_toolbar_down);
            mWordLayout.setVisibility(View.GONE);
        } else if (id == R.id.topbar) {
            if (mWord.getVisibility() == View.VISIBLE) {
                mWord.setVisibility(View.GONE);
                mWordLayout.setVisibility(View.GONE);
                mMenuLayout.setVisibility(View.GONE);
                mMainLayout.setVisibility(View.VISIBLE);
            } else {
                mWord.setVisibility(View.VISIBLE);
                if (mListener != null) mListener.show();
            }
        } else if (id == R.id.start_word) {

        } else if (id == R.id.share_pic) {
            ToastUtil.showToast(mContext, mContext.getString(R.string.EXPECT));
            StatService.trackCustomEvent(mContext, TAG, mContext.getString(R.string.EFAMILY_MENU_PHOTO));
        }
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mStartView.setEnabled(true);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
        }
        return super.onInterceptTouchEvent(event);

    }


    @Override
    public boolean onTouch(View v, final MotionEvent event) {
        if (!Environment.getExternalStorageDirectory().exists()) {
            ToastUtil.showFailToast(mContext, mContext.getString(R.string.has_not_sdcard));
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mRecorderManager = new RecorderManager();
            if (mRecordWorker == null) {
                mRecordWorker = getRecordWorker(event);
                mHandler.postDelayed(mRecordWorker, 250);
            }
            setClickEnable(false);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            mStartView.setEnabled(true);
            setClickEnable(true);
            mRecorderManager.stop();
            mHandler.removeCallbacksAndMessages(null);
            mRecordWorker = null;
            if (event.getY() < 0 && Math.abs(event.getY()) > 50) {
                if (tmpFilePath != null) {
                    File file = new File(tmpFilePath);
                    if (file.exists()) {
                        file.delete();
                    }
                }
            } else {
                int sec = Integer.parseInt(mTimelengthView.getText().toString().split(":")[1]);
                sendWord(sec);
            }
            invalidateUI(event);
            mTimelengthView.stop();
            mTimelengthView.setBase(SystemClock.elapsedRealtime());
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            invalidateUI(event);
        }
        if (bottomMenuOnTouch != null)
            bottomMenuOnTouch.onTouch(event);

        return true;
    }


    private void invalidateUI(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInfoView.setText(R.string.EFAMILY_CANCEL_RECORD);
                mInfoView.setTextColor(mContext.getResources().getColor(R.color.efamily_bottom_text_color));
                mAudioAmplitudeView1.setVisibility(View.VISIBLE);
                mAudioAmplitudeView2.setVisibility(View.VISIBLE);
                mTimelengthView.setText("00:00");
                mStartView.setImageResource(R.drawable.ico_efamily_word_pressed);
                break;
            case MotionEvent.ACTION_UP:
                initRecordBtn();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getY() < 0 && Math.abs(event.getY()) > 50) {
                    mInfoView.setText(R.string.EFAMILY_CANCEL_SEND);
                    mInfoView.setTextColor(mContext.getResources().getColor(R.color.efamily_bottom_text_red_color));
                    mAudioAmplitudeView1.setVisibility(View.VISIBLE);
                    mAudioAmplitudeView2.setVisibility(View.VISIBLE);
                    mStartView.setImageResource(R.drawable.ico_efamily_word_back);
                } else {
                    if (event.getX() > 0 && event.getX() < mStartView.getWidth() && event.getY() > 0 && event.getY() < mStartView.getHeight()) {
                        return;
                    }
                    mInfoView.setText(R.string.EFAMILY_CANCEL_RECORD);
                    mInfoView.setTextColor(mContext.getResources().getColor(R.color.efamily_bottom_text_color));
                    mAudioAmplitudeView1.setVisibility(View.VISIBLE);
                    mAudioAmplitudeView2.setVisibility(View.VISIBLE);
                    mStartView.setImageResource(R.drawable.ico_efamily_word_pressed);


                }
                break;
        }

    }

    public void setOnBommtomMenuListener(BottomMenuListener listener) {
        this.mListener = listener;
    }

    private void initRecordBtn() {
        mInfoView.setText(R.string.EFAMILY_PUSH_TO_LEAVE_MSG);
        mInfoView.setTextColor(mContext.getResources().getColor(R.color.efamily_bottom_text_color));
        mAudioAmplitudeView1.setVisibility(View.GONE);
        mAudioAmplitudeView2.setVisibility(View.GONE);
        mTimelengthView.setText("00:00");
        mStartView.setImageResource(R.drawable.ico_efamily_word);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };


    private Runnable mPollTask = new Runnable() {
        public void run() {
            int amp = (int) (mRecorderManager.getAmplitude());
            mAudioAmplitudeView1.setAmplitude(amp);
            mAudioAmplitudeView2.setAmplitude(amp);
            mHandler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };


    public void setCid(String cid) {
        this.cid = cid;
    }


    private Runnable getRecordWorker(final MotionEvent mEvent) {
        return new Runnable() {
            @Override
            public void run() {
                mTimelengthView.setBase(SystemClock.elapsedRealtime());
                mTimelengthView.start();
                mTimelengthView.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        try {
                            int sec = getSecByTime(mTimelengthView.getText().toString());
                            if (sec >= 55) {
                                if (!isVibrator) {
                                    isVibrator = true;
                                    Vibrator vib = Utils.getVibrator(mContext);
                                    vib.vibrate(1000);
                                }
                                if (sec > 60) {
                                    isVibrator = false;
                                    mTimelengthView.stop();
                                    mRecorderManager.stop();
                                    mStartView.setEnabled(false);
                                    initRecordBtn();
                                    sendWord(RecorderManager.getWordsDuration(tmpFilePath));
                                    setClickEnable(true);
                                    if (bottomMenuOnTouch != null)
                                        bottomMenuOnTouch.overSixtySec();
                                } else {
                                    mInfoView.setText(String.valueOf(60 - sec));
                                    mInfoView.setTextColor(mContext.getResources().getColor(R.color.efamily_bottom_text_red_color));
                                    mAudioAmplitudeView1.setVisibility(View.VISIBLE);
                                    mAudioAmplitudeView2.setVisibility(View.VISIBLE);
                                    mStartView.setImageResource(R.drawable.ico_efamily_word_back);
                                }
                            }
                        } catch (Exception e) {
                            DswLog.ex(e.toString());
                        }

                    }
                });
                invalidateUI(mEvent);
                tmpFilePath = PathGetter.getRecordAudioPath(mContext, cid, ""
                        + (System.currentTimeMillis() / 1000 + PreferenceUtil.getKeyNtpTimeDiff(mContext)));
                mRecorderManager.start(tmpFilePath);
                mHandler.postDelayed(mPollTask, POLL_INTERVAL);
            }
        };

    }

    private void sendWord(int sec) {
        if (tmpFilePath != null) {
            File file = new File(tmpFilePath);
            if (file.exists()) {

                Log.i("Bottom", "sec-->" + sec);
                if (mRecorderManager != null && sec < 2) {
                    file.delete();
                } else {
                    EfamlMsg bean = new EfamlMsg();
                    bean.isRead = 0;
                    bean.timeDuration = sec;
                    bean.timeBegin = Long.parseLong(new File(tmpFilePath).getName().replaceAll(PathGetter.FILE_SUFFIX, ""));
                    bean.msgType = EfamlMsg.MSG_WORD;
                    bean.isPlay = false;
                    bean.url = null;
                    bean.isRead = 0;
                    bean.send_state = EfamlMsg.SENDING;
                    if (mListener != null&&bean.timeDuration!=0) mListener.sendword(bean);
                }
            }
            tmpFilePath = null;
        }
    }

    private int getSecByTime(String time) {
        int min = Integer.parseInt(time.split(":")[0]);
        int sec = Integer.parseInt(time.split(":")[1]);
        return min * 60 + sec;
    }

    private void setClickEnable(boolean isEnable){
        mTopBar.setClickable(isEnable);
        mBackView.setClickable(isEnable);
    }

    public interface BottomMenuOnTouch{
        void onTouch(MotionEvent event);
        void overSixtySec();
    }

    public void setOnBottomMenuOnTouchListener(BottomMenuOnTouch onTouch){
        bottomMenuOnTouch = onTouch;
    }
}