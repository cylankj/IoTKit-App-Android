package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetAliasContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineInfoSetNamePresenterImpl extends AbstractPresenter<MineInfoSetAliasContract.View> implements MineInfoSetAliasContract.Presenter {


    public MineInfoSetNamePresenterImpl(MineInfoSetAliasContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    /**
     * 发送保存昵称的请求
     */
    @Override
    public void saveName(final String newAlias) {
        if (getView() != null) {
            getView().showSendHint();
        }
        addSubscription(Observable.just(newAlias)
                .subscribeOn(Schedulers.io())
                .map(s -> {
                    JFGAccount jfgAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                    jfgAccount.resetFlag();
                    jfgAccount.setAlias(s);
                    try {
                        BaseApplication.getAppComponent().getCmd().setAccount(jfgAccount);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .flatMap(o -> RxBus.getCacheInstance().toObservableSticky(RxEvent.AccountArrived.class))
                .filter(ret -> mView != null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> getView().handlerResult(ret), AppLogger::e), "saveName");
    }

    @Override
    public boolean isEditEmpty(String string) {
        return TextUtils.isEmpty(string);
    }


    @Override
    public void onNetworkChanged(Context context, Intent intent) {
        super.onNetworkChanged(context, intent);
        if (mView != null) {
            mView.onNetStateChanged(NetUtils.getJfgNetType());
        }
    }

    @Override
    protected String[] registerNetworkAction() {
        return new String[]{ConnectivityManager.CONNECTIVITY_ACTION,
                WifiManager.NETWORK_STATE_CHANGED_ACTION};
    }

}
