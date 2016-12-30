package com.cylan.jiafeigou.n.view.cam;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamWarnContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamAlarmPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
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

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.rb_warn_effect_silence)
    RadioButton rbWarnEffectSilence;
    @BindView(R.id.rb_warn_effect_dog_)
    RadioButton rbWarnEffectDog;
    @BindView(R.id.rb_warn_effect_waring)
    RadioButton rbWarnEffectWaring;
    @BindView(R.id.tv_warn_repeat_mode)
    TextView tvWarnRepeatMode;
    @BindView(R.id.lLayout_warn_repeat_mode)
    LinearLayout lLayoutWarnRepeatMode;
    @BindView(R.id.rg_warn_effect)
    RadioGroup rgWarnEffect;
    private BeanCamInfo info;
    private DpMsgDefine.NotificationInfo notificationInfo;

    public AlarmSoundEffectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_video_talk_item new instance of
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
        basePresenter = new CamAlarmPresenterImpl(this, getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE));
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
        imgVTopBarCenter.setText(getString(R.string.SOUNDS));
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
        info = basePresenter.getBeanCamInfo();
        notificationInfo = info.cameraAlarmNotification == null ?
                new DpMsgDefine.NotificationInfo() : info.cameraAlarmNotification;
        int effect = notificationInfo.notification;
        final int count = rgWarnEffect.getChildCount();
        for (int i = 0; i < count; i++) {
            final int index = i;
            RadioButton box = (RadioButton) rgWarnEffect.getChildAt(i);
            box.setChecked(i == effect);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        notificationInfo.notification = index;
                        info.cameraAlarmNotification = notificationInfo;
                        basePresenter.updateInfoReq(info.cameraAlarmNotification, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION);
                    }
                }
            });
        }
        tvWarnRepeatMode.setText(String.format(Locale.getDefault(), getString(R.string.EFAMILY_CALL_DURATION_S),
                notificationInfo.duration));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (callBack != null) {
            callBack.callBack(info.cameraAlarmNotification);
        }
    }

    @OnClick({R.id.imgV_top_bar_center, R.id.lLayout_warn_repeat_mode})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_top_bar_center:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.lLayout_warn_repeat_mode:
                ViewUtils.deBounceClick(view);
                DurationDialogFragment durationDialogFragment = DurationDialogFragment.newInstance(null);
                durationDialogFragment.setValue(info.cameraAlarmNotification.duration);
                durationDialogFragment.setAction(new BaseDialog.BaseDialogAction() {

                    @Override
                    public void onDialogAction(int id, Object value) {
                        tvWarnRepeatMode.setText(String.format(Locale.getDefault(), "%ss", value));
                        notificationInfo.duration = (int) value;
                        info.cameraAlarmNotification = notificationInfo;
                        basePresenter.updateInfoReq(info.cameraAlarmNotification, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION);
                    }
                });
                durationDialogFragment.show(getActivity().getSupportFragmentManager(), "durationDialogFragment");
                break;
        }
    }

    @Override
    public void setPresenter(CamWarnContract.Presenter presenter) {
        basePresenter = presenter;
    }
}
