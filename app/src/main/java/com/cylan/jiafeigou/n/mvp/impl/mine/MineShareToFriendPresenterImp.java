package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToFriendContract;
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
public class MineShareToFriendPresenterImp extends AbstractPresenter<MineShareToFriendContract.View>
        implements MineShareToFriendContract.Presenter {

    private Subscription friendListSub;
    private Subscription getHasShareFriendNumSub;

    private static int hasShareNum;
    private Subscription sendShareFriendSub;

    public MineShareToFriendPresenterImp(MineShareToFriendContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        initFriendListData();
        handlerHasShareFriendNumber();
    }

    @Override
    public void stop() {
        if (friendListSub != null && friendListSub.isUnsubscribed()){
            friendListSub.unsubscribe();
        }

        if (getHasShareFriendNumSub != null && getHasShareFriendNumSub.isUnsubscribed()){
            getHasShareFriendNumSub.unsubscribe();
        }

        if (sendShareFriendSub != null && sendShareFriendSub.isUnsubscribed()){
            sendShareFriendSub.unsubscribe();
        }
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
     * 处理已分享的亲友人数
     */
    @Override
    public void handlerHasShareFriendNumber() {
        //TODO 从SDK获取
        getHasShareFriendNumSub = Observable.just(null)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        hasShareNum = 1;
                        handlerHasShareNumResult(hasShareNum);
                    }
                });
    }

    /**
     * 获取已分享的亲友人数
     */
    @Override
    public int getHasShareFriendNumber() {
        return hasShareNum;
    }

    /**
     * 发送分享给亲友请求
     */
    @Override
    public void sendShareToFriendReq(ArrayList<RelAndFriendBean> list) {
        sendShareFriendSub = Observable.just(null)
                .map(new Func1<Object, Integer>() {
                    @Override
                    public Integer call(Object o) {
                        //TODO 调用SDK分享设备给亲友
                        // 返回1.分享成功；返回2.部分分享成功：返回3,分享失败
                        return 2;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer o) {
                        handlerAfterSendShareReq(o);
                    }
                });
    }

    /**
     * 处理分享请求发送出去之后
     * @param o
     */
    private void handlerAfterSendShareReq(Integer o) {

        switch (o){
            case 1:        //分享成功
                getView().showShareAllSuccess();
                break;

            case 2:         //分享部分失败
                getView().showShareSomeFail(2);
                break;

            case 3:         //分享失败
                getView().showShareAllFail();
                break;
        }

    }

    /**
     * 处理已分享的人数的实时显示
     * @param hasShareNum
     */
    private void handlerHasShareNumResult(int hasShareNum) {
        if (getView() != null){
            getView().setHasShareFriendNum(false,hasShareNum);//第一次进入默认显示为灰色
        }
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
        for (int i = 0; i < 4; i++) {
            RelAndFriendBean emMessage = new RelAndFriendBean();
            emMessage.account = "账号"+i;
            emMessage.alids = "昵称"+i;
            list.add(emMessage);
        }
        return list;
    }
}
