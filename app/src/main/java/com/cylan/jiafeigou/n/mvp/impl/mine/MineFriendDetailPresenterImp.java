package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

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


    public MineFriendDetailPresenterImp(MineFriendDetailContract.View view) {
        super(view);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    /**
     * 发送删除好友请求
     * @param account
     */
    @Override
    public void sendDeleteFriendReq(final String account) {
        if (getView() != null){
            getView().showDeleteProgress();
        }
        rx.Observable.just(account)
               .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        JfgCmdInsurance.getCmd().delFriend(account);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e(throwable.getLocalizedMessage());
                    }
                });
    }

}
