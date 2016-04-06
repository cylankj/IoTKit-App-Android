package com.cylan.jiafeigou.activity.efamily.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.JniPlay;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.jiafeigou.activity.efamily.EFamilySettingActivity;
import com.cylan.jiafeigou.activity.efamily.ResendWordsDialog;
import com.cylan.jiafeigou.activity.efamily.audio.RecorderManager;
import com.cylan.jiafeigou.activity.efamily.facetime.FaceTimeActivity;
import com.cylan.jiafeigou.activity.efamily.facetime.FaceTimeCallingActivity;
import com.cylan.jiafeigou.adapter.EFamilyMainAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.HttpRequest;
import com.cylan.jiafeigou.entity.msg.EfamlMsg;
import com.cylan.jiafeigou.entity.msg.HttpResult;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgClientPushSimpleNotice;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOffline;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOnline;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgClientEfamlMsgListReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgClientEfamlMsgListRsp;
import com.cylan.jiafeigou.entity.msg.rsp.MsgSetCidAliasRsp;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-09
 * Time: 09:24
 */

public class EfamilyMainActivity extends BaseActivity implements View.OnClickListener,
        BottomMenuListener, EFamilyMainAdapter.OnReSendListener,
        EFamilyMainAdapter.OnCallBackListener, FaceTimeCallbackListener {

    private static final String TAG = "EfamilyMainActivity";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private EFamilyMainAdapter mAdapter;
    private BottomMenu mBottomMenu;
    private int lastFirstVisibleItem;
    private MsgCidData mData = null;
    private List<HttpRequest> mRequestPool = new ArrayList<>();
    private long pageStartTime;
    private ResendWordsDialog mResendWordsDialog;
    private boolean isFirstTime = true;

    private static final int MSG_SENDWORDS_TIMEOUT = 0x01;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_SENDWORDS_TIMEOUT){
                long time = (long) msg.obj;
                changeWordState(EfamlMsg.SENDFAIL, time, null);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_efamily_main);
        mData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        initView();
        getData();
    }


    private void initView() {
        setTitleName();
        if (StringUtils.isEmptyOrNull(mData.share_account)) {
            setRightImageView(R.drawable.btn_online_set_selector, this);
        }
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setScrollbarFadingEnabled(true);
        mSwipeRefreshLayout.setColorSchemeColors(Color.RED, Color.BLUE);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                getData();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mListView = (ListView) findViewById(R.id.listview);
        mListView.setEmptyView(findViewById(R.id.empty_view));
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (mBottomMenu != null) {
                        if (view.getLastVisiblePosition() == view.getCount() - 1) {
                            mBottomMenu.show();
                        }
                        if (view.getLastVisiblePosition() < lastFirstVisibleItem) {
                            if (mBottomMenu.getVisibility() == View.VISIBLE) {
                                mBottomMenu.dismiss();
                            }
                        }
                    }
                    lastFirstVisibleItem = view.getLastVisiblePosition();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mHandler.post(getCache);
        List<EfamlMsg> cacheList = new ArrayList<>();
        mAdapter = new EFamilyMainAdapter(this, cacheList);
        mAdapter.setOnReSendListener(this);
        mAdapter.setOnCallBackListener(this);
        mAdapter.setListView(mListView);
        mAdapter.setCid(mData.cid);
        mListView.setAdapter(mAdapter);
        mBottomMenu = (BottomMenu) findViewById(R.id.bottom_menu);
        mBottomMenu.setCid(mData.cid);
        mBottomMenu.setOnBommtomMenuListener(this);
        mBottomMenu.findViewById(R.id.facetime).setOnClickListener(this);
        if (PreferenceUtil.getKeyIsFirstToEfamily(this)) {
            PreferenceUtil.setKeyIsFirstToEfamily(this, false);
            mBottomMenu.show();
        } else {
            mBottomMenu.dismiss();
        }
        mBottomMenu.setOnBottomMenuOnTouchListener(new BottomMenu.BottomMenuOnTouch() {
            @Override
            public void onTouch(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setOnClickEnable(false);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    setOnClickEnable(true);
                }
            }

            @Override
            public void overSixtySec() {
                setOnClickEnable(true);
            }
        });

        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        FaceTimeActivity.setOnEFamlCallbackListener(this);
    }

    private void setOnClickEnable(boolean isEnable){
        findViewById(R.id.right_ico).setClickable(isEnable);
        findViewById(R.id.ico_back).setClickable(isEnable);
        mListView.setEnabled(isEnable);
        mAdapter.setOnClick(isEnable);
    }

    private Runnable getCache = new Runnable() {
        @Override
        public void run() {
            List<EfamlMsg> cacheList = (List<EfamlMsg>) CacheUtil.readObject(
                    CacheUtil.getMsgEfamilyHomeListKey(mData.cid));
            if (mAdapter != null && cacheList != null){
                if (cacheList.size() == 0){
                    mListView.setEmptyView(findViewById(R.id.empty_view));
                    return;
                }
                mAdapter.clear();
                mAdapter.addAll(cacheList);
                mListView.setSelection(mAdapter.getCount());
            }
        }
    };

    //TODO 本地保存后不需要再从服务器取列表，先留着看以后是否会用到
    private void getData() {
        if (MyApp.getIsLogin()) {
            MsgClientEfamlMsgListReq req = new MsgClientEfamlMsgListReq(mData.cid);
            req.time = pageStartTime;
            MyApp.wsRequest(req.toBytes());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ImageLoader.getInstance().resume();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_ico:
                startActivity(new Intent(EfamilyMainActivity.this, EFamilySettingActivity.class).putExtra(ClientConstants.CIDINFO, mData));
                break;
            case R.id.facetime:
                geToFaceTime();
                StatService.trackCustomEvent(this, TAG, getString(R.string.EFAMILY_MENU_VIDEOCALL));
                break;

        }
    }

    private void setTitleName() {
        if (!MyApp.getIsConnectServer()) {
            setTitle(String.format(getString(R.string.doorbell_title), mData.mName(), getString(R.string.DOOR_NOT_CONNECT)));
        } else if (mData.net == MsgCidData.CID_NET_CONNECT || mData.net == MsgCidData.CID_NET_OFFLINE) {
            setTitle(mData.mName() + "(" + getString(R.string.OFF_LINE) + ")");
        } else {
            setTitle(mData.mName());
        }
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgHeader) {
        if (msgHeader.msgId == MsgpackMsg.CLIENT_SYNC_CIDONLINE) {
            MsgSyncCidOnline mMsgSyncCidOnline = (MsgSyncCidOnline) msgHeader;
            String cid = mMsgSyncCidOnline.cid;
            int net = mMsgSyncCidOnline.net;
            if (mData != null) {
                if (mData.cid.equals(cid)) {
                    mData.net = net;
                    mData.name = mMsgSyncCidOnline.name;
                    mData.version = mMsgSyncCidOnline.version;
                    setTitleName();
                }

            }

        } else if (msgHeader.msgId == MsgpackMsg.CLIENT_SYNC_CIDOFFLINE) {
            MsgSyncCidOffline mMsgSyncCidOffline = (MsgSyncCidOffline) msgHeader;
            String cid = mMsgSyncCidOffline.cid;
            if (mData != null) {
                if (mData.cid.equals(cid)) {
                    mData.net = MsgCidData.CID_NET_OFFLINE;
                    mData.alias = "";
                    setTitleName();
                }
            }
        } else if (msgHeader.msgId == MsgpackMsg.CLIENT_SETCIDALIAS_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgHeader;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                MsgSetCidAliasRsp mMsgSetCidAliasRsp = (MsgSetCidAliasRsp) mRspMsgHeader;
                String cid = mMsgSetCidAliasRsp.cid;
                String alias = mMsgSetCidAliasRsp.alias;
                if (mData.cid.equals(cid)) {
                    mData.alias = alias;
                    setTitleName();
                }
            }
        } else if (msgHeader.msgId == MsgpackMsg.CLIENT_EFAML_MSG_LIST_REQ) {   //不需要服务器返回数据
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgHeader;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                MsgClientEfamlMsgListRsp rsp = (MsgClientEfamlMsgListRsp) mRspMsgHeader;
                if (pageStartTime == 0) {
                    mAdapter.clear();
                    mListView.setEmptyView(findViewById(R.id.empty_view));
                }
                for (EfamlMsg em : rsp.msgList) {
                    if (!mAdapter.getData().contains(em)) {
                        mAdapter.add(em);
                    }
                }

                if (isFirstTime) {
                    isFirstTime = false;
                    addNativeVoiceList();
                } else {
                    Collections.sort(mAdapter.getData());
                    mAdapter.notifyDataSetChanged();
                }

                mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
                if (pageStartTime != 0) {
                    if (rsp.msgList.size() > 0) {
                        mListView.setSelection(rsp.msgList.size() - 1);
                    } else {
                        mListView.setSelection(0);
                    }
                } else {
                    mListView.setSelection(mAdapter.getCount() - 1);
                }

                if (!rsp.msgList.isEmpty() && rsp.msgList.size() > 0) {
                    pageStartTime = rsp.msgList.get(rsp.msgList.size() - 1).timeBegin;
                }
                mSwipeRefreshLayout.setRefreshing(false);

            }
        } else if (MsgpackMsg.CLIENT_DEL_EFAML_MSG_RSP == msgHeader.msgId) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgHeader;
            if (mRspMsgHeader.caller.equals(mData.cid)) {
                mAdapter.clear();
                clearFolder();
                mListView.setEmptyView(findViewById(R.id.empty_view));
            }
        } else if (MsgpackMsg.CLIENT_PUSH_SIMPLE_NOTICE == msgHeader.msgId) {
            MsgClientPushSimpleNotice rsp = (MsgClientPushSimpleNotice) msgHeader;
            if (rsp.push_type == MsgClientPushSimpleNotice.TYPE_EFAML_UPDATE) {
                if (rsp.caller.equals(mData.cid)) {
                    pageStartTime = 0;
//                    getData();
                }
            }
        }
    }

    @Override
    public void dismiss() {
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
    }

    @Override
    public void show() {
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    }

    @Override
    public void sendword(EfamlMsg bean) {
        String url = "/index.php?sessid=" + PreferenceUtil.getSessionId(this) + "&mod=client&act=voiceMsg&timeBegin=" + bean.timeBegin + "&timeDuration=" + bean.timeDuration + "&cid=" + mData.cid;
        int requestid = JniPlay.HttpPostFile(Constants.WEB_ADDR, Constants.WEB_PORT, url, bean.filePath(mData.cid));
        DswLog.i("send words request ID:" + requestid);
        mRequestPool.add(new HttpRequest(bean, requestid));
        mAdapter.add(bean);
        mListView.setSelection(mAdapter.getCount() - 1);
        Message msg = mHandler.obtainMessage(MSG_SENDWORDS_TIMEOUT);
        msg.obj = bean.timeBegin;
        mHandler.sendMessageDelayed(msg, 60000);
    }

    @Override
    public void httpDone(HttpResult mResult) {
        try {
            mHandler.removeMessages(MSG_SENDWORDS_TIMEOUT);
            EfamlMsg eMsg = HttpRequest.queryTimeBeginByRequestid(mRequestPool, mResult.requestId);
            if (mResult.ret == Constants.HTTP_RETOK) {
                if (!StringUtils.isEmptyOrNull(mResult.result)) {
                    JSONObject jsonobject = new JSONObject(mResult.result);
                    String act = jsonobject.has(Constants.ACT) ? jsonobject.getString(Constants.ACT) : "";
                    int ret = jsonobject.has(Constants.RET) ? jsonobject.getInt(Constants.RET) : 0;
                    if (act.equals("voiceMsg_rsp")) {
                        String filename = jsonobject.getString("filename");
                        String url = jsonobject.getString("url");
                        long time = Long.parseLong(filename.substring(0, filename.indexOf("_")));
                        if (Constants.RETOK == ret) {
                            changeWordState(EfamlMsg.SENDSUC, time, url);
                        } else {
                            changeWordState(EfamlMsg.SENDFAIL, time, url);
                        }
                    }
                }
            } else {
                if (eMsg != null) {
                    changeWordState(EfamlMsg.SENDFAIL, eMsg.timeBegin, null);
                }
            }
            removeById(mResult.requestId);
        } catch (JSONException E) {
            E.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        ImageLoader.getInstance().stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.distory();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CacheUtil.saveObject((Serializable) mAdapter.getData(), CacheUtil.getMsgEfamilyHomeListKey(mData.cid));
            }
        });
    }

    private void changeWordState(int state, long time, String url) {
        for (int i = 0, size = mAdapter.getCount(); i < size; i++) {
            if (mAdapter.getItem(i).timeBegin == time) {
                mAdapter.getItem(i).send_state = state;
                if (url != null) mAdapter.getItem(i).url = url;
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void addNativeVoiceList() {
        File file = new File(PathGetter.getRecordAudioDirPath(EfamilyMainActivity.this, mData.cid));
        if (file.exists() && file.isDirectory()) {
            File[] filelist = file.listFiles();
            for (int i = 0, size = filelist.length; i < size; i++) {
                if (filelist[i].isFile()) {
                    EfamlMsg bean = new EfamlMsg();
                    bean.isRead = 0;
                    bean.timeDuration = RecorderManager.getWordsDuration(filelist[i].getAbsolutePath());
                    bean.timeBegin = Long.parseLong(filelist[i].getName().replaceAll(PathGetter.FILE_SUFFIX, ""));
                    bean.msgType = EfamlMsg.MSG_WORD;
                    bean.isPlay = false;
                    bean.url = null;
                    bean.isRead = 0;
                    bean.send_state = EfamlMsg.SENDFAIL;
                    if (!mAdapter.getData().contains(bean) && bean.timeDuration != 0) {
                        mAdapter.add(bean);
                    }
                }
            }

        }
        Collections.sort(mAdapter.getData());
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void resend(final EfamlMsg mEfamlMsg, final int position) {
        if (mResendWordsDialog == null) {
            mResendWordsDialog = new ResendWordsDialog(this);
        }
        mResendWordsDialog.setResendListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new File(mEfamlMsg.filePath(mData.cid)).exists()) {
                    mEfamlMsg.send_state = EfamlMsg.SENDING;
                    String url = "/index.php?sessid=" + PreferenceUtil.getSessionId(
                            EfamilyMainActivity.this) + "&mod=client&act=voiceMsg&timeBegin="
                            + mEfamlMsg.timeBegin + "&timeDuration=" + mEfamlMsg.timeDuration
                            + "&cid=" + mData.cid;
                    int requestid = JniPlay.HttpPostFile(Constants.WEB_ADDR, Constants.WEB_PORT,
                            url, mEfamlMsg.filePath(mData.cid));
                    mRequestPool.add(new HttpRequest(mEfamlMsg, requestid));
                    mAdapter.notifyDataSetChanged();
                    mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
                }
                mResendWordsDialog.dismiss();
            }
        });
        mBottomMenu.dismiss();
        mResendWordsDialog.show();
    }

    private void clearFolder() {
        File file = new File(PathGetter.getRecordAudioDirPath(EfamilyMainActivity.this, mData.cid));
        if (file.exists() && file.isDirectory()) {
            File[] filelist = file.listFiles();
            for (int i = 0, size = filelist.length; i < size; i++) {
                filelist[i].delete();
            }

        }
    }

    @Override
    public void callback() {
        geToFaceTime();
    }

    private void geToFaceTime() {
        if (!MyApp.getIsLogin()) {
            ToastUtil.showFailToast(EfamilyMainActivity.this, "(-" + MyApp.getMsgID() + ")"
                    + getString(R.string.GLOBAL_NO_NETWORK));
            return;
        }
        if (AppManager.getAppManager().isActivityTop(FaceTimeCallingActivity.class.getName()))
            return;
        if (mData.net == MsgCidData.CID_NET_CONNECT || mData.net == MsgCidData.CID_NET_OFFLINE){
            ToastUtil.showFailToast(this, getString(R.string.OFFLINE_ERR));
            return;
        }
        startActivity(new Intent(EfamilyMainActivity.this, FaceTimeCallingActivity.class)
                .putExtra(ClientConstants.CIDINFO, mData).putExtra("page_start", pageStartTime));
    }

    private void removeById(int id) {
        for (HttpRequest request : mRequestPool) {
            if (request.getRequestid() == id) {
                mRequestPool.remove(request);
                break;
            }
        }
    }

    @Override
    public void disconnectServer() {
        setTitleName();
    }

    @Override
    public void connectServer() {
        setTitleName();
    }

    @Override
    public void missCallByOverTime(boolean isCalled, String cid) {
        saveCache(cid, isCalled, 0, 0);
    }

    @Override
    public void missCallByCancel(boolean isCalled, String cid) {
        saveCache(cid, isCalled, 0, 0);
    }

    @Override
    public void haveAnswered(boolean isCalled, int timeDuration, String cid) {
        saveCache(cid, isCalled, 1, timeDuration);
    }

    private void updateEFamlList(boolean isCalled, int isRead, int timeDuration){
        EfamlMsg efamlMsg = faceTimeData(isCalled, isRead, timeDuration);
        if (!mAdapter.getData().contains(efamlMsg))
            mAdapter.add(efamlMsg);
        mListView.setSelection(mAdapter.getCount() - 1);
        mAdapter.notifyDataSetChanged();

    }

    private void saveCache(final String cid, final boolean isCalled, final int isRead, final int timeDuration){
        updateEFamlList(isCalled, isRead, timeDuration);

        mHandler.post(new Runnable() {
                @Override
                public void run() {
                    List<EfamlMsg> cacheList = (List<EfamlMsg>) CacheUtil.readObject(
                            CacheUtil.getMsgEfamilyHomeListKey(cid));
                    EfamlMsg efamlMsg = faceTimeData(isCalled, isRead, timeDuration);
                    if (!cacheList.contains(efamlMsg)){
                        cacheList.add(efamlMsg);
                        CacheUtil.saveObject((Serializable)cacheList, CacheUtil.getMsgEfamilyHomeListKey(cid));
                    }
                }
            });
    }

    private EfamlMsg faceTimeData(boolean isCalled, int isRead, int timeDuration){
        EfamlMsg faceTime = new EfamlMsg();
        faceTime.isRead = isRead;
        faceTime.timeDuration = timeDuration;
        faceTime.timeBegin = System.currentTimeMillis() / 1000 + PreferenceUtil.getKeyNtpTimeDiff(this);
        faceTime.msgType = isCalled ?  EfamlMsg.PASSIVE_CALL : EfamlMsg.ACTIVE_CALL;
        return faceTime;
    }
}