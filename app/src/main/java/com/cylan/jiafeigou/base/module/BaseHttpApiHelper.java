package com.cylan.jiafeigou.base.module;

import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.util.Pair;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.toolsfinal.io.Charsets;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.udpMsgPack.JfgUdpMsg;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/5/8.
 */

public class BaseHttpApiHelper {
    private Map<String, Pair<Retrofit, IHttpApi>> cachedHttpApiMap = new HashMap<>();
    private static final BaseHttpApiHelper instance = new BaseHttpApiHelper();

    public static BaseHttpApiHelper getInstance() {
        return instance;
    }


    private boolean checkConnection() {
        WifiInfo info = NetUtils.getWifiManager(ContextUtils.getContext()).getConnectionInfo();
        return info != null && JFGRules.isCylanDevice(info.getSSID());
    }

    private IHttpApi createHttpApi(String uuid, String baseUrl) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Response proceed = chain.proceed(request);
                    String string = proceed.body().string();
                    Response build = proceed.newBuilder().body(new RealResponseBody(proceed.headers(), new Buffer().writeString(string, Charsets.UTF_8))).build();
                    AppLogger.e("http 请求返回的结果:" + new Gson().toJson(string));
                    return build;
                })
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ImageFileConverterFactory.create())
                .client(client)
                .build();
        IHttpApi httpApi = retrofit.create(IHttpApi.class);
        cachedHttpApiMap.put(uuid, new Pair<>(retrofit, httpApi));
        return httpApi;
    }

    public Observable<IHttpApi> getHttpApi(String uuid) {
        Pair<Retrofit, IHttpApi> apiPair = cachedHttpApiMap.get(uuid);
        if (apiPair != null) {
            return Observable.just(apiPair.second);
        }
        return getBaseUrl(uuid).map(baseUrl -> createHttpApi(uuid, baseUrl));
    }

    public String getBaseUrl(String uuid, String defaultValue) {
        Pair<Retrofit, IHttpApi> apiPair = cachedHttpApiMap.get(uuid);
        return apiPair == null ? defaultValue : apiPair.first.baseUrl().toString();
    }

    public Observable<String> getBaseUrl(String uuid) {
        return Observable.just(checkConnection())
                .observeOn(Schedulers.io())
                .filter(isApMode -> {
                    if (isApMode) {
                        try {
                            AppLogger.d("正在发送 FPing 消息");
                            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                            BaseApplication.getAppComponent().getCmd().sendLocalMessage(UdpConstant.PIP, UdpConstant.PORT, new JfgUdpMsg.FPing().toBytes());
                        } catch (JfgException e) {
                            e.printStackTrace();
                            AppLogger.d("发送FPing 消息出现错误");
                        }
                    }
                    return isApMode;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.LocalUdpMsg.class))
                .map(msg -> {
                    String deviceIp = null;
                    try {
                        JfgUdpMsg.UdpHeader header = DpUtils.unpackData(msg.data, JfgUdpMsg.UdpHeader.class);
                        AppLogger.d("header: " + new Gson().toJson(header));
                        if (header != null && TextUtils.equals(header.cmd, "f_ping_ack")) {
                            JfgUdpMsg.FPingAck pingAck = DpUtils.unpackData(msg.data, JfgUdpMsg.FPingAck.class);
                            AppLogger.d("pingAck: " + new Gson().toJson(pingAck));
                            if (pingAck != null && TextUtils.equals(pingAck.cid, uuid)) {
                                deviceIp = msg.ip;
                                AppLogger.d("获取到设备地址:" + deviceIp);
                            }
                        }
                    } catch (Exception e) {
                        AppLogger.e("err: " + e.getLocalizedMessage());
                    }
                    return "http://" + deviceIp;
                })
                .first();
    }
}
