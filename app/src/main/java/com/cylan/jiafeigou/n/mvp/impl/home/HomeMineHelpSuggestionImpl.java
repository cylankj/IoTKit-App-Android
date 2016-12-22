package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.entity.jniCall.JFGFeedbackInfo;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineHelpSuggestionContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/22 11:05
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineHelpSuggestionImpl extends AbstractPresenter<HomeMineHelpSuggestionContract.View>
        implements HomeMineHelpSuggestionContract.Presenter {

    private CompositeSubscription compositeSubscription;
    private ArrayList<MineHelpSuggestionBean> list;
    private DbManager dbManager;
    private JFGAccount userInfomation;

    public HomeMineHelpSuggestionImpl(HomeMineHelpSuggestionContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getAccountInfo());
            compositeSubscription.add(getSystemAutoReplyCallBack());
        }
    }

    @Override
    public void stop() {
        unSubscribe(compositeSubscription);
    }

    /**
     * 获取到列表的数据
     */
    @Override
    public void initData() {
        rx.Observable.just(null)
                .flatMap(new Func1<Object, Observable<ArrayList<MineHelpSuggestionBean>>>() {
                    @Override
                    public Observable<ArrayList<MineHelpSuggestionBean>> call(Object o) {
                        ArrayList<MineHelpSuggestionBean> tempList = new ArrayList<MineHelpSuggestionBean>();
                        if (dbManager == null) {
                            return Observable.just(tempList);
                        }
                        try {
                            List<MineHelpSuggestionBean> list = dbManager.findAll(MineHelpSuggestionBean.class);
                            if (list != null && list.size() != 0) {
                                tempList.addAll(list);
                            }
                        } catch (DbException e) {
                            e.printStackTrace();
                            return Observable.just(tempList);
                        }
                        return Observable.just(tempList);
                    }
                })
                .subscribe(new Action1<ArrayList<MineHelpSuggestionBean>>() {
                    @Override
                    public void call(ArrayList<MineHelpSuggestionBean> list) {
                        if (getView() != null) {
                            getView().initRecycleView(list);
                        }
                    }
                });
    }

    /**
     * 清空所有会话
     */
    @Override
    public void onClearAllTalk() {
        try {
            dbManager.delete(MineHelpSuggestionBean.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拿到数据库对象
     */
    @Override
    public Subscription getAccountInfo() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo userInfo) {
                        if (userInfo != null && userInfo instanceof RxEvent.GetUserInfo) {
                            userInfomation = userInfo.jfgAccount;
                            dbManager = DataBaseUtil.getInstance(userInfo.jfgAccount.getAccount()).dbManager;
                            initData();
                        }
                    }
                });
    }

    /**
     * 保存到本地数据库
     *
     * @param bean
     */
    @Override
    public void saveIntoDb(MineHelpSuggestionBean bean) {
        try {
            dbManager.save(bean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取到用户的头像地址
     */
    @Override
    public String getUserPhotoUrl() {
        if (userInfomation == null) {
            return "";
        } else {
            return userInfomation.getPhotoUrl();
        }
    }

    /**
     * 检测是否超时5分钟
     *
     * @return
     */
    @Override
    public boolean checkOverTime(String time) {
        long lastItemTime = Long.parseLong(time);
        if (System.currentTimeMillis() - lastItemTime > 5 * 60 * 1000) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检测是否超过20s
     *
     * @param time
     * @return
     */
    @Override
    public boolean checkOver20Min(String time) {
        long lastItemTime = Long.parseLong(time);
        if (System.currentTimeMillis() - lastItemTime > 2 * 60 * 1000) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 上传意见反馈
     */
    @Override
    public void sendFeedBack(MineHelpSuggestionBean bean) {
        rx.Observable.just(bean)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<MineHelpSuggestionBean>() {
                    @Override
                    public void call(MineHelpSuggestionBean bean) {
                        JfgCmdInsurance.getCmd().sendFeedback(Long.parseLong(bean.getDate()), bean.getText(), true);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.d("sendFeedBack" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 获取系统的自动回复
     */
    @Override
    public void getSystemAutoReply() {
        rx.Observable.just(null)
            .subscribeOn(Schedulers.newThread())
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object o) {
//                    JfgCmdInsurance.getCmd().getFeedbackList();
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    AppLogger.e("getSystemAutoReply"+throwable.getLocalizedMessage());
                }
            });
    }

    /**
     * 获取系统自动回复的回调
     * @return
     */
    @Override
    public Subscription getSystemAutoReplyCallBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.GetFeedBackRsp.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetFeedBackRsp>() {
                    @Override
                    public void call(RxEvent.GetFeedBackRsp getFeedBackRsp) {
                        if (getFeedBackRsp != null && getFeedBackRsp instanceof RxEvent.GetFeedBackRsp){
                            if(getView() != null && getFeedBackRsp.arrayList.size() != 0){
                                JFGFeedbackInfo jfgFeedbackInfo = getFeedBackRsp.arrayList.get(0);
                                getView().addSystemAutoReply(jfgFeedbackInfo.time,jfgFeedbackInfo.msg);
                            }
                        }
                    }
                });
    }

}
