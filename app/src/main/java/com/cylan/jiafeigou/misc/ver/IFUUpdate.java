package com.cylan.jiafeigou.misc.ver;

/**
 * Created by hds on 17-6-3.
 */

import com.cylan.jiafeigou.misc.SimulatePercent;

import rx.functions.Action1;

/**
 * 固件升级
 */
public interface IFUUpdate extends Action1<String>, SimulatePercent.OnAction {
    interface FUpgradingListener {

        void upgradeStart();

        void upgradeProgress(int percent);

        void upgradeErr(int errCode);

        void upgradeSuccess();
    }
}
