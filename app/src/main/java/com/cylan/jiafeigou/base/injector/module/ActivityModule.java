package com.cylan.jiafeigou.base.injector.module;

import com.cylan.jiafeigou.base.injector.lifecycle.PerActivity;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellLivePresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.DBellHomePresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.record.DelayRecordPresenterImpl;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaDetailContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSettingContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSettingPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@Module
@PerActivity
public class ActivityModule {

    @Provides
    @PerActivity
    public BellLiveContract.Presenter provideBellLivePresenter() {
        return new BellLivePresenterImpl();
    }

    @Provides
    @PerActivity
    public PanoramaCameraContact.Presenter providePanoramaCameraPresenter() {
        return new PanoramaPresenter();
    }

    @Provides
    @PerActivity
    public PanoramaAlbumContact.Presenter providePanoramaAlbumPresenter() {
        return new PanoramaAlbumPresenter();
    }

    @Provides
    @PerActivity
    public PanoramaDetailContact.Presenter providePanoramaDetailPresenter() {
        return null;
    }

    @Provides
    @PerActivity
    public PanoramaSettingContact.Presenter providePanoramaSettingPresenter() {
        return new PanoramaSettingPresenter();
    }

    @Provides
    @PerActivity
    public DelayRecordContract.Presenter provideDelayRecordPresenter() {
        return new DelayRecordPresenterImpl();
    }

    @Provides
    @PerActivity
    public DoorBellHomeContract.Presenter provideDoorBellHomePresenter() {
        return new DBellHomePresenterImpl();
    }
}
