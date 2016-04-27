package com.cylan.jiafeigou.activity.video.addDevice;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.main.ActivityIsResumeManager;
import com.cylan.jiafeigou.activity.video.AddCameraHelpActivity;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.ClientUDP;
import com.cylan.jiafeigou.engine.RequestMessage;
import com.cylan.jiafeigou.entity.MyScanResult;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgClientPost;
import com.cylan.jiafeigou.entity.msg.MsgSyncCidOnline;
import com.cylan.jiafeigou.entity.msg.RspMsgHeader;
import com.cylan.jiafeigou.entity.msg.req.MsgBindCidReq;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.listener.UDPMessageListener;
import com.cylan.jiafeigou.utils.CIDCheck;
import com.cylan.jiafeigou.utils.CacheUtil;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.utils.WifiUtils;
import com.cylan.jiafeigou.worker.ConfigWIfiWorker;
import com.cylan.jiafeigou.worker.EnableWifiWorker;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.MsgpackMsg;
import com.cylan.publicApi.NetUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.cylan.support.DswLog;

public class AddVideoActivity extends BaseActivity implements OnClickListener, UDPMessageListener, SearchDeviceFragment.OnSearchDeviceListener, ChooseDeviceFragment.OnSelectDeviceListener, SubmitWifiInfoFragment.OnSubmitWifiInfoListener, BindResultFragment.OnCompleteButtonClickListener, UpgradeFragement.OnUpgradeButtonClickListener {

    // page tag
    protected static final int SEARCHPAGE = 0x00;
    protected static final int CHOOSEPAGE = 0x01;
    protected static final int SETPAGE = 0x02;
    protected static final int ADDPAGE = 0x03;
    protected static final int UPGRADE = 0x04;

    public static List<String> newCid = new ArrayList<>();

    protected String[] titles = null;

    protected ImageView mHelpView;

    protected SearchDeviceFragment mSearchDeviceFragment;
    protected ChooseDeviceFragment mChooseDeviceFragment;
    protected SubmitWifiInfoFragment mSubmitWifiInfoFragment;
    protected BindResultFragment mBindResultFragment;
    protected UpgradeFragement mUpgradeFragement;


    protected FragmentManager fm;
    protected FragmentTransaction ft;

    public WifiManager wm;

    private boolean isAuthenticating = false;
    private boolean isAuthenticating_state = false;
    private boolean isHasSend = false;
    private boolean isSearchWifiList = true;
    private boolean isSetWifiSuc = false;
    protected static final int MSG_SCAN_FINISH = 0x00;
    protected static final int MSG_SCAN_OVER_TIME = 0x01;
    private static final int MSG_RECOVER_NET = 0x02;
    private static final int BIND_VIDEO_OVERTIME = 0x05;
    private static final int CONN_AP_OVERTIME = 0x06;
    private static final int MSG_UCOS_UPGRADE = 0x07;
    private static final int MSG_UCOS_UPGRADE_COMPLETE = 0x08;
    private static final int MSG_UCOS_UPGRADE_OVERTIME = 0x09;
    private static final int MSG_CONNCT_OVERTIME = 0x10;
    private static final int MSG_SENDPING_DALY = 0x11;
    private static final int MSG_CONFIG_WIFI = 0x12;
    private static final int RESULT_CONNECT_HAND = 0x13;
    private static final int MSG_RECONNECT_UDP = 0x14;
    private static final int SEND_PING_OVERTIME = 0x15;

    private MyScanResult mChooseDevice;
    public List<ScanResult> results;
    private String mAlias;


    private int net;
    private String bindcid = null;
    private NotifyDialog nd = null;

    /**
     * 定时器 *
     */
    private int recLen = 0;
    private Timer timer = null;


    private boolean isStartSendUrl = false;

    private NotifyDialog mUpgradeDialog;

    private int flag = 0;


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                    if (!isSearchWifiList) {
                        isSearchWifiList = true;
                        onWifiList();
                    }
                } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (parcelableExtra != null) {
                        NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;

                        switch (networkInfo.getState()) {
                            case CONNECTING:
                                isAuthenticating = true;
                                DswLog.i("CONNECTING--->" + networkInfo.getExtraInfo());
                                break;
                            case CONNECTED:
                                DswLog.i("CONNECTED--->" + networkInfo.getExtraInfo() + "  isAuthenticating_state:" + isAuthenticating_state
                                        + "  isAuthenticating:" + isAuthenticating);
                                if (isAuthenticating) {
                                    WifiInfo wi = wm.getConnectionInfo();
                                    if (isAuthenticating_state) {
                                        if (isMyDeviceByWifi(wi.getSSID())) {
                                            if (!isHasSend) {
                                                mHandler.sendEmptyMessageDelayed(MSG_SENDPING_DALY, 1000);
                                                mHandler.removeMessages(CONN_AP_OVERTIME);
                                                isHasSend = true;
                                            }
                                        }
                                    }
                                }
                                break;
                            case DISCONNECTED:
                                DswLog.i("DISCONNECTED--->" + networkInfo.getExtraInfo());
                                if (isStartSendUrl) {
                                    isStartSendUrl = false;
                                    mHandler.removeMessages(MSG_UCOS_UPGRADE_OVERTIME);
                                    showDiscountFromApDialog();
                                }

                                break;
                            default:
                                break;
                        }
                    }

                }
            } catch (Exception e) {
                DswLog.i("BroadcastReceiver--->" + e.toString());
                DswLog.ex(e.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addvideo);
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        setTitle(R.string.DEVICES_TITLE);
        setBackBtnOnClickListener(this);

        mHelpView = (ImageView) findViewById(R.id.add_help);
        mHelpView.setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, filter);

        ClientUDP.getInstance().setUDPMsgListener(this);

        fm = getFragmentManager();

        setViewVisibly(SEARCHPAGE);

        showGuide();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityIsResumeManager.getActivityIsResumeListener() != null)
            ActivityIsResumeManager.getActivityIsResumeListener().isResume();
    }

    protected void showGuide() {
        if (PreferenceUtil.getFirstAddCarame(this)) {
            showUseGuideView(R.drawable.add_camera_guide, true);
            PreferenceUtil.setFirstAddCarame(this, false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                onBackPressed();
                break;
            case R.id.add_help:
                startActivity(new Intent(getApplicationContext(), AddCameraHelpActivity.class));
                overridePendingTransition(R.anim.slide_down_in, 0);
                break;
            default:
                break;
        }

    }

    private void showUnCompleteDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.setButtonText(R.string.CARRY_ON, R.string.GIVE_UP);
        mDialog.show(R.string.BIND_INFO, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        }, new OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (bindcid != null)
//                    MyApp.wsRequest(RequestMessage.getMsgUnbindCidReq(bindcid).toBytes());
                mDialog.dismiss();
                recoverWifiConfig();
                finish();
            }
        });
    }
    //先关,再开wifi,比较合适.
    protected void getWifiList() {
        isSearchWifiList = false;
        wm.setWifiEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wm.setWifiEnabled(true);
            }
        }, 100);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wm.startScan();
            }
        }, 500);
        mHandler.sendEmptyMessageDelayed(MSG_SCAN_OVER_TIME, 25000);
    }

    protected void onWifiList() {
        if (results != null && results.size() > 0)
            results.clear();
        results = wm.getScanResults();
        Gson gson = new Gson();
        DswLog.i("SCAN_WIFILIST--->" + gson.toJson(results));
        if (results.size() == 0) {
            mProgressDialog.dismissDialog();
            mHandler.removeMessages(MSG_SCAN_OVER_TIME);
            showOpenPositionNotify();
            isSearchWifiList = false;
            return;
        }
        List<MyScanResult> list = new ArrayList<>();
        if (results != null && !results.isEmpty()) {
            for (ScanResult info : results) {
                MyScanResult scan = new MyScanResult();
                scan.scanResult = info;
                if (isMyDeviceByWifi(scan.scanResult.SSID)) {
                    list.add(scan);
                }
            }
        }
        Message msg = mHandler.obtainMessage(MSG_SCAN_FINISH);
        msg.obj = list;
        mHandler.sendMessage(msg);
    }

    protected Handler mHandler = new Handler() {
        ArrayList<MyScanResult> list = null;

        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCAN_FINISH:
                    removeMessages(MSG_SCAN_OVER_TIME);
                    list = (ArrayList<MyScanResult>) msg.obj;
                    // don't use break;
                case MSG_SCAN_OVER_TIME:
                    mProgressDialog.dismissDialog();
                    scanApComplete(list);
                    break;
                case MSG_RECOVER_NET:
                    recoverWifiConfig();
                    break;
                case BIND_VIDEO_OVERTIME:
                    mProgressDialog.dismissDialog();
                    if (mBindResultFragment != null) {
                        mBindResultFragment.setCompleteBtnVisiblty(View.VISIBLE);
                        mBindResultFragment.setConnStateViewResours(R.drawable.addvideo_failed);
                        mBindResultFragment.setCompleteBtnText(R.string.TRY_AGAIN);
                        mBindResultFragment.stopAnimation();
                        mBindResultFragment.setConnViewVisiblty(View.GONE);
                        if (isSetWifiSuc) {
                            mBindResultFragment.setStateTextViewText(R.string.ADD_ERR_INFO);
                        } else {
                            mBindResultFragment.setStateTextViewText(R.string.TIMEOUT_TRY_AGIN);
                        }
                    }

                    break;
                case CONN_AP_OVERTIME:
                    removeMessages(MSG_CONFIG_WIFI);
                    mProgressDialog.dismissDialog();
                    WifiInfo info = wm.getConnectionInfo();
                    if (flag == 0 && !isMyDeviceByWifi(info.getSSID())) {
                        onSelectDevice(mChooseDevice);
                        flag++;
                    } else if (flag == 1 && !isMyDeviceByWifi(info.getSSID()) || !isAuthenticating_state) {
                        removeMessages(CONN_AP_OVERTIME);
                        flag = 0;
                        if (isMyDeviceByWifi(info.getSSID())) {
                            mHandler.sendEmptyMessageDelayed(MSG_RECONNECT_UDP, 1000);
                            break;
                        }
                        showConnectByHand();
                    } else {
                        if (isAuthenticating_state) {
                            mProgressDialog.showDialog(getString(R.string.connecting_ap));
                            mHandler.sendEmptyMessageDelayed(MSG_RECONNECT_UDP, 1000);
                            break;
                        }
                        recoverWifiConfig();
                        ToastUtil.showFailToast(AddVideoActivity.this, getString(R.string.conn_ap_overtime));
                        finish();
                    }
                    break;
                case MSG_UCOS_UPGRADE:
                    mHandler.removeMessages(CONN_AP_OVERTIME);
                    mProgressDialog.dismissDialog();
                    showUpgradeDialog((String) msg.obj);
                    break;
                case MSG_UCOS_UPGRADE_COMPLETE:
                    if (timer != null) {
                        timer.cancel();
                    }
                    if (mUpgradeFragement != null) {
                        mUpgradeFragement.showUpgradeAnimation(MsgCidlistRsp.getInstance().isHasBindDeviceByCid(bindcid) ? R.string.SAVE : R.string.RE_ADD_BUTTONG);
                    }
                    break;

                case MSG_UCOS_UPGRADE_OVERTIME:
                    isStartSendUrl = false;
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                        recLen = 0;
                    }
                    showUpgradeOverTimeDialog();
                    break;
                case MSG_CONNCT_OVERTIME:
                    mProgressDialog.dismissDialog();

                    if (mBindResultFragment != null) {
                        mBindResultFragment.setCompleteBtnVisiblty(View.VISIBLE);
                        mBindResultFragment.setConnStateViewResours(R.drawable.addvideo_failed);
                        mBindResultFragment.setCompleteBtnText(R.string.WELL_OK);
                        mBindResultFragment.stopAnimation();
                        mBindResultFragment.setConnViewVisiblty(View.GONE);
                        mBindResultFragment.setStateTextViewText(R.string.NO_NERWORK_CHEAKDEVICE);
                    }
                    break;
                case MSG_SENDPING_DALY:
                    authenticating_finish();
                    break;
                case MSG_CONFIG_WIFI:
                    isAuthenticating_state = (boolean) msg.obj;
                    DswLog.d("rev MSG_CONFIG_WIFI--isAuthenticating_state" + isAuthenticating_state);
                    if (!isAuthenticating_state) {
                        mHandler.removeMessages(CONN_AP_OVERTIME);
                        mHandler.sendEmptyMessage(CONN_AP_OVERTIME);
                    }
                    break;
                case MSG_RECONNECT_UDP:
                    authenticating_finish();
                    break;
                case SEND_PING_OVERTIME:
                    DswLog.e("SEND_PING_OVERTIME\t" + getString(R.string.conn_ap_overtime));
                    mProgressDialog.dismissDialog();
                    recoverWifiConfig();
                    ToastUtil.showFailToast(AddVideoActivity.this, getString(R.string.conn_ap_overtime));
                    finish();
                    break;
                default:
                    break;
            }

        }

    };

    private void scanApComplete(ArrayList<MyScanResult> list) {
        Gson gson = new Gson();
        DswLog.i("ScanApComplete\tlist -->" + gson.toJson(list));
        if (list == null || list.size() == 0) {
            ToastUtil.showFailToast(AddVideoActivity.this, getString(R.string.NO_DEVICE));
        } else {
            setViewVisibly(CHOOSEPAGE, list);
        }
    }

    private void authenticating_finish() {
        String ssid = wm.getConnectionInfo().getSSID();
        if (ssid.replaceAll("\"", "").equals(mChooseDevice.scanResult.SSID.replaceAll("\"", ""))) {
            ClientUDP.getInstance().setCid(subStringCid(mChooseDevice.scanResult.SSID));
            ClientUDP.getInstance().sendPing();
        }
        mHandler.sendEmptyMessageDelayed(SEND_PING_OVERTIME, ClientConstants.CONNECT_AP_OVERTIME);
    }


    private void recoverWifiConfig() {
        try {
            if (wm == null)
                wm = NetUtils.getWifiManager(getApplicationContext());
            WifiInfo info = wm.getConnectionInfo();
            if (MyApp.getConnectNet() != null && info != null && (info.getNetworkId() != MyApp.getConnectNet().getNetworkId() || info.getSSID().replaceAll("\"", "").startsWith("DOG-") || info.getSSID().replaceAll("\"", "").startsWith("DOG-ML-"))) {
                EnableWifiWorker worker = new EnableWifiWorker(wm, MyApp.getConnectNet().getNetworkId());
                WifiUtils.recoveryWifi(worker, mHandler);
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    @Override
    public void finish() {
        super.finish();
        recoverWifiConfig();
    }

    @Override
    public void disconnectServer() {
        if (mHandler.hasMessages(MSG_CONNCT_OVERTIME)) {
            mHandler.removeMessages(MSG_CONNCT_OVERTIME);
            mProgressDialog.dismissDialog();
            if (mBindResultFragment != null) {
                mBindResultFragment.setCompleteBtnVisiblty(View.VISIBLE);
                mBindResultFragment.setConnStateViewResours(R.drawable.addvideo_failed);
                mBindResultFragment.setCompleteBtnText(R.string.WELL_OK);
                mBindResultFragment.stopAnimation();
                mBindResultFragment.setConnViewVisiblty(View.GONE);
                mBindResultFragment.setStateTextViewText(R.string.NO_NERWORK_CHEAKDEVICE);
            }
        }
    }

    @Override
    public void handleMsgpackMsg(int msg, MsgpackMsg.MsgHeader msgpackMsg) {
        if (MsgpackMsg.CLIENT_BINDCID_RSP == msgpackMsg.msgId) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            mHandler.removeMessages(BIND_VIDEO_OVERTIME);
            if (mRspMsgHeader.ret == Constants.RETOK) {
                if (mBindResultFragment != null) {
                    mBindResultFragment.setStateTextViewText(R.string.DEVICE_CONNECTING);
                }
                mHandler.removeMessages(MSG_CONNCT_OVERTIME);
                mHandler.sendEmptyMessageDelayed(MSG_CONNCT_OVERTIME, 30000);
            } else {
                setWifiFailed(getResources().getString(R.string.ADD_FAILED) + "\n" + mRspMsgHeader.msg);

                if (mRspMsgHeader.ret == ClientConstants.EBINDCID_IN_BINDING) {
                    dealReBind();
                }

            }
        } else if (MsgpackMsg.CLIENT_SYNC_CIDONLINE == msgpackMsg.msgId) {
            MsgSyncCidOnline cidOnline = (MsgSyncCidOnline) msgpackMsg;
            String cid = cidOnline.cid;
            newCid.add(cid);
            int net = cidOnline.net;
            if (mHandler.hasMessages(MSG_CONNCT_OVERTIME) && Utils.isValid(bindcid)) {
                if (cid.equals(bindcid)) {
                    if ((net == MsgCidData.CID_NET_WIFI
                            || net == MsgCidData.CID_NET_3G)) {
                        mHandler.removeMessages(MSG_CONNCT_OVERTIME);
                        if (mBindResultFragment != null) {
                            mBindResultFragment.setCompleteBtnVisiblty(View.VISIBLE);
                            mBindResultFragment.stopAnimation();
                            mBindResultFragment.setConnViewResourse(R.drawable.part6);
                            mBindResultFragment.setConnStateViewResours(R.drawable.addvideo_success);
                            mBindResultFragment.setStateTextViewText(R.string.ADD_SUCC_1);
                            mBindResultFragment.setCompleteBtnText(R.string.SAVE);
                        }
                    }
                }
            }
        } else if (msgpackMsg.msgId == MsgpackMsg.CLIENT_CIDLIST_RSP) {
            RspMsgHeader mRspMsgHeader = (RspMsgHeader) msgpackMsg;
            if (Constants.RETOK == mRspMsgHeader.ret) {
                MsgCidlistRsp mMsgCidlistRsp = (MsgCidlistRsp) mRspMsgHeader;
                if (mHandler.hasMessages(MSG_CONNCT_OVERTIME)) {
                    if (Utils.isValid(bindcid)) {
                        int sceneListSize = mMsgCidlistRsp.data.size();
                        for (int i = 0; i < sceneListSize; i++) {
                            int cidListSize = mMsgCidlistRsp.data.get(i).data.size();
                            for (int j = 0; j < cidListSize; j++) {
                                MsgCidData cidData = mMsgCidlistRsp.data.get(i).data.get(j);
                                if (cidData.cid.equals(bindcid) && (cidData.net == MsgCidData.CID_NET_WIFI
                                        || cidData.net == MsgCidData.CID_NET_3G)) {
                                    mHandler.removeMessages(MSG_CONNCT_OVERTIME);
                                    if (mBindResultFragment != null) {
                                        mBindResultFragment.setCompleteBtnVisiblty(View.VISIBLE);
                                        mBindResultFragment.stopAnimation();
                                        mBindResultFragment.setConnViewResourse(R.drawable.part6);
                                        mBindResultFragment.setConnStateViewResours(R.drawable.addvideo_success);
                                        mBindResultFragment.setStateTextViewText(R.string.ADD_SUCC_1);
                                        mBindResultFragment.setCompleteBtnText(R.string.SAVE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    private void setWifiFailed(String s) {
        if (mBindResultFragment != null) {
            mBindResultFragment.setCompleteBtnVisiblty(View.VISIBLE);
            mBindResultFragment.stopAnimation();
            mBindResultFragment.setConnStateViewResours(R.drawable.addvideo_failed);
            mBindResultFragment.setStateTextViewText(s);
            mBindResultFragment.setCompleteBtnText(R.string.BACK);
            mBindResultFragment.setConnViewVisiblty(View.GONE);
        }
    }

    private void dealReBind() {
        nd = new NotifyDialog(this);// has_bind_other
        nd.setCanceledOnTouchOutside(false);
        nd.setCancelable(false);
        nd.setButtonText(R.string.CARRY_ON, R.string.CANCEL);
        nd.show(R.string.CID_USED, new OnClickListener() {
            @Override
            public void onClick(View v) {
                nd.dismiss();
                if (mBindResultFragment != null) {
                    mBindResultFragment.setConnViewVisiblty(View.VISIBLE);
                    //mBindResultFragment.stopAnimation();
                    mBindResultFragment.setCompleteBtnVisiblty(View.GONE);
                    mBindResultFragment.setStateTextViewText(R.string.PLEASE_WAIT_2);
                    mBindResultFragment.setConnStateViewResours(R.drawable.addvideo_connecting);
                }
                bindRequest(ClientConstants.COVER_BIND);
                mHandler.sendEmptyMessageDelayed(MSG_CONNCT_OVERTIME, 30000);
            }
        }, new OnClickListener() {

            @Override
            public void onClick(View v) {
                nd.dismiss();
            }
        });

    }


    private void decideNet(int net) {
        Boolean is3G = false;
        if (net == 2) {
            is3G = true;
        }
        toBind(is3G);
    }

    private void toBind(boolean is3G) {
        isAuthenticating = false;
        short type = 0;
        mProgressDialog.dismissDialog();
        if (is3G) {
            ClientUDP.getInstance().toSendWifi(type, "", "", PreferenceUtil.getBindingPhone(AddVideoActivity.this));
            mHandler.sendEmptyMessageDelayed(BIND_VIDEO_OVERTIME, 90000);
            setViewVisibly(ADDPAGE);
        } else {
            setViewVisibly(SETPAGE, bindcid);
        }
    }


    private void bindRequest(int con) {
        MsgBindCidReq mMsgBindCidReq = new MsgBindCidReq(bindcid);
        mMsgBindCidReq.cid = bindcid;
        mMsgBindCidReq.is_rebind = con;
        mMsgBindCidReq.timezone = TimeZone.getDefault().getID();
        mMsgBindCidReq.alias = (mAlias == null ? bindcid : mAlias);
        mMsgBindCidReq.mac = "";
        if (!MyApp.getIsLogin()) {
            CacheUtil.saveObject(mMsgBindCidReq, CacheUtil.getADD_DEVICE_CACHE());
            return;
        }
        MyApp.wsRequest(mMsgBindCidReq.toBytes());
        DswLog.i("send MsgBindCidReq msg-->" + mMsgBindCidReq.toString());

    }

    protected void setViewVisibly(int index, Object... obj) {
        if (titles == null)
            titles = getResources().getStringArray(R.array.addvideo_title);
        setTitle(titles[index]);
        ft = fm.beginTransaction();
        switch (index) {
            case SEARCHPAGE:
                if (mSearchDeviceFragment == null) {
                    mSearchDeviceFragment = SearchDeviceFragment.newInstance();
                    mSearchDeviceFragment.setOnSearchDeviceListener(this);
                }
                ft.replace(R.id.container_layout, mSearchDeviceFragment, "SEARCHPAGE");
                break;
            case CHOOSEPAGE:
                if (mChooseDeviceFragment == null) {
                    mChooseDeviceFragment = ChooseDeviceFragment.newInstance((ArrayList<MyScanResult>) obj[0]);
                    mChooseDeviceFragment.setOnSelectDeviceListener(this);
                }
                ft.replace(R.id.container_layout, mChooseDeviceFragment, "CHOOSEPAGE");
                break;
            case SETPAGE:
                if (mSubmitWifiInfoFragment == null) {
                    mSubmitWifiInfoFragment = SubmitWifiInfoFragment.newInstance((String) obj[0]);
                    mSubmitWifiInfoFragment.setOnSubmitWifiInfoListener(this);
                }
                ft.replace(R.id.container_layout, mSubmitWifiInfoFragment, "SETPAGE");
                break;
            case ADDPAGE:
                if (mBindResultFragment == null) {
                    mBindResultFragment = BindResultFragment.newInstance();
                    mBindResultFragment.setOnCompleteButtonClickListener(this);
                }
                ft.replace(R.id.container_layout, mBindResultFragment, "ADDPAGE");
                break;
            case UPGRADE:
                if (mUpgradeFragement == null) {
                    mUpgradeFragement = UpgradeFragement.newInstance();
                    mUpgradeFragement.setOnUpgradeButtonClickListener(this);
                }
                ft.replace(R.id.container_layout, mUpgradeFragement, "UPGRADE");
                break;
        }
        ft.commitAllowingStateLoss();
    }


    @Override
    protected void onDestroy() {
        Log.d("big", "AddVideoActivity destroy");
        try {
            ClientUDP.getInstance().removeUDPMsgListener(this);
            unregisterReceiver(broadcastReceiver);
            mHandler.removeCallbacksAndMessages(null);

            if (timer != null)
                timer.cancel();

        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mSearchDeviceFragment != null && mSearchDeviceFragment.isVisible()) {
            recoverWifiConfig();
            finish();
        } else {
            showUnCompleteDialog();
        }
    }

    protected String subStringCid(String str) {
        return str.substring(4);
    }

    protected boolean isMyDeviceByWifi(String ssid) {
        return (ssid.replaceAll("\"", "").startsWith("DOG-") && ssid.replaceAll("\"", "").length() == 10);
    }


    protected void setTryAgain() {
        startActivity(new Intent(AddVideoActivity.this, AddVideoActivity.class));
        recoverWifiConfig();
        finish();
    }

    private void saveMacAddress(String cid, String mac) {
        try {
            String macMap = PreferenceUtil.getDeviceMacAddress(this);
            JSONObject object = new JSONObject();
            object.put(cid, mac);
            JSONArray ja;
            if (!macMap.equals("")) {
                ja = new JSONArray(macMap);
            } else {
                ja = new JSONArray();
            }
            ja.put(object);
            PreferenceUtil.setDeviceMacAddress(this, ja.toString());
        } catch (JSONException e) {
            DswLog.ex(e.toString());
        }

    }


    private void showUpgradeDialog(final String filename) {

        if (mUpgradeDialog == null) {
            mUpgradeDialog = new NotifyDialog(this);
        } else {
            if (mUpgradeDialog.isShowing())
                return;
        }
        mUpgradeDialog.hideNegButton();
        mUpgradeDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mUpgradeDialog.show(R.string.DEVICE_UPGRADE_TIPS, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpgradeDialog.dismiss();
                String ipAdd = Utils.getlocalip(AddVideoActivity.this);
                if (ipAdd == null) {
                    ToastUtil.showFailToast(AddVideoActivity.this, "ip address is NULL");
                    return;
                }

                setViewVisibly(UPGRADE);

                timer = new Timer(true);
                timer.schedule(new Ta(), 153, 153); // timeTask
                try {
                    Utils.copyAssetFile(AddVideoActivity.this, filename);
                    String url = "http://" + ipAdd + ":8998/" + filename;
                    DswLog.i("upgrade url-->" + url);
                    ClientUDP.getInstance().sendUpgrade(url);

                } catch (Exception e) {
                    DswLog.ex(e.toString());
                }
                isStartSendUrl = true;
                mHandler.sendEmptyMessageDelayed(MSG_UCOS_UPGRADE_OVERTIME, 30000);
            }
        }, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mUpgradeDialog.dismiss();
            }
        });
    }

    @Override
    public void submitWifiInfo(short type, String ssid, String pwd, String alias, List<String> list) {
        mAlias = alias;
        ClientUDP.getInstance().toSendWifi(type, ssid, pwd, PreferenceUtil.getBindingPhone(AddVideoActivity.this));
        mHandler.sendEmptyMessageDelayed(BIND_VIDEO_OVERTIME, 90000);
        setViewVisibly(ADDPAGE);
        MsgClientPost post = RequestMessage.getMsgClientPost(bindcid, MsgClientPost.BIND, list);
        MyApp.wsRequest(post.toBytes());
    }

    @Override
    public void OnCompleteButtonClick() {
        if (isSetWifiSuc) {
            recoverWifiConfig();
            finish();
            return;
        }
        setTryAgain();
    }

    @Override
    public void OnUpgradeButtonClick() {
        if (MsgCidlistRsp.getInstance().isHasBindDeviceByCid(bindcid)) {
            recoverWifiConfig();
            finish();
        } else {
            setTryAgain();
        }
    }


    class Ta extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() { // UI thread
                @Override
                public void run() {
                    recLen++;
                    if (mUpgradeFragement != null) {
                        mUpgradeFragement.setUpgradeTextViewText(getString(R.string.UPDATING_LABEL) + "(" + recLen + "%)");
                    }
                    if (recLen >= 98 && timer != null) {
                        timer.cancel();
                        timer = null;
                        recLen = 0;
                    }
                }
            });

        }

    }


    private void showUpgradeOverTimeDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.hideNegButton();
        mDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mDialog.show(R.string.ADD_FAILED_1, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                String ipAdd = Utils.getlocalip(AddVideoActivity.this);
                if (ipAdd == null) {
                    ToastUtil.showFailToast(AddVideoActivity.this, "ip address is NULL");
                    return;
                }
                if (bindcid == null)
                    return;

                timer = new Timer(true);
                timer.schedule(new Ta(), 153, 153); // timeTask
                String filename = getFilename(bindcid);
                try {
                    Utils.copyAssetFile(AddVideoActivity.this, filename);
                    String url = "http://" + ipAdd + ":8998/" + filename;
                    DswLog.i("upgrade url-->" + url);
                    ClientUDP.getInstance().sendUpgrade(url);

                } catch (Exception e) {
                    DswLog.ex(e.toString());
                }
                isStartSendUrl = true;
                mHandler.sendEmptyMessageDelayed(MSG_UCOS_UPGRADE_OVERTIME, 30000);

            }
        }, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }

    private void showDiscountFromApDialog() {
        final NotifyDialog mDialog = new NotifyDialog(this);
        mDialog.hideNegButton();
        mDialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        mDialog.show(R.string.UPDATE_FAIL, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                setTryAgain();

            }
        }, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
    }


    @Override
    public void JfgMsgPong(ClientUDP.JFG_PONG jfg) {
        if (jfg.mCid.contains(subStringCid(mChooseDevice.scanResult.SSID))) {
            net = jfg.mNet;
            bindcid = jfg.mCid;
            ClientUDP.getInstance().setCid(bindcid);
            ClientUDP.getInstance().sendServer(PreferenceUtil.getIP(this), String.valueOf(PreferenceUtil.getPort(this)));
            ClientUDP.getInstance().sendLanguage(Utils.getLanguageType(this));
            ClientUDP.getInstance().sendFPing(bindcid);
        }
    }

    @Override
    public void JfgMsgSetWifiRsp(ClientUDP.JFG_RESPONSE rsp) {
        DswLog.i("JfgMsgSetWifiRsp msgid = " + rsp.mMsgid + " cid = " + rsp.mCid);
        if (rsp.mCid.equals(bindcid)) {
            isSetWifiSuc = true;
            recoverWifiConfig();
            bindRequest(ClientConstants.CUSTOM_BIND);
        }
    }

    @Override
    public void JfgMsgFPong(ClientUDP.JFG_F_PONG req) {
        if (req.mCid.equals(bindcid)) {
            DswLog.i("JFG_F_PONG msgid=" + req.mMsgid + " cid=" + req.mCid + " mac="
                    + req.mac + " version=" + req.version);
            mHandler.removeMessages(CONN_AP_OVERTIME);
            mHandler.removeMessages(SEND_PING_OVERTIME);
            mProgressDialog.dismissDialog();
            saveMacAddress(req.mCid, req.mac);
            String version = req.version;
            if (CIDCheck.isUcos(req.mCid)) {
                if (Utils.isNoSurrortVersion(ClientConstants.UPGRADE_VERSION, version)) {
                    mHandler.obtainMessage(MSG_UCOS_UPGRADE, new Message().obj = getFilename(req.mCid)).sendToTarget();
                    return;
                }
            }
            decideNet(net);
        }
    }

    @Override
    public void JfgMsgFAck(ClientUDP.JFG_F_ACK ack) {
        if (ack.mCid.equals(bindcid)) {
            isStartSendUrl = false;
            mHandler.removeMessages(MSG_UCOS_UPGRADE_OVERTIME);
            if (ack.mResult == Constants.RETOK) {
                //TODO deal upgrade success
                mHandler.sendEmptyMessage(MSG_UCOS_UPGRADE_COMPLETE);
            } else {
                mHandler.sendEmptyMessage(MSG_UCOS_UPGRADE_OVERTIME);
            }

        }
    }

    @Override
    public void JfgMsgBellPress(ClientUDP.JFGCFG_HEADER data) {

    }

    private String getFilename(String cid) {
        return ClientConstants.UPGRADE_FILE_V1;

    }

    @Override
    public void OnSearchDevice() {
        if (wm.isWifiEnabled()) {
            mProgressDialog.showDialog(getString(R.string.addvideo_searching));
            getWifiList();
        } else {
            ToastUtil.showFailToast(this, getString(R.string.WIFI_OFF));
        }
    }

    @Override
    public void onSelectDevice(MyScanResult scan) {
        try {
            mChooseDevice = scan;
            mProgressDialog.showDialog(getString(R.string.connecting_ap));
            final String ssid = NetUtils.removeDoubleQuotes(wm.getConnectionInfo().getSSID());
            if (!TextUtils.equals(ssid,
                    mChooseDevice.scanResult.SSID)) {
                ConfigWIfiWorker worker = new ConfigWIfiWorker(wm, mChooseDevice.scanResult, mHandler, MSG_CONFIG_WIFI);
                WifiUtils.configWifi(worker, mHandler);
                mHandler.sendEmptyMessageDelayed(CONN_AP_OVERTIME, ClientConstants.CONNECT_AP_OVERTIME);
            } else {
                authenticating_finish();
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    private void showOpenPositionNotify() {
        final NotifyDialog dialog = new NotifyDialog(this);
        dialog.setButtonText(R.string.OK, R.string.CANCEL);
        dialog.hideNegButton();
        dialog.show(R.string.turn_on_gps, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {

                    case R.id.confirm:
                        dialog.dismiss();
                        break;
                }

            }
        }, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CONNECT_HAND) {
            if (wm.getConnectionInfo() != null && mChooseDevice != null && wm.getConnectionInfo().getSSID().replaceAll("\"", "").equals(mChooseDevice.scanResult.SSID)) {
                mProgressDialog.showDialog(getString(R.string.connecting_ap));
                authenticating_finish();
            } else {
                showConnectByHand();
            }
        }
    }

    private void showConnectByHand() {
        DswLog.d("Connect fail, try to showConnectByHand!");
        final NotifyDialog dialog = new NotifyDialog(this);
        dialog.hideNegButton();
        dialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        dialog.show(String.format(getString(R.string.connect_by_hand), mChooseDevice.scanResult.SSID), new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.confirm:
                        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), RESULT_CONNECT_HAND);
                        dialog.dismiss();
                        break;
                }
            }
        }, null);

    }
}