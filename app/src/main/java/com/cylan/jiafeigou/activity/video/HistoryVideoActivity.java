package com.cylan.jiafeigou.activity.video;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.CallMessageCallBack;
import com.cylan.publicApi.Constants;
import com.cylan.support.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.HistoryVideoContainer;
import com.cylan.jiafeigou.entity.msg.MsgAudioControl;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgHistoryInfo;
import com.cylan.jiafeigou.entity.msg.MsgPush;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOffline;
import com.cylan.jiafeigou.entity.msg.MsgSyncLogout;
import com.cylan.jiafeigou.entity.msg.MsgTimeData;
import com.cylan.jiafeigou.entity.msg.req.IdHistoryReq;
import com.cylan.jiafeigou.entity.msg.req.MsgHistoryListReq;
import com.cylan.jiafeigou.entity.msg.req.MsgRelayMaskInfoReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgHistoryListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgRelayMaskInfoRsp;
import com.cylan.jiafeigou.receiver.PhoneBroadcastReceiver;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.CustomProgressView;
import com.cylan.jiafeigou.widget.SlowHorizontalScrollView;
import com.cylan.jiafeigou.widget.wheel.OnWheelChangedListener;
import com.cylan.jiafeigou.widget.wheel.WheelView;
import com.cylan.jiafeigou.widget.wheel.adapters.MyWheelViewAdapter;
import com.tencent.stat.StatService;

import org.webrtc.videoengine.ViERenderer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HistoryVideoActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener, SlowHorizontalScrollView.OnScrollChangedListener {

    private boolean isMineMsg = false;

    private static final String TAG = "HistoryVideoActivity";
    private ImageView mCacheView;
    private RelativeLayout remoteVideoLayout;
    private SurfaceView mRemoteGles20;
    private RadioGroup mDaysRadioGroup;
    private SlowHorizontalScrollView mProgressView;

    private CustomProgressView mProgressLayout;

    private LinearLayout mProgressTimeLyaout;
    private TextView mTimeView;
    private ViewSwitcher mSwitcher;
    // private RelativeLayout mPlayLayout;
    private ImageView mPlayView;
    private ProgressBar mProgressBar;
    private LinearLayout mLinearLayout;//
    private RelativeLayout mRoot;

    private long initTime;
    private MsgCidData mData;

    private Boolean isanswer = false;
    private Boolean isPause = true;

    private NotifyDialog notifyDlg;

    private Boolean isUp = false;

    private Boolean isScroll = false;

    private List<Integer> speedList;

    private static final int HISTORY_READ_FILE_OK = 0;
    private static final int HISTORY_READ_FILE_ALL = 1;
    private static final int HISTORY_READ_FILE_ERROR = 2;
    private static final int HISTORY_SDCARD_ERR = 3;

    private static final int HANDLE_CONNECT_MOVETIME = 0x02;
    private static final int HANDLE_GETLIST_MOVETIME = 0x03;
    private static final int HANDLE_PALYBTN_OVERTIME = 0x04;
    private static final int HANDLE_HISTORYVIDEO_LIST = 0X05;
    private final static int HANDLE_DISCONNECT_DEALY = 0X06;

    void onConnectError(int text, final Boolean isDis) {
        try {
            if (notifyDlg == null) {
                notifyDlg = new NotifyDialog(this);
                notifyDlg.hideNegButton();
            }

            if (notifyDlg.isShowing())
                return;

            if (findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE) {
                notifyDlg.setCancelable(false);
                notifyDlg.setCanceledOnTouchOutside(false);
            }

            notifyDlg.show(text, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.confirm: {
                            notifyDlg.dismiss();
                            if (isDis)
                                finish();
                        }
                        break;
                    }
                }
            }, null);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    void onConnectError(String text, final Boolean isDis) {
        try {
            if (notifyDlg == null) {
                notifyDlg = new NotifyDialog(this);
                notifyDlg.hideNegButton();
            }

            if (notifyDlg.isShowing())
                return;

            if (findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE) {
                notifyDlg.setCancelable(false);
                notifyDlg.setCanceledOnTouchOutside(false);
            }

            notifyDlg.show(text, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.confirm: {
                            notifyDlg.dismiss();
                            if (isDis)
                                finish();
                        }
                        break;
                    }
                }
            }, null);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
    SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.UK);
    SimpleDateFormat mShowDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.UK);
    private boolean isFirst = false;

    private int w;
    private int h;

    private Dialog mTimeDialog;

    private WheelView mDatePicker;
    private WheelView mHourPicker;
    private WheelView mMinutePicker;

    private List<HistoryVideoContainer> list = null;


    private boolean isFirstPlay = true;

    private AudioManager audioManager;

    private IncomingPhoneReceiver inComingReceiver;

    private MyWheelViewAdapter mDataAdapter;
    private MyWheelViewAdapter mHourAdapter;
    private MyWheelViewAdapter mMinuteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        audioManager = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        setContentView(R.layout.activity_histiory_video);
        mData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        initTime = getIntent().getLongExtra("time", 0);

        if (mData == null)
            finish();

        setTitle(mData.mName());

        initWidget();

        initHoursLayout();

        setBottomWidgetParams();


        MsgHistoryListReq req = new MsgHistoryListReq(mData.cid);
        JniPlay.SendBytes(req.toBytes());
        mHandler.sendEmptyMessageDelayed(HANDLE_GETLIST_MOVETIME, 30000);
        findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);


        if (mData.net == MsgCidData.CID_NET_3G || mData.net == MsgCidData.CID_NET_WIFI) {
            if (CacheUtil.readObject(CacheUtil.getCID_RELAYMASKINFO_KEY(mData.cid)) == null)
                JniPlay.SendBytes(new MsgRelayMaskInfoReq(mData.cid).toBytes());
        }

        inComingReceiver = new IncomingPhoneReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PhoneBroadcastReceiver.SEND_ACTION);
        registerReceiver(inComingReceiver, filter);

    }

    private void showGuideUI() {

        if (PreferenceUtil.getFirstHistoryVideo(this)) {
            new HistoryVideoGuideView(mRoot, this, mProgressView);
            PreferenceUtil.setFirstHistoryVideo(this, false);
        }

    }

    private void setBottomWidgetParams() {
        LayoutParams params = mLinearLayout.getLayoutParams();
        params.width = DensityUtil.getScreenWidth(this) + DensityUtil.dip2px(this, 24 * 100);
        params.height = LayoutParams.WRAP_CONTENT;
        mLinearLayout.setLayoutParams(params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatService.trackBeginPage(this, "HistoryVideo");
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatService.trackEndPage(this, "HistoryVideo");
    }

    @Override
    protected void onStop() {
        super.onStop();
        discount();
        isPause = true;
    }

    @Override
    protected void onDestroy() {
        ReleaseVideoView();
        super.onDestroy();
        audioManager.abandonAudioFocus(afChangeListener);
        unregisterReceiver(inComingReceiver);
        mCacheView.setImageResource(0);
    }

    private void initWidget() {
        mCacheView = (ImageView) findViewById(R.id.cache_page);
        remoteVideoLayout = (RelativeLayout) findViewById(R.id.remoteView);
        remoteVideoLayout.setOnClickListener(this);
        mDaysRadioGroup = (RadioGroup) findViewById(R.id.mDays);
        ImageView mCoverView = (ImageView) findViewById(R.id.cover);
        mCoverView.getBackground().setAlpha(200);

        mProgressView = (SlowHorizontalScrollView) findViewById(R.id.progress_hs);
        mProgressView.setOnScrollChangedListener(this);
        mProgressView.setOnTouchListener(new ScrollOnTouchListener());

        mProgressLayout = (CustomProgressView) findViewById(R.id.progress_layout);
        mProgressTimeLyaout = (LinearLayout) findViewById(R.id.progress_time);
        mLinearLayout = (LinearLayout) findViewById(R.id.progress);

        mTimeView = (TextView) findViewById(R.id.time);
        mTimeView.setOnClickListener(this);

        mSwitcher = (ViewSwitcher) findViewById(R.id.viewswitch);
        mPlayView = (ImageView) findViewById(R.id.btn_play);
        mPlayView.setOnClickListener(this);
        mProgressBar = (ProgressBar) findViewById(R.id.load_progress);

        mRemoteGles20 = ViERenderer.CreateRenderer(HistoryVideoActivity.this, true);
        remoteVideoLayout.addView(mRemoteGles20);

        mRoot = (RelativeLayout) findViewById(R.id.root);
    }

    private void initDaysChooser(List<HistoryVideoContainer> strs) {
        if (mDaysRadioGroup.getChildCount() > 0)
            mDaysRadioGroup.removeAllViews();
        for (int i = 0; i < strs.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.history_video_radiobutton_item, null);
            view.setId(i);
            view.setTag(strs.get(i).getmList());
            ((RadioButton) view).setText(strs.get(i).getDay());
            view.setPadding(15, 0, 15, 0);
            ((RadioButton) view).setOnCheckedChangeListener(this);
            mDaysRadioGroup.addView(view, i);
        }
        if (mDaysRadioGroup.getChildCount() > 0) {
            RadioButton rb = null;
            if (initTime == 0) {
                rb = (RadioButton) mDaysRadioGroup.getChildAt(0);
            } else {
                if (!isExistHistoryByTime(initTime)) {
                    onConnectError(R.string.RECORD_NO_FIND, false);
                }
                String day = mSimpleDateFormat.format(new Date(initTime * 1000));
                if (mDaysRadioGroup.getChildCount() > 0) {
                    for (int i = 0; i < mDaysRadioGroup.getChildCount(); i++) {
                        RadioButton mRadioButton = (RadioButton) mDaysRadioGroup.getChildAt(i);
                        if (mRadioButton.getText().toString().equals(day)) {
                            rb = mRadioButton;
                            break;
                        }

                    }
                }
            }
            if (rb != null && !rb.isChecked()) {
                rb.setChecked(true);
            } else {
                if (mDaysRadioGroup.getChildCount() > 0) {
                    ((RadioButton) mDaysRadioGroup.getChildAt(0)).setChecked(true);
                }
            }
        }
        setPlayBtnVisiblty(true);
    }

    private void initHoursLayout() {
        String[] hours = getResources().getStringArray(R.array.hours);
        for (int i = 0; i < hours.length; i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.history_video_textview_item, null);
            ((TextView) view).setText(hours[i]);
            mProgressTimeLyaout.addView(view);
        }
        TextView tv = (TextView) mProgressTimeLyaout.getChildAt(0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(DensityUtil.getScreenWidth(this) / 2, 0, 0, 0);
        tv.setLayoutParams(params);
        TextView tv1 = (TextView) mProgressTimeLyaout.getChildAt(mProgressTimeLyaout.getChildCount() - 1);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.setMargins(0, 0, DensityUtil.getScreenWidth(this) / 2, 0);
        tv1.setLayoutParams(params1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                onBackPressed();
                break;
            case R.id.time:
                if (list != null) {
                    pickupTime();
                }
                break;
            case R.id.btn_play:
                if (isPause) {
                    if (!isanswer)
                        makeCall();
                    if (mProgressLayout.getCount() > 0) {
                        setProgressBarVisiblty(false);
                    } else {
                        if (mDaysRadioGroup == null) {
                            JniPlay.DisconnectFromPeer();
                        }
                    }
                } else {
                    mHandler.removeMessages(HANDLE_CONNECT_MOVETIME);
                    mHandler.removeMessages(HANDLE_PALYBTN_OVERTIME);
                    setPlayBtnVisiblty(true);
                    JniPlay.DisconnectFromPeer();
                    isanswer = false;
                }
                isPause = !isPause;
                break;
            case R.id.confirm:
                if (mTimeDialog == null)
                    return;
                if (mDatePicker == null || mHourPicker == null || mMinutePicker == null)
                    return;
                String hour = mHourAdapter.getItemText(mHourPicker.getCurrentItem()).toString().substring(0, 2);
                String minute = mMinuteAdapter.getItemText(mMinutePicker.getCurrentItem()).toString().substring(0, 2);
                String time = mDataAdapter.getItemText(mDatePicker.getCurrentItem()).toString().substring(0, 10) + " " + hour + ":" + minute;
                try {
                    Date da = mDateFormat.parse(time);
                    mTimeView.setTag(da.getTime() / 1000);
                    mTimeView.setText(mShowDateFormat.format(da.getTime()));
                    mProgressView.scrollTo((int) getMaginLeft(da.getTime() / 1000), 0);
                    if (mDaysRadioGroup.getChildCount() > 0) {
                        String day = mSimpleDateFormat.format(da);
                        for (int i = 0; i < mDaysRadioGroup.getChildCount(); i++) {
                            RadioButton mRadioButton = (RadioButton) mDaysRadioGroup.getChildAt(i);
                            if (mRadioButton.getText().toString().equals(day)) {
                                if (!mRadioButton.isChecked())
                                    mRadioButton.setChecked(true);
                                break;
                            }
                        }
                    }
                    if (isanswer)
                        play(da.getTime() / 1000);
                } catch (ParseException e) {
                    DswLog.ex(e.toString());
                }
                isScroll = true;
                stopRemoteVideo();
                mTimeDialog.dismiss();
                if (isanswer)
                    setProgressBarVisiblty(false);
                break;
            case R.id.remoteView:

                if (mSwitcher.getVisibility() == View.GONE && isanswer) {
                    mSwitcher.setVisibility(View.VISIBLE);
                    setPlayBtnVisiblty(false);

                    mHandler.removeMessages(HANDLE_PALYBTN_OVERTIME);
                    mHandler.sendEmptyMessageDelayed(HANDLE_PALYBTN_OVERTIME, 3000);
                } else if (mSwitcher.getVisibility() == View.VISIBLE && isanswer) {
                    mSwitcher.setVisibility(View.GONE);
                    mHandler.removeMessages(HANDLE_PALYBTN_OVERTIME);
                }

                break;
            default:
                break;
        }

    }


    private void dealJson(MsgHistoryListRsp mMsgHistoryListRsp) {


        if (StringUtils.isEmptyOrNull(mMsgHistoryListRsp.toString())) {
            onConnectError(R.string.FILE_ERR, true);
            discount();
            isPause = true;
            return;
        }
        if (mMsgHistoryListRsp == null) {
            findViewById(R.id.loading_layout).setVisibility(View.GONE);
            findViewById(R.id.no_video_layout).setVisibility(View.VISIBLE);
            return;
        }
        final List<MsgTimeData> ja = mMsgHistoryListRsp.data;
        if (ja.size() == 0) {
            findViewById(R.id.loading_layout).setVisibility(View.GONE);
            findViewById(R.id.no_video_layout).setVisibility(View.VISIBLE);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                list = HistoryVideoContainer.parseJson(ja);
                Message msg = mHandler.obtainMessage();
                msg.what = HANDLE_HISTORYVIDEO_LIST;
                msg.obj = list;
                mHandler.sendMessage(msg);
            }
        }).start();

    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            List<MsgTimeData> list = (List<MsgTimeData>) buttonView.getTag();
            mProgressLayout.setList(list);
            if (isFirstPlay) {
                isFirstPlay = false;
                if (initTime != 0) {
                    if (isExistHistoryByTime(initTime)) {
                        mTimeView.setTag(initTime);
                        mProgressView.scrollBy((int) getMaginLeft(initTime), 0);
                        mTimeView.setText(mShowDateFormat.format(new Date((initTime) * 1000)));
                        return;
                    }
                }
                initTime = 0;
                if (list.size() > 0) {
                    mTimeView.setTag(list.get(0).begin);
                    mProgressView.scrollTo((int) getMaginLeft(list.get(0).begin), 0);
                    mTimeView.setText(mShowDateFormat.format(new Date((list.get(0).begin) * 1000)));
                }

            }
        }

    }

    private float getMaginLeft(long time) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.UK);

        String str = dateFormat.format(new Date(time * 1000));
        String[] s = str.split(":");
        int size = StringUtils.toInt(s[0]) * 60 + StringUtils.toInt(s[1]);
        int smoothSize = size * 60 / 36;

        return DensityUtil.dip2px(this, smoothSize);

    }

    private void makeCall() {
        mHandler.removeMessages(HANDLE_CONNECT_MOVETIME);
        overTimeHandler();
        int netType = (Utils.getNetType(this) == ConnectivityManager.TYPE_WIFI ? 1 : 0);
        MsgRelayMaskInfoRsp mMsgRelayMaskInfoRsp = (MsgRelayMaskInfoRsp) CacheUtil.readObject(CacheUtil.getCID_RELAYMASKINFO_KEY(mData.cid));
        if (mMsgRelayMaskInfoRsp != null)
            mMsgRelayMaskInfoRsp.callee = mData.cid;
        if (mMsgRelayMaskInfoRsp != null) {
            int relaymask[] = new int[mMsgRelayMaskInfoRsp.mask_list.size()];
            for (int i = 0; i < mMsgRelayMaskInfoRsp.mask_list.size(); i++) {
                relaymask[i] = mMsgRelayMaskInfoRsp.mask_list.get(i);
            }
            JniPlay.ConnectToPeer(mData.cid, true, netType, true, mData.os, relaymask, false, mData.os == Constants.OS_CAMERA_ANDROID);
        } else {
            JniPlay.ConnectToPeer(mData.cid, true, netType, true, mData.os, new int[0], false, mData.os == Constants.OS_CAMERA_ANDROID);
        }

        isMineMsg = true;
    }

    @Override
    public void handleMsg(int msg, Object obj) {
        switch (CallMessageCallBack.MSG_TO_UI.values()[msg]) {

            case CONNECT_SERVER_FAILED:
            case RESOLVE_SERVER_FAILED:
            case SERVER_DISCONNECTED: {
                if (isanswer || !isFirst) {
                    if (!isFirst)
                        isFirst = true;
                    onConnectError(R.string.GLOBAL_NO_NETWORK, findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE);
                }
                discount();
                isPause = true;
                break;
            }

            case NOTIFY_RESOLUTION:
                if (isMineMsg && AppManager.getAppManager().isActivityTop(HistoryVideoActivity.class.getName())) {

                    mHandler.removeMessages(HANDLE_CONNECT_MOVETIME);

                    isanswer = true;
                    if (mSwitcher.getVisibility() == View.VISIBLE)
                        mSwitcher.setVisibility(View.GONE);


                    JniPlay.EnableSpeaker(true);
                    JniPlay.EnableMike(false);
                    audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
                    MsgAudioControl audioCtrl = new MsgAudioControl(mData.cid);
                    audioCtrl.mSpeaker = false;
                    audioCtrl.mMike = true;
                    JniPlay.SendBytes(audioCtrl.toBytes());

                    setReslution(new String((byte[]) obj));
                    if (mSwitcher.getVisibility() == View.VISIBLE)
                        mSwitcher.setVisibility(View.GONE);
                    mCacheView.setVisibility(View.GONE);
                }
                break;

            case TRANSPORT_READY:
                if (AppManager.getAppManager().isActivityTop(HistoryVideoActivity.class.getName())) {
                    JniPlay.StartRendeRemoteView(0, mRemoteGles20);
                    play(Long.parseLong(mTimeView.getTag().toString()));
                }
                break;
            case RECV_DISCONN:
                discount();

                switch (StringUtils.toInt(new String((byte[]) obj))) {
                    case ClientConstants.CAUSE_PEERDISCONNECT:
                        onConnectError(R.string.CONN_INTERRUPTED, findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE);

                        break;
                    case ClientConstants.CAUSE_PEERNOTEXIST:
                        onConnectError(R.string.OFFLINE_ERR, findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE);
                        break;
                    case ClientConstants.CAUSE_PEERINCONNECT:
                        onConnectError(R.string.CONNECTING, findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE);
                        break;
                    case ClientConstants.CAUSE_CALLER_NOTLOGIN:
                        onConnectError(R.string.GLOBAL_NO_NETWORK, findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE);
                        break;
                    default:
                        onConnectError("(" + new String((byte[]) obj) + ")" + getString(R.string.GLOBAL_NO_NETWORK),
                                findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE);
                        break;

                }
                if (mSwitcher.getVisibility() == View.VISIBLE)
                    mSwitcher.setVisibility(View.GONE);

                if (!isPause) {
                    isPause = !isPause;
                }

                setPlayBtnVisiblty(true);

                mHandler.removeMessages(HANDLE_CONNECT_MOVETIME);

                break;

            case MSGPACK_MESSAGE:
                handleJsonMsg((MsgpackMsg.MsgHeader) obj);
                break;
            case NOTIFY_RTCP:
                if (isScroll || !isMineMsg)
                    return;
                if (!StringUtils.isEmptyOrNull(obj.toString())) {
                    String[] res = new String((byte[]) obj).split("x");
                    String temp = res[res.length - 1];
                    int ll = StringUtils.toInt(res[2]);
                    if (!temp.equals("0")) {
                        String str = mShowDateFormat.format(new Date((Long.parseLong(temp)) * 1000));
                        String day = mSimpleDateFormat.format(new Date(Long.parseLong(temp) * 1000));
                        if (mDaysRadioGroup.getChildCount() > 0) {
                            for (int i = 0; i < mDaysRadioGroup.getChildCount(); i++) {
                                RadioButton mRadioButton = (RadioButton) mDaysRadioGroup.getChildAt(i);
                                if (mRadioButton.getText().toString().equals(day)) {
                                    if (!mRadioButton.isChecked())
                                        mRadioButton.setChecked(true);
                                    break;
                                }

                            }
                        }
                        mProgressView.scrollTo((int) getMaginLeft(StringUtils.toInt(temp)), 0);
                        mTimeView.setText(str);
                        mTimeView.setTag(Long.parseLong(temp));

                    }
                    if (speedList == null)
                        speedList = new ArrayList<>();
                    if (speedList.size() < 10){
                        speedList.add(ll);
                    }else {
                        Set<Integer> set = new HashSet();
                        for (int i = 0; i < 10; i++) {
                            set.add(speedList.get(i));
                        }
                        if (set.size() == 1) {
                            DswLog.i("历史录像一直没速度，重连");
                            onConnectError(R.string.CONN_INTERRUPTED,
                                    findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE);
                        }
                        speedList.clear();
                    }
                }
                break;
            default:
                break;
        }

    }


    private void setReslution(String str) {
        if (!StringUtils.isEmptyOrNull(str)) {
            String[] res;
            if (str.contains(",")) {
                String[] org = str.split(",");
                res = org[1].split("x");
            }else {
                res = str.split("x");
            }
            android.view.ViewGroup.LayoutParams lp = findViewById(R.id.layout1).getLayoutParams();
            lp.width = w = getWindowManager().getDefaultDisplay().getWidth();
            lp.height = h = (lp.width * StringUtils.toInt(res[1])) / StringUtils.toInt(res[0]);
            findViewById(R.id.layout1).setLayoutParams(lp);
        }

    }

    @SuppressLint("NewApi")
    private void ReleaseVideoView() {
        remoteVideoLayout.removeView(mRemoteGles20);
        mRemoteGles20 = null;
    }

    private void play(long time) {
        IdHistoryReq mIdHistoryReq = new IdHistoryReq(mData.cid);
        mIdHistoryReq.time = time;
        JniPlay.SendBytes(mIdHistoryReq.toBytes());
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    private void overTimeHandler() {
        mHandler.sendEmptyMessageDelayed(HANDLE_CONNECT_MOVETIME, 30000);
    }

    private void discount() {
        JniPlay.DisconnectFromPeer();

        isanswer = false;
        setPlayBtnVisiblty(true);
        if (mCacheView.getVisibility() == View.GONE) {
            mCacheView.setVisibility(View.VISIBLE);
            android.view.ViewGroup.LayoutParams lp1 = mCacheView.getLayoutParams();
            lp1.width = w;
            lp1.height = h;
            mCacheView.setLayoutParams(lp1);
        }
        audioManager.abandonAudioFocus(afChangeListener);
    }


    public void handleJsonMsg(MsgpackMsg.MsgHeader mMsgHeader) {
        if (mMsgHeader.msgId == MsgpackMsg.CLIENT_SYNC_CIDOFFLINE) {
            MsgSyncCidOffline mMsgSyncCidOffline = (MsgSyncCidOffline) mMsgHeader;
            String cid = mMsgSyncCidOffline.cid;
            if (cid.equals(mData.cid)) {
                mHandler.removeMessages(HANDLE_CONNECT_MOVETIME);
                mHandler.removeMessages(HANDLE_GETLIST_MOVETIME);
                onConnectError(R.string.CONN_INTERRUPTED, findViewById(R.id.loading_layout).getVisibility() == View.VISIBLE);
                if (!isPause) {
                    isPause = !isPause;
                }
            }
        } else if (mMsgHeader.msgId == MsgpackMsg.CLIENT_PUSH) {
            MsgPush mMsgPush = (MsgPush) mMsgHeader;
            int pushtype = mMsgPush.push_type;
            if (pushtype == ClientConstants.PUSH_TYPE_SDCARD_OFF || pushtype == ClientConstants.PUSH_TYPE_SDCARD_ON) {
                if (mData != null) {
                    if (mData.cid.equals(mMsgPush.cid)) {
                        mData.sdcard = (pushtype == ClientConstants.PUSH_TYPE_SDCARD_OFF ? 0 : 1);
                        mData.err = mMsgPush.err;
                        if (pushtype == ClientConstants.PUSH_TYPE_SDCARD_OFF) {
                            onConnectError(R.string.SD_ERR_1, true);
                            discount();
                            isPause = true;
                        }

                    }
                }
            }
        } else if (mMsgHeader.msgId == MsgpackMsg.ID_HISTORY_LIST_RSP) {
            mHandler.removeMessages(HANDLE_GETLIST_MOVETIME);
            MsgHistoryListRsp mMsgHistoryListRsp = (MsgHistoryListRsp) mMsgHeader;
            dealJson(mMsgHistoryListRsp);
        } else if (mMsgHeader.msgId == MsgpackMsg.ID_HISTORY_INFO) {
            MsgHistoryInfo mMsgHistoryInfo = (MsgHistoryInfo) mMsgHeader;
            isPause = false;
            int error = mMsgHistoryInfo.err;
            long time = mMsgHistoryInfo.time;
            if (error == HISTORY_READ_FILE_OK) {
                isScroll = false;
                if (isanswer) {
                    if (mSwitcher.getVisibility() == View.VISIBLE)
                        mSwitcher.setVisibility(View.GONE);
                    startRemoteVideo();
                }
            } else if (error == HISTORY_READ_FILE_ERROR) {
                onConnectError(R.string.FILE_ERR, false);
                discount();
                isPause = true;
            } else if (error == HISTORY_READ_FILE_ALL) {
                onConnectError(R.string.FILE_FINISHED, false);
                discount();
                isPause = true;
            } else if (error == HISTORY_SDCARD_ERR) {
                onConnectError(R.string.SD_ERR_1, true);
                discount();
                isPause = true;
            }

        } else if (MsgpackMsg.BELL_PRESS == mMsgHeader.msgId) {
            if (!Utils.isRunOnBackground(this)) {
                mHandler.sendEmptyMessage(HANDLE_DISCONNECT_DEALY);
            }
        } else if (MsgpackMsg.ID_RELAY_MASK_INFO_RSP == mMsgHeader.msgId) {
            MsgRelayMaskInfoRsp mMsgRelayMaskInfoRsp = (MsgRelayMaskInfoRsp) mMsgHeader;
            CacheUtil.saveObject(mMsgRelayMaskInfoRsp, CacheUtil.getCID_RELAYMASKINFO_KEY(mMsgRelayMaskInfoRsp.caller));
        }else if (MsgpackMsg.CLIENT_SYNC_LOGOUT == mMsgHeader.msgId){
            MsgSyncLogout mMsgSyncLogout = (MsgSyncLogout) mMsgHeader;
            if ((mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_CHANGEPASS_REQ) || (mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_SETPASS_REQ)) {
                MyApp.showForceNotifyDialog(this, getString(R.string.PWD_CHANGED));
            } else {
                MyApp.showForceNotifyDialog(this, getString(R.string.RET_ESESSION_NOT_EXIST));
            }
        }
    }


    private void stopRemoteVideo() {
        if (isanswer) {
            StatService.trackCustomEndEvent(this, TAG, "HistoryVideo");
            if (mProgressBar.getVisibility() == View.GONE)
                mProgressBar.setVisibility(View.VISIBLE);
            JniPlay.StopRendeRemoteView();
        }
    }

    private void startRemoteVideo() {
        if (isanswer) {
            StatService.trackCustomBeginEvent(this, TAG, "HistoryVideo");
            if (mProgressBar.getVisibility() == View.VISIBLE)
                mProgressBar.setVisibility(View.GONE);
            if (mRemoteGles20 != null)
                JniPlay.StartRendeRemoteView(0, mRemoteGles20);
        }
    }

    void pickupTime() {
        try {
            if (mTimeDialog == null) {
                mTimeDialog = new Dialog(this, R.style.func_custom_dialog);
                View content = View.inflate(this, R.layout.dialog_historyvideo_time, null);
                TextView cancel = (TextView) content.findViewById(R.id.cancle);
                mDatePicker = (WheelView) content.findViewById(R.id.data);
                mDatePicker.addChangingListener(new OnWheelChangedListener() {
                    @Override
                    public void onChanged(WheelView wheel, int oldValue, int newValue) {
                        if (mDataAdapter != null) {
                            mDataAdapter.setCurrent(newValue);
                            mDataAdapter.notifyDataSetChanged();
                        }

                    }
                });

                mHourPicker = (WheelView) content.findViewById(R.id.hour);

                mHourPicker.addChangingListener(new OnWheelChangedListener() {
                    @Override
                    public void onChanged(WheelView wheel, int oldValue, int newValue) {
                        if (mHourAdapter != null) {
                            mHourAdapter.setCurrent(newValue);
                            mHourAdapter.notifyDataSetChanged();
                        }

                    }
                });

                mMinutePicker = (WheelView) content.findViewById(R.id.minute);

                mMinutePicker.addChangingListener(new OnWheelChangedListener() {
                    @Override
                    public void onChanged(WheelView wheel, int oldValue, int newValue) {
                        if (mMinuteAdapter != null) {
                            mMinuteAdapter.setCurrent(newValue);
                            mMinuteAdapter.notifyDataSetChanged();
                        }

                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mTimeDialog.dismiss();
                    }
                });

                TextView confirm = (TextView) content.findViewById(R.id.confirm);
                confirm.setOnClickListener(this);

                mTimeDialog.setContentView(content);
                mTimeDialog.setOwnerActivity(this);
                mTimeDialog.setCanceledOnTouchOutside(true);

                setDateAdapter(this, mDatePicker);
                setHourAdapter(this, mHourPicker);
                setMinuteAdapter(this, mMinutePicker);

            }

            setCurrentValue();

            mTimeDialog.show();
            WindowManager windowManager = getWindowManager();
            Display display = windowManager.getDefaultDisplay();
            WindowManager.LayoutParams lp = mTimeDialog.getWindow().getAttributes();
            lp.width = display.getWidth(); // set width
            mTimeDialog.getWindow().setAttributes(lp);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }


    private void setCurrentValue() {
        if (mDataAdapter != null) {
            String date = mSimpleDateFormat.format(new Date(Long.parseLong(mTimeView.getTag().toString()) * 1000));
            for (int j = 0; j < mDataAdapter.getItemsCount(); j++) {
                if (mDataAdapter.getItemText(j).toString().startsWith(date)) {
                    mDatePicker.setCurrentItem(j);
                    break;
                }
            }
        }
        if (mHourAdapter != null && mMinuteAdapter != null) {
            String date = new SimpleDateFormat("HH:mm", Locale.UK).format(new Date(Long.parseLong(mTimeView.getTag().toString()) * 1000));
            String[] str = date.split(":");
            for (int j = 0; j < mHourAdapter.getItemsCount(); j++) {
                if (mHourAdapter.getItemText(j).toString().substring(0, 2).equals(str[0])) {
                    mHourPicker.setCurrentItem(j);
                    break;
                }
            }

            for (int j = 0; j < mMinuteAdapter.getItemsCount(); j++) {
                if (mMinuteAdapter.getItemText(j).toString().substring(0, 2).equals(str[1])) {
                    mMinutePicker.setCurrentItem(j);
                    break;
                }
            }
        }

    }

    private void setDateAdapter(Context ctx, WheelView view) {
        try {
            String[] datas = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                Date date = mSimpleDateFormat.parse(list.get(i).getDay());
                datas[i] = (list.get(i).getDay() + " " + StringUtils.getWeekOfDate(this.getResources().getStringArray(R.array.xingqi), date));

            }
            mDataAdapter = new MyWheelViewAdapter(ctx, datas);
            view.setViewAdapter(mDataAdapter);
        } catch (ParseException e) {
            DswLog.ex(e.toString());
        }
    }

    private void setHourAdapter(Context ctx, WheelView view) {
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d", i) + getString(R.string.HOUR);
        }
        mHourAdapter = new MyWheelViewAdapter(ctx, hours);
        view.setViewAdapter(mHourAdapter);
    }

    private void setMinuteAdapter(Context ctx, WheelView view) {
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = (String.format("%02d", i) + getString(R.string.MINUTE));
        }
        mMinuteAdapter = new MyWheelViewAdapter(ctx, minutes);
        view.setViewAdapter(mMinuteAdapter);
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case HANDLE_CONNECT_MOVETIME:
                    onConnectError(R.string.NETWORK_TIMEOUT, false);
                    setPlayBtnVisiblty(true);
                    mHandler.removeMessages(HANDLE_CONNECT_MOVETIME);
                    isPause = true;
                    isanswer = false;
                    break;
                case HANDLE_GETLIST_MOVETIME:
                    onConnectError(R.string.NETWORK_TIMEOUT, true);
                    setPlayBtnVisiblty(true);
                    mHandler.removeMessages(HANDLE_GETLIST_MOVETIME);
                    isPause = true;
                    isanswer = false;
                    break;
                case HANDLE_PALYBTN_OVERTIME:
                    if (mSwitcher.getVisibility() == View.VISIBLE)
                        mSwitcher.setVisibility(View.GONE);
                    break;
                case HANDLE_HISTORYVIDEO_LIST:
                    if (msg.obj == null) {
                        findViewById(R.id.loading_layout).setVisibility(View.GONE);
                        findViewById(R.id.no_video_layout).setVisibility(View.VISIBLE);
                        return;
                    }
                    initDaysChooser((List<HistoryVideoContainer>) msg.obj);
                    findViewById(R.id.loading_layout).setVisibility(View.GONE);
                    showGuideUI();
                    break;
                case HANDLE_DISCONNECT_DEALY:
                    discount();
                    isPause = true;
                    break;
                default:
                    break;
            }

        }

    };

    /**
     * loading gone
     *
     * @param isStop
     */
    private void setProgressBarVisiblty(boolean isStop) {
        if (mSwitcher.getVisibility() == View.GONE)
            mSwitcher.setVisibility(View.VISIBLE);
        if (isStop) {
            mPlayView.setImageResource(R.drawable.bg_online_play_selector);
        } else {
            mPlayView.setImageResource(R.drawable.bg_online_pause_selector);
        }
        if (mSwitcher.getCurrentView() instanceof ImageView)
            mSwitcher.showNext();
    }

    /**
     * play btn visiblty
     *
     * @param isStop
     */
    private void setPlayBtnVisiblty(boolean isStop) {
        if (mSwitcher.getVisibility() == View.GONE)
            mSwitcher.setVisibility(View.VISIBLE);
        if (isStop) {
            mPlayView.setImageResource(R.drawable.bg_online_play_selector);
        } else {
            mPlayView.setImageResource(R.drawable.bg_online_pause_selector);
        }
        if (mSwitcher.getCurrentView() instanceof ProgressBar)
            mSwitcher.showPrevious();
    }


    @Override
    public void getScrollDistance(int distance) {
        try {
            int x = DensityUtil.px2dip(HistoryVideoActivity.this, distance);
            int mins = (int) (0.6 * x);
            if (mTimeView.getTag() == null) {
                return;
            }
            String date = mSimpleDateFormat.format(Long.parseLong(mTimeView.getTag().toString()) * 1000);
            long data_sec = mSimpleDateFormat.parse(date).getTime() / 1000;
            long all_sec = (mins * 60 > 86340 ? 86399 : mins * 60) + data_sec;
            mTimeView.setText(mShowDateFormat.format(all_sec * 1000));
            mTimeView.setTag(all_sec);
        } catch (ParseException e) {
            DswLog.ex(e.toString());
        }
    }

    public class ScrollOnTouchListener implements OnTouchListener {
        int scrollX;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                isUp = true;
            }
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isScroll = true;
                    stopRemoteVideo();
                    if (isanswer)
                        setProgressBarVisiblty(false);
                    break;
                case MotionEvent.ACTION_MOVE:
                case MotionEvent.ACTION_UP:
                    stopRemoteVideo();
                    scrollX = v.getScrollX();
                    changeTextSwicher(scrollX);
                    detectScrollX();

                    break;
            }
            return false;
        }

        public void detectScrollX() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int tempScrollX = mProgressView.getScrollX();
                    if (tempScrollX != scrollX) {
                        scrollX = tempScrollX;
                        changeTextSwicher(tempScrollX);
                    } else {
                        return;
                    }
                }
            }, 100);
        }

        public void changeTextSwicher(int scroll) {

            if (isUp) {
                isUp = false;

                if (isanswer) {
                    if (mTimeView.getText().toString().endsWith("00:00:00") && mProgressView.getScrollX() == 0) {
                        if (mSwitcher.getVisibility() == View.VISIBLE)
                            mSwitcher.setVisibility(View.GONE);
                        return;
                    }
                    play(Long.parseLong(mTimeView.getTag().toString()));
                }


            }
        }
    }

    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                audioManager.abandonAudioFocus(afChangeListener);
            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            } else if (focusChange == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            }

        }
    };

    private boolean isExistHistoryByTime(long time) {
        if (list == null)
            return false;
        if (!list.contains(new HistoryVideoContainer(mSimpleDateFormat.format(new Date(time * 1000))))) {
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            HistoryVideoContainer hvv = list.get(i);
            if (mSimpleDateFormat.format(new Date(time * 1000)).equals(hvv.getDay())) {
                for (int j = 0; j < hvv.getmList().size(); j++) {
                    MsgTimeData data = hvv.getmList().get(j);
                    if (time >= (data.begin - 60) && time <= (data.begin + data.time + 60)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private class IncomingPhoneReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String flag = intent.getStringExtra(PhoneBroadcastReceiver.TAG);
            if (flag.equals(PhoneBroadcastReceiver.CALL_STATE_RINGING)) {
                setPlayBtnVisiblty(true);
                JniPlay.DisconnectFromPeer();
                audioManager.setSpeakerphoneOn(false);
                audioManager.abandonAudioFocus(afChangeListener);
            }
        }
    }

}