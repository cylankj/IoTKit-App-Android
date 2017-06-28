package com.cylan.jiafeigou.n.view.home;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeSettingPresenterImp;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SafeSwitchButton;
import com.cylan.jiafeigou.widget.ShareGridView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeSettingFragment extends Fragment implements HomeSettingContract.View, CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.rl_home_setting_about)
    RelativeLayout rlHomeSettingAbout;
    @BindView(R.id.rl_home_setting_clear)
    RelativeLayout rlHomeSettingClear;
    @BindView(R.id.progressbar_load_cache_size)
    ProgressBar progressbarLoadCacheSize;
    @BindView(R.id.tv_cache_size)
    TextView tvCacheSize;
    @BindView(R.id.btn_item_switch_accessMes)
    SafeSwitchButton btnItemSwitchAccessMes;
    @BindView(R.id.btn_item_switch_voide)
    SafeSwitchButton btnItemSwitchVoide;
    @BindView(R.id.btn_item_switch_shake)
    SafeSwitchButton btnItemSwitchShake;
    @BindView(R.id.rl_sound_container)
    RelativeLayout rlSoundContainer;
    @BindView(R.id.rl_vibrate_container)
    RelativeLayout rlVibrateContainer;
    @BindView(R.id.rl_home_setting_recommend)
    RelativeLayout rlHomeSettingRecommend;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.btn_home_mine_personal_information)
    TextView btnHomeMinePersonalInformation;

    private HomeSettingContract.Presenter presenter;
    private AboutFragment aboutFragment;
    private Dialog mShareDlg;
    private AppAdapter appAdater;

    public static HomeSettingFragment newInstance() {
        return new HomeSettingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aboutFragment = AboutFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_setting, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        presenter.calculateCacheSize();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rlHomeSettingAbout.setVisibility(getResources().getBoolean(R.bool.show_about) ? View.VISIBLE : View.GONE);
        customToolbar.setBackAction(click -> getActivity().getSupportFragmentManager().popBackStack());
    }

    private void initPresenter() {
        presenter = new HomeSettingPresenterImp(this);
    }

    @Override
    public void setPresenter(HomeSettingContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public String getUuid() {
        return null;
    }

    @OnClick({R.id.rl_home_setting_about, R.id.rl_home_setting_clear, R.id.rl_home_setting_recommend, R.id.btn_home_mine_personal_information
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_home_setting_about:
                ViewUtils.deBounceClick(view);
                AppLogger.e("rl_home_setting_about");
                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                        aboutFragment, android.R.id.content);
                break;

            case R.id.rl_home_setting_clear:
                if ("0.0M".equals(tvCacheSize.getText())) return;
                presenter.clearCache();
                break;

            case R.id.rl_home_setting_recommend:
                //推荐给亲友
                share();
                break;
            case R.id.btn_home_mine_personal_information:
                showLogOutDialog(view);
                break;
        }
    }

    @Override
    public void showLoadCacheSizeProgress() {
        progressbarLoadCacheSize.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadCacheSizeProgress() {
        progressbarLoadCacheSize.setVisibility(View.GONE);
    }

    @Override
    public void setCacheSize(String size) {
        tvCacheSize.setText(size);
    }

    @Override
    public void showClearingCacheProgress() {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.ClearingTips));
    }

    @Override
    public void hideClearingCacheProgress() {
        if (LoadingDialog.isShowing(getActivity().getSupportFragmentManager()))
            LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    @Override
    public void clearFinish() {
        if (isDetached()) return;
        tvCacheSize.setText("0.0M");
        ToastUtil.showToast(getString(R.string.Clear_Sdcard_tips3));
    }

    @Override
    public void clearNoCache() {
        ToastUtil.showToast(getString(R.string.RET_EREPORT_NO_DATA));
    }

    @Override
    public boolean switchAcceptMesg() {
        return presenter.getNegation();
    }

    @Override
    public void initSwitchState(final RxEvent.AccountArrived accountArrived) {
        btnItemSwitchAccessMes.setChecked(accountArrived.jfgAccount.isEnablePush() && NotificationManagerCompat.from(getContext()).areNotificationsEnabled(), false);
        btnItemSwitchVoide.setChecked(accountArrived.jfgAccount.isEnableSound(), false);
        btnItemSwitchShake.setChecked(accountArrived.jfgAccount.isEnableVibrate(), false);
        if (!btnItemSwitchAccessMes.isChecked()) {
            rlSoundContainer.setVisibility(View.GONE);
            rlVibrateContainer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
        if (mShareDlg != null) {
            mShareDlg.dismiss();
            mShareDlg = null;
        }
        if (appAdater != null) {
            appAdater = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
        initSwitchBtnListener();
    }

    private void initSwitchBtnListener() {
        btnItemSwitchAccessMes.setOnCheckedChangeListener(this);
        btnItemSwitchVoide.setOnCheckedChangeListener(this);
        btnItemSwitchShake.setOnCheckedChangeListener(this);

    }

    private void openSetting() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getContext().getPackageName());
        }
        startActivity(localIntent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.btn_item_switch_accessMes:
                boolean notificationsEnabled = NotificationManagerCompat.from(getContext()).areNotificationsEnabled();
                if (!notificationsEnabled && isChecked) {
                    btnItemSwitchAccessMes.setChecked(false);
                    AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivity());
                    builder.setMessage(getString(R.string.LOCAL_NOTIFICATION_AndroidMSG, getString(R.string.SYSTEM)))
                            .setPositiveButton(R.string.WELL_OK, (dialog, which) -> openSetting())
                            .setTitle(R.string.PUSH_MSG);
                    AlertDialogManager.getInstance().showDialog(getString(R.string.LOCAL_NOTIFICATION_AndroidMSG), getActivity(), builder);
                } else {
                    presenter.savaSwitchState(isChecked, JConstant.RECEIVE_MESSAGE_NOTIFICATION);
                    if (!isChecked) {
                        rlSoundContainer.setVisibility(View.GONE);
                        rlVibrateContainer.setVisibility(View.GONE);
                    } else {
                        rlSoundContainer.setVisibility(View.VISIBLE);
                        rlVibrateContainer.setVisibility(View.VISIBLE);
                    }
                }
                break;

            case R.id.btn_item_switch_voide:
                presenter.savaSwitchState(isChecked, JConstant.OPEN_VOICE);
                break;

            case R.id.btn_item_switch_shake:
                presenter.savaSwitchState(isChecked, JConstant.OPEN_SHAKE);
                break;
        }
    }

    private static final String tencent = "tencent";
    private static final String facebook = "facebook";
    private static final String twitter = "twitter";
    private static final String sina = "sina";

    private boolean addFirst(final String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        return name.contains(tencent)
                || name.contains(facebook)
                || name.contains(twitter)
                || name.contains(sina);
    }

    //*************
    public void share() {
        if (mShareDlg == null) {
            mShareDlg = new Dialog(getActivity(), R.style.func_dialog);
            View content = View.inflate(getContext(), R.layout.dialog_app_share, null);
            TextView cancel = (TextView) content.findViewById(R.id.btn_cancle);
            cancel.setOnClickListener(v -> mShareDlg.dismiss());
            ShareGridView gridView = (ShareGridView) content.findViewById(R.id.gridview);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, LinkManager.getLinkShareByApp());
            List<ResolveInfo> list = getContext().getPackageManager().queryIntentActivities(intent, 0);
            if (appAdater == null)
                appAdater = new AppAdapter(getContext());
            LinkedList<ResolveInfoEx> finalList = new LinkedList<>();
            for (ResolveInfo info : list) {
                final String name = info.activityInfo.packageName;
                if (!"com.cloudsync.android.netdisk.activity.NetDiskShareLinkActivity".equals(info.activityInfo.name)) {
                    if (addFirst(name)) finalList.add(0, new ResolveInfoEx().setInfo(info));
                    else finalList.add(new ResolveInfoEx().setInfo(info));
                }
            }
            appAdater.addAll(finalList);
            gridView.setOnItemClickListener((parent, view, position, id) -> {
                ResolveInfo info = appAdater.getItem(position).getInfo();
                intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                startActivity(intent);
            });
            gridView.setAdapter(appAdater);
            mShareDlg.setContentView(content);
            mShareDlg.setCanceledOnTouchOutside(true);
        }
        try {
            if (mShareDlg.isShowing()) return;
            mShareDlg.show();
        } catch (Exception e) {
            AppLogger.e(e.toString());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
    }

    private static class ResolveInfoEx {
        private ResolveInfo info;

        public ResolveInfoEx setInfo(ResolveInfo info) {
            this.info = info;
            return this;
        }

        public ResolveInfo getInfo() {
            return info;
        }
    }

    private class AppAdapter extends ArrayAdapter<ResolveInfoEx> {

        public AppAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (null == convertView) {
                convertView = View.inflate(getContext(), R.layout.item_app_share, null);
                vh = new ViewHolder();
                vh.icon = (ImageView) convertView.findViewById(R.id.icon);
                vh.name = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            ResolveInfo info = getItem(position).getInfo();
            PackageManager pm = getContext().getPackageManager();
            vh.name.setText(info.loadLabel(pm));
            vh.icon.setImageDrawable(info.loadIcon(pm));
            return convertView;
        }
    }

    /**
     * 删除亲友对话框
     */
    public void showLogOutDialog(View v) {
        AlertDialogManager.getInstance().showDialog(getActivity(),
                "showLogOutDialog", getString(R.string.LOGOUT_INFO),
                getString(R.string.LOGOUT), (DialogInterface dialog, int which) -> {
                    JFGAccount jfgAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                    if (jfgAccount != null) {
                        presenter.logOut(jfgAccount.getAccount());
                        //进入登陆页 login page
                        Intent intent = new Intent(getContext(), SmartcallActivity.class);
                        intent.putExtra(JConstant.FROM_LOG_OUT, true);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }, getString(R.string.CANCEL), null, false);
    }


}
