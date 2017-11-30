package com.cylan.jiafeigou.n.view.home;

import android.content.DialogInterface;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.ex.JfgException;
import com.cylan.jfgapp.jni.JfgAppCmd;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.module.Command;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeSettingPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.ResolveInfoEx;
import com.cylan.jiafeigou.n.view.mine.BindMailFragment;
import com.cylan.jiafeigou.n.view.mine.MineInfoBindPhoneFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.cylan.jiafeigou.widget.dialog.ShareToListDialog;
import com.google.gson.Gson;
import com.tencent.mm.opensdk.modelbiz.JumpToBizProfile;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.cylan.jiafeigou.misc.JFGRules.LANGUAGE_TYPE_SIMPLE_CHINESE;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
@Badge(parentTag = "HomeMineFragment")
public class HomeSettingFragment extends IBaseFragment<HomeSettingContract.Presenter> implements
        HomeSettingContract.View, UMAuthListener {
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
    @BindView(R.id.sv_setting_wechat_switch)
    SettingItemView1 svSettingWechatSwitch;

    private List<ResolveInfoEx> finalList;
    @BindView(R.id.btn_to_info)
    TextView btnHomeMinePersonalInformation;

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
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateHint();
        svHomeSettingAbout.setVisibility(getResources().getBoolean(R.bool.show_about) ? View.VISIBLE : View.GONE);
        customToolbar.setBackAction(click -> getActivity().getSupportFragmentManager().popBackStack());
        svHomeSettingClear.setSubTitle(presenter.calculateCacheSize());
    }

    private void initPresenter() {
        presenter = new HomeSettingPresenterImp(this);
    }

    @OnClick({R.id.sv_home_setting_about,
            R.id.sv_home_setting_clear,
            R.id.sv_home_setting_recommend,
            R.id.btn_to_info
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sv_home_setting_about:
                ViewUtils.deBounceClick(view);
                AppLogger.e("sv_home_setting_about");
                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                        aboutFragment, android.R.id.content);
                break;
            case R.id.sv_home_setting_clear:
                if ("0.0M".equals(svHomeSettingClear.getSubTitle())) {
                    return;
                }
                presenter.clearCache();
                break;
            case R.id.sv_home_setting_recommend:
                //推荐给亲友
                Observable.just("loadList")
                        .subscribeOn(Schedulers.io())
                        .map(s -> {
                            if (!ListUtils.isEmpty(finalList)) {
                                return finalList;
                            }
                            final Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, LinkManager.getLinkShareByApp());
                            List<ResolveInfo> list = getContext().getPackageManager().queryIntentActivities(intent, 0);
                            finalList = new LinkedList<>();
                            for (ResolveInfo info : list) {
                                final String name = info.activityInfo.packageName;
                                if (!"com.cloudsync.android.netdisk.activity.NetDiskShareLinkActivity".equals(info.activityInfo.name)) {
                                    if (addFirst(name)) {
                                        finalList.add(0, new ResolveInfoEx().setInfo(info));
                                    } else {
                                        finalList.add(new ResolveInfoEx().setInfo(info));
                                    }
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
            case R.id.btn_to_info:
                showLogOutDialog(view);
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
        svHomeSettingClear.setSubTitle(size);
    }

    @Override
    public void showClearingCacheProgress() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.ClearingTips), true);
    }

    @Override
    public void hideClearingCacheProgress() {
        if (LoadingDialog.isShowLoading()) {
            LoadingDialog.dismissLoading();
        }
    }

    @Override
    public void clearFinish() {
        if (!isAdded()) {
            return;
        }
        svHomeSettingClear.setSubTitle("0.0M");
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
        boolean systemNotificationEnable = NotificationManagerCompat.from(getContext()).areNotificationsEnabled();
        boolean appPushEnable = accountArrived.jfgAccount.isEnablePush() && systemNotificationEnable;
        boolean appSoundEnable = accountArrived.jfgAccount.isEnableSound() && systemNotificationEnable;
        boolean appVibrateEnable = accountArrived.jfgAccount.isEnableVibrate() && systemNotificationEnable;

        svHomeSettingAccessMes.setChecked(appPushEnable, false);
        svSoundContainer.setVisibility(appPushEnable ? View.VISIBLE : View.GONE);
        svVibrateContainer.setVisibility(appPushEnable ? View.VISIBLE : View.GONE);
        svSoundContainer.setChecked(appSoundEnable, false);
        svVibrateContainer.setChecked(appVibrateEnable, false);

        initSwitchBtnListener();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        svSoundContainer.postDelayed(() -> {
            //刷新账号
            //需要等动画完成,否则 homeMine 会有闪烁
            JfgAppCmd.getInstance().getAccount();
        },1000);
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
                presenter.savaSwitchState(isChecked, JConstant.RECEIVE_MESSAGE_NOTIFICATION);
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
            presenter.savaSwitchState(isChecked, JConstant.OPEN_VOICE);
        });
        svVibrateContainer.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            presenter.savaSwitchState(isChecked, JConstant.OPEN_SHAKE);
        });
        JFGAccount jfgAccount = DataSourceManager.getInstance().getJFGAccount();
        //已经关注公众号
        boolean BizProfile = jfgAccount != null && jfgAccount.getWxPush() == 1 && !TextUtils.isEmpty(jfgAccount.getWXOpenID());
        svHomeSettingWechat.setChecked(BizProfile);
        //更换微信号
        svSettingWechatSwitch.setVisibility(BizProfile && getResources().getBoolean(R.bool.show_wechat_entrance) ? View.VISIBLE : View.GONE);
        svSettingWechatSwitch.setOnClickListener(v -> getAlertDialogManager().showDialog(getActivity(), "qiehuan", getString(R.string.SETTINGS_Wechat_Switch_Open),
                getString(R.string.I_KNOW), (dialog, which) -> dialog.dismiss()));
        //开关 微信推送通知
        svHomeSettingWechat.setVisibility(getResources().getBoolean(R.bool.show_wechat_entrance) ? View.VISIBLE : View.GONE);
        svHomeSettingWechat.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            final JFGAccount account = DataSourceManager.getInstance().getJFGAccount();
            if (isChecked) {
                if (NetUtils.getJfgNetType() == 0) {
                    ToastUtil.showToast(getString(R.string.NoNetworkTips));
                    return;
                }
                //第三方账号需要绑定手机/邮箱
                int type = DataSourceManager.getInstance().getLoginType();
                if (type >= 3 && account != null) {
                    //字符串相加
                    if (TextUtils.isEmpty(account.getEmail() + account.getPhone())) {
                        final String content = getString(type >= 6 ? R.string.Tap3_Friends_NoBindTips
                                : R.string.Tap3_Friends_NoBindTips);
                        showBindPhoneOrEmailDialog(content);
                        return;
                    }
                }
                //绑定过账号,直接
                if (account != null && !TextUtils.isEmpty(account.getWXOpenID())) {
                    try {
                        account.setWXPush(1);
                        Command.getInstance().setAccount(account);
                    } catch (JfgException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                //跳转到
                jump2Guide();
            } else {
                //先setCheck(false),视角上有效果.
                svHomeSettingWechat.setChecked(true);
                AlertDialogManager.getInstance().showDialog(getActivity(), "weixin",
                        getString(R.string.SETTINGS_Wechat_Switch_Cancel), getString(R.string.OK), (dialog, which) -> {
                            svHomeSettingWechat.setChecked(false);
                            try {
                                account.setWXPush(0);
                                Command.getInstance().setAccount(account);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                        }, getString(R.string.CANCEL), null);
            }
        });
    }

    private void jump2Guide() {
        IBaseFragment fragment = WechatGuideFragment.newInstance();
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                fragment,
                android.R.id.content);
        //扫描关注了.
        fragment.setCallBack(t -> {
            //刷新账号
            JfgAppCmd.getInstance().getAccount();
            updateHint();
        });
    }

    private void updateHint() {
        TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName(WechatGuideFragment.class.getSimpleName());
        svHomeSettingWechat.showHint(node != null && node.getNodeCount() > 0);
    }

    /**
     * 弹出绑定手机或者邮箱的提示框
     */
    private void showBindPhoneOrEmailDialog(String title) {
        svHomeSettingWechat.setChecked(false);
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("bindphone");
        if (f == null) {
            AlertDialogManager.getInstance().showDialog(getActivity(), title, title,
                    getString(R.string.Tap2_Index_Open_NoDeviceOption),
                    (DialogInterface dialog, int which) -> {
                        int i = DataSourceManager.getInstance().getLoginType();
                        if (i == 3 || i == 4) {
                            int language = JFGRules.getLanguageType(ContextUtils.getContext());
                            if (language == LANGUAGE_TYPE_SIMPLE_CHINESE) {
                                Bundle bundle = new Bundle();
                                MineInfoBindPhoneFragment bindPhoneFragment = MineInfoBindPhoneFragment.newInstance(bundle);
                                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                                        bindPhoneFragment, android.R.id.content);
                                bindPhoneFragment.setCallBack(t -> updateHint());
                            } else {
                                Bundle bundle = new Bundle();
                                BindMailFragment bindMailFragment = BindMailFragment.newInstance(bundle);
                                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                                        bindMailFragment, android.R.id.content);
                                bindMailFragment.setCallBack(t -> updateHint());
                            }
                        } else if (i == 6 || i == 7) {
                            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                                    BindMailFragment.newInstance(null), android.R.id.content);
                        }
                    }, getString(R.string.CANCEL), (dialog, which) -> svHomeSettingWechat.setChecked(false), false);
        }
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
        BaseApplication.getAppComponent().getTreeHelper().markNodeRead(WechatGuideFragment.class.getSimpleName());
        RxBus.getCacheInstance().post(new RxEvent.InfoUpdate());
    }

    @Override
    public void onStart(SHARE_MEDIA share_media) {
        Log.d("getOpenID", "start: ");
    }

    @Override
    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
        Log.d("getOpenID", "getOpenID: " + new Gson().toJson(map));
//        JFGAccount account = DataSourceManager.getInstance().getJFGAccount();
//        account.
        // {"unionid":"opmVqv7ftlm_34oAF2Q231o8zaRM","screen_name":"test","city":"","accessToken":"1oY6MJhWyiCpeqfxQht8FX8PcO1iG1N0q3ijat4Xp0fAzUhY8YX5pPukTUGJ4v9WRuI4DUhDlQJJaBOrEx3uLZjZlYcPiGGRqXLemmTui1U","refreshToken":"Bewb12Zh-kL6S5knI54clzIib2nYUw-JX_xXyGvPEq2N_32CuIwQgJ-VvD_hqWwXlT2b_xzkT0a8rQb_oxtybtdjXXsUavCMiTyzKY1SnnI","gender":"0","province":"","openid":"ol0PtwnLAXeret_wLZNxGihc546I","profile_image_url":"http://wx.qlogo.cn/mmopen/iatA0oGrrscZV14ibGqUDlKEJ82XxVEhYefw0vepGricPDWEJw1aWdwNMVgKft1jwiaKzhuicOicABxXvDkMLiaOwOflwYicQIicsiaZax/0","country":"中国","access_token":"1oY6MJhWyiCpeqfxQht8FX8PcO1iG1N0q3ijat4Xp0fAzUhY8YX5pPukTUGJ4v9WRuI4DUhDlQJJaBOrEx3uLZjZlYcPiGGRqXLemmTui1U","iconurl":"http://wx.qlogo.cn/mmopen/iatA0oGrrscZV14ibGqUDlKEJ82XxVEhYefw0vepGricPDWEJw1aWdwNMVgKft1jwiaKzhuicOicABxXvDkMLiaOwOflwYicQIicsiaZax/0","name":"test","uid":"opmVqv7ftlm_34oAF2Q231o8zaRM","expiration":"1496930699855","language":"zh_CN","expires_in":"1496930699855"}
        ToastUtil.showToast("成功了." + map.get("openid"));
        //            //跳转微信公众号
        String appId = "wx70338a9ca5e4122e";//开发者平台ID
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
        if (!isAdded()) {
            return;
        }
        LoadingDialog.dismissLoading();
    }

    @Override
    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
        Log.d("getOpenID", "onError: " + throwable);
        if (!isAdded()) {
            return;
        }
        LoadingDialog.dismissLoading();
    }

    @Override
    public void onCancel(SHARE_MEDIA share_media, int i) {
        Log.d("getOpenID", "onCancel: ");
        if (!isAdded()) {
            return;
        }
        LoadingDialog.dismissLoading();
    }

    /**
     * 删除亲友对话框
     */
    public void showLogOutDialog(View v) {
        AlertDialogManager.getInstance().showDialog(getActivity(),
                "showLogOutDialog", getString(R.string.LOGOUT_INFO),
                getString(R.string.LOGOUT), (DialogInterface dialog, int which) -> {
                    JFGAccount jfgAccount = DataSourceManager.getInstance().getJFGAccount();
                    if (jfgAccount != null) {
                        presenter.logOut(jfgAccount.getAccount(), getActivity());
                        //进入登陆页 login page
                        Intent intent = new Intent(getContext(), SmartcallActivity.class);
                        intent.putExtra(JConstant.FROM_LOG_OUT, true);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }, getString(R.string.CANCEL), null, false);
    }


}
