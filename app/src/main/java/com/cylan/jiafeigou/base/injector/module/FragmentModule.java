package com.cylan.jiafeigou.base.injector.module;

import com.cylan.jiafeigou.base.injector.lifecycle.PerFragment;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellDetailContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellDetailSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeWonderfulPresenterImpl;
import com.cylan.jiafeigou.n.view.panorama.Pan720FullContract;
import com.cylan.jiafeigou.n.view.panorama.Pan720FullPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigurePresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@Module
public class FragmentModule {
    @Provides
    @PerFragment
    public BellDetailContract.Presenter provideBellDetailPresenter() {
        return new BellDetailSettingPresenterImpl();
    }

    @Provides
    @PerFragment
    public BellSettingContract.Presenter provideBellSettingPresenter() {
        return new BellSettingPresenterImpl();
    }

    @Provides
    @PerFragment
    public HomeWonderfulContract.Presenter provideHomeWonderfulPresenter() {
        return new HomeWonderfulPresenterImpl();
    }

    @Provides
    @PerFragment
    public Pan720FullContract.Presenter providePan720FullPresenter() {
        return new Pan720FullPresenter();
    }

    @Provides
    @PerFragment
    public PanoramaLogoConfigureContact.Presenter providePanoramaLogoConfigurePresenter() {
        return new PanoramaLogoConfigurePresenter();
    }
    @Provides
    @PerFragment
    public CamDelayRecordContract.Presenter provideCamDelayRecordPresenter() {
        return new CamDelayRecordContract.Presenter();
    }
}
