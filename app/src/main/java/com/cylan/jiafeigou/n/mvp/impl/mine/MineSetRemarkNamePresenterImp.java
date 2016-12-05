package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.text.TextUtils;

import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineSetRemarkNameContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.tencent.open.utils.HttpUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public class MineSetRemarkNamePresenterImp extends AbstractPresenter<MineSetRemarkNameContract.View> implements MineSetRemarkNameContract.Presenter {

    private CompositeSubscription compositeSubscription;

    public MineSetRemarkNamePresenterImp(MineSetRemarkNameContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getFriendRemarkNameCallBack());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    public boolean isEditEmpty(String string) {
        return TextUtils.isEmpty(string) ? true : false;
    }

    /**
     * 发送修改备注名的请求
     * @param friendBean
     */
    @Override
    public void sendSetmarkNameReq(final String newName, final RelAndFriendBean friendBean) {
        getView().showSendReqPro();
        rx.Observable.just(friendBean)
              .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<RelAndFriendBean>() {
                    @Override
                    public void call(RelAndFriendBean bean) {
                        JfgCmdInsurance.getCmd().setFriendMarkName(friendBean.account, newName);
                        JfgCmdInsurance.getCmd().getFriendInfo(friendBean.account);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendSetmarkNameReq: " + throwable.getLocalizedMessage());
                    }
                });

    }

    /**
     * 设置好友的备注名回调
     * @return
     */
    @Override
    public Subscription getFriendRemarkNameCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetFriendInfoCall.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetFriendInfoCall>() {
                    @Override
                    public void call(RxEvent.GetFriendInfoCall getFriendInfoCall) {
                        if (getFriendInfoCall != null && getFriendInfoCall instanceof RxEvent.GetFriendInfoCall){
                            if(getView() != null){
                                getView().hideSendReqPro();
                                getView().showFinishResult(getFriendInfoCall);
                            }
                        }
                    }
                });
    }


}
