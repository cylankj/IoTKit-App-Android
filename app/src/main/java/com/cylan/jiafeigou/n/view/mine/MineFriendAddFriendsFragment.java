package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsAddFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendsAddFriendPresenterImp;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.PermissionUtils;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendAddFriendsFragment extends IBaseFragment<MineFriendsAddFriendContract.Presenter> implements MineFriendsAddFriendContract.View {

    @BindView(R.id.et_friend_phonenumber)
    EditText etFriendPhonenumber;
    @BindView(R.id.tv_scan_add)
    TextView tvScanAdd;
    @BindView(R.id.tv_add_from_contract)
    TextView tvAddFromContract;
    @BindView(R.id.fragment_container)
    FrameLayout fragmentContainer;

    private MineFriendsAddFriendContract.Presenter presenter;

    public static MineFriendAddFriendsFragment newInstance() {
        return new MineFriendAddFriendsFragment();
    }

    @Override
    public void setPresenter(MineFriendsAddFriendContract.Presenter presenter) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_friends_addfriends, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initPresenter() {
        presenter = new MineFriendsAddFriendPresenterImp(this);
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_scan_add, R.id.tv_add_from_contract, R.id.et_friend_phonenumber})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:        //返回
                getFragmentManager().popBackStack();
                break;

            case R.id.tv_scan_add:                                      //扫一扫添加
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_scan_add));
                AppLogger.d("tv_scan_add");
                if (presenter.checkCameraPermission()) {
                    jump2ScanAddFragment();
                } else {
                    if (MineFriendAddFriendsFragment.this.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        AppLogger.d("request_Y");
//                        MineFriendAddFriendsFragment.this.requestPermissions(
//                                new String[]{Manifest.permission.READ_CONTACTS},
//                                1);
                        setPermissionDialog(getString(R.string.camera_auth));
                    } else {
                        AppLogger.d("request_N");
                        MineFriendAddFriendsFragment.this.requestPermissions(
                                new String[]{Manifest.permission.CAMERA},
                                2);
                    }
                }
                break;

            case R.id.tv_add_from_contract:                             //从通讯录添加
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_add_from_contract));
                AppLogger.d("tv_add_from_contract");
                if (presenter.checkContractPermission()) {
                    AppLogger.d("from_contract_Y");
                    jump2AddFromContactFragment();
                } else {
                    AppLogger.d("from_contract_N");
                    if (MineFriendAddFriendsFragment.this.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AppLogger.d("request_Y");
//                        MineFriendAddFriendsFragment.this.requestPermissions(
//                                new String[]{Manifest.permission.READ_CONTACTS},
//                                1);
                        setPermissionDialog(getString(R.string.Tap3_ShareDevice_Contacts));
                    } else {
                        AppLogger.d("request_N");
                        MineFriendAddFriendsFragment.this.requestPermissions(
                                new String[]{Manifest.permission.READ_CONTACTS},
                                1);
                    }
                }
                break;

            case R.id.et_friend_phonenumber:                            //根据对方账号添加
                ViewUtils.deBounceClick(view);//防重复点击
                jump2AddByNumberFragment();
                break;
        }
    }

    private void jump2AddByNumberFragment() {
        MineFriendAddByNumFragment addByNumFragment = MineFriendAddByNumFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, addByNumFragment, addByNumFragment.getClass().getName())
                .addToBackStack("AddFlowStack")
                .commit();
    }

    private void jump2AddFromContactFragment() {
        MineFriendAddFromContactFragment addFromContactFragment = MineFriendAddFromContactFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, addFromContactFragment, "addFromContactFragment")
                .addToBackStack("AddFlowStack")
                .commitAllowingStateLoss();
    }

    private void jump2ScanAddFragment() {
        MineFriendScanAddFragment scanAddFragment = MineFriendScanAddFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, scanAddFragment, "scanAddFragment")
                .addToBackStack("AddFlowStack")
                .commitAllowingStateLoss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (PermissionUtils.hasSelfPermissions(getContext(), permissions[0])) {
                jump2AddFromContactFragment();
            } else {
                setPermissionDialog(getString(R.string.Tap3_ShareDevice_Contacts));
            }
        } else if (requestCode == 2) {
            if (PermissionUtils.hasSelfPermissions(getContext(), permissions[0])) {
                jump2ScanAddFragment();
            } else {
                setPermissionDialog(getString(R.string.camera_auth));
            }
        }
    }

    public void setPermissionDialog(String permission) {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.permission_auth, "", permission))
                .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.SETTINGS), (DialogInterface dialog, int which) -> {
                    openSetting();
                })
                .create()
                .show();
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
}
