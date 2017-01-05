package com.cylan.jiafeigou.n.view.record;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.record.DelayRecordPresenterImpl;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;

public class DelayRecordActivity extends BaseActivity<DelayRecordContract.Presenter> implements DelayRecordContract.View {

    @BindView(R.id.act_delay_record_content)
    FrameLayout mContentContainer;
    @BindView(R.id.act_delay_record_header)
    ViewGroup mRecordHeader;
    @BindView(R.id.activity_delay_record)
    ViewGroup mRootContent;
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
    protected void initViewAndListener() {
        ViewUtils.setViewMarginStatusBar(mRecordHeader);
    }

    @Override
    public void onShowRecordMainView() {
        if (mRecordMainFrag == null) {
            mRecordMainFrag = DelayRecordMainFragment.newInstance(mUUID);
        }
        mRootContent.setBackgroundResource(R.drawable.delay_record_bg);
        ActivityUtils.replaceFragment(R.id.act_delay_record_content, getSupportFragmentManager(), mRecordMainFrag);
    }

    @Override
    public void onShowRecordGuideView() {
        if (mRecordGuideFrag == null) {
            mRecordGuideFrag = DelayRecordGuideFragment.newInstance(mUUID);
        }
        mRootContent.setBackgroundResource(R.color.color_0ba8cf);
        ActivityUtils.loadFragmentNoAnimation(R.id.act_delay_record_content, getSupportFragmentManager(), mRecordGuideFrag);
    }

    @Override
    public void onShowRecordDeviceView() {
        if (mRecordDeviceFrag == null) {
            mRecordDeviceFrag = DelayRecordDeviceFragment.newInstance(null);
        }
        ActivityUtils.loadFragmentNoBackStack(R.id.act_delay_record_content, getSupportFragmentManager(), mRecordDeviceFrag);
    }

    @Override
    public void onShowDeviceSettingView() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
