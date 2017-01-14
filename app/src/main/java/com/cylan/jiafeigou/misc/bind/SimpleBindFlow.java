package com.cylan.jiafeigou.misc.bind;

import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.engine.task.OfflineTaskQueue;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.cylan.utils.PackageUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
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
        unSubscribe(pingFPingSub);
        //空
        setDevicePortrait(null);
        //zip用法,合并,这里使用了timeout,也就是说,次subscription的生命周期只有1s
        pingFPingSub = Observable.zip(pingObservable(shortUUID),
                fPingObservable(shortUUID),
                new Func2<JfgUdpMsg.PingAck, JfgUdpMsg.FPingAck, UdpConstant.UdpDevicePortrait>() {
                    @Override
                    public UdpConstant.UdpDevicePortrait call(JfgUdpMsg.PingAck pingAck, JfgUdpMsg.FPingAck fPingAck) {
                        //此处完成了第1和第2步.
                        UdpConstant.UdpDevicePortrait d = BindUtils.assemble(pingAck, fPingAck);
                        setDevicePortrait(d);
                        AppLogger.i(BIND_TAG + d);
                        return d;
                    }
                })
                .subscribeOn(Schedulers.newThread())
                //是否需要升级
                .filter(new Func1<UdpConstant.UdpDevicePortrait, Boolean>() {
                    @Override
                    public Boolean call(UdpConstant.UdpDevicePortrait udpDevicePortrait) {
                        boolean needUpdate = BindUtils.versionCompare(UPGRADE_VERSION, udpDevicePortrait.version) > 0
                                && BindUtils.isUcos(udpDevicePortrait.uuid);
                        //是否需要升级
                        if (needUpdate)
                            iBindResult.needToUpgrade();
                        return !needUpdate;
                    }
                })
                .map(new Func1<UdpConstant.UdpDevicePortrait, UdpConstant.UdpDevicePortrait>() {
                    @Override
                    public UdpConstant.UdpDevicePortrait call(final UdpConstant.UdpDevicePortrait udpDevicePortrait) {
                        if (udpDevicePortrait.net == 2) {
                            iBindResult.isMobileNet();
                        }
                        setServerLanguage(udpDevicePortrait);
                        return udpDevicePortrait;
                    }
                })
                //1s内
                .timeout(1000, TimeUnit.MILLISECONDS, timeoutException())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
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
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        //如果在升级,就不继续
                        UdpConstant.UpgradeStatus status = RxBus.getCacheInstance().getStickyEvent(UdpConstant.UpgradeStatus.class);
                        AppLogger.i(BIND_TAG + ":升级状态: " + status);
                        return !isDogUpgrading && status != null && status.state != IBindResult.UPGRADE_FAILED;
                    }
                })
                .map(new Func1<Boolean, Object>() {
                    @Override
                    public Object call(Boolean aBoolean) {
                        AppLogger.e(BIND_TAG + "开始启动httpServer升级狗");
                        RxBus.getCacheInstance().postSticky(new UdpConstant.UpgradeStatus(IBindResult.UPGRADING));
                        return null;
                    }
                });
    }

    private void registerUpgradeMonitor() {
        RxBus.getCacheInstance().toObservableSticky(JfgUdpMsg.FPingAck.class)
                .filter(new Func1<JfgUdpMsg.FPingAck, Boolean>() {
                    @Override
                    public Boolean call(JfgUdpMsg.FPingAck fPingAck) {
                        //hit target
                        return isDogUpgrading
                                && TextUtils.equals(fPingAck.cid, devicePortrait.uuid)
                                && TextUtils.equals(fPingAck.mac, devicePortrait.mac);
                    }
                })
                .map(new Func1<JfgUdpMsg.FPingAck, Object>() {
                    @Override
                    public Object call(JfgUdpMsg.FPingAck fPingAck) {
                        AppLogger.e(BIND_TAG + "sdk还没支持 upgrade status");
                        return null;
                    }
                });
    }

    /**
     * 发送,三次
     */
    private void sendPingFPing() {
        Observable.just(1, 2)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        try {
                            JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                                    UdpConstant.PORT,
                                    new JfgUdpMsg.Ping().toBytes());
                            JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                                    UdpConstant.PORT,
                                    new JfgUdpMsg.FPing().toBytes());
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }

                        AppLogger.i(BIND_TAG + integer);
                    }
                });
    }

    /**
     * 强势使用{@link Observable#timeout(long, TimeUnit, Observable)}
     *
     * @return
     */
    private Observable<UdpConstant.UdpDevicePortrait> timeoutException() {
        return Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        //没有设备画像
                        return devicePortrait == null;
                    }
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
    private void setServerLanguage(UdpConstant.UdpDevicePortrait udpDevicePortrait) {
        AppLogger.i(BIND_TAG + udpDevicePortrait);
        final String[] serverDetails = PackageUtils.getMetaString(ContextUtils.getContext(),
                "ServerAddress").split(":");
        String address = serverDetails[0];
        int port = Integer.parseInt(serverDetails[1]);
        //设置语言
        JfgUdpMsg.SetLanguage setLanguage = new JfgUdpMsg.SetLanguage(
                udpDevicePortrait.uuid,
                udpDevicePortrait.mac,
                JFGRules.getLanguageType(ContextUtils.getContext()));
        //设置服务器
        JfgUdpMsg.SetServer setServer =
                new JfgUdpMsg.SetServer(udpDevicePortrait.uuid,
                        udpDevicePortrait.mac,
                        address,
                        port,
                        80);
        AppLogger.i(BIND_TAG + "setServer: " + new Gson().toJson(setServer));
        AppLogger.i(BIND_TAG + "setLanguage: " + new Gson().toJson(setLanguage));
        //增加绑定随机数.
        bindCode = GlobalDataProxy.getInstance().getJfgAccount().getAccount() + System.currentTimeMillis();
        JfgUdpMsg.FBindDeviceCode code = new JfgUdpMsg.FBindDeviceCode(
                udpDevicePortrait.uuid, udpDevicePortrait.mac, bindCode);
        try {
            JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP, UdpConstant.PORT, code.toBytes());
        } catch (JfgException e) {
            AppLogger.e("e: " + e.getLocalizedMessage());
        }
        try {
            JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                    UdpConstant.PORT,
                    setServer.toBytes());
            //
            JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                    UdpConstant.PORT,
                    setLanguage.toBytes());
        } catch (JfgException e) {
            e.printStackTrace();
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
                .filter(new Func1<JfgUdpMsg.PingAck, Boolean>() {
                    @Override
                    public Boolean call(JfgUdpMsg.PingAck pingAck) {
                        //注意条件
                        return !TextUtils.isEmpty(pingAck.cid)
                                && pingAck.cid.endsWith(ssidInDigits);
                    }
                })
                .throttleFirst(100, TimeUnit.MILLISECONDS);
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
                .filter(new Func1<JfgUdpMsg.FPingAck, Boolean>() {
                    @Override
                    public Boolean call(JfgUdpMsg.FPingAck pingAck) {
                        //注意条件
                        return !TextUtils.isEmpty(pingAck.cid)
                                && pingAck.cid.endsWith(ssidInDigits);
                    }
                })
                .throttleFirst(100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void sendServerInfo(String ip, int host) {

    }


    @Override
    public void sendLanguageInfo() {

    }

    @Override
    public void sendWifiInfo(final String ssid, final String pwd, final int type) {
        AppLogger.i("sendWifiInfo:");
        Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        JfgUdpMsg.DoSetWifi setWifi = new JfgUdpMsg.DoSetWifi(devicePortrait.uuid,
                                devicePortrait.mac,
                                ssid, pwd);
                        setWifi.security = type;
                        //发送wifi配置
                        try {
                            JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                                    UdpConstant.PORT,
                                    setWifi.toBytes());
                            AppLogger.i(TAG + new Gson().toJson(setWifi));
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }


                        //此时,设备还没恢复连接,需要加入队列
                        int key = ("JfgCmdInsurance.getCmd().bindDevice" + devicePortrait.uuid).hashCode();
                        OfflineTaskQueue.getInstance().enqueue(key, new Runnable() {
                            private String cid = devicePortrait.uuid;

                            @Override
                            public void run() {
                                AppLogger.i(BIND_TAG + cid);
                                Log.d("run", "run: ");
                                try {
                                    JfgCmdInsurance.getCmd().bindDevice(cid, bindCode);
                                } catch (JfgException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        return null;
                    }
                })
                .delay(500, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        //恢复wifi
                        iBindResult.onLocalFlowFinish();
                        AppLogger.i(BIND_TAG + "onLocalFlowFinish");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e(BIND_TAG + throwable.getLocalizedMessage());
                    }
                });

    }
}
