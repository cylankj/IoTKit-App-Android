package com.cylan.jiafeigou.n.mvp.impl.setting;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.entity.jniCall.RobotoGetDataRsp;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DataPoint;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.contract.setting.SafeInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class SafeInfoPresenterImpl extends AbstractPresenter<SafeInfoContract.View>
        implements SafeInfoContract.Presenter {
    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};

    public SafeInfoPresenterImpl(SafeInfoContract.View view) {
        super(view);
    }

    @Override
    public void start() {
        super.start();
        DataSourceManager.getInstance().syncAllProperty(uuid, 204, 222);
        robotDataSync();
        robotDeviceDataSync();
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
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        boolean f = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        if (!f) {
            return getView().getContext().getString(R.string.MAGNETISM_OFF);
        }
        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
        int day = info == null ? 0 : info.day;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (((day >> (7 - 1 - i)) & 0x01) == 1) {
                //hit
                builder.append(context.getString(periodResId[i]));
                builder.append(",");
            }
        }
        if (builder.length() > 1) {
            builder.replace(builder.toString().length() - 1, builder.toString().length(), "");
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
    public void getAIStrategy() {
        Subscription subscribe = Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                try {
                    String server = OptionsImpl.getServer();
                    if (!server.contains("http") && !server.contains("https")) {
                        server = "http://" + server;
                    }
                    if (server.contains(":443")) {
                        server = server.replace(":443", ":80");
                    }
                    server = server + "/gray";
                    Log.d("GRAY", "server is:" + server);
                    String account = DataSourceManager.getInstance().getAccount().getAccount();
                    Device device = DataSourceManager.getInstance().getDevice(uuid);
                    JSONArray dataList = new JSONArray();
                    JSONObject tokenParams = new JSONObject();
                    tokenParams.put("act", "get_token");
                    tokenParams.put("account", account);
                    tokenParams.put("app_id", "");
                    Response execute = OkGo.post(server)
                            .requestBody(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), tokenParams.toString()))
                            .execute();
                    JSONObject jsonObject = new JSONObject(execute.body().string());
                    Log.d("GRAY", "get token response:" + jsonObject.toString());
                    int ret = jsonObject.getInt("ret");

                    if (ret == 0) {
                        JSONObject grayParams = new JSONObject();
                        grayParams.put("act", "report_data");
                        grayParams.put("token", jsonObject.getString("token"));
                        grayParams.put("vid", OptionsImpl.getVid());
                        grayParams.put("account", account);
                        grayParams.put("version", device.$(DpMsgMap.ID_207_DEVICE_VERSION, ""));
                        grayParams.put("sys_version", device.$(DpMsgMap.ID_208_DEVICE_SYS_VERSION, ""));
                        grayParams.put("region", DataSourceManager.getInstance().getStorageType());
                        grayParams.put("app_type", 2);
                        grayParams.put("data_list", dataList);
                        execute = OkGo.post(server)
                                .requestBody(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), grayParams.toString()))
                                .execute();
                        JSONObject jsonObject1 = new JSONObject(execute.body().string());
                        Log.d("GRAY", "get gray response:" + jsonObject1.toString());
                        subscriber.onNext(jsonObject);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(new IllegalArgumentException("get token error, ret is:" + ret));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JSONObject>() {
                    @Override
                    public void call(JSONObject jsonObject) {
                      mView.onAIStrategyRsp(jsonObject);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        AppLogger.e(throwable);
                    }
                });
        addStopSubscription(subscribe);
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private void robotDataSync() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class)
                .filter((RobotoGetDataRsp jfgRobotSyncData) -> (
                        getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.identity)
                ))
                .observeOn(AndroidSchedulers.mainThread())
                .map((RobotoGetDataRsp update) -> {
                    getView().deviceUpdate(DataSourceManager.getInstance().getDevice(uuid));
                    return null;
                })
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err: " + MiscUtils.getErr(throwable)));
        addStopSubscription(subscribe);
    }

    /**
     * robot同步数据
     *
     * @return
     */
    private void robotDeviceDataSync() {
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.DeviceSyncRsp.class)
                .filter(jfgRobotSyncData -> (
                        ListUtils.getSize(jfgRobotSyncData.dpList) > 0 &&
                                getView() != null && TextUtils.equals(uuid, jfgRobotSyncData.uuid)
                ))
                .flatMap(ret -> Observable.from(ret.dpList))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                .subscribe(msg -> {
                    try {
                        mView.deviceUpdate(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.e("err: " + MiscUtils.getErr(throwable)));
        addStopSubscription(subscribe);
    }

}
