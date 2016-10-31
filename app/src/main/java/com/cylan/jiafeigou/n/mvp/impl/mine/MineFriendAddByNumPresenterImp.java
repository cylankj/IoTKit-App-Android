package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddByNumContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;

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

    public MineFriendAddByNumPresenterImp(MineFriendAddByNumContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (findUserFromServerSub != null) {
            findUserFromServerSub.unsubscribe();
        }
    }

    @Override
    public void findUserFromServer(String number) {
        if (number == null) {
            return;
        }
        findUserFromServerSub = Observable.just(number)
                .map(new Func1<String, UserInfoBean>() {
                    @Override
                    public UserInfoBean call(String s) {
                        //TODO 访问服务器查询该用户
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserInfoBean>() {
                    @Override
                    public void call(UserInfoBean bean) {
                        getView().showFindResult(bean);
                    }
                });

    }
}
