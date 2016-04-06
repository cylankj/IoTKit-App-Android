package com.cylan.jiafeigou.activity.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.AccountInfo;
import com.cylan.jiafeigou.entity.msg.HttpResult;
import com.cylan.jiafeigou.entity.msg.MsgClientLogout;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgGetAccountinfoReq;
import com.cylan.jiafeigou.listener.SaveCompleteListener;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.CircleImageView;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MyAccount extends BaseActivity implements OnClickListener, LogoutActivity.LogoutListener, SaveCompleteListener {
    public static final int FLAG_USERINFO = 0x01;
    public static final int FLAG_MODIFY_USERINFO = 0x02;
    public static final int HTTP_LOGOUT = 0x03;
    public static final int TO_SELECT_PHOTO = 0x04;

    // onActivityResult
    public static final int TO_SET_NICK = 0x06;
    public static final int TO_SET_SMS_RECEIVER = 0x07;
    public static final int TO_SET_EMAIL = 0x08;

    public static final String SET_NICK = "SET_NICK";
    public static final String SET_EMAIL = "SET_EMAIL";

    private TextView mSmsReceiveTel;

    private NotifyDialog notifyDlg;

    private CircleImageView pic;

    private TextView mNickView;

    private TextView mEmailView;

    MyApp app;
    private String url;

    private String picPath;
    private Boolean isChange = false;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mProgressDialog.dismissDialog();
                MyApp.logout(MyAccount.this);
                MyApp.startActivityToSmartCall(MyAccount.this);
            } else {
                if (msg.obj != null)
                    try {
                        Bitmap bm = (Bitmap) msg.obj;
                        pic.setImageBitmap(bm);
                    } catch (Exception e) {
                        DswLog.ex(e.toString());
                    } catch (Throwable e1) {
                        e1.printStackTrace();
                    }
            }

        }
    };

    private AccountInfo mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_myaccount);
        int vid = getIntent().getIntExtra(ClientConstants.ACCOUNT_VID, 0);

        setTitle(R.string.ACCOUNT_1);
        setBackBtnOnClickListener(this);

        app = (MyApp) getApplication();
        View mPwd = findViewById(R.id.pwd);
        RelativeLayout mPicView = (RelativeLayout) findViewById(R.id.headpic);
        pic = (CircleImageView) findViewById(R.id.pic);
        Button mBtnOut = (Button) findViewById(R.id.logout);
        TextView mAccountView = (TextView) findViewById(R.id.my_account);
        RelativeLayout mEmailLayout = (RelativeLayout) findViewById(R.id.layout_email);
        mEmailLayout.setOnClickListener(this);
        mEmailView = (TextView) findViewById(R.id.email);
        View mSmsReceive = findViewById(R.id.sms_receive);
        mSmsReceiveTel = (TextView) mSmsReceive.findViewById(R.id.receive_tel);
        RelativeLayout mNickLayout = (RelativeLayout) findViewById(R.id.nick);
        mNickView = (TextView) findViewById(R.id.nickname);

        String strPhoneNum = PreferenceUtil.getBindingPhone(this);

        boolean isThirDswLoginAccount = PreferenceUtil.getIsOtherLoginType(this);
        if (isThirDswLoginAccount && !PreferenceUtil.getIsLoginType(this)) {
            if (PreferenceUtil.getOtherLoginType(this) != -1 && PreferenceUtil.getOtherLoginType(this) == 0) {
                mAccountView.setText(R.string.ACCOUNT_QQ);
            } else if (PreferenceUtil.getOtherLoginType(this) != -1 && PreferenceUtil.getOtherLoginType(this) == 1) {
                mAccountView.setText(R.string.ACCOUNT_XL);
            }
            findViewById(R.id.picarrow).setVisibility(View.GONE);
            findViewById(R.id.nickarrow).setVisibility(View.GONE);
            mEmailLayout.setVisibility(View.GONE);
            mSmsReceive.setVisibility(View.GONE);
            mPwd.setVisibility(View.GONE);

        } else {
            mAccountView.setText(strPhoneNum);
            mNickLayout.setOnClickListener(this);
            mPicView.setOnClickListener(this);
        }

        mPwd.setOnClickListener(this);
        mSmsReceive.setOnClickListener(this);
        mBtnOut.setOnClickListener(this);

        url = "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT + "/index.php?mod=client&act=photo&sessid="
                + PreferenceUtil.getSessionId(MyAccount.this);
        if (isThirDswLoginAccount && !PreferenceUtil.getIsLoginType(this)) {
            if (!StringUtils.isEmptyOrNull(PreferenceUtil.getThirDswLoginPicUrl(this))) {
                url = PreferenceUtil.getThirDswLoginPicUrl(this);
            }
        }

        String key = CacheUtil.getMSG_ACCOUNT_KEY();
        mAccount = (AccountInfo) CacheUtil.readObject(key);
        if (mAccount == null) {
            MsgGetAccountinfoReq msgGetAccountinfoReq = new MsgGetAccountinfoReq();
            MyApp.wsRequest(msgGetAccountinfoReq.toBytes());
            DswLog.i("send MsgGetAccountinfoReq msg-->" + msgGetAccountinfoReq.toString());
        } else {
            if (vid > mAccount.vid) {
                MsgGetAccountinfoReq msgGetAccountinfoReq = new MsgGetAccountinfoReq();
                MyApp.wsRequest(msgGetAccountinfoReq.toBytes());
                clearHeadPicCache();
                DswLog.i("send MsgGetAccountinfoReq msg-->" + msgGetAccountinfoReq.toString());
            } else {
                onSuc(mAccount);
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isChange) {
            isChange = false;
        } else {
            requestUserPic();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                onBackPressed();
                break;
            case R.id.pwd:
                startActivity(new Intent(MyAccount.this, ModifyPwd.class));
                break;
            case R.id.sms_receive:
                startActivityForResult(new Intent(this, ModifyBindphone.class).putExtra("data", mAccount),
                        TO_SET_SMS_RECEIVER);
                break;
            case R.id.logout:
                LogoutActivity.setOnLogoutListener(this);
                startActivity(new Intent(MyAccount.this, LogoutActivity.class));
                break;
            case R.id.headpic:

                SelectPicCropActivity.setSaveCompleteListener(MyAccount.this);
                Intent intent = new Intent(MyAccount.this, SelectPicCropActivity.class);
                intent.putExtra(SelectPicCropActivity.CROP_WIDTH, 340);
                intent.putExtra(SelectPicCropActivity.CROP_HEIGHT, 340);
                startActivityForResult(intent, TO_SELECT_PHOTO);

                break;

            case R.id.nick:
                startActivityForResult(new Intent(this, ModifyNickName.class).putExtra("data", mAccount).putExtra("flag", SET_NICK), TO_SET_NICK);
                break;
            case R.id.layout_email:
                startActivityForResult(new Intent(this, ModifyNickName.class).putExtra("data", mAccount).putExtra("flag", SET_EMAIL),
                        TO_SET_EMAIL);
                break;
        }
    }

    private void requestUserPic() {
        try {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(url, pic, options);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DswLog.i("MyAccount---Destory");
        ImageLoader.getInstance().cancelDisplayTask(pic);
        mHandler.removeCallbacksAndMessages(null);
        LogoutActivity.setOnLogoutListener(null);
        SelectPicCropActivity.setSaveCompleteListener(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_right_out);
    }

    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
        }
        notifyDlg.hideNegButton();
        if (error == 22) {
            notifyDlg.setCancelable(false);
        } else {
            notifyDlg.setCancelable(true);
        }
        notifyDlg.show(msg, error);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == TO_SELECT_PHOTO) {
                    if (data != null) {
                        picPath = data.getStringExtra(SetCoverActivity.PIC_POSITION);
                        File file = new File(picPath);
                        if (file.exists()) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                            options.inSampleSize = BitmapUtil.calculateInSampleSize(options, pic.getWidth(), pic.getHeight());
                            options.inJustDecodeBounds = false;
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                            pic.setImageBitmap(bitmap);
                            isChange = true;
                            postPic(picPath);

                        }
                        AppManager.getAppManager().finishActivity(SelectPicCropActivity.class);
                    }
                } else if (requestCode == TO_SET_NICK) {
                    if (data == null)
                        return;
                    mNickView.setText(data.getStringExtra("nick"));
                    mAccount.alias = data.getStringExtra("nick");
                } else if (requestCode == TO_SET_SMS_RECEIVER) {
                    if (data == null)
                        return;
                    mSmsReceiveTel.setText(data.getStringExtra("tel"));
                    mSmsReceiveTel.setTextColor(getResources().getColor(R.color.title_text_color));
                    mAccount.sms_phone = data.getStringExtra("tel");
                } else if (requestCode == TO_SET_EMAIL) {
                    if (data == null)
                        return;
                    mEmailView.setText(data.getStringExtra("email"));
                    mAccount.email = data.getStringExtra("email");
                }
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    private void postPic(final String path) {
        if (Utils.isNetworkConnected(MyAccount.this)) {
            String url = "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT + "/index.php?mod=client&" + Constants.ACT + "=" + "set_photo" + "&sessid="
                    + PreferenceUtil.getSessionId(MyAccount.this);
            JniPlay.HttpPostFile(Constants.WEB_ADDR, Constants.WEB_PORT, url, path);
        }
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        if (MsgpackMsg.CLIENT_GETACCOUNTINFO_RSP == msgpackMsg.msgId) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                AccountInfo mMsgSetAccountinfoRsp = (AccountInfo) mRspMsgHeader;
                onSuc(mMsgSetAccountinfoRsp);
                mAccount = mMsgSetAccountinfoRsp;
            } else {
                showNotify(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        }
        if (MsgpackMsg.CLIENT_SETACCOUNTINFO_RSP == msgpackMsg.msgId) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                AccountInfo mMsgSetAccountinfoRsp = (AccountInfo) mRspMsgHeader;
                clearHeadPicCache();
                onSuc(mMsgSetAccountinfoRsp);
                mAccount = mMsgSetAccountinfoRsp;
            }

        }
    }

    private void onSuc(AccountInfo msgGetAccountinfoRsp) {
        String tel = msgGetAccountinfoRsp.sms_phone;
        mSmsReceiveTel.setTag(tel);
        if (StringUtils.isEmptyOrNull(tel)) {
            mSmsReceiveTel.setTextColor(getResources().getColor(R.color.mycount_not_set));
            mSmsReceiveTel.setText(getResources().getString(R.string.NO_SET));
        } else {
            mSmsReceiveTel.setTextColor(getResources().getColor(R.color.title_text_color));
            mSmsReceiveTel.setText(tel);
        }

        String email = msgGetAccountinfoRsp.email;
        mEmailView.setTag(email);
        mEmailView.setText(StringUtils.isEmptyOrNull(email) ? getResources().getString(R.string.NO_SET) : email);
        String nick = msgGetAccountinfoRsp.alias;
        mNickView.setTag(nick);
        mNickView.setText(StringUtils.isEmptyOrNull(nick) ? getResources().getString(R.string.NO_SET) : nick);
    }

    @Override
    public void httpDone(HttpResult mResult) {
        try {
            Gson gson = new Gson();
            DswLog.i("set photo--->" + gson.toJson(mResult));
            JSONObject mObject = new JSONObject(mResult.result);
            int ret = mObject.has("ret") ? mObject.getInt("ret") : 1;
            if (ret == Constants.RETOK && mResult.ret == Constants.HTTP_RETOK) {
                ToastUtil.showSuccessToast(this, getString(R.string.PWD_OK_2));
                clearHeadPicCache();
            } else {
                ToastUtil.showFailToast(this, getString(R.string.set_failed));
            }

        } catch (JSONException e) {
            DswLog.ex(e.toString());
        }

    }


    private void clearHeadPicCache() {
        MyImageLoader.removeFromCache(url);
    }


    @Override
    public void logout() {
        mProgressDialog.showDialog(R.string.is_logoutting);
        mHandler.sendEmptyMessageDelayed(0, 500);
        if (MyApp.getIsConnectServer()) {
            JniPlay.SendBytes(new MsgClientLogout().toBytes());
        }
    }

    @Override
    public void complete(Intent data) {
        if (data != null) {
            picPath = data.getStringExtra(SetCoverActivity.PIC_POSITION);
            File file = new File(picPath);
            if (file.exists()) {
                Options opts = new Options();
                opts.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                pic.setImageBitmap(bitmap);
                isChange = true;
                postPic(picPath);
            }
            AppManager.getAppManager().finishActivity(SelectPicCropActivity.class);


        }
    }
}
