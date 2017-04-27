package com.cylan.jiafeigou.n.mvp.impl.home;

import android.os.Environment;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.entity.jniCall.JFGMsgHttpResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineHelpSuggestionContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.Security;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/22 11:05
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineHelpSuggestionImpl extends AbstractPresenter<HomeMineHelpSuggestionContract.View>
        implements HomeMineHelpSuggestionContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private DbManager dbManager;
    private JFGAccount userInfomation;
    private boolean isOpenLogin;
    private boolean hasSendLog;
    private File outFile;

    public HomeMineHelpSuggestionImpl(HomeMineHelpSuggestionContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(isOpenLogin());
            compositeSubscription.add(getAccountInfo());
            compositeSubscription.add(sendFeedBackReq());
            compositeSubscription.add(sendLogBack());
            compositeSubscription.add(getSystemAutoReplyCallBack());
        }
        getSystemAutoReply();
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(compositeSubscription);
        DataBaseUtil.release();
        RxBus.getCacheInstance().removeStickyEvent(RxEvent.GetFeedBackRsp.class);
    }

    /**
     * 获取到列表的数据
     */
    @Override
    public void initData() {
        rx.Observable.just(null)
                .flatMap(new Func1<Object, Observable<ArrayList<MineHelpSuggestionBean>>>() {
                    @Override
                    public Observable<ArrayList<MineHelpSuggestionBean>> call(Object o) {
                        ArrayList<MineHelpSuggestionBean> tempList = new ArrayList<MineHelpSuggestionBean>();
                        if (dbManager == null) {
                            return Observable.just(tempList);
                        }
                        try {
                            List<MineHelpSuggestionBean> list = dbManager.findAll(MineHelpSuggestionBean.class);
                            if (list != null && list.size() != 0) {
                                tempList.addAll(list);
                                Collections.sort(tempList, new SortComparator());
                            }
                        } catch (DbException e) {
                            e.printStackTrace();
                            return Observable.just(tempList);
                        }
                        return Observable.just(tempList);
                    }
                })
                .subscribe(new Action1<ArrayList<MineHelpSuggestionBean>>() {
                    @Override
                    public void call(ArrayList<MineHelpSuggestionBean> list) {
                        if (getView() != null) {
                            getView().initRecycleView(list);
                            AppLogger.d("database_size:" + list.size());
                        }
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    /**
     * 清空所有会话
     */
    @Override
    public void onClearAllTalk() {
        try {
            dbManager.delete(MineHelpSuggestionBean.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拿到数据库对象
     */
    @Override
    public Subscription getAccountInfo() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo userInfo) {
                        if (userInfo != null) {
                            userInfomation = userInfo.jfgAccount;
                            dbManager = DataBaseUtil.getInstance(userInfo.jfgAccount.getAccount()).dbManager;
                            initData();
                        }
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
        try {
            dbManager.save(bean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取到用户的头像地址
     */
    @Override
    public String getUserPhotoUrl() {
        if (isOpenLogin) {
            return PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON);
        }
        if (userInfomation == null) {
            return "";
        } else {
            return userInfomation.getPhotoUrl();
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
                .subscribe(new Action1<MineHelpSuggestionBean>() {
                    @Override
                    public void call(MineHelpSuggestionBean bean) {
                        BaseApplication.getAppComponent().getCmd().sendFeedback((Long.parseLong(bean.getDate())) / 1000, bean.getText(), !hasSendLog);
                        if (!hasSendLog) {
                            upLoadLogFile(bean);
                        }
                    }
                }, throwable -> {
                    AppLogger.d("sendFeedBack" + throwable.getLocalizedMessage());
                });
    }

    /**
     * 获取系统的自动回复
     */
    @Override
    public void getSystemAutoReply() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        int req = BaseApplication.getAppComponent().getCmd().getFeedbackList();
                        AppLogger.d("getSystemAutoReply:" + req);
                    }
                }, throwable -> {
                    AppLogger.e("getSystemAutoReply" + throwable.getLocalizedMessage());
                });
    }

    /**
     * 获取系统自动回复的回调
     *
     * @return
     */
    @Override
    public Subscription getSystemAutoReplyCallBack() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetFeedBackRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetFeedBackRsp>() {
                    @Override
                    public void call(RxEvent.GetFeedBackRsp getFeedBackRsp) {
                        if (getFeedBackRsp != null) {
                            if (getView() != null && getFeedBackRsp.arrayList.size() != 0) {
                                JFGFeedbackInfo jfgFeedbackInfo = getFeedBackRsp.arrayList.get(0);
                                AppLogger.d("getSystemAuto:" + jfgFeedbackInfo.time);
                                AppLogger.d("getSystemAuto2:" + System.currentTimeMillis());
                                getView().addSystemAutoReply(jfgFeedbackInfo.time, jfgFeedbackInfo.msg);
                            }
                        }
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
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
        try {
            dbManager.delete(bean);
        } catch (DbException e) {
            e.printStackTrace();
        }
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
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        isOpenLogin = aBoolean;
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }

    @Override
    public void upLoadLogFile(MineHelpSuggestionBean bean) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
            return;
        }
        File logFile = new File(JConstant.LOG_PATH + "/log.txt");
        File smartcall_t = new File(JConstant.LOG_PATH + "/smartCall_t.txt");
        File smartcall_w = new File(JConstant.LOG_PATH + "/smartCall_w.txt");
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
            remoteUrl = "/log/" + Security.getVId() + "/" + userInfomation.getAccount() + "/" + fileName;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((JFGMsgHttpResult jfgMsgHttpResult) -> {
                    if (jfgMsgHttpResult != null) {
                        hasSendLog = true;
                        getView().sendLogResult(0);
                        getView().refrshRecycleView(0);
                        deleteLocalLogFile();
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
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
