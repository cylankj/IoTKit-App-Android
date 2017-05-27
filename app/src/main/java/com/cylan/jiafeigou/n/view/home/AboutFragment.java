package com.cylan.jiafeigou.n.view.home;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.n.view.login.AgreementFragment;
import com.cylan.jiafeigou.n.view.mine.WebsiteFragment;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.SettingItemView0;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class AboutFragment extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    @BindView(R.id.tv_user_agreement)
    TextView tvUserAgreement;
    @BindView(R.id.tv_app_version)
    TextView tvAppVersion;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.sv_hot_line)
    SettingItemView0 svHotLine;
    @BindView(R.id.tv_copy_right)
    TextView tvCopyRight;
    @BindView(R.id.sv_official_website)
    SettingItemView0 svOfficialWebsite;

    private Intent intent;
    private static final String COPY_RIGHT = "Copyright @ 2005-%s Cylan.All Rights Reserved";
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_setting_about, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvCopyRight.setText(String.format(COPY_RIGHT, simpleDateFormat.format(new Date(System.currentTimeMillis()))));
        tvAppVersion.setText(String.format(Locale.getDefault(), "%s", PackageUtils.getAppVersionName(getActivity())));
        customToolbar.setBackAction((View v) -> {
            getActivity().getSupportFragmentManager().popBackStack();
        });
        tvCopyRight.setVisibility(getResources().getBoolean(R.bool.show_all_right) ? View.VISIBLE : View.INVISIBLE);
        tvUserAgreement.setVisibility(getResources().getBoolean(R.bool.show_agreement) ? View.VISIBLE : View.INVISIBLE);
        svOfficialWebsite.setVisibility(getResources().getBoolean(R.bool.show_official_website) ? View.VISIBLE : View.INVISIBLE);
        svHotLine.setVisibility(getResources().getBoolean(R.bool.show_official_hot_line) ? View.VISIBLE : View.INVISIBLE);
    }

    @OnClick({R.id.sv_hot_line, R.id.tv_user_agreement, R.id.sv_official_website})
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        switch (view.getId()) {
            case R.id.sv_hot_line:
                intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getHotPhone()));
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    AboutFragment.this.requestPermissions(
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    return;
                }
                getContext().startActivity(intent);
                break;
            case R.id.sv_official_website:
                enterWeb();
                break;
            case R.id.tv_user_agreement:
                IMEUtils.hide(getActivity());
                AgreementFragment fragment = AgreementFragment.getInstance(null);
                ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                        fragment, android.R.id.content);
                break;
        }
    }

    private void enterWeb() {
        IMEUtils.hide(getActivity());
        WebsiteFragment fragment = WebsiteFragment.getInstance(null);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                fragment, android.R.id.content);
    }

    public String getHotPhone() {
        return (String) svHotLine.getSubTitle();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE:
                //如果请求被取消，那么 result 数组将为空
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 已经获取对应权限
                    getContext().startActivity(intent);
                } else {
                    setPermissionDialog(getString(R.string.callto));
                }
                break;
        }
    }

    public void setPermissionDialog(String permission) {
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivity());
        builder.setMessage(getString(R.string.permission_auth, permission))
                .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.SETTINGS), (DialogInterface dialog, int which) -> {
                    openSetting();
                });
        AlertDialogManager.getInstance().showDialog("setPermissionDialog", getActivity(), builder);
    }

    private void openSetting() {
        //打开设置界面
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
    public void onDestroyView() {
        super.onDestroyView();
    }
}
