package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineAddFromContactPresenterImp extends AbstractPresenter<MineAddFromContactContract.View> implements MineAddFromContactContract.Presenter {

    private Subscription sendRequestSub;

    public MineAddFromContactPresenterImp(MineAddFromContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (sendRequestSub != null) {
            sendRequestSub.unsubscribe();
        }
    }

    @Override
    public void sendRequest(String mesg) {
        //TODO 向服务器发送请求
        sendRequestSub = Observable.just(mesg)
                .map(new Func1<String, Object>() {
                    @Override
                    public Object call(String s) {
                        //TODO 向服务器发送请求添加请求
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().showResultDialog();
                    }
                });
    }
}
