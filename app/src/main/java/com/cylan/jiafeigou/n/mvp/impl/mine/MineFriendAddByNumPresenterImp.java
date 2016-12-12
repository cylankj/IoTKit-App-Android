package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddByNumContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.rx.RxBus;


import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineFriendAddByNumPresenterImp extends AbstractPresenter<MineFriendAddByNumContract.View>
        implements MineFriendAddByNumContract.Presenter {

    private CompositeSubscription compositeSubscription;

    public MineFriendAddByNumPresenterImp(MineFriendAddByNumContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            unSubscribe(compositeSubscription);
        }
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(checkFriendAccountCallBack());
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }


    /**
     * 是否想我发送过请求
     * @param bean
     */
    @Override
    public void checkIsSendAddReqToMe(MineAddReqBean bean) {

    }

    /**
     * 检测好友账号是否注册过
     */
    @Override
    public void checkFriendAccount(final String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String account) {
                        try {
                            JfgCmdInsurance.getCmd().checkFriendAccount(account);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.d("checkFriendAccount"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 检测好友的回调
     * @return
     */
    @Override
    public Subscription checkFriendAccountCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckAccountCallback.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckAccountCallback>() {
                    @Override
                    public void call(RxEvent.CheckAccountCallback checkAccountCallback) {
                        if (checkAccountCallback != null){
                            handlerCheckCallBackResult(checkAccountCallback);
                        }
                    }
                });
    }
    /**
     * 处理检测的回调结果
     * @param checkAccountCallback
     */
    private void handlerCheckCallBackResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (checkAccountCallback.i == 0){
            // 是亲友 已注册
            if (getView() != null){
                MineAddReqBean addReqBean = new MineAddReqBean();
                addReqBean.account = checkAccountCallback.s;
                addReqBean.alias = checkAccountCallback.s1;
                getView().hideFindLoading();
                getView().setFindResult(false,addReqBean);
            }
        }else {
            // 不是亲友 未注册 无结果
            if (getView() != null){
                getView().hideFindLoading();
                getView().showFindNoResult();
            }
        }
    }

}
