package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.view.adapter.item.ShareFriendItem;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToFriendPresenterImp extends AbstractPresenter<MineShareToFriendContract.View>
        implements MineShareToFriendContract.Presenter {

    public MineShareToFriendPresenterImp(MineShareToFriendContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 发送分享给亲友请求
     */
    @Override
    public void shareDeviceToFriend(final String cid, ArrayList<ShareFriendItem> friendItems) {
        Subscription subscribe = Observable.just("shareDeviceToFriend")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(ret -> {
                    try {
                        String[] accounts = new String[friendItems.size()];
                        for (int i = 0; i < friendItems.size(); i++) {
                            accounts[i] = friendItems.get(i).friendAccount.account;
                        }
                        AppLogger.d("shareDeviceToFriend: cid:" + cid + accounts[0]);
                        BaseApplication.getAppComponent().getCmd().multiShareDevices(new String[]{cid}, accounts);
                        BaseApplication.getAppComponent().getCmd().multiShareDevices(accounts, new String[]{cid});
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return ret;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.MultiShareDeviceEvent.class)
                        .first(rsp -> {
                            if (rsp.ret == 0) {
                                JFGShareListInfo result = null;
                                for (JFGShareListInfo listInfo : DataSourceManager.getInstance().getShareList()) {
                                    if (TextUtils.equals(listInfo.cid, cid)) {
                                        result = listInfo;
                                        break;
                                    }
                                }
                                if (result != null) {
                                    for (ShareFriendItem item : friendItems) {
                                        result.friends.add(item.friendAccount);
                                    }
                                }
                            }
                            return true;
                        }))
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading(R.string.LOADING))
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(result -> {
                    getView().showShareToFriendsResult(result);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    @Override
    public void getCanShareFriendsList(String uuid) {
        Subscription subscribe = Observable.just("getCanShareFriendsList")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(ret -> {
                    try {
                        AppLogger.d("getCanShareFriendsList:" + uuid);
                        BaseApplication.getAppComponent().getCmd().getUnShareListByCid(uuid);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return ret;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.UnShareListByCidEvent.class).first(rsp -> rsp.i == 0))
                .map(ret -> {
                    ArrayList<ShareFriendItem> result = new ArrayList<>(ret.arrayList.size());
                    for (JFGFriendAccount account : ret.arrayList) {
                        result.add(new ShareFriendItem(account));
                    }
                    return result;
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading(R.string.LOADING))
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(result -> {
                    getView().onInitCanShareFriendList(result);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }
}
