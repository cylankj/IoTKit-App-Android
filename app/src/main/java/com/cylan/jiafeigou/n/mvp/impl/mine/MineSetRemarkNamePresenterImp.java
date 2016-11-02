package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.text.TextUtils;

import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineSetRemarkNameContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.tencent.open.utils.HttpUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/23
 * 描述：
 */
public class MineSetRemarkNamePresenterImp extends AbstractPresenter<MineSetRemarkNameContract.View> implements MineSetRemarkNameContract.Presenter {

    private Subscription sendSetMarkNameReqSub;

    public MineSetRemarkNamePresenterImp(MineSetRemarkNameContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (sendSetMarkNameReqSub != null && !sendSetMarkNameReqSub.isUnsubscribed()){
            sendSetMarkNameReqSub.unsubscribe();
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
        if (getView() != null){
            getView().showSendReqPro();
        }
        sendSetMarkNameReqSub = Observable.just(friendBean)
                .map(new Func1<RelAndFriendBean, Integer>() {
                    @Override
                    public Integer call(RelAndFriendBean friendBean) {
                        //调用SDK 发送修改备注名的请求
                        try {
                            return JfgAppCmd.getInstance().setFriendMarkName(friendBean.account,newName);
                        }catch (Exception e){
                            throw new NullPointerException(e.getMessage());
                        }
                    }
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        if (integer != null){
                            getView().hideSendReqPro();
                            getView().showFinishResult(integer);
                        }
                    }
                });
    }
}
