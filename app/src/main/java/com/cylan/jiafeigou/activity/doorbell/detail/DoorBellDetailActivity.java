package com.cylan.jiafeigou.activity.doorbell.detail;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.doorbell.DoorBellActivity;
import com.cylan.jiafeigou.activity.doorbell.setwifi.SetDoorBellWifi;
import com.cylan.jiafeigou.activity.main.RenameDialog;
import com.cylan.jiafeigou.activity.video.NearSharedActivity;
import com.cylan.jiafeigou.activity.video.setting.LocationActivity;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.RequestMessage;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgCidGetSetParent;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOffline;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOnline;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgCidGetReq;
import com.cylan.jiafeigou.entity.msg.req.MsgCidSetReq;
import com.cylan.jiafeigou.entity.msg.req.MsgClientBellCallDeleteReq;
import com.cylan.jiafeigou.entity.msg.req.MsgSetCidAliasReq;
import com.cylan.jiafeigou.entity.msg.rsp.LoginRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgSetCidAliasRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgUnbindCidRsp;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.tencent.stat.StatService;

public class DoorBellDetailActivity extends BaseActivity implements
        OnClickListener {

    // onActivityResult
    public final static int TO_SET_WIFI = 0x01;
    public final static int TO_SET_DOORBELL_POSITON = 0x02;

    private final static String MTATAG = "DoorBellSetting";


    private TextView mNameView;
    private TextView mWifiView;
    private TextView mVisionView;
    private TextView mLocationValue;
    private NotifyDialog notifyDlg;
    private MsgCidData info;

    private Dialog mDelDialog;

    private Dialog mClearDialog;

    private MsgCidGetSetParent msgCidGetSetParent;

    private RenameDialog mRenameDialog = null;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doorbell_setting);
        setTitle(R.string.SETTINGS_1);
        info = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);

        LinearLayout mRelativeLayoutDeviceName = (LinearLayout) findViewById(R.id.doorbell_devicename);
        mRelativeLayoutDeviceName.setOnClickListener(this);
        LinearLayout mRelativeLayoutDeviceWifiInfo = (LinearLayout) findViewById(R.id.doorbell_wificonfiguration);
        mRelativeLayoutDeviceWifiInfo.setOnClickListener(this);
        LinearLayout mRelativeLayoutDeviceInfo = (LinearLayout) findViewById(R.id.doorbell_deviceinfo);
        mRelativeLayoutDeviceInfo.setOnClickListener(this);
        TextView mRelativeLayoutDeviceVol = (TextView) findViewById(R.id.doorbell_setting_voice_layout);
        mRelativeLayoutDeviceVol.setOnClickListener(this);
        LinearLayout mRelativeLayoutDevicePostion = (LinearLayout) findViewById(R.id.doorbell_location_layout);
        mRelativeLayoutDevicePostion.setOnClickListener(this);

        TextView mNearShareView = (TextView) findViewById(R.id.doorbell_setting_nearshare);
        mNearShareView.setOnClickListener(this);

        mNameView = (TextView) findViewById(R.id.name);
        mWifiView = (TextView) findViewById(R.id.wifiname);
        mVisionView = (TextView) findViewById(R.id.vision);
        mLocationValue = (TextView) findViewById(R.id.doorbell_location_value);

        if (info != null) {
            mNameView.setText(info.mName());
            mWifiView.setText(info.name);
            mVisionView.setText(info.version);
        }

        initData();
        if (msgCidGetSetParent != null) {
            mLocationValue.setText(MsgCidlistRsp.getInstance().getSceneName(msgCidGetSetParent.location));
        }
        getConfig();

        Button mUnBindBtn = (Button) findViewById(R.id.delete_device);
        mUnBindBtn.setOnClickListener(this);
        TextView mClearBtn = (TextView) findViewById(R.id.doorbell_setting_clear_record);
        mClearBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.doorbell_deviceinfo:
                startActivity(new Intent(DoorBellDetailActivity.this,
                        DoorBellDeviceInfoActivity.class).putExtra(ClientConstants.CIDINFO, info));
                break;
            case R.id.doorbell_devicename:
                rename();
                break;
            case R.id.doorbell_wificonfiguration:
                startActivityForResult(new Intent(DoorBellDetailActivity.this, SetDoorBellWifi.class).putExtra(ClientConstants.CIDINFO, info), TO_SET_WIFI);
                break;
            case R.id.delete_device:
                showUnBindDialog();
                break;
            case R.id.doorbell_setting_clear_record:
                showClearDialog();
                break;
            case R.id.doorbell_location_layout:
                if (msgCidGetSetParent != null && info != null) {
                    startActivityForResult(new Intent(this, LocationActivity.class).putExtra(ClientConstants.SELECT_INDEX, msgCidGetSetParent.location), TO_SET_DOORBELL_POSITON);
                }
                break;
            case R.id.doorbell_setting_nearshare:
                StatService.trackCustomEvent(this, MTATAG, getString(R.string.SHARE));
                startActivity(new Intent(DoorBellDetailActivity.this, NearSharedActivity.class).putExtra(ClientConstants.CIDINFO, info));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == TO_SET_DOORBELL_POSITON) {
            boolean isLocationChange = true;
            int tran = data.getIntExtra(ClientConstants.SELECT_INDEX, msgCidGetSetParent.location);
            if (msgCidGetSetParent.location == tran) {
                isLocationChange = false;
                PreferenceUtil.setLocationisChange(this, false);
            } else {
                PreferenceUtil.setLocationisChange(this, true);
            }
            mLocationValue.setText(MsgCidlistRsp.getInstance().getSceneName(tran));
            msgCidGetSetParent.location = tran;
            if (isLocationChange) {
                summitSet(ClientConstants.FLAG_LOCATION);
            }
        }
    }

    private void initData() {
        String key = CacheUtil.getMSG_VIDEO_CONFIG_KEY(info.cid);
        msgCidGetSetParent = (MsgCidGetSetParent) CacheUtil.readObject(key);
    }

    private void getConfig() {
        if (msgCidGetSetParent == null) {
            getWarmConfig();
        } else {
            if (info.vid > msgCidGetSetParent.vid) {
                getWarmConfig();
            } else {
                onSuc(msgCidGetSetParent);
            }
        }
    }

    private void getWarmConfig() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(DoorBellDetailActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        MsgCidGetReq msgCidGetReq = new MsgCidGetReq();
        msgCidGetReq.cid = info.cid;
        MyApp.wsRequest(msgCidGetReq.toBytes());
        DswLog.i("send MsgCidGetReq msg-->" + msgCidGetReq.toString());
    }

    private void onSuc(MsgCidGetSetParent msgCidGetSetParent) {
        this.msgCidGetSetParent = msgCidGetSetParent;
        mLocationValue.setText(MsgCidlistRsp.getInstance().getSceneName(this.msgCidGetSetParent.location));
    }

    private void summitSet(int flag) {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(DoorBellDetailActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        MsgCidSetReq msgCidSetReq = new MsgCidSetReq();
        msgCidSetReq.cid = info.cid;
        msgCidSetReq.warn_enable = (flag == ClientConstants.FLAG_AUTO_RECORD ? msgCidGetSetParent.warn_enable : Constants.DEFAULT_VALUE);
        msgCidSetReq.warn_begin_time = Constants.DEFAULT_VALUE;
        msgCidSetReq.warn_end_time = Constants.DEFAULT_VALUE;
        msgCidSetReq.warn_week = Constants.DEFAULT_VALUE;
        msgCidSetReq.led = (flag == ClientConstants.FLAG_LED ? msgCidGetSetParent.led : Constants.DEFAULT_VALUE);
        msgCidSetReq.sound = Constants.DEFAULT_VALUE;
        msgCidSetReq.direction = (flag == ClientConstants.FLAG_VIDEO_DIRECTION ? msgCidGetSetParent.direction : Constants.DEFAULT_VALUE);
        msgCidSetReq.timezonestr = (flag == ClientConstants.FLAG_TIMEZONE ? msgCidGetSetParent.timezonestr : "");
        msgCidSetReq.sound_long = Constants.DEFAULT_VALUE;
        msgCidSetReq.auto_record = (flag == ClientConstants.FLAG_AUTO_RECORD ? msgCidGetSetParent.auto_record : Constants.DEFAULT_VALUE);
        msgCidSetReq.location = (flag == ClientConstants.FLAG_LOCATION ? msgCidGetSetParent.location : Constants.DEFAULT_VALUE);
        MyApp.wsRequest(msgCidSetReq.toBytes());
        DswLog.i("send MsgCidSetReq-->" + msgCidSetReq.toString());
    }

    private void rename() {
        if (mRenameDialog != null)
            mRenameDialog = null;
        mRenameDialog = new RenameDialog(this, info.mName());

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

    private void renameRequest(String name) {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(DoorBellDetailActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }

        MsgSetCidAliasReq msgSetCidAliasReq = new MsgSetCidAliasReq();
        msgSetCidAliasReq.cid = info.cid;
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
                    if (info.cid.equals(msgSetCidAliasRsp.cid)) {
                        info.alias = msgSetCidAliasRsp.alias;
                        mNameView.setText(msgSetCidAliasRsp.alias);
                    }
                } else {
                    ToastUtil.showFailToast(DoorBellDetailActivity.this, rspMsgHeader.msg);
                }
                Intent intent = getIntent();
                intent.putExtra("handler", "alias");
                intent.putExtra("alias", info.mName());
                setResult(RESULT_OK, intent);

            } else if (MsgpackMsg.CLIENT_UNBINDCID_RSP == msgpackMsg.msgId) {
                RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == rspMsgHeader.ret) {
                    MsgUnbindCidRsp msgUnbindCidRsp = (MsgUnbindCidRsp) msgpackMsg;
                    if (msgUnbindCidRsp.cid.equals(info.cid)) {
                        AppManager.getAppManager().finishActivity(DoorBellDetailActivity.class);
                        AppManager.getAppManager().finishActivity(DoorBellActivity.class);
                    }
                } else {
                    showNotify(rspMsgHeader.msg, rspMsgHeader.ret);
                }

            } else if (MsgpackMsg.CLIENT_SYNC_CIDONLINE == msgpackMsg.msgId) {
                MsgSyncCidOnline msgSyncCidOnline = (MsgSyncCidOnline) msgpackMsg;
                if (info != null) {
                    if (info.cid.equals(msgSyncCidOnline.cid)) {
                        info.net = msgSyncCidOnline.net;
                        info.name = msgSyncCidOnline.name;
                        if (!StringUtils.isEmptyOrNull(msgSyncCidOnline.version))
                            info.version = msgSyncCidOnline.version;
                        mWifiView.setText(info.name);
                        mVisionView.setText(info.version);

                    }
                }
            } else if (MsgpackMsg.CLIENT_SYNC_CIDOFFLINE == msgpackMsg.msgId) {
                MsgSyncCidOffline msgSyncCidOffline = (MsgSyncCidOffline) msgpackMsg;
                if (info != null) {
                    if (info.cid.equals(msgSyncCidOffline.cid)) {
                        info.net = 0;
                        info.name = "";
                        mWifiView.setText(info.name);

                    }
                }
            } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_BELL_CALL_DELETE_RSP) {
                RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (mRspMsgHeader.caller.equals(info.cid)) {
                    mProgressDialog.dismissDialog();
                    if (Constants.RETOK != mRspMsgHeader.ret) {
                        ToastUtil.showFailToast(this, mRspMsgHeader.msg);
                    }
                }
            } else if (MsgpackMsg.CLIENT_CIDGET_RSP == msgpackMsg.msgId) {
                RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == rspMsgHeader.ret) {
                    MsgCidGetSetParent msgCidGetRsp = (MsgCidGetSetParent) msgpackMsg;
                    if (msgCidGetRsp.cid.equals(info.cid)) {
                        onSuc(msgCidGetRsp);
                    }
                } else {
                    ToastUtil.showFailToast(this.getApplicationContext(), rspMsgHeader.msg);
                }
            } else if (MsgpackMsg.CLIENT_CIDSET_RSP == msgpackMsg.msgId) {
                RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (rspMsgHeader.ret == -1 || rspMsgHeader.ret == 1) {
                    ToastUtil.showToast(DoorBellDetailActivity.this, rspMsgHeader.msg);
                } else {
                    if (Constants.RETOK == rspMsgHeader.ret) {
                        MsgCidGetSetParent msgCidSetRsp = (MsgCidGetSetParent) msgpackMsg;
                        if (msgCidSetRsp.cid.equals(info.cid)) {
                            info.vid = msgCidSetRsp.vid;
                            onSuc(msgCidSetRsp);
                            ToastUtil.showSuccessToast(DoorBellDetailActivity.this, getString(R.string.PWD_OK_2));
                        }
                    } else {
                        ToastUtil.showFailToast(DoorBellDetailActivity.this, rspMsgHeader.msg);
                    }
                }

            } else if (MsgpackMsg.CLIENT_RELOGIN_RSP == msgpackMsg.msgId) {
                RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (mRspMsgHeader.ret == Constants.RETOK) {
                    LoginRsp rsp = (LoginRsp) mRspMsgHeader;
                    int sceneSize = rsp.data.size();
                    boolean has = false;
                    for (int i = 0; i < sceneSize; i++) {
                        int deviceSize = rsp.data.get(i).data.size();
                        for (int j = 0; j < deviceSize; j++) {
                            MsgCidData mcd = rsp.data.get(i).data.get(j);
                            if (mcd.cid.equals(info.cid)) {
                                info.net = mcd.net;
                                info.name = mcd.name;
                                info.version = mcd.version;
                                mWifiView.setText(info.name);
                                has = true;
                                break;
                            }
                        }
                        if (has)
                            break;
                    }
                }

            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }


    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showToast(DoorBellDetailActivity.this, getString(R.string.GLOBAL_NO_NETWORK), Gravity.CENTER, 3000);
        }
    }

    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
            notifyDlg.hideNegButton();
        }
        if (!notifyDlg.isShowing())
            notifyDlg.show(msg, error);
    }

    private void showUnBindDialog() {

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
                    final NotifyDialog dialog = new NotifyDialog(DoorBellDetailActivity.this);
                    dialog.setButtonText(R.string.DELETE_CID, R.string.CANCEL);
                    dialog.setPosRedTheme(R.drawable.bg_dialogdel_selector, getResources().getColor(R.color.mycount_not_set));
                    dialog.show(String.format(getString(R.string.SURE_DELETE_1), info.mName()), new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {

                                case R.id.confirm:
                                    dialog.dismiss();
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


    private void httpDelCids() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(DoorBellDetailActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        mProgressDialog.showDialog(getString(R.string.DELETEING));
        MyApp.wsRequest(RequestMessage.getMsgUnbindCidReq(info.cid).toBytes());
    }


    private void showClearDialog() {
        try {
            if (mClearDialog == null) {
                mClearDialog = new Dialog(this, R.style.func_dialog);
                View content = View.inflate(this, R.layout.dialog_logout, null);
                TextView cancel = (TextView) content.findViewById(R.id.btn_cancel);
                cancel.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mClearDialog.dismiss();
                    }
                });
                Button btn = (Button) content.findViewById(R.id.logout_confirm);
                btn.setText(R.string.DOOR_CLEAR_ALL);
                btn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StatService.trackCustomEvent(DoorBellDetailActivity.this, MTATAG,
                                getString(R.string.DOOR_CLEAR_REOCRD));
                        mClearDialog.dismiss();
                        clear();

                    }
                });
                Button title = (Button) content.findViewById(R.id.btn_pick_photo);
                title.setText(R.string.MSG_DELETE_TITLE);
                mClearDialog.setContentView(content);
                mClearDialog.setCanceledOnTouchOutside(true);

            }

            mClearDialog.show();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    private void clear() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(DoorBellDetailActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        mProgressDialog.showDialog(R.string.DELETEING);
        MsgClientBellCallDeleteReq mMsgClientBellCallDeleteReq = new MsgClientBellCallDeleteReq(info.cid);
        mMsgClientBellCallDeleteReq.timeBegin = 0;
        MyApp.wsRequest(mMsgClientBellCallDeleteReq.toBytes());
    }


}
