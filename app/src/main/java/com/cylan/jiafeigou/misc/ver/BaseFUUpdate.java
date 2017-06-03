package com.cylan.jiafeigou.misc.ver;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.network.NetMonitor;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.google.gson.Gson;

import rx.subscriptions.CompositeSubscription;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;

/**
 * Created by hds on 17-6-3.
 */

public abstract class BaseFUUpdate implements IFUUpdate {

    private NetMonitor netMonitor;

    protected String uuid;
    protected SimulatePercent simulatePercent;
    protected FUpgradingListener listener;
    protected CompositeSubscription compositeSubscription = new CompositeSubscription();
    protected AbstractVersion.BinVersion binVersion;
    protected static final String TAG = "BaseFUUpdate";

    public String getUuid() {
        return uuid;
    }

    public void setListener(FUpgradingListener listener) {
        this.listener = listener;
    }

    /**
     * {@link com.cylan.jiafeigou.misc.JConstant.U#FAILED_90S}
     * {@link com.cylan.jiafeigou.misc.JConstant.U#FAILED_120S}
     * {@link com.cylan.jiafeigou.misc.JConstant.U#IDLE}
     * {@link com.cylan.jiafeigou.misc.JConstant.U#UPDATING}
     * {@link com.cylan.jiafeigou.misc.JConstant.U#SUCCESS}
     */
    protected int updateState;

    public int getUpdateState() {
        return updateState;
    }

    public BaseFUUpdate() {
        if (netMonitor == null)
            netMonitor = NetMonitor.getNetMonitor();
        if (netMonitor == null)
            netMonitor = NetMonitor.getNetMonitor();
        netMonitor.registerNet(this::onNetworkChanged,
                new String[]{ConnectivityManager.CONNECTIVITY_ACTION,
                        NETWORK_STATE_CHANGED_ACTION});
        simulatePercent = new SimulatePercent();
        simulatePercent.setOnAction(this);
        getVersion();
    }

    private void getVersion() {
        final String content = PreferencesUtils.getString(JConstant.KEY_FIRMWARE_CONTENT + getUuid());
        if (TextUtils.isEmpty(content)) {
            binVersion = AbstractVersion.BinVersion.NULL;
            AppLogger.d("这里出错了,就坑了.");
        }
        try {
            binVersion = new Gson().fromJson(content, AbstractVersion.BinVersion.class);
        } catch (Exception e) {
            binVersion = AbstractVersion.BinVersion.NULL;
            AppLogger.d("这里出错了,就坑了.");
        }
    }

    public int getSimulatePercent() {
        if (simulatePercent == null) return 0;
        return simulatePercent.getProgress();
    }

    protected void onNetworkChanged(Context context, Intent intent) {
    }
}
