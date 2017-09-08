package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.content.Context;

import com.cylan.jiafeigou.n.mvp.contract.setting.ApSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

/**
 * Created by hds on 17-9-7.
 */

public class ApSettingsPresenter extends AbstractPresenter<ApSettingContract.View>
        implements ApSettingContract.Presenter {
    public ApSettingsPresenter(ApSettingContract.View view) {
        super(view);
    }

    @Override
    public void setPresenter(Object presenter) {

    }

    @Override
    public Context getContext() {
        return null;
    }
}
