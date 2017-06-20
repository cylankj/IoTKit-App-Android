package com.cylan.jiafeigou.misc.bind;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MD5Util;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.bind.UdpConstant.BIND_TAG;
import static com.cylan.jiafeigou.misc.bind.UdpConstant.UPGRADE_VERSION;

/**
 * Created by cylan-hunt on 16-11-14.
 */

public class SimpleBindFlow extends AFullBind {

    public SimpleBindFlow(IBindResult iBindResult) {
        super(iBindResult);
    }

    @Override
    public void startPingFPing(final String shortUUID) {
        //空
        setDevicePortrait(null);
        sendPingFPing();
    }

    @Override
    public void startUpgrade() {
        registerUpgradeMonitor();
        startupHttpServerSendFile();
    }

    private void startupHttpServerSendFile() {
        //开始发送升级消息
        Observable.just(isDogUpgrading)
                .filter((Boolean aBoolean) -> {
                    //如果在升级,就不继续
                    UdpConstant.UpgradeStatus status = RxBus.getCacheInstance().getStickyEvent(UdpConstant.UpgradeStatus.class);
                    AppLogger.i(BIND_TAG + ":升级状态: " + status);
                    return !isDogUpgrading && status != null && status.state != IBindResult.UPGRADE_FAILED;
                })
                .map((Boolean aBoolean) -> {
                    AppLogger.e(BIND_TAG + "开始启动httpServer升级狗");
                    RxBus.getCacheInstance().postSticky(new UdpConstant.UpgradeStatus(IBindResult.UPGRADING));
                    return null;
                });
    }

    private void registerUpgradeMonitor() {
        RxBus.getCacheInstance().toObservableSticky(JfgUdpMsg.FPingAck.class)
                .filter((JfgUdpMsg.FPingAck fPingAck) -> {
                    //hit target
                    return isDogUpgrading
                            && TextUtils.equals(fPingAck.cid, devicePortrait.uuid)
                            && TextUtils.equals(fPingAck.mac, devicePortrait.mac);
                })
                .map((JfgUdpMsg.FPingAck fPingAck) -> {
                    AppLogger.e(BIND_TAG + "sdk还没支持 upgrade status");
                    return null;
                });
    }

    /**
     * 发送,三次
     */
    private void sendPingFPing() {
        Observable.just(1)
                .subscribeOn(Schedulers.newThread())
                .subscribe(i -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                                UdpConstant.PORT,
                                new JfgUdpMsg.Ping().toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                                UdpConstant.PORT,
                                new JfgUdpMsg.FPing().toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP,
                                UdpConstant.PORT,
                                new JfgUdpMsg.Ping().toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP,
                                UdpConstant.PORT,
                                new JfgUdpMsg.FPing().toBytes());
                    } catch (JfgException e) {
                        AppLogger.e("err:" + MiscUtils.getErr(e));
                    }
                    AppLogger.i(BIND_TAG + i);
                }, AppLogger::e);
        AppLogger.i("sendPingFPing");
    }

    /**
     * 强势使用{@link Observable#timeout(long, TimeUnit, Observable)}
     *
     * @return
     */
    private Observable<UdpConstant.UdpDevicePortrait> timeoutException(boolean check3G) {
        return Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter((Object o) -> {
                    //没有设备画像
                    return devicePortrait == null && !check3G;
                })
                .map(new Func1<Object, UdpConstant.UdpDevicePortrait>() {
                    @Override
                    public UdpConstant.UdpDevicePortrait call(Object o) {
                        iBindResult.pingFPingFailed();
                        AppLogger.i(BIND_TAG + "ping fping flow failed");
                        return null;
                    }
                });
    }

    /**
     * 发送服务器信息,发送timeZone信息
     *
     * @param udpDevicePortrait
     */
    public void setServerLanguage(UdpConstant.UdpDevicePortrait udpDevicePortrait) {
        try {
            AppLogger.i(BIND_TAG + udpDevicePortrait);
            //
            String serverAddress = OptionsImpl.getServer();
            int port = Integer.parseInt(serverAddress.substring(serverAddress.indexOf(":") + 1));
            serverAddress = serverAddress.split(":")[0];
            if (TextUtils.isEmpty(serverAddress) && BuildConfig.DEBUG)
                throw new IllegalArgumentException("server address is empty");
            //设置语言
            JfgUdpMsg.SetLanguage setLanguage = new JfgUdpMsg.SetLanguage(
                    udpDevicePortrait.uuid,
                    udpDevicePortrait.mac,
                    JFGRules.getLanguageType(ContextUtils.getContext()));

//            JfgUdpMsg.SetTimeZone setTimeZone = new JfgUdpMsg.SetTimeZone(udpDevicePortrait.uuid,
//                    udpDevicePortrait.mac, TimeZone.getDefault().getRawOffset() / 1000);
            //设置服务器
            JfgUdpMsg.SetServer setServer = new JfgUdpMsg.SetServer(udpDevicePortrait.uuid,
                    udpDevicePortrait.mac,
                    serverAddress,
                    port,
                    80);
            //增加绑定随机数.
            bindCode = BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount() + System.currentTimeMillis();
            bindCode = MD5Util.lowerCaseMD5(bindCode);//cast to md5
            devicePortrait.bindCode = bindCode;
            JfgUdpMsg.FBindDeviceCode code = new JfgUdpMsg.FBindDeviceCode(
                    udpDevicePortrait.uuid, udpDevicePortrait.mac, bindCode);
            try {
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                        UdpConstant.PORT, code.toBytes());
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP,
                        UdpConstant.PORT, code.toBytes());
            } catch (JfgException e) {
                AppLogger.e("e: " + e.getLocalizedMessage());
            }
            AppLogger.i(BIND_TAG + "setServer: " + new Gson().toJson(setServer));
            AppLogger.i(BIND_TAG + "setLanguage: " + new Gson().toJson(setLanguage));
            AppLogger.i(BIND_TAG + "setCode: " + new Gson().toJson(code));
            try {
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                        UdpConstant.PORT,
                        setServer.toBytes());
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                        UdpConstant.PORT,
                        setLanguage.toBytes());
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP,
                        UdpConstant.PORT,
                        setServer.toBytes());
                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP,
                        UdpConstant.PORT,
                        setLanguage.toBytes());
            } catch (JfgException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            AppLogger.e("err: " + e.getLocalizedMessage());
        }
    }

    /**
     * 此处的消息来源非常关键{@link com.cylan.jiafeigou.n.engine.GlobalUdpDataSource}
     * emit出来 {@link JfgUdpMsg.PingAck},两者都是异步.
     *
     * @param ssidInDigits
     * @return
     */
    private Observable<JfgUdpMsg.PingAck> pingObservable(final String ssidInDigits) {
        return RxBus.getCacheInstance().toObservable(JfgUdpMsg.PingAck.class)
                .filter((JfgUdpMsg.PingAck pingAck) -> {
                    //注意条件
                    return !TextUtils.isEmpty(pingAck.cid)
                            && pingAck.cid.endsWith(ssidInDigits);
                })
                .first();
    }

    /**
     * 此处的消息来源非常关键{@link com.cylan.jiafeigou.n.engine.GlobalUdpDataSource}
     * emit出来 {@link JfgUdpMsg.FPingAck}
     *
     * @param ssidInDigits f_ping_ack 消息只取100ms内的第一条
     * @return
     */
    private Observable<JfgUdpMsg.FPingAck> fPingObservable(final String ssidInDigits) {
        return RxBus.getCacheInstance().toObservable(JfgUdpMsg.FPingAck.class)
                .filter((JfgUdpMsg.FPingAck pingAck) -> {
                    //注意条件
                    return !TextUtils.isEmpty(pingAck.cid)
                            && pingAck.cid.endsWith(ssidInDigits);
                })
                .first();
    }

    @Override
    public void sendServerInfo(String ip, int host) {

    }


    @Override
    public void sendLanguageInfo() {

    }

    @Override
    public void sendWifiInfo(final String ssid, final String pwd, final int type) {
        Observable.just(1)
                .subscribeOn(Schedulers.newThread())
                .filter(ret -> devicePortrait != null)
                .map((Integer o) -> {
                    AppLogger.d(BIND_TAG + "sendWifiInfo:" + devicePortrait);
                    Log.e(TAG, "sendWifiInfo: " + new Gson().toJson(devicePortrait));
                    JfgUdpMsg.DoSetWifi setWifi = new JfgUdpMsg.DoSetWifi(devicePortrait.uuid,
                            devicePortrait.mac,
                            ssid, pwd);
                    setWifi.security = type;
                    //发送wifi配置
                    try {
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                                UdpConstant.PORT,
                                setWifi.toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP,
                                UdpConstant.PORT,
                                setWifi.toBytes());
                        AppLogger.d(TAG + new Gson().toJson(setWifi));
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return devicePortrait;
                })
                .last()
                .subscribe((UdpConstant.UdpDevicePortrait portrait) -> {
                    //此时,设备还没恢复连接,需要加入队列
                    AppLogger.d("设备画像为:" + portrait);
                    portrait.bindCode = bindCode;
                    PreferencesUtils.putString(JConstant.BINDING_DEVICE, new Gson().toJson(portrait));
                    AppLogger.i(BIND_TAG + "onLocalFlowFinish: " + portrait);
                    //恢复wifi
                    iBindResult.onLocalFlowFinish();
                }, (Throwable throwable) -> {
                    AppLogger.e(BIND_TAG + throwable.getLocalizedMessage());
                });

    }

    @Override
    public Observable<UdpConstant.UdpDevicePortrait> getBindObservable(boolean check3GCase, String shortUUID) {
        //zip用法,合并,这里使用了timeout,也就是说,次subscription的生命周期只有1s
        AppLogger.d(BIND_TAG + "check3GCase:" + check3GCase);
        return Observable.zip(pingObservable(shortUUID),
                fPingObservable(shortUUID), (JfgUdpMsg.PingAck pingAck, JfgUdpMsg.FPingAck fPingAck) -> {
                    //此处完成了第1和第2步.
                    UdpConstant.UdpDevicePortrait d = BindUtils.assemble(pingAck, fPingAck);
                    setDevicePortrait(d);
                    AppLogger.i(BIND_TAG + d);
                    return d;
                })
                //1s内
                .timeout(3000, TimeUnit.MILLISECONDS, timeoutException(check3GCase))
                .subscribeOn(Schedulers.newThread())
                //是否需要升级
                .filter((UdpConstant.UdpDevicePortrait udpDevicePortrait) -> {
                    boolean needUpdate = udpDevicePortrait != null && BindUtils.versionCompare(UPGRADE_VERSION, udpDevicePortrait.version) > 0
                            && BindUtils.isUcos(udpDevicePortrait.uuid);
                    //是否需要升级
                    if (needUpdate)
                        iBindResult.needToUpgrade();
                    AppLogger.d(BIND_TAG + "need to upgrade: " + needUpdate);
                    return !needUpdate;
                }).first();
    }

    @Override
    public Observable<Boolean> sendWifiInfo(String uuid, String mac, String ssid, String pwd, int type) {
        return Observable.just("send")
                .flatMap(s -> {
                    JfgUdpMsg.DoSetWifi setWifi = new JfgUdpMsg.DoSetWifi(
                            uuid,
                            mac,
                            ssid,
                            pwd);
                    setWifi.security = type;
                    //发送wifi配置
                    try {
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                                UdpConstant.PORT,
                                setWifi.toBytes());
                        BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP,
                                UdpConstant.PORT,
                                setWifi.toBytes());
                        AppLogger.d(TAG + new Gson().toJson(setWifi));
                    } catch (JfgException e) {
                        AppLogger.e("err:" + MiscUtils.getErr(e));
                    }
                    return Observable.just(true);
                });
    }
}
