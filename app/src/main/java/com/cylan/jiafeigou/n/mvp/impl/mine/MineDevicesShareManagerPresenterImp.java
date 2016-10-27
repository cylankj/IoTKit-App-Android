package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineDevicesShareManagerContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.MineHasShareAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;

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
 * 创建时间：2016/9/8
 * 描述：
 */
public class MineDevicesShareManagerPresenterImp extends AbstractPresenter<MineDevicesShareManagerContract.View>
        implements MineDevicesShareManagerContract.Presenter, MineHasShareAdapter.OnCancleShareListenter {

    private MineHasShareAdapter hasShareAdapter;
    private Subscription cancleShareSub;

    public MineDevicesShareManagerPresenterImp(MineDevicesShareManagerContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (cancleShareSub != null && cancleShareSub.isUnsubscribed()){
            cancleShareSub.unsubscribe();
        }
    }

    @Override
    public void initHasShareListData(ArrayList<RelAndFriendBean> shareDeviceFriendlist) {

        if (getView() != null && shareDeviceFriendlist != null && shareDeviceFriendlist.size() != 0){
            hasShareAdapter = new MineHasShareAdapter(getView().getContext(),shareDeviceFriendlist,null);
            getView().inintHasShareFriendRecyView(hasShareAdapter);
            hasShareAdapter.setOnCancleShareListenter(this);
        }else {
            getView().hideHasShareListTitle();
            getView().showNoHasShareFriendNullView();
        }

    }

    @Override
    public void cancleShare(final RelAndFriendBean bean) {
        if (getView() != null){
            getView().showCancleShareProgress();
        }
        //调用SDK 取消分享
        cancleShareSub = Observable.just(null)
                .map(new Func1<Object, Boolean>() {
                    @Override
                    public Boolean call(Object o) {
                        //TODO 调用SDK 取消分享
                        return null;
                    }
                })
                .delay(3000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        getView().hideCancleShareProgress();
                        deleteItems(bean);
                        ToastUtil.showToast(getView().getContext(),"取消分享成功");
                    }
                });
    }

    @Override
    public void deleteItems(RelAndFriendBean bean) {
        hasShareAdapter.remove(bean);
        hasShareAdapter.notifyDataSetHasChanged();
    }

    /**
     * desc:点击取消分享按钮
     * @param item
     */
    @Override
    public void onCancleShare(RelAndFriendBean item) {
        if (getView() != null){
            getView().showCancleShareDialog(item);
        }
    }
}
