package com.cylan.jiafeigou.dagger.module;

import com.cylan.jiafeigou.base.wrapper.DaggerActivity;
import com.cylan.jiafeigou.dagger.annotation.ActivityScope;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.n.view.bell.BellRecordDetailActivity;
import com.cylan.jiafeigou.n.view.bell.DoorBellHomeActivity;
import com.cylan.jiafeigou.n.view.bind.WireBindActivity;
import com.cylan.jiafeigou.n.view.mine.ShareContentWebH5Activity;
import com.cylan.jiafeigou.n.view.panorama.LiveSettingActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaCameraActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaDetailActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSettingActivity;
import com.cylan.jiafeigou.n.view.record.DelayRecordActivity;
import com.cylan.jiafeigou.support.share.H5ShareEditorActivity;
import com.cylan.jiafeigou.support.share.ShareMediaActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by yanzhendong on 2017/10/26.
 */

@Module
public abstract class ActivityModule {
    @ActivityScope
    @ContributesAndroidInjector
    public abstract DaggerActivity daggerActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract BellLiveActivity bellLiveActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract PanoramaAlbumActivity panoramaAlbumActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract PanoramaDetailActivity panoramaDetailActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract PanoramaSettingActivity panoramaSettingActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract DelayRecordActivity delayRecordActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract BellRecordDetailActivity bellRecordDetailActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract DoorBellHomeActivity doorBellHomeActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract ShareMediaActivity shareMediaActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract PanoramaCameraActivity panoramaCameraActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract H5ShareEditorActivity h5ShareEditorActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract LiveSettingActivity liveSettingActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract ShareContentWebH5Activity shareContentWebH5Activity();

    @ActivityScope
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract WireBindActivity wireBindActivity();
}
