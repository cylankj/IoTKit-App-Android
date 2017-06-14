package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendsPresenterImp extends AbstractPresenter<MineFriendsContract.View> implements MineFriendsContract.Presenter {

    private boolean addReqNull;
    private boolean friendListNull;
    private String account;

    public MineFriendsPresenterImp(MineFriendsContract.View view) {
        super(view);
        view.setPresenter(this);
        account = BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount();
    }


    @Override
    protected Subscription[] register() {
        return new Subscription[]{
                getAddRequest(),
                getFriendList(),
                initAddReqRecyListData(),
                initFriendRecyListData(),
                deleteAddReqBack()};
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public ArrayList<MineAddReqBean> initAddRequestData(RxEvent.GetAddReqList addReqList) {
        ArrayList<MineAddReqBean> list = new ArrayList<MineAddReqBean>();
        for (JFGFriendRequest jfgFriendRequest : addReqList.arrayList) {
            MineAddReqBean emMessage = new MineAddReqBean();
            emMessage.alias = jfgFriendRequest.alias;
            emMessage.sayHi = jfgFriendRequest.sayHi;
            emMessage.account = jfgFriendRequest.account;
            emMessage.time = jfgFriendRequest.time;
            try {
                int type = BaseApplication.getAppComponent().getSourceManager().getStorageType();
                emMessage.iconUrl = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(type, String.format(Locale.getDefault(), "/image/%s.jpg", jfgFriendRequest.account));
            } catch (JfgException e) {
                e.printStackTrace();
            }
            list.add(emMessage);
        }
        sortAddReqList(list);
        return list;
    }

    @Override
    public ArrayList<FriendBean> initRelFriendsData(RxEvent.GetFriendList friendList) {
        clearAll();
        ArrayList<FriendBean> list = new ArrayList<FriendBean>();
        for (JFGFriendAccount account : friendList.arrayList) {
            FriendBean emMessage = new FriendBean();
            emMessage.markName = account.markName;
            emMessage.account = account.account;
            emMessage.alias = account.alias;
            try {
                int type = BaseApplication.getAppComponent().getSourceManager().getStorageType();
                emMessage.iconUrl = BaseApplication.getAppComponent().getCmd().getSignedCloudUrl(type, String.format(Locale.getDefault(), "/image/%s.jpg", account.account));
            } catch (Exception e) {
                e.printStackTrace();
            }
            list.add(emMessage);
            saveInDb(emMessage);
        }
        return list;
    }

    @Override
    public boolean checkAddRequestOutTime(MineAddReqBean bean) {
        long oneMonth = 30 * 24 * 60 * 60 * 1000L;
        long current = System.currentTimeMillis();
        //怀疑 bean.time是秒
        boolean isLongTime = String.valueOf(bean.time).length() == String.valueOf(current).length();
        return (current - (isLongTime ? bean.time : bean.time * 1000L)) > oneMonth;
    }

    /**
     * desc：添加请求集合的排序
     *
     * @param list
     * @return
     */
    public ArrayList<MineAddReqBean> sortAddReqList(ArrayList<MineAddReqBean> list) {
        Comparator<MineAddReqBean> comparator = (lhs, rhs) -> {
            long oldTime = Long.parseLong(rhs.time + "");
            long newTime = Long.parseLong(lhs.time + "");
            return (int) (newTime - oldTime);
        };
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * desc：初始化好友列表的数据
     */
    @Override
    public Subscription initFriendRecyListData() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetFriendList.class)
                .flatMap(new Func1<RxEvent.GetFriendList, Observable<ArrayList<FriendBean>>>() {
                    @Override
                    public Observable<ArrayList<FriendBean>> call(RxEvent.GetFriendList getFriendList) {
                        if (getFriendList != null) {
                            return Observable.just(initRelFriendsData(getFriendList));
                        } else {
                            return Observable.just(null);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list != null && list.size() != 0) {
                        handleInitFriendListDataResult(list);
                    } else {
                        friendListNull = true;
                        checkAllNull();
                        getView().hideFriendListTitle();
                        getView().initFriendRecyList(new ArrayList<FriendBean>());
                    }
                }, AppLogger::e);
    }

    /**
     * desc：初始化添加请求列表的数据
     */
    @Override
    public Subscription initAddReqRecyListData() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetAddReqList.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleInitAddReqListDataResult, AppLogger::e);
    }

    @Override
    public void checkAllNull() {
        if (addReqNull && friendListNull) {
            if (getView() != null) {
                getView().hideLoadingDialog();
                getView().hideAddReqListTitle();
                getView().hideFriendListTitle();
                getView().showNullView();
            }
        }
    }

    /**
     * 启动获取添加请求的SDK
     *
     * @return
     */
    @Override
    public Subscription getAddRequest() {
        return rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> BaseApplication.getAppComponent().getCmd().getFriendRequestList(), throwable -> {
                    AppLogger.e("getAddRequest: " + throwable.getLocalizedMessage());
                });
    }

    /**
     * 启动获取好友列表的SDK
     *
     * @return
     */
    @Override
    public Subscription getFriendList() {
        return Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(o -> {
                    if (NetUtils.getNetType(getView().getContext()) == -1) {
                        return getAllFromDb();
                    } else {
                        BaseApplication.getAppComponent().getCmd().getFriendList();
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list != null) {
                        if (list.size() > 0) {
                            ArrayList<FriendBean> allList = new ArrayList<FriendBean>();
                            allList.addAll(list);
                            handleInitFriendListDataResult(allList);
                        }
                    }
                }, throwable -> {
                    AppLogger.e("getFriendList: " + throwable.getLocalizedMessage());
                });
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
                }, throwable -> {
                    AppLogger.e("sendAddReq: " + throwable.getLocalizedMessage());
                });
    }

    /**
     * 同意添加后SDK的调用
     */
    @Override
    public void acceptAddSDK(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(account1 -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().consentAddFriend(account1);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    AppLogger.e("acceptAddSDK: " + throwable.getLocalizedMessage());
                });
    }

    /**
     * desc:处理请求列表数据
     *
     * @param addReqList
     */
    private void handleInitAddReqListDataResult(final RxEvent.GetAddReqList addReqList) {
        if (getView() != null) {
            if (addReqList.arrayList.size() != 0) {
                getView().showAddReqListTitle();
                getView().initAddReqRecyList(initAddRequestData(addReqList));
            } else {
                addReqNull = true;
                checkAllNull();
                getView().hideAddReqListTitle();
            }
        }
    }

    /**
     * desc:处理列表数据
     *
     * @param friendList
     */
    private void handleInitFriendListDataResult(ArrayList<FriendBean> friendList) {
        if (getView() != null) {
            if (friendList.size() != 0) {
                getView().showFriendListTitle();
                getView().initFriendRecyList(friendList);
            } else {
                friendListNull = true;
                checkAllNull();
                getView().hideFriendListTitle();
                getView().initFriendRecyList(new ArrayList<FriendBean>());
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
                .subscribe(s -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().delAddFriendMsg(account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    AppLogger.e("deleteAddReq" + throwable.getLocalizedMessage());
                });
    }

    /**
     * 删除好友添加请求的回调
     *
     * @return
     */
    @Override
    public Subscription deleteAddReqBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.DeleteAddReqBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deleteAddReqBack -> {
                    getView().longClickDeleteItem(deleteAddReqBack.jfgResult.code);
                }, AppLogger::e);
    }


    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        if (mView == null || !mView.isAdded()) return;
        Observable.just(NetUtils.getJfgNetType())
                .filter(integer -> getView() != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> getView().onNetStateChanged(integer), AppLogger::e);
    }


    public void saveInDb(FriendBean bean) {
//        try {
//            DataBaseUtil.getInstance(account).dbManager.save(bean);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
    }

    public void clearAll() {
//        try {
//            DataBaseUtil.getInstance(account).dbManager.delete(FriendBean.class);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
    }

    public List<FriendBean> getAllFromDb() {
        List<FriendBean> all = null;
//        try {
//            all = DataBaseUtil.getInstance(account).dbManager.findAll(FriendBean.class);
//        } catch (DbException e) {
//            e.printStackTrace();
//        }
        return all;
    }

}
