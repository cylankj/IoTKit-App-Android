package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.NewHomeActivityContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-23.
 */
public class NewHomeActivityPresenterImpl extends AbstractPresenter<NewHomeActivityContract.View>
        implements NewHomeActivityContract.Presenter {

    public NewHomeActivityPresenterImpl(NewHomeActivityContract.View view) {
        super(view);
        view.setPresenter(this);
        view.initView();
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{updateRsp(), mineTabNewInfoRsp()};
    }

    private Subscription updateRsp() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.ApkDownload.class)
                .subscribeOn(Schedulers.io())
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    long time = PreferencesUtils.getLong(JConstant.KEY_CLIENT_NEW_VERSION_DIALOG);
                    boolean force = ret.updateType == RxEvent.UpdateType.GOOGLE_PLAY || (ret.forceUpdate == 1);//强制升级
                    if (force || time == 0 || System.currentTimeMillis() - time > 24 * 3600 * 1000) {
                        PreferencesUtils.putLong(JConstant.KEY_CLIENT_NEW_VERSION_DIALOG, System.currentTimeMillis());
                        mView.needUpdate(ret.updateType, "", ret.filePath, ret.forceUpdate);
                    }
                    if (!force) {
                        RxBus.getCacheInstance().removeStickyEvent(RxEvent.ApkDownload.class);
                    }
                }, throwable -> {
                    AppLogger.e("err:" + MiscUtils.getErr(throwable));
                    RxBus.getCacheInstance().removeStickyEvent(RxEvent.ApkDownload.class);
//                    addSubscription(updateRsp());
                });
    }

    @Override
    public void start() {
        super.start();
        addSubscription(updateRsp());
    }

    private Subscription mineTabNewInfoRsp() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.InfoUpdate.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> mView != null)
                .subscribe(ret -> {
                    TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName("HomeMineFragment");
                    mView.refreshHint(node != null && node.getTraversalCount() > 0);
                }, throwable -> addSubscription(mineTabNewInfoRsp()));
    }
}
