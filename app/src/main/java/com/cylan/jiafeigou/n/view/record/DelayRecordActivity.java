package com.cylan.jiafeigou.n.view.record;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.base.wrapper.BaseActivity;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.record.DelayRecordPresenterImpl;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.List;

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
    public void onShowRecordMainView(String uuid) {
        if (mRecordMainFrag == null) {
            mRecordMainFrag = DelayRecordMainFragment.newInstance(uuid);
        }
        mRootContent.setBackgroundResource(R.drawable.delay_record_bg);
        ActivityUtils.replaceFragment(R.id.act_delay_record_content, getSupportFragmentManager(), mRecordMainFrag);
    }

    @Override
    public void onShowRecordGuideView(String uuid) {
        if (mRecordGuideFrag == null) {
            mRecordGuideFrag = DelayRecordGuideFragment.newInstance(uuid);
        }
        mRootContent.setBackgroundResource(R.color.color_0ba8cf);
        ActivityUtils.replaceFragmentNoAnimation(R.id.act_delay_record_content, getSupportFragmentManager(), mRecordGuideFrag);
    }

    @Override
    public void onShowRecordDeviceView(List<String> devices) {
        if (mRecordDeviceFrag == null) {
            mRecordDeviceFrag = DelayRecordDeviceFragment.newInstance(devices);
        }
        mRootContent.setBackgroundResource(R.color.color_0ba8cf);
        ActivityUtils.replaceFragmentNoAnimation(R.id.act_delay_record_content, getSupportFragmentManager(), mRecordDeviceFrag);
    }

    @Override
    public void onShowDeviceSettingView(String uuid) {

    }

    @Override
    public void onShowNoDeviceView() {
        if (mRecordDeviceFrag == null) {
            onShowRecordDeviceView(null);
        } else {
            mRecordDeviceFrag.onViewAction(JFGView.VIEW_ACTION_OFFER, "empty", null);
        }
    }

    @Override
    public void onUsableDeviceRsp(List<String> devices) {
        if (mRecordDeviceFrag != null && mRecordDeviceFrag.isVisible()) {//当前设备选择列表对用户可见
            mRecordDeviceFrag.onViewAction(VIEW_ACTION_OFFER, "devices", devices);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
