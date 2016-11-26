package com.cylan.jiafeigou.n.mvp.impl.cam;

import com.cylan.jiafeigou.n.mvp.contract.cam.CamInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

/**
 * Created by cylan-hunt on 16-11-25.
 */

public class CamInfoPresenterImpl extends AbstractPresenter<CamInfoContract.View>
        implements CamInfoContract.Presenter {

    private BeanCamInfo beanCamInfo;

    public CamInfoPresenterImpl(CamInfoContract.View view, BeanCamInfo info) {
        super(view);
        this.beanCamInfo = info;
        view.setPresenter(this);
    }

    @Override
    public void updateInfo(BeanCamInfo info, int... ids) {
        AppLogger.i("update camInfo: " + new Gson().toJson(info));
    }

    @Override
    public BeanCamInfo getBeanCamInfo() {
        if (this.beanCamInfo == null) {
            AppLogger.e("should not happen");
        }
        return beanCamInfo;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
