package com.cylan.jiafeigou.activity.login;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.Function;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.main.MyVideos;
import com.cylan.jiafeigou.activity.main.WebViewActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.base.NotLoginBaseActivity;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgGetCodeReq;
import com.cylan.jiafeigou.entity.msg.req.MsgRegisterReq;
import com.cylan.jiafeigou.receiver.SMSBroadcastReceiver;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class RegisterByPhone extends NotLoginBaseActivity implements OnClickListener {

    private static final int SUBMITDATE = 0x01;
    private static final int GETCODE = 0x02;

    private EditText mCodeView;
    private Button mBtnGet;
    private CheckBox mCheckBox1;
    private EditText mPhone;
    private EditText mPwdText;


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

    private String strPhoneNum;
    private String pwd;


    /**
     * 定时器 *
     */
    private int recLen = 180;
    private Timer timer = null;

    private SMSBroadcastReceiver mSMSBroadcastReceiver;
    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_by_phone);
        setTitle(getString(R.string.SIGN_UP));
        setBackBtnOnClickListener(this);
    }


    @Override
    protected void initView() {

        mCodeView = (EditText) findViewById(R.id.code);
        mBtnGet = (Button) findViewById(R.id.get_code);
        mBtnGet.setOnClickListener(this);
        Button mConfirmBtn = (Button) findViewById(R.id.confirm);
        mConfirmBtn.setOnClickListener(this);
        mPhone = (EditText) findViewById(R.id.input);
        mPwdText = (EditText) findViewById(R.id.pwd);
        mCheckBox1 = (CheckBox) findViewById(R.id.isread);

        TextView mAgreement = (TextView) findViewById(R.id.agreement);
        mAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mAgreement.setOnClickListener(this);
        findViewById(R.id.register_by_email).setOnClickListener(this);

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
        //注销短信监听广播
        unregisterReceiver(mSMSBroadcastReceiver);
        if (timer != null)
            timer.cancel();
    }

    @Override
    public void handleMsgpackMsg(int msgId, MsgpackMsg.MsgHeader param) {
        if (!AppManager.getAppManager().isActivityTop(this.getClass().getName()))
            return;
        if (param.msgId == MsgpackMsg.CLIENT_GET_CODE_RSP) {
            mProgressDialog.dismissDialog();
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) param;
            if (mRspMsgHeader.ret == Constants.RETOK) {
                // getCode_success();
                timer = new Timer(true);
                timer.schedule(new Ta(), 1000, 1000); // timeTask
                mBtnGet.setEnabled(false);
            } else {
                showNotify(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        } else if (param.msgId == MsgpackMsg.CLIENT_REGISTER_RSP) {
            mProgressDialog.dismissDialog();
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) param;
            if (mRspMsgHeader.ret == Constants.RETOK) {
                if (timer != null)
                    timer.cancel();
                ToastUtil.showSuccessToast(this, getString(R.string.RIGN_SUC));
                PreferenceUtil.setLoginAccount(this, mPhone.getText().toString().trim());
                PreferenceUtil.setPSW(this, Utils.getMD5(pwd.getBytes()));
                startActivity(new Intent(this, MyVideos.class));
                AppManager.getAppManager().finishAllActivity();
            } else {
                showNotify(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                onBackPressed();
                break;
            case R.id.register_by_email:
                startActivity(new Intent(this, RegisterByEmail.class));
                overridePendingTransition(R.anim.slide_down_in, 0);
                break;

            case R.id.agreement:
                startActivity(new Intent(this, WebViewActivity.class).putExtra(WebViewActivity.URL, OEMConf.showTreayUrl()).putExtra(WebViewActivity.TITLE,
                        getString(R.string.TERM_OF_USE)));
                break;
            case R.id.get_code:

                getCode();

                break;
            case R.id.confirm:
                if (CheckPsw()) {
                    wsRequest(SUBMITDATE);
                }
                break;
            default:
                break;
        }
    }

    // 获取短信验证码
    public void getCode() {
        strPhoneNum = mPhone.getText().toString();

        if (!strPhoneNum.matches(Constants.RegPhone)) {
            DswLog.i("strPhoneNum-->" + strPhoneNum);
            showNotify(R.string.PHONE_NUMBER_2);
            return;
        }
        if (!mCheckBox1.isChecked()) {
            // notify 请先阅读协议
            showNotify(R.string.READ_AGREEMENT);
            return;
        }

        wsRequest(GETCODE);

    }

    // flag=0获取验证码
    private void wsRequest(int flag) {
        strPhoneNum = mPhone.getText().toString();
        pwd = mPwdText.getText().toString();
        if (MyApp.getIsConnectServer()) {
            if (flag == SUBMITDATE) {
                MsgRegisterReq mMsgRegisterReq = new MsgRegisterReq();
                mMsgRegisterReq.language_type = Utils.getLanguageType(this);
                mMsgRegisterReq.account = strPhoneNum;
                mMsgRegisterReq.pass = Utils.getMD5(pwd.getBytes());
                mMsgRegisterReq.os = Constants.OS_ANDROID_PHONE;
                mMsgRegisterReq.version = Function.getVersion(this);
                mMsgRegisterReq.sys_version = android.os.Build.VERSION.RELEASE;
                mMsgRegisterReq.model = android.os.Build.BRAND + "-" + android.os.Build.MODEL;
                mMsgRegisterReq.net = Utils.getNetType(this);
                mMsgRegisterReq.name = Utils.getNetName(this);
                mMsgRegisterReq.time = System.currentTimeMillis() / 1000;
                mMsgRegisterReq.bundleId = Utils.getBundleId(this);
                mMsgRegisterReq.device_token = Utils.getIMEI(this);
                mMsgRegisterReq.alias = "";
                mMsgRegisterReq.register_type = Constants.REGISTER_TYPE_PHONE;
                mMsgRegisterReq.code = mCodeView.getText().toString().trim();
                mMsgRegisterReq.newpass = "";
                mMsgRegisterReq.sessid = "";
                mMsgRegisterReq.oem = OEMConf.getOEM();
                JniPlay.SendBytes(mMsgRegisterReq.toBytes());
                mProgressDialog.showDialog(getString(R.string.upload));
            } else if (flag == GETCODE) {
                MsgGetCodeReq mMsgGetCodeReq = new MsgGetCodeReq();
                mMsgGetCodeReq.language_type = Utils.getLanguageType(this);
                mMsgGetCodeReq.account = strPhoneNum;
                mMsgGetCodeReq.type = Constants.CLIENT_GETCODE_TYPE_REGISTER;
                mMsgGetCodeReq.oem = OEMConf.getOEM();
                JniPlay.SendBytes(mMsgGetCodeReq.toBytes());
                mProgressDialog.showDialog(getString(R.string.getting));
                DswLog.i("send mMsgGetCodeReq--->" + mMsgGetCodeReq.toString());

            }
        } else {
            ToastUtil.showFailToast(RegisterByPhone.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));

        }

    }

    class Ta extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() { // UI thread
                @Override
                public void run() {
                    recLen--;
                    mBtnGet.setText(getString(R.string.SECOND, recLen));
                    if (recLen < 0) {
                        timer.cancel();
                        mBtnGet.setEnabled(true);
                        mBtnGet.setText(R.string.ANEW_SEND);
                        recLen = 180;
                    }
                }
            });

        }

    }

    public boolean CheckPsw() {
        strPhoneNum = mPhone.getText().toString();
        pwd = mPwdText.getText().toString();

        if (StringUtils.isEmptyOrNull(strPhoneNum) || !StringUtils.isPhoneNumber(strPhoneNum)) {
            DswLog.i("strPhoneNum-->" + strPhoneNum);
            showNotify(R.string.PHONE_NUMBER_2);
            return false;
        }

        if (mCodeView.getText().toString().trim().isEmpty() || !mCodeView.getText().toString().trim().matches(Constants.RegCode)) {
            DswLog.i("code-->" + mCodeView.getText().toString().trim());
            showNotify(R.string.RET_ELOGIN_VCODE_ERROR);
            return false;
        }

        if (StringUtils.isEmptyOrNull(pwd)) {
            DswLog.i("pwd-->" + pwd);
            showNotify(R.string.PASSWORD_LESSTHAN_SIX);
            return false;
        }

        if (!StringUtils.isLength6To12(pwd)) {
            DswLog.i("pwd-->" + pwd);
            showNotify(R.string.PASSWORD_LESSTHAN_SIX);
            return false;
        }

        if (!mCheckBox1.isChecked()) {
            DswLog.i("!mCheckBox1.isChecked()-->" + !mCheckBox1.isChecked());
            showNotify(R.string.READ_AGREEMENT);
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }


}