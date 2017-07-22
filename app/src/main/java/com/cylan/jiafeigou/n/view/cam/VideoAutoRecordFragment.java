package com.cylan.jiafeigou.n.view.cam;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.pty.IProperty;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.setting.VideoAutoRecordContract;
import com.cylan.jiafeigou.n.mvp.impl.setting.VideoAutoRecordPresenterImpl;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.SettingItemView0;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_303_DEVICE_AUTO_VIDEO_RECORD;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_501_CAMERA_ALARM_FLAG;
import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_IS_BELL;
import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoAutoRecordFragment#newInstance} factory method to
 * fetch an instance of this fragment.
 */
@Badge(parentTag = "CamSettingActivity", asRefresh = true)
public class VideoAutoRecordFragment extends IBaseFragment<VideoAutoRecordContract.Presenter>
        implements VideoAutoRecordContract.View {

    @BindView(R.id.rg_auto_record_mode)
    RadioGroup rgAutoRecordMode;
    @BindView(R.id.rb_motion)
    RadioButton rbMotion;
    @BindView(R.id.rb_24_hours)
    RadioButton rb24Hours;
    @BindView(R.id.rb_never)
    RadioButton rbNever;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.lLayout_mode_motion)
    SettingItemView0 siv_mode_motion;
    @BindView(R.id.lLayout_mode_24_hours)
    SettingItemView0 siv_mode_24_hours;
    @BindView(R.id.lLayout_mode_never)
    SettingItemView0 siv_mode_never;
    private String uuid;
    private int oldOption;

    @BindView(R.id.rl_alarm_setting_container)
    RelativeLayout rlAlarmSettingContainer;
    @BindView(R.id.rl_watch_video_container)
    RelativeLayout rlWatchVideoContainer;
    @BindView(R.id.siv_watch_video_switcher)
    SettingItemView0 sivWatchVideoSwitcher;
    private boolean isBell = false;

    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        this.isBell = getArguments().getBoolean(KEY_DEVICE_ITEM_IS_BELL, false);
        basePresenter = new VideoAutoRecordPresenterImpl(this, uuid);
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @param args Parameter 2.
     * @return A new instance of fragment SafeProtectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoAutoRecordFragment newInstance(Bundle args) {
        VideoAutoRecordFragment fragment = new VideoAutoRecordFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.getAppComponent().getTreeHelper().markNodeRead(this.getClass().getSimpleName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video_auto_record, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        IProperty property = BaseApplication.getAppComponent().getProductProperty();
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        boolean record24 = property.hasProperty(device.pid, "24RECORD");

        rb24Hours.setVisibility(record24 ? View.VISIBLE : View.GONE);
        siv_mode_24_hours.setVisibility(record24 ? View.VISIBLE : View.GONE);

        boolean isRSBell = JFGRules.isRsBell(device.pid);
        siv_mode_never.setVisibility(isRSBell ? View.GONE : View.VISIBLE);
        rbNever.setVisibility(isRSBell ? View.GONE : View.VISIBLE);
        siv_mode_motion.setSwitcherVisibility(isRSBell ? View.VISIBLE : View.GONE);

        rbMotion.setVisibility(isRSBell ? View.GONE : View.VISIBLE);

        DpMsgDefine.DPStandby standby = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(508, new DpMsgDefine.DPStandby());
        customToolbar.setBackAction(v -> getFragmentManager().popBackStack());
        boolean isRs = JFGRules.isRS(device.pid);
        oldOption = device.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, isRs && !isRSBell ? 2 : -1);

        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
        if (!status.hasSdcard) oldOption = -1;
        boolean alarm = device.$(DpMsgMap.ID_501_CAMERA_ALARM_FLAG, false);
        if (!alarm) oldOption = -1;
        if (!isRSBell) {
            rbMotion.setChecked(oldOption == 0);
            rb24Hours.setChecked(oldOption == 1);
            rbNever.setChecked(oldOption == 2);
        }
        AppLogger.d("");
        siv_mode_motion.setOnCheckedChangeListener(null);
        siv_mode_motion.setChecked(oldOption == 0);
        siv_mode_motion.setOnCheckedChangeListener(this::onSwitcherModeMotion);
        if (property.hasProperty(device.pid, "VIDEO")) {
            rlAlarmSettingContainer.setVisibility(View.GONE);
            rlWatchVideoContainer.setVisibility(View.VISIBLE);
            DpMsgDefine.DPAutoRecordWatcher recordWatcher = device.$(305, new DpMsgDefine.DPAutoRecordWatcher(false));
            onRecordWatcherSync(recordWatcher);
        } else {
            rlAlarmSettingContainer.setVisibility(View.VISIBLE);
            rlWatchVideoContainer.setVisibility(View.GONE);
        }
        onSDCardSync(status);
        sivWatchVideoSwitcher.setOnCheckedChangeListener(this::clickWatchVideoSwitcher);
    }

    private void onSwitcherModeMotion(CompoundButton button, boolean checked) {
        AppLogger.d("Aaaaaaaaaaaaaaaaa");
        if (checked) {
            if (!hasSdcard()) {//先提示没有 sd卡再提示关闭移动侦测
                ToastUtil.showToast(getString(R.string.has_not_sdcard));
                siv_mode_motion.setChecked(false);
                return;
            }
            if (!alarmDisable()) {
                siv_mode_motion.setChecked(false);
                openAlarm(0);
                return;
            }
        }
        DpMsgDefine.DPPrimary<Integer> flag = new DpMsgDefine.DPPrimary<>();
        oldOption = flag.value = checked ? 0 : -1;
        if (oldOption == -1) oldOption = 2;
        basePresenter.updateInfoReq(flag, ID_303_DEVICE_AUTO_VIDEO_RECORD);
    }

    private void clickWatchVideoSwitcher(CompoundButton button, boolean checked) {
        Device device = DataSourceManager.getInstance().getDevice(uuid);
        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
        if (checked && !status.hasSdcard) {
            ToastUtil.showNegativeToast(getString(R.string.NO_SDCARD));
            button.setChecked(false);
            return;
        }
        AppLogger.d("开启自动录像:" + checked);
        basePresenter.updateInfoReq(new DpMsgDefine.DPAutoRecordWatcher(checked), 305);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        int a = device.$(ID_303_DEVICE_AUTO_VIDEO_RECORD, -1);
        if (oldOption != a && oldOption != -1) {
            ToastUtil.showToast(getString(R.string.SCENE_SAVED));
        }
    }


    @Override
    public void setPresenter(VideoAutoRecordContract.Presenter presenter) {
        basePresenter = presenter;
    }

    @OnClick({R.id.rb_motion, R.id.lLayout_mode_24_hours, R.id.lLayout_mode_never})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lLayout_mode_motion: {
                if (!hasSdcard()) {//先提示没有 sd卡再提示关闭移动侦测
                    ToastUtil.showToast(getString(R.string.has_not_sdcard));
                    return;
                }
                if (!alarmDisable()) {
                    openAlarm(0);
                    return;
                }

                rbMotion.setChecked(true);
                DpMsgDefine.DPPrimary<Integer> flag = new DpMsgDefine.DPPrimary<>();
                flag.value = 0;
                basePresenter.updateInfoReq(flag, ID_303_DEVICE_AUTO_VIDEO_RECORD);
            }
            break;
            case R.id.lLayout_mode_24_hours: {
//                if (!alarmDisable()) {
//                    openAlarm(1);
//                    return;
//                }
                if (!hasSdcard()) {
                    ToastUtil.showToast(getString(R.string.has_not_sdcard));
                    return;
                }
                rb24Hours.setChecked(true);
                DpMsgDefine.DPPrimary<Integer> flag = new DpMsgDefine.DPPrimary<>();
                flag.value = 1;
                basePresenter.updateInfoReq(flag, ID_303_DEVICE_AUTO_VIDEO_RECORD);
            }
            break;
            case R.id.lLayout_mode_never: {
                if (!hasSdcard()) {
                    ToastUtil.showToast(getString(R.string.has_not_sdcard));
                    return;
                }
                rbNever.setChecked(true);
                DpMsgDefine.DPPrimary<Integer> flag = new DpMsgDefine.DPPrimary<>();
                flag.value = 2;
                basePresenter.updateInfoReq(flag, ID_303_DEVICE_AUTO_VIDEO_RECORD);
            }
            break;
        }
    }

    private void openAlarm(final int index) {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.RECORD_ALARM_OPEN))
                .setPositiveButton(getString(R.string.OPEN), (DialogInterface dialog, int which) -> {
                    if (index == 0 && !hasSdcard()) {
                        ToastUtil.showToast(getString(R.string.has_not_sdcard));
                        return;
                    }
                    DpMsgDefine.DPPrimary<Boolean> wFlag = new DpMsgDefine.DPPrimary<>();
                    wFlag.value = true;
                    basePresenter.updateInfoReq(wFlag, ID_501_CAMERA_ALARM_FLAG);
                    ToastUtil.showToast(getString(R.string.SCENE_SAVED));
                    if (index == 1)
                        rb24Hours.setChecked(true);
                    else {
                        rbMotion.setChecked(true);
                        siv_mode_motion.setChecked(true);
                    }
                    DpMsgDefine.DPPrimary<Integer> flag = new DpMsgDefine.DPPrimary<>();
                    flag.value = index;
                    basePresenter.updateInfoReq(flag, ID_303_DEVICE_AUTO_VIDEO_RECORD);
                })
                .setNegativeButton(getString(R.string.CANCEL), null)
                .show();
    }

    private boolean alarmDisable() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        return device.$(ID_501_CAMERA_ALARM_FLAG, false);
    }

    private boolean hasSdcard() {
        Device device = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid);
        DpMsgDefine.DPSdStatus status = device.$(204, new DpMsgDefine.DPSdStatus());
        return status != null && status.hasSdcard && status.err == 0;
    }

    /**
     * 1、首次在新版本添加设备后：“功能设置”图标、“录像设置”标上小红点提醒，点击进入“录像设置”后返回小红点消失。
     * 2、“监看录像”默认关闭状态。
     * 3、“监看录像”只有在插入SD卡后，才能开启，未插入SD卡时，点击开关按钮开启时轻提示①
     * 4、拔出SD卡后，“监看录像”开关自动切换至关闭状态。
     * 5、插入SD卡后，开关按钮默认处于关闭状态，只能手动开启。
     * 6、开启此功能后当点击设备上的“监看”按钮或是点击客户端“播放”按钮都会产生监看录像历史视频，可通过滑动历史录像时间轴查看。
     * 7、AP模式下不录像。
     * 8、开启监看录像，设备连上服务器且插入SD卡时，
     * A、按下设备“监看键”时录10s视频，10s内再按“监看键”不会重复计算。
     * B、点击客户端“播放”键时录视频，视频时长：点击播放开始到暂停播放或是跳转到其他页面、查看历史录像时结束。
     * C、在设备端点击“监看键”后10s内点击客户端的“播放”按钮，或是，先点击客户端的“播放”按钮后10s内点击设备端的“监看键”，录视频时长：(最先监看时间开始--客户端暂停播放结束)。
     * 最先监看时间开始：设备端“监看键”或是客户端“播放”按钮，先点击哪个，就从该时间点算起。
     * 客户端暂停播放结束：只要客户端直播停止播放，就视为结束。
     */
    @Override
    public void onSDCardSync(DpMsgDefine.DPSdStatus status) {
        if (!status.hasSdcard) {
            sivWatchVideoSwitcher.setChecked(false, false);
//            sivWatchVideoSwitcher.setCheckEnable(false);
        }
    }

    @Override
    public void onRecordWatcherSync(DpMsgDefine.DPAutoRecordWatcher recordWatcher) {
        sivWatchVideoSwitcher.setChecked(recordWatcher.recordEnable);
    }
}
