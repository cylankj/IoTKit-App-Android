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
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamWarnContract;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WarnEffectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WarnEffectFragment extends IBaseFragment<CamWarnContract.Presenter> implements CamWarnContract.View {

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

    public WarnEffectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_video_talk_item new instance of
     * this fragment using the provided parameters.
     *
     * @param args Parameter 1.
     * @return A new instance of fragment WarnEffectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WarnEffectFragment newInstance(Bundle args) {
        WarnEffectFragment fragment = new WarnEffectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_warn_effect, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        imgVTopBarCenter.setText("设备提示音");
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
        final BeanCamInfo info = getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        final DpMsgDefine.NotificationInfo notificationInfo = info.cameraAlarmNotification == null ?
                new DpMsgDefine.NotificationInfo() : info.cameraAlarmNotification;
        int effect = notificationInfo.notification;
        int duration = notificationInfo.duration;
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

                    }
                }
            });
        }
        tvWarnRepeatMode.setText(String.format(Locale.getDefault(), "%ss", duration));
    }

    @OnClick({R.id.imgV_top_bar_center, R.id.lLayout_warn_repeat_mode})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_top_bar_center:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.lLayout_warn_repeat_mode:
                break;
        }
    }

    @Override
    public void setPresenter(CamWarnContract.Presenter presenter) {
        basePresenter = presenter;
    }
}
