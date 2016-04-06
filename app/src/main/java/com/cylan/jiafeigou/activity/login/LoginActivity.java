package com.cylan.jiafeigou.activity.login;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import cylan.log.DswLog;
import com.cylan.publicApi.Function;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.main.MyVideos;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.base.NotLoginBaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.ClientLoginReq;
import com.cylan.jiafeigou.entity.msg.req.MsgLoginByQQReq;
import com.cylan.jiafeigou.entity.msg.req.MsgLoginBySinaReq;
import com.cylan.jiafeigou.sina.AccessTokenKeeper;
import com.cylan.jiafeigou.sina.SinaWeiboUtil;
import com.cylan.jiafeigou.sina.UsersAPI;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.OEMConf;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.TencentLoginUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends NotLoginBaseActivity implements TextWatcher {

    //账号异常,请重新登陆
    public static final int STATE_ERR = 22;
    // handler
    private static final int MSG_OVER_TIME = 0x01;
    private static final int HANDLER_THIRDLOGIN_USERINFO = 0x02;
    private static final int HANDLER_SENDLOGIN_DEALY = 0x03;


    private EditText mTelNum;
    private EditText mPass;

    private NotifyDialog notifyDlg;
    private String strPhoneNum;
    private String strPsw;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OVER_TIME:
                    mProgressDialog.dismissDialog();
                    ToastUtil.showFailToast(LoginActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                    break;
                case HANDLER_THIRDLOGIN_USERINFO:
                    mProgressDialog.dismissDialog();
                    JSONObject response = (JSONObject) msg.obj;
                    String alias = "";
                    try {
                        if (response.has("nickname"))
                            alias = response.getString("nickname");
                        PreferenceUtil.setThirDswLoginPicUrl(LoginActivity.this, response.getString("figureurl_qq_1"));
                    } catch (JSONException e) {
                        DswLog.ex(e.toString());
                    }
                    if (MyApp.getIsConnectServer()) {

                        theThirdPartyLogin(mTencentLoginUtils.getMyTencent().getOpenId(), MsgpackMsg.CLIENT_LOGINBYQQ_REQ, alias);
                    } else {
                        ToastUtil.showFailToast(LoginActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                    }
                    mTencentLoginUtils.logout(LoginActivity.this);
                    break;
                case HANDLER_SENDLOGIN_DEALY:
                    if (!MyApp.getIsConnectServer()) {
                        mProgressDialog.dismissDialog();
                        ToastUtil.showFailToast(LoginActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_OVER_TIME, ClientConstants.DURATION_OVER_TIME);
                        JniPlay.SendBytes(((ClientLoginReq) msg.obj).toBytes());
                    }
                    break;
            }
            return true;
        }
    });

    private TencentLoginUtils mTencentLoginUtils = null;

    private SinaWeiboUtil mSinaLoginUtils = null;

    private boolean isEdit;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        setTitle(getString(R.string.LOGIN));
    }

    private void thirdLogin() {
        if (Utils.getLanguageType(this) == Constants.LANGUAGE_TYPE_CHINESE) {
            TextView mThirDswLoginView1 = (TextView) findViewById(R.id.the_third_login_app1);
            TextView mThirDswLoginView2 = (TextView) findViewById(R.id.the_third_login_app2);

            if (OEMConf.showQQLogin()) {
                mTencentLoginUtils = new TencentLoginUtils(this.getApplicationContext());

                mThirDswLoginView1.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (Utils.isNetworkConnected(LoginActivity.this)) {

                            if (!mTencentLoginUtils.isSessionValid()) {
                                mTencentLoginUtils.login(LoginActivity.this, mIUiListener);
                            }

                        } else {
                            ToastUtil.showToast(LoginActivity.this, getString(R.string.GLOBAL_NO_NETWORK), Gravity.CENTER, 3000);
                        }
                    }
                });
            } else {
                mThirDswLoginView1.setVisibility(View.GONE);
                findViewById(R.id.line).setVisibility(View.GONE);
            }

            if (OEMConf.showXLLogin()) {
                mSinaLoginUtils = new SinaWeiboUtil(this.getApplicationContext());

                mThirDswLoginView2.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            if (Utils.isNetworkConnected(LoginActivity.this)) {
                                AuthListener mAuthListener = new AuthListener();
                                mSinaLoginUtils.login(LoginActivity.this, mAuthListener);

                            } else {
                                ToastUtil.showToast(LoginActivity.this, getString(R.string.GLOBAL_NO_NETWORK), Gravity.CENTER, 3000);
                            }
                        } catch (Exception e) {
                            DswLog.ex(e.toString());
                        }
                    }
                });
            } else {
                mThirDswLoginView2.setVisibility(View.GONE);
                findViewById(R.id.line).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.third_login_app).setVisibility(View.GONE);
        }
    }

    private boolean check() {
        if (StringUtils.isEmptyOrNull(strPhoneNum)) {
            DswLog.i("strPhoneNum-->" + strPhoneNum);
            ToastUtil.showFailToast(LoginActivity.this, getString(R.string.ACCOUNT_ERR));
            mTelNum.requestFocus();
            return false;
        }

        if (!StringUtils.isPhoneNumber(strPhoneNum) && !StringUtils.isEmail(strPhoneNum)) {
            DswLog.i("strPhoneNum-->" + strPhoneNum);
            ToastUtil.showFailToast(LoginActivity.this, getString(R.string.ACCOUNT_ERR));
            mTelNum.requestFocus();
            return false;
        }

        if (!StringUtils.isLength6To12(strPsw)) {
            DswLog.i("strPsw-->" + strPsw);
            ToastUtil.showFailToast(LoginActivity.this, getString(R.string.PASSWORD_LESSTHAN_SIX));
            mPass.requestFocus();
            return false;
        }
        return true;
    }

    private void loginClient() {

        ClientLoginReq clientLoginReq = new ClientLoginReq();
        clientLoginReq.language_type = Utils.getLanguageType(this);
        clientLoginReq.account = strPhoneNum;
        clientLoginReq.pass = PreferenceUtil.getPSW(this);
        clientLoginReq.os = Constants.OS_ANDROID_PHONE;
        clientLoginReq.version = Function.getVersion(this.getApplicationContext());
        clientLoginReq.sys_version = android.os.Build.VERSION.RELEASE;
        clientLoginReq.model = android.os.Build.BRAND + "-" + android.os.Build.MODEL;
        clientLoginReq.net = Utils.getNetType(this);
        clientLoginReq.name = Utils.getNetName(this);
        clientLoginReq.time = System.currentTimeMillis() / 1000;
        clientLoginReq.bundleId = Utils.getBundleId(this);
        clientLoginReq.device_token = Utils.getIMEI(this);
        clientLoginReq.alias = "";
        clientLoginReq.register_type = Constants.REGISTER_TYPE_PHONE;
        clientLoginReq.code = "";
        clientLoginReq.newpass = "";
        clientLoginReq.sessid = "";
        clientLoginReq.oem = OEMConf.getOEM();
        mProgressDialog.showDialog(getString(R.string.PLEASE_WAIT_1));
        if (!MyApp.getIsConnectServer()) {
            Message msg = mHandler.obtainMessage(HANDLER_SENDLOGIN_DEALY, clientLoginReq);
            mHandler.sendMessageDelayed(msg, 3000);
        } else {
            mHandler.obtainMessage(HANDLER_SENDLOGIN_DEALY, clientLoginReq).sendToTarget();
        }
    }


    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
            notifyDlg.hideNegButton();
        }
        if (error == STATE_ERR) {
            notifyDlg.setCancelable(false);
        } else {
            notifyDlg.setCancelable(true);
        }
        notifyDlg.show(msg, error);
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {

        if (MsgpackMsg.CLIENT_LOGIN_RSP == msgpackMsg.msgId
                || MsgpackMsg.CLIENT_RELOGIN_RSP == msgpackMsg.msgId
                || MsgpackMsg.CLIENT_LOGINBYQQ_RSP == msgpackMsg.msgId
                || MsgpackMsg.CLIENT_LOGINBYSINA_RSP == msgpackMsg.msgId) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            mHandler.removeMessages(MSG_OVER_TIME);
            mProgressDialog.dismissDialog();
            if (mRspMsgHeader.ret == Constants.RETOK) {
                PreferenceUtil.setIsLogout(this, false);
                PreferenceUtil.setLoginAccount(this, mTelNum.getText().toString().trim());
                startActivity(new Intent(this, MyVideos.class));
                AppManager.getAppManager().finishAllActivity();
            } else if (mRspMsgHeader.ret == STATE_ERR) {
                showNotify(StringUtils.isEmptyOrNull(mRspMsgHeader.msg) ? "" : mRspMsgHeader.msg, mRspMsgHeader.ret);
                PreferenceUtil.cleanSessionId(this);
            } else {
                if (!isFinishing() && !TextUtils.isEmpty(mRspMsgHeader.msg)) {
                    ToastUtil.showFailToast(LoginActivity.this, mRspMsgHeader.msg);
                }
            }
        }
    }


    private IUiListener mIUiListener = new IUiListener() {

        @Override
        public void onError(UiError arg0) {
            ToastUtil.showFailToast(LoginActivity.this, arg0.errorDetail);
        }

        @Override
        public void onComplete(Object response) {

            if (null == response) {
                ToastUtil.showFailToast(LoginActivity.this, getString(R.string.LOGIN_ERR));
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                ToastUtil.showFailToast(LoginActivity.this, getString(R.string.LOGIN_ERR));
                return;
            }

            mProgressDialog.showDialog(R.string.getting);
            initOpenidAndToken(jsonResponse);
            getUserInfoInThread();

        }

        @Override
        public void onCancel() {
        }
    };

    private void theThirdPartyLogin(String id, int act, String alias) {
        if (!MyApp.getIsConnectServer()) {
            ToastUtil.showFailToast(LoginActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        if (act == MsgpackMsg.CLIENT_LOGINBYQQ_REQ) {
            MsgLoginByQQReq msgLoginByQQReq = new MsgLoginByQQReq();
            msgLoginByQQReq.account = id;
            msgLoginByQQReq.os = Constants.OS_ANDROID_PHONE;
            msgLoginByQQReq.pass = "";
            msgLoginByQQReq.version = Function.getVersion(this.getApplicationContext());
            msgLoginByQQReq.language_type = Utils.getLanguageType(this);
            msgLoginByQQReq.sys_version = android.os.Build.VERSION.RELEASE;
            msgLoginByQQReq.model = Build.MODEL;
            msgLoginByQQReq.net = Utils.getNetType(this);
            msgLoginByQQReq.name = Utils.getNetName(this);
            msgLoginByQQReq.time = System.currentTimeMillis() / 1000;
            msgLoginByQQReq.bundleId = Utils.getBundleId(this);
            msgLoginByQQReq.device_token = Utils.getIMEI(this);
            msgLoginByQQReq.alias = alias;
            msgLoginByQQReq.register_type = Constants.REGISTER_TYPE_PHONE;
            msgLoginByQQReq.code = "";
            msgLoginByQQReq.newpass = "";
            msgLoginByQQReq.sessid = "";
            msgLoginByQQReq.oem = OEMConf.getOEM();
            mHandler.sendEmptyMessageDelayed(MSG_OVER_TIME, ClientConstants.DURATION_OVER_TIME);
            mProgressDialog.showDialog(R.string.PLEASE_WAIT_1);

            JniPlay.SendBytes(msgLoginByQQReq.toBytes());

        } else if (act == MsgpackMsg.CLIENT_LOGINBYSINA_REQ) {
            MsgLoginBySinaReq msgLoginBySinaReq = new MsgLoginBySinaReq();
            msgLoginBySinaReq.account = id;
            msgLoginBySinaReq.os = Constants.OS_ANDROID_PHONE;
            msgLoginBySinaReq.pass = "";
            msgLoginBySinaReq.version = Function.getVersion(this.getApplicationContext());
            msgLoginBySinaReq.language_type = Utils.getLanguageType(this);
            msgLoginBySinaReq.sys_version = android.os.Build.VERSION.RELEASE;
            msgLoginBySinaReq.model = Build.MODEL;
            msgLoginBySinaReq.net = Utils.getNetType(this);
            msgLoginBySinaReq.name = Utils.getNetName(this);
            msgLoginBySinaReq.time = System.currentTimeMillis() / 1000;
            msgLoginBySinaReq.bundleId = Utils.getBundleId(this);
            msgLoginBySinaReq.device_token = Utils.getIMEI(this);
            msgLoginBySinaReq.alias = alias;
            msgLoginBySinaReq.register_type = Constants.REGISTER_TYPE_PHONE;
            msgLoginBySinaReq.code = "";
            msgLoginBySinaReq.newpass = "";
            msgLoginBySinaReq.sessid = "";
            msgLoginBySinaReq.oem = OEMConf.getOEM();
            mHandler.sendEmptyMessageDelayed(MSG_OVER_TIME, ClientConstants.DURATION_OVER_TIME);
            mProgressDialog.showDialog(R.string.PLEASE_WAIT_1);

            JniPlay.SendBytes(msgLoginBySinaReq.toBytes());
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        isEdit = true;
    }

    @Override
    protected void initView() {
        TextView mloginForgetpwd = (TextView) findViewById(R.id.loginForgetpwd);
        mTelNum = (EditText) this.findViewById(R.id.loginTelNum);
        mPass = (EditText) this.findViewById(R.id.loginPass);
        Button mBtnLogin = (Button) findViewById(R.id.btnLogin);
        strPhoneNum = PreferenceUtil.getLoginAccount(this);
        mTelNum.setText(strPhoneNum);
        mPass.setText(StringUtils.isEmpty(strPhoneNum) ? "" : PreferenceUtil.getPSW(this));
        mPass.addTextChangedListener(this);

        thirdLogin();

        setBackBtnOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();

            }
        });

        mloginForgetpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(LoginActivity.this, ForgetPwdActivity.class).putExtra(ClientConstants.EDIT_LOGIN_ACCOUNT, mTelNum.getText().toString());
                startActivity(it);
            }
        });

        mBtnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                strPhoneNum = mTelNum.getText().toString();
                strPsw = mPass.getText().toString();
                if (check()) {
                    try {
                        if (isEdit) {
                            PreferenceUtil.setPSW(LoginActivity.this, Utils.getMD5(strPsw.getBytes()));
                        }
                        loginClient();
                    } catch (Exception e) {
                        DswLog.ex(e.toString());
                    }
                }

            }

        });

    }

    @Override
    protected void initData() {

    }


    private class AuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                AccessTokenKeeper.writeAccessToken(getApplicationContext(), accessToken);
                Oauth2AccessToken mAccessToken = AccessTokenKeeper.readAccessToken(LoginActivity.this);
                UsersAPI mUsersAPI = new UsersAPI(mAccessToken);
                long uid = Long.parseLong(mAccessToken.getUid());
                mUsersAPI.show(uid, mListener);
                mProgressDialog.showDialog(R.string.getting);
            }
        }

        @Override
        public void onWeiboException(WeiboException e) {
            ToastUtil.showToast(LoginActivity.this, getString(R.string.LOGIN_ERR));
            mSinaLoginUtils.mSsoHandler = null;
            mSinaLoginUtils.logout(LoginActivity.this);
        }

        @Override
        public void onCancel() {
            mSinaLoginUtils.mSsoHandler = null;
            mSinaLoginUtils.logout(LoginActivity.this);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == com.tencent.connect.common.Constants.REQUEST_API) {
            if (resultCode == com.tencent.connect.common.Constants.RESULT_LOGIN) {
                mTencentLoginUtils.getMyTencent().handleLoginData(data, mIUiListener);
            }
        } else {
            if (mSinaLoginUtils.getMySsoHandler() != null) {
                mSinaLoginUtils.getMySsoHandler().authorizeCallBack(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 微锟斤拷 OpenAPI 锟截碉拷锟接口★拷
     */
    private RequestListener mListener = new RequestListener() {
        @Override
        public void onComplete(String response) {
            mProgressDialog.dismissDialog();
            try {
                if (!TextUtils.isEmpty(response)) {
                    String strId = new JSONObject(response).getString("idstr");
                    PreferenceUtil.setThirDswLoginPicUrl(LoginActivity.this, new JSONObject(response).getString("profile_image_url"));

                    if (MyApp.getIsConnectServer())
                        theThirdPartyLogin(strId, MsgpackMsg.CLIENT_LOGINBYSINA_REQ, new JSONObject(response).getString("name"));
                    else
                        ToastUtil.showFailToast(LoginActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));

                } else {
                    ToastUtil.showFailToast(LoginActivity.this, getString(R.string.LOGIN_ERR));

                }
            } catch (JSONException e) {
                DswLog.ex(e.toString());
            }
            mSinaLoginUtils.mSsoHandler = null;
            mSinaLoginUtils.logout(LoginActivity.this);
        }

        @Override
        public void onWeiboException(WeiboException e) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(LoginActivity.this, getString(R.string.LOGIN_ERR));
            // Toast.makeText(WBUserAPIActivity.this, cidData.toString(),
            // Toast.LENGTH_LONG).show();
        }
    };

    public void initOpenidAndToken(JSONObject jsonObject) {
        try {
            String token = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_EXPIRES_IN);
            String openId = jsonObject.getString(com.tencent.connect.common.Constants.PARAM_OPEN_ID);
            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires) && !TextUtils.isEmpty(openId)) {
                mTencentLoginUtils.getMyTencent().setAccessToken(token, expires);
                mTencentLoginUtils.getMyTencent().setOpenId(openId);
            }
        } catch (Exception e) {
        }
    }

    public void getUserInfoInThread() {
        if (mTencentLoginUtils.getMyTencent() != null && mTencentLoginUtils.isSessionValid()) {
            IUiListener listener = new IUiListener() {

                @Override
                public void onError(UiError e) {
                    mTencentLoginUtils.logout(LoginActivity.this);
                    ToastUtil.showFailToast(LoginActivity.this, getString(R.string.LOGIN_ERR));
                    mProgressDialog.dismissDialog();
                }

                @Override
                public void onComplete(final Object response) {

                    Message msg = new Message();
                    msg.obj = response;
                    msg.what = HANDLER_THIRDLOGIN_USERINFO;
                    mHandler.sendMessage(msg);

                }

                @Override
                public void onCancel() {
                    mTencentLoginUtils.logout(LoginActivity.this);
                    ToastUtil.showFailToast(LoginActivity.this, getString(R.string.LOGIN_ERR));
                    mProgressDialog.dismissDialog();
                }
            };
            UserInfo mInfo = new UserInfo(this, mTencentLoginUtils.getMyTencent().getQQToken());
            mInfo.getUserInfo(listener);

        } else {
            mTencentLoginUtils.logout(LoginActivity.this);
            ToastUtil.showFailToast(LoginActivity.this, getString(R.string.LOGIN_ERR));
            mProgressDialog.dismissDialog();
        }

    }

    @Override
    protected void onDestroy() {
        mPass.addTextChangedListener(null);
        mHandler.removeCallbacksAndMessages(null);
        mProgressDialog.dismissDialog();
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mTencentLoginUtils = null;
        mSinaLoginUtils = null;
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }
}