package com.cylan.jiafeigou.n.mvp.impl.mine;


import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.cache.db.module.FriendsReqBean;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddReqDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ListUtils;

import java.util.ArrayList;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class AddFriendsReqDetailPresenterImp extends AbstractPresenter<MineFriendAddReqDetailContract.View> implements MineFriendAddReqDetailContract.Presenter {


    private boolean isAddReqBack;   //请求添加过时 回加好友

    public AddFriendsReqDetailPresenterImp(MineFriendAddReqDetailContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        addSubscription(executeGetAddReqList());
        addSubscription(getAddReqListDataCall());
        addSubscription(sendAddFriendRsp());
        addSubscription(consentAddFriendBack());
    }


    /**
     * 添加为亲友；
     */
    @Override
    public void handlerAddAsFriend(String addRequestItems) {
        rx.Observable.just(addRequestItems)
                .subscribeOn(Schedulers.newThread())
                .subscribe(account -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().consentAddFriend(account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    /**
     * 判断添加请求是否过期
     *
     * @param addRequestItems
     * @return
     */
    @Override
    public void checkAddReqOutTime(FriendsReqBean addRequestItems) {
        //true 过期 false未过期
        long oneMount = 30 * 24 * 60 * 60 * 1000L;
        if (oneMount - addRequestItems.time < 0) {
            if (getView() != null) {
                getView().showReqOutTimeDialog();
            }
        } else {
            handlerAddAsFriend(addRequestItems.account);
        }
    }

    /**
     * 发送好友添加请求
     *
     * @param addRequestItems
     */
    @Override
    public void sendAddReq(FriendsReqBean addRequestItems) {
        rx.Observable.just(addRequestItems)
                .subscribeOn(Schedulers.newThread())
                .subscribe(mineAddReqBean -> {
                    try {
                        BaseApplication.getAppComponent().getCmd().addFriend(mineAddReqBean.account, "");
                        isAddReqBack = true;
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                }, AppLogger::e);
    }

    /**
     * 判断是否向我发送过添加请求
     *
     * @return
     */
    @Override
    public Subscription getAddReqListDataCall() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetAddReqList.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getAddReqList -> {
                    ArrayList<FriendsReqBean> arrayList = BaseApplication.getAppComponent().getSourceManager().getFriendsReqList();
                    if (ListUtils.getSize(arrayList) == 0) {
                        // 未向我发送过请求
                        if (getView() != null) getView().jump2AddReqFragment();
                    } else {
                        // 判断是否包含该账号
                        if (getView() != null) getView().isHasAccountResult(getAddReqList);
                    }

                }, AppLogger::e);
    }


    /**
     * 执行请求数据
     */
    @Override
    public Subscription executeGetAddReqList() {
        return rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    BaseApplication.getAppComponent().getCmd().getFriendRequestList();
                }, AppLogger::e);
    }

    /**
     * 添加好友的回调
     *
     * @return
     */
    @Override
    public Subscription sendAddFriendRsp() {
        return RxBus.getCacheInstance().toObservable(RxEvent.AddFriendBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(addFriendBack -> {
                    if (isAddReqBack && addFriendBack != null) {
                        getView().showSendAddReqResult(addFriendBack.jfgResult.code == 0);
                    }
                }, AppLogger::e);
    }

    /**
     * 同意添加好友的回调
     *
     * @return
     */
    @Override
    public Subscription consentAddFriendBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ConsentAddFriendBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consentAddFriendBack -> {
                    if (consentAddFriendBack != null) {
                        getView().showAddedReult(consentAddFriendBack.jfgResult.code == 0);
                    }
                }, AppLogger::e);
    }
}
