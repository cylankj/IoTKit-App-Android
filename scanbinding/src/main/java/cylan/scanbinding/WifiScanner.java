package cylan.scanbinding;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cylan.scanbinding.presenter.Presenter;

/**
 * Created by hunt on 16-4-6.
 */
public class WifiScanner implements Scanner {


    private Context context;
    private ScanConfigure.Builder builder;
    private Worker worker;
    private Handler handler;
    Presenter presenter;
    private WifiBr wifiBr;

    public WifiScanner(Presenter presenter, ScanConfigure configure) {
        this.presenter = presenter;
        setConfigure(configure);
    }

    public void setConfigure(ScanConfigure configure) {
        this.context = configure.getBuilder().getContext();
        this.builder = configure.getBuilder();
        initWorker();
    }

    private void initWorker() {
        worker = new Worker("WifiScanner");
        handler = worker.getHandler();
    }

    @Override
    public void init() {
        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        wifiBr = new WifiBr(presenter, builder);
        this.context.registerReceiver(wifiBr, intentFilter);
    }

    @Override
    public void startScan() {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (this.builder == null)
            throw new NullPointerException("Builder may not be initialized");
        if (wifiBr == null)
            throw new NullPointerException("Builder may not be initialized");
        boolean startScan;
        if (builder.isTurnOffWifiBeforeScan()) {
            wifiManager.setWifiEnabled(false);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    wifiManager.setWifiEnabled(true);
                }
            }, 500);
            startScan = true;
        } else {
            startScan = wifiManager.startScan();
        }
        if (presenter != null)
            presenter.onScan(startScan);
    }

    @Override
    public void destroy() {
        try {
            context.unregisterReceiver(wifiBr);
        } catch (Exception e) {
        }
        if (presenter != null)
            presenter = null;
        if (worker != null && worker.getHandler() != null)
            worker.getHandler().removeCallbacksAndMessages(null);
    }

    private static class WifiBr extends BroadcastReceiver {

        private WeakReference<Presenter> weakReference;
        private WeakReference<ScanConfigure.Builder> builderWeakReference;


        WifiBr(Presenter presenter, ScanConfigure.Builder builder) {
            this.weakReference = new WeakReference<>(presenter);
            this.builderWeakReference = new WeakReference<>(builder);
        }


        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Presenter presenter = weakReference.get();
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (presenter == null)
                return;
            if (TextUtils.equals(action, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                presenter.onResult(filterResults(wifiManager.getScanResults()));
            }
        }

        /**
         * @param results : 根据提供的 过滤条件,提取最终的结果.
         * @return
         */
        private List<ScanResult> filterResults(List<ScanResult> results) {
            final int count = results == null ? 0 : results.size();
            ScanConfigure.Builder builder = builderWeakReference.get();
            if (builder == null || count == 0)
                return null;
            List<ScanResult> finalResult = new ArrayList<>();
            List<String> filters = builder.getFilterList();
            for (int i = 0; i < count; i++) {
                ScanResult scanResult = results.get(i);
                if (TextUtils.isEmpty(scanResult.SSID)) continue;
                final String ssid = Utils.removeDoubleQuotes(scanResult.SSID);
                if (Utils.contains(filters, ssid))
                    finalResult.add(scanResult);
            }
            return finalResult;
        }
    }
}
