package com.cylan.jiafeigou;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.ads.AdsActivity;
import com.cylan.jiafeigou.ads.AdsStrategy;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.module.LoginHelper;
import com.cylan.jiafeigou.n.engine.AppServices;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.splash.SmartCallPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.login.LoginFragment;
import com.cylan.jiafeigou.n.view.splash.BeforeLoginFragment;
import com.cylan.jiafeigou.n.view.splash.GuideFragmentV3_2;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;

import butterknife.BindView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by chen on 5/24/16.
 */
@RuntimePermissions
public class SmartcallActivity extends NeedLoginActivity<SplashContract.Presenter>
        implements SplashContract.View {

    @BindView(R.id.fLayout_splash)
    FrameLayout fLayoutSplash;
    @BindView(R.id.tv_copy_right)
    TextView tvCopyRight;
    @BindView(R.id.welcome_switcher)
    ViewSwitcher welcomeSwitcher;
    boolean isADOver = false;

    @Override
    protected boolean onSetContentView() {
        setContentView(R.layout.activity_welcome_page);
        initPresenter();
        return true;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        PerformanceUtils.stopTrace("SmartcallActivity");
        IMEUtils.fixFocusedViewLeak(getApplication());
        startService(new Intent(this, AppServices.class));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !PermissionUtils.hasSelfPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            SmartcallActivityPermissionsDispatcher.showWriteStoragePermissionsWithCheck(this);
        } else {
            showWriteStoragePermissions();
        }

    }

    @Override
    protected int[] getOverridePendingTransition() {
        return new int[]{R.anim.alpha_in, R.anim.alpha_out};
    }

    private void initPresenter() {
        presenter = new SmartCallPresenterImpl(this);
    }

    @Override
    public boolean performBackIntercept(boolean willExit) {
        View view = findViewById(android.R.id.content);
        if (view != null && ((ViewGroup) view).getChildCount() >= 2) {
            if (((ViewGroup) view).getChildAt(1).getId() == R.id.rLayout_login) {
                getSupportFragmentManager().popBackStack();
            }
            return true;
        }
        return super.performBackIntercept(willExit);
    }

    @Override
    protected int[] getExitOverridePendingTransition() {
        return new int[]{R.anim.alpha_in, R.anim.alpha_out};
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SmartcallActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    /**
     * 拒绝也弹框,知道用户做出,app弹框的选择.
     */
    private void reEnablePermission() {
        Log.d("reEnablePermission", "reEnablePermission");
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.permission_auth, getString(R.string.VALID_STORAGE)), getString(R.string.permission_auth, getString(R.string.VALID_STORAGE)),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                },
                getString(R.string.CANCEL), (DialogInterface dialog, int which) -> finish());
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void onWriteStoragePermissionsDenied() {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + ",onWriteSdCardDenied");
        AppLogger.permissionGranted = false;
        reEnablePermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("onNewIntent", "onNewIntent");
        if (intent != null && intent.hasExtra(JConstant.KEY_NEED_LOGIN)) {
            enterBeforeLogin();
        }
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void showWriteStoragePermissions() {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showWriteSdCard");
        AppLogger.permissionGranted = true;
        if (!AdsStrategy.hasAdsChecked()) {
            presenter.deciderShowAdvert();
        } else {
            onAdvertOver();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);//接入了友盟登录功能,必须调用 super
        switch (requestCode) {
            case JConstant.CODE_AD_FINISH: {
                onAdvertOver();
            }
            break;
        }
    }

    @NeedsPermission({Manifest.permission.SYSTEM_ALERT_WINDOW})
    public void showAlertWindowPermissions() {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showAlertWindowPermissions");
    }

    @Override
    public void onShowAdvert(AdsStrategy.AdsDescription adsDescription) {
        AppLogger.d("onShowAdvert:" + adsDescription);
        Intent intent = new Intent(SmartcallActivity.this, AdsActivity.class);
        intent.putExtra(JConstant.KEY_ADD_DESC + JFGRules.getLanguageType(), adsDescription);
        startActivityForResult(intent, JConstant.CODE_AD_FINISH);
    }

    @Override
    public void onAdvertOver() {
        AppLogger.d("onAdvertOver");
        if (!isADOver) {
            isADOver = true;
            deciderFirstAction();
        }
    }

    @Override
    public void onAutoLoginFailed() {
        AppLogger.d("onAutoLoginFailed");
        enterBeforeLogin();
    }

    @Override
    public void onPasswordChanged() {
        AppLogger.d("onPasswordChanged");
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(this);
        builder.setMessage(R.string.PWD_CHANGED)
                .setTitle(R.string.LOGIN_ERR)
                .setPositiveButton(R.string.OK, (dialog, which) -> enterBeforeLogin());
        AlertDialogManager.getInstance().showDialog(getString(R.string.PWD_CHANGED), this, builder);
    }

    @Override
    public void onAutoLoginSuccess() {
        AppLogger.d("onAutoLoginSuccess");
        enterMain();
    }

    private void enterMain() {
        AppLogger.d("enterMain");
        Intent intent = new Intent(this, NewHomeActivity.class);
        PerformanceUtils.stopTrace("smartCall2LogResult");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishExt();
    }

    public void deciderFirstAction() {
        AppLogger.d("deciderFirstAction");
//        if (hasDecided) {
//            return;
//        }
//        hasDecided = true;
        if (LoginHelper.isFirstUseApp()) {
            //第一次使用 APP 需要进入引导页
            enterUserGuide();
        } else if (LoginHelper.isLoginSuccessful()) {
            enterMain();
        } else if (getIntent().getBooleanExtra(JConstant.FROM_LOG_OUT, false)) {
            //进入登录页面
            enterBeforeLogin();
        } else {
            presenter.performAutoLogin();
        }
    }

    private void enterLogin() {
        AppLogger.d("enterLogin");
        welcomeSwitcher.setDisplayedChild(1);
        Bundle bundle = new Bundle();
        bundle.putBoolean(JConstant.KEY_SHOW_LOGIN_FRAGMENT_EXTRA, true);
        bundle.putInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID, android.R.id.content);
        bundle.putInt(JConstant.KEY_SHOW_LOGIN_FRAGMENT, 1);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(BeforeLoginFragment.class.getSimpleName());
        if (fragment == null) {
            fragment = BeforeLoginFragment.newInstance(null);
            ActivityUtils.replaceFragmentNoAnimation(R.id.welcome_frame_container, getSupportFragmentManager(), fragment);

        }
        fragment = getSupportFragmentManager().findFragmentByTag(LoginFragment.class.getSimpleName());
        if (fragment == null) {
            fragment = LoginFragment.newInstance(bundle);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment, R.id.welcome_frame_container);
        } else {
            getSupportFragmentManager().beginTransaction().show(fragment).commit();
        }

    }

    private void enterBeforeLogin() {
        AppLogger.d("enterBeforeLogin");
        welcomeSwitcher.setDisplayedChild(1);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(BeforeLoginFragment.class.getSimpleName());
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().show(fragment).commitAllowingStateLoss();
        } else {
            fragment = BeforeLoginFragment.newInstance(null);
            ActivityUtils.replaceFragmentNoAnimation(R.id.welcome_frame_container, getSupportFragmentManager(), fragment);
        }
    }

    public void enterUserGuide() {
        AppLogger.d("enterUserGuide");
        welcomeSwitcher.setDisplayedChild(1);
        Bundle bundle = new Bundle();
        bundle.putInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID, R.id.welcome_frame_container);
        GuideFragmentV3_2 fragment = GuideFragmentV3_2.newInstance();
        fragment.setArguments(bundle);
        ActivityUtils.replaceFragmentNoAnimation(R.id.welcome_frame_container, getSupportFragmentManager(), fragment);
    }
}
