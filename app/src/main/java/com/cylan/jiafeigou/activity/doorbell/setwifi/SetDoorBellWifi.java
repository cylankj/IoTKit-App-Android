package com.cylan.jiafeigou.activity.doorbell.setwifi;

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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.support.DswLog;
import com.cylan.jiafeigou.activity.doorbell.AddDoorBellHelpActivity;
import com.cylan.jiafeigou.activity.doorbell.addDoorbell.SearchDoorBellFragment;
import com.cylan.jiafeigou.activity.video.addDevice.SearchDeviceFragment;
import com.cylan.jiafeigou.activity.video.addDevice.SubmitWifiInfoFragment;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.engine.ClientUDP;
import com.cylan.jiafeigou.engine.RequestMessage;
import com.cylan.jiafeigou.entity.MyScanResult;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgClientPost;
import com.cylan.jiafeigou.listener.UDPMessageListener;
import com.cylan.jiafeigou.utils.NotifyDialog;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.WifiUtils;
import com.cylan.jiafeigou.worker.ConfigWIfiWorker;
import com.cylan.jiafeigou.worker.EnableWifiWorker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangc on 2015/7/15.
 */
public class SetDoorBellWifi extends BaseActivity implements View.OnClickListener, SearchDeviceFragment.OnSearchDeviceListener,
        SubmitWifiInfoFragment.OnSubmitWifiInfoListener, UDPMessageListener {

    public ImageView helpImage;
    public WifiManager wm;
    public MsgCidData mDate;
    private MyScanResult mConnWifi;

    public List<ScanResult> results;

    private boolean isSearchWifiList = true;
    private List<MyScanResult> myScanResults;
    private boolean isAuthenticating = false;
    private boolean isAuthenticating_state = false;
    private boolean isHasSend = false;
    private int count = 0;

    /**
     * handler message what
     */
    protected static final int MSG_SCAN_FINISH = 0x00;
    protected static final int MSG_SCAN_OVER_TIME = 0x01;
    private static final int HANDLER_SETWIFI_DELAYEXIT = 0x02;
    private static final int CONN_AP_OVERTIME = 0x03;
    private static final int MSG_SCAN_TIMEOUT = 0x04;
    private static final int MSG_CONFIG_WIFI = 0x05;
    private static final int RESULT_CONNECT_HAND = 0x06;
    private static final int MSG_RECONECT_UDP = 0x07;

    // page tag
    private static final int SEARCHPAGE = 0x00;
    private static final int SETPAGE = 0x01;

    protected FragmentManager fm;
    protected FragmentTransaction ft;

    private SearchDoorBellFragment mSearchDoorBellFragment;
    private DoorBellSetWifiInfoFragment mDoorBellSetWifiInfoFragment;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_SCAN_FINISH:
                    try {
                        removeMessages(MSG_SCAN_OVER_TIME);
                        removeMessages(MSG_SCAN_TIMEOUT);
                        myScanResults = ((List<MyScanResult>) msg.obj);
                    } catch (Exception e) {
                        DswLog.ex(e.toString());
                    }
                case MSG_SCAN_OVER_TIME:
                    scanApComplete();
                    break;
                case HANDLER_SETWIFI_DELAYEXIT:
                    mProgressDialog.dismissDialog();
                    recoverWifiConfig();
                    ToastUtil.showToast(SetDoorBellWifi.this, getString(R.string.DOOR_SET_WIFI_MSG));
                    setResult(RESULT_OK);
                    finish();
                    break;

                case CONN_AP_OVERTIME:
                    mProgressDialog.dismissDialog();
                    showConnectByHand();
                    break;
                case MSG_SCAN_TIMEOUT:
                    mProgressDialog.dismissDialog();
                    showConnectByHand();
                    break;
                case MSG_CONFIG_WIFI:
                    isAuthenticating_state = (boolean) msg.obj;
                    if (!isAuthenticating_state) {
                        mHandler.removeMessages(CONN_AP_OVERTIME);
                        sendEmptyMessage(CONN_AP_OVERTIME);
                    }
                    break;
                case MSG_RECONECT_UDP:
                    ClientUDP.getInstance().setCid(mDate.cid);
                    ClientUDP.getInstance().setUDPMsgListener(SetDoorBellWifi.this);
                    authenticating_finish(true);
                    break;
                default:
                    break;
            }
        }
    };


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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
                                break;
                            case CONNECTED:
                                if (isAuthenticating) {
                                    WifiInfo wi = wm.getConnectionInfo();
                                    if (isAuthenticating_state) {
                                        if (isMyDeviceByWifi(wi.getSSID()) && !isHasSend) {
                                            setViewVisibly(SETPAGE);
                                            authenticating_finish(true);
                                            mHandler.removeMessages(CONN_AP_OVERTIME);
                                            isHasSend = true;
                                            mProgressDialog.dismissDialog();
                                        }
                                    }

                                }
                                break;
                            default:
                                break;
                        }
                    }

                }
            } catch (Exception e) {
                DswLog.ex(e.toString());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addvideo);
        mDate = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        helpImage = (ImageView) findViewById(R.id.add_help);
        helpImage.setOnClickListener(this);
        setTitle(R.string.DEVICES_TITLE_3);

        fm = getFragmentManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, filter);

        String ssid = wm.getConnectionInfo().getSSID().replaceAll("\"", "");
        if (ssid.length() >= 7 && mDate.cid.substring(6).equals(subStringCid(ssid))) {
            setViewVisibly(SETPAGE);
        } else {
            setViewVisibly(SEARCHPAGE);
        }
        ClientUDP.getInstance().setCid(mDate.cid);
        ClientUDP.getInstance().setUDPMsgListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_help:
                startActivity(new Intent(getApplicationContext(), AddDoorBellHelpActivity.class));
                overridePendingTransition(R.anim.slide_down_in, 0);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(broadcastReceiver);
            mHandler.removeCallbacksAndMessages(null);
            ClientUDP.getInstance().removeUDPMsgListener(this);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
        super.onDestroy();
    }

    private void scanApComplete() {
        if (myScanResults.size() == 0 || myScanResults == null) {
            ToastUtil.showFailToast(SetDoorBellWifi.this, getString(R.string.NO_DEVICE));
            mProgressDialog.dismissDialog();
        } else {
            boolean isHas = false;
            for (MyScanResult result : myScanResults) {
                if (mDate.cid.substring(6).equals(subStringCid(result.scanResult.SSID.replaceAll("\"", ""))) && result.scanResult != null) {
                    isHas = true;
                    mConnWifi = result;
                }
            }
            if (!isHas) {
                ToastUtil.showFailToast(SetDoorBellWifi.this, getString(R.string.NO_DEVICE));
                mProgressDialog.dismissDialog();
            } else {
                ConfigWIfiWorker worker = new ConfigWIfiWorker(wm, mConnWifi.scanResult, mHandler, MSG_CONFIG_WIFI);
                WifiUtils.configWifi(worker, mHandler);
                mHandler.sendEmptyMessageDelayed(CONN_AP_OVERTIME, 30000);
            }

        }

    }

    private void setViewVisibly(int index) {
        setTitle(R.string.DEVICES_TITLE_3);
        ft = fm.beginTransaction();
        if (index == SEARCHPAGE) {
            if (mSearchDoorBellFragment == null) {
                mSearchDoorBellFragment = SearchDoorBellFragment.newInstance();
                mSearchDoorBellFragment.setOnSearchDeviceListener(this);
            }
            ft.replace(R.id.container_layout, mSearchDoorBellFragment, "SEARCHPAGE");
        } else {
            if (mDoorBellSetWifiInfoFragment == null) {
                mDoorBellSetWifiInfoFragment = DoorBellSetWifiInfoFragment.newInstance();
                mDoorBellSetWifiInfoFragment.setOnSubmitWifiInfoListener(this);
            }
            ft.replace(R.id.container_layout, mDoorBellSetWifiInfoFragment, "SETPAGE");
        }
        ft.commitAllowingStateLoss();
    }


    protected boolean isMyDeviceByWifi(String ssid) {
        return (ssid.replaceAll("\"", "").startsWith("DOG-ML-") && ssid.replaceAll("\"", "").length() == 13);
    }

    protected void onWifiList() {
        if (results != null && results.size() > 0)
            results.clear();
        results = wm.getScanResults();
        Gson gson = new Gson();
        DswLog.i("SCAN_WIFILIST--->" + gson.toJson(results));
        List<MyScanResult> list = new ArrayList<>();
        if (results != null && !results.isEmpty()) {
            if (list.size() > 0)
                list.clear();
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

    protected String subStringCid(String str) {
        return str.substring(7);
    }

    private void authenticating_finish(boolean isSuccess) {
        isAuthenticating = false;
        String ssid = wm.getConnectionInfo().getSSID();
        if (isSuccess && ssid.replaceAll("\"", "").equals(mConnWifi.scanResult.SSID.replaceAll("\"", ""))) {
            ClientUDP.getInstance().sendPing();
            mHandler.sendEmptyMessageDelayed(MSG_RECONECT_UDP, 5000);
        }

    }

    private void recoverWifiConfig() {
        if (MyApp.getConnectNet() == null)
            return;
        WifiInfo info = wm.getConnectionInfo();
        if (info.getNetworkId() != MyApp.getConnectNet().getNetworkId() || info.getSSID().replaceAll("\"", "").startsWith("DOG-") || info.getSSID().replaceAll("\"", "").startsWith("DOG-ML-")) {
            EnableWifiWorker worker=new EnableWifiWorker(wm, MyApp.getConnectNet().getNetworkId());
            WifiUtils.recoveryWifi(worker,mHandler);
        }
    }


    @Override
    public void OnSearchDevice() {
        if (wm.isWifiEnabled()) {
            mProgressDialog.setIsCancelable(false);
            mProgressDialog.showDialog(getString(R.string.addvideo_searching));
            isSearchWifiList = false;
            wm.startScan();
            mHandler.sendEmptyMessageDelayed(MSG_SCAN_TIMEOUT, 60000);
        } else {
            ToastUtil.showFailToast(this, getString(R.string.WIFI_OFF));
        }
    }

    @Override
    public void submitWifiInfo(short type, String ssid, String pwd, String alias, List<String> list) {
        for (int i = 0; i < 3; i++) {
            ClientUDP.getInstance().toSendWifi(type, ssid, pwd, PreferenceUtil.getBindingPhone(SetDoorBellWifi.this));
        }
        mProgressDialog.setIsCancelable(false);
        mProgressDialog.showDialog(getString(R.string.submiting));
        mHandler.sendEmptyMessageDelayed(HANDLER_SETWIFI_DELAYEXIT, 5000);
        MsgClientPost post = RequestMessage.getMsgClientPost(mDate.cid,MsgClientPost.SETWIFI, list);
        MyApp.wsRequest(post.toBytes());
    }

    @Override
    public void finish() {
        super.finish();
        try {
            recoverWifiConfig();
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CONNECT_HAND) {
            if (isMyDeviceByWifi(wm.getConnectionInfo().getSSID())) {
                setViewVisibly(SETPAGE);
                authenticating_finish(true);
                mHandler.removeMessages(CONN_AP_OVERTIME);
            } else {
                showConnectByHand();
            }
        }
    }

    private void showConnectByHand() {
        final NotifyDialog dialog = new NotifyDialog(this);
        dialog.hideNegButton();
        dialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        dialog.show(String.format(getString(R.string.connect_by_hand), "ML-" + mDate.cid.substring(6)),
                new View.OnClickListener() {
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

    @Override
    public void JfgMsgPong(ClientUDP.JFG_PONG jfg) {
        mHandler.removeMessages(MSG_RECONECT_UDP);
    }

    @Override
    public void JfgMsgSetWifiRsp(ClientUDP.JFG_RESPONSE rsp) {
    }

    @Override
    public void JfgMsgFPong(ClientUDP.JFG_F_PONG req) {
    }

    @Override
    public void JfgMsgFAck(ClientUDP.JFG_F_ACK ack) {
    }

    @Override
    public void JfgMsgBellPress(ClientUDP.JFGCFG_HEADER data) {

    }
}
