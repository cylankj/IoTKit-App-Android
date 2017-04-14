package com.cylan.jiafeigou.n.mvp.impl.mine;


import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddReqDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineFriendAddReqDetailPresenterImp extends AbstractPresenter<MineFriendAddReqDetailContract.View> implements MineFriendAddReqDetailContract.Presenter {

    private CompositeSubscription compositeSubscription;

    private boolean isAddReqBack;   //请求添加过时 回加好友

    public MineFriendAddReqDetailPresenterImp(MineFriendAddReqDetailContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(excuteGetAddReqlistData());
        compositeSubscription.add(getAddReqListDataCall());
        compositeSubscription.add(sendAddFriendReqBack());
        compositeSubscription.add(consentAddFriendBack());
    }

    @Override
    public void stop() {
        if (compositeSubscription != null) {
            if (!compositeSubscription.isUnsubscribed()) {
                unSubscribe(compositeSubscription);
            }
        }
    }

    /**
     * 添加为亲友；
     */
    @Override
    public void handlerAddAsFriend(String addRequestItems) {
        rx.Observable.just(addRequestItems)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String account) {
                        try {
                            BaseApplication.getAppComponent().getCmd().consentAddFriend(account);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("handlerAddAsFriend" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 判断添加请求是否过期
     *
     * @param addRequestItems
     * @return
     */
    @Override
    public void checkAddReqOutTime(MineAddReqBean addRequestItems) {
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
    public void sendAddReq(MineAddReqBean addRequestItems) {
        rx.Observable.just(addRequestItems)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<MineAddReqBean>() {
                    @Override
                    public void call(MineAddReqBean mineAddReqBean) {
                        try {
                            BaseApplication.getAppComponent().getCmd().addFriend(mineAddReqBean.account, "");
                            isAddReqBack = true;
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendAddReq" + throwable.getLocalizedMessage());
                    }
                });
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
                .subscribe(new Action1<RxEvent.GetAddReqList>() {
                    @Override
                    public void call(RxEvent.GetAddReqList getAddReqList) {
                        if (getAddReqList != null && getAddReqList instanceof RxEvent.GetAddReqList) {
                            if (getAddReqList.arrayList.size() == 0) {
                                // 未向我发送过请求
                                if (getView() != null) getView().jump2AddReqFragment();
                            } else {
                                // 判断是否包含该账号
                                if (getView() != null) getView().isHasAccountResult(getAddReqList);
                            }
                        }
                    }
                }, AppLogger::e);
    }


    /**
     * 执行请求数据
     */
    @Override
    public Subscription excuteGetAddReqlistData() {
        return rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        BaseApplication.getAppComponent().getCmd().getFriendRequestList();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("excuteGetAddReqlistData" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 添加好友的回调
     *
     * @return
     */
    @Override
    public Subscription sendAddFriendReqBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.AddFriendBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.AddFriendBack>() {
                    @Override
                    public void call(RxEvent.AddFriendBack addFriendBack) {
                        if (isAddReqBack && addFriendBack != null && addFriendBack instanceof RxEvent.AddFriendBack) {
                            getView().showSendAddReqResult(addFriendBack.jfgResult.code == 0 ? true : false);
                        }
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
                .subscribe(new Action1<RxEvent.ConsentAddFriendBack>() {
                    @Override
                    public void call(RxEvent.ConsentAddFriendBack consentAddFriendBack) {
                        if (consentAddFriendBack != null && consentAddFriendBack instanceof RxEvent.ConsentAddFriendBack) {
                            getView().showAddedReult(consentAddFriendBack.jfgResult.code == 0 ? true : false);
                        }
                    }
                }, AppLogger::e);
    }
}
