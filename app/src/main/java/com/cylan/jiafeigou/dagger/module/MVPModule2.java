package com.cylan.jiafeigou.dagger.module;

import android.app.Activity;
import android.content.Context;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellDetailContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellLiveContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.BellSettingContract;
import com.cylan.jiafeigou.n.mvp.contract.bell.DoorBellHomeContract;
import com.cylan.jiafeigou.n.mvp.contract.bind.SelectCidContract;
import com.cylan.jiafeigou.n.mvp.contract.bind.WireBindContract;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamDelayRecordContract;
import com.cylan.jiafeigou.n.mvp.contract.cam.DoorPassWordSettingContact;
import com.cylan.jiafeigou.n.mvp.contract.cam.FaceSettingContract;
import com.cylan.jiafeigou.n.mvp.contract.cam.MonitorAreaSettingContact;
import com.cylan.jiafeigou.n.mvp.contract.cam.RegisterFaceContract;
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
import com.cylan.jiafeigou.n.mvp.impl.bind.SelectCidPresenter;
import com.cylan.jiafeigou.n.mvp.impl.bind.WireBindPresenter;
import com.cylan.jiafeigou.n.mvp.impl.cam.DoorPasswordSettingPresenter;
import com.cylan.jiafeigou.n.mvp.impl.cam.FaceSettingPresenter;
import com.cylan.jiafeigou.n.mvp.impl.cam.MonitorAreaSettingPresenter;
import com.cylan.jiafeigou.n.mvp.impl.cam.RegisterFacePresenter;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeWonderfulPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareContentPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.record.DelayRecordPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.setting.AIRecognitionPresenter;
import com.cylan.jiafeigou.n.view.bell.BellDetailFragment;
import com.cylan.jiafeigou.n.view.bell.BellLiveActivity;
import com.cylan.jiafeigou.n.view.bell.BellSettingFragment;
import com.cylan.jiafeigou.n.view.bell.DoorBellHomeActivity;
import com.cylan.jiafeigou.n.view.bind.SelectCidFragment;
import com.cylan.jiafeigou.n.view.bind.WireBindActivity;
import com.cylan.jiafeigou.n.view.cam.AIRecognitionFragment;
import com.cylan.jiafeigou.n.view.cam.CreateFaceContact;
import com.cylan.jiafeigou.n.view.cam.CreateNewFaceFragment;
import com.cylan.jiafeigou.n.view.cam.DoorPassWordSettingFragment;
import com.cylan.jiafeigou.n.view.cam.FaceListContact;
import com.cylan.jiafeigou.n.view.cam.FaceListFragment;
import com.cylan.jiafeigou.n.view.cam.FaceManagerContact;
import com.cylan.jiafeigou.n.view.cam.FaceManagerFragment;
import com.cylan.jiafeigou.n.view.cam.FaceSettingFragment;
import com.cylan.jiafeigou.n.view.cam.MonitorAreaSettingFragment;
import com.cylan.jiafeigou.n.view.cam.RegisterFaceActivity;
import com.cylan.jiafeigou.n.view.cam.SetFaceNameContact;
import com.cylan.jiafeigou.n.view.cam.SetFaceNameFragment;
import com.cylan.jiafeigou.n.view.home.HomeWonderfulFragmentExt;
import com.cylan.jiafeigou.n.view.mine.HomeMineShareContentFragment;
import com.cylan.jiafeigou.n.view.panorama.LiveSettingActivity;
import com.cylan.jiafeigou.n.view.panorama.LiveSettingContact;
import com.cylan.jiafeigou.n.view.panorama.LiveSettingPresenter;
import com.cylan.jiafeigou.n.view.panorama.Pan720FullContract;
import com.cylan.jiafeigou.n.view.panorama.Pan720FullFragment;
import com.cylan.jiafeigou.n.view.panorama.Pan720FullPresenter;
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
import com.cylan.jiafeigou.utils.ContextUtils;

import javax.inject.Inject;

import dagger.Binds;
import dagger.Module;

/**
 * Created by yanzhendong on 2017/10/26.
 */
@Module
public abstract class MVPModule2 {

    public static class DefaultPresenter extends BasePresenter implements JFGPresenter {
        @Inject
        public DefaultPresenter(JFGView view) {
            super(view);

        }
    }

    public static class DefaultView implements JFGView {
        @Inject
        public DefaultView() {
        }

        @Override
        public Activity activity() {
            return null;
        }

        @Override
        public Context getContext() {
            return ContextUtils.getContext();
        }

        @Override
        public String uuid() {
            return "404 not found";
        }

        @Override
        public void onLoginStateChanged(boolean online) {

        }
    }

    @Binds
    public abstract JFGPresenter providePresenter(DefaultPresenter presenter);

    @Binds
    public abstract JFGView bindDefaultView(DefaultView view);

    @Binds
    public abstract BellLiveContract.Presenter provideBellLivePresenter(BellLivePresenterImpl presenter);

    @Binds
    public abstract BellLiveContract.View bindBellLiveView(BellLiveActivity activity);

    @Binds
    public abstract PanoramaAlbumContact.Presenter providePanoramaAlbumPresenter(PanoramaAlbumPresenter presenter);

    @Binds
    public abstract PanoramaAlbumContact.View bindPanoramaAlbumView(PanoramaAlbumActivity activity);

    @Binds
    public abstract PanoramaDetailContact.Presenter providePanoramaDetailPresenter(PanoramaDetailPresenter presenter);

    @Binds
    public abstract PanoramaDetailContact.View bindPanoramaDetailView(PanoramaDetailActivity activity);

    @Binds
    public abstract PanoramaSettingContact.Presenter providePanoramaSettingPresenter(PanoramaSettingPresenter presenter);

    @Binds
    public abstract PanoramaSettingContact.View bindPanoramaSettingView(PanoramaSettingActivity activity);

    @Binds
    public abstract DelayRecordContract.Presenter provideDelayRecordPresenter(DelayRecordPresenterImpl presenter);

    @Binds
    public abstract DelayRecordContract.View bindDelayRecordView(DelayRecordActivity activity);

    @Binds
    public abstract DoorBellHomeContract.Presenter provideDoorBellHomePresenter(DBellHomePresenterImpl presenter);

    @Binds
    public abstract DoorBellHomeContract.View bindDoorBellHomeView(DoorBellHomeActivity activity);

    @Binds
    public abstract ShareMediaContact.Presenter provideSharePresenter(SharePresenter presenter);

    @Binds
    public abstract ShareMediaContact.View bindShareMediaView(ShareMediaActivity activity);

    @Binds
    public abstract PanoramaCameraContact.Presenter providePanoramaCameraPresenter(PanoramaPresenter presenter);

    @Binds
    public abstract PanoramaCameraContact.View bindPanoramaCameraView(PanoramaCameraActivity activity);

    @Binds
    public abstract PanoramaShareContact.Presenter providePanoramaSharePresenter(PanoramaSharePresenter presenter);

    @Binds
    public abstract PanoramaShareContact.View bindPanoramaShareVeiw(H5ShareEditorActivity activity);

    @Binds
    public abstract LiveSettingContact.Presenter provideLiveSettingPresenter(LiveSettingPresenter presenter);

    @Binds
    public abstract LiveSettingContact.View bindLiveSettingView(LiveSettingActivity activity);

    @Binds
    public abstract BellDetailContract.Presenter provideBellDetailPresenter(BellDetailSettingPresenterImpl presenter);

    @Binds
    public abstract BellDetailContract.View bindBellDetailView(BellDetailFragment fragment);

    @Binds
    public abstract BellSettingContract.Presenter provideBellSettingPresenter(BellSettingPresenterImpl presenter);

    @Binds
    public abstract BellSettingContract.View bindBellSettingView(BellSettingFragment fragment);

    @Binds
    public abstract HomeWonderfulContract.Presenter provideHomeWonderfulPresenter(HomeWonderfulPresenterImpl presenter);

    @Binds
    public abstract HomeWonderfulContract.View bindHomeWonderfulView(HomeWonderfulFragmentExt fragmentExt);

    @Binds
    public abstract PanoramaLogoConfigureContact.Presenter providePanoramaLogoConfigurePresenter(PanoramaLogoConfigurePresenter presenter);

    @Binds
    public abstract PanoramaLogoConfigureContact.View bindPanoramaLogoConfigureView(PanoramaLogoConfigureFragment fragment);

//    @Binds
//    public abstract CamDelayRecordContract.Presenter provideCamDelayRecordPresenter(CamDelayRecordContract.Presenter presenter);

    @Binds
    public abstract CamDelayRecordContract.View bindCamDelayRecordView(DelayRecordMainFragment fragment);

    @Binds
    public abstract MineShareContentContract.Presenter provideShareContentPresenter(MineShareContentPresenterImpl presenter);

    @Binds
    public abstract MineShareContentContract.View bindMineShareContentView(HomeMineShareContentFragment fragment);

    @Binds
    public abstract AIRecognitionContact.Presenter provideAIRecognitionPresenter(AIRecognitionPresenter presenter);

    @Binds
    public abstract AIRecognitionContact.View bindAIRecognitionView(AIRecognitionFragment fragment);

    @Binds
    public abstract YouTubeLiveSetting.Presenter provideYoutubeLiveSettingPresenter(YouTubeLiveSettingPresenter presenter);

    @Binds
    public abstract YouTubeLiveSetting.View bindYouTubeLiveSettingView(YouTubeLiveSettingFragment fragment);

    @Binds
    public abstract YouTubeLiveCreateContract.Presenter provideYoutubeLiveCreatePresenter(YouTubeLiveCreatePresenter presenter);

    @Binds
    public abstract YouTubeLiveCreateContract.View bindYouTubeLiveCreateView(YouTubeLiveCreateFragment fragment);

    @Binds
    public abstract SetFaceNameContact.Presenter provideSetFaceNamePresenter(SetFaceNamePresenter presenter);

    @Binds
    public abstract SetFaceNameContact.View bindSetFaceNameView(SetFaceNameFragment faceNameFragment);

    @Binds
    public abstract FaceManagerContact.Presenter provideFaceManagerPresenter(FaceManagerPresenter presenter);

    @Binds
    public abstract FaceManagerContact.View bindFaceManagerView(FaceManagerFragment fragment);

    @Binds
    public abstract CreateFaceContact.Presenter provideNewFacePresenter(CreateNewFacePresenter presenter);

    @Binds
    public abstract CreateFaceContact.View bindCreateFaceView(CreateNewFaceFragment fragment);

    @Binds
    public abstract FaceListContact.Presenter provideFaceListPresenter(FaceListPresenter presenter);

    @Binds
    public abstract FaceListContact.View bindFaceListView(FaceListFragment fragment);

    @Binds
    public abstract Pan720FullContract.Presenter providePan720FullPresenter(Pan720FullPresenter presenter);

    @Binds
    public abstract Pan720FullContract.View bindPan720FullView(Pan720FullFragment fragment);

    @Binds
    public abstract DoorPassWordSettingContact.Presenter bindDoorPasswordPresenter(DoorPasswordSettingPresenter passwordSettingPresenter);

    @Binds
    public abstract DoorPassWordSettingContact.View bindDoorPasswordSetting(DoorPassWordSettingFragment fragment);

    @Binds
    public abstract MonitorAreaSettingContact.Presenter bindMonitorAreaSettingPresenter(MonitorAreaSettingPresenter presenter);

    @Binds
    public abstract MonitorAreaSettingContact.View bindMonitorAreaSettingView(MonitorAreaSettingFragment fragment);

    @Binds
    public abstract WireBindContract.Presenter bindWireBindPresenter(WireBindPresenter presenter);

    @Binds
    public abstract WireBindContract.View bindWireBindView(WireBindActivity view);

    @Binds
    public abstract SelectCidContract.Presenter bindSelectCidPresenter(SelectCidPresenter presenter);

    @Binds
    public abstract SelectCidContract.View bindSelectCidView(SelectCidFragment fragment);

    @Binds
    public abstract RegisterFaceContract.View bindRegisterFaceView(RegisterFaceActivity activity);

    @Binds
    public abstract RegisterFaceContract.Presenter bindRegisterFacePresenter(RegisterFacePresenter presenter);

    @Binds
    public abstract FaceSettingContract.View bindFaceSettingView(FaceSettingFragment faceSettingFragment);

    @Binds
    public abstract FaceSettingContract.Presenter bindFaceSettingPresenter(FaceSettingPresenter presenter);
}
