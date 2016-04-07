package cylan.scanbinding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.ref.WeakReference;

import cylan.scanbinding.view.BinderView;

/**
 * Created by hunt on 16-4-7.
 */
public class WifiBinder implements Binder {
    private static final String TAG = "WifiBinder";
    private WifiConfigBr wifiConfigBr;
    private BinderView binderView;
    private Context context;
    private BinderConfigure.Builder builder;
    private WifiManager wifiManager;
    private static Result configuringResult;

    public WifiBinder(BinderConfigure.Builder builder) {
        this.builder = builder;
    }

    @Override
    public void init(BinderView binderView) {
        this.binderView = binderView;
        this.context = binderView.getContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        initBr();
    }

    private void initBr() {
        wifiConfigBr = new WifiConfigBr(this.binderView);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        context.registerReceiver(wifiConfigBr, intentFilter);
    }


    @Override
    public void start(Result scanResult) {
        if (wifiConfigBr == null)
            throw new NullPointerException("you may not be initialized");
        configuringResult = scanResult;
        final String ssid = WifiFunction.removeDoubleQuotes(configuringResult.scanResult.SSID);
        final int security = WifiSecurityUtils.getSecurity(configuringResult.scanResult);
        final String pwd = scanResult.pwd;
        WifiConfiguration wifiConfiguration = WifiFunction.createWifiInfo(ssid, pwd, security);
        WifiFunction.configWifi(wifiManager, wifiConfiguration);
    }

    @Override
    public void onBinding(int state) {
        Log.d(TAG, "state: " + state);
    }

    @Override
    public void onFinish() {
        if (context != null && wifiConfigBr != null)
            context.unregisterReceiver(wifiConfigBr);
        wifiConfigBr = null;
        binderView = null;
    }

    /**
     * @param state
     * @return : 防抖动
     */
    private static boolean isHandshakeState(SupplicantState state) {
        switch (state) {
            case AUTHENTICATING:
            case ASSOCIATING:
            case ASSOCIATED:
            case FOUR_WAY_HANDSHAKE:
            case GROUP_HANDSHAKE:
                return true;
            case COMPLETED:
            case DISCONNECTED:
            case INTERFACE_DISABLED:
            case INACTIVE:
            case SCANNING:
            case DORMANT:
            case UNINITIALIZED:
            case INVALID:
                return false;
            default:
                throw new IllegalArgumentException("Unknown supplicant state");
        }
    }

    private static class WifiConfigBr extends BroadcastReceiver {
        WeakReference<BinderView> weakReference;

        public WifiConfigBr(BinderView binderView) {
            this.weakReference = new WeakReference<>(binderView);
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            BinderView binderView = weakReference.get();
            Result result = configuringResult;
            if (result == null) {
                Log.w(TAG, "result == null ");
                return;
            }
            if (TextUtils.isEmpty(result.scanResult.SSID)) {
                Log.w(TAG, "empty ");
                return;
            }
            if (TextUtils.isEmpty(result.pwd)) {
                Log.w(TAG, "pwd null? ");
                return;
            }
            if (binderView == null) {
                Log.w(TAG, "binderView == null ");
                return;
            }
            doStuff(intent, context);
        }

        private void doStuff(final Intent intent, Context context) {
            final String action = intent.getAction();
            if (TextUtils.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION, action)) {
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                if (isHandshakeState(state)) return;
            } else if (TextUtils.equals(WifiManager.RSSI_CHANGED_ACTION, action)) {
                //
            } else if (TextUtils.equals(WifiManager.WIFI_STATE_CHANGED_ACTION, action)) {
                final int stating = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                Log.d(TAG, "stating : " + stating);
                SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                Log.d(TAG, "SupplicantState : " + state);
            } else if (TextUtils.equals(WifiManager.NETWORK_IDS_CHANGED_ACTION, action)) {

            } else if (TextUtils.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION, action)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "networkInfo : " + networkInfo);
                final String BSSID = intent.getStringExtra(WifiManager.EXTRA_BSSID);
                Log.d(TAG, "BSSID : " + BSSID);
                final WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                Log.d(TAG, "wifiInfo : " + wifiInfo);
            } else if (TextUtils.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION, action)) {

            } else if (TextUtils.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION, action)) {
                final boolean connected = intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false);
                Log.d(TAG, "connected : " + connected);
            }
            Log.d(TAG, "action: " + action);
        }
    }
}