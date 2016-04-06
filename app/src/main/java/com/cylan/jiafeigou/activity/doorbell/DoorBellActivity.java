package com.cylan.jiafeigou.activity.doorbell;

import android.animation.ObjectAnimator;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.doorbell.detail.DoorBellDetailActivity;
import com.cylan.jiafeigou.activity.message.MessageDelDialog;
import com.cylan.jiafeigou.activity.message.MessagePicturesActivity;
import com.cylan.jiafeigou.activity.video.addDevice.AddVideoActivity;
import com.cylan.jiafeigou.adapter.DoorBellAdapter;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.base.RootActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.CallListData;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgData;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOffline;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOnline;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgBellCallListReq;
import com.cylan.jiafeigou.entity.msg.req.MsgClientBellCallDeleteReq;
import com.cylan.jiafeigou.entity.msg.rsp.LoginRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgBellCallListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgSetCidAliasRsp;
import com.cylan.jiafeigou.listener.CheckDoorbellHeadPicListener;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.PullToZoomListView;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.MsgpackMsg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cylan.uil.core.ImageLoader;

public class DoorBellActivity extends RootActivity implements OnClickListener, AdapterView.OnItemLongClickListener, CheckDoorbellHeadPicListener, PullToZoomListView.OnScrollStateChangeListener {


    private MsgCidData mData;

    private PullToZoomListView mListView;

    private TextView mTitleView;

    private ImageView mProgressView;

    private DoorBellAdapter mAdapter;

    private boolean isStartAnim = false;

    private MessageDelDialog mDialog;

    private TextView mNoCallLogView;

    private boolean isLifePause;

    private ImageView ico_remind;


    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doorbell_main);
        mData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        initView();
        initData();
    }

    private void initView() {
        ImageView mBackView = (ImageView) findViewById(R.id.ico_back);
        ImageView mSettingView = (ImageView) findViewById(R.id.ico_set);
        mTitleView = (TextView) findViewById(R.id.title);
        mProgressView = (ImageView) findViewById(R.id.progress);
        ico_remind = (ImageView) findViewById(R.id.ico_set_remind);
        mBackView.setOnClickListener(this);
        mSettingView.setOnClickListener(this);
        mListView = (PullToZoomListView) findViewById(R.id.doordell_list);
        mListView.setOnScrollStateChangeListener(this);
        mListView.setOnItemLongClickListener(this);

        mNoCallLogView = (TextView) findViewById(R.id.nocalllog);
        if (mData != null && !StringUtils.isEmptyOrNull(mData.share_account)) {
            mSettingView.setVisibility(View.GONE);
        } else {
            if (PreferenceUtil.getKeyIsFirstClickDoorBellSet(this))
                ico_remind.setVisibility(View.VISIBLE);
        }
        mTitleView.setText(mData.mName());
        if (!MyApp.getIsLogin()) {
            mTitleView.setText(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.DOOR_NOT_CONNECT)));
        } else if (mData.net == MsgCidData.CID_NET_CONNECT || mData.net == MsgCidData.CID_NET_OFFLINE) {
            mTitleView.setText(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.OFF_LINE)));
        } else {
            mTitleView.setText(mData.mName());
        }

        setAdapter();
    }

    private void initData() {

        List<CallListData> list = (List<CallListData>) CacheUtil.readObject(CacheUtil.getMSG_DOORBELLLIS_KEY(mData.cid));
        if (list != null) {
            mAdapter.addAll(list);
            mAdapter.notifyDataSetChanged();
            showNoCallLogView();
        }
        if (AddVideoActivity.newCid.contains(mData.cid)) {
            Log.d("newCid", "newCid-->" + AddVideoActivity.newCid.toString());
            if (mData.battery < 80) {
                ShowLowPower lowPower = new ShowLowPower(this, ShowLowPower.SHOWLOWPOWER);
                lowPower.show();
            }
            AddVideoActivity.newCid.remove(mData.cid);
        } else if (mData.battery < 20 && mData.net == MsgCidData.CID_NET_WIFI) {
            ShowLowPower lowPower = new ShowLowPower(this, ShowLowPower.SHOWLOWPOWER);
            lowPower.show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ImageLoader.getInstance().resume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLifePause = false;
        if (MyApp.getIsLogin()) {
            getData();
        } else {
            ToastUtil.showFailToast(DoorBellActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isLifePause = true;
        ImageLoader.getInstance().stop();
    }

    private void setAdapter() {
        mAdapter = new DoorBellAdapter(this, new ArrayList<CallListData>());
        mAdapter.setOnCheckHeadPicListener(this);
        mListView.getHeaderView().setScaleType(ImageView.ScaleType.CENTER_CROP);
        mListView.getHeaderView().setImageResource(R.drawable.bg_doorbell_top);
        View mHeaderView = getLayoutInflater().inflate(R.layout.layout_doorbell_head, null);
        TextView mLookView = (TextView) mHeaderView.findViewById(R.id.look);
        mLookView.setOnClickListener(this);
        mListView.getHeaderContainer().addView(mHeaderView);
        mListView.setTitleView(mHeaderView.findViewById(R.id.headview));
        mListView.setHeaderView();
        mListView.setAdapter(mAdapter);

    }

    private void getData() {
        MsgBellCallListReq mMsgBellCallListReq = new MsgBellCallListReq(mData.cid);
        MyApp.wsRequest(mMsgBellCallListReq.toBytes());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                finish();
                break;

            case R.id.ico_set:
                startActivity(new Intent(DoorBellActivity.this, DoorBellDetailActivity.class).putExtra(ClientConstants.CIDINFO, mData));
                ico_remind.setVisibility(View.GONE);
                PreferenceUtil.setKeyIsFirstClickDoorBellSet(this, false);
                break;
            case R.id.look:
                if (!MyApp.getIsLogin()) {
                    ToastUtil.showFailToast(DoorBellActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                    return;
                }
                if (mData.net == MsgCidData.CID_NET_CONNECT || mData.net == MsgCidData.CID_NET_OFFLINE) {
                    ToastUtil.showFailToast(DoorBellActivity.this, getString(R.string.OFFLINE_ERR));
                    return;
                }
                if (!isLifePause) {
                    startActivity(DoorBellCalledActivity.getIntent(DoorBellActivity.this, mData, 0, true));
                }
                break;
            default:
                break;
        }

    }


    private void startPullAnimation(float fromDegress, float toDegress) {
        mProgressView.clearAnimation();
        ObjectAnimator animation = ObjectAnimator.ofFloat(mProgressView, "rotation", fromDegress, toDegress);
        animation.setStartDelay(0);
        animation.start();
    }

    private void startRefreshAnimation() {
        mProgressView.clearAnimation();
        RotateAnimation mRotateAnimation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setRepeatCount(-1);
        mRotateAnimation.setRepeatMode(Animation.RESTART);
        mRotateAnimation.setDuration(600);
        mProgressView.startAnimation(mRotateAnimation);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressView.clearAnimation();
    }


    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {

        if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SETCIDALIAS_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                MsgSetCidAliasRsp mMsgSetCidAliasRsp = (MsgSetCidAliasRsp) mRspMsgHeader;
                String cid = mMsgSetCidAliasRsp.cid;
                String alias = mMsgSetCidAliasRsp.alias;
                if (mData.cid.equals(cid)) {
                    mData.alias = alias;
                    mTitleView.setText(mData.mName());
                    if (!MyApp.getIsLogin()) {
                        mTitleView.setText(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.DOOR_NOT_CONNECT)));
                    } else if (mData.net == MsgCidData.CID_NET_CONNECT || mData.net == MsgCidData.CID_NET_OFFLINE) {
                        mTitleView.setText(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.OFF_LINE)));
                    } else {
                        mTitleView.setText(mData.mName());
                    }
                }
            }

        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_BELL_CALL_LIST_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (mRspMsgHeader.caller.equals(mData.cid)) {
                mProgressView.clearAnimation();
                mProgressView.setVisibility(View.GONE);
                mTitleView.setVisibility(View.VISIBLE);
                if (Constants.RETOK == mRspMsgHeader.ret) {
                    MsgBellCallListRsp mMsgBellCallListRsp = (MsgBellCallListRsp) mRspMsgHeader;
                    mAdapter.clear();
                    mAdapter.addAll(mMsgBellCallListRsp.data);
                    mAdapter.notifyDataSetChanged();
                    showNoCallLogView();
                } else {
                    ToastUtil.showFailToast(this, mRspMsgHeader.msg);
                }
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_BELL_CALL_DELETE_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (mRspMsgHeader.caller.equals(mData.cid)) {
                if (Constants.RETOK == mRspMsgHeader.ret) {
                    getData();
                } else {
                    ToastUtil.showFailToast(this, mRspMsgHeader.msg);
                }
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SYNC_CIDONLINE) {
            MsgSyncCidOnline mMsgSyncCidOnline = (MsgSyncCidOnline) msgpackMsg;
            String cid = mMsgSyncCidOnline.cid;
            int net = mMsgSyncCidOnline.net;
            if (mData != null) {
                if (mData.cid.equals(cid)) {
                    mData.net = net;
                    mData.name = mMsgSyncCidOnline.name;
                    mData.version = mMsgSyncCidOnline.version;
                    mTitleView.setText(mData.mName());
                }

            }

        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SYNC_CIDOFFLINE) {
            MsgSyncCidOffline mMsgSyncCidOffline = (MsgSyncCidOffline) msgpackMsg;
            String cid = mMsgSyncCidOffline.cid;
            if (mData != null) {
                if (mData.cid.equals(cid)) {
                    mData.net = MsgCidData.CID_NET_OFFLINE;
                    mData.name = "";
                    if (!MyApp.getIsLogin()) {
                        mTitleView.setText(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.DOOR_NOT_CONNECT)));
                    } else if (mData.net == MsgCidData.CID_NET_CONNECT || mData.net == MsgCidData.CID_NET_OFFLINE) {
                        mTitleView.setText(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.OFF_LINE)));
                    } else {
                        mTitleView.setText(mData.mName());
                    }

                }

            }

        } else if (MsgpackMsg.CLIENT_RELOGIN_RSP == msgpackMsg.msgId) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (mRspMsgHeader.ret == Constants.RETOK) {
                LoginRsp rsp = (LoginRsp) mRspMsgHeader;
                int sceneSize = rsp.data.size();
                boolean has = false;
                for (int i = 0; i < sceneSize; i++) {
                    int deviceSize = rsp.data.get(i).data.size();
                    for (int j = 0; j < deviceSize; j++) {
                        MsgCidData mcd = rsp.data.get(i).data.get(j);
                        if (mcd.cid.equals(mData.cid)) {
                            mData.net = mcd.net;
                            mData.name = mcd.name;
                            mData.version = mcd.version;
                            has = true;
                            break;
                        }
                    }
                    if (has)
                        break;
                }
            }

        }
    }

    @Override
    public void disconnectServer() {
        if (!MyApp.getIsLogin()) {
            mTitleView.setText(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.DOOR_NOT_CONNECT)));
        }
    }

    @Override
    public void connectServer() {
        if (!MyApp.getIsConnectServer()) {
            mTitleView.setText(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.DOOR_NOT_CONNECT)));
        } else if (mData.net == MsgCidData.CID_NET_CONNECT || mData.net == MsgCidData.CID_NET_OFFLINE) {
            mTitleView.setText(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.OFF_LINE)));
        } else {
            mTitleView.setText(mData.mName());
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                   long id) {
        if (mData != null && !StringUtils.isEmptyOrNull(mData.share_account)) {
            return true;
        }

        if (mAdapter != null && position != 0) {
            if (mDialog == null) {
                mDialog = new MessageDelDialog(this);
            }
            mDialog.setListenter(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                    if (!MyApp.getIsLogin()) {
                        ToastUtil.showFailToast(DoorBellActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
                        return;
                    }
                    MsgClientBellCallDeleteReq mMsgClientBellCallDeleteReq = new MsgClientBellCallDeleteReq(mData.cid);
                    mMsgClientBellCallDeleteReq.timeBegin = mAdapter.getItem(position - 1).timeBegin;
                    MyApp.wsRequest(mMsgClientBellCallDeleteReq.toBytes());
                }
            });
            mDialog.show();
            Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
            vib.vibrate(50);
        }

        return true;
    }

    private void showDelMessageDialog(final int pos) {
        final NotifyDialog dialog = new NotifyDialog(this);
        dialog.setButtonText(R.string.DELETE, R.string.CANCEL);
        dialog.setPosRedTheme(R.drawable.bg_dialogdel_selector, getResources().getColor(R.color.mycount_not_set));
        dialog.show(R.string.DELETE_SURE, new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.confirm:
                        dialog.dismiss();
                        MsgClientBellCallDeleteReq mMsgClientBellCallDeleteReq = new MsgClientBellCallDeleteReq(mData.cid);
                        mMsgClientBellCallDeleteReq.timeBegin = mAdapter.getItem(pos - 1).timeBegin;
                        MyApp.wsRequest(mMsgClientBellCallDeleteReq.toBytes());
                        break;
                    case R.id.cancel:
                        dialog.dismiss();
                        break;
                    default:
                        break;
                }

            }
        }, null);

    }

    @Override
    protected void onStop() {
        super.onStop();
        CacheUtil.saveObject((Serializable) mAdapter.getData(), CacheUtil.getMSG_DOORBELLLIS_KEY(mData.cid));

    }

    private void showNoCallLogView() {
        if (mAdapter.getCount() == 0) {
            mNoCallLogView.setVisibility(View.VISIBLE);
        } else {
            mNoCallLogView.setVisibility(View.GONE);
        }
    }

    @Override
    public void check(int pos) {
        if (!mAdapter.getItem(pos).url.equals("")) {
            MsgData info = new MsgData();
            info.time = mAdapter.getItem(pos).timeBegin;
            List<String> list = new ArrayList<String>();
            list.add(mAdapter.getItem(pos).url);
            info.urllist = list;
            startActivity(
                    new Intent(this, MessagePicturesActivity.class).putExtra(MessagePicturesActivity.TIME, info).putExtra("index", 0));
        }
    }

    @Override
    public void onStartPull(float fromDegress, float toDegress) {
        if (mTitleView.getVisibility() == View.VISIBLE)
            mTitleView.setVisibility(View.GONE);
        if (mProgressView.getVisibility() == View.GONE)
            mProgressView.setVisibility(View.VISIBLE);
        isStartAnim = false;
        startPullAnimation(fromDegress, toDegress);
    }

    @Override
    public void onStartRefresh() {
        if (!isStartAnim && mProgressView.getVisibility() == View.VISIBLE) {
            isStartAnim = true;
            startRefreshAnimation();
            if (MyApp.getIsLogin()) {
                getData();
            } else {
                mProgressView.clearAnimation();
                mProgressView.setVisibility(View.GONE);
                mTitleView.setVisibility(View.VISIBLE);
                ToastUtil.showFailToast(DoorBellActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            }
        }
    }

    @Override
    public void onStartLoadMore() {

    }
}
