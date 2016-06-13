package com.cylan.jiafeigou.n.model.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.cylan.jiafeigou.n.model.contract.ModelContract;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;

import java.util.Calendar;

/**
 * Created by chen on 6/12/16.
 */
public class HomeWonderfulModelImpl extends BroadcastReceiver implements ModelContract.HomeWonderfulOps {
    private int curMiniutes = 0;
    private final int daytime = 0;
    private final int night = 1;
    private final Calendar calendar = Calendar.getInstance();
    private HomeWonderfulContract.PresenterRequiredOps mPresenter;

    public HomeWonderfulModelImpl(HomeWonderfulContract.PresenterRequiredOps presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_TIME_TICK)) {
            if (curMiniutes < 60)
                curMiniutes++;
            else {
                //每小时触发一次
                halfDayCount();
                curMiniutes = 0;
            }
        }
    }

    public void halfDayCount() {
        calendar.setTimeInMillis( System.currentTimeMillis());
        final int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (curHour >= 6 && curHour < 18) {
            mPresenter.changeHeadBackground(daytime);
        }else {
            mPresenter.changeHeadBackground(night);
        }
    }

    @Override
    public void setHeadBackground() {
        halfDayCount();
    }
}
