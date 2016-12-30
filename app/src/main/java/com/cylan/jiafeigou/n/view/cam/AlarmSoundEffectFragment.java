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
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamWarnContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamAlarmPresenterImpl;
import com.cylan.jiafeigou.utils.ViewUtils;
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
    private String uuid;

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
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        basePresenter = new CamAlarmPresenterImpl(this, uuid);
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
        DpMsgDefine.NotificationInfo notificationInfo = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION, null);
        int effect = notificationInfo.notification;
        final int count = rgWarnEffect.getChildCount();
        for (int i = 0; i < count; i++) {
            final int index = i;
            RadioButton box = (RadioButton) rgWarnEffect.getChildAt(i);
            box.setChecked(i == effect);
            box.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                if (isChecked) {
                    notificationInfo.notification = index;
                    basePresenter.updateInfoReq(notificationInfo, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION);
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
            DpMsgDefine.NotificationInfo notificationInfo = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION, null);
            callBack.callBack(notificationInfo);
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
                DpMsgDefine.NotificationInfo notificationInfo = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION, null);
                DurationDialogFragment durationDialogFragment = DurationDialogFragment.newInstance(null);
                durationDialogFragment.setValue(notificationInfo.duration);
                durationDialogFragment.setAction((int id, Object value) -> {
                    tvWarnRepeatMode.setText(String.format(Locale.getDefault(), "%ss", value));
                    DpMsgDefine.NotificationInfo info = GlobalDataProxy.getInstance().getValue(uuid, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION, null);
                    info.duration = (int) value;
                    basePresenter.updateInfoReq(info, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION);
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
