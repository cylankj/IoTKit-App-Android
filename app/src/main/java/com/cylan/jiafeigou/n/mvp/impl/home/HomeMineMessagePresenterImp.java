package com.cylan.jiafeigou.n.mvp.impl.home;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineMessageContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeMineMessagePresenterImp extends AbstractPresenter<HomeMineMessageContract.View> implements HomeMineMessageContract.Presenter {

    private ArrayList<MineMessageBean> list;

    private CompositeSubscription compositeSubscription;

    private DbManager dbManager;

    public HomeMineMessagePresenterImp(HomeMineMessageContract.View view,ArrayList<MineMessageBean> list) {
        super(view);
        view.setPresenter(this);
        this.list = list;
    }

    @Override
    public void start() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getAccount());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        }
    }


    /**
     * 加载消息数据
     */
    @Override
    public void initMesgData() {

        if (list == null){
            list = new ArrayList<MineMessageBean>();
        }

        list.clear();

//        MineMessageBean emMessage = new MineMessageBean("亲爱的用户,客户端将进行系统维护升级,期间对设备正常使用将会造成一定影响，对您造成的不便之处敬请谅解。再次感谢您对加菲狗的支持！", 1, System.currentTimeMillis() + "");
//        MineMessageBean emMessage2 = new MineMessageBean("设备已被删除", 1, System.currentTimeMillis() + "");
//
//        list.add(emMessage);
//        list.add(emMessage2);

        list.addAll(findAllFromDb());

        handlerDataResult(list);

    }

    /**
     * 处理数据的显示
     *
     * @param list
     */
    private void handlerDataResult(ArrayList<MineMessageBean> list) {
        if (getView() != null) {
            if (list.size() != 0) {
                getView().hideNoMesgView();
                getView().initRecycleView(list);
            } else {
                getView().showNoMesgView();
            }
        }
    }

    /**
     * 拿到数据库的操作对象
     *
     * @return
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(JFGAccount.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<JFGAccount>() {
                    @Override
                    public void call(JFGAccount account) {
                        if (account != null && account instanceof JFGAccount) {
                            // 加载数据库数据
                            dbManager = DataBaseUtil.getInstance(account.getAccount()).dbManager;
                            initMesgData();
                        }
                    }
                });
    }

    /**
     * 获取到本地数据库中的所有消息记录
     *
     * @return
     */
    @Override
    public List<MineMessageBean> findAllFromDb() {
        List<MineMessageBean> tempList = new ArrayList<>();
        if (dbManager != null) {
            try {
                List<MineMessageBean> allData = dbManager.findAll(MineMessageBean.class);
                if (allData != null) {
                    tempList.addAll(allData);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        return tempList;
    }

    /**
     * 清空本地消息记录
     */
    @Override
    public void clearRecoard() {
        try {
            dbManager.delete(MineMessageBean.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息保存到数据库
     *
     * @param bean
     */
    @Override
    public void saveIntoDb(MineMessageBean bean) {
        try {
            dbManager.save(bean);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

}
