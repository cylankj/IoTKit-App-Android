package com.cylan.jiafeigou.n.mvp.impl.mine;


import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddReqDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineFriendAddReqDetailPresenterImp extends AbstractPresenter<MineFriendAddReqDetailContract.View> implements MineFriendAddReqDetailContract.Presenter {

    private Subscription addAsFriendSub;

    public MineFriendAddReqDetailPresenterImp(MineFriendAddReqDetailContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (addAsFriendSub != null && addAsFriendSub.isUnsubscribed()){
            addAsFriendSub.unsubscribe();
        }
    }

    /**
     * 添加为亲友；
     */
    @Override
    public void handlerAddAsFriend(JFGFriendRequest addRequestItems) {

        addAsFriendSub = Observable.just(addRequestItems)
                .map(new Func1<JFGFriendRequest, Boolean>() {
                    @Override
                    public Boolean call(JFGFriendRequest jfgFriendRequest) {
                        //TODO 调用SDK 添加为好友
                        return false;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {

                    }
                });

    }
}
