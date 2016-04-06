package com.cylan.jiafeigou.activity.video.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.main.EditSceneActivity;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class AutomaticVideoActivity extends BaseActivity implements OnClickListener {
    private List<View> list = null;
    private MsgCidData mInfo;
    private int enable;
    private NotifyDialog mModifyModeDialog;
    private NotifyDialog mConfirmDialog;

    private static final int RESULT_TO_SET_MODE = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_video);
        setTitle(R.string.SETTING_RECORD);
        setBackBtnOnClickListener(this);
        mInfo = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.VIDEOINFO);
        enable = getIntent().getIntExtra(ClientConstants.K_ENABLE, -1);
        LinearLayout mRootLayout = (LinearLayout) findViewById(R.id.root);
        list = new ArrayList<>();
        for (int i = 0; i < mRootLayout.getChildCount(); i++) {
            View v = mRootLayout.getChildAt(i);
            if (v instanceof LinearLayout) {
                v.setOnClickListener(this);
                for (int j = 0; j < ((LinearLayout) v).getChildCount(); j++) {
                    View view = ((LinearLayout) v).getChildAt(j);
                    if (view instanceof ImageView) {
                        list.add(view);
                    }
                }
            }
        }

        setModeStype(getIntent().getIntExtra(ClientConstants.K_VIDEO_MODEL, ClientConstants.AUTO_RECORD1));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout1:
                if (enable == ClientConstants.WARN_UNENABLE) {
                    if (MsgCidlistRsp.getInstance().isSomeoneMode(mInfo.cid, MsgSceneData.MODE_STANDARD)) {
                        showConfirmDialog();
                        return;
                    }
                }
                if (MsgCidlistRsp.getInstance().isSomeoneMode(mInfo.cid, MsgSceneData.MODE_HOME_IN) || enable == ClientConstants.WARN_UNENABLE) {
                    showModifyModeDialog();
                    return;
                }
                initSDInitState(0);
                break;
            case R.id.layout2:
                initSDInitState(1);
                break;
            case R.id.layout3:
                setModeStype(2);
                break;
            case R.id.ico_back:
                onBackPressed();
                break;
            default:
                break;
        }

    }

    private void setModeStype(int type) {

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setVisibility(View.GONE);
            if (type == i) {
                list.get(i).setVisibility(View.VISIBLE);
                if ((mInfo.sdcard != 0 && mInfo.err != 0) || mInfo.sdcard == 0) {
                    if (i == ClientConstants.AUTO_RECORD1 || i == ClientConstants.AUTO_RECORD2) {
                        list.get(i).setVisibility(View.GONE);
                    }
                }
                if ((MsgCidlistRsp.getInstance().isSomeoneMode(mInfo.cid, MsgSceneData.MODE_HOME_IN) || enable == ClientConstants.WARN_UNENABLE) && i == ClientConstants.AUTO_RECORD1) {
                    list.get(i).setVisibility(View.GONE);
                }
            } else {
                list.get(i).setVisibility(View.GONE);
            }
        }


    }

    private int getModeStype() {

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getVisibility() == View.VISIBLE) {
                return i;
            }
        }
        return ClientConstants.AUTO_RECORD1;

    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, getIntent().putExtra(ClientConstants.K_VIDEO_MODEL, getModeStype()).putExtra(ClientConstants.K_ENABLE, enable));
        super.onBackPressed();
    }

    private void initSDInitState(int pos) {
        if (mInfo.sdcard != 0 && mInfo.err != 0) {
            ToastUtil.showFailToast(this, getString(R.string.SD_ERR_2));
        } else if (mInfo.sdcard == 0) {
            ToastUtil.showFailToast(this, getString(R.string.SD_ERR_1));
        } else {
            setModeStype(pos);
        }

    }

    private void showConfirmDialog() {
        if (mConfirmDialog == null)
            mConfirmDialog = new NotifyDialog(this);
        mConfirmDialog.setButtonText(R.string.OPEN, R.string.CANCEL);
        mConfirmDialog.show(R.string.RECORD_ALARM_OPEN, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.confirm:
                        mConfirmDialog.dismiss();
                        enable = 1;
                        initSDInitState(0);
                        break;
                    case R.id.cancel:
                        mConfirmDialog.dismiss();
                        break;
                }

            }
        }, null);
    }



    private void showModifyModeDialog() {
        if (mModifyModeDialog == null)
            mModifyModeDialog = new NotifyDialog(this);
        mModifyModeDialog.setButtonText(R.string.ALTER, R.string.CANCEL);
        mModifyModeDialog.show(R.string.RECORD_MODE_CHANGE, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.confirm:
                        mModifyModeDialog.dismiss();
                        MsgCidlistRsp rsp= MsgCidlistRsp.getInstance();
                        if (rsp == null)
                            return;
                        startActivityForResult(new Intent(AutomaticVideoActivity.this, EditSceneActivity.class).putExtra(ClientConstants.SCENCINFO, rsp.getEnableMsgSceneData()).putExtra("CurrentTheme", rsp.getEnableSceneIndex())
                                .putExtra("flag", EditSceneActivity.FLAG_MODIFY).putExtra("scenc_count", rsp.data.size()), RESULT_TO_SET_MODE);
                        break;
                    case R.id.cancel:
                        mModifyModeDialog.dismiss();
                        break;
                }

            }
        }, null);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == RESULT_TO_SET_MODE) {
            if (data != null && data.getStringExtra("auto") != null) {
                initSDInitState(0);
                list.get(0).setVisibility(View.VISIBLE);
            }
        }
    }
}
