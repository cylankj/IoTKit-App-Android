package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.text.TextUtils;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.n.mvp.contract.setting.SdcardInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

/**
 * Created by cylan-hunt on 17-2-12.
 */

public class SdcardInfoPresenterImpl extends AbstractPresenter<SdcardInfoContract.View>
        implements SdcardInfoContract.Presenter {

    public SdcardInfoPresenterImpl(SdcardInfoContract.View view, String uuid) {
        super(view, uuid);
        view.setPresenter(this);
    }

    @Override
    public void startFormat() {
        if (TextUtils.isEmpty(uuid)) {
            if (BuildConfig.DEBUG)
                throw new IllegalArgumentException("uuid is empty");
            AppLogger.e("uuid is null");
            return;
        }

    }
}
