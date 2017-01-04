package com.cylan.jiafeigou.n.view.record;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

/**
 * Created by yzd on 16-12-16.
 */

public class DelayRecordGuideFragment extends IBaseFragment {

    public static final String KEY_DEVICE_INFO = "key_device_info";

    //    private BeanCamInfo mBean;
    private String uuid;

    public static DelayRecordGuideFragment newInstance(Bundle bundle) {
        DelayRecordGuideFragment fragment = new DelayRecordGuideFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout mTopBarContainer;

    private WeakReference<BaseDialog> mEnableDeviceDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delay_record_guide, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewPaddingStatusBar(mTopBarContainer);
    }

    @OnClick(R.id.fragment_delay_record_start_now)
    public void startNow() {
        if (isDeviceSleeping()) {
            initDeviceEnableDialog();
            mEnableDeviceDialog.get().show(getChildFragmentManager(), BaseDialog.class.getName());
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
            Intent intent = new Intent(getContext(), CamDelayRecordActivity.class);
            intent.putExtras(getArguments());
            intent.putExtra(KEY_DEVICE_ITEM_UUID, uuid);
            startActivity(intent);
        }
    }

    private boolean isDeviceSleeping() {
        return GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG, false);
    }

    private void initDeviceEnableDialog() {
        if (mEnableDeviceDialog == null || mEnableDeviceDialog.get() == null) {
            mEnableDeviceDialog = new WeakReference<>(new BaseDialog());
            View view = View.inflate(getContext(), R.layout.dialog_enable_device, null);
            mEnableDeviceDialog.get().setContentView(view);
        }
    }
}
