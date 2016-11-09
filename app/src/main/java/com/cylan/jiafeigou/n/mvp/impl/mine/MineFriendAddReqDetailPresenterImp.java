package com.cylan.jiafeigou.n.mvp.impl.mine;


import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddReqDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;

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
public class MineFriendAddReqDetailPresenterImp extends AbstractPresenter<MineFriendAddReqDetailContract.View> implements MineFriendAddReqDetailContract.Presenter {

    private Subscription addAsFriendSub;
    private Subscription sendAddReqSub;

    public MineFriendAddReqDetailPresenterImp(MineFriendAddReqDetailContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (addAsFriendSub != null && !addAsFriendSub.isUnsubscribed()){
            addAsFriendSub.unsubscribe();
        }

        if (sendAddReqSub != null && !sendAddReqSub.isUnsubscribed()){
            sendAddReqSub.unsubscribe();
        }
    }

    /**
     * 添加为亲友；
     */
    @Override
    public void handlerAddAsFriend(MineAddReqBean addRequestItems) {

        addAsFriendSub = Observable.just(addRequestItems)
                .map(new Func1<MineAddReqBean, Boolean>() {
                    @Override
                    public Boolean call(MineAddReqBean jfgFriendRequest) {
                        //TODO 调用SDK 添加为好友
                        return false;
                    }
                })
                .delay(2000,TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        getView().showAddedReult(aBoolean);
                    }
                });
    }

    /**
     * 判断添加请求是否过期
     * @param addRequestItems
     * @return
     */
    @Override
    public void checkAddReqOutTime(MineAddReqBean addRequestItems) {
        //true 过期 false未过期
        if ((System.currentTimeMillis() - addRequestItems.time) > 30*24*60*1000 ){
            if (getView() != null){
                getView().showReqOutTimeDialog();
            }
        }else {
            handlerAddAsFriend(addRequestItems);
        }
    }

    /**
     * 发送好友添加请求
     * @param addRequestItems
     */
    @Override
    public void sendAddReq(MineAddReqBean addRequestItems) {
        //调用SDK 模拟发送请求
        sendAddReqSub = Observable.just(addRequestItems)
                .map(new Func1<MineAddReqBean, Boolean>() {
                    @Override
                    public Boolean call(MineAddReqBean jfgFriendRequest) {
                        //TODO 调用SDK 发送添加请求
                        return false;
                    }
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean o) {
                        getView().showSendAddReqResult(o);
                    }
                });
    }
}
