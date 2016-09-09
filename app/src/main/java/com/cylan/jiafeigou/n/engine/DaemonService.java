package com.cylan.jiafeigou.n.engine;

import android.app.IntentService;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.utils.FileUtils;
import com.cylan.utils.HandlerThreadUtils;
import com.cylan.utils.ProcessUtils;

import java.io.File;


public class DaemonService extends Service {

    private static final String TAG = DaemonService.class.getSimpleName();

    /**
     * Creates an Service.  Invoked by your subclass's constructor.
     * <p/>
     * name Used to name the worker thread, important only for debugging.
     */
    public DaemonService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CopyDaemonFile();
        registerBootComplete();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isServiceRunning()) {
            startService(new Intent(this, DataSourceService.class));
            try2startForeground();
            AppLogger.i(TAG + "re start data service");
        }
        return START_STICKY;
    }

    private boolean isServiceRunning() {
        return ProcessUtils.isServiceRunning(this, DataSourceService.class);
    }

    /**
     * 注册启动监听广播
     */
    private void registerBootComplete() {
        try {
            BootCompletedReceiver receiver = new BootCompletedReceiver();
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
            registerReceiver(receiver, intentFilter);
            Log.d(TAG, "bootComplete");
        } catch (Exception e) {
            Log.d(TAG, "bootComplete: e: " + e.toString());
        }
    }

    /**
     * a simple
     */
    private void CopyDaemonFile() {
        HandlerThreadUtils.post(new Runnable() {
            @Override
            public void run() {
                AppLogger.d("CopyDaemonFile");
                onHandleIntent();
            }
        });
    }

    protected void onHandleIntent() {
        try {
            final String daemonServiceName = this.getClass().getName();
            final String filename = "daemon_c";
            final String daemonPath = getFilesDir() + File.separator + filename;
            final File destFile = new File(daemonPath);
            FileUtils.copyAssetsFile(this, destFile, "daemon_c");
            boolean set = new File(daemonPath).setExecutable(true);
            final String packageName = getPackageName();
            final String processName = ProcessUtils.myProcessName(this) + ":push";
            final String logPath = PathGetter.createPath(JConstant.DAEMON_DIR);
            new ProcessBuilder().command(daemonPath,
                    packageName,
                    processName,
                    daemonServiceName,
                    BuildConfig.DEBUG ? "1" : "0", logPath)
                    .start();
            Log.d(TAG, "daemonPath: " + daemonPath);
            Log.d(TAG, "packageName: " + packageName);
            Log.d(TAG, "processName: " + processName);
            Log.d(TAG, "logPath: " + logPath);
        } catch (Exception e) {
            Log.d(TAG, "err: " + e.toString());
        }
    }

    /**
     * 尝试提升优先级
     */
    private void try2startForeground() {
        if (Build.VERSION.SDK_INT >= 18) {
            //start an inner service
            startForeground(SERVICE_ID, sendEmptyNotification(this));
            startService(new Intent(this, InnerAssistService.class));
        } else {
            startForeground(SERVICE_ID, new Notification());
        }
    }

    private static final int SERVICE_ID = 11111;

    private static Notification sendEmptyNotification(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setAutoCancel(false);
        return mBuilder.build();
    }

    //此作用基于 微信保活意见
    public static class InnerAssistService extends IntentService {
        public InnerAssistService() {
            this("");
        }

        /**
         * Creates an IntentService.  Invoked by your subclass's constructor.
         *
         * @param name Used to name the worker thread, important only for debugging.
         */
        public InnerAssistService(String name) {
            super(name);
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            startForeground(SERVICE_ID, sendEmptyNotification(this));
        }
    }

    public static class BootCompletedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(new Intent(context, DaemonService.class));
        }
    }

}
