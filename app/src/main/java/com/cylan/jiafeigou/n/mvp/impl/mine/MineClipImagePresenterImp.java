package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineClipImageContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.rx.RxBus;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/11/7
 * 描述：
 */
public class MineClipImagePresenterImp extends AbstractPresenter<MineClipImageContract.View> implements MineClipImageContract.Presenter {

    private CompositeSubscription subscription;
    public JFGAccount jfgAccount;

    public MineClipImagePresenterImp(MineClipImageContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 上传用户的头像
     * @param path
     */
    @Override
    public void upLoadUserHeadImag(String path) {
        rx.Observable.just(path)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String path) {
                        JfgCmdInsurance.getCmd().updateAccountPortrait(path);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("upLoadUserHeadImag: " + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 接收上传头像的回调
     */
    @Override
    public Subscription getUpLoadResult() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetHttpDoneResult.class)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetHttpDoneResult>() {
                    @Override
                    public void call(RxEvent.GetHttpDoneResult getHttpDoneResult) {
                        if (getHttpDoneResult != null && getHttpDoneResult instanceof RxEvent.GetHttpDoneResult){
                            getView().hideUpLoadPro();
                            handlerUploadImage(getHttpDoneResult);
                            getView().upLoadResultView(getHttpDoneResult.jfgMsgHttpResult.requestId);
                        }
                    }
                });
    }

    /**
     * 处理上传头像文件后
     * @param getHttpDoneResult
     */
    private void handlerUploadImage(RxEvent.GetHttpDoneResult getHttpDoneResult) {
        if (getHttpDoneResult.jfgMsgHttpResult.requestId == JError.ErrorOK){
            sendResetUrl();
        }
    }

    /**
     * 更新头像的Url
     */
    private void sendResetUrl() {
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        if (jfgAccount != null){
                            jfgAccount.setPhoto(true);
                            JfgCmdInsurance.getCmd().setAccount(jfgAccount);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendResetUrl"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到用户的信息
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo){
                            jfgAccount = getUserInfo.jfgAccount;
                        }
                    }
                });
    }

    @Override
    public void start() {
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }else {
            subscription = new CompositeSubscription();
            subscription.add(getAccount());
            subscription.add(getUpLoadResult());
        }
    }

    @Override
    public void stop() {
        if (subscription != null && !subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }
    }
}
