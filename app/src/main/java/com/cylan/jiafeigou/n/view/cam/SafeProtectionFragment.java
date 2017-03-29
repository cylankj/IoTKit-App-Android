package com.cylan.jiafeigou.n.view.cam;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.setting.SafeInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.SafeInfoPresenterImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.TimePickDialogFragment;
import com.kyleduo.switchbutton.SwitchButton;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_503_CAMERA_ALARM_SENSITIVITY;
import static com.cylan.jiafeigou.widget.dialog.BaseDialog.KEY_TITLE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SafeProtectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SafeProtectionFragment extends IBaseFragment<SafeInfoContract.Presenter>
        implements SafeInfoContract.View {

    @BindView(R.id.sw_motion_detection)
    SettingItemView1 swMotionDetection;
    @BindView(R.id.lLayout_safe_container)
    LinearLayout lLayoutSafeContainer;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.fLayout_protection_sensitivity)
    SettingItemView0 fLayoutProtectionSensitivity;
    @BindView(R.id.fLayout_protection_warn_effect)
    SettingItemView0 fLayoutProtectionWarnEffect;
    @BindView(R.id.fLayout_protection_start_time)
    SettingItemView0 fLayoutProtectionStartTime;
    @BindView(R.id.fLayout_protection_end_time)
    SettingItemView0 fLayoutProtectionEndTime;
    @BindView(R.id.fLayout_protection_repeat_period)
    SettingItemView0 fLayoutProtectionRepeatPeriod;
    private WeakReference<AlarmSoundEffectFragment> warnEffectFragmentWeakReference;
    private TimePickDialogFragment timePickDialogFragment;
    private String uuid;

    private Device device;


    public SafeProtectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        basePresenter = new SafeInfoPresenterImpl(this, uuid);
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
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
        device = DataSourceManager.getInstance().getJFGDevice(this.uuid);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        customToolbar.setBackAction((View v) -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });
        updateDetails();
        boolean f = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        showDetail(f);
        //移动侦测
        swMotionDetection.setChecked(f);
        swMotionDetection.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            Device aDevice = DataSourceManager.getInstance().getJFGDevice(uuid);
            DpMsgDefine.DPSdStatus net = aDevice.$(204, new DpMsgDefine.DPSdStatus());
            if (!isChecked) {
                if (!JFGRules.hasSdcard(net)) {
                    DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                    wFlag.value = false;
                    basePresenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                    showDetail(false);
                    updateDetails();
                    ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                    return;//不插卡 不需要提示
                }
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.Tap1_Camera_MotionDetection_OffTips))
                        .setPositiveButton(getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                            DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                            wFlag.value = false;
                            basePresenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                            showDetail(false);
                            updateDetails();
                            ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                        })
                        .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                            ((SwitchButton) swMotionDetection.findViewById(R.id.btn_item_switch)).setChecked(true);
                        })
                        .show();
            } else {
                DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                wFlag.value = true;
                basePresenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                showDetail(true);
                updateDetails();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_safe_protection, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    private void showDetail(boolean show) {
        final int count = lLayoutSafeContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = lLayoutSafeContainer.getChildAt(i);
            if (v.getId() == R.id.sw_motion_detection) continue;//不要隐藏自己了
            if (v instanceof SettingItemView0 || v instanceof FrameLayout) {
                v.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
        if (show && device != null && JFGRules.isFreeCam(device.pid)) {
            fLayoutProtectionWarnEffect.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (callBack != null)
            callBack.callBack(null);
    }

    private void updateDetails() {
        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
        //提示音
        DpMsgDefine.DPNotificationInfo notificationInfo = device.$(504, new DpMsgDefine.DPNotificationInfo());
        fLayoutProtectionWarnEffect.setTvSubTitle(getString(notificationInfo.notification == 0
                ? R.string.MUTE : (notificationInfo.notification == 1
                ? R.string.BARKING : R.string.ALARM)));
        //灵敏度
        int s = device.$(ID_503_CAMERA_ALARM_SENSITIVITY, 0);
        fLayoutProtectionSensitivity.setTvSubTitle(s == 0 ? getString(R.string.SENSITIVI_LOW)
                : (s == 1 ? getString(R.string.SENSITIVI_STANDARD) : getString(R.string.SENSITIVI_HIGHT)));
        //报警周期
        DpMsgDefine.DPAlarmInfo info = device.$(502, new DpMsgDefine.DPAlarmInfo());
        fLayoutProtectionRepeatPeriod.setTvSubTitle(basePresenter.getRepeatMode(getContext()));
        if (info != null) {
            fLayoutProtectionStartTime.setTvSubTitle(MiscUtils.parse2Time(info.timeStart));
            fLayoutProtectionEndTime.setTvSubTitle(MiscUtils.parse2Time(info.timeEnd));
        }
    }

    @OnClick({
            R.id.fLayout_protection_sensitivity,
            R.id.fLayout_protection_warn_effect,
            R.id.fLayout_protection_start_time,
            R.id.fLayout_protection_end_time,
            R.id.fLayout_protection_repeat_period})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fLayout_protection_sensitivity: {
                SetSensitivityDialogFragment fragment = SetSensitivityDialogFragment.newInstance(getArguments());
                fragment.setAction((int id, Object value) -> {
                    if (value != null && value instanceof Integer) {
                        int level = (int) value;
                        DpMsgDefine.DPPrimary<Integer> wFlag = new DpMsgDefine.DPPrimary<>();
                        wFlag.value = level;
                        basePresenter.updateInfoReq(wFlag, ID_503_CAMERA_ALARM_SENSITIVITY);
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
                ViewUtils.deBounceClick(view);
                initTimePickDialogFragment();
                timePickDialogFragment.setArguments(getBundle(getString(R.string.FROME)));
                timePickDialogFragment.show(getActivity().getSupportFragmentManager(), "timePickDialogFragmentStart");
                timePickDialogFragment.setAction((int id, Object value) -> {
                    if (value != null && value instanceof Integer) {
                        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
                        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
                        if (info.timeStart != (int) value) {
                            info.timeStart = (int) value;
                            basePresenter.updateInfoReq(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
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
                        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
                        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
                        if (info.timeEnd != (int) value) {
                            info.timeEnd = (int) value;
                            basePresenter.updateInfoReq(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
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
                        Device device = DataSourceManager.getInstance().getJFGDevice(uuid);
                        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
                        if (info.day != result) {
                            info.day = result;
                            basePresenter.updateInfoReq(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
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
    public void setPresenter(SafeInfoContract.Presenter presenter) {
        this.basePresenter = presenter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
