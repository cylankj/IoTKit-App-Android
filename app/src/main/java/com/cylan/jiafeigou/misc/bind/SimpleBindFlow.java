package com.cylan.jiafeigou.misc.bind;

import android.text.TextUtils;

import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.engine.task.OfflineTaskQueue;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.BindUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.cylan.utils.PackageUtils;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
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

    /**
     * ping fping流程.
     */
    private Subscription pingFPingSub;


    public SimpleBindFlow(IBindResult iBindResult) {
        super(iBindResult);
    }

    @Override
    public void startPingFPing(final String shortUUID) {
        unSubscribe(pingFPingSub);
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
                                && BindUtils.isUcos(udpDevicePortrait.cid);
                        //是否需要升级
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
                        //此时,设备还没恢复连接,需要加入队列
                        int key = ("JfgCmdInsurance.getCmd().bindDevice" + udpDevicePortrait.cid).hashCode();
                        OfflineTaskQueue.getInstance().enqueue(key, new Runnable() {
                            @Override
                            public void run() {
                                AppLogger.i(BIND_TAG + udpDevicePortrait.cid);
                                JfgCmdInsurance.getCmd().bindDevice(udpDevicePortrait.cid, "fxx");
                            }
                        });
                        return null;
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
                        UdpConstant.UpgradeStatus status = RxBus.getDefault().getStickyEvent(UdpConstant.UpgradeStatus.class);
                        AppLogger.i(BIND_TAG + ":升级状态: " + status);
                        return !isDogUpgrading && status != null && status.state != IBindResult.UPGRADE_FAILED;
                    }
                })
                .map(new Func1<Boolean, Object>() {
                    @Override
                    public Object call(Boolean aBoolean) {
                        AppLogger.e("开始启动httpServer升级狗");
                        RxBus.getDefault().postSticky(new UdpConstant.UpgradeStatus(IBindResult.UPGRADING));
                        return null;
                    }
                });
    }

    private void registerUpgradeMonitor() {
        RxBus.getDefault().toObservableSticky(JfgUdpMsg.FPingAck.class)
                .filter(new Func1<JfgUdpMsg.FPingAck, Boolean>() {
                    @Override
                    public Boolean call(JfgUdpMsg.FPingAck fPingAck) {
                        //hit target
                        return isDogUpgrading
                                && TextUtils.equals(fPingAck.cid, devicePortrait.cid)
                                && TextUtils.equals(fPingAck.mac, devicePortrait.mac);
                    }
                })
                .map(new Func1<JfgUdpMsg.FPingAck, Object>() {
                    @Override
                    public Object call(JfgUdpMsg.FPingAck fPingAck) {
                        AppLogger.e("sdk还没支持 upgrade status");
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
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                                UdpConstant.PORT,
                                new JfgUdpMsg.Ping().toBytes());
                        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                                UdpConstant.PORT,
                                new JfgUdpMsg.FPing().toBytes());
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
        AppLogger.i("ConfigApPresenterImpl: " + udpDevicePortrait);
        final String[] serverDetails = PackageUtils.getMetaString(ContextUtils.getContext(),
                "ServerAddress").split(":");
        String address = serverDetails[0];
        int port = Integer.parseInt(serverDetails[1]);
        //设置语言
        JfgUdpMsg.SetLanguage setLanguage = new JfgUdpMsg.SetLanguage(
                udpDevicePortrait.cid,
                udpDevicePortrait.mac,
                JFGRules.getLanguageType(ContextUtils.getContext()));
        //设置服务器
        JfgUdpMsg.SetServer setServer =
                new JfgUdpMsg.SetServer(udpDevicePortrait.cid,
                        udpDevicePortrait.mac,
                        address,
                        port,
                        80);
        AppLogger.i("setServer: " + new Gson().toJson(setServer));
        AppLogger.i("setLanguage: " + new Gson().toJson(setLanguage));
        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                UdpConstant.PORT,
                setServer.toBytes());
        //
        JfgCmdInsurance.getCmd().sendLocalMessage(UdpConstant.IP,
                UdpConstant.PORT,
                setLanguage.toBytes());
    }

    /**
     * 此处的消息来源非常关键{@link com.cylan.jiafeigou.n.engine.GlobalUdpDataSource}
     * emit出来 {@link JfgUdpMsg.PingAck},两者都是异步.
     *
     * @param ssidInDigits
     * @return
     */
    private Observable<JfgUdpMsg.PingAck> pingObservable(final String ssidInDigits) {
        return RxBus.getDefault().toObservable(JfgUdpMsg.PingAck.class)
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
        return RxBus.getDefault().toObservable(JfgUdpMsg.FPingAck.class)
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
    public void sendWifiInfo(String ssid, String pwd, int type) {

    }
}
