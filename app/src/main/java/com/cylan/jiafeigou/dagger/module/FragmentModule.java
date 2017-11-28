package com.cylan.jiafeigou.dagger.module;

import com.cylan.jiafeigou.base.wrapper.DaggerSupportFragment;
import com.cylan.jiafeigou.n.view.bell.BellDetailFragment;
import com.cylan.jiafeigou.n.view.bell.BellSettingFragment;
import com.cylan.jiafeigou.n.view.bind.SelectCidFragment;
import com.cylan.jiafeigou.n.view.cam.AIRecognitionFragment;
import com.cylan.jiafeigou.n.view.cam.CreateNewFaceFragment;
import com.cylan.jiafeigou.n.view.cam.DoorPassWordSettingFragment;
import com.cylan.jiafeigou.n.view.cam.FaceListFragment;
import com.cylan.jiafeigou.n.view.cam.FaceManagerFragment;
import com.cylan.jiafeigou.n.view.cam.MonitorAreaSettingFragment;
import com.cylan.jiafeigou.n.view.cam.SetFaceNameFragment;
import com.cylan.jiafeigou.n.view.home.HomeWonderfulFragmentExt;
import com.cylan.jiafeigou.n.view.mine.HomeMineShareContentFragment;
import com.cylan.jiafeigou.n.view.mine.HomeMineShareManagerFragment;
import com.cylan.jiafeigou.n.view.panorama.Pan720FullFragment;
import com.cylan.jiafeigou.n.view.panorama.PanoramaLogoConfigureFragment;
import com.cylan.jiafeigou.n.view.panorama.YouTubeLiveSettingFragment;
import com.cylan.jiafeigou.n.view.record.DelayRecordDeviceFragment;
import com.cylan.jiafeigou.n.view.record.DelayRecordGuideFragment;
import com.cylan.jiafeigou.n.view.record.DelayRecordMainFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by yanzhendong on 2017/10/26.
 */
@Module
public abstract class FragmentModule {
    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract DaggerSupportFragment daggerSupportFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract BellDetailFragment bellDetailFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract BellSettingFragment bellSettingFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract HomeWonderfulFragmentExt homeWonderfulFragmentExt();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract Pan720FullFragment pan720FullFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract PanoramaLogoConfigureFragment panoramaLogoConfigureFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract DelayRecordDeviceFragment delayRecordDeviceFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract DelayRecordGuideFragment delayRecordGuideFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract DelayRecordMainFragment delayRecordMainFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract HomeMineShareManagerFragment homeMineShareManagerFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract HomeMineShareContentFragment homeMineShareContentFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract AIRecognitionFragment aIRecognitionFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract YouTubeLiveSettingFragment youTubeLiveSettingFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract SetFaceNameFragment setFaceNameFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract FaceManagerFragment faceManagerFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract CreateNewFaceFragment createNewFaceFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract FaceListFragment faceListFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract DoorPassWordSettingFragment doorPassWordSettingFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract MonitorAreaSettingFragment monitorAreaSettingFragment();

    @ContributesAndroidInjector(modules = MVPModule2.class)
    abstract SelectCidFragment selectCidFragment();
}
