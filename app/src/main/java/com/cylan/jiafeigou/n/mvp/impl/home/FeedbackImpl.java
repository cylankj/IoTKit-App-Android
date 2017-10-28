package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.base.module.FeedbackManager;
import com.cylan.jiafeigou.base.module.IManager;
import com.cylan.jiafeigou.cache.db.module.FeedBackBean;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.FeedBackContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.CacheObject;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 */
public class FeedbackImpl extends AbstractPresenter<FeedBackContract.View>
        implements FeedBackContract.Presenter {

    private IManager<FeedBackBean, FeedbackManager.SubmitFeedbackTask> pushManager;

    public FeedbackImpl(FeedBackContract.View view) {
        super(view);
        pushManager = FeedbackManager.getInstance();
    }

    @Override
    public void start() {
        super.start();
        TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName(HomeMineHelpActivity.class.getSimpleName());
        if (node != null) {
            node.setCacheData(new CacheObject().setCount(0).setObject(null));
        }
        pushManager.getNewList()
                .subscribeOn(Schedulers.io())
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(throwable -> mView.initList(null))
                .subscribe(ret -> getView().initList(ret), AppLogger::e);
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                newListRsp(), sendLogRspSub()
        };
    }

    private Subscription sendLogRspSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SendLogRsp.class)
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> mView.updateItem(ret.bean));
    }

    private Subscription newListRsp() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetFeedBackRsp.class)
                .subscribeOn(Schedulers.io())
                .filter(ret -> mView != null && !ListUtils.isEmpty(ret.newList))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> mView.appendList(ret.newList), AppLogger::e);
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{
                ConnectivityManager.CONNECTIVITY_ACTION
        };
    }

    @Override
    public void onNetworkChanged(Context context, Intent intent) {

    }

    /**
     * '
     * 清空所有会话
     */
    @Override
    public void onClearAllTalk() {
        pushManager.deleteAllCache().subscribeOn(Schedulers.io())
                .subscribe(ret -> AppLogger.d("清空"), AppLogger::e);
    }

    /**
     * 保存到本地数据库
     *
     * @param bean
     */
    @Override
    public void saveIntoDb(FeedBackBean bean) {
        ArrayList<FeedBackBean> list = new ArrayList<>();
        list.add(bean);
        pushManager.saveToCache(list)
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> AppLogger.d("保存"), AppLogger::e);
    }

    /**
     * 获取到用户的头像地址
     */
    public String getUserPhotoUrl() {
        if (RxBus.getCacheInstance().hasStickyEvent(RxEvent.ThirdLoginTab.class)) {
            return PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON);
        }
        JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        return account != null ? account.getPhotoUrl() : "";
    }


    /**
     * 检测是否超过20
     *
     * @return
     */
    @Override
    public boolean checkOver20Min(long lastItemTime) {
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
    public Observable<Boolean> sendFeedBack(FeedBackBean bean) {
        return rx.Observable.just(bean)
                .subscribeOn(Schedulers.io())
                .map(ret -> {
                    JFGAccount account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                    FeedbackManager.SubmitFeedbackTask task = new FeedbackManager.SubmitFeedbackTask(account.getAccount(), bean);
                    pushManager.submitTask(task);
                    return true;
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public void deleteItemFromDb(FeedBackBean bean) {
        ArrayList<FeedBackBean> list = new ArrayList<>();
        list.add(bean);
        pushManager.deleteCache(list)
                .subscribeOn(Schedulers.io())
                .subscribe(ret -> AppLogger.d("删除:" + bean), AppLogger::e);
    }

}
