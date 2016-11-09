package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddByNumContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;

import java.util.concurrent.TimeUnit;

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
public class MineFriendAddByNumPresenterImp extends AbstractPresenter<MineFriendAddByNumContract.View>
        implements MineFriendAddByNumContract.Presenter {

    private Subscription findUserFromServerSub;
    private Subscription checkSendToMeSub;

    public MineFriendAddByNumPresenterImp(MineFriendAddByNumContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (findUserFromServerSub != null && !findUserFromServerSub.isUnsubscribed()) {
            findUserFromServerSub.unsubscribe();
        }

        if (checkSendToMeSub != null && !checkSendToMeSub.isUnsubscribed()){
            checkSendToMeSub.unsubscribe();
        }
    }

    @Override
    public void findUserFromServer(String number) {
        if (number == null) {
            return;
        }
        if (getView() != null){
            getView().showFindLoading();
        }
        findUserFromServerSub = Observable.just(number)
                .map(new Func1<String, MineAddReqBean>() {
                    @Override
                    public MineAddReqBean call(String s) {
                        //TODO 访问服务器查询该用户
                        return testData();
                    }
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<MineAddReqBean>() {
                    @Override
                    public void call(MineAddReqBean bean) {
                        getView().hideFindLoading();
                        getView().showFindResult(bean);
                    }
                });
    }

    @Override
    public void checkIsSendAddReqToMe(final MineAddReqBean bean) {

        checkSendToMeSub = Observable.just(bean)
                .map(new Func1<MineAddReqBean, Boolean>() {
                    @Override
                    public Boolean call(MineAddReqBean bean) {
                        // TODO SDK 有么有接口 获取好友请求列表查询
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean o) {
                        getView().setFindResult(false,o,bean);
                    }
                });
    }

    /**
     * 测试数据
     * @return
     */
    private MineAddReqBean testData() {
        MineAddReqBean info = new MineAddReqBean();
        info.alias = "赵四";
        info.account = "13413544333";
        return info;
    }
}
