package com.cylan.jiafeigou.support;

import android.content.Context;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.mcxiaoke.packer.helper.PackerNg;

import java.io.IOException;

import rx.Observable;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-3-30.
 */

public class OptionsImpl {
    private static final String TAG = "iDebugOptions";
    private static final String KEY_SERVER = "server";
    private static final String KEY_ROBOT_SERVER = "key_robot_server";
    private static String server;
    private static String robotServer;

    private OptionsImpl() {
    }

    public static void enableCrashHandler(Context context, String dir) {
        Log.d("iDebugOptions", "enableCrashHandler");
        CrashHandler.getInstance().init(context, dir);
    }

    public static void enableStrictMode() {
        Log.d("iDebugOptions", "enableStrictMode");
        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder()).detectDiskReads().detectDiskWrites().detectNetwork().detectAll().penaltyLog().penaltyDialog().build());
    }

    public static void setServer(String server) {
        PreferencesUtils.putString(KEY_SERVER, server);
        OptionsImpl.server = server;
    }

    /**
     * 1.debug环境下,先从配置文件中读取.
     * 2.从渠道配置信息中读取
     * 3.从Manifest中读取
     *
     * @return
     */
    public static String getServer() {
        try {
            if (!TextUtils.isEmpty(OptionsImpl.server)) {
                return OptionsImpl.server.replace("_", ":");
            }
            String server = PreferencesUtils.getString(KEY_SERVER, "");
            if (!TextUtils.isEmpty(server)) {
                return OptionsImpl.server = server.replace("_", ":");
            }
            // com.mcxiaoke.packer.helper.PackerNg
            final String domain = PackerNg.getChannel(ContextUtils.getContext());
            if (!TextUtils.isEmpty(domain)) {
                OptionsImpl.server = domain.trim();
                Log.d(TAG, "get serverFrom ng: " + OptionsImpl.server);
                PreferencesUtils.putString(KEY_SERVER, OptionsImpl.server);
                return OptionsImpl.server.replace("_", ":");
            }
            OptionsImpl.server = server = PackageUtils.getMetaString(ContextUtils.getContext(), "server").trim();
            if (!BuildConfig.DEBUG) {
                return server.replace("_", ":");
            }
            return server.replace("_", ":");
        } catch (Exception e) {
            Log.d("IOException", ":" + e.getLocalizedMessage());
            return "";
        }
    }

    /**
     * 1.debug环境下,先从配置文件中读取.
     * 2.从渠道配置信息中读取
     * 3.从Manifest中读取
     *
     * @return
     */
    public static DpMsgDefine.GetRobotServerRsp getRobotServer(String cid, String vid) {
        BlockingObservable<DpMsgDefine.GetRobotServerRsp> toBlocking = Observable.just("")
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        return Command.getInstance().sendUniservalDataSeq(12, DpUtils.pack(new DpMsgDefine.GetRobotServerReq(cid, vid)));
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return -1L;
                })
                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RxEvent.UniversalDataRsp.class).first(rsp -> rsp.seq == seq))
                .map(rsp -> {
                    try {
                        return DpUtils.unpackData(rsp.data, DpMsgDefine.GetRobotServerRsp.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .toBlocking();
          return toBlocking.first();
    }

    public static String getVKey() {
        String vkey = PackageUtils.getMetaString(ContextUtils.getContext(), "vKey");
        if (!BuildConfig.DEBUG) {
            return vkey;
        }
        return vkey;
    }

    public static String getVid() {
        String vid = PackageUtils.getMetaString(ContextUtils.getContext(), "vId");
        if (!BuildConfig.DEBUG) {
            return vid;
        }
        return vid;
    }

    public static String getServiceKey(String vid) {
        if (TextUtils.equals("0001", vid)) {
            return "qg9vRfvofK9b4xsWh1kHnEb998lQZYwA";
        } else if (TextUtils.equals("0002", vid)) {
            return "PB14hf9peVw6CBoVS7Gs4LcMfcOzWNKR";
        }
        return "PB14hf9peVw6CBoVS7Gs4LcMfcOzWNKR";
    }

    public static String getServiceSeceret(String vid) {
        if (TextUtils.equals("0001", vid)) {
            return "VQ9jvWXe4YhJbhrPepqH7zaRppuUGNSf";
        } else if (TextUtils.equals("0002", vid)) {
            return "B8nxU7u3GUDukGlBbjeidOVmYhtPApn0";
        }
        return "B8nxU7u3GUDukGlBbjeidOVmYhtPApn0";
    }
}