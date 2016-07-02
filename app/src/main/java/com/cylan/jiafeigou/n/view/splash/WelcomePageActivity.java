package com.cylan.jiafeigou.n.view.splash;

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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.NewHomeActivity;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.mvp.impl.splash.SplashPresenterImpl;
import com.cylan.jiafeigou.n.view.adapter.SimpleFragmentAdapter;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.UiHelper;
import com.cylan.viewindicator.CirclePageIndicator;

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
public class WelcomePageActivity extends BaseFullScreenFragmentActivity implements SplashContract.View {


    @BindView(R.id.imgvWelcomeSplash)
    ImageView imgvWelcomeSplash;
    @BindView(R.id.vpWelcome)
    ViewPager vpWelcome;
    @BindView(R.id.v_indicator)
    CirclePageIndicator vIndicator;
    @Nullable

    private SplashContract.Presenter mPresenter;
    private final String TAG_COMEIN = "isTheUserFirstLoginIn";

    private List<Fragment> listSplashFreg;
    private SimpleFragmentAdapter mSplashListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);
        ButterKnife.bind(this);
        initData();
    }


    @Override
    protected void onDestroy() {
        //do something
        super.onDestroy();
    }


    private void initData() {
        mPresenter = new SplashPresenterImpl(this);
        mPresenter.splashTime();
    }

    private void initGuidePage() {
        setFirstUseApp();
        if (listSplashFreg == null) {
            listSplashFreg = new ArrayList<Fragment>();
            listSplashFreg.add(FragmentSplash.newInstance(null));
            listSplashFreg.add(FragmentSplash.newInstance(null));
            listSplashFreg.add(FragmentSplash.newInstance(null));
            listSplashFreg.add(BeforeLoginFragment.newInstance(null));
            mSplashListAdapter = new SimpleFragmentAdapter(getSupportFragmentManager(), listSplashFreg);

            vpWelcome.setAdapter(mSplashListAdapter);
            vpWelcome.addOnPageChangeListener(new PageChangeListen());
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
            if (listSplashFreg == null) {
                listSplashFreg = new ArrayList<Fragment>();
                listSplashFreg.add(BeforeLoginFragment.newInstance(null));
            }
            mSplashListAdapter = new SimpleFragmentAdapter(getSupportFragmentManager(), listSplashFreg);
            vpWelcome.setAdapter(mSplashListAdapter);
        }
    }

    @Override
    public void timeSplashed() {
        SplashPermissionDispatcher.showWriteSdCardWithCheck(this);
    }

    @Override
    public void finishDelayed() {
//        StateMaintainer.getAppManager().finishAllActivity();
    }


    private boolean isLoginIn() {
        return PreferencesUtils.getBoolean(this, UiHelper.TAG_LOGING_STATUS, false);
    }

    private boolean isFirstUseApp() {
        return PreferencesUtils.getBoolean(this, TAG_COMEIN, true);
        // return true;
    }

    private void setFirstUseApp() {
        PreferencesUtils.putBoolean(this, TAG_COMEIN, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SplashPermissionDispatcher.onRequestPermissionsResult(this, permissions, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void onWriteSdCardDenied() {
        Toast.makeText(this, "请你开启SD卡读写权限,应用才能正常工作", Toast.LENGTH_SHORT).show();
        if (mPresenter != null) mPresenter.finishAppDelay();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void showWriteSdCard() {
        //do you business;
        if (isFirstUseApp()) {
            //第一次打开app
            initGuidePage();
        } else {
            initLoginPage();
        }
        imgvWelcomeSplash.setVisibility(android.view.View.GONE);
        imgvWelcomeSplash.startAnimation(AnimationUtils.loadAnimation(this, R.anim.push_up_out));
    }


    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onCameraDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void onCameraNeverAskAgain() {
        Toast.makeText(this, R.string.permission_camera_never_askagain, Toast.LENGTH_SHORT).show();
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showCamera() {
        // NOTE: Perform action that requires the permission. If this is run by PermissionCheckerUitls, the permission will have been granted
        //do you business
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(PermissionRequest request) {
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

    }

    @Override
    public Context getContext() {
        return null;
    }

    private class PageChangeListen implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

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
