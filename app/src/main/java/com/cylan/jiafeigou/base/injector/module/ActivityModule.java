package com.cylan.jiafeigou.base.injector.module;

import com.cylan.jiafeigou.base.injector.lifecycle.PerActivity;
import com.cylan.jiafeigou.base.module.BasePresenterInjector;
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
import com.cylan.jiafeigou.n.view.panorama.PanoramaDetailPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSettingContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSettingPresenter;
import com.cylan.jiafeigou.support.share.ShareMediaContact;
import com.cylan.jiafeigou.support.share.SharePresenter;

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
    public static BellLiveContract.Presenter provideBellLivePresenter(BasePresenterInjector injector) {
        return injector.inject(new BellLivePresenterImpl());
    }

    @Provides
    @PerActivity
    public static PanoramaCameraContact.Presenter providePanoramaCameraPresenter(BasePresenterInjector injector) {
        PanoramaPresenter presenter = injector.inject(new PanoramaPresenter());
        return presenter;
    }

    @Provides
    @PerActivity
    public static PanoramaAlbumContact.Presenter providePanoramaAlbumPresenter(BasePresenterInjector injector) {
        return injector.inject(new PanoramaAlbumPresenter());
    }

    @Provides
    @PerActivity
    public static PanoramaDetailContact.Presenter providePanoramaDetailPresenter(BasePresenterInjector injector) {
        return injector.inject(new PanoramaDetailPresenter());
    }

    @Provides
    @PerActivity
    public static PanoramaSettingContact.Presenter providePanoramaSettingPresenter(BasePresenterInjector injector) {
        return injector.inject(new PanoramaSettingPresenter());
    }

    @Provides
    @PerActivity
    public static DelayRecordContract.Presenter provideDelayRecordPresenter(BasePresenterInjector injector) {
        return injector.inject(new DelayRecordPresenterImpl());
    }

    @Provides
    @PerActivity
    public static DoorBellHomeContract.Presenter provideDoorBellHomePresenter(BasePresenterInjector injector) {
        return injector.inject(new DBellHomePresenterImpl());
    }

    @Provides
    @PerActivity
    public static ShareMediaContact.Presenter provideSharePresenter(BasePresenterInjector injector) {
        return injector.inject(new SharePresenter());
    }
}
