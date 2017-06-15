package com.cylan.jiafeigou.n.mvp.impl.home;

import android.os.Build;
import android.os.Environment;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.module.MineHelpSuggestionBean;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.FeedBackContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.task.FetchFeedbackTask;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ProcessUtils;
import com.cylan.jiafeigou.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/22 11:05
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class FeedBackImpl extends AbstractPresenter<FeedBackContract.View>
        implements FeedBackContract.Presenter {

    private BaseDBHelper helper;
    private JFGAccount userInformation;
    private boolean isOpenLogin;
    private boolean hasSendLog;
    private File outFile;
    private boolean isSending = false;

    public FeedBackImpl(FeedBackContract.View view) {
        super(view);
        helper = (BaseDBHelper) BaseApplication.getAppComponent().getDBHelper();
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName(HomeMineHelpFragment.class.getSimpleName());
        if (node != null) node.setData(0);
        getSystemAutoReply();
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                isOpenLogin(),
                getAccountInfo(),
                sendFeedBackReq(),
                sendLogBack(),
                getSystemAutoReplyCallBack(),
                getBadNetBack()
        };
    }

    private Subscription getBadNetBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.NetConnectionEvent.class)
                .throttleFirst(5, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                    if (!event.isOnLine) {
                        getView().refrshRecycleView(-100);
                    }
                }, AppLogger::e);
    }

    /**
     * 获取到列表的数据
     */
    @Override
    public void initData() {
        //需要合并.
        rx.Observable.just(null)
                .flatMap(new Func1<Object, Observable<ArrayList<MineHelpSuggestionBean>>>() {
                    @Override
                    public Observable<ArrayList<MineHelpSuggestionBean>> call(Object o) {
                        ArrayList<MineHelpSuggestionBean> tempList = new ArrayList<MineHelpSuggestionBean>();
                        if (helper == null) {
                            return Observable.just(tempList);
                        }
                        List<MineHelpSuggestionBean> list = helper.getDaoSession().getMineHelpSuggestionBeanDao().loadAll();
                        if (list != null && list.size() != 0) {
                            tempList.addAll(list);
                            Collections.sort(tempList, new SortComparator());
                        }
                        return Observable.just(tempList);
                    }
                })
                .subscribe(list -> {
                    if (getView() != null) {
                        getView().initRecycleView(list);
                        AppLogger.d("database_size:" + list.size());
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    /**
     * 清空所有会话
     */
    @Override
    public void onClearAllTalk() {
        helper.getDaoSession().getMineHelpSuggestionBeanDao().deleteAll();
        BaseApplication.getAppComponent().getSourceManager().cacheNewFeedbackList(new ArrayList<>());
    }

    /**
     * 拿到数据库对象
     */
    @Override
    public Subscription getAccountInfo() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userInfo -> {
                    if (userInfo != null) {
                        userInformation = userInfo.jfgAccount;
                        helper = (BaseDBHelper) BaseApplication.getAppComponent().getDBHelper();
                        initData();
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    /**
     * 保存到本地数据库
     *
     * @param bean
     */
    @Override
    public void saveIntoDb(MineHelpSuggestionBean bean) {
        helper.getDaoSession().getMineHelpSuggestionBeanDao().save(bean);
    }

    public void update(MineHelpSuggestionBean bean) {
        helper.getDaoSession().getMineHelpSuggestionBeanDao().update(bean);
    }

    /**
     * 获取到用户的头像地址
     */
    @Override
    public String getUserPhotoUrl() {
        if (isOpenLogin) {
            return PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON);
        }
        if (userInformation == null) {
            return "";
        } else {
            return userInformation.getPhotoUrl();
        }
    }

    /**
     * 检测是否超时5分钟
     *
     * @return
     */
    @Override
    public boolean checkOverTime(String time) {
        long lastItemTime = Long.parseLong(time);
        if (System.currentTimeMillis() - lastItemTime > 5 * 60 * 1000) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检测是否超过20s
     *
     * @param time
     * @return
     */
    @Override
    public boolean checkOver20Min(String time) {
        long lastItemTime = Long.parseLong(time);
        if (System.currentTimeMillis() - lastItemTime > 2 * 60 * 1000) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 上传意见反馈
     */
    @Override
    public void sendFeedBack(MineHelpSuggestionBean bean) {
        rx.Observable.just(bean)
                .subscribeOn(Schedulers.newThread())
                .subscribe(bean1 -> {
                    BaseApplication.getAppComponent().getCmd().sendFeedback((Long.parseLong(bean1.getDate())) / 1000, bean1.getText(), !hasSendLog);
                    if (!hasSendLog) {
                        upLoadLogFile(bean1);
                    }
                    hasSendLog = true;//只发送一次

                }, throwable -> {
                    AppLogger.d("sendFeedBack" + throwable.getLocalizedMessage());
                });
    }

    /**
     * 获取系统的自动回复
     */
    @Override
    public void getSystemAutoReply() {
        //用户反馈
        Observable.just(new FetchFeedbackTask())
                .subscribeOn(Schedulers.newThread())
                .subscribe(objectAction1 -> objectAction1.call(""), AppLogger::e);
    }

    /**
     * 获取系统自动回复的回调
     *
     * @return
     */
    @Override
    public Subscription getSystemAutoReplyCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetFeedBackRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getFeedBackRsp -> {
                    if (getFeedBackRsp != null && getView() != null) {
                        ArrayList<JFGFeedbackInfo> list = BaseApplication.getAppComponent().getSourceManager().getNewFeedbackList();
                        int size = ListUtils.getSize(list);
                        for (int i = 0; i < size; i++) {
                            JFGFeedbackInfo info = list.get(i);
                            AppLogger.d("getSystemAuto:" + info.time);
                            AppLogger.d("getSystemAuto2:" + System.currentTimeMillis());
                            getView().addSystemAutoReply(info.time, info.msg);
                        }
                        BaseApplication.getAppComponent().getSourceManager().clearFeedbackList();
                    }
                }, AppLogger::e);
    }

    /**
     * 发送反馈的回调
     *
     * @return
     */
    @Override
    public Subscription sendFeedBackReq() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SendFeekBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sendFeekBack -> {
                    if (sendFeekBack != null && hasSendLog) {
                        getView().refrshRecycleView(sendFeekBack.jfgResult.code);
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    @Override
    public void deleteOnItemFromDb(MineHelpSuggestionBean bean) {
        helper.getDaoSession().getMineHelpSuggestionBeanDao().delete(bean);
    }

    /**
     * 是否三方登录
     *
     * @return
     */
    @Override
    public Subscription isOpenLogin() {
        return RxBus.getCacheInstance().toObservableSticky(Boolean.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> isOpenLogin = aBoolean, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    @Override
    public void upLoadLogFile(MineHelpSuggestionBean bean) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
            return;
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
        outFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + bean.getDate() + JConstant.getRoot() + ".zip");
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        String fileName = (Long.parseLong(bean.getDate())) / 1000 + ".zip";
        String remoteUrl = null;
        try {
            remoteUrl = "/log/" + Security.getVId() + "/" + userInformation.getAccount() + "/" + fileName;
            isSending = true;
            BaseApplication.getAppComponent().getCmd().putFileToCloud(remoteUrl, outFile.getAbsolutePath());
            AppLogger.d("upload log:" + remoteUrl);
        } catch (JfgException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传日志的回调
     */
    @Override
    public Subscription sendLogBack() {
        return RxBus.getCacheInstance().toObservable(JFGMsgHttpResult.class)
                .filter(ret -> isSending)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((JFGMsgHttpResult jfgMsgHttpResult) -> {
                    if (jfgMsgHttpResult != null && jfgMsgHttpResult.ret == 200) {
                        hasSendLog = true;
                        isSending = false;
                        getView().sendLogResult(0);
                        getView().refrshRecycleView(0);
                        deleteLocalLogFile();
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    @Override
    public boolean isSending() {
        return isSending;
    }

    /**
     * 按时间排序
     */
    public class SortComparator implements Comparator<MineHelpSuggestionBean> {
        @Override
        public int compare(MineHelpSuggestionBean lhs, MineHelpSuggestionBean rhs) {
            long oldTime = Long.parseLong(rhs.getDate());
            long newTime = Long.parseLong(lhs.getDate());
            if (oldTime == newTime)
                return 0;
            if (oldTime > newTime)
                return -1;
            return 1;
        }
    }

    /**
     * 删除生成的本地log文件
     */
    private void deleteLocalLogFile() {
        Observable.just("delete")
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    if (outFile != null && outFile.exists()) {
                        boolean delete = outFile.delete();
                        return delete ? 0 : -1;
                    }
                    return 0;
                })
                .subscribe(ret -> {
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

}
