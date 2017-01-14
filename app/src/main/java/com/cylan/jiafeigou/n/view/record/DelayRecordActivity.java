package com.cylan.jiafeigou.n.view.record;

import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.record.DelayRecordPresenterImpl;
import com.cylan.jiafeigou.utils.ActivityUtils;

import java.util.List;

import butterknife.BindView;

public class DelayRecordActivity extends BaseActivity<DelayRecordContract.Presenter> implements DelayRecordContract.View {

    @BindView(R.id.act_delay_record_content)
    FrameLayout mContentContainer;
    private BaseFragment mRecordMainFrag;
    private BaseFragment mRecordGuideFrag;
    private BaseFragment mRecordDeviceFrag;

    @Override
    protected DelayRecordContract.Presenter onCreatePresenter() {
        return new DelayRecordPresenterImpl();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_delay_record;
    }


    @Override
    public void onShowRecordMainView(String uuid) {
        if (mRecordMainFrag == null) {
            mRecordMainFrag = DelayRecordMainFragment.newInstance(uuid);
        }
        ActivityUtils.replaceFragment(R.id.act_delay_record_content, getSupportFragmentManager(), mRecordMainFrag);
    }

    @Override
    public void onShowRecordGuideView(String uuid) {
        if (mRecordGuideFrag == null) {
            mRecordGuideFrag = DelayRecordGuideFragment.newInstance(uuid);
        }
        ActivityUtils.replaceFragmentNoAnimation(R.id.act_delay_record_content, getSupportFragmentManager(), mRecordGuideFrag);
    }

    @Override
    public void onShowRecordDeviceView(List<String> devices) {
        if (mRecordDeviceFrag == null) {
            mRecordDeviceFrag = DelayRecordDeviceFragment.newInstance(devices);
        }
        ActivityUtils.replaceFragmentNoAnimation(R.id.act_delay_record_content, getSupportFragmentManager(), mRecordDeviceFrag);
    }

    @Override
    public void onShowDeviceSettingView(String uuid) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
