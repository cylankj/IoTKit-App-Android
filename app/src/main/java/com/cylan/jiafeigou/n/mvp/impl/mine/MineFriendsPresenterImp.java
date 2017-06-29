package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.entity.jniCall.JFGResult;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.task.FetchFriendsTask;
import com.cylan.jiafeigou.n.view.adapter.item.FriendGroupChildItem;
import com.cylan.jiafeigou.n.view.adapter.item.FriendGroupParentItem;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.xiaomi.push.service.am.s;

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
//        fetchFriends();
    }

    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                fetchFriendsRspSub()};
    }

    private Subscription fetchFriendsRspSub() {
//        return Observable.just("go")
//                .subscribeOn(Schedulers.newThread())
//                .flatMap(s -> RxBus.getCacheInstance().toObservableSticky(RxEvent.AllFriendsRsp.class))
//                .flatMap(ret -> Observable.just(BaseApplication.getAppComponent().getSourceManager().getFriendsList()))
//                .observeOn(AndroidSchedulers.mainThread())
//                .filter(ret -> ret != null && mView != null && mView.isAdded())
//                .subscribe(ret -> {
//                    mView.initAddReqReqList(sortAddReqList(BaseApplication.getAppComponent().getSourceManager().getFriendsReqList()));
//                    mView.initFriendList(BaseApplication.getAppComponent().getSourceManager().getFriendsList());
//                }, AppLogger::e);
        return null;
    }

    private void fetchFriends() {
        Observable.just(new FetchFriendsTask())
                .subscribeOn(Schedulers.newThread())
                .subscribe(objectAction1 -> objectAction1.call(""), AppLogger::e);
    }

    @Override
    public void stop() {
        super.stop();
    }


    public boolean checkRequestAvailable(FriendGroupChildItem bean) {
        long oneMonth = 30 * 24 * 60 * 60 * 1000L;
        long current = System.currentTimeMillis();
        boolean isLongTime = String.valueOf(bean.friendRequest.time).length() == String.valueOf(current).length();
        return (current - (isLongTime ? bean.friendRequest.time : bean.friendRequest.time * 1000L)) > oneMonth;
    }

    /**
     * 发送添加请求
     */
    @Override
    public void sendAddReq(final String account) {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().addFriend(account, "");
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> AppLogger.e("sendAddReq: " + throwable.getLocalizedMessage()));
    }

    /**
     * 同意添加后SDK的调用
     */
    @Override
    public void acceptAddSDK(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .delay(1, TimeUnit.SECONDS)
                .subscribe(new ConsentAccountTask(MineFriendsPresenterImp.this, mView), AppLogger::e);
    }

    /**
     * 删除选项
     */
    private static class DeleteReqTask implements Action1<String> {

        private WeakReference<MineFriendsContract.Presenter> weakReference;
        private WeakReference<MineFriendsContract.View> viewWeakReference;

        public DeleteReqTask(MineFriendsContract.Presenter contract, MineFriendsContract.View view) {
            this.weakReference = new WeakReference<>(contract);
            this.viewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void call(String account) {
            Observable.just(account)
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(o -> RxBus.getCacheInstance().toObservable(RxEvent.DeleteAddReqBack.class))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ret -> {
                        JFGResult result = ret.jfgResult;
                        if (result.code == JError.ErrorOK) {
                            updateReqList(account);
                        }
                        AppLogger.d("需要更新缓存");
//                        if (viewWeakReference.get() != null)
////                            viewWeakReference.get().deleteItemRsp(account, result.code);
//                            BaseDBHelper dbHelper = (BaseDBHelper) BaseApplication.getAppComponent().getDBHelper();
//                        FriendsReqBeanDao dao = dbHelper.getDaoSession().getFriendsReqBeanDao();
//                        List<FriendsReqBean> list1 = dao.queryBuilder().where(FriendsReqBeanDao.Properties.Account.eq(account)).list();
//                        dao.deleteInTx(list1);
//                        throw new RxEvent.HelperBreaker("结束了");
                    }, AppLogger::e);
            try {
                BaseApplication.getAppComponent().getCmd().delAddFriendMsg(account);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }

    private static void updateReqList(final String account) {
//        ArrayList<FriendsReqBean> list = BaseApplication.getAppComponent().getSourceManager().getFriendsReqList();
//        if (list != null) {
//            for (FriendsReqBean bean : list) {
//                if (bean != null && TextUtils.equals(bean.account, account)) {
//                    list.remove(bean);
//                    break;
//                }
//            }
//        }
//        AppLogger.d("重新刷新列表,走一遍流程,就不需要特殊处理");
//        Observable.just(new FetchFriendsTask())
//                .subscribeOn(Schedulers.newThread())
//                .subscribe(objectAction1 -> objectAction1.call(""), AppLogger::e);
    }

    /**
     * 发送同意
     */
    private static class ConsentAccountTask implements Action1<String> {
        private WeakReference<MineFriendsContract.Presenter> weakReference;
        private WeakReference<MineFriendsContract.View> viewWeakReference;

        public ConsentAccountTask(MineFriendsContract.Presenter contract, MineFriendsContract.View view) {
            this.weakReference = new WeakReference<>(contract);
            this.viewWeakReference = new WeakReference<>(view);
        }

        @Override
        public void call(String s) {
            Observable.just(s)
                    .subscribeOn(Schedulers.newThread())
                    .flatMap(ret -> RxBus.getCacheInstance().toObservable(RxEvent.ConsentAddFriendBack.class)
                            .timeout(30, TimeUnit.SECONDS))
                    .subscribe(ret -> {
                        JFGResult result = ret.jfgResult;
                        if (result.code == JError.ErrorOK) {
                            updateReqList(s);
                            AppLogger.d("刷新列表");
                        }
                        if (viewWeakReference.get() != null && viewWeakReference.get().isAdded()) {
//                            viewWeakReference.get().acceptItemRsp(s, result.code);
                        }
                        throw new RxEvent.HelperBreaker("结束了");
                    }, throwable -> {
                        if (throwable instanceof TimeoutException && viewWeakReference.get() != null && viewWeakReference.get().isAdded()) {
//                            viewWeakReference.get().acceptItemRsp(s, -1);
                        }
                    });
            try {
                BaseApplication.getAppComponent().getCmd().consentAddFriend(s);
            } catch (JfgException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{
                ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION
        };
    }

    /**
     * 删除好友添加请求
     *
     * @param account
     */
    @Override
    public void deleteAddReq(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new DeleteReqTask(MineFriendsPresenterImp.this, mView), AppLogger::e);
    }

    @Override
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
                    List<FriendGroupParentItem> groupChildItems = new ArrayList<>(2);//request ,friends
                    JFGSourceManager manager = BaseApplication.getAppComponent().getSourceManager();
                    ArrayList<JFGFriendRequest> friendsReqList = manager.getFriendsReqList();
                    ArrayList<JFGFriendAccount> friendsList = manager.getFriendsList();
                    FriendGroupParentItem parentItem;
                    FriendGroupChildItem childItem;
                    List<FriendGroupChildItem> childItems;
                    if (friendsReqList != null && friendsReqList.size() > 0) {
                        parentItem = new FriendGroupParentItem();
                        parentItem.withIdentifier(1000);
                        childItems = new ArrayList<>(friendsReqList.size());
                        for (int i = 0; i < friendsReqList.size(); i++) {
                            childItem = new FriendGroupChildItem(friendsReqList.get(i));
                            childItem.withIdentifier(1000 + i);
                            childItems.add(childItem);
                        }
                        parentItem.withSubItems(childItems);
                        parentItem.withTitle(ContextUtils.getContext().getString(R.string.Tap3_FriendsAdd_Request));
                        parentItem.withIsExpanded(true);
                        groupChildItems.add(parentItem);
                    }
                    if (friendsList != null && friendsList.size() > 0) {
                        parentItem = new FriendGroupParentItem();
                        parentItem.withIdentifier(5000);
                        childItems = new ArrayList<>(friendsList.size());
                        for (int i = 0; i < friendsList.size(); i++) {
                            childItem = new FriendGroupChildItem(friendsList.get(i));
                            childItem.withIdentifier(5000 + i);
                            childItems.add(childItem);
                        }
                        parentItem.withSubItems(childItems);
                        parentItem.withTitle(ContextUtils.getContext().getString(R.string.Tap3_FriendsList));
                        parentItem.withIsExpanded(true);
                        groupChildItems.add(parentItem);
                    }
                    return groupChildItems;
                })
                .timeout(30, TimeUnit.SECONDS, Observable.just(null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    getView().onInitRequestAndFriendList(result);
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
        addSubscription(subscribe);
    }

    @Override
    public void deleteFriendRequest(FriendGroupChildItem item) {
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
    public void acceptFriendRequest(FriendGroupChildItem item) {
        Subscription subscribe = Observable.just("acceptFriendRequest")
                .subscribeOn(AndroidSchedulers.mainThread())
                .filter(cmd -> {
                    boolean available = checkRequestAvailable(item);
                    if (!available) {
                        getView().onRequestExpired(item);
                    }
                    return available;
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
                .subscribe(ret -> {
                    JFGResult result = ret.jfgResult;
                    if (result.code == JError.ErrorOK) {
                        updateReqList(s);
                        AppLogger.d("刷新列表");
                    }
                    getView().acceptItemRsp(item, ret == null ? -1 : result.code);
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
