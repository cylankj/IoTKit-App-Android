package com.cylan.jiafeigou.activity.main;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import cylan.log.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.AccountInfo;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgGetCodeReq;
import com.cylan.jiafeigou.entity.msg.req.MsgSetAccountinfoReq;
import com.cylan.jiafeigou.receiver.SMSBroadcastReceiver;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.EditDelText;

import java.util.Timer;
import java.util.TimerTask;

public class ModifyBindphone extends BaseActivity implements OnClickListener {

    public static final int TCP_GETCODE = 0x01;
    public static final int TCP_SET_USERINFO = 0x02;


    private NotifyDialog notifyDlg;
    private NotifyDialog mNotifyDlg;

    private EditDelText mBindphoneView;
    private EditText mCodeView;
    private Button mGetCodeBtn;
    private Button mSubmitBtn;

    private String tel;
    private String code;

    private int recLen = 180;
    private Timer timer = null;

    private AccountInfo mMsgSetAccountinfoRsp;

    private SMSBroadcastReceiver mSMSBroadcastReceiver;
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    void hideImm() {
        InputMethodManager imm = (InputMethodManager) ModifyBindphone.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBindphoneView.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                if (isValide()) {
                    TcpSend(TCP_SET_USERINFO);
                }
                break;

            case R.id.right_btn:
                tel = mBindphoneView.getText().toString().trim();

                if (StringUtils.isEmptyOrNull(tel)) {
                    notifyDlg.show(R.string.PHONE_NUMBER_2);
                    return;
                }

                if (!StringUtils.isPhoneNumber(tel)) {
                    notifyDlg.show(R.string.PHONE_NUMBER_2);
                    return;
                }

                TcpSend(TCP_SET_USERINFO);
                break;

            case R.id.ico_back:
                finish();
                break;
            case R.id.get_code:
                if (mMsgSetAccountinfoRsp == null)
                    return;
                tel = mBindphoneView.getText().toString().trim();
                if (!tel.matches(Constants.RegPhone)) {
                    notifyDlg.show(R.string.PHONE_NUMBER_2);
                    return;
                }

                if (mMsgSetAccountinfoRsp != null) {
                    if (mMsgSetAccountinfoRsp.sms_phone.equals(tel)) {
                        mNotifyDlg.setButtonText(R.string.I_KNOW, 0);
                        mNotifyDlg.show(R.string.PHONE_NUMBER_3);
                        return;
                    }
                }
                TcpSend(TCP_GETCODE);
                break;
        }
    }

    private void TcpSend(int flag) {

        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(ModifyBindphone.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }

        if (flag == TCP_SET_USERINFO) {
            MsgSetAccountinfoReq mMsgSetAccountinfoReq = new MsgSetAccountinfoReq("", "");
            mMsgSetAccountinfoReq.sms_phone = tel;
            mMsgSetAccountinfoReq.code = code;
            mMsgSetAccountinfoReq.alias = "";
            mMsgSetAccountinfoReq.push_enable = Constants.DEFAULT_VALUE;
            mMsgSetAccountinfoReq.vibrate = Constants.DEFAULT_VALUE;
            mMsgSetAccountinfoReq.sound = Constants.DEFAULT_VALUE;
            mMsgSetAccountinfoReq.email = "";
            JniPlay.SendBytes(mMsgSetAccountinfoReq.toBytes());
            DswLog.i("send MsgSetAccountinfoReq--->" + mMsgSetAccountinfoReq.toString());
        } else if (flag == TCP_GETCODE) {
            MsgGetCodeReq mMsgGetCodeReq = new MsgGetCodeReq();
            mMsgGetCodeReq.language_type = Utils.getLanguageType(this);
            mMsgGetCodeReq.account = tel;
            mMsgGetCodeReq.type = Constants.CLIENT_GETCODE_TYPE_EDIT_USERINFO;
            mMsgGetCodeReq.oem = OEMConf.getOEM();
            JniPlay.SendBytes(mMsgGetCodeReq.toBytes());
            mProgressDialog.showDialog(flag == TCP_SET_USERINFO ? getString(R.string.upload) : getString(R.string.getting));
            DswLog.i("send MsgGetCodeReq--->" + mMsgGetCodeReq.toString());
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fg_modify_bindphone);

        mMsgSetAccountinfoRsp = (AccountInfo) getIntent().getSerializableExtra("data");
        setTitle(R.string.CHANGE_PHONE_NUM);

        mBindphoneView = (EditDelText) findViewById(R.id.phone);

//        mBindphoneView.setText(mMsgSetAccountinfoRsp.sms_phone);

        mSubmitBtn = (Button) findViewById(R.id.submit);
        mSubmitBtn.setOnClickListener(this);

        mCodeView = (EditText) findViewById(R.id.code);
        mGetCodeBtn = (Button) findViewById(R.id.get_code);
        mGetCodeBtn.setOnClickListener(this);

        notifyDlg = new NotifyDialog(this);
        notifyDlg.hideNegButton();
        mNotifyDlg = new NotifyDialog(this);
        mNotifyDlg.hideNegButton();

        completeCode();
    }

    /**
     * 自动填充短信验证码
     */
    private void completeCode() {
        mSMSBroadcastReceiver = new SMSBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ACTION);
        intentFilter.setPriority(Integer.MAX_VALUE);
        this.registerReceiver(mSMSBroadcastReceiver, intentFilter);
        // 设置广播的监听
        mSMSBroadcastReceiver.setOnReceivedMessageListener(new SMSBroadcastReceiver.MessageListener() {
            @Override
            public void onReceived(String message) {
                mCodeView.setText(message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null)
            timer.cancel();
        unregisterReceiver(mSMSBroadcastReceiver);
    }

    @Override
    public void handleMsgpackMsg(int id, MsgpackMsg.MsgHeader mMsgHeader) {

        if (MsgpackMsg.CLIENT_SETACCOUNTINFO_RSP == mMsgHeader.msgId) {
            mProgressDialog.dismissDialog();
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) mMsgHeader;
            if (mRspMsgHeader.ret == Constants.RETOK) {
                AccountInfo mMsgSetAccountinfoRsp = (AccountInfo) mRspMsgHeader;
                ToastUtil.showSuccessToast(ModifyBindphone.this, getString(R.string.PWD_OK_2));
                hideImm();
                setResult(RESULT_OK, getIntent().putExtra("tel", mMsgSetAccountinfoRsp.sms_phone));
                finish();
            } else {
                notifyDlg.show(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }

        } else if (MsgpackMsg.CLIENT_GET_CODE_RSP == mMsgHeader.msgId) {
            mProgressDialog.dismissDialog();
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) mMsgHeader;
            if (mRspMsgHeader.ret == Constants.RETOK) {
                timer = new Timer(true);
                timer.schedule(new Ta(), 1000, 1000); // timeTask
                mGetCodeBtn.setEnabled(false);
            } else {
                notifyDlg.show(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        }
    }


    private boolean isValide() {

        tel = mBindphoneView.getText().toString();
        code = mCodeView.getText().toString();

        if (StringUtils.isEmptyOrNull(tel) || !StringUtils.isPhoneNumber(tel)) {
            notifyDlg.show(R.string.PHONE_NUMBER_2);
            return false;
        }

        if (mMsgSetAccountinfoRsp != null && mMsgSetAccountinfoRsp.sms_phone.equals(tel)) {
            notifyDlg.show(R.string.PHONE_NUMBER_2);
            return false;
        }

        if (code.isEmpty() || !code.matches(Constants.RegCode)) {
            notifyDlg.show(R.string.RET_ELOGIN_VCODE_ERROR);
            return false;
        }

        return true;
    }

    class Ta extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() { // UI thread
                @Override
                public void run() {
                    recLen--;
                    mGetCodeBtn.setText(getString(R.string.SECOND,recLen));
                    if (recLen < 0) {
                        timer.cancel();
                        mGetCodeBtn.setEnabled(true);
                        mGetCodeBtn.setText(R.string.ANEW_SEND);
                        recLen = 180;
                    }
                }
            });

        }

    }
}

