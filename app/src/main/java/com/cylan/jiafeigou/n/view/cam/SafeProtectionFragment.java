package com.cylan.jiafeigou.n.view.cam;


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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.pty.IProperty;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.setting.SafeInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.SafeInfoPresenterImpl;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.cylan.jiafeigou.widget.dialog.TimePickDialogFragment;
import com.kyleduo.switchbutton.SwitchButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_503_CAMERA_ALARM_SENSITIVITY;
import static com.cylan.jiafeigou.widget.dialog.BaseDialog.KEY_TITLE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SafeProtectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@Badge(parentTag = "CamSettingActivity", asRefresh = true)
public class SafeProtectionFragment extends IBaseFragment<SafeInfoContract.Presenter>
        implements SafeInfoContract.View {

    @BindView(R.id.tv_motion_detection_title)
    TextView tvMotionDetectionTitle;
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
    @BindView(R.id.fl_protection_title)
    FrameLayout flProtectionTitle;
    @BindView(R.id.ll_24_record_container)
    LinearLayout ll24RecordContainer;
    @BindView(R.id.sw_motion_AI)
    SettingItemView0 swMotionAI;
    @BindView(R.id.sw_motion_interval)
    SettingItemView0 swMotionInterval;
    @BindView(R.id.sw_infrared_strengthen)
    SettingItemView1 swInfraredStrengthen;
    @BindView(R.id.sw_monitoring_area)
    SettingItemView0 swMonitoringArea;
    @BindView(R.id.rl_monitoring_area_container)
    RelativeLayout rlMonitorAreaContainer;
    private Device device;


    public SafeProtectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        presenter = new SafeInfoPresenterImpl(this);
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
        device = BaseApplication.getAppComponent().getSourceManager().getDevice(this.uuid);
        BaseApplication.getAppComponent().getTreeHelper().markNodeRead(this.getClass().getSimpleName());
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        customToolbar.setBackAction((View v) -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });
        updateDetails();
        boolean isRs = JFGRules.isRS(device.pid);
        boolean f = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, isRs ? false : false);
        showDetail(f);
        //移动侦测
        swMotionDetection.setChecked(f);
        swMotionDetection.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            Device aDevice = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
            DpMsgDefine.DPSdStatus sdcard = aDevice.$(204, new DpMsgDefine.DPSdStatus());
            if (!isChecked) {
                if (!JFGRules.hasSdcard(sdcard)) {
                    DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                    wFlag.value = false;
                    presenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                    showDetail(false);
                    updateDetails();
                    ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                    return;//不插卡 不需要提示
                }
                //自动录像选择 侦测到异常时 需要弹框
                int oldOption = aDevice.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
                if (oldOption != 0) {
                    DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                    wFlag.value = false;
                    presenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                    showDetail(false);
                    ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                    return;
                }
                AlertDialogManager.getInstance().showDialog(getActivity(), getString(R.string.Tap1_Camera_MotionDetection_OffTips), getString(R.string.Tap1_Camera_MotionDetection_OffTips),
                        getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                            DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                            wFlag.value = false;
                            presenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                            showDetail(false);
                            updateDetails();
                            ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                        }, getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                            ((SwitchButton) swMotionDetection.findViewById(R.id.btn_item_switch)).setChecked(true);
                        }, false);
            } else {
                DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                wFlag.value = true;
                presenter.updateInfoReq(wFlag, DpMsgMap.ID_501_CAMERA_ALARM_FLAG);
                showDetail(true);
                updateDetails();
            }
        });
        Boolean enableInfrared = device.$(DpMsgMap.ID_520_CAM_INFRARED, false);
        swInfraredStrengthen.setChecked(enableInfrared);
        swInfraredStrengthen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                wFlag.value = true;
                presenter.updateInfoReq(wFlag, DpMsgMap.ID_520_CAM_INFRARED);
                updateDetails();
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            } else {
                DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                wFlag.value = false;
                presenter.updateInfoReq(wFlag, DpMsgMap.ID_520_CAM_INFRARED);
                updateDetails();
                ToastUtil.showToast(getString(R.string.SCENE_SAVED));
            }
        });

        TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName(AIRecognitionFragment.class.getSimpleName());
        swMotionAI.showRedHint(node != null && node.getNodeCount() > 0);
    }

    /**
     * @Deprecated 需要根据设备属性表
     */
    private void showDetail(boolean show) {
        IProperty property = BaseApplication.getAppComponent().getProductProperty();
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        boolean protection = property.hasProperty(device.pid, "PROTECTION");
        boolean warmsound = property.hasProperty(device.pid, "WARMSOUND");
        boolean enableAI = property.hasProperty(device.pid, "AI_RECOGNITION");//todo 暂时还没有定义该字段
        boolean warmInterval = property.hasProperty(device.pid, "INTERVAL_ALARM");//todo 暂时还没有定义该字段
        boolean infrared_enhanced_recognition = property.hasProperty(device.pid, "INFRARED_ENHANCED_RECOGNITION");
        boolean detection_zone_setting = property.hasProperty(device.pid, "DETECTION_ZONE_SETTING");
        //先隐藏
        detection_zone_setting = true;
        int pid = device.pid;
        if (pid == 10 || pid == 18 || pid == 36 || pid == 37 || pid == 4 || pid == 5 || pid == 7 || pid == 17) {
            warmInterval = false;
        }


        tvMotionDetectionTitle.setVisibility(protection ? View.VISIBLE : View.GONE);
        flProtectionTitle.setVisibility(protection && show ? View.VISIBLE : View.GONE);

        swMotionDetection.setVisibility(protection ? View.VISIBLE : View.GONE);
        fLayoutProtectionSensitivity.setVisibility(protection && show ? View.VISIBLE : View.GONE);
        fLayoutProtectionWarnEffect.setVisibility(warmsound && show ? View.VISIBLE : View.GONE);

        ll24RecordContainer.setVisibility(protection && show ? View.VISIBLE : View.GONE);
        swMotionAI.setVisibility(enableAI && show ? View.VISIBLE : View.GONE);
        swMotionInterval.setVisibility(warmInterval && show ? View.VISIBLE : View.GONE);

        swInfraredStrengthen.setVisibility(show && infrared_enhanced_recognition ? View.VISIBLE : View.GONE);
        rlMonitorAreaContainer.setVisibility(show && detection_zone_setting ? View.VISIBLE : View.GONE);
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
    public void onDetach() {
        super.onDetach();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    private Subscription subscription;

    private void updateDetails() {
        subscription = Observable.just("update: ")
                .subscribeOn(Schedulers.io())
                .delay(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(ret -> !isDetached())
                .subscribe(what -> {
                    Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                    //提示音
                    DpMsgDefine.DPNotificationInfo notificationInfo = device.$(504, new DpMsgDefine.DPNotificationInfo());
                    fLayoutProtectionWarnEffect.setSubTitle(getString(notificationInfo.notification == 0
                            ? R.string.MUTE : (notificationInfo.notification == 1
                            ? R.string.BARKING : R.string.ALARM)));
                    //灵敏度
                    int s = device.$(ID_503_CAMERA_ALARM_SENSITIVITY, 1);
                    fLayoutProtectionSensitivity.setSubTitle(s == 0 ? getString(R.string.SENSITIVI_LOW)
                            : (s == 1 ? getString(R.string.SENSITIVI_STANDARD) : getString(R.string.SENSITIVI_HIGHT)));
                    //报警周期
                    DpMsgDefine.DPAlarmInfo info = device.$(502, new DpMsgDefine.DPAlarmInfo());
                    fLayoutProtectionRepeatPeriod.setSubTitle(presenter.getRepeatMode(getContext()));
                    if (info != null) {
                        fLayoutProtectionStartTime.setSubTitle(MiscUtils.parse2Time(info.timeStart));
                        fLayoutProtectionEndTime.setSubTitle(MiscUtils.parse2Time(info.timeEnd));
                    }

                    //报警间隔
                    int warnInterval = device.$(DpMsgMap.ID_514_CAM_WARNINTERVAL, 0);
                    int sec = warnInterval / 60;
                    swMotionInterval.setSubTitle(sec > 0 ? "" + sec + getString(R.string.MINUTE_Cloud) : "30" + getString(R.string.REPEAT_TIME));

                    int[] objectDetect = device.$(DpMsgMap.ID_515_CAM_ObjectDetect, new int[]{0});
                    if (objectDetect == null || objectDetect.length == 0) {
                        //未开启 AI 识别
                        swMotionAI.setSubTitle(getString(R.string.Tap1_Setting_Unopened));
                    } else {
                        swMotionAI.setSubTitle(JConstant.getAIText(objectDetect));
                    }

                }, throwable -> AppLogger.d("err:" + throwable.getLocalizedMessage()));
    }

    @OnClick({
            R.id.fLayout_protection_sensitivity,
            R.id.fLayout_protection_warn_effect,
            R.id.fLayout_protection_start_time,
            R.id.fLayout_protection_end_time,
            R.id.fLayout_protection_repeat_period,
            R.id.sw_motion_AI,
            R.id.sw_motion_interval,
            R.id.sw_monitoring_area
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fLayout_protection_sensitivity: {
                SetSensitivityDialogFragment fragment = SetSensitivityDialogFragment.newInstance(getArguments());
                fragment.setAction((int id, Object value) -> {
                    if (value != null && value instanceof Integer) {
                        int level = (int) value;
                        DpMsgDefine.DPPrimary<Integer> wFlag = new DpMsgDefine.DPPrimary<>();
                        wFlag.value = level;
                        presenter.updateInfoReq(wFlag, ID_503_CAMERA_ALARM_SENSITIVITY);
                        fLayoutProtectionSensitivity.setSubTitle(level == 0 ? getString(R.string.SENSITIVI_LOW)
                                : (level == 1 ? getString(R.string.SENSITIVI_STANDARD) : getString(R.string.SENSITIVI_HIGHT)));
                        ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                    }
                });
                fragment.setArguments(getArguments());
                showFragment(fragment);
            }
            break;
            case R.id.fLayout_protection_warn_effect: {
                AlarmSoundEffectFragment fragment = AlarmSoundEffectFragment.newInstance(getArguments());
                fragment.setCallBack((Object o) -> updateDetails());
                loadFragment(android.R.id.content, fragment);
            }
            break;
            case R.id.fLayout_protection_start_time: {
                ViewUtils.deBounceClick(view);
                TimePickDialogFragment timePickDialogFragment = TimePickDialogFragment.newInstance(getBundle(getString(R.string.FROME)));
                timePickDialogFragment.setAction((int id, Object value) -> {
                    if (value != null && value instanceof Integer) {
                        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
                        if (info.timeStart != (int) value) {
                            info.timeStart = (int) value;
                            presenter.updateInfoReq(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
                        }
                        updateDetails();
                        ToastUtil.showToast(getString(R.string.PWD_OK_2));
                    }
                });
                if (getActivity().getSupportFragmentManager().findFragmentByTag("timePickDialogFragmentStart") != null) {
                    return;
                }
                timePickDialogFragment.show(getActivity().getSupportFragmentManager(), "timePickDialogFragmentStart");
            }
            break;
            case R.id.fLayout_protection_end_time: {
                TimePickDialogFragment timePickDialogFragment = TimePickDialogFragment.newInstance(getBundle(getString(R.string.TO)));
                timePickDialogFragment.setAction((int id, Object value) -> {
                    if (value != null && value instanceof Integer) {
                        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
                        if (info.timeEnd != (int) value) {
                            info.timeEnd = (int) value;
                            presenter.updateInfoReq(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
                        }
                        updateDetails();
                        ToastUtil.showToast(getString(R.string.PWD_OK_2));
                    }
                });
                if (getActivity().getSupportFragmentManager().findFragmentByTag("timePickDialogFragmentEnd") != null) {
                    return;
                }
                timePickDialogFragment.show(getActivity().getSupportFragmentManager(), "timePickDialogFragmentEnd");
            }
            break;
            case R.id.fLayout_protection_repeat_period: {
                int index = device.$(502, new DpMsgDefine.DPAlarmInfo()).day;
                WeekFragment weekFragment = WeekFragment.Companion.newInstance(index);
                weekFragment.setCallBack(value -> {
                    ToastUtil.showToast("good?" + value);
                    if (value != null && value instanceof Integer) {
                        int result = (int) value;
                        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                        DpMsgDefine.DPAlarmInfo info = device.$(DpMsgMap.ID_502_CAMERA_ALARM_INFO, new DpMsgDefine.DPAlarmInfo());
                        if (info.day != result) {
                            info.day = result;
                            presenter.updateInfoReq(info, DpMsgMap.ID_502_CAMERA_ALARM_INFO);
                        }
                        updateDetails();
                        ToastUtil.showToast(getString(R.string.PWD_OK_2));
                    }
                });
                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), weekFragment, android.R.id.content);
            }
            break;

            case R.id.sw_motion_AI: {
                AIRecognitionFragment aiRecognitionFragment = AIRecognitionFragment.newInstance(uuid);
                aiRecognitionFragment.setCallBack(result -> {
                    swMotionAI.showRedHint(false);
                    if (result instanceof int[]) {
                        int[] select = (int[]) result;
                        int[] objectDetect = device.$(DpMsgMap.ID_515_CAM_ObjectDetect, new int[]{});

                        List<Integer> list1 = new ArrayList<>(objectDetect.length);
                        List<Integer> list2 = new ArrayList<>(select.length);

                        for (int object : objectDetect) {
                            list1.add(object);
                        }

                        for (int i : select) {
                            list2.add(i);
                        }
                        if (list1.size() != list2.size() || ListUtils.getDiff(list1, list2).size() != 0) {
                            objectDetect = select;
                            presenter.updateInfoReq(new DpMsgDefine.DPPrimary<>(objectDetect), DpMsgMap.ID_515_CAM_ObjectDetect);
                            updateDetails();
                            ToastUtil.showToast(getString(R.string.PWD_OK_2));
                        }
                    }
                });
                ActivityUtils.addFragmentSlideInFromRight(getFragmentManager(), aiRecognitionFragment, android.R.id.content);
            }
            break;

            case R.id.sw_motion_interval: {
                WarmIntervalFragment fragment = WarmIntervalFragment.newInstance(getBundle(getString(R.string.SECURE_Interval_Alarm)));
                fragment.setAction((id, value) -> {
                    if (value != null && value instanceof Integer) {
                        Integer result = (Integer) value;
                        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
                        int info = device.$(DpMsgMap.ID_514_CAM_WARNINTERVAL, 0);
                        if (info != result) {
                            info = result;
                            presenter.updateInfoReq(new DpMsgDefine.DPPrimary<>(info), DpMsgMap.ID_514_CAM_WARNINTERVAL);
                            updateDetails();
                            ToastUtil.showToast(getString(R.string.PWD_OK_2));
                        }
                    }
                });
                showFragment(fragment);
            }
            break;

            case R.id.sw_monitoring_area: {
                MonitorAreaSettingFragment fragment = MonitorAreaSettingFragment.Companion.newInstance(uuid);
                ActivityUtils.addFragmentToActivity(getFragmentManager(), fragment, android.R.id.content);
            }
            break;
        }
    }

    private Bundle getBundle(String title) {
        Bundle bundle = getArguments();
        bundle.putString(KEY_TITLE, title);
        return bundle;
    }

    private void showFragment(DialogFragment fragment) {
        if (fragment != null
                && !fragment.isResumed()
                && getActivity() != null) {
            fragment.show(getActivity().getSupportFragmentManager(), fragment.getClass().getSimpleName());
        }
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onAIStrategyRsp() {
        // TODO: 2017/8/1 获取当前账号 AI 策略,根据策略和设备属性表来决定是否显示 AI 选项

    }

    @Override
    public void deviceUpdate(Device device) {
        updateDetails();
    }

    @Override
    public void deviceUpdate(JFGDPMsg msg) throws IOException {
        if (msg.id == 501 || msg.id == 502 || msg.id == 503 || msg.id == 504 || msg.id == 514 || msg.id == 515) {
            updateDetails();
        }
    }
}
