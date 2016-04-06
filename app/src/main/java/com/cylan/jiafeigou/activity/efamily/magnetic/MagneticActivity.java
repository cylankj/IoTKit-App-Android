package com.cylan.jiafeigou.activity.efamily.magnetic;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.adapter.MagneticAdapter;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.base.RootActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MagStatusList;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgPush;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOffline;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOnline;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgClientMagGetWarnReq;
import com.cylan.jiafeigou.entity.msg.req.MsgClientMagStatusListReq;
import com.cylan.jiafeigou.entity.msg.rsp.LoginRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientMagGetWarnRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientMagStatusListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgSetCidAliasRsp;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.PullToZoomListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangc on 2015/12/16.
 */
public class MagneticActivity extends RootActivity implements View.OnClickListener, PullToZoomListView.OnScrollStateChangeListener {

    private static final String TAG = MagneticActivity.class.getClass().getName();

    private MsgCidData info;

    private PullToZoomListView listView;
    private TextView noContacts;
    private RelativeLayout titleLayout;
    private ImageView ic_process;
    private TextView title;
    private ImageView set_remind;
    private ImageView doorStatus;
    private View headView;

    MagneticAdapter mAdapter;

    private boolean isStartAnim = false;

    private static final int ON = 1;
    private static final int OFF = 0;

    private List<MagStatusList> lists;

    private static final int MSG_REFRESH_TIMEOUT = 0x01;

    private long timebegin;
    private long timeend;

    private boolean isReturnResult = true;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_TIMEOUT:
                    ic_process.clearAnimation();
                    ic_process.setVisibility(View.GONE);
                    title.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetic_main);
        info = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        initView();
        initData();
        getWarnInfo();
    }

    private void initView() {
        listView = (PullToZoomListView) findViewById(R.id.magnetic_list_info);
        noContacts = (TextView) findViewById(R.id.magnetic_no_contacts);
        titleLayout = (RelativeLayout) findViewById(R.id.magnetic_title_layout);
        ImageView ic_back = (ImageView) findViewById(R.id.magnetic_back);
        ImageView ic_set = (ImageView) findViewById(R.id.magnetic_set);
        ic_process = (ImageView) findViewById(R.id.magnetic_progress);
        set_remind = (ImageView) findViewById(R.id.magnetic_set_remind);
        title = (TextView) findViewById(R.id.magnetic_title);

        title.setText(info.mName());
        if (info != null && !StringUtils.isEmptyOrNull(info.share_account)) {
            ic_set.setVisibility(View.GONE);
        }

        ic_back.setOnClickListener(this);
        ic_set.setOnClickListener(this);

        setData();
    }

    private void initData() {
        lists = (List<MagStatusList>)
                CacheUtil.readObject(CacheUtil.getMsg_MagneticList_Key(info.cid));
        if (lists != null && lists.size() > 0) {
            MagStatusList list = lists.get(0);
            setHeadStatus(list.status);
            mAdapter.addAll(lists);
            mAdapter.notifyDataSetChanged();
        } else {
            showNoContacts();
        }
    }

    private void setData() {
        mAdapter = new MagneticAdapter(this, new ArrayList<MagStatusList>());
        listView.getHeaderView().setBackgroundColor(Color.parseColor("#66BB6A"));
        headView = getLayoutInflater().inflate(R.layout.layout_magnetic_header, null);
        doorStatus = (ImageView) headView.findViewById(R.id.magnetic_head_status);
        listView.getHeaderContainer().addView(headView);
        listView.setHeaderView();
        listView.setOnScrollStateChangeListener(this);
        listView.setAdapter(mAdapter);
    }

    private void getData(long begin, long end) {
        MsgClientMagStatusListReq magStatusListReq = new MsgClientMagStatusListReq(info.cid);
        magStatusListReq.timeBegin = begin;
        magStatusListReq.timeEnd = end;
        timebegin = begin;
        timeend = end;
        MyApp.wsRequest(magStatusListReq.toBytes());
        Log.d("Magnetic", "getData-->" + magStatusListReq.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApp.getIsLogin()) {
            getData(getBeginTime(), 0);
        } else {
            ToastUtil.showFailToast(MagneticActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.magnetic_back:
                finish();
                break;
            case R.id.magnetic_set:
                startActivity(new Intent(this, MagneticSetActivity.class).putExtra(ClientConstants.CIDINFO, info));
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (lists != null) {
            CacheUtil.saveObject((Serializable) lists,
                    CacheUtil.getMsg_MagneticList_Key(info.cid));
        }
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        if (msgpackMsg.msgId == MsgpackMsg.CLIENT_MAG_STATUS_LIST_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (mRspMsgHeader.caller.equals(info.cid)) {
                ic_process.clearAnimation();
                ic_process.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                isReturnResult = true;
                if (Constants.RETOK == mRspMsgHeader.ret) {
                    MsgClientMagStatusListRsp listRsp = (MsgClientMagStatusListRsp) mRspMsgHeader;
                    if (lists == null)
                        lists = new ArrayList<>();
                    if (timebegin != 0 && timeend == 0) {
                        if (listRsp.lists.size() > 0 && mAdapter.getCount() > 0) {
                            if (listRsp.lists.get(listRsp.lists.size() - 1).time < mAdapter.getItem(mAdapter.getCount() - 1).time) {
                                return;
                            }
                        }
                        lists.addAll(0, listRsp.lists);
                    } else {
                        if (timebegin == 0 && timeend != 0) {
                            if (listRsp.lists.size() > 0 && mAdapter.getCount() > 0) {
                                if (listRsp.lists.get(listRsp.lists.size() - 1).time > mAdapter.getItem(mAdapter.getCount() - 1).time) {
                                    return;
                                }
                            }
                        }
                        lists.addAll(listRsp.lists);
                    }

                    mAdapter.clear();
                    mAdapter.addAll(lists);
                    mAdapter.notifyDataSetChanged();
                    if (doorStatus != null) {
                        setHeadStatus(listRsp.curStatus);
                    }
                    showNoContacts();

                } else {
                    ToastUtil.showFailToast(this, mRspMsgHeader.msg);
                }
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_PUSH) {
            MsgPush msgPush = (MsgPush) msgpackMsg;
            handlePush(msgPush);
            showNoContacts();
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SETCIDALIAS_RSP) {
            MsgSetCidAliasRsp aliasRsp = (MsgSetCidAliasRsp) msgpackMsg;
            if (aliasRsp.ret == Constants.RETOK) {
                if (aliasRsp.cid.equals(info.cid)) {
                    info.name = aliasRsp.alias;
                    title.setText(info.mName());
                }
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SYNC_CIDONLINE) {
            MsgSyncCidOnline mMsgSyncCidOnline = (MsgSyncCidOnline) msgpackMsg;
            String cid = mMsgSyncCidOnline.cid;
            int net = mMsgSyncCidOnline.net;
            if (info != null) {
                if (info.cid.equals(cid)) {
                    info.net = net;
                    info.name = mMsgSyncCidOnline.name;
                    info.version = mMsgSyncCidOnline.version;
                    title.setText(info.mName());
                }

            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_SYNC_CIDOFFLINE) {
            MsgSyncCidOffline mMsgSyncCidOffline = (MsgSyncCidOffline) msgpackMsg;
            String cid = mMsgSyncCidOffline.cid;
            if (info != null) {
                if (info.cid.equals(cid)) {
                    info.net = MsgCidData.CID_NET_OFFLINE;
                    info.name = "";
                    if (!MyApp.getIsLogin()) {
                        title.setText(String.format(getString(R.string.doorbell_title), info.mName(), getString(R.string.DOOR_NOT_CONNECT)));
                    } else if (info.net == MsgCidData.CID_NET_CONNECT || info.net == MsgCidData.CID_NET_OFFLINE) {
                        title.setText(String.format(getString(R.string.doorbell_title), info.mName(), getString(R.string.OFF_LINE)));
                    } else {
                        title.setText(info.mName());
                    }
                }
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_RELOGIN_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (mRspMsgHeader.ret == Constants.RETOK) {
                LoginRsp rsp = (LoginRsp) mRspMsgHeader;
                int sceneSize = rsp.data.size();
                boolean has = false;
                for (int i = 0; i < sceneSize; i++) {
                    int deviceSize = rsp.data.get(i).data.size();
                    for (int j = 0; j < deviceSize; j++) {
                        MsgCidData mcd = rsp.data.get(i).data.get(j);
                        if (mcd.cid.equals(info.cid)) {
                            info.net = mcd.net;
                            info.name = mcd.name;
                            info.version = mcd.version;
                            has = true;
                            break;
                        }
                    }
                    if (has)
                        break;
                }
            }
        }else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_MAG_GET_WARN_RSP){
            RspMsgHeader rspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == rspMsgHeader.ret){
                MsgClientMagGetWarnRsp getWarnRsp = (MsgClientMagGetWarnRsp) rspMsgHeader;
                PreferenceUtil.setKeyMagWarnRsp(this, getWarnRsp.warn == 1);
            }
        }
    }

    private void handlePush(MsgPush msgPush) {
        if (lists == null)
            lists = new ArrayList<>();
        switch (msgPush.push_type) {
            case ClientConstants.PUSH_TYPE_MAGNET_OFF:
                MagStatusList magnetOn = new MagStatusList();
                magnetOn.status = OFF;
                magnetOn.time = msgPush.time;
                mAdapter.addFirst(magnetOn);
                mAdapter.notifyDataSetChanged();
                setHeadStatus(OFF);
                lists.add(0, magnetOn);
                break;
            case ClientConstants.PUSH_TYPE_MAGNET_ON:
                MagStatusList magnetOff = new MagStatusList();
                magnetOff.status = ON;
                magnetOff.time = msgPush.time;
                mAdapter.addFirst(magnetOff);
                mAdapter.notifyDataSetChanged();
                setHeadStatus(ON);
                lists.add(0, magnetOff);
                break;
        }
    }

    private void showNoContacts() {
        if (mAdapter.getCount() == 0) {
            noContacts.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            headView.setVisibility(View.GONE);
            ImageView cover = (ImageView) findViewById(R.id.magnetic_title_cover);
            cover.getBackground().setAlpha(100);
            cover.setVisibility(View.VISIBLE);
            ImageView background = (ImageView) findViewById(R.id.magnetic_title_background);
            background.setVisibility(View.VISIBLE);
            MyImageLoader.loadTitlebarImage(this, background);
        } else {
            noContacts.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            headView.setVisibility(View.VISIBLE);
            findViewById(R.id.magnetic_title_background).setVisibility(View.GONE);
            findViewById(R.id.magnetic_title_cover).setVisibility(View.GONE);
        }
    }

    private void setHeadStatus(int status) {
        switch (status) {
            case OFF:
                doorStatus.setImageResource(R.drawable.img_door_close);
                listView.getHeaderView().setBackgroundColor(Color.parseColor("#66BB6A"));
                titleLayout.setBackgroundColor(Color.parseColor("#66BB6A"));
                break;
            case ON:
                doorStatus.setImageResource(R.drawable.img_door_open);
                listView.getHeaderView().setBackgroundColor(Color.parseColor("#E57373"));
                titleLayout.setBackgroundColor(Color.parseColor("#E57373"));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ic_process.clearAnimation();
    }


    private void startPullAnimation(float fromDegress, float toDegress) {
        ic_process.clearAnimation();
        ObjectAnimator animation = ObjectAnimator.ofFloat(ic_process, "rotation", fromDegress, toDegress);
        animation.setStartDelay(0);
        animation.start();
    }

    private void startRefreshAnimation() {
        ic_process.clearAnimation();

        RotateAnimation mRotateAnimation = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setRepeatCount(-1);
        mRotateAnimation.setRepeatMode(Animation.RESTART);
        mRotateAnimation.setDuration(600);
        ic_process.startAnimation(mRotateAnimation);
    }

    @Override
    public void onStartPull(float fromDegress, float toDegress) {
        /*if (title.getVisibility() == View.VISIBLE)
            title.setVisibility(View.GONE);
        if (ic_process.getVisibility() == View.GONE)
            ic_process.setVisibility(View.VISIBLE);
        isStartAnim = false;
        startPullAnimation(fromDegress, toDegress);*/
    }

    @Override
    public void onStartRefresh() {
       /* if (!isStartAnim && ic_process.getVisibility() == View.VISIBLE) {
            isStartAnim = true;
            startRefreshAnimation();
            if (MyApp.getIsLogin()) {
                getData(getBeginTime(), 0);
                mHandler.sendEmptyMessageDelayed(MSG_REFRESH_TIMEOUT, 15000);
            } else {
                ic_process.clearAnimation();
                ic_process.setVisibility(View.GONE);
                title.setVisibility(View.VISIBLE);
                ToastUtil.showFailToast(MagneticActivity.this, "(-" + MyApp.getMsgID() + ")" + getString(R.string.GLOBAL_NO_NETWORK));
            }
        }*/
    }

    @Override
    public void onStartLoadMore() {
        if (isReturnResult) {
            isReturnResult = false;
            getData(0, getEndTime());
        }
    }

    private long getBeginTime() {
        if (mAdapter != null && mAdapter.getCount() > 0) {
            return mAdapter.getItem(0).time;
        } else {
            return 0;
        }
    }

    private long getEndTime() {
        if (mAdapter != null && mAdapter.getCount() > 0) {
            return mAdapter.getItem(mAdapter.getCount() - 1).time;
        } else {
            return 0;
        }
    }

    private void getWarnInfo(){
        MsgClientMagGetWarnReq warnReq = new MsgClientMagGetWarnReq("", info.cid);
        JniPlay.SendBytes(warnReq.toBytes());
    }
}
