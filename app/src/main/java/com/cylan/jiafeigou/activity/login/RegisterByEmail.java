package com.cylan.jiafeigou.activity.login;


import android.content.Intent;
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
import com.cylan.jiafeigou.activity.main.MyVideos;
import com.cylan.jiafeigou.activity.main.WebViewActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.base.NotLoginBaseActivity;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgRegisterReq;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.EditDelText;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.Function;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;

import cylan.log.DswLog;

public class RegisterByEmail extends NotLoginBaseActivity implements OnClickListener {

    private EditDelText mEmailView;
    private EditText mFirstPwdView;
    private String mEmail;
    private String mFirstPwd;
    private CheckBox mCheckBox2;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_by_email);
        setTitle(getString(R.string.EMAIL_SIGNUP));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                onBackPressed();
                break;
            case R.id.submit:
                if (checkEmailSubmit()) {
                    if (!MyApp.getIsConnectServer()) {
                        ToastUtil.showFailToast(RegisterByEmail.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                    } else {
                        mProgressDialog.showDialog(getString(R.string.pb_request));
                        wsRequest();
                    }
                }
                break;

            case R.id.agreement:
                startActivity(new Intent(this, WebViewActivity.class).putExtra(WebViewActivity.URL, OEMConf.showTreayUrl()).putExtra(
                        WebViewActivity.TITLE, getString(R.string.TERM_OF_USE)));
                break;
            default:
                break;
        }

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

        if (Utils.getLanguageType(this) == Constants.LANGUAGE_TYPE_CHINESE) {
            overridePendingTransition(0, R.anim.slide_down_out);
        } else {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void handleMsgpackMsg(int msgId, MsgpackMsg.MsgHeader param) {
        if (param.msgId == MsgpackMsg.CLIENT_REGISTER_RSP) {
            mProgressDialog.dismissDialog();
            RspMsgHeader mMsgRegisterRsp = (RspMsgHeader) param;
            if (mMsgRegisterRsp.ret == Constants.RETOK) {
                ToastUtil.showSuccessToast(this, getString(R.string.RIGN_SUC));
                PreferenceUtil.setLoginAccount(this, mEmailView.getText().toString().trim());
                PreferenceUtil.setPSW(this, Utils.getMD5(mFirstPwd.getBytes()));
                startActivity(new Intent(this, MyVideos.class));
                AppManager.getAppManager().finishAllActivity();
            } else {
                showNotify(mMsgRegisterRsp.msg, mMsgRegisterRsp.ret);
            }
        }
    }

    private void wsRequest() {
        MsgRegisterReq mMsgRegisterReq = new MsgRegisterReq();
        mMsgRegisterReq.language_type = Utils.getLanguageType(this);
        mMsgRegisterReq.account = mEmail;
        mMsgRegisterReq.pass = Utils.getMD5(mFirstPwd.getBytes());
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
        mMsgRegisterReq.register_type = Constants.REGISTER_TYPE_EMAIL;
        mMsgRegisterReq.code = "";
        mMsgRegisterReq.newpass = "";
        mMsgRegisterReq.sessid = "";
        mMsgRegisterReq.oem = OEMConf.getOEM();
        JniPlay.SendBytes(mMsgRegisterReq.toBytes());
        mProgressDialog.showDialog(getString(R.string.upload));
    }

    private boolean checkEmailSubmit() {
        mEmail = mEmailView.getText().toString().trim();
        mFirstPwd = mFirstPwdView.getText().toString().trim();
        if (!StringUtils.isEmail(mEmail)) {
            DswLog.d("mEmail-->" + mEmail);
            showNotify(R.string.EMAIL_2);
            return false;
        }
        if (!StringUtils.isLength6To12(mFirstPwd)) {
            DswLog.d("mFirstPwd-->" + mFirstPwd);
            showNotify(R.string.PASSWORD_LESSTHAN_SIX);
            return false;
        }
        if (!mCheckBox2.isChecked()) {
            showNotify(R.string.READ_AGREEMENT);
            return false;
        }
        return true;

    }

    @Override
    protected void initView() {
        if (Utils.getLanguageType(this) == Constants.LANGUAGE_TYPE_CHINESE) {
            setBackBtnResourse(R.drawable.btn_quicklogin_back_selector);
        }
        setBackBtnOnClickListener(this);
        mEmailView = (EditDelText) findViewById(R.id.email_add);
        mFirstPwdView = (EditText) findViewById(R.id.pwd);
        Button mOKBtn = (Button) findViewById(R.id.submit);
        mOKBtn.setOnClickListener(this);

        mCheckBox2 = (CheckBox) findViewById(R.id.isread);
        TextView mAgreement = (TextView) findViewById(R.id.agreement);
        mAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mAgreement.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }
}