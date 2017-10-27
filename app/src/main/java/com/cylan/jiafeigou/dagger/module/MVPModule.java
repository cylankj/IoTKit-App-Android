package com.cylan.jiafeigou.dagger.module;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.dagger.annotation.ActivityScope;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellDetailContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeWonderfulContract;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareContentContract;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.n.mvp.contract.setting.AIRecognitionContact;
import com.cylan.jiafeigou.n.mvp.impl.CreateNewFacePresenter;
import com.cylan.jiafeigou.n.mvp.impl.FaceListPresenter;
import com.cylan.jiafeigou.n.mvp.impl.FaceManagerPresenter;
import com.cylan.jiafeigou.n.mvp.impl.SetFaceNamePresenter;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellDetailSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellLivePresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.BellSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.bell.DBellHomePresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeWonderfulPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareContentPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.record.DelayRecordPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.setting.AIRecognitionPresenter;
import com.cylan.jiafeigou.n.view.bell.BellDetailFragment;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.n.view.bell.BellSettingFragment;
import com.cylan.jiafeigou.n.view.bell.DoorBellHomeActivity;
import com.cylan.jiafeigou.n.view.cam.AIRecognitionFragment;
import com.cylan.jiafeigou.n.view.cam.CreateFaceContact;
import com.cylan.jiafeigou.n.view.cam.CreateNewFaceFragment;
import com.cylan.jiafeigou.n.view.cam.FaceListContact;
import com.cylan.jiafeigou.n.view.cam.FaceListFragment;
import com.cylan.jiafeigou.n.view.cam.FaceManagerContact;
import com.cylan.jiafeigou.n.view.cam.FaceManagerFragment;
import com.cylan.jiafeigou.n.view.cam.SetFaceNameContact;
import com.cylan.jiafeigou.n.view.cam.SetFaceNameFragment;
import com.cylan.jiafeigou.n.view.home.HomeWonderfulFragmentExt;
import com.cylan.jiafeigou.n.view.mine.HomeMineShareContentFragment;
import com.cylan.jiafeigou.n.view.panorama.LiveSettingActivity;
import com.cylan.jiafeigou.n.view.panorama.LiveSettingContact;
import com.cylan.jiafeigou.n.view.panorama.LiveSettingPresenter;
import com.cylan.jiafeigou.n.view.panorama.Pan720FullContract;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaAlbumPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaCameraActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaCameraContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaDetailActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaDetailContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaDetailPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureFragment;
import com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigurePresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSettingActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSettingContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSettingPresenter;
import com.cylan.jiafeigou.n.view.panorama.PanoramaShareContact;
import com.cylan.jiafeigou.n.view.panorama.PanoramaSharePresenter;
import com.cylan.jiafeigou.n.view.panorama.YouTubeLiveCreateContract;
import com.cylan.jiafeigou.n.view.panorama.YouTubeLiveCreateFragment;
import com.cylan.jiafeigou.n.view.panorama.YouTubeLiveCreatePresenter;
import com.cylan.jiafeigou.n.view.panorama.YouTubeLiveSetting;
import com.cylan.jiafeigou.n.view.panorama.YouTubeLiveSettingFragment;
import com.cylan.jiafeigou.n.view.panorama.YouTubeLiveSettingPresenter;
import com.cylan.jiafeigou.n.view.record.DelayRecordActivity;
import com.cylan.jiafeigou.n.view.record.DelayRecordMainFragment;
import com.cylan.jiafeigou.support.share.H5ShareEditorActivity;
import com.cylan.jiafeigou.support.share.ShareMediaActivity;
import com.cylan.jiafeigou.support.share.ShareMediaContact;
import com.cylan.jiafeigou.support.share.SharePresenter;
import com.cylan.jiafeigou.support.splash.SplashActivity;
import com.cylan.jiafeigou.support.splash.SplashContact;
import com.cylan.jiafeigou.support.splash.SplashPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by yanzhendong on 2017/10/26.
 */
@Module
public abstract class MVPModule {

    @Provides
    public static JFGPresenter providePresenter() {
        return null;
    }

    @Provides
    @ActivityScope
    public static BellLiveContract.Presenter provideBellLivePresenter(BellLiveActivity activity) {
        return new BellLivePresenterImpl(activity);
    }

    @Provides
    public static PanoramaAlbumContact.Presenter providePanoramaAlbumPresenter(PanoramaAlbumActivity activity) {
        return new PanoramaAlbumPresenter(activity);
    }

    @Provides
    public static PanoramaDetailContact.Presenter providePanoramaDetailPresenter(PanoramaDetailActivity activity) {
        return new PanoramaDetailPresenter(activity);
    }

    @Provides
    public static PanoramaSettingContact.Presenter providePanoramaSettingPresenter(PanoramaSettingActivity activity) {
        return new PanoramaSettingPresenter(activity);
    }

    @Provides
    public static DelayRecordContract.Presenter provideDelayRecordPresenter(DelayRecordActivity activity) {
        return new DelayRecordPresenterImpl(activity);
    }

    @Provides
    public static DoorBellHomeContract.Presenter provideDoorBellHomePresenter(DoorBellHomeActivity activity) {
        return new DBellHomePresenterImpl(activity);
    }

    @Provides
    public static ShareMediaContact.Presenter provideSharePresenter(ShareMediaActivity activity) {
        return new SharePresenter(activity);
    }

    @Provides
    public static SplashContact.Presenter provideSplashPresenter(SplashActivity activity) {
        return new SplashPresenter(activity);
    }

    @Provides
    public static PanoramaCameraContact.Presenter providePanoramaCameraPresenter(PanoramaCameraActivity activity) {
        return new PanoramaPresenter(activity);
    }

    @Provides
    public static PanoramaShareContact.Presenter providePanoramaSharePresenter(H5ShareEditorActivity activity) {
        return new PanoramaSharePresenter(activity);
    }

    @Provides
    public static LiveSettingContact.Presenter provideLiveSettingPresenter(LiveSettingActivity activity) {
        return new LiveSettingPresenter(activity);
    }

    @Provides
    public static BellDetailContract.Presenter provideBellDetailPresenter(BellDetailFragment fragment) {
        return new BellDetailSettingPresenterImpl(fragment);
    }

    @Provides
    public static BellSettingContract.Presenter provideBellSettingPresenter(BellSettingFragment fragment) {
        return new BellSettingPresenterImpl(fragment);
    }

    @Provides
    public static HomeWonderfulContract.Presenter provideHomeWonderfulPresenter(HomeWonderfulFragmentExt fragmentExt) {
        return new HomeWonderfulPresenterImpl(fragmentExt);
    }

    @Provides
    public static PanoramaLogoConfigureContact.Presenter providePanoramaLogoConfigurePresenter(PanoramaLogoConfigureFragment fragment) {
        return new PanoramaLogoConfigurePresenter(fragment);
    }

    @Provides
    public static CamDelayRecordContract.Presenter provideCamDelayRecordPresenter(DelayRecordMainFragment fragment) {
        return new CamDelayRecordContract.Presenter(fragment);
    }

    @Provides
    public static MineShareContentContract.Presenter provideShareContentPresenter(HomeMineShareContentFragment fragment) {
        return new MineShareContentPresenterImpl(fragment);
    }

    @Provides
    public static AIRecognitionContact.Presenter provideAIRecognitionPresenter(AIRecognitionFragment fragment) {
        return new AIRecognitionPresenter(fragment);
    }

    @Provides
    public static YouTubeLiveSetting.Presenter provideYoutubeLiveSettingPresenter(YouTubeLiveSettingFragment fragment) {
        return new YouTubeLiveSettingPresenter(fragment);
    }

    @Provides
    public static YouTubeLiveCreateContract.Presenter provideYoutubeLiveCreatePresenter(YouTubeLiveCreateFragment fragment) {
        return new YouTubeLiveCreatePresenter(fragment);
    }

    @Provides
    public static SetFaceNameContact.Presenter provideSetFaceNamePresenter(SetFaceNameFragment faceNameFragment) {
        return new SetFaceNamePresenter(faceNameFragment);
    }

    @Provides
    public static FaceManagerContact.Presenter provideFaceManagerPresenter(FaceManagerFragment fragment) {
        return new FaceManagerPresenter(fragment);
    }

    @Provides
    public static CreateFaceContact.Presenter provideNewFacePresenter(CreateNewFaceFragment faceFragment) {
        return new CreateNewFacePresenter(faceFragment);
    }

    @Provides
    public static FaceListContact.Presenter provideFaceListPresenter(FaceListFragment fragment) {
        return new FaceListPresenter(fragment);
    }

    @Provides
    public static Pan720FullContract.Presenter providePan720FullPresenter() {
        return null;
    }
}
