package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineDevicesShareManagerContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.rx.RxBus;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public class MineDevicesShareManagerPresenterImp extends AbstractPresenter<MineDevicesShareManagerContract.View>
        implements MineDevicesShareManagerContract.Presenter{

    private CompositeSubscription compositeSubscription;

    public MineDevicesShareManagerPresenterImp(MineDevicesShareManagerContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getHasShareListCallback());
            compositeSubscription.add(cancleShareCallBack());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }
    }

    /**
     * 获取已分享的好友列表
     * @param cid
     */
    @Override
    public void getHasShareList(String cid) {
        rx.Observable.just(cid)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String cid) {
                        try {
                            JfgCmdInsurance.getCmd().getUnShareListByCid(cid);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("getHasShareList"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到已分享好友的回调
     * @return
     */
    @Override
    public Subscription getHasShareListCallback() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetHasShareFriendCallBack.class)
                .flatMap(new Func1<RxEvent.GetHasShareFriendCallBack, Observable<ArrayList<RelAndFriendBean>>>() {
                    @Override
                    public Observable<ArrayList<RelAndFriendBean>> call(RxEvent.GetHasShareFriendCallBack getHasShareFriendCallBack) {
                        if (getHasShareFriendCallBack != null && getHasShareFriendCallBack instanceof RxEvent.GetHasShareFriendCallBack){
                            return Observable.just(converData(getHasShareFriendCallBack));
                        }else {
                            return null;
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<RelAndFriendBean>>() {
                    @Override
                    public void call(ArrayList<RelAndFriendBean> list) {
                        initHasShareListData(list);
                    }
                });
    }

    /**
     * 将数据装换
     * @param getHasShareFriendCallBack
     */
    private ArrayList<RelAndFriendBean> converData(RxEvent.GetHasShareFriendCallBack getHasShareFriendCallBack) {
        ArrayList<RelAndFriendBean> list = new ArrayList<>();
        for (JFGFriendAccount friendBean:getHasShareFriendCallBack.arrayList){
            RelAndFriendBean tempBean = new RelAndFriendBean();
            tempBean.account = friendBean.account;
            tempBean.alias = friendBean.alias;
            tempBean.markName = friendBean.markName;
            list.add(tempBean);
        }
        return list;
    }

    @Override
    public void initHasShareListData(ArrayList<RelAndFriendBean> shareDeviceFriendlist) {
        if (getView() != null && shareDeviceFriendlist != null && shareDeviceFriendlist.size() != 0){
            getView().showHasShareListTitle();
            getView().initHasShareFriendRecyView(shareDeviceFriendlist);
        }else {
            getView().hideHasShareListTitle();
            getView().showNoHasShareFriendNullView();
        }
    }

    /**
     * 取消分享设备
     * @param cid
     * @param bean
     */
    @Override
    public void cancleShare(final String cid, final RelAndFriendBean bean) {
        if (getView() != null){
            getView().showCancleShareProgress();
        }
        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            JfgCmdInsurance.getCmd().unShareDevice(cid,bean.account);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("cancleShare"+throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 取消分享的回调
     * @return
     */
    @Override
    public Subscription cancleShareCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.UnshareDeviceCallBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.UnshareDeviceCallBack>() {
                    @Override
                    public void call(RxEvent.UnshareDeviceCallBack unshareDeviceCallBack) {
                        if (unshareDeviceCallBack != null && unshareDeviceCallBack instanceof RxEvent.UnshareDeviceCallBack){
                            handlerUnShareCallback(unshareDeviceCallBack);
                        }
                    }
                });
    }

    /**
     * 取消分享回调的处理
     * @param unshareDeviceCallBack
     */
    private void handlerUnShareCallback(RxEvent.UnshareDeviceCallBack unshareDeviceCallBack) {
        if (getView() != null){
            getView().hideCancleShareProgress();
            if (unshareDeviceCallBack.i == JError.ErrorOK){
                getView().deleteItems();
                getView().showUnShareResult(getView().getContext().getString(R.string.Tap3_ShareDevice_DeleteSucces));
            }else {
                getView().showUnShareResult("取消分享失败");
            }
        }
    }

}
