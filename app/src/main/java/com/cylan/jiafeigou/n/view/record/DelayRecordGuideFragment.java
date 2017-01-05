package com.cylan.jiafeigou.n.view.record;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.mvp.contract.record.DelayRecordContract;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yzd on 16-12-16.
 */

public class DelayRecordGuideFragment extends BaseFragment {

    public static DelayRecordGuideFragment newInstance(Bundle bundle) {
        DelayRecordGuideFragment fragment = new DelayRecordGuideFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout mTopBarContainer;

    private WeakReference<BaseDialog> mEnableDeviceDialog;

    @Override
    protected JFGPresenter onCreatePresenter() {
        return new BasePresenter<JFGView>() {
            //有些view过于简单则不必使用presenter,但任然保留此接口,以便以后维护
        };
    }

    @Override
    protected int getContentViewID() {
        return R.layout.fragment_delay_record_guide;
    }

    @Override
    protected void initViewAndListener() {
        ViewUtils.setViewPaddingStatusBar(mTopBarContainer);
    }

    @OnClick(R.id.fragment_delay_record_start_now)
    public void startNow() {
        if (isDeviceSleeping()) {
            //TODO:关闭设备待机模式,需要跳转另一个activity,以后有时间再写
            initDeviceEnableDialog();
            mEnableDeviceDialog.get().show(getChildFragmentManager(), BaseDialog.class.getName());
        } else {
            onViewActionToActivity(JFGView.VIEW_ACTION_OK, DelayRecordContract.View.VIEW_HANDLER_GUIDE_START_NOW, null);
        }
    }

    private boolean isDeviceSleeping() {
        return GlobalDataProxy.getInstance().getValue(mUUID, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, false);
    }

    private void initDeviceEnableDialog() {
        if (mEnableDeviceDialog == null || mEnableDeviceDialog.get() == null) {
            mEnableDeviceDialog = new WeakReference<>(new BaseDialog());
            View view = View.inflate(getContext(), R.layout.dialog_enable_device, null);
            mEnableDeviceDialog.get().setContentView(view);
        }
    }
}
