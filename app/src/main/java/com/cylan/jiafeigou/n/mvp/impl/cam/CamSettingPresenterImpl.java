package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.CamInfoBean;
import com.cylan.utils.RandomUtils;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamSettingPresenterImpl extends AbstractPresenter<CamSettingContract.View> implements
        CamSettingContract.Presenter {

    private Subscription subscription;

    private CamInfoBean camInfoBean;

    /**
     * 从主页传过来
     */
    private String cid;

    public CamSettingPresenterImpl(CamSettingContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }

    @Override
    public void fetchCamInfo(String cid) {
        subscription = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Object, CamInfoBean>() {
                    @Override
                    public CamInfoBean call(Object o) {
                        CamInfoBean camInfoBean = new CamInfoBean();
                        camInfoBean.isDeviceIndicator = RandomUtils.getRandom(2000) % 2 == 0;
                        camInfoBean.isMobileNet = RandomUtils.getRandom(100) % 3 == 0;
                        camInfoBean.isStandby = RandomUtils.getRandom(50) % 4 == 0;
                        camInfoBean.isSupport110V = RandomUtils.getRandom(20) % 2 == 0;
                        camInfoBean.ssid = "test";
                        CamInfoBean.Safe safe = new CamInfoBean.Safe();
                        safe.startTime = System.currentTimeMillis();
                        safe.endTime = System.currentTimeMillis() + RandomUtils.getRandom(3000) * 60 * 1000;
                        safe.isMotionDetect = RandomUtils.getRandom(10) % 2 == 0;
                        safe.sensitivity = RandomUtils.getRandom(3);
                        safe.warnNotification = 0x1a;
                        safe.repeatMode = RandomUtils.getRandom(128);
                        camInfoBean.safe = safe;
                        return camInfoBean;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CamInfoBean>() {
                    @Override
                    public void call(CamInfoBean camInfoBean) {
                        CamSettingPresenterImpl.this.camInfoBean = camInfoBean;
                        getView().onCamInfoRsp(camInfoBean);
                    }
                });
    }

    @Override
    public CamInfoBean getCamInfoBean() {
        if (camInfoBean == null)
            camInfoBean = new CamInfoBean();
        return camInfoBean;
    }

    @Override
    public void saveCamInfoBean(CamInfoBean camInfoBean) {
        this.camInfoBean = camInfoBean;
    }
}
