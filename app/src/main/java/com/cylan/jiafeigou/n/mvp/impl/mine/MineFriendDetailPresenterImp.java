package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

import java.util.concurrent.TimeUnit;

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
public class MineFriendDetailPresenterImp extends AbstractPresenter<MineFriendDetailContract.View> implements MineFriendDetailContract.Presenter {

    private Subscription sendDelFriendReqSub;

    public MineFriendDetailPresenterImp(MineFriendDetailContract.View view) {
        super(view);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (sendDelFriendReqSub != null && !sendDelFriendReqSub.isUnsubscribed()){
            sendDelFriendReqSub.unsubscribe();
        }
    }

    /**
     * 发送删除好友请求
     * @param account
     */
    @Override
    public void sendDeleteFriendReq(String account) {
        if (getView() != null){
            getView().showDeleteProgress();
        }
        sendDelFriendReqSub = Observable.just(account)
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        // TODO 调用SDK 发送删除好友请求
                        return null;
                    }
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        // TODO 根据integer判断结果

                        getView().hideDeleteProgress();
                        getView().handlerDelCallBack();
                    }
                });
    }


}
