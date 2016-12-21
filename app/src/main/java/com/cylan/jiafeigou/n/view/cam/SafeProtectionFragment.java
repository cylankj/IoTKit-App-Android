package com.cylan.jiafeigou.n.view.cam;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.setting.SafeInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.SafeInfoPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.TimePickDialogFragment;
import com.kyleduo.switchbutton.SwitchButton;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.widget.dialog.BaseDialog.KEY_TITLE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SafeProtectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SafeProtectionFragment extends IBaseFragment<SafeInfoContract.Presenter>
        implements SafeInfoContract.View {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.tv_protection_sensitivity)
    TextView tvProtectionSensitivity;
    @BindView(R.id.tv_protection_notification)
    TextView tvProtectionNotification;
    @BindView(R.id.tv_protection_start_time)
    TextView tvProtectionStartTime;
    @BindView(R.id.tv_protection_end_time)
    TextView tvProtectionEndTime;
    @BindView(R.id.tv_protection_repeat_period)
    TextView tvProtectionRepeatPeriod;
    @BindView(R.id.sw_motion_detection)
    SettingItemView1 swMotionDetection;
    @BindView(R.id.lLayout_safe_container)
    LinearLayout lLayoutSafeContainer;
    private WeakReference<AlarmSoundEffectFragment> warnEffectFragmentWeakReference;
    private TimePickDialogFragment timePickDialogFragment;

    public SafeProtectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        basePresenter = new SafeInfoPresenterImpl(this,
                getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE));
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_video_talk_item new instance of
     * this fragment using the provided parameters.
     *
     * @param args Parameter 2.
     * @return A new instance of fragment SafeProtectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SafeProtectionFragment newInstance(Bundle args) {
        SafeProtectionFragment fragment = new SafeProtectionFragment();
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
        View view = inflater.inflate(R.layout.fragment_safe_protection, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
        updateDetails();
        ((SwitchButton) swMotionDetection.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
                    BeanCamInfo info = basePresenter.getBeanCamInfo();
                    info.cameraAlarmFlag = isChecked;
                    basePresenter.saveCamInfoBean(info, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                    showDetail(isChecked);
                });
        showDetail(basePresenter.getBeanCamInfo().cameraAlarmFlag);
    }

    private void showDetail(boolean show) {
        final int count = lLayoutSafeContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = lLayoutSafeContainer.getChildAt(i);
            if (v.getId() == R.id.sw_motion_detection) continue;//不要隐藏自己了
            if (v instanceof FrameLayout) {
                v.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (callBack != null)
            callBack.callBack(null);
    }

    private void updateDetails() {
        BeanCamInfo info = basePresenter.getBeanCamInfo();
        imgVTopBarCenter.setText(R.string.SECURE);
        //移动侦测
        swMotionDetection.setSwitchButtonState(info.cameraAlarmFlag);
        //提示音
        DpMsgDefine.NotificationInfo notificationInfo = info.cameraAlarmNotification;
        if (notificationInfo != null) {
            tvProtectionNotification.setText(getString(notificationInfo.notification == 0
                    ? R.string.MUTE : (notificationInfo.notification == 1
                    ? R.string.BARKING : R.string.ALARM)));
        }
        //灵敏度
        tvProtectionSensitivity.setText(info.cameraAlarmSensitivity == 0 ? getString(R.string.SENSITIVI_LOW)
                : (info.cameraAlarmSensitivity == 1 ? getString(R.string.SENSITIVI_STANDARD) : getString(R.string.SENSITIVI_HIGHT)));
        //报警周期
        tvProtectionRepeatPeriod.setText(basePresenter.getRepeatMode(getContext()));
        if (info.cameraAlarmInfo != null) {
            tvProtectionStartTime.setText(MiscUtils.parse2Time(info.cameraAlarmInfo.timeStart));
            tvProtectionEndTime.setText(MiscUtils.parse2Time(info.cameraAlarmInfo.timeEnd));
        }
    }

    @OnClick({R.id.imgV_top_bar_center,
            R.id.fLayout_protection_sensitivity,
            R.id.fLayout_protection_warn_effect,
            R.id.fLayout_protection_start_time,
            R.id.fLayout_protection_end_time,
            R.id.fLayout_protection_repeat_period})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_top_bar_center:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.fLayout_protection_sensitivity: {
                SetSensitivityDialogFragment fragment = SetSensitivityDialogFragment.newInstance(getArguments());
                fragment.setAction((int id, Object value) -> {
                    if (value != null && value instanceof Integer) {
                        int level = (int) value;
                        BeanCamInfo info = basePresenter.getBeanCamInfo();
                        info.cameraAlarmSensitivity = level;
                        basePresenter.saveCamInfoBean(info, DpMsgMap.ID_503_CAMERA_ALARM_SENSITIVITY);
                        updateDetails();
                    }
                });
                fragment.setArguments(getArguments());
                showFragment(fragment);
            }
            break;
            case R.id.fLayout_protection_warn_effect: {
                initWarnEffectFragment();
                AlarmSoundEffectFragment fragment = warnEffectFragmentWeakReference.get();
                fragment.setCallBack((Object o) -> {
                    updateDetails();
                });
                fragment.setArguments(getArguments());
                loadFragment(android.R.id.content, fragment);
            }
            break;
            case R.id.fLayout_protection_start_time: {
                initTimePickDialogFragment();
                timePickDialogFragment.setArguments(getBundle(getString(R.string.FROME)));
                timePickDialogFragment.show(getActivity().getSupportFragmentManager(), "timePickDialogFragmentStart");
                timePickDialogFragment.setAction((int id, Object value) -> {
                    if (value != null && value instanceof Integer) {
                        BeanCamInfo info = basePresenter.getBeanCamInfo();
                        if (info.cameraAlarmInfo.timeStart != (int) value) {
                            info.cameraAlarmInfo.timeStart = (int) value;
                            basePresenter.saveCamInfoBean(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
                        }
                        updateDetails();
                    }
                });
            }
            break;
            case R.id.fLayout_protection_end_time: {
                initTimePickDialogFragment();
                timePickDialogFragment.setArguments(getBundle(getString(R.string.TO)));
                timePickDialogFragment.show(getActivity().getSupportFragmentManager(), "timePickDialogFragmentEnd");
                timePickDialogFragment.setAction((int id, Object value) -> {
                    if (value != null && value instanceof Integer) {
                        BeanCamInfo info = basePresenter.getBeanCamInfo();
                        if (info.cameraAlarmInfo.timeEnd != (int) value) {
                            info.cameraAlarmInfo.timeEnd = (int) value;
                            basePresenter.saveCamInfoBean(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
                        }
                        updateDetails();
                    }
                });
            }
            break;
            case R.id.fLayout_protection_repeat_period: {
                BaseDialog fragment = CapturePeriodDialogFragment.newInstance(getArguments());
                fragment.setArguments(getArguments());
                fragment.setAction((int id, Object value) -> {
                    if (value != null && value instanceof Integer) {
                        int result = (int) value;
                        BeanCamInfo info = basePresenter.getBeanCamInfo();
                        DpMsgDefine.AlarmInfo alarmInfo = info.cameraAlarmInfo == null ? new DpMsgDefine.AlarmInfo() : info.cameraAlarmInfo;
                        if (alarmInfo.day != result) {
                            alarmInfo.day = result;
                            info.cameraAlarmInfo = alarmInfo;
                            basePresenter.saveCamInfoBean(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
                        }
                        updateDetails();
                    }
                });
                showFragment(fragment);
            }
            break;
        }
    }

    private Bundle getBundle(String title) {
        Bundle bundle = getArguments();
        bundle.putString(KEY_TITLE, title);
        return bundle;
    }

    private void initTimePickDialogFragment() {
        if (timePickDialogFragment == null) {
            timePickDialogFragment = TimePickDialogFragment.newInstance(null);
        }
    }

    private void initWarnEffectFragment() {
        if (warnEffectFragmentWeakReference == null
                || warnEffectFragmentWeakReference.get() == null) {
            warnEffectFragmentWeakReference = new WeakReference<>(AlarmSoundEffectFragment.newInstance(new Bundle()));
        }
    }

    private void showFragment(DialogFragment fragment) {
        if (fragment != null
                && !fragment.isResumed()
                && getActivity() != null)
            fragment.show(getActivity().getSupportFragmentManager(), fragment.getClass().getSimpleName());
    }

    /**
     * 用来加载fragment的方法。
     */
    private void loadFragment(int id, Fragment fragment) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(fragment.getClass().getSimpleName());
        if (f != null) {
            AppLogger.d("fragment is already added: " + f.getClass().getSimpleName());
            return;
        }
        getActivity().getSupportFragmentManager().beginTransaction()
                //如果需要动画，可以把动画添加进来
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    @Override
    public void beanUpdate(BeanCamInfo info) {
        basePresenter.saveCamInfoBean(info, -1);
        updateDetails();
    }

    @Override
    public void setPresenter(SafeInfoContract.Presenter presenter) {
        this.basePresenter = presenter;
    }
}
