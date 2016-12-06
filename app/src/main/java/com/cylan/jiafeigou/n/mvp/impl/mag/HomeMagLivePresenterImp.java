package com.cylan.jiafeigou.n.mvp.impl.mag;

import android.text.TextUtils;
import android.util.Pair;

import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.db.DataBaseUtil;
import com.cylan.jiafeigou.n.mvp.contract.mag.HomeMagLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.BeanMagInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.db.DbManager;
import com.cylan.jiafeigou.support.db.ex.DbException;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2016/9/19
 * 描述：
 */
public class HomeMagLivePresenterImp extends AbstractPresenter<HomeMagLiveContract.View> implements HomeMagLiveContract.Presenter {

    private boolean isChick = false;
    private DbManager dbManager;
    private CompositeSubscription compositeSubscription;
    private BeanMagInfo magInfoBean;
    private DeviceBean bean;

    public HomeMagLivePresenterImp(HomeMagLiveContract.View view,DeviceBean bean) {
        super(view);
        view.setPresenter(this);
        this.bean = bean;
    }

    /**
     * 填充数据
     * @param bean
     */
    private void fillData(DeviceBean bean) {
        magInfoBean = new BeanMagInfo();
        BaseBean baseBean = new BaseBean();
        baseBean.alias = bean.alias;
        baseBean.pid = bean.pid;
        baseBean.uuid = bean.uuid;
        baseBean.sn = bean.sn;
        magInfoBean.convert(baseBean, bean.dataList);
        getView().onMagInfoRsp(magInfoBean);
    }

    @Override
    public void start() {
        fillData(bean);
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(getAccount());
        }
    }

    @Override
    public void stop() {
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()){
            compositeSubscription.unsubscribe();
        }
    }

    @Override
    public void clearOpenAndCloseRecord() {
        try {
            if (dbManager.findAll(MagBean.class).size() == 0){
                getView().showNoMesg();
                return;
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        if (getView() != null){
            getView().showClearProgress();
        }

        rx.Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .delay(2000, TimeUnit.MILLISECONDS)
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        try {
                            dbManager.delete(MagBean.class);
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        getView().hideClearProgress();
                    }
                });
    }

    @Override
    public boolean getNegation() {
        isChick = !isChick;
        return isChick;
    }

    @Override
    public void saveSwitchState(boolean isChick, String key) {
        PreferencesUtils.putBoolean(key, isChick);
    }

    @Override
    public boolean getSwitchState(String key) {
        return PreferencesUtils.getBoolean(key);
    }

    /**
     * 拿到用户对象获取数据库
     */
    @Override
    public Subscription getAccount() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.GetUserInfo.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.GetUserInfo>() {
                    @Override
                    public void call(RxEvent.GetUserInfo getUserInfo) {
                        if (getUserInfo != null && getUserInfo instanceof RxEvent.GetUserInfo){
                            if (dbManager == null)
                            dbManager = DataBaseUtil.getInstance(getUserInfo.jfgAccount.getAccount()).dbManager;
                        }
                    }
                });
    }

    /**
     * 拿到门磁信息
     * @return
     */
    @Override
    public BeanMagInfo getMagInfoBean() {
        if (magInfoBean == null)
            magInfoBean = new BeanMagInfo();
        return magInfoBean;
    }

    /**
     * 获取到设备是名字
     * @return
     */
    @Override
    public String getDeviceName() {
        return TextUtils.isEmpty(magInfoBean.deviceBase.alias) ?
                magInfoBean.deviceBase.uuid : magInfoBean.deviceBase.alias;
    }

    /**
     * 保存设备信息
     * @param magInfoBean
     * @param id
     */
    @Override
    public void saveMagInfoBean(final BeanMagInfo magInfoBean, int id) {
        this.magInfoBean = magInfoBean;
        Observable.just(new Pair<>(magInfoBean, id))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<BeanMagInfo, Integer>>() {
                    @Override
                    public void call(Pair<BeanMagInfo, Integer> beanMagInfoIntegerPair) {
                        int id = beanMagInfoIntegerPair.second;
                        RxEvent.JFGAttributeUpdate update = new RxEvent.JFGAttributeUpdate();
                        update.uuid = magInfoBean.deviceBase.uuid;
                        update.o = beanMagInfoIntegerPair.first.getObject(id);
                        update.msgId = id;
                        update.version = System.currentTimeMillis();
                        RxBus.getCacheInstance().post(update);
                        JfgCmdInsurance.getCmd().robotSetData(magInfoBean.deviceBase.uuid,
                                DpUtils.getList(id,
                                        beanMagInfoIntegerPair.first.getByte(id)
                                        , System.currentTimeMillis()));
                        AppLogger.i("save bean Cam info");
                    }
                });
    }

}
