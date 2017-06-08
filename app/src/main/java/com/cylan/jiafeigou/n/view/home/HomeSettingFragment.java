package com.cylan.jiafeigou.n.view.home;

import android.content.Intent;
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
import android.widget.CompoundButton;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeSettingPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.ResolveInfoEx;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.cylan.jiafeigou.widget.dialog.ShareToListDialog;
import com.tencent.mm.opensdk.modelbiz.JumpToBizProfile;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeSettingFragment extends IBaseFragment<HomeSettingContract.Presenter> implements HomeSettingContract.View {
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.sv_home_setting_accessMes)
    SettingItemView1 svHomeSettingAccessMes;
    @BindView(R.id.sv_sound_container)
    SettingItemView1 svSoundContainer;
    @BindView(R.id.sv_vibrate_container)
    SettingItemView1 svVibrateContainer;
    @BindView(R.id.sv_home_setting_wechat)
    SettingItemView1 svHomeSettingWechat;
    @BindView(R.id.sv_home_setting_clear)
    SettingItemView0 svHomeSettingClear;
    @BindView(R.id.sv_home_setting_recommend)
    SettingItemView1 svHomeSettingRecommend;
    @BindView(R.id.sv_home_setting_about)
    SettingItemView1 svHomeSettingAbout;

    private List<ResolveInfoEx> finalList;
    private AboutFragment aboutFragment;

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
        basePresenter.calculateCacheSize();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        svHomeSettingAbout.setVisibility(getResources().getBoolean(R.bool.show_about) ? View.VISIBLE : View.GONE);
        customToolbar.setBackAction(click -> getActivity().getSupportFragmentManager().popBackStack());
    }

    private void initPresenter() {
        basePresenter = new HomeSettingPresenterImp(this);
    }

    @Override
    public void setPresenter(HomeSettingContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @Override
    public String getUuid() {
        return null;
    }

    @OnClick({R.id.sv_home_setting_about,
            R.id.sv_home_setting_clear,
            R.id.sv_home_setting_recommend})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sv_home_setting_about:
                ViewUtils.deBounceClick(view);
                AppLogger.e("sv_home_setting_about");
                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                        aboutFragment, android.R.id.content);
                break;
            case R.id.sv_home_setting_clear:
                if ("0.0M".equals(svHomeSettingClear.getSubTitle())) return;
                basePresenter.clearCache();
                break;
            case R.id.sv_home_setting_recommend:
                //推荐给亲友
                Observable.just("loadList")
                        .subscribeOn(Schedulers.newThread())
                        .map(s -> {
                            if (!ListUtils.isEmpty(finalList)) return finalList;
                            final Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            final String app = getString(R.string.share_content) + getString(R.string.share_to_friends_link, getContext().getPackageName());
                            intent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.getDefault(), app, getContext().getPackageName()));
                            List<ResolveInfo> list = getContext().getPackageManager().queryIntentActivities(intent, 0);
                            finalList = new LinkedList<>();
                            for (ResolveInfo info : list) {
                                final String name = info.activityInfo.packageName;
                                if (!"com.cloudsync.android.netdisk.activity.NetDiskShareLinkActivity".equals(info.activityInfo.name)) {
                                    if (addFirst(name))
                                        finalList.add(0, new ResolveInfoEx().setInfo(info));
                                    else finalList.add(new ResolveInfoEx().setInfo(info));
                                }
                            }
                            return finalList;
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ret -> {
                            ShareToListDialog dialog = new ShareToListDialog();
                            dialog.updateDataList(ret);
                            dialog.show(getActivity().getSupportFragmentManager(), "share");
                        }, AppLogger::e);
                break;
        }
    }

    @Override
    public void showLoadCacheSizeProgress() {
    }

    @Override
    public void hideLoadCacheSizeProgress() {
    }

    @Override
    public void setCacheSize(String size) {
        svHomeSettingClear.setTvSubTitle(size);
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
        ToastUtil.showToast(getString(R.string.Clear_Sdcard_tips3));
    }

    @Override
    public void clearNoCache() {
        ToastUtil.showToast(getString(R.string.RET_EREPORT_NO_DATA));
    }

    @Override
    public boolean switchAcceptMesg() {
        return basePresenter.getNegation();
    }

    @Override
    public void initSwitchState(final RxEvent.AccountArrived accountArrived) {
        boolean enable = accountArrived.jfgAccount.isEnablePush() && NotificationManagerCompat.from(getContext()).areNotificationsEnabled();
        //过滤
//        if (svHomeSettingAccessMes.isChecked() == enable) return;
        svHomeSettingAccessMes.setChecked(enable, false);
        if (!accountArrived.jfgAccount.isEnablePush() && NotificationManagerCompat.from(getContext()).areNotificationsEnabled()) {
            svSoundContainer.setVisibility(View.GONE);
            svVibrateContainer.setVisibility(View.GONE);
            return;
        }
        svSoundContainer.setChecked(accountArrived.jfgAccount.isEnableSound(), false);
        svVibrateContainer.setChecked(accountArrived.jfgAccount.isEnableVibrate(), false);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        initSwitchBtnListener();
//        basePresenter.refreshWechat();
    }

    private void initSwitchBtnListener() {
        svHomeSettingAccessMes.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            boolean notificationsEnabled = NotificationManagerCompat.from(getContext()).areNotificationsEnabled();
            if (!notificationsEnabled && isChecked) {
                svHomeSettingAccessMes.setChecked(false, false);
                AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivity());
                builder.setMessage(getString(R.string.LOCAL_NOTIFICATION_AndroidMSG, getString(R.string.SYSTEM)))
                        .setPositiveButton(R.string.WELL_OK, (dialog, which) -> openSetting())
                        .setTitle(R.string.PUSH_MSG);
                AlertDialogManager.getInstance().showDialog(getString(R.string.LOCAL_NOTIFICATION_AndroidMSG), getActivity(), builder);
            } else {
                basePresenter.savaSwitchState(isChecked, JConstant.RECEIVE_MESSAGE_NOTIFICATION);
                if (!isChecked) {
                    svSoundContainer.setVisibility(View.GONE);
                    svVibrateContainer.setVisibility(View.GONE);
                } else {
                    svSoundContainer.setVisibility(View.VISIBLE);
                    svVibrateContainer.setVisibility(View.VISIBLE);
                }
            }
        });
        svSoundContainer.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            basePresenter.savaSwitchState(isChecked, JConstant.OPEN_VOICE);
        });
        svVibrateContainer.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            basePresenter.savaSwitchState(isChecked, JConstant.OPEN_SHAKE);
        });
        svHomeSettingWechat.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            //跳转微信公众号
            String appId = "jfg_2014";//开发者平台ID
            IWXAPI api = WXAPIFactory.createWXAPI(getActivity(), appId, false);
            if (api.isWXAppInstalled()) {
                JumpToBizProfile.Req req = new JumpToBizProfile.Req();
                req.toUserName = "gh_b0394a4ee894"; // 公众号原始ID
                req.extMsg = "";
                req.profileType = JumpToBizProfile.JUMP_TO_NORMAL_BIZ_PROFILE; // 普通公众号
                api.sendReq(req);
            } else {
                Toast.makeText(getActivity(), getString(R.string.Tap1_Album_Share_NotInstalledTips, getString(R.string.WeChat)), Toast.LENGTH_SHORT).show();
            }
        });

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


    private static final String QQ = "com.qq.mobileqq";
    private static final String WECHAT = "com.qq.mm";
    private static final String FACEBOOK = "com.facebook.katana";
    private static final String FACEBOOK1 = "com.facebook.Mentions";
    private static final String TWITTER = "com.twitter.android";
    private static final String SINA = "com.sina.weibo";
    private static final String SINA1 = "com.weico.international";

    private boolean addFirst(final String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        return TextUtils.equals(name, QQ) ||
                TextUtils.equals(name, FACEBOOK) ||
                TextUtils.equals(name, SINA1) ||
                TextUtils.equals(name, FACEBOOK1) ||
                TextUtils.equals(name, WECHAT) ||
                TextUtils.equals(name, TWITTER) ||
                TextUtils.equals(name, SINA);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
