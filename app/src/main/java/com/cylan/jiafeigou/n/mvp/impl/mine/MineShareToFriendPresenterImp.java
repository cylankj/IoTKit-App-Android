package com.cylan.jiafeigou.n.mvp.impl.mine;

import com.cylan.entity.JfgEnum;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PackageUtils;

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
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToFriendPresenterImp extends AbstractPresenter<MineShareToFriendContract.View>
        implements MineShareToFriendContract.Presenter {

    private CompositeSubscription compositeSubscription;

    private ArrayList<RxEvent.ShareDeviceCallBack> callbackList = new ArrayList();

    private int ShareTotalFriend;

    public MineShareToFriendPresenterImp(MineShareToFriendContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getAllShareFriendCallBack());
            compositeSubscription.add(shareDeviceCallBack());
        }

    }

    @Override
    public void stop() {
        super.stop();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }

    /**
     * 发送分享给亲友请求
     */
    @Override
    public void sendShareToFriendReq(final String cid, ArrayList<RelAndFriendBean> list) {
        if (getView() != null) {
            getView().showSendProgress();
        }
        ShareTotalFriend = list.size();
        rx.Observable.just(list)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<ArrayList<RelAndFriendBean>>() {
                    @Override
                    public void call(ArrayList<RelAndFriendBean> list) {
                        for (RelAndFriendBean bean : list) {
                            try {
                                JfgCmdInsurance.getCmd().shareDevice(cid, bean.account);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("sendShareToFriendReq" + throwable.getLocalizedMessage());
                    }
                });
    }

    @Override
    public boolean checkNetConnetion() {
        //TODO 检测是否有网络
        return true;
    }

    /**
     * 判断分享人数是否已超过5人
     *
     * @param number
     * @return
     */
    @Override
    public void checkShareNumIsOver(SuperViewHolder holder, boolean isChange, int number) {
        if (number > 5 && getView() != null) {
            getView().showNumIsOverDialog(holder);
        } else {
            getView().setHasShareFriendNum(isChange, number);
        }
    }

    /**
     * 获取到未分享的亲友
     */
    @Override
    public void getAllShareFriend(String cid) {
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
                        AppLogger.e("getAllShareFriend" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取到未分享亲友的回调
     *
     * @return
     */
    @Override
    public Subscription getAllShareFriendCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetHasShareFriendCallBack.class)
                .flatMap(new Func1<RxEvent.GetHasShareFriendCallBack, Observable<ArrayList<RelAndFriendBean>>>() {
                    @Override
                    public Observable<ArrayList<RelAndFriendBean>> call(RxEvent.GetHasShareFriendCallBack getFriendList) {
                        if (getFriendList != null && getFriendList instanceof RxEvent.GetHasShareFriendCallBack) {
                            if (getFriendList.i == 0 && getFriendList.arrayList.size() != 0) {
                                return Observable.just(converData(getFriendList.arrayList));
                            } else {
                                return Observable.just(null);
                            }
                        } else {
                            return Observable.just(null);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<RelAndFriendBean>>() {
                    @Override
                    public void call(ArrayList<RelAndFriendBean> list) {
                        if (list != null) {
                            handlerDataResult(list);
                        } else {
                            getView().showNoFriendNullView();
                        }
                    }
                });
    }

    /**
     * 分享设备的回调
     *
     * @return
     */
    @Override
    public Subscription shareDeviceCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ShareDeviceCallBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ShareDeviceCallBack>() {
                    @Override
                    public void call(RxEvent.ShareDeviceCallBack shareDeviceCallBack) {
                        if (shareDeviceCallBack != null && shareDeviceCallBack instanceof RxEvent.ShareDeviceCallBack) {
                            callbackList.add(shareDeviceCallBack);
                        }

                        if (callbackList.size() == ShareTotalFriend) {
                            if (getView() != null) {
                                getView().handlerAfterSendShareReq(callbackList);
                            }
                        }
                    }
                });
    }

    /**
     * 数据的转换
     *
     * @param friendList
     * @return
     */
    private ArrayList<RelAndFriendBean> converData(ArrayList<JFGFriendAccount> friendList) {
        ArrayList<RelAndFriendBean> list = new ArrayList<>();
        for (JFGFriendAccount friendAccount : friendList) {
            RelAndFriendBean bean = new RelAndFriendBean();
            bean.account = friendAccount.account;
            bean.alias = friendAccount.alias;
            bean.markName = friendAccount.markName;
            try {
                bean.iconUrl = JfgCmdInsurance.getCmd().getCloudUrlByType(JfgEnum.JFG_URL.PORTRAIT, 0, friendAccount.account + ".jpg", "", PackageUtils.getMetaString(ContextUtils.getContext(), "vid"));
            } catch (JfgException e) {
                e.printStackTrace();
            }
            list.add(bean);
        }
        return list;
    }

    /**
     * 处理返回数据
     *
     * @param relAndFriendBeen
     */
    private void handlerDataResult(ArrayList<RelAndFriendBean> relAndFriendBeen) {
        if (relAndFriendBeen != null) {
            if (getView() != null && relAndFriendBeen.size() != 0) {
                getView().initRecycleView(relAndFriendBeen);
            } else {
                getView().showNoFriendNullView();
            }
        } else {
            getView().showNoFriendNullView();
        }
    }

}
