package com.cylan.jiafeigou;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.splash.SmartCallPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.splash.BeforeLoginFragment;
import com.cylan.jiafeigou.n.view.splash.GuideFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Created by chen on 5/24/16.
 */
@RuntimePermissions
public class SmartcallActivity extends NeedLoginActivity
        implements SplashContract.View {

    private static final String COPY_RIGHT = "Copyright @ 2005-%s Cylan.All Rights Reserved";
    @BindView(R.id.fLayout_splash)
    FrameLayout fLayoutSplash;
    @BindView(R.id.tv_copy_right)
    TextView tvCopyRight;
    @Nullable
    private SplashContract.Presenter presenter;
    private boolean frist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IMEUtils.fixFocusedViewLeak(getApplication());
        setContentView(R.layout.activity_welcome_page);
        ButterKnife.bind(this);
        initPresenter();
        fullScreen(true);
    }

    /**
     * 进入全屏模式
     *
     * @param full
     */
    private void fullScreen(boolean full) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (full) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }


    @Override
    protected void onStart() {
        super.onStart();
        SmartcallActivityPermissionsDispatcher.showWriteStoragePermissionsWithCheck(this);
        if (!getIntent().getBooleanExtra("from_log_out", false)) {
            if (presenter != null) presenter.start();
        } else {
            splashOver();
        }
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

    @Override
    protected void onResume() {
        super.onResume();
        tvCopyRight.setText(String.format(COPY_RIGHT, simpleDateFormat.format(new Date(System.currentTimeMillis()))));
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
        View view = findViewById(android.R.id.content);
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
    private void initGuidePage() {
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, GuideFragment.newInstance())
                .commitAllowingStateLoss();
    }

    /**
     * pre-登陆
     */
    private void initLoginPage() {
        //进入登陆页 login page
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, BeforeLoginFragment.newInstance(null))
                .addToBackStack(BeforeLoginFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    @Override
    public void splashOver() {
        fullScreen(false);
        //do your business;
        if (isFirstUseApp()) {
            //第一次打开app
            setFirstUseApp();
            initGuidePage();
        } else {
            initLoginPage();
        }
        View v = findViewById(android.R.id.content);
        if (v != null) {
            View splashView = v.findViewById(R.id.fLayout_splash);
            if (splashView != null) {
                ((ViewGroup) v).removeView(splashView);
            }
        }
    }

    @Override
    public void finishDelayed() {
//        StateMaintainer.getAppManager().finishAllActivity();
    }

    @Override
    public void loginResult(int code) {
        if (code == JError.ErrorOK || code == JError.LoginTimeOut || code == JError.NoNet) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                startActivity(new Intent(this, NewHomeActivity.class),
                        ActivityOptionsCompat.makeCustomAnimation(getContext(),
                                R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
            } else {
                startActivity(new Intent(this, NewHomeActivity.class));
            }
            finish();
        }else if(code == JError.StartLoginPage && !frist){
            splashOver();
            RxBus.getCacheInstance().removeStickyEvent(RxEvent.ResultAutoLogin.class);
            frist = true;
        }else if (code == JError.ErrorAccountNotExist){
            ToastUtil.showNegativeToast(getString(R.string.RET_ELOGIN_ACCOUNT_NOT_EXIST));
        }else if (code == JError.ErrorLoginInvalidPass){
            ToastUtil.showNegativeToast(getString(R.string.RET_ELOGIN_ERROR));
        }
    }

    /**
     * check is the app is fresh
     *
     * @return
     */
    private boolean isFirstUseApp() {
        return PreferencesUtils.getBoolean(JConstant.KEY_FRESH, true);
        // return true;
    }

    private void setFirstUseApp() {
        PreferencesUtils.putBoolean(JConstant.KEY_FRESH, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SmartcallActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        if (permissions.length == 1) {
            if (TextUtils.equals(permissions[0], Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                SmartcallActivityPermissionsDispatcher.showPhonePermissionsWithCheck(this);
            }
        }
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void onWriteStoragePermissionsDenied() {
        // NOTE: Perform option that requires the permission.
        // If this is run by PermissionsDispatcher, the permission will have been granted
//        Toast.makeText(this, "请你开启SD卡读写权限,应用才能正常工作", Toast.LENGTH_SHORT).show();
        if (presenter != null) presenter.finishAppDelay();
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "onWriteSdCardDenied");
    }

    @OnPermissionDenied(Manifest.permission.READ_PHONE_STATE)
    public void onPhoneStateDenied() {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showWriteSdCard");
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE})
    public void showWriteStoragePermissions() {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showWriteSdCard");
    }

    @NeedsPermission({Manifest.permission.READ_PHONE_STATE})
    public void showPhonePermissions() {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showWriteSdCard");
    }


    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onCameraDenied() {
        // NOTE: Deal with activity_cloud_live_mesg_call_out_item denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.SET_PHOTO_FAIL, Toast.LENGTH_SHORT).show();
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "onCameraDenied");
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    public void onCameraNeverAskAgain() {
//        Toast.makeText(this, R.string.permission_camera_never_askagain, Toast.LENGTH_SHORT).show();
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "onCameraNeverAskAgain");
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void showCamera() {
        // NOTE: Perform option that requires the permission. If this is run by PermissionCheckerUitls, the permission will have been granted
        //do you business
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showCamera");
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    public void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show activity_cloud_live_mesg_call_out_item rationale to explain why the permission is needed, e.g. with activity_cloud_live_mesg_call_out_item dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.SET_PHOTO_FAIL, request);
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showRationaleForCamera");
    }


    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.SET_PHOTO_FAIL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.SET_PHOTO_FAIL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    @Override
    public void setPresenter(SplashContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
