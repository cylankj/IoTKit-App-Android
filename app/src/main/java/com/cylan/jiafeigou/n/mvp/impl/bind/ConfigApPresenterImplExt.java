package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.jiafeigou.misc.bind.AFullBind;
import com.cylan.jiafeigou.misc.bind.IBindResult;
import com.cylan.jiafeigou.misc.bind.SimpleBindFlow;
import com.cylan.jiafeigou.n.mvp.contract.bind.ConfigApContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * Created by cylan-hunt on 16-11-14.
 */

public class ConfigApPresenterImplExt extends AbstractPresenter<ConfigApContract.View>
        implements ConfigApContract.Presenter, IBindResult {

    private AFullBind aFullBind;

    public ConfigApPresenterImplExt(ConfigApContract.View view) {
        super(view);
        view.setPresenter(this);
        aFullBind = new SimpleBindFlow(this);
    }

    @Override
    public void registerNetworkMonitor() {

    }

    @Override
    public void unregisterNetworkMonitor() {

    }

    @Override
    public void sendWifiInfo(String ssid, String pwd, int type) {
        if (aFullBind != null)
            aFullBind.sendWifiInfo(ssid, pwd, type);
    }

    @Override
    public void checkDeviceState() {

    }

    @Override
    public void refreshWifiList() {

    }

    @Override
    public void clearConnection() {

    }

    @Override
    public void startPingFlow() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void pingFPingFailed() {

    }

    @Override
    public void isMobileNet() {

    }


    @Override
    public void needToUpgrade() {

    }

    @Override
    public void updateState(int state) {

    }


    @Override
    public void bindFailed() {

    }

    @Override
    public void bindSuccess() {

    }
}
