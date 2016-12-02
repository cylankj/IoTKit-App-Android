package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.content.Context;
import android.util.Pair;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.setting.SafeInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class SafeInfoPresenterImpl extends AbstractPresenter<SafeInfoContract.View>
        implements SafeInfoContract.Presenter {

    private BeanCamInfo beanCamInfo;
    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};

    public SafeInfoPresenterImpl(SafeInfoContract.View view, BeanCamInfo beanCamInfo) {
        super(view);
        this.beanCamInfo = beanCamInfo;
        view.setPresenter(this);
    }

    @Override
    public void saveCamInfoBean(BeanCamInfo beanCamInfo, int id) {
        this.beanCamInfo = beanCamInfo;
        Observable.just(new Pair<>(beanCamInfo, id))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<BeanCamInfo, Integer>>() {
                    @Override
                    public void call(Pair<BeanCamInfo, Integer> beanCamInfoIntegerPair) {
                        int id = beanCamInfoIntegerPair.second;
                        RxEvent.JFGAttributeUpdate update = new RxEvent.JFGAttributeUpdate();
                        update.uuid = beanCamInfoIntegerPair.first.deviceBase.uuid;
                        update.o = beanCamInfoIntegerPair.first.getObject(id);
                        update.msgId = id;
                        update.version = System.currentTimeMillis();
                        RxBus.getCacheInstance().post(update);
                        JfgCmdInsurance.getCmd().robotSetData(beanCamInfoIntegerPair.first.deviceBase.uuid,
                                DpUtils.getList(id,
                                        beanCamInfoIntegerPair.first.getByte(id)
                                        , System.currentTimeMillis()));
                        AppLogger.i("save bean Cam info");
                    }
                });
    }

    @Override
    public BeanCamInfo getBeanCamInfo() {
        return beanCamInfo;
    }

    @Override
    public String getRepeatMode(Context context) {
        if (!beanCamInfo.cameraAlarmFlag || beanCamInfo.cameraAlarmInfo == null) {
            return getView().getContext().getString(R.string.MAGNETISM_OFF);
        }
        int day = beanCamInfo.cameraAlarmInfo.day;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (((day >> (7 - 1 - i)) & 0x01) == 1) {
                //hit
                builder.append(context.getString(periodResId[i]));
                builder.append(",");
            }
        }
        if (day == 127) {//全天
            builder.setLength(0);
            builder.append(context.getString(R.string.HOURS));
        } else if (day == 124) {//工作日
            builder.setLength(0);
            builder.append(context.getString(R.string.WEEKDAYS));
        }
        return builder.toString();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
