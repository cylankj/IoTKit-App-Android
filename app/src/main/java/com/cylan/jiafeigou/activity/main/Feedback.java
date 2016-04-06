package com.cylan.jiafeigou.activity.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.HttpResult;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.worker.UploadLogWorker;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class Feedback extends BaseActivity implements View.OnClickListener {

    public static final int HANDLE_IMM = 0x01;

    private EditText mEdit;
    private CheckBox mSubmitLogCheckBox;

    private String strContent;
    private NotifyDialog notifyDlg;


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_IMM: {
                    if (!isFinishing()) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mEdit, 0);
                    }
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_feedback);

        setTitle(R.string.FEEDBACK);
        setRightBtn(R.string.SEND, this);
        setBackBtnOnClickListener(this);

        mEdit = (EditText) findViewById(R.id.edit);
        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ((TextView) findViewById(R.id.textlength)).setText(s.toString().length() + "/128");
            }
        });

        mSubmitLogCheckBox = (CheckBox) findViewById(R.id.submit_log);
        mSubmitLogCheckBox.setChecked(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHandler.sendEmptyMessageDelayed(HANDLE_IMM, 200);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeMessages(HANDLE_IMM);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back: {
                hideImm();
                onBackPressed();
            }
            break;
            case R.id.right_btn:
                postContent();
                break;

        }
    }

    void MyToast(String Content) {
        ToastUtil.showToast(this, Content, Gravity.CENTER);
        mEdit.postDelayed(new Runnable() {

            @Override
            public void run() {
                finish();
            }
        }, 600);
    }

    private void postContent() {
        strContent = mEdit.getText().toString().trim();
        if (strContent.isEmpty()) {
            showNotify(getString(R.string.FEEDBACK_NULL), Constants.RETOK);
            return;
        } else {
            if (mSubmitLogCheckBox.isChecked()) {
                new Thread(new UploadLogWorker(Utils.getFeedbackUrl(strContent,PreferenceUtil.getSessionId(this)))).start();
            } else {
                JniPlay.HttpGet(Constants.WEB_ADDR, Constants.WEB_PORT, Utils.getFeedbackUrl(strContent,PreferenceUtil.getSessionId(this)));
            }
            mProgressDialog.showDialog(R.string.submiting);
        }

    }


    void hideImm() {
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_right_out);

    }

    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
            notifyDlg.hideNegButton();
        }
        notifyDlg.show(msg, error);
    }


    @Override
    public void httpDone(HttpResult mResult) {
        try {
            String[] strs = getResources().getStringArray(R.array.error_msg);
            Gson gson = new Gson();
            DswLog.i("Feedback--->" + gson.toJson(mResult));
            int ret = 0;
            String act = "";
            if (!StringUtils.isEmptyOrNull(mResult.result)) {
                JSONObject mObject = new JSONObject(mResult.result);
                ret = mObject.has("ret") ? mObject.getInt("ret") : 1;
                act = mObject.has("act") ? mObject.getString("act") : "";
            }
            mProgressDialog.dismissDialog();
            if (mResult.ret == Constants.HTTP_RETOK) {
                if ("feedback_rsp".equals(act)) {
                    hideImm();
                    if (ret == Constants.RETOK) {
                        MyToast(getString(R.string.DEAL_FEEDBACK));
                    } else {
                        showNotify(strs[ret + 1], ret);
                    }

                }
            } else {
                ToastUtil.showFailToast(Feedback.this, getString(R.string.SUBMIT_FAIL));
            }
        } catch (JSONException e) {
            DswLog.ex(e.toString());
        }

    }

    @Override
    public void disconnectServer() {
        if (mProgressDialog.isShow()) {
            mProgressDialog.dismissDialog();
            ToastUtil.showFailToast(Feedback.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
    }
}