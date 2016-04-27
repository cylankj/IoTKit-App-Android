package com.cylan.jiafeigou.activity.main;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.support.DswLog;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.AccountInfo;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgGetAccountinfoReq;
import com.cylan.jiafeigou.entity.msg.req.MsgMsgIgnoreReq;
import com.cylan.jiafeigou.entity.msg.req.MsgSetAccountinfoReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.MyGridView;

import java.util.List;

public class Setting extends BaseActivity implements View.OnClickListener, OnCheckedChangeListener {

    public static final int HANDLE_MSG = 0x01;
    public static final int HANDLE_CLEAR_CACHE = 0x02;

    private static final int FLAG_PUSH_ENABLE = 0x01;
    private static final int FLAG_PUSH_SOUND = 0x02;
    private static final int FLAG_PUSH_VIBRATE = 0x03;

    private View mAbout, mShare;
    private ToggleButton mIsPushBtn, mIsOpenVoice, mIsOpenVibrate;
    private TextView mClearView;
    private NotifyDialog notifyDlg;
    private Dialog mShareDlg;
    private ImageView mVersionView;
    private String key;
    private int vid;
    private AccountInfo mAccountInfo;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg)// 此方法在ui线程运行
        {
            switch (msg.what) {
                case HANDLE_MSG: {
                    showNotify(msg.getData().getString("INFO"), msg.arg1);
                }
                break;

                case HANDLE_CLEAR_CACHE:
                    mProgressDialog.dismissDialog();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_setting);
        vid = getIntent().getIntExtra(ClientConstants.ACCOUNT_VID, 0);
        setTitle(R.string.SETTINGS);
        setBackBtnOnClickListener(this);

        mShare = findViewById(R.id.share);
        mAbout = findViewById(R.id.about);

        mIsPushBtn = (ToggleButton) findViewById(R.id.toggle_ispush);
        mIsOpenVoice = (ToggleButton) findViewById(R.id.toggle_isopenvoice);

        mIsOpenVibrate = (ToggleButton) findViewById(R.id.toggle_isopenvibrate);

        mClearView = (TextView) findViewById(R.id.clear_cache);
        mClearView.setOnClickListener(this);

        mVersionView = (ImageView) findViewById(R.id.new_version_remind);
        if (PreferenceUtil.getIsNeedUpgrade(this)) {
            mVersionView.setVisibility(View.VISIBLE);
        }

        mAbout.setOnClickListener(this);
        mShare.setOnClickListener(this);

        key = CacheUtil.getMSG_ACCOUNT_KEY();
        mAccountInfo = (AccountInfo) CacheUtil.readObject(key);
        if (mAccountInfo == null) {
            MsgGetAccountinfoReq msgGetAccountinfoReq = new MsgGetAccountinfoReq();
            MyApp.wsRequest(msgGetAccountinfoReq.toBytes());
            DswLog.i("send MsgGetAccountinfoReq msg-->" + msgGetAccountinfoReq.toString());
        } else {
            if (vid > mAccountInfo.vid) {
                MsgGetAccountinfoReq msgGetAccountinfoReq = new MsgGetAccountinfoReq();
                MyApp.wsRequest(msgGetAccountinfoReq.toBytes());
                DswLog.i("send MsgGetAccountinfoReq msg-->" + msgGetAccountinfoReq.toString());
            } else {
                initWidgetState(mAccountInfo);
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back: {
                onBackPressed();
            }
            break;

            case R.id.about:
                startActivity(new Intent(this, About.class));
                break;

            case R.id.share:
                share();

                break;

            case R.id.clear_cache:
                mProgressDialog.showDialog(getString(R.string.DELETEING));
                new Thread() {
                    public void run() {
                        try {
                            sleep(500);
                            clearMsgCache();
                        } catch (Exception e) {
                            DswLog.ex(e.toString());
                        }
                        mHandler.sendEmptyMessage(HANDLE_CLEAR_CACHE);
                    }
                }.start();
                break;
        }
    }

    void showNotify(String msg, int error) {
        if (notifyDlg == null) {
            notifyDlg = new NotifyDialog(this);
            notifyDlg.hideNegButton();
        }
        notifyDlg.show(msg, error);
    }

    void share() {
        if (mShareDlg == null) {
            mShareDlg = new Dialog(this, R.style.func_dialog);
            View content = View.inflate(this, R.layout.dialog_app_share, null);
            TextView cancel = (TextView) content.findViewById(R.id.btn_cancle);
            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mShareDlg.dismiss();
                }
            });
            MyGridView gridView = (MyGridView) content.findViewById(R.id.gridview);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String con = getString(R.string.share_content);
            if (!StringUtils.isEmptyOrNull(PreferenceUtil.getDownloadAddressUrl(Setting.this))) {
                con += PreferenceUtil.getDownloadAddressUrl(Setting.this);
            }
            intent.putExtra(Intent.EXTRA_TEXT, con);

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
            final AppAdater appAdater = new AppAdater(this);
            for (ResolveInfo info : list) {
                appAdater.add(info);
            }
            gridView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ResolveInfo info = appAdater.getItem(position);
                    intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    startActivity(intent);
                }
            });
            gridView.setAdapter(appAdater);
            mShareDlg.setContentView(content);
            mShareDlg.setCanceledOnTouchOutside(true);
        }
        try {
            mShareDlg.show();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
        ResolveInfo info;
    }

    class AppAdater extends ArrayAdapter<ResolveInfo> {

        public AppAdater(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (null == convertView) {
                convertView = View.inflate(getContext(), R.layout.item_app_share, null);
                vh = new ViewHolder();
                vh.icon = (ImageView) convertView.findViewById(R.id.icon);
                vh.name = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            ResolveInfo info = getItem(position);
            PackageManager pm = getPackageManager();
            vh.name.setText(info.loadLabel(pm));
            vh.icon.setImageDrawable(info.loadIcon(pm));
            return convertView;
        }
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {

        try {
            if (MsgpackMsg.CLIENT_GETACCOUNTINFO_RSP == msgpackMsg.msgId) {
                RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == mRspMsgHeader.ret) {
                    AccountInfo msgGetAccountinfoRsp = (AccountInfo) mRspMsgHeader;
                    initWidgetState(msgGetAccountinfoRsp);
                    mAccountInfo = msgGetAccountinfoRsp;
                } else {
                    onError(mRspMsgHeader.msg, mRspMsgHeader.ret);
                }
            } else if (MsgpackMsg.CLIENT_SETACCOUNTINFO_RSP == msgpackMsg.msgId) {
                RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
                if (Constants.RETOK == mRspMsgHeader.ret) {
                    AccountInfo msgSetAccountinfoRsp = (AccountInfo) mRspMsgHeader;
                    int sound = msgSetAccountinfoRsp.sound;
                    int vibrate = msgSetAccountinfoRsp.vibrate;
                    //initWidgetState(msgSetAccountinfoRsp);
                    PreferenceUtil.setKeySetIsOpenVoice(Setting.this, sound == 1);
                    PreferenceUtil.setKeySetIsOpenVibrate(Setting.this, vibrate == 1);
                    mAccountInfo = msgSetAccountinfoRsp;
                    ToastUtil.showSuccessToast(Setting.this, getResources().getString(R.string.PWD_OK_2));
                } else {
                    onError(mRspMsgHeader.msg, mRspMsgHeader.ret);
                }
            }
        } catch (NotFoundException e) {
            DswLog.ex(e.toString());
        }
    }


    public void onError(String msg, int ret) {
        showNotify(msg, ret);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.slide_right_out);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.toggle_ispush:

                if (isChecked) {
                    findViewById(R.id.layout_set_vioce).setVisibility(View.VISIBLE);
                    findViewById(R.id.devide).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.layout_set_vioce).setVisibility(View.GONE);
                    findViewById(R.id.devide).setVisibility(View.GONE);

                    ignoreUnRead();
                }
                onSubmitChange(FLAG_PUSH_ENABLE);
                break;
            case R.id.toggle_isopenvoice:
                onSubmitChange(FLAG_PUSH_SOUND);
                break;
            case R.id.toggle_isopenvibrate:
                onSubmitChange(FLAG_PUSH_VIBRATE);
                break;
            default:
                break;
        }

    }

    private void ignoreUnRead() {
        MsgMsgIgnoreReq msgMsgIgnoreReq = new MsgMsgIgnoreReq();
        MyApp.wsRequest(msgMsgIgnoreReq.toBytes());
        DswLog.i("send MsgMsgIgnoreReq msg-->" + msgMsgIgnoreReq.toString());
        setResult(RESULT_OK);
    }

    public void clearMsgCache() {
        // 清除数据缓存
        CacheUtil.remove(CacheUtil.getMSG_CENTER_KEY());
        MsgCidlistRsp instance = MsgCidlistRsp.getInstance();
        if (instance != null) {
            for (MsgSceneData data : instance.data) {
                for (MsgCidData mcd : data.data) {
                    CacheUtil.remove(CacheUtil.getMSG_DETAIL_KEY(mcd.cid));
                    CacheUtil.remove(CacheUtil.getMsg_MagneticList_Key(mcd.cid));
                }
            }
        }
    }


    private void onSubmitChange(int flag) {
        MsgSetAccountinfoReq msgSetAccountinfoReq = new MsgSetAccountinfoReq("", "");
        msgSetAccountinfoReq.sms_phone = "";
        msgSetAccountinfoReq.code = "";
        msgSetAccountinfoReq.alias = "";
        msgSetAccountinfoReq.push_enable = flag == FLAG_PUSH_ENABLE ? (mIsPushBtn.isChecked() ? 1 : 0) : Constants.DEFAULT_VALUE;
        msgSetAccountinfoReq.vibrate = flag == FLAG_PUSH_VIBRATE ? (mIsOpenVibrate.isChecked() ? 1 : 0) : Constants.DEFAULT_VALUE;
        msgSetAccountinfoReq.sound = flag == FLAG_PUSH_SOUND ? (mIsOpenVoice.isChecked() ? 1 : 0) : Constants.DEFAULT_VALUE;
        msgSetAccountinfoReq.email = "";
        MyApp.wsRequest(msgSetAccountinfoReq.toBytes());
        DswLog.i("send MsgSetAccountinfoReq msg-->" + msgSetAccountinfoReq.toString());

    }

    private void initWidgetState(AccountInfo msgGetAccountinfoRsp) {

        int pushEnable = msgGetAccountinfoRsp.push_enable;
        mIsPushBtn.setChecked(pushEnable == 1);
        mIsPushBtn.setOnCheckedChangeListener(this);

        int sound = msgGetAccountinfoRsp.sound;
        int vibrate = msgGetAccountinfoRsp.vibrate;
        mIsOpenVoice.setChecked(sound == 1);
        mIsOpenVibrate.setChecked(vibrate == 1);
        PreferenceUtil.setKeySetIsOpenVoice(Setting.this, sound == 1);
        PreferenceUtil.setKeySetIsOpenVibrate(Setting.this, vibrate == 1);
        if (mIsPushBtn.isChecked()) {
            findViewById(R.id.layout_set_vioce).setVisibility(View.VISIBLE);
            findViewById(R.id.devide).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layout_set_vioce).setVisibility(View.GONE);
            findViewById(R.id.devide).setVisibility(View.GONE);
        }

        mIsOpenVoice.setOnCheckedChangeListener(this);
        mIsOpenVibrate.setOnCheckedChangeListener(this);
    }

}