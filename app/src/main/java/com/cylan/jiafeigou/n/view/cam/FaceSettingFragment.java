package com.cylan.jiafeigou.n.view.cam;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.wrapper.BaseFragment;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.cam.FaceSettingContract;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.SettingItemView0;

import butterknife.BindView;

/**
 * Created by yanzhendong on 2018/1/25.
 */

public class FaceSettingFragment extends BaseFragment<FaceSettingContract.Presenter> implements FaceSettingContract.View, FaceDetectionSizeChooseFragment.ChooseCallback {
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.siv_face_detection_setting)
    SettingItemView0 sivFaceDetectionSetting;
    @BindView(R.id.siv_face_detection_size_setting)
    SettingItemView0 sivFaceDetectionSizeSetting;
    private int detectionSize = 120;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_face_setting, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.performCheckAndInitFaceSetting();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customToolbar.setBackAction(this::onBackClicked);
        sivFaceDetectionSetting.setOnCheckedChangeListener(this::onFaceDetectionChanged);
        sivFaceDetectionSizeSetting.setOnClickListener(this::onFaceDetectionSizeClicked);
    }

    private void onFaceDetectionSizeClicked(View view) {
        FaceDetectionSizeChooseFragment sizeChooseFragment = FaceDetectionSizeChooseFragment.newInstance("", detectionSize);
        sizeChooseFragment.setChooseCallback(this);
        sizeChooseFragment.show(getFragmentManager(), FaceDetectionSizeChooseFragment.class.getSimpleName());
    }

    void onFaceDetectionChanged(CompoundButton button, boolean isChecked) {
        AppLogger.w("onFaceDetectionChanged,isChecked:" + isChecked);
        presenter.performChangeFaceDetectionAction(isChecked);
    }

    public static FaceSettingFragment newInstance(String uuid) {
        FaceSettingFragment fragment = new FaceSettingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        fragment.setArguments(bundle);
        return fragment;
    }

    void onBackClicked(View view) {
        AppLogger.w("onBackClicked");
        getFragmentManager().popBackStack();
    }

    @Override
    public void onQueryFaceDetectionFinished(Boolean isFaceDetectionOpened) {
        AppLogger.d("onQueryFaceDetectionFinished,isFaceDetectionOpened:" + isFaceDetectionOpened);
        sivFaceDetectionSetting.setChecked(isFaceDetectionOpened);
    }

    @Override
    public void onChangeFaceDetectionFinished(Boolean isSuccessful) {
        AppLogger.d("onChangeFaceDetectionFinished,isSuccessful:" + isSuccessful);
        if (!isSuccessful) {
            sivFaceDetectionSetting.setChecked(!sivFaceDetectionSetting.isChecked());
            ToastUtil.showToast(getString(R.string.SETTINGS_FAILED));
        } else {
            ToastUtil.showToast(getString(R.string.PWD_OK_2));
        }
    }

    @Override
    public void onQueryFaceDetectionSizeFinished(DpMsgDefine.DPDetectionSize size) {
        AppLogger.d("onQueryFaceDetectionSizeFinished:" + size);
        if (size != null) {
            detectionSize = size.x;
        }
        sivFaceDetectionSizeSetting.setSubTitle(String.format("%spx", detectionSize));
    }

    @Override
    public void onChangeFaceDetectionSizeFinished(Boolean success, DpMsgDefine.DPDetectionSize detectionSize) {
        if (!success) {
            ToastUtil.showToast(getString(R.string.SETTINGS_FAILED));
        } else {
            this.detectionSize = detectionSize.x;
            sivFaceDetectionSizeSetting.setSubTitle(String.format("%spx", this.detectionSize));
        }
    }

    @Override
    public void onChoose(int size) {
        presenter.performChangeFaceDetectionSizeAction(new DpMsgDefine.DPDetectionSize(size, size));
    }
}
