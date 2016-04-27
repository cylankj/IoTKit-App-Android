package com.cylan.jiafeigou.activity.video.setting;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.support.DswLog;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.main.RenameDialog;
import com.cylan.jiafeigou.activity.video.CallOrConf;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.RequestMessage;
import com.cylan.jiafeigou.entity.TimeZoneBean;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgCidGetSetParent;
import com.cylan.jiafeigou.entity.msg.MsgCidSdcardFormat;
import com.cylan.jiafeigou.entity.msg.MsgPush;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOffline;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOnline;
import com.cylan.jiafeigou.entity.msg.MsgSyncSdcard;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgCidGetReq;
import com.cylan.jiafeigou.entity.msg.req.MsgCidSetReq;
import com.cylan.jiafeigou.entity.msg.req.MsgClientHasMobileReq;
import com.cylan.jiafeigou.entity.msg.req.MsgSetCidAliasReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidSdcardFormatRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientHasMobileRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgSetCidAliasRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgUnbindCidRsp;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.tencent.stat.StatService;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class DeviceSettingActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener {

    // handler
    private final static int HANDLER_SCAN_OVER_TIME = 0X01;

    private final static String MTATAG = "CameraSetting";

    // onActivityResult
    public static int TO_SET_WIFI = 0x01;
    public static int TO_SET_DIRECTION = 0x02;
    public static int TO_SET_TIMEZONE = 0x03;
    public static int TO_SET_VIDEO_MODEL = 0x04;
    public static int TO_SET_VIDEO_POSITON = 0x05;


    private ToggleButton mLedBtn;
    private ToggleButton mDataBtn;
    private ToggleButton mHz;

    private LinearLayout dataLayout;
    private LinearLayout hzLayout;

    private TextView mNameView;
    private TextView mWifiView;
    private TextView mPositionView;
    private TextView mVisionView;
    private TextView mDirctionView;
    private TextView mSdCardView;
    private ProgressBar mInitSdcardLoading;
    private TextView mTimeZoneView;
    private TextView mAutomaticVideoView;
    private TextView mMobileText;
    private TextView mHzText;

    private MsgCidData mData;
    private MsgCidGetSetParent msgCidGetSetParent;

    private NotifyDialog notifyDlg;

    private String[] directions = null;
    private List<TimeZoneBean> mList = null;

    private Boolean isClick = false;

    private WifiManager wm;

    private MyBroadcastReceiver mBroadcastReceiver;

    private String key;

    private Dialog mDelDialog = null;

    private int DEFAULT_TIMEZONE = -1;

    private RenameDialog mRenameDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_set);
        mData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);

        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        setTitle(R.string.SETTINGS_1);

        initDate();
        initView();
        getConfig();
        registerReceiver();
        if (mData.os == Constants.OS_CAMERA_ANDROID) {
            getHasMobile();
            hzLayout.setVisibility(View.GONE);
            mHzText.setVisibility(View.GONE);
        }
    }

    private void initView() {
        RelativeLayout mModifyName = (RelativeLayout) findViewById(R.id.modifyname);
        RelativeLayout mSettingWifi = (RelativeLayout) findViewById(R.id.wifipeizhi);
        LinearLayout mPositionLayout = (LinearLayout) findViewById(R.id.position);
        LinearLayout mDeviceMsg = (LinearLayout) findViewById(R.id.devicemsg);
        mLedBtn = (ToggleButton) findViewById(R.id.toggle_led);
        Button mUnbindBtn = (Button) findViewById(R.id.delete_device);
        mModifyName.setOnClickListener(this);
        mSettingWifi.setOnClickListener(this);
        mPositionLayout.setOnClickListener(this);
        mDeviceMsg.setOnClickListener(this);
        mUnbindBtn.setOnClickListener(this);
        mNameView = (TextView) findViewById(R.id.name);
        mWifiView = (TextView) findViewById(R.id.wifiname);
        mPositionView = (TextView) findViewById(R.id.positionname);
        mVisionView = (TextView) findViewById(R.id.vision);
        LinearLayout mDirectionLayout = (LinearLayout) findViewById(R.id.layout_direction);
        mDirectionLayout.setOnClickListener(this);
        mDirctionView = (TextView) findViewById(R.id.video_direction);
        RelativeLayout mSdCardLayout = (RelativeLayout) findViewById(R.id.layout_sdcard);
        mSdCardLayout.setOnClickListener(this);
        mSdCardView = (TextView) findViewById(R.id.video_sdcard);
        mInitSdcardLoading = (ProgressBar) findViewById(R.id.init_sdcard_loading);
        LinearLayout mTimeZoneLayout = (LinearLayout) findViewById(R.id.timezone_layout);
        mTimeZoneLayout.setOnClickListener(this);
        mTimeZoneView = (TextView) findViewById(R.id.timezone);
        LinearLayout mAutomaticVideoLayout = (LinearLayout) findViewById(R.id.layout_automatic_video);
        mAutomaticVideoLayout.setOnClickListener(this);
        mAutomaticVideoView = (TextView) findViewById(R.id.video_automatic_video);
        mDataBtn = (ToggleButton) findViewById(R.id.toggle_mobile_data);
        mHz = (ToggleButton) findViewById(R.id.toggle_hz);
        dataLayout = (LinearLayout) findViewById(R.id.layout_mobile_data);
        hzLayout = (LinearLayout) findViewById(R.id.layout_hz);
        mMobileText = (TextView) findViewById(R.id.tv_mobile_text);
        mHzText = (TextView) findViewById(R.id.tv_hz_text);

        mNameView.setText(mData.mName());

        if (mData.net != MsgCidData.CID_NET_OFFLINE) {
            if (mData.net != MsgCidData.CID_NET_3G)
                mWifiView.setText(mData.name);
        } else {
            mDataBtn.setClickable(false);
            if (msgCidGetSetParent != null) {
                if (msgCidGetSetParent.isMobile == 1) {
                    mDataBtn.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.ico_switch_open_unenable));
                } else {
                    mDataBtn.setBackgroundDrawable(getResources().getDrawable(
                            R.drawable.ico_switch_close_unenable));
                }
            }
        }

        if (msgCidGetSetParent != null)
            mPositionView.setText(MsgCidlistRsp.getInstance().getSceneName(msgCidGetSetParent.location));

        mVisionView.setText(mData.version);

        initSDState();

        if (PreferenceUtil.getIsFirstClickAutomaticVideo(this)) {
            findViewById(R.id.automatic_video_remind).setVisibility(View.VISIBLE);
        }

        mLedBtn.setOnCheckedChangeListener(this);
        mHz.setOnCheckedChangeListener(this);

    }

    private void initDate() {
        key = CacheUtil.getMSG_VIDEO_CONFIG_KEY(mData.cid);

        if (directions == null) {
            directions = getResources().getStringArray(R.array.video_direction);
        }
        if (mList == null) {
            mList = TimeZoneBean.parseXml(getResources().getXml(R.xml.timezones));
            Collections.sort(mList);
        }
        msgCidGetSetParent = (MsgCidGetSetParent) CacheUtil.readObject(key);
    }


    private void getConfig() {
        if (msgCidGetSetParent == null) {
            getWarmConfig();
        } else {
            if (mData.vid > msgCidGetSetParent.vid) {
                getWarmConfig();
            } else {
                onSuc(msgCidGetSetParent);
            }

        }
    }

    private void registerReceiver() {
        mBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mBroadcastReceiver, filter);
    }

    private void initSDState() {
        if (mData.sdcard != 0 && mData.err != 0) {
            mSdCardView.setText(String.format(getString(R.string.SD_INIT_ERR), mData.err));
            mSdCardView.setTextColor(getResources().getColor(R.color.mycount_not_set));
            ((TextView) findViewById(R.id.video_sd_desc)).setText(R.string.VIDEO_SD_DESC);
            findViewById(R.id.video_sd_desc).setVisibility(View.VISIBLE);
            findViewById(R.id.sdcard_arrow).setVisibility(View.VISIBLE);
        } else if (mData.sdcard == 0) {
            mSdCardView.setText(R.string.SD_NO);
            mSdCardView.setTextColor(getResources().getColor(R.color.normal_text));
            findViewById(R.id.video_sd_desc).setVisibility(View.GONE);
            findViewById(R.id.sdcard_arrow).setVisibility(View.INVISIBLE);

        } else if (mData.sdcard != 0 && mData.err == 0) {
            mSdCardView.setText(R.string.SD_NORMAL);
            mSdCardView.setTextColor(getResources().getColor(R.color.normal_text));
            ((TextView) findViewById(R.id.video_sd_desc)).setText(R.string.SD_INFO);
            findViewById(R.id.video_sd_desc).setVisibility(View.VISIBLE);
            findViewById(R.id.sdcard_arrow).setVisibility(View.INVISIBLE);
        }

        mAutomaticVideoView.setVisibility(View.VISIBLE);
        if ((mData.sdcard != 0 && mData.err != 0) || mData.sdcard == 0) {
            if (msgCidGetSetParent == null)
                return;
            if (msgCidGetSetParent.auto_record != ClientConstants.AUTO_RECORD3) {
                mAutomaticVideoView.setVisibility(View.GONE);
            }
        }
    }

    private void getWarmConfig() {

        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(DeviceSettingActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        MsgCidGetReq msgCidGetReq = new MsgCidGetReq();
        msgCidGetReq.cid = mData.cid;
        MyApp.wsRequest(msgCidGetReq.toBytes());
        DswLog.i("send MsgCidGetReq msg-->" + msgCidGetReq.toString());
        mProgressDialog.showDialog(getString(R.string.getting));

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                finish();
                break;
            case R.id.modifyname:
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.EQUIPMENT_NAME));
                rename();
                break;
            case R.id.wifipeizhi:
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.DEVICES_TITLE_3));
                if (wm.isWifiEnabled()) {
                    isClick = true;
                    wm.startScan();
                    mProgressDialog.showDialog(getString(R.string.LOADING));

                    Message msg = mHandler.obtainMessage();
                    msg.what = HANDLER_SCAN_OVER_TIME;
                    msg.obj = false;
                    mHandler.sendMessageDelayed(msg, 15000);
                } else {
                    ToastUtil.showFailToast(this, getString(R.string.WIFI_OFF));
                }

                break;
            case R.id.devicemsg:
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.EQUIPMENT_INFO));

                startActivity(new Intent(DeviceSettingActivity.this, DeviceState.class).putExtra(ClientConstants.CIDINFO, mData));
                break;
            case R.id.delete_device:
                showLogoutDialog();

                break;

            case R.id.layout_direction:
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.SETTING_DIRECTION));
                if (mData == null || msgCidGetSetParent == null)
                    return;
                startActivityForResult(new Intent(DeviceSettingActivity.this, DirectionActivity.class)
                                .putExtra(ClientConstants.SELECT_INDEX, msgCidGetSetParent.direction),
                        TO_SET_DIRECTION);
                break;
            case R.id.layout_sdcard:
                if (mData == null || msgCidGetSetParent == null)
                    return;
                if (mData.sdcard != 0 && mData.err != 0) {
                    final NotifyDialog dialog = new NotifyDialog(this);
                    dialog.setButtonText(R.string.SD_INIT, R.string.CANCEL);
                    dialog.show(R.string.SD_INFO_1, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {
                                case R.id.cancel:

                                    dialog.dismiss();
                                    break;
                                case R.id.confirm:

                                    dialog.dismiss();
                                    if (mData.net != MsgCidData.CID_NET_OFFLINE && mData.net != MsgCidData.CID_NET_CONNECT) {
                                        mSdCardView.setVisibility(View.GONE);
                                        mInitSdcardLoading.setVisibility(View.VISIBLE);
                                        if (!MyApp.getIsLogin()) {
                                            ToastUtil.showFailToast(DeviceSettingActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                                            return;
                                        }
                                        MsgCidSdcardFormat msgCidSdcardFormat = new MsgCidSdcardFormat(mData.cid);
                                        MyApp.wsRequest(msgCidSdcardFormat.toBytes());
                                        DswLog.i("send MsgCidSdcardFormat msg-->" + msgCidSdcardFormat.toString());

                                    } else {
                                        ToastUtil.showFailToast(DeviceSettingActivity.this, getString(R.string.RET_EUNONLINE_CID));
                                    }

                                    break;
                            }

                        }
                    }, null);
                }
                break;
            case R.id.timezone_layout: {
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.SETTING_TIMEZONE));

                final NotifyDialog dialog1 = new NotifyDialog(this);
                dialog1.setButtonText(R.string.CARRY_ON, R.string.CANCEL);
                dialog1.show(R.string.TIMEZONE_INFO, new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {

                            case R.id.confirm:
                                dialog1.dismiss();
                                Intent intent = new Intent(DeviceSettingActivity.this, TimeZoneActivity.class);
                                intent.putExtra(ClientConstants.TIMEZONE_DATA, (Serializable) mList);
                                intent.putExtra(ClientConstants.TIMEZONE_SET_POS, DEFAULT_TIMEZONE);
                                startActivityForResult(intent, TO_SET_TIMEZONE);
                                break;
                            case R.id.cancel:
                                dialog1.dismiss();
                                break;
                        }

                    }
                }, null);
            }
            break;
            case R.id.layout_automatic_video:
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.SETTING_RECORD));
                if (mData == null || msgCidGetSetParent == null)
                    return;
                startActivityForResult(new Intent(DeviceSettingActivity.this, AutomaticVideoActivity.class).putExtra(ClientConstants.K_ENABLE, msgCidGetSetParent.warn_enable).putExtra(ClientConstants.K_VIDEO_MODEL, msgCidGetSetParent.auto_record).putExtra(ClientConstants.VIDEOINFO, mData),
                        TO_SET_VIDEO_MODEL);
                PreferenceUtil.setIsFirstClickAutomaticVideo(this, false);
                findViewById(R.id.automatic_video_remind).setVisibility(View.GONE);
                break;
            case R.id.position:
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.LOCATION_SETTING));
                if (mData == null || msgCidGetSetParent == null)
                    return;
                startActivityForResult(
                        new Intent(this, LocationActivity.class).putExtra(ClientConstants.SELECT_INDEX, msgCidGetSetParent.location),
                        TO_SET_VIDEO_POSITON);
                break;
            default:
                break;
        }
    }

    private void showLogoutDialog() {

        if (mDelDialog == null) {
            mDelDialog = new Dialog(this, R.style.func_dialog);
            View content = View.inflate(this, R.layout.dialog_deldevice, null);
            TextView cancel = (TextView) content.findViewById(R.id.btn_cancel);
            cancel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDelDialog.dismiss();
                }
            });
            Button btn = (Button) content.findViewById(R.id.del);

            btn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDelDialog.dismiss();
                    final NotifyDialog dialog = new NotifyDialog(DeviceSettingActivity.this);
                    dialog.setButtonText(R.string.DELETE_CID, R.string.CANCEL);
                    dialog.setPosRedTheme(R.drawable.bg_dialogdel_selector, getResources().getColor(R.color.mycount_not_set));
                    dialog.show(String.format(getString(R.string.SURE_DELETE_1), mData.mName()), new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {

                                case R.id.confirm:
                                    dialog.dismiss();
                                    StatService.trackCustomEvent(DeviceSettingActivity.this, MTATAG,
                                            getString(R.string.DELETE_CID));
                                    httpDelCids();
                                    break;
                                case R.id.cancel:
                                    dialog.dismiss();
                                    break;
                            }

                        }
                    }, null);

                }
            });
            mDelDialog.setContentView(content);
            mDelDialog.setCanceledOnTouchOutside(true);

        }
        try {
            mDelDialog.show();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    void httpDelCids() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(DeviceSettingActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        mProgressDialog.showDialog(getString(R.string.DELETEING));

        MyApp.wsRequest(RequestMessage.getMsgUnbindCidReq(mData.cid).toBytes());

    }

    private Boolean isSameNet() {
        WifiInfo mConnWif = wm.getConnectionInfo();
        return (mData.net == MsgCidData.CID_NET_WIFI && !StringUtils.isEmptyOrNull(mConnWif.getSSID())
                && mConnWif.getSSID().replaceAll("\"", "").equals(mData.name.replaceAll("\"", "")));
    }

    private Boolean isExistAp() {
        if (mData.net != MsgCidData.CID_NET_WIFI) {
            List<ScanResult> results = wm.getScanResults();
            for (ScanResult sr : results) {
                if (sr.SSID.equals(getAPName(mData.cid))) {
                    return true;
                }

            }
        }
        return false;
    }

    private String getAPName(String cid) {
        return "DOG-" + cid.substring(6);
    }

    private void rename() {
        if (mRenameDialog != null)
            mRenameDialog = null;
        mRenameDialog = new RenameDialog(this, mData.mName());

        mRenameDialog.setOnConfirmListener(new RenameDialog.Request() {
            @Override
            public void callback(String name) {
                renameRequest(name);
            }
        });
        if (!isFinishing()) {
            mRenameDialog.show();
            mRenameDialog.setFullScreen(this);
        }
    }

    void renameRequest(String name) {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(DeviceSettingActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }

        MsgSetCidAliasReq msgSetCidAliasReq = new MsgSetCidAliasReq();
        msgSetCidAliasReq.cid = mData.cid;
        msgSetCidAliasReq.alias = name;
        MyApp.wsRequest(msgSetCidAliasReq.toBytes());
        mProgressDialog.showDialog(getString(R.string.rename));
        DswLog.i("send MsgSetCidAliasReq msg-->" + msgSetCidAliasReq.toString());
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        mProgressDialog.dismissDialog();
        try {
            if (MsgpackMsg.CLIENT_SETCIDALIAS_RSP == msgpackMsg.msgId) {
                RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == rspMsgHeader.ret) {
                    MsgSetCidAliasRsp msgSetCidAliasRsp = (MsgSetCidAliasRsp) msgpackMsg;
                    mData.alias = msgSetCidAliasRsp.alias;
                    mNameView.setText(msgSetCidAliasRsp.alias);
                } else {
                    ToastUtil.showFailToast(DeviceSettingActivity.this, rspMsgHeader.msg);
                }
                Intent intent = getIntent();
                intent.putExtra("handler", "alias");
                intent.putExtra("alias", mData.mName());
                setResult(RESULT_OK, intent);

            } else if (MsgpackMsg.CLIENT_UNBINDCID_RSP == msgpackMsg.msgId) {
                RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == rspMsgHeader.ret) {
                    MsgUnbindCidRsp msgUnbindCidRsp = (MsgUnbindCidRsp) msgpackMsg;
                    if (msgUnbindCidRsp.cid.equals(mData.cid)) {
                        AppManager.getAppManager().finishActivity(DeviceSettingActivity.class);
                        AppManager.getAppManager().finishActivity(CallOrConf.class);
                    }

                } else {
                    showNotify(rspMsgHeader.msg, rspMsgHeader.ret);
                }

            } else if (MsgpackMsg.CLIENT_CIDGET_RSP == msgpackMsg.msgId) {
                RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == rspMsgHeader.ret) {
                    MsgCidGetSetParent msgCidGetRsp = (MsgCidGetSetParent) msgpackMsg;
                    if (msgCidGetRsp.cid.equals(mData.cid)) {
                        CacheUtil.saveObject(msgCidGetRsp, key);
                        onSuc(msgCidGetRsp);
                    }
                } else {
                    onError(rspMsgHeader.msg);
                }
            } else if (MsgpackMsg.CLIENT_SYNC_SDCARD == msgpackMsg.msgId) {
                MsgSyncSdcard msgSyncSdcard = (MsgSyncSdcard) msgpackMsg;
                if (mData != null) {
                    if (mData.cid.equals(msgSyncSdcard.caller)) {
                        mData.sdcard = msgSyncSdcard.sdcard;
                        mData.err = msgSyncSdcard.err;
                        initSDState();
                    }
                }
            } else if (MsgpackMsg.CLIENT_SYNC_CIDONLINE == msgpackMsg.msgId) {
                MsgSyncCidOnline msgSyncCidOnline = (MsgSyncCidOnline) msgpackMsg;
                if (mData != null) {
                    if (mData.cid.equals(msgSyncCidOnline.cid)) {
                        mData.net = msgSyncCidOnline.net;
                        mData.name = msgSyncCidOnline.name;
                        if (!StringUtils.isEmptyOrNull(msgSyncCidOnline.version))
                            mData.version = msgSyncCidOnline.version;
                        if (mData.net != MsgCidData.CID_NET_3G)
                            mWifiView.setText(mData.name);
                        mVisionView.setText(mData.version);

                    }
                    int net = msgSyncCidOnline.net;
                    if (net == MsgCidData.CID_NET_OFFLINE || net == MsgCidData.CID_NET_CONNECT) {
                        mDataBtn.setClickable(false);
                        if (msgCidGetSetParent.isMobile == 1) {
                            mDataBtn.setBackgroundDrawable(getResources().getDrawable(
                                    R.drawable.ico_switch_open_unenable));
                        } else {
                            mDataBtn.setBackgroundDrawable(getResources().getDrawable(
                                    R.drawable.ico_switch_close_unenable));
                        }
                    } else {
                        mDataBtn.setClickable(true);
                        mDataBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.checkbox_btn));
                        mDataBtn.setChecked(msgCidGetSetParent.isMobile == 1);
                    }
                }
            } else if (MsgpackMsg.CLIENT_SYNC_CIDOFFLINE == msgpackMsg.msgId) {
                MsgSyncCidOffline msgSyncCidOffline = (MsgSyncCidOffline) msgpackMsg;
                if (mData != null) {
                    if (mData.cid.equals(msgSyncCidOffline.cid)) {
                        mData.net = 0;
                        mData.name = "";
                        mWifiView.setText(mData.name);

                    }
                    mDataBtn.setClickable(false);
                    if (msgCidGetSetParent.isMobile == 1) {
                        mDataBtn.setBackgroundDrawable(getResources().getDrawable(
                                R.drawable.ico_switch_open_unenable));
                    } else {
                        mDataBtn.setBackgroundDrawable(getResources().getDrawable(
                                R.drawable.ico_switch_close_unenable));
                    }
                }
            } else if (MsgpackMsg.CLIENT_CIDSET_RSP == msgpackMsg.msgId) {
                RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (rspMsgHeader.ret == -1 || rspMsgHeader.ret == 1) {
                    ToastUtil.showToast(DeviceSettingActivity.this, rspMsgHeader.msg);
                } else {
                    if (Constants.RETOK == rspMsgHeader.ret) {
                        MsgCidGetSetParent msgCidSetRsp = (MsgCidGetSetParent) msgpackMsg;
                        if (msgCidSetRsp.cid.equals(mData.cid)) {
                            mData.vid = msgCidSetRsp.vid;
                            CacheUtil.saveObject(msgCidSetRsp, key);
                            onSuc(msgCidSetRsp);
                            ToastUtil.showSuccessToast(DeviceSettingActivity.this, getString(R.string.PWD_OK_2));
                        }
                    } else {
                        ToastUtil.showFailToast(DeviceSettingActivity.this, rspMsgHeader.msg);
                    }
                }

            } else if (MsgpackMsg.CID_SDCARD_FORMAT_RSP == msgpackMsg.msgId) {
                MsgCidSdcardFormatRsp msgCidSdcardFormatRsp = (MsgCidSdcardFormatRsp) msgpackMsg;
                int sdcard = msgCidSdcardFormatRsp.sdcard;
                int sdcard_errno = msgCidSdcardFormatRsp.err;
                String cid = msgCidSdcardFormatRsp.caller;

                mSdCardView.setVisibility(View.VISIBLE);
                mInitSdcardLoading.setVisibility(View.GONE);
                if (cid.equals(mData.cid)) {
                    mData.sdcard = sdcard;
                    mData.err = sdcard_errno;
                    initSDState();
                }

            } else if (MsgpackMsg.CLIENT_PUSH == msgpackMsg.msgId) {
                MsgPush msgPush = (MsgPush) msgpackMsg;
                int pushtype = msgPush.push_type;
                if (pushtype == ClientConstants.PUSH_TYPE_SDCARD_OFF || pushtype == ClientConstants.PUSH_TYPE_SDCARD_ON) {
                    if (mData.cid.equals(msgPush.cid)) {
                        if (pushtype == ClientConstants.PUSH_TYPE_SDCARD_OFF) {
                            mSdCardView.setText(R.string.SD_NO);
                            mSdCardView.setTextColor(getResources().getColor(R.color.title_text_color));
                        } else {
                            mData.sdcard = msgPush.push_type == ClientConstants.PUSH_TYPE_SDCARD_OFF ? 0 : 1;
                            mData.err = msgPush.err;
                            initSDState();
                        }
                    }
                }

            } else if (MsgpackMsg.CLIENT_HAS_MOBILE_RSP == msgpackMsg.msgId) {
                MsgClientHasMobileRsp hasMobileRsp = (MsgClientHasMobileRsp) msgpackMsg;
                if (Constants.RETOK == hasMobileRsp.ret) {
                    int isMobile = hasMobileRsp.isMobile;
                    PreferenceUtil.setHasMobile(this, isMobile);
                    if (isMobile == 1) {
                        dataLayout.setVisibility(View.VISIBLE);
                        mMobileText.setVisibility(View.VISIBLE);
                    }
                } else {
                    ToastUtil.showFailToast(DeviceSettingActivity.this, hasMobileRsp.msg);
                }
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    public void onSuc(MsgCidGetSetParent mMsgCidGetSetParent) {

        mProgressDialog.dismissDialog();
        msgCidGetSetParent = mMsgCidGetSetParent;
        mLedBtn.setChecked(msgCidGetSetParent.led == 1);
        mDataBtn.setChecked(msgCidGetSetParent.isMobile == 1);
        mDataBtn.setOnCheckedChangeListener(this);
        mHz.setChecked(msgCidGetSetParent.isNTSC == 1);
        mDirctionView.setText(directions[msgCidGetSetParent.direction]);
        if (!StringUtils.isEmptyOrNull(msgCidGetSetParent.timezonestr)) {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).getTimezoneid().equals(msgCidGetSetParent.timezonestr)) {
                    mTimeZoneView.setText(mList.get(i).getTimezonename());
                    DEFAULT_TIMEZONE = i;
                    break;
                } else {
                    mTimeZoneView.setText(msgCidGetSetParent.timezonestr);
                }
            }
        }
        setAutoReco(msgCidGetSetParent.auto_record);
        mPositionView.setText(MsgCidlistRsp.getInstance().getSceneName(msgCidGetSetParent.location));
    }


    public void onError(String msg) {
        mProgressDialog.dismissDialog();
        ToastUtil.showFailToast(this, msg);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    removeMessages(HANDLER_SCAN_OVER_TIME);

                case HANDLER_SCAN_OVER_TIME:
                    mProgressDialog.dismissDialog();
                    if ((Boolean) msg.obj) {
                        startActivityForResult(new Intent(DeviceSettingActivity.this, SetCameraWifi.class).putExtra(ClientConstants.CIDINFO, mData), TO_SET_WIFI);
                    } else {
                        String str = String.format(getResources().getString(R.string.setwifi_check), mData.name.replaceAll("\"", ""));

                        final NotifyDialog dialog = new NotifyDialog(DeviceSettingActivity.this);
                        dialog.setButtonText(R.string.OK, R.string.CANCEL);
                        dialog.hideNegButton();
                        dialog.show(mData.net == MsgCidData.CID_NET_WIFI ? str : getResources().getString(R.string.BLUE_BLINKING), new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                switch (v.getId()) {

                                    case R.id.confirm:
                                        dialog.dismiss();
                                        break;
                                }

                            }
                        }, null);

                    }

                    break;
                default:
                    break;
            }

        }

    };

    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
            notifyDlg.hideNegButton();
        }
        if (!notifyDlg.isShowing())
            notifyDlg.show(msg, error);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {

            case R.id.toggle_led:
                Utils.disableView(mLedBtn);
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.LED));
                msgCidGetSetParent.led = isChecked ? 1 : 0;
                submitSet(ClientConstants.FLAG_LED);
                break;
            case R.id.toggle_mobile_data:
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.MOBILE_DATA));
                msgCidGetSetParent.isMobile = isChecked ? 1 : 0;
                submitSet(ClientConstants.FLAG_ISMOBILE);
                break;
            case R.id.toggle_hz:
                Utils.disableView(mHz);
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.HZ_VOTE));
                msgCidGetSetParent.isNTSC = isChecked ? 1 : 0;
                submitSet(ClientConstants.FLAG_ISNTSC);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == TO_SET_WIFI) {
            Intent intent = getIntent();
            intent.putExtra("handler", "unbindcid");
            setResult(RESULT_OK, intent);
            finish();
        } else if (requestCode == TO_SET_DIRECTION) {

            boolean isVoiceChange = true;
            if (msgCidGetSetParent.direction == data.getIntExtra(ClientConstants.SELECT_INDEX, msgCidGetSetParent.direction)) {
                isVoiceChange = false;
            }
            msgCidGetSetParent.direction = data.getIntExtra(ClientConstants.SELECT_INDEX, msgCidGetSetParent.direction);
            mDirctionView.setText(directions[msgCidGetSetParent.direction]);
            if (isVoiceChange) {
                submitSet(ClientConstants.FLAG_VIDEO_DIRECTION);
            }
        } else if (requestCode == TO_SET_TIMEZONE) {

            boolean isVoiceChange = true;
            int index = data.getIntExtra(ClientConstants.K_TIMEZONE, -1);
            TimeZoneBean tzb;
            if (index != -1 && mList != null && msgCidGetSetParent != null) {
                tzb = mList.get(index);
                if (msgCidGetSetParent.timezonestr.equals(tzb.getTimezoneid())) {
                    isVoiceChange = false;
                }
                msgCidGetSetParent.timezonestr = tzb.getTimezoneid();
                mTimeZoneView.setText(tzb.getTimezonename());

                DEFAULT_TIMEZONE = data.getIntExtra(ClientConstants.K_TIMEZONE, -1);
                if (isVoiceChange) {
                    submitSet(ClientConstants.FLAG_TIMEZONE);
                }
            }

        } else if (requestCode == TO_SET_VIDEO_MODEL) {
            boolean isVoiceChange = true;
            int tran = data.getIntExtra(ClientConstants.K_VIDEO_MODEL, msgCidGetSetParent.auto_record);
            int enable = data.getIntExtra(ClientConstants.K_ENABLE, msgCidGetSetParent.warn_enable);
            if (msgCidGetSetParent.auto_record == tran && enable == msgCidGetSetParent.warn_enable) {
                isVoiceChange = false;
            }
            msgCidGetSetParent.auto_record = tran;
            msgCidGetSetParent.warn_enable = enable;
            setAutoReco(tran);
            if (isVoiceChange) {
                submitSet(ClientConstants.FLAG_AUTO_RECORD);
            }
        } else if (requestCode == TO_SET_VIDEO_POSITON) {
            boolean isVoiceChange = true;
            int tran = data.getIntExtra(ClientConstants.SELECT_INDEX, msgCidGetSetParent.location);
            if (msgCidGetSetParent.location == tran) {
                isVoiceChange = false;
                PreferenceUtil.setLocationisChange(this, false);
            } else {
                PreferenceUtil.setLocationisChange(this, true);
            }
            mPositionView.setText(MsgCidlistRsp.getInstance().getSceneName(tran));
            msgCidGetSetParent.location = tran;
            if (isVoiceChange) {
                submitSet(ClientConstants.FLAG_LOCATION);
            }
        }

    }

    private void submitSet(int flag) {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(DeviceSettingActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        MsgCidSetReq msgCidSetReq = new MsgCidSetReq();
        msgCidSetReq.cid = mData.cid;
        msgCidSetReq.warn_enable = (flag == ClientConstants.FLAG_AUTO_RECORD ? msgCidGetSetParent.warn_enable : Constants.DEFAULT_VALUE);
        msgCidSetReq.warn_begin_time = Constants.DEFAULT_VALUE;
        msgCidSetReq.warn_end_time = Constants.DEFAULT_VALUE;
        msgCidSetReq.warn_week = Constants.DEFAULT_VALUE;
        msgCidSetReq.led = (flag == ClientConstants.FLAG_LED ? msgCidGetSetParent.led : Constants.DEFAULT_VALUE);
        msgCidSetReq.sound = Constants.DEFAULT_VALUE;
        msgCidSetReq.direction = (flag == ClientConstants.FLAG_VIDEO_DIRECTION ? msgCidGetSetParent.direction : Constants.DEFAULT_VALUE);
        msgCidSetReq.timezonestr = (flag == ClientConstants.FLAG_TIMEZONE ? msgCidGetSetParent.timezonestr : "");
        msgCidSetReq.sound_long = Constants.DEFAULT_VALUE;
        msgCidSetReq.sensitivity = Constants.DEFAULT_VALUE;
        msgCidSetReq.auto_record = (flag == ClientConstants.FLAG_AUTO_RECORD ? msgCidGetSetParent.auto_record : Constants.DEFAULT_VALUE);
        msgCidSetReq.location = (flag == ClientConstants.FLAG_LOCATION ? msgCidGetSetParent.location : Constants.DEFAULT_VALUE);
        msgCidSetReq.isMobile = (flag == ClientConstants.FLAG_ISMOBILE ? msgCidGetSetParent.isMobile : Constants.DEFAULT_VALUE);
        msgCidSetReq.isNTSC = (flag == ClientConstants.FLAG_ISNTSC ? msgCidGetSetParent.isNTSC : Constants.DEFAULT_VALUE);
        MyApp.wsRequest(msgCidSetReq.toBytes());
        DswLog.i("send MsgCidSetReq-->" + msgCidSetReq.toString());
    }

    @Override
    protected void onDestroy() {
        try {
            if (mBroadcastReceiver != null)
                unregisterReceiver(mBroadcastReceiver);
            mHandler.removeCallbacksAndMessages(null);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        } catch (Throwable e) {
        }
        super.onDestroy();
    }

    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(DeviceSettingActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
    }

    private void setAutoReco(int tran) {
        String str = "";
        switch (tran) {
            case ClientConstants.AUTO_RECORD1:
                str = getString(R.string.RECORD_MODE);
                break;
            case ClientConstants.AUTO_RECORD2:
                str = getString(R.string.RECORD_MODE_1);
                break;
            case ClientConstants.AUTO_RECORD3:
                str = getString(R.string.RECORD_MODE_2);
                break;

            default:
                break;
        }
        mAutomaticVideoView.setText(str);
        if ((MsgCidlistRsp.getInstance().isSomeoneMode(mData.cid, MsgSceneData.MODE_HOME_IN) || msgCidGetSetParent.warn_enable == ClientConstants.WARN_UNENABLE) && tran == ClientConstants.AUTO_RECORD1) {
            mAutomaticVideoView.setText("");
        }
        initSDState();
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                    Log.d("big", "SCAN_RESULTS_AVAILABLE_ACTION");
                    if (isClick) {
                        isClick = false;
                        Boolean is = isSameNet() || isExistAp();
                        Message msg = mHandler.obtainMessage();
                        msg.what = 0;
                        msg.obj = is;
                        mHandler.sendMessage(msg);
                    }

                }
            } catch (Exception e) {
                DswLog.ex(e.toString());
            }
        }

    }


    private void getHasMobile() {
        MsgClientHasMobileReq hasMobileReq = new MsgClientHasMobileReq(mData.cid);
        MyApp.wsRequest(hasMobileReq.toBytes());

    }
}
