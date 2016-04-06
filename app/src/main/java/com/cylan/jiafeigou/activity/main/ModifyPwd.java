package com.cylan.jiafeigou.activity.main;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.Function;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgChangePassReq;
import com.cylan.jiafeigou.entity.msg.rsp.LoginRsp;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.EditDelText;

public class ModifyPwd extends BaseActivity implements OnClickListener {
    protected static final String TAG = "MSG_JSON";
    private EditDelText mOldPwd, mNewPwd, mConfirmPwd;
    private String strOldPsw;
    private String strNewPsw;
    private NotifyDialog notifyDlg;


    public boolean CheckPsw() {
        strNewPsw = mNewPwd.getText().toString();
        String strConfirmPsw = mConfirmPwd.getText().toString();

        strOldPsw = mOldPwd.getText().toString();

        if (StringUtils.isEmptyOrNull(strOldPsw)) {
            notifyDlg.show(R.string.CURRENT_PWD);
            return false;
        }

        if (!StringUtils.isLength6To12(strOldPsw)) {
            notifyDlg.show(R.string.PASSWORD_LESSTHAN_SIX);
            return false;
        }

        if (StringUtils.isEmptyOrNull(strNewPsw)) {
            notifyDlg.show(R.string.PASSWORD_LESSTHAN_SIX);
            return false;
        }

        if (!StringUtils.isLength6To12(strNewPsw)) {
            notifyDlg.show(R.string.PASSWORD_LESSTHAN_SIX);
            return false;
        }

        if (strConfirmPsw.isEmpty()) {
            notifyDlg.show(R.string.PWD_ERR);
            return false;
        }

        if (!strNewPsw.equals(strConfirmPsw)) {
            notifyDlg.show(R.string.PWD_ERR_1);
            return false;
        }

        if (strOldPsw.equals(strConfirmPsw)) {
            notifyDlg.show(R.string.RET_ECHANGEPASS_SAME);
            return false;
        }

        return true;
    }

    private void wsRequest() {

        MsgChangePassReq mMsgChangePassReq = new MsgChangePassReq();
        mMsgChangePassReq.language_type = Utils.getLanguageType(this);
        mMsgChangePassReq.account = PreferenceUtil.getBindingPhone(this);
        mMsgChangePassReq.pass = Utils.getMD5(strOldPsw.getBytes());
        mMsgChangePassReq.os = Constants.OS_ANDROID_PHONE;
        mMsgChangePassReq.version = Function.getVersion(this);
        mMsgChangePassReq.sys_version = android.os.Build.VERSION.RELEASE;
        mMsgChangePassReq.model = android.os.Build.BRAND + "-" + android.os.Build.MODEL;
        mMsgChangePassReq.net = Utils.getNetType(this);
        mMsgChangePassReq.name = Utils.getNetName(this);
        mMsgChangePassReq.time = System.currentTimeMillis() / 1000;
        mMsgChangePassReq.bundleId = Utils.getBundleId(this);
        mMsgChangePassReq.device_token = Utils.getIMEI(this);
        mMsgChangePassReq.alias = "";
        mMsgChangePassReq.register_type = Constants.REGISTER_TYPE_PHONE;
        mMsgChangePassReq.code = "";
        mMsgChangePassReq.newpass = Utils.getMD5(strNewPsw.getBytes());
        mMsgChangePassReq.sessid = "";
        mMsgChangePassReq.oem = OEMConf.getOEM();
        JniPlay.SendBytes(mMsgChangePassReq.toBytes());
        mProgressDialog.showDialog(getString(R.string.upload));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fg_modify_pwd);

        setTitle(R.string.CHANGE_PWD);
        setRightBtn(R.string.SAVE, this);
        setTitleMagin(DensityUtil.dip2px(this, 90));

        notifyDlg = new NotifyDialog(this);
        notifyDlg.hideNegButton();

        mNewPwd = (EditDelText) findViewById(R.id.newpwd);
        mConfirmPwd = (EditDelText) findViewById(R.id.confrim_newpwd);

        mOldPwd = (EditDelText) findViewById(R.id.nowpwd);
        mOldPwd.setVisibility(View.VISIBLE);

    }

    void hideImm() {
        InputMethodManager imm = (InputMethodManager) ModifyPwd.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNewPwd.getWindowToken(), 0);
    }


    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader mMsgHeader) {

        if (mMsgHeader.msgId == MsgpackMsg.CLIENT_CHANGEPASS_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) mMsgHeader;
            mProgressDialog.dismissDialog();
            if (mRspMsgHeader.ret == Constants.RETOK) {
                LoginRsp mMsgChangePassRsp = (LoginRsp) mRspMsgHeader;
                PreferenceUtil.setPSW(ModifyPwd.this, Utils.getMD5(strNewPsw.getBytes()));
                ToastUtil.showSuccessToast(ModifyPwd.this, getString(R.string.PWD_OK_2));
                MyApp.initConfig(ModifyPwd.this, mMsgChangePassRsp);
                hideImm();
                finish();
            } else {
                notifyDlg.show(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ico_back:
                hideImm();
                finish();

                break;
            case R.id.right_btn:
                if (!CheckPsw())
                    return;

                wsRequest();
                break;

        }
    }

}