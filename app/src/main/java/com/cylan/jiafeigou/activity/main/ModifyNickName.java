package com.cylan.jiafeigou.activity.main;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.support.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.AccountInfo;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgSetAccountinfoReq;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.EditDelText;

public class ModifyNickName extends BaseActivity implements OnClickListener {

    private EditDelText mNickView;
    private NotifyDialog notifyDlg;

    MyApp app;

    private boolean isNick = false;


    void hideImm() {
        InputMethodManager imm = (InputMethodManager) ModifyNickName.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNickView.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_btn:
                String nick = mNickView.getText().toString().trim();

                // 检查输入格式是否正确
                if (StringUtils.isEmptyOrNull(nick)) {
                    notifyDlg.show(isNick ? R.string.USER_NAME : R.string.EMAIL_2);
                    return;
                }

                if (!isNick && (!StringUtils.isEmail(nick))) {
                    notifyDlg.show(R.string.EMAIL_2);
                    return;
                }

                if (getIntent().getStringExtra("nick") != null && nick.equals(getIntent().getStringExtra("nick"))) {
                    ToastUtil.showSuccessToast(ModifyNickName.this, getString(R.string.PWD_OK_1));
                    setResult(RESULT_OK, getIntent().putExtra("nick", nick));
                    finish();
                    return;
                }
                if (getIntent().getStringExtra("email") != null && nick.equals(getIntent().getStringExtra("email"))) {
                    ToastUtil.showSuccessToast(ModifyNickName.this, getString(R.string.PWD_OK_1));
                    setResult(RESULT_OK, getIntent().putExtra("email", nick));
                    finish();
                    return;
                }

                // 修改昵称
                if (!MyApp.getIsLogin()) {
                    ToastUtil.showFailToast(ModifyNickName.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                    return;
                }
                wsRequest(nick);

                break;
            case R.id.ico_back:
                finish();
                break;
        }
    }

    private void wsRequest(String nick) {
        MsgSetAccountinfoReq mMsgSetAccountinfoReq = new MsgSetAccountinfoReq("", "");
        mMsgSetAccountinfoReq.sms_phone = "";
        mMsgSetAccountinfoReq.code = "";
        mMsgSetAccountinfoReq.alias = isNick ? nick : "";
        mMsgSetAccountinfoReq.push_enable = Constants.DEFAULT_VALUE;
        mMsgSetAccountinfoReq.vibrate = Constants.DEFAULT_VALUE;
        mMsgSetAccountinfoReq.sound = Constants.DEFAULT_VALUE;
        mMsgSetAccountinfoReq.email = !isNick ? nick : "";
        JniPlay.SendBytes(mMsgSetAccountinfoReq.toBytes());
        mProgressDialog.showDialog(getString(R.string.upload));
        DswLog.i("send MsgSetAccountinfoReq--->" + mMsgSetAccountinfoReq.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fg_modify_nickname);
        String flag = getIntent().getStringExtra("flag");
        AccountInfo mAccountinfo = (AccountInfo) getIntent().getSerializableExtra("data");
        if (flag.equals(MyAccount.SET_NICK)) {
            isNick = true;
        }
        setBaseTitlebarVisbitly(false);
        View view = findViewById(R.id.rLayoutTitleBar);

        view.findViewById(R.id.ico_back).setOnClickListener(this);
        ((TextView) view.findViewById(R.id.title)).setText(isNick ? R.string.ALIAS : R.string.CHANGE_EMAIL);
        TextView right = (TextView) view.findViewById(R.id.right_btn);
        right.setText(R.string.SAVE);
        right.setVisibility(View.VISIBLE);
        right.setOnClickListener(this);
        findViewById(R.id.title_cover).getBackground().setAlpha(100);
        setTitlebarDrawable();

        mNickView = (EditDelText) findViewById(R.id.nickname);
        if (isNick) {
            mNickView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(12)});
            mNickView.setText(mAccountinfo != null ? mAccountinfo.alias : "");
        } else {
            findViewById(R.id.msg).setVisibility(View.VISIBLE);
            mNickView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
            mNickView.setText(mAccountinfo != null ? mAccountinfo.email : "");
            mNickView.setHint(R.string.EMAIL_2);
            mNickView.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        }

        notifyDlg = new NotifyDialog(this);
        notifyDlg.hideNegButton();

    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader mMsgHeader) {

        if (MsgpackMsg.CLIENT_SETACCOUNTINFO_RSP == mMsgHeader.msgId) {
            mProgressDialog.dismissDialog();
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) mMsgHeader;
            if (mRspMsgHeader.ret == Constants.RETOK) {
                AccountInfo mMsgSetAccountinfoRsp = (AccountInfo) mRspMsgHeader;
                ToastUtil.showSuccessToast(ModifyNickName.this.getApplicationContext(), getString(R.string.PWD_OK_2));
                /** 修改成功 **/
                hideImm();
                if (isNick)
                    setResult(RESULT_OK, getIntent().putExtra("nick", mMsgSetAccountinfoRsp.alias).putExtra("vid", mMsgSetAccountinfoRsp.vid));
                else
                    setResult(RESULT_OK, getIntent().putExtra("email", mMsgSetAccountinfoRsp.email).putExtra("vid", mMsgSetAccountinfoRsp.vid));

                finish();
            } else {
                notifyDlg.show(mRspMsgHeader.msg, mRspMsgHeader.ret);
            }
        }
    }


    private void setTitlebarDrawable() {
        MyImageLoader.loadTitlebarImage(this, ((ImageView) findViewById(R.id.title_background)));
    }

    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(ModifyNickName.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
    }


}
