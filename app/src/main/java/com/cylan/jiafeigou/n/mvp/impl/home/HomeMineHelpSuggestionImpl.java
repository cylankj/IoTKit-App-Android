package com.cylan.jiafeigou.n.mvp.impl.home;

import android.content.Context;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineHelpSuggestionContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

    Subscription subscription;
    private ArrayList<MineHelpSuggestionBean> list;
    private DbManager dbManager;

    private HomeMineHelpSuggestionImpl(HomeMineHelpSuggestionContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    private ArrayList<MineHelpSuggestionBean> testData() {
        list = new ArrayList<>();
        String server = "亲爱的用户,客户端将于2016年4月1日23:00至00:00进行系统维护升级," +
                "期间对设备正常使用将会造成一定影响,对您造成的不便之处敬请谅解。再次感谢您对加菲狗的支持！";
        String client = "希望你们会做视频下载功能，非常实用呢。";
        for (int i = 0; i < 2; i++) {
            MineHelpSuggestionBean bean = new MineHelpSuggestionBean();
            if (i == 0) {
                bean.setType(0);
                bean.setText(server);
                bean.setIsShowTime(true);
                list.add(bean);
            } else {
                bean.setType(1);
                bean.setText(client);
                bean.setIsShowTime(true);
                list.add(bean);
            }
        }
        return list;
    }

    @Override
    public void start() {
        subscription = Observable.just(null)
                .subscribeOn(Schedulers.io())
                .map(new Func1<Object, ArrayList<MineHelpSuggestionBean>>() {
                    @Override
                    public ArrayList<MineHelpSuggestionBean> call(Object o) {
                        return testData();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<MineHelpSuggestionBean>>() {
                    @Override
                    public void call(ArrayList<MineHelpSuggestionBean> beanArrayList) {

                    }
                });
    }



    @Override
    public void stop() {
        unSubscribe(subscription);
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
                        if (dbManager == null){
                            return Observable.just(tempList);
                        }
                        try {
                            tempList.addAll(dbManager.findAll(MineHelpSuggestionBean.class));
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
                        if (getView() != null){
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
                        if (userInfo != null && userInfo instanceof RxEvent.GetUserInfo){
                            dbManager = DataBaseUtil.getInstance(userInfo.jfgAccount.getAccount()).dbManager;
                        }
                    }
                });
    }
}
