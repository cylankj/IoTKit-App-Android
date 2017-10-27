//package com.cylan.jiafeigou.base.injector.component;
//
//import com.cylan.jiafeigou.base.injector.module.FragmentModule;
//import com.cylan.jiafeigou.dagger.annotation.FragmentScope;
//import com.cylan.jiafeigou.dagger.component.AppComponent;
//import com.cylan.jiafeigou.n.view.bell.BellDetailFragment;
//import com.cylan.jiafeigou.n.view.bell.BellSettingFragment;
//import com.cylan.jiafeigou.n.view.cam.AIRecognitionFragment;
//import com.cylan.jiafeigou.n.view.cam.CreateNewFaceFragment;
//import com.cylan.jiafeigou.n.view.cam.FaceListFragment;
//import com.cylan.jiafeigou.n.view.cam.FaceManagerFragment;
//import com.cylan.jiafeigou.n.view.cam.SetFaceNameFragment;
//import com.cylan.jiafeigou.n.view.home.HomeWonderfulFragmentExt;
//import com.cylan.jiafeigou.n.view.mine.HomeMineShareContentFragment;
//import com.cylan.jiafeigou.n.view.mine.HomeMineShareManagerFragment;
//import com.cylan.jiafeigou.n.view.panorama.Pan720FullFragment;
//import com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureFragment;
//import com.cylan.jiafeigou.n.view.panorama.YouTubeLiveCreateFragment;
//import com.cylan.jiafeigou.n.view.panorama.YouTubeLiveSettingFragment;
//import com.cylan.jiafeigou.n.view.record.DelayRecordDeviceFragment;
//import com.cylan.jiafeigou.n.view.record.DelayRecordGuideFragment;
//import com.cylan.jiafeigou.n.view.record.DelayRecordMainFragment;
//
//import dagger.Component;
//
///**
// * Created by yanzhendong on 2017/4/12.
// */
//@FragmentScope
//@Component(dependencies = AppComponent.class, modules = FragmentModule.class)
//public interface FragmentComponent {
//
//    void inject(BellDetailFragment fragment);
//
//    void inject(BellSettingFragment fragment);
//
//    void inject(HomeWonderfulFragmentExt fragment);
//
//    void inject(Pan720FullFragment fragment);
//
//    void inject(PanoramaLogoConfigureFragment fragment);
//
//    void inject(DelayRecordDeviceFragment fragment);
//
//    void inject(DelayRecordGuideFragment fragment);
//
//    void inject(DelayRecordMainFragment fragment);
//
//    void inject(HomeMineShareManagerFragment fragment);
//
//    void inject(HomeMineShareContentFragment fragment);
//
//    void inject(AIRecognitionFragment fragment);
//
//    void inject(YouTubeLiveSettingFragment fragment);
//
//    void inject(YouTubeLiveCreateFragment fragment);
//
//    void inject(SetFaceNameFragment fragment);
//
//    void inject(FaceManagerFragment fragment);
//
//    void inject(CreateNewFaceFragment faceFragment);
//
//    void inject(FaceListFragment fragment);
//}
