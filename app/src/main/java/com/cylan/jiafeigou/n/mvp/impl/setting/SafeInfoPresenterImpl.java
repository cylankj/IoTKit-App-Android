package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.content.Context;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.contract.setting.SafeInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class SafeInfoPresenterImpl extends AbstractPresenter<SafeInfoContract.View>
        implements SafeInfoContract.Presenter {
    private String uuid;
    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};

    public SafeInfoPresenterImpl(SafeInfoContract.View view, String uuid) {
        super(view);
        this.uuid = uuid;
        view.setPresenter(this);
    }

    @Override
    public <T extends DataPoint> void updateInfoReq(T value, long id) {
        Observable.just(value)
                .subscribeOn(Schedulers.io())
                .subscribe((Object o) -> {
                    try {
                        DataSourceManager.getInstance().updateValue(uuid, value, (int) id);
                    } catch (IllegalAccessException e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, (Throwable throwable) -> {
                    AppLogger.e(throwable.getLocalizedMessage());
                });
    }

    @Override
    public String getRepeatMode(Context context) {
        DpMsgDefine.DPPrimary<Boolean> flag = DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
        boolean f = MiscUtils.safeGet(flag, false);
        if (!f) {
            return getView().getContext().getString(R.string.MAGNETISM_OFF);
        }
        DpMsgDefine.DPAlarmInfo info = MiscUtils.safeGet_(DataSourceManager.getInstance().getValue(uuid, DpMsgMap.ID_502_CAMERA_ALARM_INFO), DpMsgDefine.EMPTY.ALARM_INFO);
        int day = info == null ? 0 : info.day;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (((day >> (7 - 1 - i)) & 0x01) == 1) {
                //hit
                builder.append(context.getString(periodResId[i]));
                builder.append(",");
            }
        }
        if (builder.length() > 1)
            builder.replace(builder.toString().length() - 1, builder.toString().length(), "");
        if (day == 127) {//全天
            builder.setLength(0);
            builder.append(context.getString(R.string.HOURS));
        } else if (day == 124) {//工作日
            builder.setLength(0);
            builder.append(context.getString(R.string.WEEKDAYS));
        }
        return builder.toString();
    }
}
