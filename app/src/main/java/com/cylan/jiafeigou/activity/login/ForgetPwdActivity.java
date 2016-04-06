package com.cylan.jiafeigou.activity.login;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.Function;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.main.MyVideos;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.base.NotLoginBaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgForgetPassByEmailReq;
import com.cylan.jiafeigou.entity.msg.req.MsgGetCodeReq;
import com.cylan.jiafeigou.entity.msg.req.MsgSetPassReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgForgetPassByEmailRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgGetCodeRsp;
import com.cylan.jiafeigou.receiver.SMSBroadcastReceiver;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.EditDelText;

import java.util.Timer;
import java.util.TimerTask;

public class ForgetPwdActivity extends NotLoginBaseActivity implements OnClickListener {

    private static int TCP_GETPWD_BY_PHONE = 0X01;
    private static int TCP_GETPWD_BY_EMAIL = 0X02;

    View view;
    private LinearLayout mEditAccountLayout;
    private LinearLayout mSetPwdLayout;
    private EditDelText mAccoutView;
    private Button mNextBtn;
    private TextView mMsgView;
    private EditText mCodeView;
    private Button mGetBtn;
    private EditText mPasswordView;
    private EditText mPasswordView1;
    private Button mSubmitBtn;

    private LinearLayout mEmailLayout;
    private TextView mEmailMsgView;

    private String mCode;
    private String mPassword;

    private NotifyDialog notifyDlg;

    void showNotify(int msg) {
        showNotify(getString(msg), Constants.RETOK);
    }

    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
            notifyDlg.hideNegButton();
        }
        notifyDlg.show(msg, error);
    }

    private void hideImm(View v) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private int recLen = 180;
    private Timer timer = null;


    class Ta extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() { // UI thread
                @Override
                public void run() {
                    recLen--;
                    mGetBtn.setText(getString(R.string.SECOND, recLen));
                    if (recLen < 0) {
                        timer.cancel();
                        mGetBtn.setEnabled(true);
                        mGetBtn.setText(R.string.ANEW_SEND);
                        recLen = 180;
                    }
                }
            });

        }

    }

    private String str;

    private SMSBroadcastReceiver mSMSBroadcastReceiver;
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);
        setTitle(getString(R.string.FORGOT_PWD));
        setBackBtnOnClickListener(this);
    }


    @Override
    protected void initView() {
        mEditAccountLayout = (LinearLayout) findViewById(R.id.edit_layout);
        mSetPwdLayout = (LinearLayout) findViewById(R.id.reset_layout);
        mAccoutView = (EditDelText) findViewById(R.id.email_and_phone);
        mNextBtn = (Button) findViewById(R.id.next_step);
        mNextBtn.setOnClickListener(this);
        mMsgView = (TextView) findViewById(R.id.reset_msg);
        mCodeView = (EditText) findViewById(R.id.code);
        mGetBtn = (Button) findViewById(R.id.get_code);
        mGetBtn.setOnClickListener(this);
        mPasswordView = (EditText) findViewById(R.id.pwd);
        mPasswordView1 = (EditText) findViewById(R.id.pwd_again);
        mSubmitBtn = (Button) findViewById(R.id.submit);
        mSubmitBtn.setOnClickListener(this);

        mEmailLayout = (LinearLayout) findViewById(R.id.email_layout);
        mEmailMsgView = (TextView) findViewById(R.id.reset_msg_by_email);
        Button mRsendBtn = (Button) findViewById(R.id.send_reset);
        mRsendBtn.setOnClickListener(this);

        mAccoutView.setText(getIntent().getStringExtra(ClientConstants.EDIT_LOGIN_ACCOUNT));
        completeCode();
    }

    @Override
    protected void initData() {

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
        unregisterReceiver(mSMSBroadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_reset:
            case R.id.ico_back:
                onBackPressed();
                break;
            case R.id.next_step:
                str = mAccoutView.getText().toString().trim();
                if (!StringUtils.isPhoneNumber(str) && !StringUtils.isEmail(str)) {
                    showNotify(R.string.ACCOUNT_ERR);
                    return;
                } else if (StringUtils.isEmail(str)) {

                    submit(TCP_GETPWD_BY_EMAIL);
                    return;
                }

            case R.id.get_code:
                getCode();
                break;
            case R.id.submit:

                submit(TCP_GETPWD_BY_PHONE);

                break;

            default:
                break;
        }
    }

    private void submit(int flag) {
        if (MyApp.getIsConnectServer()) {
            if (flag == TCP_GETPWD_BY_PHONE) {
                if (checkFormat()) {
                    MsgSetPassReq clientLoginReq = new MsgSetPassReq();
                    clientLoginReq.language_type = Utils.getLanguageType(this);
                    clientLoginReq.account = str;
                    clientLoginReq.pass = Utils.getMD5(mPassword.getBytes());
                    clientLoginReq.os = Constants.OS_ANDROID_PHONE;
                    clientLoginReq.version = Function.getVersion(this);
                    clientLoginReq.sys_version = android.os.Build.VERSION.RELEASE;
                    clientLoginReq.model = android.os.Build.BRAND + "-" + android.os.Build.MODEL;
                    clientLoginReq.net = Utils.getNetType(this);
                    clientLoginReq.name = Utils.getNetName(this);
                    clientLoginReq.time = System.currentTimeMillis() / 1000;
                    clientLoginReq.bundleId = Utils.getBundleId(this);
                    clientLoginReq.device_token = Utils.getIMEI(this);
                    clientLoginReq.alias = "";
                    clientLoginReq.register_type = Constants.REGISTER_TYPE_PHONE;
                    clientLoginReq.code = mCode;
                    clientLoginReq.newpass = "";
                    clientLoginReq.sessid = "";
                    clientLoginReq.oem = OEMConf.getOEM();
                    JniPlay.SendBytes(clientLoginReq.toBytes());
                    mProgressDialog.showDialog(getString(R.string.upload));
                }
            } else if (flag == TCP_GETPWD_BY_EMAIL) {
                MsgForgetPassByEmailReq msg = new MsgForgetPassByEmailReq("", "");
                msg.language_type = Utils.getLanguageType(this);
                msg.account = str;
                msg.oem = OEMConf.getOEM();
                JniPlay.SendBytes(msg.toBytes());
                DswLog.i("send MsgForgetPassByEmailReq--->" + msg.toString());
                mProgressDialog.showDialog(getString(R.string.upload));
            }
        } else {
            ToastUtil.showToast(this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK),
                    Gravity.CENTER, 3000);
        }

    }

    public void getCode() {

        if (!MyApp.getIsConnectServer()) {
            ToastUtil.showToast(this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK),
                    Gravity.CENTER, 3000);
            return;
        }
        MsgGetCodeReq req = new MsgGetCodeReq();
        req.language_type = Utils.getLanguageType(this);
        req.account = str;
        req.type = Constants.CLIENT_GETCODE_TYPE_FORGETPASS;
        req.oem = OEMConf.getOEM();
        JniPlay.SendBytes(req.toBytes());
        DswLog.i("send getcode--->" + req.toString());
        mProgressDialog.showDialog(getString(R.string.getting));
    }


    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showToast(this, getString(R.string.GLOBAL_NO_NETWORK), Gravity.CENTER, 3000);
        }
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        if (MsgpackMsg.CLIENT_GET_CODE_RSP == msgpackMsg.msgId) {
            // MsgGetCodeRsp msgGetCodeRsp = (MsgGetCodeRsp) msgpackMsg;
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            mNextBtn.setEnabled(true);
            mProgressDialog.dismissDialog();
            if (mRspMsgHeader.ret == Constants.RETOK) {
                hideImm(mSubmitBtn);
                timer = new Timer(true);
                timer.schedule(new Ta(), 1000, 1000); // timeTask
                mGetBtn.setEnabled(false);
                mGetBtn.setText(getString(R.string.SECOND, recLen));
                if (mEditAccountLayout.getVisibility() == View.VISIBLE)
                    mEditAccountLayout.setVisibility(View.GONE);
                if (mSetPwdLayout.getVisibility() == View.GONE)
                    mSetPwdLayout.setVisibility(View.VISIBLE);
                setTitle(getString(R.string.RESET_PWD));

                String smsphone = StringUtils.isEmptyOrNull(((MsgGetCodeRsp) mRspMsgHeader).sms_phone) ? str : ((MsgGetCodeRsp) mRspMsgHeader).sms_phone;

                mMsgView.setText(String.format(getString(R.string.SMS_CODE), Utils.phoneNumchange(smsphone)));

            } else {
                showNotify(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        } else if (MsgpackMsg.CLIENT_SETPASS_RSP == msgpackMsg.msgId) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            mProgressDialog.dismissDialog();
            if (mRspMsgHeader.ret == Constants.RETOK) {
                PreferenceUtil.setLoginAccount(this, str);
                PreferenceUtil.setPSW(this, Utils.getMD5(mPassword.getBytes()));
                ToastUtil.showSuccessToast(this, getString(R.string.PWD_OK_2));
                startActivity(new Intent(this, MyVideos.class));
                AppManager.getAppManager().finishAllActivity();
                onBackPressed();
            } else {
                showNotify(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        } else if (MsgpackMsg.CLIENT_FORGETPASSBYEMAIL_RSP == msgpackMsg.msgId) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            mProgressDialog.dismissDialog();
            if (mRspMsgHeader.ret == Constants.RETOK) {
                MsgForgetPassByEmailRsp mMsgForgetPassByEmailRsp = (MsgForgetPassByEmailRsp) mRspMsgHeader;
                if (mEditAccountLayout.getVisibility() == View.VISIBLE)
                    mEditAccountLayout.setVisibility(View.GONE);
                if (mEmailLayout.getVisibility() == View.GONE) {
                    mEmailLayout.setVisibility(View.VISIBLE);
                    mEmailLayout.requestFocus();
                }
                mEmailMsgView.setText(String.format(getString(R.string.EMAIL_RESET_PWD), mMsgForgetPassByEmailRsp.email));
                final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mAccoutView.getWindowToken(), 0);
            } else {
                showNotify(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        }
    }

    private boolean checkFormat() {
        mCode = mCodeView.getText().toString().trim();
        mPassword = mPasswordView.getText().toString().trim();
        String mPassword1 = mPasswordView1.getText().toString().trim();
        if (StringUtils.isEmptyOrNull(mCode) || !mCode.matches(Constants.RegCode)) {
            DswLog.i("mCode-->" + mCode);
            showNotify(R.string.RET_ELOGIN_VCODE_ERROR);
            return false;
        }
        if (StringUtils.isEmptyOrNull(mPassword)) {
            DswLog.i("mPassword-->" + mPassword);
            showNotify(R.string.PASSWORD_LESSTHAN_SIX);
            return false;
        }
        if (!StringUtils.isLength6To12(mPassword)) {
            DswLog.i("mPassword-->" + mPassword);
            showNotify(R.string.PASSWORD_LESSTHAN_SIX);
            return false;
        }
        if (StringUtils.isEmptyOrNull(mPassword1)) {
            DswLog.i("mPassword1-->" + mPassword1);
            showNotify(R.string.PWD_ERR);
            return false;
        }
        if (!StringUtils.isLength6To12(mPassword1)) {
            DswLog.i("mPassword1-->" + mPassword1);
            showNotify(R.string.PASSWORD_LESSTHAN_SIX);
            return false;
        }
        if (!mPassword.equals(mPassword1)) {
            DswLog.i("mPassword1-->" + mPassword1);
            showNotify(R.string.PWD_ERR_1);
            return false;
        }
        return true;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mAccoutView.getWindowToken(), 0);
    }
}