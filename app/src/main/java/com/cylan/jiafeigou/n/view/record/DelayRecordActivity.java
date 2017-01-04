package com.cylan.jiafeigou.n.view.record;

import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.record.DelayRecordPresenterImpl;

import butterknife.BindView;

public class DelayRecordActivity extends BaseFullScreenActivity<DelayRecordContract.Presenter> implements DelayRecordContract.View {

    @BindView(R.id.act_delay_record_content)
    FrameLayout mContentContainer;

    @Override
    protected DelayRecordContract.Presenter onCreatePresenter() {
        return new DelayRecordPresenterImpl();
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_delay_record;
    }

    @Override
    public void onShowRecordMainView() {

    }

    @Override
    public void onShowRecordGuideView() {

    }

    @Override
    public void onShowRecordDeviceView() {

    }

    @Override
    public void onShowDeviceSettingView() {

    }
}
