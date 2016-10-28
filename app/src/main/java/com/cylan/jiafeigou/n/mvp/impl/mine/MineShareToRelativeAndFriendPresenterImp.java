package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToRelativeAndFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToRelativeAndFriendPresenterImp extends AbstractPresenter<MineShareToRelativeAndFriendContract.View>
        implements MineShareToRelativeAndFriendContract.Presenter {

    private Subscription friendListSub;

    public MineShareToRelativeAndFriendPresenterImp(MineShareToRelativeAndFriendContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        initFriendListData();
    }

    @Override
    public void stop() {

    }

    @Override
    public void initFriendListData() {

        friendListSub = Observable.just(null)
                .map(new Func1<Object, ArrayList<RelAndFriendBean>>() {
                    @Override
                    public ArrayList<RelAndFriendBean> call(Object o) {
                        //TODO 获取亲友列表
                        return testData();
                    }
                })
                .delay(2000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<RelAndFriendBean>>() {
                    @Override
                    public void call(ArrayList<RelAndFriendBean> relAndFriendBeen) {
                        handlerDataResult(relAndFriendBeen);
                    }
                });
    }

    /**
     * 处理返回数据
     * @param relAndFriendBeen
     */
    private void handlerDataResult(ArrayList<RelAndFriendBean> relAndFriendBeen) {
        if (getView() != null && relAndFriendBeen.size() != 0){
            getView().initRecycleView(relAndFriendBeen);
        }else {
            getView().showNoFriendNullView();
        }
    }

    public ArrayList<RelAndFriendBean> testData() {
        ArrayList list = new ArrayList<RelAndFriendBean>();
        for (int i = 0; i < 9; i++) {
            RelAndFriendBean emMessage = new RelAndFriendBean();
            emMessage.account = "账号"+i;
            emMessage.alids = "昵称"+i;
            list.add(emMessage);
        }
        return list;
    }
}
