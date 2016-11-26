package com.cylan.jiafeigou.n.view.cam;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SafeProtectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SafeProtectionFragment extends IBaseFragment<SafeInfoContract.Presenter>
        implements SafeInfoContract.View {

    private static final int weekStringId[] = {
            R.string.MON_1, R.string.TUE_1, R.string.WED_1, R.string.THU_1, R.string.FRI_1,
            R.string.SAT_1, R.string.SUN_1};
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

    private WeakReference<SetSensitivityDialogFragment> setSensitivityFragmentWeakReference;
    private WeakReference<WarnEffectFragment> warnEffectFragmentWeakReference;
    private WeakReference<CapturePeriodDialogFragment> capturePeriodDialogFragmentWeakReference;

    public SafeProtectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        basePresenter = new SafeInfoPresenterImpl(this,
                (BeanCamInfo) getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE));
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
        updateDetails();
    }

    private void updateDetails() {
        BeanCamInfo info = basePresenter.getBeanCamInfo();
        imgVTopBarCenter.setText(R.string.SECURE);
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
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
                initSensitivityFragment();
                Fragment fragment = setSensitivityFragmentWeakReference.get();
                fragment.setArguments(getArguments());
                showFragment((DialogFragment) fragment);
            }
            break;
            case R.id.fLayout_protection_warn_effect: {
                initWarnEffectFragment();
                WarnEffectFragment fragment = warnEffectFragmentWeakReference.get();
                fragment.setCallBack(new CallBack() {
                    @Override
                    public void callBack(Object o) {
                        BeanCamInfo info = basePresenter.getBeanCamInfo();
                        if (o instanceof DpMsgDefine.NotificationInfo) {
                            if (info.cameraAlarmNotification.notification != ((DpMsgDefine.NotificationInfo) o).notification
                                    || info.cameraAlarmNotification.duration != ((DpMsgDefine.NotificationInfo) o).duration) {
                                //something update
                                info.cameraAlarmNotification = (DpMsgDefine.NotificationInfo) o;
                                basePresenter.updateInfo(info, DpMsgMap.ID_504_CAMERA_ALARM_NOTIFICATION);
                                updateDetails();
                            }
                        }
                    }
                });
                fragment.setArguments(getArguments());
                loadFragment(android.R.id.content, fragment);
            }
            break;
            case R.id.fLayout_protection_start_time:
                break;
            case R.id.fLayout_protection_end_time:
                break;
            case R.id.fLayout_protection_repeat_period: {
                initCapturePeriodFragment();
                BaseDialog fragment = capturePeriodDialogFragmentWeakReference.get();
                fragment.setArguments(getArguments());
                fragment.setAction(new BaseDialog.SimpleDialogAction() {
                    @Override
                    public void onDialogAction(int id, Object value) {
                        if (value != null && value instanceof Integer) {
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < 7; i++) {
                                if ((((int) value >> (7 - 1 - i)) & 0x01) == 1) {
                                    builder.append(getString(weekStringId[i]));
                                    builder.append(" ");
                                }
                            }
                            tvProtectionRepeatPeriod.setText(builder.toString());
                            BeanCamInfo info = basePresenter.getBeanCamInfo();
                            DpMsgDefine.AlarmInfo alarmInfo = info == null ? new DpMsgDefine.AlarmInfo() : info.cameraAlarmInfo;
                            if (alarmInfo.day != (int) value) {
                                alarmInfo.day = (int) value;
                                info.cameraAlarmInfo = alarmInfo;
                                basePresenter.updateInfo(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
                            }
                        }
                    }
                });
                showFragment(fragment);
            }
            break;
        }
    }

    private void initSensitivityFragment() {
        if (setSensitivityFragmentWeakReference == null
                || setSensitivityFragmentWeakReference.get() == null) {
            setSensitivityFragmentWeakReference = new WeakReference<>(SetSensitivityDialogFragment.newInstance(new Bundle()));
        }
    }

    private void initWarnEffectFragment() {
        if (warnEffectFragmentWeakReference == null
                || warnEffectFragmentWeakReference.get() == null) {
            warnEffectFragmentWeakReference = new WeakReference<>(WarnEffectFragment.newInstance(new Bundle()));
        }
    }

    private void initCapturePeriodFragment() {
        if (capturePeriodDialogFragmentWeakReference == null
                || capturePeriodDialogFragmentWeakReference.get() == null) {
            capturePeriodDialogFragmentWeakReference = new WeakReference<>(CapturePeriodDialogFragment.newInstance(new Bundle()));
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

    }

    @Override
    public void setPresenter(SafeInfoContract.Presenter presenter) {
        this.basePresenter = presenter;
    }
}
