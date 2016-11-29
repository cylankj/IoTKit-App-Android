package com.cylan.jiafeigou.n.view.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeSettingAboutContract;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class HomeSettingAboutFragment extends Fragment implements HomeSettingAboutContract.View {
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    @BindView(R.id.iv_home_setting_about_back)
    ImageView ivHomeSettingAboutBack;
    @BindView(R.id.rLayout_home_setting_hotphone)
    RelativeLayout rLayoutHomeSettingHotphone;
    @BindView(R.id.tv_hotphone)
    TextView tvHotphone;

    private HomeSettingAboutContract.Presenter presenter;
    private Intent intent;

    public static HomeSettingAboutFragment newInstance() {
        return new HomeSettingAboutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_setting_about, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(HomeSettingAboutContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick({R.id.iv_home_setting_about_back, R.id.rLayout_home_setting_hotphone})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.iv_home_setting_about_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.rLayout_home_setting_hotphone:
                intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + getHotPhone()));
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) view.getContext(),
                            new String[]{Manifest.permission.CALL_PHONE},
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    return;
                }
                getContext().startActivity(intent);
                break;
        }
    }

    @Override
    public String getHotPhone() {
        return tvHotphone.getText().toString().trim();
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
                    // 未获取到授权
                    ToastUtil.showToast("权限未授予");
                }
                break;
        }
    }
}
