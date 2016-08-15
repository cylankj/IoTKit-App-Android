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
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.splash.SplashPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.adapter.SimpleFragmentAdapter;
import com.cylan.jiafeigou.n.view.splash.BeforeLoginFragment;
import com.cylan.jiafeigou.n.view.splash.FragmentSplash;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.UiHelper;
import com.cylan.jiafeigou.widget.indicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.List;

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


    @BindView(R.id.fLayout_splash)
    FrameLayout fLayoutSplash;
    @BindView(R.id.vpWelcome)
    ViewPager vpWelcome;
    @BindView(R.id.v_indicator)
    CirclePageIndicator vIndicator;
    @Nullable

    private SplashContract.Presenter presenter;


    private List<Fragment> splashFragments;
    private SimpleFragmentAdapter mSplashListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IMEUtils.fixFocusedViewLeak(getApplication());
        setContentView(R.layout.activity_welcome_page);
        ButterKnife.bind(this);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (presenter != null) presenter.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

    private void initData() {
        presenter = new SplashPresenterImpl(this);

    }

    private void initGuidePage() {

        if (splashFragments == null) {
            splashFragments = new ArrayList<>();
            splashFragments.add(FragmentSplash.newInstance(0));
            splashFragments.add(FragmentSplash.newInstance(1));
            splashFragments.add(FragmentSplash.newInstance(2));
            splashFragments.add(BeforeLoginFragment.newInstance(null));
            mSplashListAdapter = new SimpleFragmentAdapter(getSupportFragmentManager(), splashFragments);
            vpWelcome.setAdapter(mSplashListAdapter);
            vpWelcome.addOnPageChangeListener(new PageChangeListener());
            vIndicator.setViewPager(vpWelcome);
        }
    }

    private void initLoginPage() {
        if (isLoginIn()) {
            //进去主页 home page
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                startActivity(new Intent(this, NewHomeActivity.class),
                        ActivityOptionsCompat.makeCustomAnimation(this, R.anim.alpha_in, R.anim.alpha_out).toBundle());
            } else {
                startActivity(new Intent(this, NewHomeActivity.class));
            }
            finish();
        } else {
            //进入登陆页 login page
            if (splashFragments == null) {
                splashFragments = new ArrayList<>();
                splashFragments.add(BeforeLoginFragment.newInstance(null));
            }
            mSplashListAdapter = new SimpleFragmentAdapter(getSupportFragmentManager(), splashFragments);
            vpWelcome.setAdapter(mSplashListAdapter);
        }
    }

//    @Override
//    public void timeSplashed() {
//        SplashPermissionDispatcher.showWriteSdCardWithCheck(this);
//    }

    @Override
    public void splashOver() {
        fLayoutSplash.setVisibility(View.GONE);
        //do you business;
        if (isFirstUseApp()) {
            //第一次打开app
            setFirstUseApp();
            initGuidePage();
        } else {
            initLoginPage();
        }
    }

    @Override
    public void finishDelayed() {
//        StateMaintainer.getAppManager().finishAllActivity();
    }


    private boolean isLoginIn() {
        return PreferencesUtils.getBoolean(this, UiHelper.TAG_LOGING_STATUS, false);
    }

    /**
     * check is the app is fresh
     *
     * @return
     */
    private boolean isFirstUseApp() {
        return PreferencesUtils.getBoolean(this, JConstant.KEY_FRESH, true);
        // return true;
    }

    private void setFirstUseApp() {
        PreferencesUtils.putBoolean(this, JConstant.KEY_FRESH, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        SplashPermissionDispatcher.onRequestPermissionsResult(this, permissions, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void onWriteSdCardDenied() {
        // NOTE: Perform action that requires the permission.
        // If this is run by PermissionsDispatcher, the permission will have been granted
        Toast.makeText(this, "请你开启SD卡读写权限,应用才能正常工作", Toast.LENGTH_SHORT).show();
        if (presenter != null) presenter.finishAppDelay();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void showWriteSdCard() {

    }


    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onCameraDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    public void onCameraNeverAskAgain() {
        Toast.makeText(this, R.string.permission_camera_never_askagain, Toast.LENGTH_SHORT).show();
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void showCamera() {
        // NOTE: Perform action that requires the permission. If this is run by PermissionCheckerUitls, the permission will have been granted
        //do you business
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    public void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.permission_camera_rationale, request);
    }


    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
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
        return null;
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (position == 2)
                vIndicator.setAlpha(1.0f - positionOffset);
        }

        @Override
        public void onPageSelected(int position) {
            if (position > 2)
                vIndicator.setVisibility(android.view.View.GONE);
            else
                vIndicator.setVisibility(android.view.View.VISIBLE);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

}
