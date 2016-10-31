package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.view.View;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.view.adapter.AddRelativesAndFriendsAdapter;
import com.cylan.jiafeigou.n.view.adapter.RelativesAndFriendsAdapter;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.superadapter.OnItemClickListener;
import com.cylan.superadapter.OnItemLongClickListener;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import rx.Subscription;
import rx.functions.Action1;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendsPresenterImp extends AbstractPresenter<MineFriendsContract.View> implements MineFriendsContract.Presenter {

    private Subscription friendListSub;
    private Subscription addReqListSub;

    private boolean addReqNull;
    private boolean friendListNull;

    public MineFriendsPresenterImp(MineFriendsContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        initAddReqRecyListData();
        initFriendRecyListData();
        checkAllNull();
    }

    @Override
    public void stop() {
        if (friendListSub != null && friendListSub.isUnsubscribed()) {
            friendListSub.unsubscribe();
        }

        if (addReqListSub != null && addReqListSub.isUnsubscribed()) {
            addReqListSub.unsubscribe();
        }
    }


    @Override
    public ArrayList<JFGFriendRequest> initAddRequestData() {

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
        sortAddReqList(list);
        return list;
    }

    @Override
    public ArrayList<JFGFriendAccount> initRelativatesAndFriendsData() {
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
    public boolean checkAddRequestOutTime(JFGFriendRequest bean) {
        long oneMount = 30 * 24 * 60 * 60 * 1000L;
        return (System.currentTimeMillis() - Long.parseLong(bean.time + "")) > oneMount;
    }

    /**
     * desc：添加请求集合的排序
     *
     * @param list
     * @return
     */
    public ArrayList<JFGFriendRequest> sortAddReqList(ArrayList<JFGFriendRequest> list) {
        Comparator<JFGFriendRequest> comparator = new Comparator<JFGFriendRequest>() {
            @Override
            public int compare(JFGFriendRequest lhs, JFGFriendRequest rhs) {
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
    public ArrayList<JFGFriendAccount> sortFriendList(ArrayList<JFGFriendAccount> list) {

        Comparator<JFGFriendAccount> comparator = new Comparator<JFGFriendAccount>() {
            @Override
            public int compare(JFGFriendAccount lhs, JFGFriendAccount rhs) {
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
    public void initFriendRecyListData() {
        friendListSub = RxBus.getDefault().toObservable(RxEvent.GetFriendList.class)
                .subscribe(new Action1<RxEvent.GetFriendList>() {
                    @Override
                    public void call(RxEvent.GetFriendList o) {
                        if (getView() != null)
                            handleInitFriendListDataResult(o);
                    }
                });

        //测试数据 TODO
        RxEvent.GetFriendList friendListTest = new RxEvent.GetFriendList(1, initRelativatesAndFriendsData());
        handleInitFriendListDataResult(friendListTest);
    }

    /**
     * desc：初始化添加请求列表的数据
     */
    @Override
    public void initAddReqRecyListData() {

        addReqListSub = RxBus.getDefault().toObservable(RxEvent.GetAddReqList.class)
                .subscribe(new Action1<RxEvent.GetAddReqList>() {
                    @Override
                    public void call(RxEvent.GetAddReqList o) {
                        if (getView() == null)
                            return;
                        handleInitAddReqListDataResult(o);
                    }
                });

        //测试数据 TODO
        RxEvent.GetAddReqList addReqListTest = new RxEvent.GetAddReqList(1, initAddRequestData());
        handleInitAddReqListDataResult(addReqListTest);
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
     * desc:处理请求列表数据
     *
     * @param addReqList
     */
    private void handleInitAddReqListDataResult(final RxEvent.GetAddReqList addReqList) {
        if (addReqList.arrayList.size() != 0) {
            getView().showAddReqListTitle();
            getView().initAddReqRecyList(addReqList.arrayList);
        } else {
            addReqNull = true;
            getView().hideAddReqListTitle();
        }
    }

    /**
     * desc:处理列表数据
     *
     * @param friendList
     */
    private void handleInitFriendListDataResult(final RxEvent.GetFriendList friendList) {
        if (friendList.arrayList.size() != 0) {
            getView().showFriendListTitle();
            getView().initFriendRecyList(friendList.arrayList);

        } else {
            friendListNull = true;
            getView().hideFriendListTitle();
        }
    }


}
