package com.cylan.jiafeigou.n.mvp.impl.bind;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.SetDeviceAliasContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-11-29.
 */

public class SetDeviceAliasPresenterImpl extends AbstractPresenter<SetDeviceAliasContract.View>
        implements SetDeviceAliasContract.Presenter {
    private UdpConstant.UdpDevicePortrait portrait;

    public SetDeviceAliasPresenterImpl(SetDeviceAliasContract.View view,
                                       UdpConstant.UdpDevicePortrait portrait) {
        super(view);
        view.setPresenter(this);
        this.portrait = portrait;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void setupAlias(String alias) {
        Observable.just(alias)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        AppLogger.i("setup alias: " + portrait);
                        if (portrait != null)
                            try {
                                JfgCmdInsurance.getCmd().setAliasByCid(portrait.uuid, s);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                        return s;
                    }
                })
                .delay(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        getView().setupAliasDone();
                    }
                });
    }
}
