package com.cylan.jiafeigou.activity.efamily.magnetic;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.main.RenameDialog;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgCidGetSetParent;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgClientMagGetWarnReq;
import com.cylan.jiafeigou.entity.msg.req.MsgClientMagSetWarnReq;
import com.cylan.jiafeigou.entity.msg.req.MsgSetCidAliasReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientMagGetWarnRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgSetCidAliasRsp;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.ProgressDialogUtil;
import com.cylan.jiafeigou.utils.ToastUtil;

/**
 * Created by yangc on 2015/12/14.
 *
 */
public class MagneticSetActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    private MsgCidData info;

    private ToggleButton btn_magnetic;
    private TextView name;
    private TextView deviceInfo;
    private ProgressDialogUtil mProgressDlg;

    private MsgCidGetSetParent msgCidGetSetParent;
    private RenameDialog mRenameDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetic_set);
        setTitle(R.string.SETTINGS_1);
        info = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        initView();
        initData();
    }

    private void initView(){
        LinearLayout deviceNameLayout = (LinearLayout) findViewById(R.id.magnetic_devicename);
        deviceNameLayout.setOnClickListener(this);
        LinearLayout deviceInfoLayout = (LinearLayout) findViewById(R.id.magnetic_deviceinfo);
        deviceInfoLayout.setOnClickListener(this);
        name = (TextView) findViewById(R.id.magnetic_name);
        name.setText(info.mName());
        deviceInfo = (TextView) findViewById(R.id.magnetic_vision);
        deviceInfo.setText(info.version);
        btn_magnetic = (ToggleButton) findViewById(R.id.toggle_magnetic);
        btn_magnetic.setOnCheckedChangeListener(this);
        btn_magnetic.setOnClickListener(this);
        mProgressDlg = new ProgressDialogUtil(this);

        if (isInMode()){
            btn_magnetic.setChecked(false);
            btn_magnetic.setClickable(false);
            btn_magnetic.setBackgroundResource(R.drawable.ico_switch_close_unenable);
        }else  if (isOutMode()){
            btn_magnetic.setChecked(true);
            btn_magnetic.setClickable(false);
            btn_magnetic.setBackgroundResource(R.drawable.ico_switch_open_unenable);
        }
        getWarnInfo();
    }

    private void initData() {
        String key = CacheUtil.getMSG_VIDEO_CONFIG_KEY(info.cid);
        msgCidGetSetParent = (MsgCidGetSetParent) CacheUtil.readObject(key);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.magnetic_devicename:
                rename(info);
                break;
            case R.id.magnetic_deviceinfo:
                startActivity(new Intent(MagneticSetActivity.this,
                        MagneticDeviceInfoActivity.class).putExtra(ClientConstants.CIDINFO, info));
                break;
            case  R.id.toggle_magnetic:
                setWarnInfo();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.toggle_magnetic){
            PreferenceUtil.setKeyMagWarnRsp(this, isChecked);
        }
    }

    private boolean isInMode() {
        return MsgCidlistRsp.getInstance().isSomeoneMode(info.cid, MsgSceneData.MODE_HOME_IN);
    }

    private boolean isOutMode() {
        return MsgCidlistRsp.getInstance().isSomeoneMode(info.cid, MsgSceneData.MODE_HOME_OUT);
    }

    private void rename(final MsgCidData info) {
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
            ToastUtil.showFailToast(MagneticSetActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }

        MsgSetCidAliasReq msgSetCidAliasReq = new MsgSetCidAliasReq();
        msgSetCidAliasReq.cid = info.cid;
        msgSetCidAliasReq.alias = name;
        MyApp.wsRequest(msgSetCidAliasReq.toBytes());
        mProgressDlg.showDialog(getString(R.string.rename));
        DswLog.i("send MsgSetCidAliasReq msg-->" + msgSetCidAliasReq.toString());
    }
 
    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        mProgressDlg.dismissDialog();
        if (MsgpackMsg.CLIENT_SETCIDALIAS_RSP == msgpackMsg.msgId) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == rspMsgHeader.ret) {
                MsgSetCidAliasRsp msgSetCidAliasRsp = (MsgSetCidAliasRsp) msgpackMsg;
                if (info.cid.equals(msgSetCidAliasRsp.cid)) {
                    info.name = msgSetCidAliasRsp.alias;
                    name.setText(msgSetCidAliasRsp.alias);
                }
            } else {
                ToastUtil.showFailToast(MagneticSetActivity.this, rspMsgHeader.msg);
            }
        }else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_MAG_GET_WARN_RSP) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == rspMsgHeader.ret){
                MsgClientMagGetWarnRsp getWarnRsp = (MsgClientMagGetWarnRsp) rspMsgHeader;
                btn_magnetic.setChecked(getWarnRsp.warn == 1);
                PreferenceUtil.setKeyMagWarnRsp(this, getWarnRsp.warn == 1);
            }else {
                ToastUtil.showFailToast(MagneticSetActivity.this, rspMsgHeader.msg);
            }
        }else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_MAG_SET_WARN_RSP) {
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == rspMsgHeader.ret){
                ToastUtil.showSuccessToast(this, getString(R.string.PWD_OK_2));
            }
        }
    }

    @Override
    public void disconnectServer() {
        if (mProgressDlg.isShow()) {
            mProgressDlg.dismissDialog();
            ToastUtil.showToast(MagneticSetActivity.this, getString(R.string.GLOBAL_NO_NETWORK), Gravity.CENTER, 3000);
        }
    }

    private void getWarnInfo(){
        MsgClientMagGetWarnReq warnReq = new MsgClientMagGetWarnReq("", info.cid);
        JniPlay.SendBytes(warnReq.toBytes());
        Log.i("MagSet", "info-->" + warnReq.toString());
    }

    private void setWarnInfo(){
        MsgClientMagSetWarnReq warnReq = new MsgClientMagSetWarnReq("", info.cid);
        warnReq.warn = btn_magnetic.isChecked() ? 1 : 0;
        JniPlay.SendBytes(warnReq.toBytes());
        Log.i("MagSet", "info-->" + warnReq.toString());
    }
}
