//package com.cylan.jiafeigou.support.stat;
//
//import android.content.Context;
//import android.os.Handler;
//import android.os.HandlerThread;
//
//import com.cylan.jiafeigou.misc.JFGRules;
//import com.cylan.jiafeigou.support.Security;
//import com.tencent.stat.StatConfig;
//import com.tencent.stat.StatService;
//
//import java.util.Properties;
//
///**
// * Created by hunt on 16-4-5.
// * 新一代产品不需要数据，删了吧。
// */
//@Deprecated
//public class MtaManager {
//
//    private static boolean debug = false;
//    private static boolean isInit;
//
//    public static void init(Context context, boolean debug) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                MtaManager.debug = debug;
//                StatConfig.setDebugEnable(debug);
//                StatConfig.setAppKey(context, Security.getMtaKey(JFGRules.getTrimPackageName()));
//                StatConfig.setInstallChannel(context, JFGRules.getTrimPackageName());
//                isInit = true;
//            }
//        }).initSubscription();
//    }
//
//    /**
//     * onResume and stop must be called .
//     *
//     * @param context
//     */
//    public static void onResume(Context context) {
//        if (!isInit) return;
//        StatService.onResume(context);
//    }
//
//    public static void onPause(Context context) {
//        if (!isInit) return;
//        StatService.onPause(context);
//    }
//
//
//    /**
//     * 统计按钮被点击次数，统计对象：value 按钮
//     *
//     * @param context
//     * @param eventId
//     * @param value
//     */
//    public static void eventClick(final Context context, final String eventId, final String value) {
//        if (debug)
//            return;
//        if (!isInit) return;
//        sendEvent(new Runnable() {
//            @Override
//            public void run() {
//
//                Properties properties = new Properties();
//                properties.setProperty(eventId, value);
//                StatService.trackCustomKVEvent(context, eventId, properties);
//            }
//        });
//    }
//
//    public static void eventClick(Context context, String eventId) {
//        if (debug)
//            return;
//        if (!isInit) return;
//        eventClick(context, eventId, "");
//    }
//
//    public static void customEvent(final Context context, final String eventId, final String value) {
//        if (debug)
//            return;
//        if (!isInit) return;
//        sendEvent(new Runnable() {
//            @Override
//            public void run() {
//                StatService.trackCustomEvent(context, eventId, value);
//            }
//        });
//
//    }
//
//    public static void customEvent(Context context, String eventId) {
//        if (debug)
//            return;
//        if (!isInit) return;
//        customEvent(context, eventId, "");
//    }
//
//    private static Handler handler;
//
//    static {
//        HandlerThread thread = new HandlerThread("custom-mta");
//        thread.initSubscription();
//        handler = new Handler(thread.getLooper());
//    }
//
//    private static void sendEvent(Runnable runnable) {
//        handler.post(runnable);
//    }
//}
