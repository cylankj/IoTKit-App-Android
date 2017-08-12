package com.cylan.jiafeigou.base.injector.component;

import com.cylan.jiafeigou.base.injector.lifecycle.PerActivity;
import com.cylan.jiafeigou.base.injector.module.ActivityModule;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.n.view.bell.BellRecordDetailActivity;
import com.cylan.jiafeigou.n.view.bell.DoorBellHomeActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaCameraActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaDetailActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSettingActivity;
import com.cylan.jiafeigou.n.view.record.DelayRecordActivity;
import com.cylan.jiafeigou.support.login.LoginActivity;
import com.cylan.jiafeigou.support.share.H5ShareEditorActivity;
import com.cylan.jiafeigou.support.share.ShareMediaActivity;
import com.cylan.jiafeigou.support.splash.SplashActivity;

import dagger.Component;

/**
 * Created by yanzhendong on 2017/4/12.
 */
@PerActivity
@Component(dependencies = AppComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(BellLiveActivity activity);

//    void inject(PanoramaCameraFragment activity);

    void inject(PanoramaAlbumActivity activity);

    void inject(PanoramaDetailActivity activity);

    void inject(PanoramaSettingActivity activity);

    void inject(DelayRecordActivity activity);

    void inject(BellRecordDetailActivity activity);

    void inject(DoorBellHomeActivity activity);

    void inject(ShareMediaActivity activity);

    void inject(SplashActivity activity);

    void inject(LoginActivity activity);

    void inject(PanoramaCameraActivity activity);

    void inject(H5ShareEditorActivity fragment);
}
