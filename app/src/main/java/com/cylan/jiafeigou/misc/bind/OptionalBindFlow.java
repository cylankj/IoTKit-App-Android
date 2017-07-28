package com.cylan.jiafeigou.misc.bind;

import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.misc.MapSubscription;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MD5Util;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.bind.UdpConstant.BIND_TAG;
import static com.cylan.jiafeigou.misc.bind.UdpConstant.UPGRADE_VERSION;

/**
 * Created by cylan-hunt on 16-11-14.
 */

public class OptionalBindFlow extends AFullBind {

    public OptionalBindFlow(IBindResult iBindResult) {
        super(iBindResult);
    }


    private MapSubscription subscriptionMap = new MapSubscription();

    private static final int PING_ERR = 1;
    private static final int FPING_ERR = 2;
    private static final int SET_WIFI_ERR = 3;

    public void setWifiInfo(final String uuidSuffix, final String ssid, final String pwd, final int type) {
        Observable.just(uuidSuffix)
                .subscribeOn(Schedulers.io())
                .flatMap(s -> ping(s)
                        .flatMap(this::fping)
                        .flatMap(this::check3G)
                        .flatMap(this::setMiscInfo))
                .subscribe(ret -> setWifiInfo(ret.uuid, ret.mac, ssid, pwd, type), throwable -> AppLogger.e(BIND_TAG + "err: " + MiscUtils.getErr(throwable)));
    }

    public void setWifiInfo(final String uuid, final String mac,
                             final String ssid, final String pwd, final int type) {
//        return Observable.create(subscriber -> {
//            final Subscription subscription = RxBus.getCacheInstance().toObservable(JfgUdpMsg.DoSetWifiAck.class)
//                    .timeout(1, TimeUnit.SECONDS)
//                    .filter(doSetWifiAck -> doSetWifiAck != null && doSetWifiAck.ret == 0)
//                    .timeout(1, TimeUnit.SECONDS)
//                    .subscribe(ret -> {
//                        AppLogger.d(BIND_TAG + "发送wifi成功");
//                        subscriber.onNext(true);
//                        subscriber.onCompleted();
//                        subscriptionMap.remove("setWifiInfo");
//                        subscriptionMap.clear();
//                    }, throwable -> {
//                        subscriptionMap.remove("setWifiInfo");
//                        subscriber.onError(new RxEvent.HelperBreaker(SET_WIFI_ERR));
//                    });
//            subscriptionMap.add(subscription, "setWifiInfo");
        JfgUdpMsg.DoSetWifi setWifi = new JfgUdpMsg.DoSetWifi(uuid, mac, ssid, pwd);
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
//        });
    }

    /**
     * 语言,server,bindCode
     *
     * @param udpDevicePortrait
     * @return
     */
    public Observable<UdpConstant.UdpDevicePortrait> setMiscInfo(UdpConstant.UdpDevicePortrait udpDevicePortrait) {
        return Observable.just(udpDevicePortrait)
                .map(ret -> {
                    try {
                        AppLogger.i(BIND_TAG + ret);
                        String serverAddress = OptionsImpl.getServer();
                        int port = Integer.parseInt(serverAddress.substring(serverAddress.indexOf(":") + 1));
                        serverAddress = serverAddress.split(":")[0];
                        if (TextUtils.isEmpty(serverAddress) && BuildConfig.DEBUG)
                            throw new IllegalArgumentException("server address is empty");
                        //设置语言
                        JfgUdpMsg.SetLanguage setLanguage = new JfgUdpMsg.SetLanguage(
                                ret.uuid,
                                ret.mac,
                                JFGRules.getLanguageType(ContextUtils.getContext()));
                        //设置服务器
                        JfgUdpMsg.SetServer setServer = new JfgUdpMsg.SetServer(ret.uuid,
                                ret.mac,
                                serverAddress,
                                port,
                                80);
                        //增加绑定随机数.
                        String bindCode = BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount() + System.currentTimeMillis();
                        bindCode = MD5Util.lowerCaseMD5(bindCode);//cast to md5
                        ret.bindCode = bindCode;
                        JfgUdpMsg.FBindDeviceCode code = new JfgUdpMsg.FBindDeviceCode(
                                ret.uuid, ret.mac, bindCode);
                        try {
                            final boolean isRs = false;
                            for (int i = 0; i < 2; i++) {
                                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                                        UdpConstant.PORT, code.toBytes());
                                BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP,
                                        UdpConstant.PORT, code.toBytes());
                            }
                        } catch (JfgException e) {
                            AppLogger.e("e: " + e.getLocalizedMessage());
                        }
                        AppLogger.i(BIND_TAG + "setServer: " + new Gson().toJson(setServer));
                        AppLogger.i(BIND_TAG + "setLanguage: " + new Gson().toJson(setLanguage));
                        AppLogger.i(BIND_TAG + "setCode: " + new Gson().toJson(code));
                        try {
                            for (int i = 0; i < 3; i++) {
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
                            }
                        } catch (JfgException e) {
                            AppLogger.e("err:" + e);
                        }
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                    return ret;
                });
    }

    public Observable<UdpConstant.UdpDevicePortrait> check3G(UdpConstant.UdpDevicePortrait fPingAck) {
        return Observable.just(fPingAck)
                .filter(ret -> {
                    AppLogger.d(BIND_TAG + "检查3G,升级");
                    boolean needUpdate = ret != null && TextUtils.isEmpty(ret.version) &&
                            BindUtils.versionCompare(UPGRADE_VERSION, ret.version) > 0
                            && BindUtils.isUcos(ret.uuid);
                    return !needUpdate;
                });
    }


    public Observable<JfgUdpMsg.PingAck> ping(final String cidSuffix) {
        return Observable.create(subscriber -> {
            final Subscription sub = RxBus.getCacheInstance().toObservable(JfgUdpMsg.PingAck.class)
                    .timeout(1, TimeUnit.SECONDS)
                    .filter(pingAck -> pingAck != null && !TextUtils.isEmpty(pingAck.cid) &&
                            pingAck.cid.endsWith(cidSuffix))
                    .timeout(1, TimeUnit.SECONDS)
                    .subscribe(pingAck -> {
                        AppLogger.d(BIND_TAG + "得到ping消息");
                        subscriber.onNext(pingAck);
                        subscriber.onCompleted();
                        //结束本身.
                        subscriptionMap.remove("PingAck");
                    }, throwable -> {
                        subscriber.onError(new RxEvent.HelperBreaker(PING_ERR));
                        subscriptionMap.remove("PingAck");
                    });
            subscriptionMap.add(sub, "PingAck");
            try {
                for (int i = 0; i < 2; i++) {
                    BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                            UdpConstant.PORT,
                            new JfgUdpMsg.Ping().toBytes());
                    BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                            UdpConstant.PORT,
                            new JfgUdpMsg.Ping().toBytes());
                }
            } catch (JfgException e) {
                e.printStackTrace();
            }
        });
    }

    public Observable<UdpConstant.UdpDevicePortrait> fping(final JfgUdpMsg.PingAck pingAck) {
        return Observable.create(subscriber -> {
            final Subscription sub = RxBus.getCacheInstance().toObservable(JfgUdpMsg.FPingAck.class)
                    .timeout(1, TimeUnit.SECONDS)
                    .filter(ret -> pingAck != null && !TextUtils.isEmpty(pingAck.cid) &&
                            TextUtils.equals(pingAck.cid, ret.cid))
                    .timeout(1, TimeUnit.SECONDS)
                    .subscribe(ret -> {
                        AppLogger.d(BIND_TAG + "得到fping消息");
                        UdpConstant.UdpDevicePortrait d = new UdpConstant.UdpDevicePortrait();
                        d.uuid = ret.cid;
                        d.mac = ret.mac;
                        d.version = ret.version;
                        d.net = pingAck.net;
                        subscriber.onNext(d);
                        subscriber.onCompleted();
                        //结束本身.
                        subscriptionMap.remove("FPingAck");
                    }, throwable -> {
                        subscriber.onError(new RxEvent.HelperBreaker(FPING_ERR));
                        subscriptionMap.remove("FPingAck");
                    });
            subscriptionMap.add(sub, "FPingAck");
            try {
                for (int i = 0; i < 2; i++) {
                    BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                            UdpConstant.PORT,
                            new JfgUdpMsg.FPing().toBytes());
                    BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.IP,
                            UdpConstant.PORT,
                            new JfgUdpMsg.FPing().toBytes());
                }
            } catch (JfgException e) {
                e.printStackTrace();
            }
        });
    }
}
