package com.cylan.jiafeigou.activity.efamily;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.efamily.main.EfamilyMainActivity;
import com.cylan.jiafeigou.activity.main.RenameDialog;
import com.cylan.jiafeigou.activity.video.NearSharedActivity;
import com.cylan.jiafeigou.activity.video.setting.LocationActivity;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.RequestMessage;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgEfamlGetSetAlarmParent;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgEfamilyGetAlarmReq;
import com.cylan.jiafeigou.entity.msg.req.MsgEfamilySetAlarmReq;
import com.cylan.jiafeigou.entity.msg.req.MsgSetCidAliasReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgSetCidAliasRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgUnbindCidRsp;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;

public class EFamilySettingActivity extends BaseActivity implements OnClickListener {

    public static String EXT_DATA = "AlarmInfo";

    // onActivityResult
    private static int TO_SET_VIDEO_POSITON = 0x01;


    private RelativeLayout mDeviceName;
    private RelativeLayout mPositionLayout;
    private RelativeLayout mDeviceMsg;
    private Button mUnbindBtn;

    private TextView mNameView;
    private TextView mPositionView;
    private TextView mMsgView;
    private TextView mShareView;
    private TextView mRelatedBellView;
    private TextView mClearRecordView;

    private MsgCidData cidData;
    private MsgEfamlGetSetAlarmParent mMsgEfamlGetSetAlarmParent;

    private NotifyDialog notifyDlg;

    private String key;

    private Dialog mDelDialog = null;

    private Dialog mClearDialog = null;
    private RenameDialog mRenameDialog=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efamily_video_set);

        cidData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        setTitle(R.string.SETTINGS_1);

        initView();

        key = CacheUtil.getMSG_EFAMILY_CONFIG_KEY(cidData.cid);
        initDate();

    }


    private void initView() {
        mDeviceName = (RelativeLayout) findViewById(R.id.devicename);
        mPositionLayout = (RelativeLayout) findViewById(R.id.position);
        mDeviceMsg = (RelativeLayout) findViewById(R.id.devicemsg);
        mUnbindBtn = (Button) findViewById(R.id.delete_device);
        mDeviceName.setOnClickListener(this);
        mPositionLayout.setOnClickListener(this);
        mDeviceMsg.setOnClickListener(this);
        mUnbindBtn.setOnClickListener(this);

        mNameView = (TextView) findViewById(R.id.efamily_name);
        mPositionView = (TextView) findViewById(R.id.positionname);
        mMsgView = (TextView) findViewById(R.id.efamily_msg);
        mShareView = (TextView) findViewById(R.id.efamily_setting_nearshare);
        mRelatedBellView = (TextView) findViewById(R.id.efamily_setting_related_bell);
        mShareView.setOnClickListener(this);
        mRelatedBellView.setOnClickListener(this);
        mClearRecordView = (TextView) findViewById(R.id.efamily_setting_clear_record);
        mClearRecordView.setOnClickListener(this);


        mNameView.setText(cidData.mName());
        mMsgView.setText(cidData.version);
    }

    private void initDate() {
        MsgEfamlGetSetAlarmParent msgEfamlGetSetAlarmParent = (MsgEfamlGetSetAlarmParent) CacheUtil.readObject(key);
        if (msgEfamlGetSetAlarmParent == null) {
            getWarmConfig();
        } else {
            if (cidData.vid > msgEfamlGetSetAlarmParent.vid) {
                getWarmConfig();
            } else {
                mMsgEfamlGetSetAlarmParent = msgEfamlGetSetAlarmParent;
                mPositionView.setText(MsgCidlistRsp.getInstance().getSceneName(mMsgEfamlGetSetAlarmParent.location));
            }
        }
    }

    private void getWarmConfig() {

        if (!MyApp.getIsLogin()) {
            ToastUtil.showToast(EFamilySettingActivity.this, getString(R.string.GLOBAL_NO_NETWORK), Gravity.CENTER, 3000);
            return;
        }
        MsgEfamilyGetAlarmReq msgEfamilyGetAlarmReq = new MsgEfamilyGetAlarmReq(cidData.cid);
        DswLog.i("send MsgEfamilyGetAlarmReq msg-->" + msgEfamilyGetAlarmReq.toString());
        MyApp.wsRequest(msgEfamilyGetAlarmReq.toBytes());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                finish();
                break;
            case R.id.devicename:
                rename();
                break;
            case R.id.devicemsg:
                startActivity(new Intent(EFamilySettingActivity.this, EfamilyDeviceState.class).putExtra(ClientConstants.CIDINFO, cidData));
                break;
            case R.id.delete_device:
                showLogoutDialog();
                break;
            case R.id.position:
                if (mMsgEfamlGetSetAlarmParent == null)
                    return;
                startActivityForResult(
                        new Intent(this, LocationActivity.class).putExtra(ClientConstants.SELECT_INDEX, mMsgEfamlGetSetAlarmParent.location),
                        TO_SET_VIDEO_POSITON);

                break;
            case R.id.efamily_setting_nearshare:
                startActivity(new Intent(this, NearSharedActivity.class).putExtra(ClientConstants.CIDINFO, cidData));
                break;
            case R.id.efamily_setting_related_bell:
                startActivity(new Intent(this, EfamilyRelatedBellActivity.class).putExtra(ClientConstants.CIDINFO, cidData));

                break;
            case R.id.efamily_setting_clear_record:
                showClearDialog();
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
                    final NotifyDialog dialog = new NotifyDialog(EFamilySettingActivity.this);
                    dialog.setButtonText(R.string.DELETE_CID, R.string.CANCEL);
                    dialog.setPosRedTheme(R.drawable.bg_dialogdel_selector, getResources().getColor(R.color.mycount_not_set));
                    dialog.show(String.format(getString(R.string.SURE_DELETE_1), cidData.mName()), new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            switch (v.getId()) {

                                case R.id.confirm:
                                    dialog.dismiss();
                                    sendDelCidMsg();
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

    void sendDelCidMsg() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showToast(EFamilySettingActivity.this, getString(R.string.GLOBAL_NO_NETWORK), Gravity.CENTER, 3000);
            return;
        }
        mProgressDialog.showDialog(R.string.DELETEING);
        MyApp.wsRequest(RequestMessage.getMsgUnbindCidReq(cidData.cid).toBytes());

    }

    private void rename() {
        if (mRenameDialog != null)
            mRenameDialog = null;
        mRenameDialog = new RenameDialog(this, cidData.mName());

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
            ToastUtil.showFailToast(EFamilySettingActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        MsgSetCidAliasReq msgSetCidAliasReq = new MsgSetCidAliasReq();
        msgSetCidAliasReq.cid = cidData.cid;
        msgSetCidAliasReq.alias = name;
        MyApp.wsRequest(msgSetCidAliasReq.toBytes());
        DswLog.i("send MsgSetCidAliasReq msg-->" + msgSetCidAliasReq.toString());
        mProgressDialog.showDialog(getString(R.string.rename));
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        try {
            if (MsgpackMsg.CLIENT_SETCIDALIAS_RSP == msgpackMsg.msgId) {
                RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
                mProgressDialog.dismissDialog();
                if (Constants.RETOK == mRspMsgHeader.ret) {
                    MsgSetCidAliasRsp msgSetCidAliasRsp = (MsgSetCidAliasRsp) msgpackMsg;
                    if (msgSetCidAliasRsp.cid.equals(cidData.cid)) {
                        cidData.alias = msgSetCidAliasRsp.alias;
                        mNameView.setText(msgSetCidAliasRsp.alias);
                    }
                } else {
                    ToastUtil.showFailToast(EFamilySettingActivity.this, mRspMsgHeader.msg);
                }
                Intent intent = getIntent();
                intent.putExtra("handler", "alias");
                intent.putExtra("alias", cidData.mName());
                setResult(RESULT_OK, intent);
            } else if (MsgpackMsg.CLIENT_EFAML_GET_ALARM_RSP == msgpackMsg.msgId) {
                RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == mRspMsgHeader.ret) {
                    MsgEfamlGetSetAlarmParent msgEfamilyGetAlarmRsp = (MsgEfamlGetSetAlarmParent) msgpackMsg;
                    if (msgEfamilyGetAlarmRsp.caller.equals(cidData.cid)) {
                        CacheUtil.saveObject(msgEfamilyGetAlarmRsp, key);
                        mMsgEfamlGetSetAlarmParent = msgEfamilyGetAlarmRsp;
                        mPositionView.setText(MsgCidlistRsp.getInstance().getSceneName(mMsgEfamlGetSetAlarmParent.location));
                    }
                } else {
                    onError(mRspMsgHeader.msg);
                }
            } else if (MsgpackMsg.CLIENT_UNBINDCID_RSP == msgpackMsg.msgId) {
                mProgressDialog.dismissDialog();
                RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == mRspMsgHeader.ret) {
                    MsgUnbindCidRsp msgUnbindCidRsp = (MsgUnbindCidRsp) msgpackMsg;
                    if (msgUnbindCidRsp.cid.equals(cidData.cid)) {
                        AppManager.getAppManager().finishActivity(EFamilySettingActivity.class);
                        AppManager.getAppManager().finishActivity(EfamilyMainActivity.class);
                    }
                } else {
                    showNotify(mRspMsgHeader.msg, mRspMsgHeader.ret);
                }
            } else if (MsgpackMsg.CLIENT_EFAML_SET_ALARM_RSP == msgpackMsg.msgId) {

                RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
                mProgressDialog.dismissDialog();
                if (rspMsgHeader.ret == -1 || rspMsgHeader.ret == 1) {
                    ToastUtil.showToast(this, rspMsgHeader.msg);
                } else {
                    if (Constants.RETOK == rspMsgHeader.ret) {
                        MsgEfamlGetSetAlarmParent msgEfamilySetAlarmRsp = (MsgEfamlGetSetAlarmParent) msgpackMsg;
                        if (msgEfamilySetAlarmRsp.caller.equals(cidData.cid)) {
                            cidData.vid = msgEfamilySetAlarmRsp.vid;
                            CacheUtil.saveObject(msgEfamilySetAlarmRsp, key);
                            ToastUtil.showSuccessToast(this, getString(R.string.PWD_OK_2));
                        }
                    } else {
                        ToastUtil.showFailToast(this, rspMsgHeader.msg);
                    }
                }
            } else if (MsgpackMsg.CLIENT_EFAML_SET_BELL_RSP == msgpackMsg.msgId) {
                RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (rspMsgHeader.caller.equals(cidData.cid)) {
                    if (Constants.RETOK == rspMsgHeader.ret) {
                        ToastUtil.showSuccessToast(this, getString(R.string.PWD_OK_2));
                    } else {
                        ToastUtil.showToast(this, rspMsgHeader.msg);
                    }
                }
            } else if (MsgpackMsg.CLIENT_DEL_EFAML_MSG_RSP == msgpackMsg.msgId) {
                RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (mRspMsgHeader.caller.equals(cidData.cid)) {
                    mProgressDialog.dismissDialog();
                    if (Constants.RETOK != mRspMsgHeader.ret) {
                        ToastUtil.showFailToast(this, mRspMsgHeader.msg);
                    }
                }
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    public void onError(String msg) {
        mProgressDialog.dismissDialog();
        ToastUtil.showFailToast(this, msg);
    }

    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
            notifyDlg.hideNegButton();
        }
        if (!notifyDlg.isShowing())
            notifyDlg.show(msg, error);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == TO_SET_VIDEO_POSITON) {
            boolean isVoiceChange = true;
            if (mMsgEfamlGetSetAlarmParent == null)
                return;
            int tran = data.getIntExtra(ClientConstants.SELECT_INDEX, mMsgEfamlGetSetAlarmParent.location);
            if (mMsgEfamlGetSetAlarmParent.location == tran) {
                isVoiceChange = false;
                PreferenceUtil.setLocationisChange(this, false);
            } else {
                PreferenceUtil.setLocationisChange(this, true);
            }
            mPositionView.setText(MsgCidlistRsp.getInstance().getSceneName(tran));
            mMsgEfamlGetSetAlarmParent.location = tran;
            if (isVoiceChange) {
                submitSet();
            }
        }
    }

    private void submitSet() {

        MsgEfamilySetAlarmReq msgEfamilySetAlarmReq = new MsgEfamilySetAlarmReq(cidData.cid);
        msgEfamilySetAlarmReq.warn_begin_time = Constants.DEFAULT_VALUE;
        msgEfamilySetAlarmReq.warn_end_time = Constants.DEFAULT_VALUE;
        msgEfamilySetAlarmReq.warn_week = Constants.DEFAULT_VALUE;
        msgEfamilySetAlarmReq.data = new ArrayList<>();
        msgEfamilySetAlarmReq.location = mMsgEfamlGetSetAlarmParent.location;
        DswLog.i("send MsgEfamilySetAlarmReq msg-->" + msgEfamilySetAlarmReq.toString());
        MyApp.wsRequest(msgEfamilySetAlarmReq.toBytes());

    }

    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(EFamilySettingActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }

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
            ToastUtil.showFailToast(EFamilySettingActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        mProgressDialog.showDialog(R.string.DELETEING);
        MsgpackMsg.MsgHeader mMsgClientBellCallDeleteReq = new MsgpackMsg.MsgHeader();
        mMsgClientBellCallDeleteReq.callee = cidData.cid;
        mMsgClientBellCallDeleteReq.msgId = MsgpackMsg.CLIENT_DEL_EFAML_MSG_REQ;
        MyApp.wsRequest(mMsgClientBellCallDeleteReq.toBytes());
    }


}