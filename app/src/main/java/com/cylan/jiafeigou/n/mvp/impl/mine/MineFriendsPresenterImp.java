package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendsPresenterImp extends AbstractPresenter<MineFriendsContract.View> implements MineFriendsContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private boolean addReqNull;
    private boolean friendListNull;

    public MineFriendsPresenterImp(MineFriendsContract.View view) {
        super(view);
        view.setPresenter(this);
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void start() {
        compositeSubscription.add(getAddRequest());
        compositeSubscription.add(getFriendList());
        compositeSubscription.add(initAddReqRecyListData());
        compositeSubscription.add(initFriendRecyListData());
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    @Override
    public ArrayList<MineAddReqBean> initAddRequestData(RxEvent.GetAddReqList addReqList) {

        ArrayList list = new ArrayList<MineAddReqBean>();

        for (JFGFriendRequest jfgFriendRequest:addReqList.arrayList){
            MineAddReqBean emMessage = new MineAddReqBean();
            //emMessage.iconUrl = JfgCmdInsurance.getCmd().getCloudUrlByType(jfgFriendRequest.account);
            emMessage.alias = jfgFriendRequest.alias;
            emMessage.sayHi = jfgFriendRequest.sayHi;
            emMessage.account = jfgFriendRequest.account;
            emMessage.time = jfgFriendRequest.time;
            list.add(emMessage);
        }
        sortAddReqList(list);
        return list;
    }

    /**
     * 测试数据
     * @return
     */
    public ArrayList<JFGFriendRequest> testAddRequestData() {

        ArrayList list = new ArrayList<JFGFriendRequest>();

        JFGFriendRequest emMessage = new JFGFriendRequest();
        emMessage.alias = "乔帮主";
        emMessage.sayHi = "我是小小姨";
        emMessage.account = "110";
        emMessage.time = System.currentTimeMillis();

        JFGFriendRequest emMessage2 = new JFGFriendRequest();
        emMessage2.alias = "张无忌";
        emMessage2.sayHi = "我是大大姨";
        emMessage2.account = "120";
        emMessage2.time = System.currentTimeMillis();
        list.add(emMessage);
        list.add(emMessage2);
        return list;
    }

    @Override
    public ArrayList<RelAndFriendBean> initRelativatesAndFriendsData(RxEvent.GetFriendList friendList) {
        ArrayList list = new ArrayList<RelAndFriendBean>();
        for (JFGFriendAccount account:friendList.arrayList) {
            RelAndFriendBean emMessage = new RelAndFriendBean();
            //emMessage.iconUrl = JfgCmdEnsurance.getCmd().getCloudUrl(account.account);
            emMessage.iconUrl = "http://www.uimaker.com/uploads/allimg/120410/1_120410103814_7.jpg";
            emMessage.markName = account.markName;
            emMessage.account = account.account;
            emMessage.alias = account.alias;
            list.add(emMessage);
        }
        return list;
    }

    public ArrayList<JFGFriendAccount> testRelativatesAndFriendsData() {
        ArrayList list = new ArrayList<JFGFriendAccount>();
        for (int i = 0; i < 9; i++) {
            JFGFriendAccount emMessage = new JFGFriendAccount();
            emMessage.markName = "阿三" + i;
            emMessage.account = "账号" + i;
            emMessage.alias = "昵称" + i;
            list.add(emMessage);
        }
        return list;
    }

    @Override
    public boolean checkAddRequestOutTime(MineAddReqBean bean) {
        long oneMount = 30 * 24 * 60 * 60 * 1000L;
        return (System.currentTimeMillis() - Long.parseLong(bean.time + "")) > oneMount;
    }

    /**
     * desc：添加请求集合的排序
     *
     * @param list
     * @return
     */
    public ArrayList<MineAddReqBean> sortAddReqList(ArrayList<MineAddReqBean> list) {
        Comparator<MineAddReqBean> comparator = new Comparator<MineAddReqBean>() {
            @Override
            public int compare(MineAddReqBean lhs, MineAddReqBean rhs) {
                long oldTime = Long.parseLong(rhs.time + "");
                long newTime = Long.parseLong(lhs.time + "");
                return (int) (newTime - oldTime);
            }
        };
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * desc：好友列表的排序
     *
     * @param list
     * @return
     */
    public ArrayList<RelAndFriendBean> sortFriendList(ArrayList<RelAndFriendBean> list) {

        Comparator<RelAndFriendBean> comparator = new Comparator<RelAndFriendBean>() {
            @Override
            public int compare(RelAndFriendBean lhs, RelAndFriendBean rhs) {
                //TODO 获取到首字母
                return 0;
            }
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetFriendList>() {
                    @Override
                    public void call(RxEvent.GetFriendList o) {
                        if (o != null && o instanceof RxEvent.GetFriendList){
                            handleInitFriendListDataResult(o);
                        }
                    }
                });
    }

    /**
     * desc：初始化添加请求列表的数据
     */
    @Override
    public Subscription initAddReqRecyListData() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetAddReqList.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetAddReqList>() {
                    @Override
                    public void call(RxEvent.GetAddReqList o) {
                        if(o != null && o instanceof RxEvent.GetAddReqList){
                            handleInitAddReqListDataResult(o);
                        }
                    }
                });
    }

    @Override
    public void checkAllNull() {
        if (addReqNull && friendListNull) {
            if (getView() != null) {
                getView().hideAddReqListTitle();
                getView().hideFriendListTitle();
                getView().showNullView();
            }
        }
    }

    /**
     * 启动获取添加请求的SDK
     * @return
     */
    @Override
    public Subscription getAddRequest() {
        return rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JfgCmdInsurance.getCmd().getFriendRequestList();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getAddRequest: " + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 启动获取好友列表的SDK
     * @return
     */
    @Override
    public Subscription getFriendList() {
        return rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JfgCmdInsurance.getCmd().getFriendList();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getFriendList: " + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 发送添加请求
     */
    @Override
    public void sendAddReq(final String account) {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        JfgCmdInsurance.getCmd().addFriend(account,"");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendAddReq: " + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 同意添加后SDK的调用
     */
    @Override
    public void acceptAddSDK(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String account) {
                        JfgCmdInsurance.getCmd().consentAddFriend(account);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("acceptAddSDK: " + throwable.getLocalizedMessage());
                    }
                });
    }


    /**
     * desc:处理请求列表数据
     *
     * @param addReqList
     */
    private void handleInitAddReqListDataResult(final RxEvent.GetAddReqList addReqList) {
        if (getView() != null){
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
    private void handleInitFriendListDataResult(final RxEvent.GetFriendList friendList) {
        if (getView() != null){
            if (friendList.arrayList.size() != 0) {
                getView().showFriendListTitle();
                getView().initFriendRecyList(initRelativatesAndFriendsData(friendList));
            } else {
                friendListNull = true;
                checkAllNull();
                getView().hideFriendListTitle();
            }
        }
    }

}