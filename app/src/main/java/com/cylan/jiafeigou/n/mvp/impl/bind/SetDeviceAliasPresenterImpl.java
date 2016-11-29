package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.SetDeviceAliasContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * Created by cylan-hunt on 16-11-29.
 */

public class SetDeviceAliasPresenterImpl extends AbstractPresenter<SetDeviceAliasContract.View>
        implements SetDeviceAliasContract.Presenter {
    private UdpConstant.UdpDevicePortrait portrait;

    public SetDeviceAliasPresenterImpl(SetDeviceAliasContract.View view,
                                       UdpConstant.UdpDevicePortrait portrait) {
        super(view);
        view.setPresenter(this);
        this.portrait = portrait;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
