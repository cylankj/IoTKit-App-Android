
package com.cylan.jiafeigou.activity.main;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.publicApi.Constants;
import com.cylan.support.DswLog;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.doorbell.DoorBellActivity;
import com.cylan.jiafeigou.activity.doorbell.addDoorbell.AddDoorBellActivity;
import com.cylan.jiafeigou.activity.doorbell.detail.DoorBellDetailActivity;
import com.cylan.jiafeigou.activity.efamily.CaptureActivity;
import com.cylan.jiafeigou.activity.efamily.EFamilySettingActivity;
import com.cylan.jiafeigou.activity.efamily.magnetic.MagneticActivity;
import com.cylan.jiafeigou.activity.efamily.main.EfamilyMainActivity;
import com.cylan.jiafeigou.activity.message.MessageActivity;
import com.cylan.jiafeigou.activity.video.CallOrConf;
import com.cylan.jiafeigou.activity.video.addDevice.AddVideoActivity;
import com.cylan.jiafeigou.activity.video.setting.DeviceSettingActivity;
import com.cylan.jiafeigou.adapter.CidDataAdapter;
import com.cylan.jiafeigou.adapter.SceneAdapter;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.base.RootActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.RequestMessage;
import com.cylan.jiafeigou.entity.msg.AccountInfo;
import com.cylan.jiafeigou.entity.msg.HttpResult;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgCidGetSetParent;
import com.cylan.jiafeigou.entity.msg.MsgClientPushSimpleNotice;
import com.cylan.jiafeigou.entity.msg.MsgPush;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOffline;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOnline;
import com.cylan.jiafeigou.entity.msg.MsgSyncLogout;
import com.cylan.jiafeigou.entity.msg.MsgSyncSdcard;
import com.cylan.jiafeigou.entity.msg.MsgSyncUrl;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgCidlistReq;
import com.cylan.jiafeigou.entity.msg.req.MsgDeleteSceneReq;
import com.cylan.jiafeigou.entity.msg.req.MsgEnableSceneReq;
import com.cylan.jiafeigou.entity.msg.req.MsgGetAccountinfoReq;
import com.cylan.jiafeigou.entity.msg.rsp.LoginRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidSdcardFormatRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgEnableSceneRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgRelayMaskInfoRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgSetCidAliasRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgUnbindCidRsp;
import com.cylan.jiafeigou.listener.ActivityIsResume;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ThreadPoolUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.UpdateManager;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.AddDeviceLayout;
import com.cylan.jiafeigou.widget.BadgeView;
import com.cylan.jiafeigou.widget.MyGridView;
import com.cylan.jiafeigou.widget.RefreshListView;
import com.cylan.jiafeigou.widget.slidingmenu.SlidingMenu;
import com.cylan.jiafeigou.worker.SaveMenuBackgroundRunnable;
import com.cylan.jiafeigou.worker.SaveTitlebarRunnable;
import com.cylan.jiafeigou.worker.UploadLogWorker;
import support.uil.core.ImageLoader;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyVideos extends RootActivity implements OnClickListener, RefreshListView.OnRefreshListener, AdapterView.OnItemClickListener,
        OnItemLongClickListener, ActivityIsResume {

    private static final int TCP_GET_LIST = 0x01;
    private static final int TCP_DELETE_SCENC = 0x02;

    private static final String MTATAG = "MainActivity";

    // handler
    public static final int HANDLER_MSG_TOAST_GONE = 0x01;
    public static final int HANDLER_TO_SAVE_SCENE_COVER = 0x2;
    public static final int HANDLER_TO_SET_THEME = 0x03;
    public static final int HANDLER_HTTP_GET_LIST = 0x04;
    public static final int HANDLER_CLICK_BACK = 0x05;
    public static final int HANDLER_HOMECOVER_COMPLETE = 0x06;
    public static final int HANDLER_TO_SET_DEVICE = 0x07;

    // onActivityResult
    public static final int RESULT_TO_SET_COVER = 0x01;
    public static final int RESULT_TO_ADD_SCENE = 0x02;
    public static final int RESULT_TO_ADD_IHOME_EXCEPTION = 0x03;

    public static final int DEFAULT_NULL_PIC_INDEX = -1;
    public static final int DEFAULT_PIC_INDEX = 1;
    public static final int DEFAULT_MAX_PIC_INDEX = 10;
    public static final int DEFAULT_MIN_PIC_INDEX = 1;

    private RefreshListView mListView;
    private CidDataAdapter mCidDataAdapter;
    private SceneAdapter mSceneAdapter;
    private ImageView mTopPicLayout;
    private TextView mInfoView;
    private MyGridView mSceneGrid;
    private SlidingMenu mSlidingMenu;
    private TextView mMenuView;
    private ImageView mMsgView;
    private ViewGroup mSlidingMenuView;
    private TextView mDateView;
    private TextView mModeView;
    private AddDeviceLayout mAddBtn;

    private NotifyDialog notifyDlg;

    private int mCurrentTheme = -1;
    private int vid;
    private int lastVid;
    private int imageid = -1;
    private int sceneid = -1;

    private boolean isClickExit;

    private HomeMenuDialog mMenuDialog;

    private AddDeviceDialog mAdialog;

    private UpdateManager mUpdateManager;

    private LinearLayout mSceneView;

    private BadgeView badge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_layout);
        initView();
        initData();
        registerReceiver();
    }

    private void initView() {
        initSlidingMenu();
        mTopPicLayout = (ImageView) findViewById(R.id.top_pic_layout);
        mDateView = (TextView) findViewById(R.id.top_date);
        mModeView = (TextView) findViewById(R.id.top_model);
        mMenuView = (TextView) findViewById(R.id.top_menu);
        mMsgView = (ImageView) findViewById(R.id.top_msg);
        mInfoView = (TextView) findViewById(R.id.notify_text);
        mAddBtn = (AddDeviceLayout) findViewById(R.id.layout_adddevice);
        mListView = (RefreshListView) findViewById(R.id.video_list);
        mMenuView.setOnClickListener(this);
        mMsgView.setOnClickListener(this);
        mTopPicLayout.setOnClickListener(this);
        mSceneGrid.setOnItemClickListener(this);
        mAddBtn.setOnClickListener(this);
        mListView.setOnRefreshListener(this);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mListView.setOverTime(true);
        initBadgeView();
        mCidDataAdapter = new CidDataAdapter(this, new ArrayList<MsgCidData>());
        mSceneAdapter = new SceneAdapter(this, new ArrayList<MsgSceneData>());
        int mCoverIndex = PreferenceUtil.getHomeCover(this);
        if (mCoverIndex == DEFAULT_NULL_PIC_INDEX) {
            PreferenceUtil.setHomeCover(this, DEFAULT_PIC_INDEX);
            mTopPicLayout.setImageResource(ClientConstants.home_covers[0]);
        } else if (mCoverIndex <= DEFAULT_MAX_PIC_INDEX && mCoverIndex >= DEFAULT_MIN_PIC_INDEX) {
            mTopPicLayout.setImageResource(ClientConstants.home_covers[mCoverIndex - 1]);
        }
        clearScenePic();

        mUpdateManager = new UpdateManager(this);
    }

    private void initSlidingMenu() {
        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setBehindOffsetRes(R.dimen.x110);
        mSlidingMenu.setFadeDegree(0.6f);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        mSlidingMenuView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.menu_layout, null);
        mSlidingMenu.setMenu(mSlidingMenuView);

        ImageView img = (ImageView) mSlidingMenuView.findViewById(R.id.menu_shade);
        Drawable image = img.getDrawable();
        image.mutate().setAlpha(100);
        img.setImageDrawable(image);

        mSceneView = (LinearLayout) mSlidingMenuView.findViewById(R.id.scenc);
        mSceneView.setOnClickListener(this);
        TextView mAccountView = (TextView) mSlidingMenuView.findViewById(R.id.account);
        mAccountView.setOnClickListener(this);
        TextView mSettingView = (TextView) mSlidingMenuView.findViewById(R.id.setting);
        mSettingView.setOnClickListener(this);
        TextView mFeedbackView = (TextView) mSlidingMenuView.findViewById(R.id.feedback);
        mFeedbackView.setOnClickListener(this);
        TextView mHelpView = (TextView) mSlidingMenuView.findViewById(R.id.web_help);
        mHelpView.setOnClickListener(this);

        mSceneGrid = (MyGridView) mSlidingMenuView.findViewById(R.id.scene_grid);
    }

    private void initBadgeView() {
        badge = new BadgeView(this, mMsgView);
        badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        badge.setWidthAndHeight(DensityUtil.dip2px(this, 8), DensityUtil.dip2px(this, 8));
        badge.setBadgeMargin(DensityUtil.dip2px(this, 15), DensityUtil.dip2px(this, 13));
    }

    private void initData() {
        if (PreferenceUtil.getIsFirstInHome(this)) {
            PreferenceUtil.setIsFirstInHome(this);
            mAddBtn.startCircle();
        }
        if (Utils.isNetworkConnected(this))
            mUpdateManager.checkAppUpdate(false);
        if (!PreferenceUtil.getIsSafeLogin(this) && !PreferenceUtil.getIsOtherLoginType(this)) {
            showSafeSubmitData();
        }
        onRefresh();
        getCacheListDate();
        clearRelayMaskInfo();
        getSettingData();
    }

    private void getSettingData() {
        MsgGetAccountinfoReq msgGetAccountinfoReq = new MsgGetAccountinfoReq();
        MyApp.wsRequest(msgGetAccountinfoReq.toBytes());
        DswLog.i("send MsgGetAccountinfoReq from MyVideos-->" + msgGetAccountinfoReq.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDate();
        if (PreferenceUtil.getKeyMsgCount(this) > 0) {
            PreferenceUtil.setKeyMsgCount(this, 0);
            badge.show();
        }
    }

    private void setDate() {
        try {
            final int lType = Utils.getLanguageType(getApplication());
            String timePatterns = lType == Constants.LANGUAGE_TYPE_CHINESE ? "M月d日" : "MMMM d";
            Format dateFormat = new SimpleDateFormat(timePatterns, Locale.getDefault());
            String date = dateFormat.format(Calendar.getInstance().getTime());

//            Calendar cale = Calendar.getInstance();
//            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d", Locale.ENGLISH);
            String days = StringUtils.getWeekOfDate(this.getResources().getStringArray(R.array.xingqi),
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(new SimpleDateFormat(
                            "yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime())));
            mDateView.setText(days + " " + date);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.adddevice:
                mAddBtn.stopCircle();
                if (mAdialog == null) {
                    mAdialog = new AddDeviceDialog(MyVideos.this);
                    mAdialog.setAddCameraListener(MyVideos.this);
                    mAdialog.setAddDoorBellListener(MyVideos.this);
                    mAdialog.setmAddEfamilyListener(MyVideos.this);
                }
                if (!mAdialog.isShowing())
                    mAdialog.showMyDialog();
                break;
            case R.id.scenc:
                if (mSceneGrid.getVisibility() == View.VISIBLE) {
                    mSceneGrid.setVisibility(View.GONE);
                    ((ImageView) mSlidingMenuView.findViewById(R.id.arrows)).setImageResource(R.drawable.menu_unexpand_arrow);
                } else {
                    mSceneGrid.setVisibility(View.VISIBLE);
                    ((ImageView) mSlidingMenuView.findViewById(R.id.arrows)).setImageResource(R.drawable.menu_expand_arrow);
                }
                break;
            case R.id.top_menu:
                mSlidingMenu.toggle(true);
                if (mSlidingMenu.isMenuShowing()) {
                    mMenuView.requestFocus();
                } else {
                    mMenuView.clearFocus();
                }
                break;
            case R.id.account:
                startActivity(new Intent(this, MyAccount.class).putExtra(ClientConstants.ACCOUNT_VID, vid));
                break;
            case R.id.setting:
                startActivity(new Intent(MyVideos.this, Setting.class).putExtra(ClientConstants.ACCOUNT_VID, vid));
                break;
            case R.id.feedback:
                startActivity(new Intent(MyVideos.this, Feedback.class));
                break;
            case R.id.top_pic_layout:
                if (!MyApp.getIsLogin() || mCurrentTheme == -1)
                    return;
                startActivityForResult(new Intent(MyVideos.this, EditSceneActivity.class).putExtra(ClientConstants.SCENCINFO, mSceneAdapter.getItem(mCurrentTheme)).putExtra("CurrentTheme", mCurrentTheme)
                        .putExtra("flag", EditSceneActivity.FLAG_MODIFY).putExtra("scenc_count", mSceneAdapter.getCount()), RESULT_TO_SET_COVER);
                break;
            case R.id.top_msg:
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.MESSAGE));
                badge.hide();
                startActivity(new Intent(MyVideos.this, MessageActivity.class));
                break;
            case R.id.notify_text:
                if (mInfoView.getVisibility() == View.VISIBLE)
                    mInfoView.setVisibility(View.GONE);
                startActivity(new Intent(this, MyAccount.class).putExtra(ClientConstants.ACCOUNT_VID, vid));
                break;
            case R.id.video:
                if (ActivityIsResumeManager.getActivityIsResumeListener() == null)
                    ActivityIsResumeManager.setActivityIsResumeListener(MyVideos.this);
                startActivity(new Intent(this, AddVideoActivity.class));
                break;
            case R.id.doorbell:
                if (ActivityIsResumeManager.getActivityIsResumeListener() == null)
                    ActivityIsResumeManager.setActivityIsResumeListener(MyVideos.this);
                startActivity(new Intent(this, AddDoorBellActivity.class));
                break;
            case R.id.efamily:
                if (ActivityIsResumeManager.getActivityIsResumeListener() == null)
                    ActivityIsResumeManager.setActivityIsResumeListener(MyVideos.this);
                startActivityForResult(new Intent(this, CaptureActivity.class), RESULT_TO_ADD_IHOME_EXCEPTION);
                break;
            case R.id.web_help:
                startActivity(new Intent(this, Help.class));
                break;
        }
    }


    private void Request(int flag) {
        switch (flag) {
            case TCP_GET_LIST:
                MsgCidlistReq mMsgCidlistReq = new MsgCidlistReq(PreferenceUtil.getSessionId(this));
                //检测登陆不成功的方式,需要重构.
                if (!MyApp.wsRequest(mMsgCidlistReq.toBytes())) {
                    getWindow().getDecorView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mListView.onRefreshComplete();
                        }
                    }, 200);
                }
                break;
            case TCP_DELETE_SCENC:
                MsgDeleteSceneReq mMsgDeleteSceneReq = new MsgDeleteSceneReq(PreferenceUtil.getBindingPhone(this));
                mMsgDeleteSceneReq.scene_id = mSceneAdapter.getItem(mCurrentTheme).scene_id;
                MyApp.wsRequest(mMsgDeleteSceneReq.toBytes());
                break;
            default:
                break;
        }
    }


    private void request_success(MsgCidlistRsp rsp) {
        MsgCidlistRsp.getInstance().setCidList(rsp);
        mCidDataAdapter.clear();
        mSceneAdapter.clear();
        vid = rsp.vid;
        mSceneAdapter.addAll(rsp.data);
        if (rsp.getEnableMsgSceneData() != null)
            mCidDataAdapter.addAll(rsp.getEnableMsgSceneData().data);
        mCurrentTheme = rsp.getEnableSceneIndex();
    }

    void httpGetCidList() {
        mProgressDialog.showDialog(R.string.LOADING);
        Request(TCP_GET_LIST);
    }

    private void updateView() {
        if (mCidDataAdapter.isEmpty()) {
            mListView.setVisibility(View.GONE);
        } else {
            mListView.onRefreshComplete();
            mListView.setVisibility(View.VISIBLE);
            mListView.setAdapter(mCidDataAdapter);
        }
        View v = findViewById(R.id.no_video_layout);
        v.setVisibility(mCidDataAdapter.getCount() == 0 ? View.VISIBLE : View.GONE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.d("MyVideos destroy");
        mSceneAdapter = null;
        mCidDataAdapter.clear();
        mHandler.removeCallbacksAndMessages(null);
        mProgressDialog.dismissDialog();
        unregisterReceiver(receiver);
        ActivityIsResumeManager.setActivityIsResumeListener(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == RESULT_TO_SET_COVER || requestCode == RESULT_TO_ADD_SCENE) {
            if (requestCode == RESULT_TO_SET_COVER)
                ToastUtil.showSuccessToast(this, getString(R.string.PWD_OK_2));
        } else if (requestCode == RESULT_TO_ADD_IHOME_EXCEPTION) {
            if (data == null)
                showErrorDialog();
            else {
                if ((data.getFlags() == 0)) {
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        Request(TCP_GET_LIST);
    }


    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
            notifyDlg.hideNegButton();
        }
        if (error == 22) {
            notifyDlg.setCancelable(false);
        } else {
            notifyDlg.setCancelable(true);
        }
        if (!notifyDlg.isShowing())
            notifyDlg.show(msg, error);
    }

    public void onError(String msg, int ret) {
        mProgressDialog.dismissDialog();
        mListView.onRefreshComplete();
        showNotify(msg, ret);
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        if (msgpackMsg.msgId == MsgpackMsg.CLIENT_PUSH) {
            MsgPush mMsgPush = (MsgPush) msgpackMsg;
            handlePush(mMsgPush.push_type, mMsgPush);
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SYNC_URL) {
            MsgSyncUrl mMsgSyncUrl = (MsgSyncUrl) msgpackMsg;
            String url = mMsgSyncUrl.url;
            MyApp.setUpdateUrl(MyVideos.this, url);
            if (Utils.isNetworkConnected(this))
                mUpdateManager.checkAppUpdate(false);
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SYNC_SDCARD) {
            MsgSyncSdcard mMsgPush = (MsgSyncSdcard) msgpackMsg;
            if (mCidDataAdapter != null) {
                for (int j = 0, count = mCidDataAdapter.getCount(); j < count; j++) {
                    if (mCidDataAdapter.getItem(j).cid.equals(mMsgPush.caller)) {
                        mCidDataAdapter.getItem(j).sdcard = mMsgPush.sdcard;
                        mCidDataAdapter.getItem(j).err = mMsgPush.err;
                        break;
                    }
                }
                mCidDataAdapter.notifyDataSetChanged();
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SYNC_CIDONLINE) {
            MsgSyncCidOnline mMsgSyncCidOnline = (MsgSyncCidOnline) msgpackMsg;
            String cid = mMsgSyncCidOnline.cid;
            int net = mMsgSyncCidOnline.net;
            if (mCidDataAdapter != null) {
                for (int j = 0, count = mCidDataAdapter.getCount(); j < count; j++) {
                    if (mCidDataAdapter.getItem(j).cid.equals(cid)) {
                        mCidDataAdapter.getItem(j).net = net;
                        mCidDataAdapter.getItem(j).name = mMsgSyncCidOnline.name;
                        mCidDataAdapter.getItem(j).version = mMsgSyncCidOnline.version;
                        break;
                    }
                }
                mCidDataAdapter.notifyDataSetChanged();
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SYNC_CIDOFFLINE) {

            MsgSyncCidOffline mMsgSyncCidOffline = (MsgSyncCidOffline) msgpackMsg;
            String cid = mMsgSyncCidOffline.cid;
            CacheUtil.remove(CacheUtil.getCID_RELAYMASKINFO_KEY(cid));
            if (mCidDataAdapter != null) {
                for (int j = 0, count = mCidDataAdapter.getCount(); j < count; j++) {
                    if (mCidDataAdapter.getItem(j).cid.equals(cid)) {
                        mCidDataAdapter.getItem(j).net = 0;
                        mCidDataAdapter.getItem(j).name = "";
                        mCidDataAdapter.getItem(j).version = "";
                        break;
                    }
                }
                mCidDataAdapter.notifyDataSetChanged();
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_LOGIN_RSP) {
            mListView.onRefreshComplete();
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (mRspMsgHeader.ret == 11 || mRspMsgHeader.ret == 22 || mRspMsgHeader.ret == 33) {
                MyApp.logout(MyVideos.this);
                MyApp.showForceNotifyDialog(MyVideos.this, mRspMsgHeader.msg);
            } else if (mRspMsgHeader.ret == Constants.RETOK) {
                LoginRsp mCilentLoginRsp = (LoginRsp) mRspMsgHeader;
                if (mCilentLoginRsp.msg_count > 0) {
                    badge.show(true);
                }
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_RELOGIN_RSP) {
            mListView.onRefreshComplete();
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (mRspMsgHeader.ret == 11 || mRspMsgHeader.ret == 22 || mRspMsgHeader.ret == 33) {
                MyApp.logout(MyVideos.this);
                MyApp.showForceNotifyDialog(MyVideos.this, mRspMsgHeader.msg);
            } else if (mRspMsgHeader.ret == Constants.RETOK) {
                LoginRsp rsp = (LoginRsp) mRspMsgHeader;
                if (rsp.msg_count > 0) {
                    badge.show(true);
                }
                onRefresh();
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SETCIDALIAS_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                MsgSetCidAliasRsp mMsgSetCidAliasRsp = (MsgSetCidAliasRsp) mRspMsgHeader;
                String cid = mMsgSetCidAliasRsp.cid;
                String alias = mMsgSetCidAliasRsp.alias;
                if (mSceneAdapter != null) {
                    List<MsgCidData> l;
                    for (int j = 0, parentCount = mSceneAdapter.getCount() - 1; j < parentCount; j++) {
                        l = mSceneAdapter.getItem(j).data;
                        for (int k = 0, size = l.size(); k < size; k++) {
                            if (l.get(k).cid.equals(cid)) {
                                l.get(k).alias = alias;
                                if (mSceneAdapter.getItem(j).enable == ClientConstants.ENABLE_SCENE) {
                                    mCidDataAdapter.getItem(k).alias = alias;
                                }
                                break;
                            }
                        }
                    }
                }
                mCidDataAdapter.notifyDataSetChanged();
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_CIDLIST_RSP) {
            mProgressDialog.dismissDialog();
            mListView.onRefreshComplete();
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                MsgCidlistRsp mMsgCidlistRsp = (MsgCidlistRsp) mRspMsgHeader;
                request_success(mMsgCidlistRsp);
                updateView();
                mSceneAdapter.add(new MsgSceneData());
                mSceneGrid.setAdapter(mSceneAdapter);
                updateCurrentCover(mCurrentTheme);
                mCidDataAdapter.notifyDataSetChanged();
            } else {
                showNotify(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_UNBINDCID_RSP) {
            mProgressDialog.dismissDialog();
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                MsgUnbindCidRsp mMsgUnbindCidRsp = (MsgUnbindCidRsp) mRspMsgHeader;
                String cid = mMsgUnbindCidRsp.cid;
                if (mSceneAdapter != null) {
                    List<MsgCidData> l;
                    for (int j = 0, scencCount = mSceneAdapter.getCount() - 1; j < scencCount; j++) {
                        l = mSceneAdapter.getItem(j).data;
                        for (int k = 0, size = l.size(); k < size; k++) {
                            if (l.get(k).cid.equals(cid)) {
                                l.remove(k);
                                if (mSceneAdapter.getItem(j).enable == ClientConstants.ENABLE_SCENE) {
                                    mCidDataAdapter.remove(mCidDataAdapter.getItem(k));
                                }
                                break;
                            }
                        }
                    }
                }

                mCidDataAdapter.notifyDataSetChanged();
                updateView();
            }

        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_BINDCID_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (mRspMsgHeader.ret == Constants.RETOK) {
                Request(TCP_GET_LIST);
            } else {
                if (AppManager.getAppManager().isActivityTop(MyVideos.this.getClass().getSimpleName())) {
                    ToastUtil.showFailToast(this, mRspMsgHeader.msg);
                }
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_ENABLESCENE_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            mProgressDialog.dismissDialog();
            if (Constants.RETOK == mRspMsgHeader.ret) {
                MsgEnableSceneRsp mMsgEnableSceneRsp = (MsgEnableSceneRsp) mRspMsgHeader;
                if (mSlidingMenu.isMenuShowing())
                    mSlidingMenu.toggle(true);
                switchScene(mMsgEnableSceneRsp);
            } else {
                ToastUtil.showFailToast(MyVideos.this, mRspMsgHeader.msg);
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_DELETESCENE_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            mProgressDialog.dismissDialog();
            if (Constants.RETOK == mRspMsgHeader.ret) {
                Request(TCP_GET_LIST);
            } else {
                ToastUtil.showFailToast(MyVideos.this, mRspMsgHeader.msg);
            }
        } else if (MsgpackMsg.CLIENT_SETACCOUNTINFO_RSP == msgpackMsg.msgId) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                AccountInfo mMsgSetAccountinfoRsp = (AccountInfo) mRspMsgHeader;
                vid = mMsgSetAccountinfoRsp.vid;
                String key = CacheUtil.getMSG_ACCOUNT_KEY();
                CacheUtil.saveObject(mMsgSetAccountinfoRsp, key);
                int sound = mMsgSetAccountinfoRsp.sound;
                int vibrate = mMsgSetAccountinfoRsp.vibrate;
                PreferenceUtil.setKeySetIsOpenVoice(MyVideos.this, sound == 1);
                PreferenceUtil.setKeySetIsOpenVibrate(MyVideos.this, vibrate == 1);
                if (!StringUtils.isEmptyOrNull(mMsgSetAccountinfoRsp.sms_phone)) {
                    if (mInfoView.getVisibility() == View.VISIBLE)
                        mInfoView.setVisibility(View.GONE);
                }
            }
        } else {
            if (MsgpackMsg.CLIENT_GETACCOUNTINFO_RSP == msgpackMsg.msgId) {
                RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == mRspMsgHeader.ret) {
                    AccountInfo mMsgSetAccountinfoRsp = (AccountInfo) mRspMsgHeader;
                    vid = mMsgSetAccountinfoRsp.vid;
                    CacheUtil.saveObject(mMsgSetAccountinfoRsp, CacheUtil.getMSG_ACCOUNT_KEY());
                    int sound = mMsgSetAccountinfoRsp.sound;
                    int vibrate = mMsgSetAccountinfoRsp.vibrate;
                    PreferenceUtil.setKeySetIsOpenVoice(MyVideos.this, sound == 1);
                    PreferenceUtil.setKeySetIsOpenVibrate(MyVideos.this, vibrate == 1);
                    if (!StringUtils.isEmptyOrNull(mMsgSetAccountinfoRsp.sms_phone)) {
                        if (mInfoView.getVisibility() == View.VISIBLE)
                            mInfoView.setVisibility(View.GONE);
                    }
                }
            } else if (MsgpackMsg.CLIENT_CIDSET_RSP == msgpackMsg.msgId || MsgpackMsg.CLIENT_CIDGET_RSP == msgpackMsg.msgId) {
                MsgCidGetSetParent mMsgCidSetRsp = (MsgCidGetSetParent) msgpackMsg;
                changeVideoVid(mMsgCidSetRsp);
                if (PreferenceUtil.getLocationisChange(this)) {
                    PreferenceUtil.setLocationisChange(this, false);
                    onRefresh();
                }
            } else if (MsgpackMsg.CLIENT_SDCARD_FORMAT_ACK == msgpackMsg.msgId) {
                MsgCidSdcardFormatRsp mMsgCidSdcardFormatRsp = (MsgCidSdcardFormatRsp) msgpackMsg;
                int sdcard = mMsgCidSdcardFormatRsp.sdcard;
                int sdcard_errno = mMsgCidSdcardFormatRsp.err;
                String cid = mMsgCidSdcardFormatRsp.caller;

                if (mCidDataAdapter != null) {
                    for (int j = 0, count = mCidDataAdapter.getCount(); j < count; j++) {
                        if (mCidDataAdapter.getItem(j).cid.equals(cid)) {
                            mCidDataAdapter.getItem(j).sdcard = sdcard;
                            mCidDataAdapter.getItem(j).err = sdcard_errno;
                            break;
                        }
                    }
                    mCidDataAdapter.notifyDataSetChanged();
                }
            } else if (MsgpackMsg.ID_RELAY_MASK_INFO_RSP == msgpackMsg.msgId) {
                MsgRelayMaskInfoRsp mMsgRelayMaskInfoRsp = (MsgRelayMaskInfoRsp) msgpackMsg;
                CacheUtil.saveObject(mMsgRelayMaskInfoRsp, CacheUtil.getCID_RELAYMASKINFO_KEY(mMsgRelayMaskInfoRsp.caller));
            } else if (MsgpackMsg.CLIENT_EFAML_GET_ALARM_RSP == msgpackMsg.msgId || MsgpackMsg.CLIENT_EFAML_SET_ALARM_RSP == msgpackMsg.msgId) {
                if (PreferenceUtil.getLocationisChange(this)) {
                    PreferenceUtil.setLocationisChange(this, false);
                    onRefresh();
                }
            } else if (MsgpackMsg.CLIENT_PUSH_SIMPLE_NOTICE == msgpackMsg.msgId) {
                MsgClientPushSimpleNotice rsp = (MsgClientPushSimpleNotice) msgpackMsg;
                if (mSceneAdapter != null) {
                    List<MsgCidData> l;
                    for (int j = 0, scencCount = mSceneAdapter.getCount() - 1; j < scencCount; j++) {
                        l = mSceneAdapter.getItem(j).data;
                        for (int k = 0, size = l.size(); k < size; k++) {
                            if (l.get(k).cid.equals(rsp.caller)) {
                                l.get(k).noAnswerBC = (rsp.push_type == MsgClientPushSimpleNotice.TYPE_EFAML_UNREAD ? MsgClientPushSimpleNotice.TYPE_EFAML_UNREAD : 0);
                                break;
                            }
                        }
                    }
                }
                mCidDataAdapter.notifyDataSetChanged();
            } else if (MsgpackMsg.CID_PUSH_GET_LOG == msgpackMsg.msgId) {
                ThreadPoolUtils.execute(new UploadLogWorker(Utils.getPostLogUrl(PreferenceUtil.getSessionId(this))));
            }else if (MsgpackMsg.CLIENT_SYNC_LOGOUT == msgpackMsg.msgId){
                MsgSyncLogout mMsgSyncLogout = (MsgSyncLogout) msgpackMsg;
                if ((mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_CHANGEPASS_REQ) || (mMsgSyncLogout.trigger_id == MsgpackMsg.CLIENT_SETPASS_REQ)) {
                    MyApp.showForceNotifyDialog(this, getString(R.string.PWD_CHANGED));
                } else {
                    MyApp.showForceNotifyDialog(this, getString(R.string.RET_ESESSION_NOT_EXIST));
                }
            }
        }
    }

    private void switchScene(MsgEnableSceneRsp mMsgEnableSceneRsp) {

        String mSceneId = String.valueOf(mMsgEnableSceneRsp.scene_id);
        if (mSceneAdapter != null) {
            for (int i = 0, count = mSceneAdapter.getCount(); i < count; i++) {
                mSceneAdapter.getItem(i).enable = 0;
                if (String.valueOf(mSceneAdapter.getItem(i).scene_id).equals(mSceneId)) {
                    mSceneAdapter.getItem(i).enable = ClientConstants.ENABLE_SCENE;
                    mCurrentTheme = i;
                    mCidDataAdapter.clear();
                    mCidDataAdapter.addAll(mSceneAdapter.getItem(i).data);
                    updateView();
                    mCidDataAdapter.notifyDataSetChanged();

                }
            }
            updateCurrentCover(mCurrentTheme);
            mSceneAdapter.notifyDataSetChanged();
        }

    }


    private void changeVideoVid(MsgCidGetSetParent mMsgCidSetRsp) {
        String cid = mMsgCidSetRsp.cid;
        if (mSceneAdapter != null) {
            List<MsgCidData> list;
            for (int i = 0, count = mSceneAdapter.getCount() - 1; i < count; i++) {
                list = mSceneAdapter.getItem(i).data;
                for (int j = 0, size = list.size(); j < size; j++) {
                    if (cid.equals(list.get(j).cid)) {
                        list.get(j).vid = mMsgCidSetRsp.vid;
                        CacheUtil.saveObject(mMsgCidSetRsp, CacheUtil.getMSG_VIDEO_CONFIG_KEY(cid));
                    }
                }
            }
        }

    }

    @SuppressLint("NewApi")
    private void updateCurrentCover(int mCurrentTheme) {
        if (mSceneAdapter != null && mCurrentTheme != -1) {
            MsgSceneData info = mSceneAdapter.getItem(mCurrentTheme);
            mMenuView.setText(info.scene_name);
//            mTopPicLayout.setImageBitmap(null);
//            mTopPicLayout.setImageResource(0);
            if (lastVid != info.vid) {
                clearScenePic();
            }
            PreferenceUtil.setHomeCover(this, info.image_id);
            String[] strs = getResources().getStringArray(R.array.modes);
            mModeView.setText(strs[info.mode]);
            mTopPicLayout.setTag(mSceneAdapter.getItem(mCurrentTheme).scene_id);
            if (info.image_id != 0) {
                mTopPicLayout.setImageResource(ClientConstants.home_covers[info.image_id - 1]);
                if (imageid != info.image_id || sceneid != info.scene_id) {
                    Bitmap bitmap = BitmapUtil.drawableToBitmap(getResources().getDrawable(ClientConstants.home_covers[info.image_id - 1]));
                    setThemeStyle(bitmap);
                    saveTitlebar(bitmap);
                }
            } else {
                getTopCoverFromNet(mCurrentTheme);
            }
            lastVid = info.vid;
            sceneid = info.scene_id;
            imageid = info.image_id;
        }

    }

    public void handlePush(int push, MsgPush mMsgPush) {

        switch (push) {
            case ClientConstants.PUSH_TYPE_WARN:
                onWarnPush(mMsgPush);
                break;
            case ClientConstants.PUSH_TYPE_NEW_VERSION: {
                if (mCidDataAdapter != null) {
                    for (int j = 0, count = mCidDataAdapter.getCount(); j < count; j++) {
                        if (mCidDataAdapter.getItem(j).cid.equals(mMsgPush.cid)) {
                            mCidDataAdapter.getItem(j).version = mMsgPush.version;
                            break;
                        }
                    }
                    mCidDataAdapter.notifyDataSetChanged();
                }
            }
            break;
            case ClientConstants.PUSH_TYPE_SDCARD_OFF:
            case ClientConstants.PUSH_TYPE_SDCARD_ON:
                if (mCidDataAdapter != null) {
                    for (int j = 0, count = mCidDataAdapter.getCount(); j < count; j++) {
                        if (mCidDataAdapter.getItem(j).cid.equals(mMsgPush.cid)) {
                            mCidDataAdapter.getItem(j).sdcard = mMsgPush.push_type == ClientConstants.PUSH_TYPE_SDCARD_OFF ? 0 : 1;
                            mCidDataAdapter.getItem(j).err = mMsgPush.err;
                            break;
                        }
                    }
                    mCidDataAdapter.notifyDataSetChanged();
                }
                break;
            case ClientConstants.PUSH_TYPE_REBIND:
                if (mCidDataAdapter != null) {
                    for (int j = 0, count = mCidDataAdapter.getCount(); j < count; j++) {
                        if (mCidDataAdapter.getItem(j).cid.equals(mMsgPush.cid)) {
                            mCidDataAdapter.remove(mCidDataAdapter.getItem(j));
                            break;
                        }
                    }
                    updateView();
                    mCidDataAdapter.notifyDataSetChanged();
                }
                break;
            case ClientConstants.PUSH_TYPE_MAGNET_ON:
                if (mCidDataAdapter != null) {
                    for (int j = 0; j < mCidDataAdapter.getCount(); j++) {
                        if (mCidDataAdapter.getItem(j).cid.equals(mMsgPush.cid)) {
                            mCidDataAdapter.getItem(j).magstate = mMsgPush.push_type
                                    == ClientConstants.PUSH_TYPE_MAGNET_ON ? 1 : 0;
                            mCidDataAdapter.getItem(j).noAnswerBC = 1;
                        }
                    }
                    mCidDataAdapter.notifyDataSetChanged();
                }
                break;
            case ClientConstants.PUSH_TYPE_MAGNET_OFF:
                if (mCidDataAdapter != null) {
                    for (int j = 0; j < mCidDataAdapter.getCount(); j++) {
                        if (mCidDataAdapter.getItem(j).cid.equals(mMsgPush.cid)) {
                            mCidDataAdapter.getItem(j).magstate = mMsgPush.push_type
                                    == ClientConstants.PUSH_TYPE_MAGNET_OFF ? 0 : 1;
                            mCidDataAdapter.getItem(j).noAnswerBC = 1;
                        }
                    }
                    mCidDataAdapter.notifyDataSetChanged();
                }
                break;
        }

        if (push != ClientConstants.PUSH_TYPE_OFFLINE && push != ClientConstants.PUSH_TYPE_ONLINE) {
            badge.show();
        }
    }

    void onWarnPush(MsgPush mMsgPush) {
        MsgCidData info = MsgCidlistRsp.getInstance().getVideoInfoByCid(mMsgPush.cid);
        if (info != null) {
            mCidDataAdapter.notifyDataSetChanged();
            showPushMsg(getString(R.string.XX_DISCOVERY, info.mName()), true);
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_HTTP_GET_LIST:
                    httpGetCidList();
                    break;

                case HANDLER_TO_SET_THEME:
                    Drawable able = new BitmapDrawable((Bitmap) msg.obj);
                    mSlidingMenu.setBackgroundDrawable(able);
                    break;
                case HANDLER_MSG_TOAST_GONE:
                    mInfoView.setVisibility(View.GONE);
                    break;
                case HANDLER_TO_SAVE_SCENE_COVER:
                    mSceneAdapter.notifyDataSetChanged();
                    break;
                case HANDLER_CLICK_BACK:
                    isClickExit = false;
                    break;
                case HANDLER_HOMECOVER_COMPLETE:
                    if (msg.obj == null)
                        break;
                    Bitmap bitmap = (Bitmap) msg.obj;
                    setThemeStyle(bitmap);
                    saveTitlebar(bitmap);
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getId() == R.id.video_list) {
            MsgCidData info = (MsgCidData) parent.getItemAtPosition(position);
            if (info != null) {
                if (info.os == Constants.OS_EFAML) {
                    startActivity(new Intent(this, EfamilyMainActivity.class).putExtra(ClientConstants.CIDINFO, info));
                    mCidDataAdapter.getItem(position - 1).noAnswerBC = 0;
                } else if (info.os == Constants.OS_DOOR_BELL || info.os == Constants.OS_DOOR_BELL_V2) {
                    mCidDataAdapter.getItem(position - 1).noAnswerBC = 0;
                    startActivity((new Intent(this, DoorBellActivity.class).putExtra(ClientConstants.CIDINFO, info)));
                } else if (info.os == Constants.OS_MAGNET) {
                    mCidDataAdapter.getItem(position - 1).noAnswerBC = 0;
                    startActivity(new Intent(this, MagneticActivity.class).putExtra(ClientConstants.CIDINFO, info));
                } else {
                    startActivity(new Intent(this, CallOrConf.class).putExtra(ClientConstants.CIDINFO, info));
                }
                mCidDataAdapter.notifyDataSetChanged();
            }
        } else if (parent.getId() == R.id.scene_grid) {
            if (!MyApp.getIsLogin())
                return;
            if (mSceneAdapter.getItem(position).enable == ClientConstants.ENABLE_SCENE)
                return;
            if (position != mSceneAdapter.getCount() - 1) {
                switchSceneRequest(position);
            } else {
                startActivityForResult(new Intent(MyVideos.this, EditSceneActivity.class).putExtra("flag", EditSceneActivity.FLAG_ADD), RESULT_TO_ADD_SCENE);
            }
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!mSlidingMenu.isMenuShowing()) {
                mSlidingMenu.toggle();
                return true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSlidingMenu.isMenuShowing()) {
                mSlidingMenu.toggle(true);
            } else {
                if (!isClickExit) {
                    isClickExit = true;
                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.click_back_again_exit), Utils.getApplicationName(this)), Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessageDelayed(HANDLER_CLICK_BACK, 3000);
                    return true;
                } else {
                    MyApp.releaseMemory();
                    return super.onKeyDown(keyCode, event);
                }

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void switchSceneRequest(int pos) {
        MsgEnableSceneReq mMsgEnableSceneReq = new MsgEnableSceneReq(PreferenceUtil.getBindingPhone(this));
        mMsgEnableSceneReq.scene_id = mSceneAdapter.getItem(pos).scene_id;
        MyApp.wsRequest(mMsgEnableSceneReq.toBytes());
        DswLog.i("send mMsgEnableSceneReq msg-->" + mMsgEnableSceneReq.toString());
    }

    private void getTopCoverFromNet(int index) {
        try {
            if (mCurrentTheme != -1) {
                new HomeCoverUtils(this, mHandler, mSceneAdapter.getItem(index).scene_id, mTopPicLayout);
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    private void showPushMsg(String msg, Boolean isDiss) {
        mInfoView.setVisibility(View.VISIBLE);
        mInfoView.setText(msg);
        mInfoView.setOnClickListener(null);
        if (isDiss) {
            mHandler.sendEmptyMessageDelayed(HANDLER_MSG_TOAST_GONE, 2000);
        } else {// discount
            mListView.setRefreshEnabled(false);
            mListView.setOverTimeViewVisibity(false);
        }
    }

    private void showSafeSubmitData() {
        mInfoView.setVisibility(View.VISIBLE);
        Drawable img_right = getResources().getDrawable(R.drawable.ico_nextstep_arrow);
        if (img_right != null)
            img_right.setBounds(0, 0, img_right.getMinimumWidth(), img_right.getMinimumHeight());
        Drawable img_left = getResources().getDrawable(R.drawable.ico_warning_symbol);
        if (img_left != null)
            img_left.setBounds(0, 0, img_left.getMinimumWidth(), img_left.getMinimumHeight());
        mInfoView.setCompoundDrawables(img_left, null, img_right, null);
        mInfoView.setOnClickListener(this);
    }

    @Override
    public void connectServer() {
        mListView.setRefreshEnabled(true);
        if (mInfoView.getVisibility() == View.VISIBLE) {
            mInfoView.setVisibility(View.GONE);
        }
    }

    @Override
    public void disconnectServer() {// net_expection
        showPushMsg("(-" + MyApp.getMsgID() + ")" + getString(R.string.NO_NETWORK_2), false);
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(MyVideos.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
        clearRelayMaskInfo();
    }


    private void getCacheListDate() {
        MsgCidlistRsp mMsgCidlistRsp = (MsgCidlistRsp) CacheUtil.readObject(CacheUtil.getCID_LIST_KEY());
        if (mMsgCidlistRsp == null)
            return;
        request_success(mMsgCidlistRsp);
        updateView();
        mSceneAdapter.add(new MsgSceneData());
        mSceneGrid.setAdapter(mSceneAdapter);
        updateCurrentCover(mCurrentTheme);
        mCidDataAdapter.notifyDataSetChanged();

    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                   long id) {
        if (mCidDataAdapter == null || !MyApp.getIsLogin() || position == 0)
            return true;
        if (mCidDataAdapter.getItem(position - 1).os == Constants.OS_MAGNET)
            return true;
        mMenuDialog = new HomeMenuDialog(MyVideos.this);
        if (!StringUtils.isEmptyOrNull(mCidDataAdapter.getItem(position - 1).share_account)) {
            mMenuDialog.hideSettingBtn();
        }
        mMenuDialog.setListenter(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMenuDialog.dismiss();
                StatService.trackCustomEvent(MyVideos.this, MTATAG, getString(R.string.SETTINGS));
                if (mCidDataAdapter.getItem(position - 1).os == Constants.OS_EFAML) {
                    startActivity(new Intent(MyVideos.this, EFamilySettingActivity.class).putExtra(ClientConstants.CIDINFO, mCidDataAdapter.getItem(position - 1)));
                } else if (mCidDataAdapter.getItem(position - 1).os == Constants.OS_DOOR_BELL || mCidDataAdapter.getItem(position - 1).os == Constants.OS_DOOR_BELL_V2) {
                    startActivity(new Intent(MyVideos.this, DoorBellDetailActivity.class).putExtra(ClientConstants.CIDINFO, mCidDataAdapter.getItem(position - 1)));
                } else {
                    startActivityForResult(new Intent(MyVideos.this, DeviceSettingActivity.class).putExtra(ClientConstants.CIDINFO, mCidDataAdapter.getItem(position - 1)),
                            HANDLER_TO_SET_DEVICE);
                }
            }
        }, new OnClickListener() {

            @Override
            public void onClick(View v) {
                mMenuDialog.dismiss();
                showDelMessageDialog(position - 1, mCidDataAdapter.getItem(position - 1).mName());
            }
        });
        mMenuDialog.show();
        Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(50);
        return true;
    }

    private void showDelMessageDialog(final int pos, String name) {
        final NotifyDialog dialog = new NotifyDialog(this);
        dialog.setButtonText(R.string.DELETE, R.string.CANCEL);
        dialog.setPosRedTheme(R.drawable.bg_dialogdel_selector, getResources().getColor(R.color.mycount_not_set));
        dialog.show(String.format(getString(R.string.SURE_DELETE_1), name), new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.confirm:
                        StatService.trackCustomEvent(MyVideos.this, MTATAG, getString(R.string.DELETE));
                        dialog.dismiss();
                        if (!MyApp.getIsLogin()) {
                            ToastUtil.showFailToast(MyVideos.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                            return;
                        }
                        if (mCidDataAdapter.getItem(pos) == null)
                            return;
                        mProgressDialog.showDialog(getString(R.string.DELETEING));
                        MyApp.wsRequest(RequestMessage.getMsgUnbindCidReq(mCidDataAdapter.getItem(pos).cid).toBytes());
                        break;
                    case R.id.cancel:
                        dialog.dismiss();
                        break;
                }

            }
        }, null);

    }


    private void setThemeStyle(Bitmap bitmap) {
        ThreadPoolUtils.execute(new SaveMenuBackgroundRunnable(this, mHandler, bitmap));
    }

    private void saveTitlebar(Bitmap bitmap) {
        ThreadPoolUtils.execute(new SaveTitlebarRunnable(this, bitmap));
    }

    @Override
    public void isResume() {
        ActivityIsResumeManager.setActivityIsResumeListener(null);
        if (mAdialog != null) {
            mAdialog.dismiss();
        }
    }

    @Override
    public void httpDone(HttpResult mResult) {
        try {
            if (mResult.ret == Constants.HTTP_RETOK) {
                JSONObject mObject = new JSONObject(mResult.result);
                if (mObject.has("ret") && mObject.getInt("ret") == Constants.RETOK) {
                    String act = mObject.has("act") ? mObject.getString("act") : "";
                    if (act.equals("add_scene_rsp") || act.equals("edit_scene_rsp")) {
                        Request(TCP_GET_LIST);
                    }
                }

            }

        } catch (JSONException e) {
            DswLog.ex(e.toString());
        }
    }

    private void clearRelayMaskInfo() {
        if (mSceneAdapter != null) {
            for (int i = 0; i < mSceneAdapter.getCount() - 1; i++) {
                MsgSceneData info = mSceneAdapter.getItem(i);
                for (int j = 0; j < info.data.size(); j++) {
                    CacheUtil.remove(CacheUtil.getCID_RELAYMASKINFO_KEY(info.data.get(j).cid));
                }
            }
        }
    }


    private void showErrorDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.hideNegButton();
        mDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mDialog.show(String.format(getString(R.string.permission_auth), Utils.getApplicationName(MyVideos.this), getString(R.string.camera_auth)), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Request(TCP_GET_LIST);
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ClientConstants.ACTION);
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(receiver, filter);
    }

    private void clearScenePic() {
        Utils.clearDirectoryFile(PathGetter.getScreenShotPath());
        Utils.clearDirectoryFile(PathGetter.getRootPath());
        ImageLoader.getInstance().getDiskCache().clear();
        clearLocationPic();
    }

    private void clearLocationPic() {
        if (mCurrentTheme != -1) {
            String url = "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT +
                    "/index.php?sessid=" + PreferenceUtil.getSessionId(this)
                    + "&mod=client&act=get_scene_image&scene_id=" + mSceneAdapter.getItem(mCurrentTheme).scene_id;
//            MyApp.getFinalBitmap().clearCache(url);
            MyImageLoader.removeFromCache(url);
        }
    }
}