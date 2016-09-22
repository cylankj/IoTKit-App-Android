package com.cylan.jiafeigou.n.mvp.impl.mag;

import com.cylan.jiafeigou.n.mvp.contract.mag.HomeMagLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.utils.PreferencesUtils;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public class HomeMagLivePresenterImp extends AbstractPresenter<HomeMagLiveContract.View> implements HomeMagLiveContract.Presenter {

    private boolean isChick = false;

    public HomeMagLivePresenterImp(HomeMagLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void clearOpenAndCloseRecord() {

    }

    @Override
    public boolean getNegation() {
        isChick = !isChick;
        return isChick;
    }

    @Override
    public void savaSwitchState(boolean isChick, String key) {
        PreferencesUtils.putBoolean(getView().getContext(),key,isChick);
    }

    @Override
    public boolean getSwitchState(String key) {
        return PreferencesUtils.getBoolean(getView().getContext(),key);
    }
}
