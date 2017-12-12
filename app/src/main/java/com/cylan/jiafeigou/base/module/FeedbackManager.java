package com.cylan.jiafeigou.base.module;

import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.module.FeedBackBean;
import com.cylan.jiafeigou.cache.db.module.FeedBackBeanDao;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.badge.CacheObject;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.ProcessUtils;
import com.cylan.jiafeigou.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by hds on 17-7-3.
 */

public class FeedbackManager implements IManager<FeedBackBean, FeedbackManager.SubmitFeedbackTask> {
    private static FeedbackManager instance;

    private FeedbackManager() {
    }

    private HashMap<Long, SubmitFeedbackTask> submitTaskMap = new HashMap<>();

    public static FeedbackManager getInstance() {
        if (instance == null) {
            instance = new FeedbackManager();
        }
        return instance;
    }


    public void cachePush(ArrayList<JFGFeedbackInfo> arrayList) {
        saveCache(arrayList);
    }

    private void saveCache(ArrayList<JFGFeedbackInfo> arrayList) {
        if (arrayList == null) {
            return;
        }
        JFGAccount account = DataSourceManager.getInstance().getJFGAccount();
        if (account == null || TextUtils.isEmpty(account.getAccount())) {
            return;
        }
        ArrayList<FeedBackBean> feedBackBeans = new ArrayList<>();
        for (JFGFeedbackInfo info : arrayList) {
            FeedBackBean bean = new FeedBackBean();
            bean.setContent(info.msg);
            bean.setMsgTime(info.time * 1000);
            bean.setAccount(account.getAccount());
            feedBackBeans.add(bean);
        }
        TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName("HomeMineHelpActivity");
        if (node == null) {
            node = new TreeNode();
        }
        CacheObject object = node.getCacheData();
        if (object != null && object.getObject() != null && object.getObject() instanceof List) {
            List<FeedBackBean> beanList = (List<FeedBackBean>) object.getObject();
            beanList.addAll(feedBackBeans);
            object.setCount(ListUtils.getSize(beanList));
        } else {
            object = new CacheObject();
            object.setCount(ListUtils.getSize(feedBackBeans));
            object.setObject(feedBackBeans);
        }
        node.setCacheData(object);
        saveToCache(feedBackBeans)
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> AppLogger.d("反馈已经存档"), AppLogger::e);
        //收到,并且存档.
        RxBus.getCacheInstance().post(new RxEvent.GetFeedBackRsp(feedBackBeans));
        RxBus.getCacheInstance().postSticky(new RxEvent.InfoUpdate());
    }

    @Override
    public Observable<List<FeedBackBean>> getNewList() {
        boolean isOnline = DataSourceManager.getInstance().isOnline();
        if (isOnline) {
            Command.getInstance().getFeedbackList();
        }
        return loadFromLocal();
    }

    private Observable<List<FeedBackBean>> loadFromLocal() {
        BaseDBHelper helper = BaseDBHelper.getInstance();
        JFGAccount account = DataSourceManager.getInstance().getJFGAccount();
        if (account == null || TextUtils.isEmpty(account.getAccount())) {
            return Observable.just(new ArrayList<>());
        }
        return helper.getDaoSession().getFeedBackBeanDao()
                .queryBuilder()
                .where(FeedBackBeanDao.Properties.Account.eq(account.getAccount()))
                .orderDesc(FeedBackBeanDao.Properties.MsgTime)
                .rx().list()
                .flatMap(feedBackBeans -> {
                    //排序过
                    if (feedBackBeans != null) {
                        ArrayList<FeedBackBean> list = new ArrayList<>(new TreeSet<>(new HashSet<>(feedBackBeans)));
                        Collections.sort(list);
                        return Observable.just(list);
                    }
                    return Observable.just(null);
                });
    }

    @Override
    public Observable<Iterable<FeedBackBean>> saveToCache(List<FeedBackBean> arrayList) {
        BaseDBHelper helper = BaseDBHelper.getInstance();
        return helper.getDaoSession().getFeedBackBeanDao().rx().saveInTx(arrayList)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<Void> deleteCache(List<FeedBackBean> arrayList) {
        BaseDBHelper helper = BaseDBHelper.getInstance();
        return helper.getDaoSession().getFeedBackBeanDao().rx().deleteInTx(arrayList);
    }

    @Override
    public Observable<Void> deleteAllCache() {
        BaseDBHelper helper = BaseDBHelper.getInstance();
        return helper.getDaoSession().getFeedBackBeanDao().rx().deleteAll();
    }

    @Override
    public SubmitFeedbackTask getTask(long key) {
        return submitTaskMap.get(key);
    }

    @Override
    public void submitTask(SubmitFeedbackTask submitTask) {
        this.submitTaskMap.put(submitTask.backBean.getMsgTime(), submitTask);
        AppLogger.d("需要执行这个task");
        submitTask.runTask();
    }

    public static final int TASK_STATE_IDLE = 0;
    public static final int TASK_STATE_STARTED = 1;
    public static final int TASK_STATE_SUCCESS = 2;
    public static final int TASK_STATE_FAILED = 3;

    private static long lastSubmitLogTime;

    public static class SubmitFeedbackTask {

        private int taskState;
        private String account;
        private FeedBackBean backBean;
        private Subscription subscription;
        private FinalClean finalClean;

        public SubmitFeedbackTask(String account, FeedBackBean backBean) {
            this.account = account;
            this.backBean = backBean;
        }

        public FeedBackBean getBackBean() {
            return backBean;
        }

        public void setTaskState(int taskState) {
            this.taskState = taskState;
        }

        public int getTaskState() {
            return taskState;
        }

        public void runTask() {
            boolean hasLog = false;
            //失败重传
            if (taskState == TASK_STATE_FAILED) {
                lastSubmitLogTime = 0;
            }
            if (lastSubmitLogTime == 0 || System.currentTimeMillis() - lastSubmitLogTime > 5 * 60 * 1000) {
                lastSubmitLogTime = System.currentTimeMillis();
                hasLog = true;
                sendLog();
            } else {
                taskState = TASK_STATE_SUCCESS;
            }
            Command.getInstance().sendFeedback(backBean.getMsgTime() / 1000, backBean.getContent(), hasLog);
        }

        private void sendLog() {
            taskState = TASK_STATE_STARTED;
            int req = upLoadLogFile();
            AppLogger.d("准备上传日志:" + req);
            if (req == -1) {
                AppLogger.e("上传日志失败:");
                taskState = TASK_STATE_FAILED;
                return;
            }
            subscription = RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class)
                    .subscribeOn(Schedulers.io())
                    .timeout(2, TimeUnit.MINUTES)
                    .filter(ret -> ret.requestId == req)
                    .first()
                    .doOnError(throwable -> {
                        taskState = TASK_STATE_FAILED;
                        RxBus.getCacheInstance().post(new RxEvent.SendLogRsp().setTime(backBean));
                        AppLogger.d("发送日志失败");
                    })
                    .subscribe(jfgMsgHttpResult -> {
                        if (jfgMsgHttpResult.ret == 200) {
                            taskState = TASK_STATE_SUCCESS;
                            cleanLocalFile();
                        } else {
                            taskState = TASK_STATE_FAILED;
                            if (jfgMsgHttpResult.ret == 500)//重试
                            {
                                if (subscription != null) {
                                    subscription.unsubscribe();
                                }
                                lastSubmitLogTime = 0;
                                runTask();
                            }
                        }
                        AppLogger.d("发送日志成功? " + jfgMsgHttpResult.ret);
                        RxBus.getCacheInstance().post(new RxEvent.SendLogRsp().setTime(backBean));
                    }, AppLogger::e);
        }

        public int upLoadLogFile() {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
                return -1;
            }
            AppLogger.d(PackageUtils.getAppVersionName(ContextUtils.getContext()));
            AppLogger.d("" + PackageUtils.getAppVersionCode(ContextUtils.getContext()));
            AppLogger.d(ProcessUtils.myProcessName(ContextUtils.getContext()));
            AppLogger.d(Build.DISPLAY);
            AppLogger.d(Build.MODEL);
            AppLogger.d(Build.VERSION.SDK_INT + " " + Build.VERSION.RELEASE);
            File logFile = new File(JConstant.WORKER_PATH + "/log.txt");
            File smartcall_t = new File(JConstant.WORKER_PATH + "/smartCall_t.txt");
            File smartcall_w = new File(JConstant.WORKER_PATH + "/smartCall_w.txt");
            File crashFile = new File(JConstant.CRASH_PATH);
            File outFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + System.currentTimeMillis() / 1000 + JConstant.getRoot() + ".zip");
            finalClean = new FinalClean();
            finalClean.zipFile = outFile;
            try {
                Collection<File> files = new ArrayList<>();
                files.add(logFile);
                files.add(smartcall_t);
                files.add(smartcall_w);
                if (crashFile.exists()) {
                    File[] file = crashFile.listFiles();
                    if (file.length <= 10) {
                        files.add(crashFile);
                    } else {
                        for (int i = file.length - 1; i > file.length - 11; i--) {
                            files.add(file[i]);
                        }
                    }
                }
                ZipUtils.zipFiles(files, outFile);
                finalClean.localOldFiles = new ArrayList<>(files);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                final String fileName = backBean.getMsgTime() / 1000 + ".zip";
                final String remoteUrl = "/log/" + Security.getVId() + "/" + account + "/" + fileName;
                AppLogger.d("upload log:" + remoteUrl);
                return Command.getInstance().putFileToCloud(remoteUrl, outFile.getAbsolutePath());
            } catch (JfgException e) {
                return -1;
            }
        }

        /**
         * 删除生成的本地log文件
         */
        private void cleanLocalFile() {
            if (finalClean != null) {
                Observable.just("clean")
                        .subscribeOn(Schedulers.io())
                        .subscribe(ret -> {
                            if (finalClean.zipFile != null) {
                                FileUtils.deleteAbsoluteFile(finalClean.zipFile.getAbsolutePath());
                            }
                            if (finalClean.localOldFiles != null) {
                                AppLogger.d("清理 日志");
                                AppLogger.permissionGranted = false;
                                String path = BaseApplication.getAppComponent().getLogPath();
                                if (BuildConfig.DEBUG) {
                                    path += "|logcat";
                                }
                                try {
                                    Command.getInstance().enableLog(false, path);
                                } catch (Exception e) {
                                }
                                for (File file : finalClean.localOldFiles) {
                                    FileUtils.deleteAbsoluteFile(file.getAbsolutePath());
                                }
                                AppLogger.permissionGranted = true;
                                try {
                                    Command.getInstance().enableLog(true, path);
                                } catch (Exception e) {
                                }
                            }
                        }, AppLogger::e);
            }
        }

        private static class FinalClean {
            private File zipFile;
            private List<File> localOldFiles;
        }
    }
}
