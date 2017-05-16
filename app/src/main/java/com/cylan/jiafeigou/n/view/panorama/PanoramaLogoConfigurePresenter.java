package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.module.BasePanoramaApiHelper;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.support.log.AppLogger;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by yanzhendong on 2017/3/15.
 */
public class PanoramaLogoConfigurePresenter extends BasePresenter<PanoramaLogoConfigureContact.View> implements PanoramaLogoConfigureContact.Presenter {

    private boolean httpApiInitFinish;
    private String baseUrl;

    @Override
    public void onStart() {
        super.onStart();
        checkAndInitLogoOption();
    }

    @Override
    protected void onRegisterSubscription() {
        super.onRegisterSubscription();
    }

    private void checkAndInitLogoOption() {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().getLogo()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.ret == 0) {//成功了
                        mView.onChangeLogoTypeSuccess(rsp.logtype);
                    } else {
                        // TODO: 2017/5/11 暂时不知道怎么处理
                    }
                }, e -> {
                    AppLogger.e(e);
                });
        registerSubscription(subscribe);
    }

    @Override
    public void changeLogoType(int position) {
        Subscription subscribe = BasePanoramaApiHelper.getInstance().setLogo(position)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rsp -> {
                    if (rsp.ret == 0) {
                        mView.onChangeLogoTypeSuccess(position);
                    } else {
                        mView.onChangeLogoTypeError(position);
                    }
                }, e -> {
                    AppLogger.e(e);
                });
        registerSubscription(subscribe);
    }
}
