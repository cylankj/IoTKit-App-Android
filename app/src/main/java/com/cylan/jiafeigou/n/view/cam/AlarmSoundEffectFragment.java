package com.cylan.jiafeigou.n.view.cam;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamWarnContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamAlarmPresenterImpl;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.dialog.DurationDialogFragment;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AlarmSoundEffectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlarmSoundEffectFragment extends IBaseFragment<CamWarnContract.Presenter> implements CamWarnContract.View {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.rg_warn_effect)
    RadioGroup rgWarnEffect;
    @BindView(R.id.rb_warn_effect_silence)
    RadioButton rbWarnEffectSilence;
    @BindView(R.id.rb_warn_effect_dog_)
    RadioButton rbWarnEffectDog;
    @BindView(R.id.rb_warn_effect_waring)
    RadioButton rbWarnEffectWaring;
    @BindView(R.id.sv_warn_repeat_mode)
    SettingItemView0 svWarnRepeatMode;
    private String uuid;

    public AlarmSoundEffectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @param args Parameter 1.
     * @return A new instance of fragment AlarmSoundEffectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlarmSoundEffectFragment newInstance(Bundle args) {
        AlarmSoundEffectFragment fragment = new AlarmSoundEffectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        presenter = new CamAlarmPresenterImpl(this, uuid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alarm_sound_effect, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        customToolbar.setBackAction((View v) -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPNotificationInfo notificationInfo = device.$(DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION, new DpMsgDefine.DPNotificationInfo());
        if (notificationInfo == null) {
            notificationInfo = new DpMsgDefine.DPNotificationInfo();
        }
        int effect = notificationInfo.notification;
        final int count = rgWarnEffect.getChildCount();
        for (int i = 0; i < count; i++) {
            if (effect == i) {
                ((RadioButton) rgWarnEffect.getChildAt(i)).setChecked(true);
            }
        }
        svWarnRepeatMode.setSubTitle(String.format(Locale.getDefault(), getString(R.string.EFAMILY_CALL_DURATION_S),
                notificationInfo.duration));
        svWarnRepeatMode.setVisibility(effect == 0 ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPNotificationInfo notificationInfo = device.$(DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION, new DpMsgDefine.DPNotificationInfo());
        setCache(notificationInfo);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnClick({R.id.sv_mode_mute, R.id.sv_mode_bark, R.id.sv_mode_alarm, R.id.sv_warn_repeat_mode})
    public void onClick(View view) {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPNotificationInfo notificationInfo = device.$(504, new DpMsgDefine.DPNotificationInfo());
        switch (view.getId()) {
            case R.id.sv_mode_mute:
                svWarnRepeatMode.setVisibility(View.GONE);
                rbWarnEffectSilence.setChecked(true);
                notificationInfo.notification = 0;
                break;
            case R.id.sv_mode_bark:
                svWarnRepeatMode.setVisibility(View.VISIBLE);
                rbWarnEffectDog.setChecked(true);
                presenter.playSound(R.raw.wangwang_voice);
                notificationInfo.notification = 1;
                break;
            case R.id.sv_mode_alarm:
                svWarnRepeatMode.setVisibility(View.VISIBLE);
                rbWarnEffectWaring.setChecked(true);
                presenter.playSound(R.raw.warm_voice);
                notificationInfo.notification = 2;
                break;
            case R.id.sv_warn_repeat_mode:
                ViewUtils.deBounceClick(view);
                DurationDialogFragment durationDialogFragment = DurationDialogFragment.newInstance(null);
                durationDialogFragment.setValue(notificationInfo.duration);
                durationDialogFragment.setAction((int id, Object value) -> {
                    svWarnRepeatMode.setSubTitle(String.format(Locale.getDefault(), "%ss", value));
                    notificationInfo.duration = (int) value;
                    presenter.updateInfoReq(notificationInfo, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION);
                });
                durationDialogFragment.show(getActivity().getSupportFragmentManager(), "durationDialogFragment");
                return;
        }
        presenter.updateInfoReq(notificationInfo, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION);
    }
}
