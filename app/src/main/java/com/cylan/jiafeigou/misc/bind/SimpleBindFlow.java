package com.cylan.jiafeigou.misc.bind;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
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
import rx.Subscription;
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
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> {
                    try {
                        for (int i = 0; i < 2; i++) {
                            Command.getInstance().sendLocalMessage(UdpConstant.IP,
                                    UdpConstant.PORT,
                                    new JfgUdpMsg.Ping().toBytes());
                            Command.getInstance().sendLocalMessage(UdpConstant.IP,
                                    UdpConstant.PORT,
                                    new JfgUdpMsg.FPing().toBytes());
                            Command.getInstance().sendLocalMessage(UdpConstant.PIP,
                                    UdpConstant.PORT,
                                    new JfgUdpMsg.Ping().toBytes());
                            Command.getInstance().sendLocalMessage(UdpConstant.PIP,
                                    UdpConstant.PORT,
                                    new JfgUdpMsg.FPing().toBytes());
                        }
                        AppLogger.d("扫描设备....ping fping");
                    } catch (JfgException e) {
                        AppLogger.e("err:" + MiscUtils.getErr(e));
                    }
                    AppLogger.i(BIND_TAG + ret);
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
                        MiscUtils.checkJFGLikeApp();
                        MiscUtils.checkVPNState();
                        return null;
                    }
                });
    }

    /**
     * 发送服务器信息,发送timeZone信息
     *
     * @param portrait
     */
    @Override
    public void setServerLanguage(UdpConstant.UdpDevicePortrait portrait) {
        Observable.just(portrait)
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> {
                    try {
                        AppLogger.i(BIND_TAG + portrait);
                        //
                        String serverAddress = OptionsImpl.getServer();
                        int port = Integer.parseInt(serverAddress.substring(serverAddress.indexOf(":") + 1));
                        serverAddress = serverAddress.split(":")[0];
                        if (TextUtils.isEmpty(serverAddress) && BuildConfig.DEBUG) {
                            throw new IllegalArgumentException("server address is empty");
                        }
                        //设置语言
                        JfgUdpMsg.SetLanguage setLanguage = new JfgUdpMsg.SetLanguage(
                                portrait.uuid,
                                portrait.mac,
                                JFGRules.getLanguageType(ContextUtils.getContext()));

                        //设置服务器
                        JfgUdpMsg.SetServer setServer = new JfgUdpMsg.SetServer(portrait.uuid,
                                portrait.mac,
                                serverAddress,
                                port,
                                80);
                        //增加绑定随机数.
                        String bindCode = DataSourceManager.getInstance().getJFGAccount().getAccount() + System.currentTimeMillis();
                        ret.bindCode = MD5Util.lowerCaseMD5(bindCode);//cast to md5
                        devicePortrait = ret;
                        JfgUdpMsg.FBindDeviceCode code = new JfgUdpMsg.FBindDeviceCode(
                                ret.uuid, portrait.mac, bindCode);
                        try {
                            for (int i = 0; i < 2; i++) {
                                Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, code.toBytes());
                                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, code.toBytes());
                            }
                        } catch (JfgException e) {
                            AppLogger.e("e: " + e.getLocalizedMessage());
                        }
                        AppLogger.i(BIND_TAG + "setServer: " + new Gson().toJson(setServer));
                        AppLogger.i(BIND_TAG + "setLanguage: " + new Gson().toJson(setLanguage));
                        AppLogger.i(BIND_TAG + "setCode: " + new Gson().toJson(code));
                        try {
                            for (int i = 0; i < 3; i++) {
                                Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, setServer.toBytes());
                                Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, setLanguage.toBytes());
                                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setServer.toBytes());
                                Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setLanguage.toBytes());
                            }
                        } catch (JfgException e) {
                            AppLogger.e("err:" + e);
                        }
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                }, AppLogger::e);
    }

    private MapSubscription subscriptionMap = new MapSubscription();

    /**
     * 此处的消息来源非常关键{@link com.cylan.jiafeigou.n.engine.GlobalUdpDataSource}
     * emit出来 {@link JfgUdpMsg.PingAck},两者都是异步.
     *
     * @param cidSuffix
     * @return
     */
    private Observable<JfgUdpMsg.PingAck> pingObservable(final String cidSuffix) {
        return Observable.create(subscriber -> {
            final Subscription sub = RxBus.getCacheInstance().toObservable(JfgUdpMsg.PingAck.class)
                    .timeout(3, TimeUnit.SECONDS)
                    .filter(pingAck -> pingAck != null && !TextUtils.isEmpty(pingAck.cid) && pingAck.cid.endsWith(cidSuffix))
                    .timeout(3, TimeUnit.SECONDS)
                    .subscribe(pingAck -> {
                        AppLogger.d(BIND_TAG + "得到ping消息");
                        if (subscriber != null && !subscriber.isUnsubscribed()) {
                            subscriber.onNext(pingAck);
                            subscriber.onCompleted();
                        }
                        //结束本身.
                        subscriptionMap.remove("PingAck");
                    }, throwable -> {
                        subscriber.onError(new RxEvent.HelperBreaker(1));
                        subscriptionMap.remove("PingAck");
                    });
            subscriptionMap.add(sub, "PingAck");
            try {
                for (int i = 0; i < 2; i++) {
                    Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.Ping().toBytes());
                    Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, new JfgUdpMsg.Ping().toBytes());
                }
            } catch (JfgException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 此处的消息来源非常关键{@link com.cylan.jiafeigou.n.engine.GlobalUdpDataSource}
     * emit出来 {@link JfgUdpMsg.FPingAck}
     *
     * @param pingAck f_ping_ack 消息只取100ms内的第一条
     * @return
     */
    private Observable<UdpConstant.UdpDevicePortrait> fPingObservable(JfgUdpMsg.PingAck pingAck) {
        return Observable.create(subscriber -> {
            final Subscription sub = RxBus.getCacheInstance().toObservable(JfgUdpMsg.FPingAck.class)
                    .timeout(3, TimeUnit.SECONDS)
                    .filter(ret -> pingAck != null && TextUtils.equals(pingAck.cid, ret.cid))
                    .timeout(3, TimeUnit.SECONDS)
                    .subscribe(ret -> {
                        AppLogger.d(BIND_TAG + "得到fping消息");
                        devicePortrait = new UdpConstant.UdpDevicePortrait();
                        devicePortrait.uuid = ret.cid;
                        devicePortrait.mac = ret.mac;
                        devicePortrait.version = ret.version;
                        devicePortrait.net = pingAck.net;
                        devicePortrait.pid = pingAck.pid;
                        if (subscriber != null && !subscriber.isUnsubscribed()) {
                            subscriber.onNext(devicePortrait);
                            subscriber.onCompleted();
                            //结束本身.
                            subscriptionMap.remove("FPingAck");
                        }
                    }, throwable -> {
                        subscriber.onError(new RxEvent.HelperBreaker(2));
                        subscriptionMap.remove("FPingAck");
                    });
            subscriptionMap.add(sub, "FPingAck");
            try {
                for (int i = 0; i < 2; i++) {
                    Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                    Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                }
            } catch (JfgException e) {
                e.printStackTrace();
            }
        });
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
                .subscribeOn(Schedulers.io())
                .delay(200, TimeUnit.MILLISECONDS)
                .filter((Integer integer) -> {
                    return devicePortrait != null;
                })
                .map((Integer o) -> {
                    AppLogger.d(BIND_TAG + "sendBindConfig:" + devicePortrait + ",ssid:" + ssid + ",psw:" + pwd);
                    Log.e(TAG, "sendBindConfig: " + new Gson().toJson(devicePortrait));
                    for (int i = 0; i < 3; i++) {
                        JfgUdpMsg.DoSetWifi setWifi = new JfgUdpMsg.DoSetWifi(devicePortrait.uuid,
                                devicePortrait.mac,
                                ssid, pwd);
                        setWifi.security = type;
                        //发送wifi配置
                        try {
                            Command.getInstance().sendLocalMessage(UdpConstant.IP,
                                    UdpConstant.PORT,
                                    setWifi.toBytes());
                            Command.getInstance().sendLocalMessage(UdpConstant.PIP,
                                    UdpConstant.PORT,
                                    setWifi.toBytes());
                            AppLogger.d(TAG + new Gson().toJson(setWifi));
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                    return devicePortrait;
                })
                .subscribe((UdpConstant.UdpDevicePortrait portrait) -> {
                    //此时,设备还没恢复连接,需要加入队列
                    AppLogger.d("设备画像为:" + portrait);
                    PreferencesUtils.putString(JConstant.BINDING_DEVICE, new Gson().toJson(portrait));
                    AppLogger.i(BIND_TAG + "onLocalFlowFinish: " + portrait);
                    //恢复wifi
                    iBindResult.onLocalFlowFinish();
                }, (Throwable throwable) -> AppLogger.e(BIND_TAG + throwable.getLocalizedMessage()));

    }

    @Override
    public Observable<UdpConstant.UdpDevicePortrait> getBindObservable(boolean check3GCase, String shortUUID) {
        //zip用法,合并,这里使用了timeout,也就是说,次subscription的生命周期只有1s
        AppLogger.d(BIND_TAG + "check3GCase:" + check3GCase);
        return Observable.just(shortUUID)
                .subscribeOn(Schedulers.io())
                .flatMap(this::pingObservable)
                .flatMap(this::fPingObservable)
                //1s内
                .timeout(3, TimeUnit.SECONDS, timeoutException(check3GCase))
//                .subscribeOn(Schedulers.io())
                //是否需要升级
                .filter((UdpConstant.UdpDevicePortrait udpDevicePortrait) -> {
                    boolean needUpdate = false;
                    try {
                        //todo 这里没有判断 udpDevicePortrait 是否为 null
                        Log.i("CYLAN_TAG", "UPGRADE_VERSION:" + UPGRADE_VERSION + ",udpDevicePortrait.version:" + (udpDevicePortrait == null ? "" : udpDevicePortrait.version));
                        needUpdate = udpDevicePortrait != null && BindUtils.versionCompare(UPGRADE_VERSION, udpDevicePortrait.version) > 0
                                && BindUtils.isUcos(udpDevicePortrait.uuid);
                    } catch (Exception e) {
                        AppLogger.e(e.getMessage());
                    }
                    //是否需要升级
                    if (needUpdate) {
                        iBindResult.needToUpgrade();
                    }
                    AppLogger.d(BIND_TAG + "need to upgrade: " + needUpdate);
                    return !needUpdate;
                });
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
                        // fix #126372 by lxh
                        Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, setWifi.toBytes());
                        Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, setWifi.toBytes());
                        Command.getInstance().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, setWifi.toBytes());
                        Command.getInstance().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, setWifi.toBytes());
                        AppLogger.d(TAG + new Gson().toJson(setWifi));
                    } catch (JfgException e) {
                        AppLogger.e("err:" + MiscUtils.getErr(e));
                    }
                    return Observable.just(true);
                });
    }
}
