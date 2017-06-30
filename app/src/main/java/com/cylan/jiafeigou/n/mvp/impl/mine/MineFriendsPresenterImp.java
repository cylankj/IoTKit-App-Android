package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Pair;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendsPresenterImp extends AbstractPresenter<MineFriendsContract.View> implements MineFriendsContract.Presenter {

    public MineFriendsPresenterImp(MineFriendsContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }


    public boolean checkRequestAvailable(FriendContextItem bean) {
        long oneMonth = 30 * 24 * 60 * 60 * 1000L;
        long current = System.currentTimeMillis();
        boolean isLongTime = String.valueOf(bean.friendRequest.time).length() == String.valueOf(current).length();
        return (current - (isLongTime ? bean.friendRequest.time : bean.friendRequest.time * 1000L)) < oneMonth;
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{
                ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION
        };
    }

    public void removeCache(String account) {
//        Pair<ArrayList<JFGFriendAccount>, ArrayList<JFGFriendRequest>> pair = BaseApplication.getAppComponent().getSourceManager().getFriendsList();
//        if (pair != null && pair.second != null) {
//            for (JFGFriendRequest request : pair.second) {
//                if (request != null && TextUtils.equals(account, request.account)) {
//                    pair.second.remove(request);
//                    break;
//                }
//            }
//        }
//        TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
//        TreeNode node = helper.findTreeNodeByName(MineFriendsFragment.class.getSimpleName());
//        node.setCacheData(new CacheObject().setCount(pair == null || pair.second == null ? 0 : ListUtils.getSize(pair.second))
//                .setObject(pair == null || pair.second == null ? 0 : pair.second));
    }

    @Override
    public void initRequestAndFriendList() {
        Subscription subscribe = Observable.just("initRequestAndFriendList")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .flatMap(cmd -> {
                    BaseApplication.getAppComponent().getCmd().getFriendRequestList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetAddReqList.class).first();
                })
                .flatMap(ret -> {
                    BaseApplication.getAppComponent().getCmd().getFriendList();
                    return RxBus.getCacheInstance().toObservable(RxEvent.GetFriendList.class).first();
                })
                .map(ret -> {
                    JFGSourceManager manager = BaseApplication.getAppComponent().getSourceManager();
                    ArrayList<JFGFriendRequest> friendsReqList = manager.getFriendsReqList();
                    ArrayList<JFGFriendAccount> friendsList = manager.getFriendsList();
                    FriendContextItem contextItem;
                    List<FriendContextItem> requestItems = null;
                    List<FriendContextItem> friendItems = null;
                    if (friendsReqList != null && friendsReqList.size() > 0) {
                        requestItems = new ArrayList<>(friendsReqList.size());
                        for (int i = 0; i < friendsReqList.size(); i++) {
                            contextItem = new FriendContextItem(friendsReqList.get(i));
                            requestItems.add(contextItem);
                        }
                    }
                    if (friendsList != null && friendsList.size() > 0) {
                        friendItems = new ArrayList<>(friendsList.size());
                        for (int i = 0; i < friendsList.size(); i++) {
                            contextItem = new FriendContextItem(friendsList.get(i));
                            friendItems.add(contextItem);
                        }
                    }
                    return new Pair<>(requestItems, friendItems);
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading(R.string.LOADING))
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(result -> {
                    getView().onInitRequestAndFriendList(result.first, result.second);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    @Override
    public void deleteFriendRequest(FriendContextItem item) {
        Subscription subscribe = Observable.just("deleteFriendRequest")
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().delAddFriendMsg(item.friendRequest.account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return cmd;
                })
                .flatMap(o -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteAddReqBack.class)
                        .first(ret -> {
                            if (ret.jfgResult.code == JError.ErrorOK) {
                                // TODO: 2017/6/29 删除数据库中的数据
                            }
                            return true;
                        }))

                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
//                    JFGResult result = ret.jfgResult;
//                    if (result.code == JError.ErrorOK) {
////                        updateReqList(account);
//                    }
                    getView().deleteItemRsp(item, ret == null ? -1 : ret.jfgResult.code);
//                    AppLogger.d("需要更新缓存");
////                    if (viewWeakReference.get() != null)
////                        viewWeakReference.get().deleteItemRsp(account, result.code);
//                    BaseDBHelper dbHelper = (BaseDBHelper) BaseApplication.getAppComponent().getDBHelper();
//                    FriendsReqBeanDao dao = dbHelper.getDaoSession().getFriendsReqBeanDao();
//                    List<FriendsReqBean> list1 = dao.queryBuilder().where(FriendsReqBeanDao.Properties.Account.eq(account)).list();
//                    dao.deleteInTx(list1);
                }, AppLogger::e);

        addSubscription(subscribe);
    }

    @Override
    public void acceptFriendRequest(FriendContextItem item) {
        Subscription subscribe = Observable.just("acceptFriendRequest")
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(cmd -> {
                    boolean pass;
                    if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {//无网络连接
                        pass = false;
                        getView().onNetStateChanged(0);
                    } else if (!(pass = checkRequestAvailable(item))) {
                        getView().onRequestExpired(item);
                    }
                    return pass;
                })
                .observeOn(Schedulers.io())
                .map(cmd -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().consentAddFriend(item.friendRequest.account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                        AppLogger.e(e.getMessage());
                    }
                    return cmd;
                })
                .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.ConsentAddFriendBack.class).first())
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(() -> getView().showLoading(R.string.LOADING))
                .doOnTerminate(() -> getView().hideLoading())
                .subscribe(ret -> {
                    getView().acceptItemRsp(item, ret == null ? -1 : ret.jfgResult.code);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }


    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        if (mView == null || !mView.isAdded()) return;
        Observable.just(NetUtils.getJfgNetType())
                .filter(integer -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> getView().onNetStateChanged(integer), AppLogger::e);
    }


}
