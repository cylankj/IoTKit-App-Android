package com.cylan.jiafeigou.activity.video.setting;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.support.DswLog;
import com.cylan.publicApi.Function;
import com.cylan.jiafeigou.adapter.BaseAdapter;
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
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.utils.WifiUtils;
import com.cylan.jiafeigou.worker.ConfigWIfiWorker;
import com.cylan.jiafeigou.worker.EnableWifiWorker;

import java.util.ArrayList;
import java.util.List;

public class SetCameraWifi extends BaseActivity implements View.OnClickListener, UDPMessageListener {

    private ListView mListView;
    private WifiAdapter mWifiAdapter;
    private MsgCidData mData;
    private boolean isAuth = false;
    private boolean isLocalWifiConnecting = false;
    private WifiInfo mInitConnectWifiInfo;
    private Boolean isSearchWifi = false;
    private String ssid, pwd;
    private short type;
    /**
     * handler message what
     */
    private static final int MSG_NO_POSTDELY = 0x01;
    private static final int CONN_AP_OVERTIME = 0x02;
    private static final int HANDLER_RECEIVER_OUTTIME = 0x03;
    private static final int HANDLER_SETWIFI_DELAYEXIT = 0x04;
    private static final int MSG_CONFIG_WIFI = 0x05;
    private static final int RESULT_CONNECT_HAND = 0x06;
    private static final int MSG_RECONECT_UDP = 0x07;
    private static final int MSG_SEND_WIFI_OVERTIME= 0x08;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                    Log.d("big", "SCAN_RESULTS_AVAILABLE_ACTION");
                    onScanResult();
                } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {

                    Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (parcelableExtra != null) {
                        NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                        DswLog.d("NETWORK_STATE_CHANGED_ACTION NETWORK-->" + networkInfo.getState() + "--isAuth-->" + isAuth + "networkInfo-->" + networkInfo.getExtraInfo() + "---isLocalWifiConnecting-->" + isLocalWifiConnecting + "---wm.getConnectionInfo().getSSID()--->" + wm.getConnectionInfo().getSSID());
                        switch (networkInfo.getState()) {
                            case CONNECTING:
                                if (isAuth)
                                    isLocalWifiConnecting = true;
                                break;
                            case CONNECTED:
                                if (isAuth && isLocalWifiConnecting) {
                                    mProgressDialog.dismissDialog();
                                    isAuth = false;
                                    isLocalWifiConnecting = false;
                                    WifiInfo wi = wm.getConnectionInfo();
                                    if (isMyDeviceByWifi(wi.getSSID())) {
                                        mHandler.removeMessages(CONN_AP_OVERTIME);
                                    }

                                }

                                break;
                            case DISCONNECTED:
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


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NO_POSTDELY:
                    isSearchWifi = false;
                    this.sendEmptyMessageDelayed(HANDLER_RECEIVER_OUTTIME, 5000);
                    break;
                case CONN_AP_OVERTIME:
                    mProgressDialog.dismissDialog();
                    showConnectByHand();
//                    recoverWifiConfig();
//                    ToastUtil.showToast(SetCameraWifi.this, getString(R.string.conn_ap_overtime), Gravity.CENTER, 5000);
//                    setResult(RESULT_OK);
//                    finish();
                    break;
                case HANDLER_RECEIVER_OUTTIME:
                    onScanResult();
                    break;
                case HANDLER_SETWIFI_DELAYEXIT:
                    mProgressDialog.dismissDialog();
                    ToastUtil.showToast(SetCameraWifi.this, getString(R.string.DOOR_SET_WIFI_MSG));
                    setResult(RESULT_OK);
                    finish();
                    break;
                case MSG_CONFIG_WIFI:
                    isAuth = (boolean) msg.obj;
                    if (!isAuth) {
                        mHandler.removeMessages(CONN_AP_OVERTIME);
                        sendEmptyMessage(CONN_AP_OVERTIME);
                    }
                    break;
                case MSG_RECONECT_UDP:
                    ClientUDP.getInstance().setCid(mData.cid);
                    ClientUDP.getInstance().setUDPMsgListener(SetCameraWifi.this);
                    wifiAuthentication(ssid, pwd, type);
                    break;
                case MSG_SEND_WIFI_OVERTIME:
                    mHandler.removeCallbacksAndMessages(null);
                    mProgressDialog.dismissDialog();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };


    private WifiManager wm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_set_camera_wifi);
        wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        mData = (MsgCidData) getIntent().getSerializableExtra(ClientConstants.CIDINFO);
        setTitle(R.string.DEVICES_TITLE_3);

        saveWifiConfig();

        mListView = (ListView) findViewById(R.id.listView);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setCameraWifiByOpen((MyScanResult) parent.getItemAtPosition(position));
            }
        });
        registerScanWifiBroadcats();
        ClientUDP.getInstance().setCid(mData.cid);
        ClientUDP.getInstance().setUDPMsgListener(SetCameraWifi.this);
        wm.startScan();
    }

    private void onScanResult() {
        if (!isSearchWifi) {
            mHandler.removeMessages(HANDLER_RECEIVER_OUTTIME);
            isSearchWifi = true;
            mHandler.sendEmptyMessageDelayed(MSG_NO_POSTDELY, 5000);
            onWifiList();
        }
    }


    private void registerScanWifiBroadcats() {
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void JfgMsgPong(ClientUDP.JFG_PONG jfg) {
    }

    @Override
    public void JfgMsgSetWifiRsp(ClientUDP.JFG_RESPONSE rsp) {
        mHandler.removeMessages(MSG_RECONECT_UDP);
        mHandler.sendEmptyMessageDelayed(HANDLER_SETWIFI_DELAYEXIT, 5000);
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


    class ViewHolder {
        View loading;
        ImageView wifi_state;
        TextView wifi_name;
        ImageView isselected;
        ImageView isLocked;
    }

    class WifiAdapter extends BaseAdapter<MyScanResult> {

        public WifiAdapter(Activity activity, List<MyScanResult> list) {
            super(activity, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                convertView = View.inflate(SetCameraWifi.this, R.layout.wifi_list_item, null);
                vh = new ViewHolder();
                convertView.setTag(vh);
                vh.loading = convertView.findViewById(R.id.loading);
                vh.wifi_state = (ImageView) convertView.findViewById(R.id.wifi_state);
                vh.wifi_name = (TextView) convertView.findViewById(R.id.wifi_name);
                vh.isselected = (ImageView) convertView.findViewById(R.id.is_selected);
                vh.isLocked = (ImageView) convertView.findViewById(R.id.is_locked);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            final MyScanResult info = getItem(position);
            if (info == null) {
                return convertView;
            }

            switch (info.connectState) {
                case CONNECTED:
                    vh.isselected.setVisibility(View.VISIBLE);
                    break;
                case DISCONNECTED:
                    vh.isselected.setVisibility(View.INVISIBLE);
                    break;
                case CONNECTING:
                    vh.isselected.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
            vh.wifi_name.setText(info.scanResult.SSID);

            switch (WifiManager.calculateSignalLevel(info.scanResult.level, 5)) {
                case 0:
                    vh.wifi_state.setImageResource(R.drawable.ico_wifi1);
                    break;
                case 1:
                case 2:
                case 3:
                    vh.wifi_state.setImageResource(R.drawable.ico_wifi2);
                    break;
                case 4:
                    vh.wifi_state.setImageResource(R.drawable.ico_wifi3);
                    break;
                default:
                    break;
            }
            vh.isLocked.setVisibility(info.scanResult.capabilities.contains("WPA") ? View.VISIBLE : View.INVISIBLE);
            return convertView;
        }


    }

    @Override
    public void finish() {
        try {
            recoverWifiConfig();
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
        super.finish();
    }

    private void setCameraWifiByOpen(MyScanResult info) {
        short type = Function.JFG_WIFI_OPEN;
        if (info.scanResult.capabilities.contains("WEP")) {
            type = Function.JFG_WIFI_WEP;
        } else if (info.scanResult.capabilities.contains("WPA2")) {
            type = Function.JFG_WIFI_WPA2;
        } else if (info.scanResult.capabilities.contains("WPA")) {
            type = Function.JFG_WIFI_WPA;
        }
        ssid = info.scanResult.SSID.replaceAll("\"", "");
        pwd = "";
        this.type = type;
        if (type == Function.JFG_WIFI_OPEN) {
            mProgressDialog.dismissDialog();
            wifiAuthentication(ssid, pwd, this.type);
            mProgressDialog.setIsCancelable(false);
            mProgressDialog.showDialog(getString(R.string.submiting));
        } else {
            setcameraWifi(info);
        }
    }

    private void setcameraWifi(final MyScanResult info) {
        final Dialog dialog = new Dialog(this, R.style.dialog);
        View view = View.inflate(this, R.layout.dialog_rename, null);
        TextView mTitle = (TextView) view.findViewById(R.id.title);
        mTitle.setText(info.scanResult.SSID.replaceAll("\"", ""));
        final EditText edit = (EditText) view.findViewById(R.id.input);
        View confirm = view.findViewById(R.id.confirm);
        View cancel = view.findViewById(R.id.cancel);
        View.OnClickListener clickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.confirm:

                        dialog.dismiss();
                        short type = Function.JFG_WIFI_OPEN;
                        if (info.scanResult.capabilities.contains("WEP")) {
                            type = Function.JFG_WIFI_WEP;
                        } else if (info.scanResult.capabilities.contains("WPA2")) {
                            type = Function.JFG_WIFI_WPA2;
                        } else if (info.scanResult.capabilities.contains("WPA")) {
                            type = Function.JFG_WIFI_WPA;
                        }

                        Log.e("big", info.scanResult.capabilities + "-what-" + type);
                        mProgressDialog.dismissDialog();
                        ssid = info.scanResult.SSID.replaceAll("\"", "");
                        pwd = edit.getText().toString().trim();
                        SetCameraWifi.this.type = type;
                        wifiAuthentication(ssid, pwd, SetCameraWifi.this.type);
                        mProgressDialog.setIsCancelable(false);
                        mProgressDialog.showDialog(getString(R.string.submiting));
//                        mHandler.sendEmptyMessageDelayed(HANDLER_SETWIFI_DELAYEXIT, 5000);
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        if (imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED)) {
                            edit.clearFocus();
                            imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
                        }
                        List<String> list = new ArrayList<>();
                        list.add(ssid);
                        list.add(pwd);
                        list.add(String.valueOf(type));
                        list.add(String.valueOf(info.scanResult.frequency));
                        list.add("");
                        MsgClientPost post = RequestMessage.getMsgClientPost(mData.cid,MsgClientPost.SETWIFI, list);
                        MyApp.wsRequest(post.toBytes());
                        break;
                    case R.id.cancel:
                        dialog.dismiss();

                        break;
                }
            }
        };
        confirm.setOnClickListener(clickListener);
        cancel.setOnClickListener(clickListener);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                edit.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);

            }
        });

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {
                edit.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);

            }
        });
        if (!isFinishing())
            dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ico_back:
                finish();
                break;
        }
    }


    boolean isConnectAp;

    public void onWifiList() {
        String connectSsid = wm.getConnectionInfo() == null || wm.getConnectionInfo().getSSID() == null ? "" : wm.getConnectionInfo().getSSID().replaceAll("\"", "");
        final List<ScanResult> results = wm.getScanResults();
        List<MyScanResult> mResult = new ArrayList<>();
        if (results == null || results.isEmpty()) {
            mListView.setVisibility(View.GONE);
        } else {
            if (mWifiAdapter != null)
                mWifiAdapter.clear();
            for (ScanResult info : results) {
                final MyScanResult scan = new MyScanResult();
                scan.scanResult = info;
                if (mData.net != MsgCidData.CID_NET_WIFI && scan.scanResult.SSID.equals(getAPName(mData.cid))) {
                    if (info.SSID.equals(connectSsid)) {
                        isConnectAp = true;
                        continue;
                    }
                    if (!isConnectAp) {
                        ConfigWIfiWorker worker = new ConfigWIfiWorker(wm, scan.scanResult, mHandler, MSG_CONFIG_WIFI);
                        WifiUtils.configWifi(worker, mHandler);
                        mHandler.sendEmptyMessageDelayed(CONN_AP_OVERTIME, 30000);
                        isConnectAp = true;
                        mProgressDialog.setIsCancelable(false);
                        mProgressDialog.showDialog(getString(R.string.is_connecting_video));
                    }
                    return;
                } else if (mData.net == MsgCidData.CID_NET_WIFI && scan.scanResult.SSID.equals(mData.name)) {
                    scan.connectState = State.CONNECTED;
                } else {
                    wm.startScan();
                }

                if (!(scan.scanResult.SSID.isEmpty() || scan.scanResult.SSID.compareTo("0x") == 0 || scan.scanResult.SSID.compareTo("<unknown ssid>") == 0 || scan.scanResult.SSID
                        .equals("NVRAM WARNING: Err = 0x10"))) {
                    if (!mResult.contains(scan) && !Utils.is5G(scan.scanResult.frequency))
                        mResult.add(scan);
                }
            }
            mWifiAdapter = new WifiAdapter(SetCameraWifi.this, mResult);
            mListView.setAdapter(mWifiAdapter);
            mListView.setVisibility(View.VISIBLE);
        }
    }


    private void wifiAuthentication(final String ssid, final String pwd, short type) {
        isAuth = true;
        mProgressDialog.setIsCancelable(false);
        mProgressDialog.showDialog(getString(R.string.wifi_authentication, ssid));
        mHandler.sendEmptyMessageDelayed(MSG_RECONECT_UDP, 5000);
        mHandler.sendEmptyMessageDelayed(MSG_SEND_WIFI_OVERTIME, 30000);

        for (int i = 0; i < 3; i++) {
            ClientUDP.getInstance().toSendWifi(type, ssid, pwd, PreferenceUtil.getBindingPhone(SetCameraWifi.this));
        }

    }


    private void saveWifiConfig() {
        mInitConnectWifiInfo = wm.getConnectionInfo();
    }

    private void recoverWifiConfig() {
        WifiInfo info = wm.getConnectionInfo();
        if (info.getNetworkId() != mInitConnectWifiInfo.getNetworkId()) {
            EnableWifiWorker worker = new EnableWifiWorker(wm, MyApp.getConnectNet().getNetworkId());
            WifiUtils.recoveryWifi(worker, mHandler);
        }
    }


    @Override
    protected void onDestroy() {
        try {
            mHandler.removeCallbacksAndMessages(null);
            unregisterReceiver(broadcastReceiver);
            ClientUDP.getInstance().removeUDPMsgListener(this);
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
        super.onDestroy();
    }


    private String getAPName(String cid) {
        if (cid == null)
            return null;
        return "DOG-" + cid.substring(6);
    }


    private boolean isMyDeviceByWifi(String ssid) {
        return (ssid.replaceAll("\"", "").startsWith("DOG-") && ssid.replaceAll("\"", "").length() == 10);
    }


    protected String subStringCid(String str) {
        return str.substring(4);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CONNECT_HAND) {
            if (!isMyDeviceByWifi(wm.getConnectionInfo().getSSID())) {
                showConnectByHand();
            }
        }
    }

    private void showConnectByHand() {
        final NotifyDialog dialog = new NotifyDialog(this);
        dialog.hideNegButton();
        dialog.setButtonText(R.string.WELL_OK, R.string.CANCEL);
        dialog.show(String.format(getString(R.string.connect_by_hand), mData.cid.substring(6)), new View.OnClickListener() {
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