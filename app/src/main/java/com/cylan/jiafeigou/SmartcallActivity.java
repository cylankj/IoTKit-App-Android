package com.cylan.jiafeigou;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.splash.SmartCallPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.login.LoginFragment;
import com.cylan.jiafeigou.n.view.splash.BeforeLoginFragment;
import com.cylan.jiafeigou.n.view.splash.GuideFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by chen on 5/24/16.
 */
@RuntimePermissions
public class SmartcallActivity extends NeedLoginActivity
        implements SplashContract.View {

    @BindView(R.id.fLayout_splash)
    FrameLayout fLayoutSplash;
    @BindView(R.id.tv_copy_right)
    TextView tvCopyRight;
    @BindView(R.id.welcome_switcher)
    ViewSwitcher welcomeSwitcher;
    @Nullable
    private SplashContract.Presenter presenter;

    //这个页面先请求 sd卡权限
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IMEUtils.fixFocusedViewLeak(getApplication());
        setContentView(R.layout.activity_welcome_page);
        ButterKnife.bind(this);
        initPresenter();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        goAheadAfterPermissionGranted();应该是权限允许之后,才登录,免得登录后,没有权限,导致奔溃
        SmartcallActivityPermissionsDispatcher.showWriteStoragePermissionsWithCheck(this);
    }

    private void goAheadAfterPermissionGranted() {
        //是否登录
        int state = BaseApplication.getAppComponent().getSourceManager().getLoginState();
        if (state == LogState.STATE_ACCOUNT_ON) {
            Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
            startActivity(new Intent(this, NewHomeActivity.class), bundle);
            finish();
        }
        if (presenter != null) {
            presenter.autoLogin();
            boolean showSplash = !getIntent().getBooleanExtra(JConstant.FROM_LOG_OUT, false);
            presenter.selectNext(showSplash);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

    protected int[] getOverridePendingTransition() {
        return new int[]{R.anim.alpha_in, R.anim.alpha_out};
    }

    private void initPresenter() {
        presenter = new SmartCallPresenterImpl(this);
    }

    @Override
    public void onBackPressed() {
        View view = findViewById(R.id.welcome_frame_container);
        if (view != null) {
            View beforeLoginLayout = ((ViewGroup) view).getChildAt(0);
            //此处逻辑和GuideFragment有关
            if (beforeLoginLayout != null
                    && beforeLoginLayout.getId() == R.id.rLayout_before_login
                    && ((ViewGroup) view).getChildCount() == 1) {
                //只有 beforeLoginFragment页面
                finish();
            } else {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    /**
     * 引导页
     */
    private void showGuidePage() {
        Bundle bundle = new Bundle();
        bundle.putInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID, R.id.welcome_frame_container);
        GuideFragment fragment = GuideFragment.newInstance();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.welcome_frame_container, fragment)
                .commitAllowingStateLoss();
    }

    public void signInFirst(Bundle extra) {
        if (extra == null)
            extra = new Bundle();
        extra.putInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID, R.id.welcome_frame_container);
        extra.putInt(JConstant.KEY_SHOW_LOGIN_FRAGMENT, 1);
        loginFragment = LoginFragment.newInstance(extra);
        loginFragment.setArguments(extra);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                loginFragment, R.id.welcome_frame_container, 0);
    }

    /**
     * pre-登陆
     */
    private void showBeforeLoginPage() {
        //进入登陆页 login page
        getSupportFragmentManager().beginTransaction()
                .add(R.id.welcome_frame_container, BeforeLoginFragment.newInstance(null))
                .addToBackStack(BeforeLoginFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    public void splashOver() {
        if (welcomeSwitcher.getDisplayedChild() != 1) welcomeSwitcher.showNext();
        if (isFirstUseApp()) {
            showGuidePage();
        } else if (!isLogin()) {
            showBeforeLoginPage();
        }
    }

    private boolean isLogin() {
        return getSupportFragmentManager().findFragmentByTag(LoginFragment.class.getSimpleName()) != null;
    }

    private void pswChanged() {
        PreferencesUtils.putBoolean(JConstant.SHOW_PASSWORD_CHANGED, false);
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(this);
        builder.setMessage(R.string.PWD_CHANGED)
                .setTitle(R.string.LOGIN_ERR)
                .setPositiveButton(R.string.OK, (dialog, which) -> {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(JConstant.KEY_SHOW_LOGIN_FRAGMENT_EXTRA, true);
                    LoginFragment loginFragment = LoginFragment.newInstance(bundle);
                    loginFragment.setArguments(bundle);
                    if (getSupportFragmentManager().findFragmentByTag(loginFragment.getClass().getSimpleName()) != null) {
                        getSupportFragmentManager().beginTransaction().remove(loginFragment).commitAllowingStateLoss();
                    }
                    getSupportFragmentManager().beginTransaction().show(loginFragment)
                            .commitAllowingStateLoss();
                });
        AlertDialogManager.getInstance().showDialog(getString(R.string.PWD_CHANGED), this, builder);
    }

    private boolean isFirstUseApp() {
        return PreferencesUtils.getBoolean(JConstant.KEY_FRESH, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SmartcallActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (permissions.length == 1) {
            if (TextUtils.equals(permissions[0], Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (grantResults[0] == -1) {
//                    reEnablePermission();
                    return;
                }
                SmartcallActivityPermissionsDispatcher.showWriteStoragePermissionsWithCheck(this);
            }
        }
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

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void showWriteStoragePermissions() {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showWriteSdCard");
        AppLogger.permissionGranted = true;
        if (RxBus.getCacheInstance().hasStickyEvent(RxEvent.ShouldCheckPermission.class)) {
            ((BaseApplication) getApplication()).try2init();
            RxBus.getCacheInstance().removeStickyEvent(RxEvent.ShouldCheckPermission.class);
        }
        goAheadAfterPermissionGranted();
    }

    @NeedsPermission({Manifest.permission.SYSTEM_ALERT_WINDOW})
    public void showAlertWindowPermissions() {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showAlertWindowPermissions");
    }

    @Override
    public void setPresenter(SplashContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void loginSuccess() {
        Bundle bundle = ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left).toBundle();
        startActivity(new Intent(this, NewHomeActivity.class), bundle);
        finish();
    }

    @Override
    public void loginError(int code) {
        AutoSignIn.getInstance().clearPsw();
        splashOver();
        if (PreferencesUtils.getBoolean(JConstant.SHOW_PASSWORD_CHANGED, false)) {
            pswChanged();
        }
    }
}
