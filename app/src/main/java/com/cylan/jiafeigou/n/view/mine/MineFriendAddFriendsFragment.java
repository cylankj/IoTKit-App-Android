package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.FragmentHomeMineFriendsAddFriendsBinding;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsAddFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendsAddFriendPresenterImp;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
@RuntimePermissions
public class MineFriendAddFriendsFragment extends IBaseFragment<MineFriendsAddFriendContract.Presenter> implements MineFriendsAddFriendContract.View {

    private MineFriendsAddFriendContract.Presenter presenter;
    private FragmentHomeMineFriendsAddFriendsBinding addFriendsBinding;

    public static MineFriendAddFriendsFragment newInstance() {
        return new MineFriendAddFriendsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        addFriendsBinding = FragmentHomeMineFriendsAddFriendsBinding.inflate(inflater, container, false);
        presenter = new MineFriendsAddFriendPresenterImp(this);
        return addFriendsBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addFriendsBinding.customToolbar.setBackAction(this::onClick);
        addFriendsBinding.tvAddFromContract.setOnClickListener(this::onClick);
        addFriendsBinding.tvScanAdd.setOnClickListener(this::onClick);
        addFriendsBinding.etFriendPhonenumber.setOnClickListener(this::onClick);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:        //返回
                getActivity().getSupportFragmentManager().popBackStack();
                break;

            case R.id.tv_scan_add:                                      //扫一扫添加
                ViewUtils.deBounceClick(view);
                AppLogger.w("tv_scan_add");
                MineFriendAddFriendsFragmentPermissionsDispatcher.onCameraPermissionWithCheck(this);
                break;
            case R.id.tv_add_from_contract:                             //从通讯录添加
                ViewUtils.deBounceClick(view);
                MineFriendAddFriendsFragmentPermissionsDispatcher.onContactsPermissionWithCheck(this);
                break;
            case R.id.et_friend_phonenumber:                            //根据对方账号添加
                ViewUtils.deBounceClick(view);//防重复点击
                jump2AddByNumberFragment();
                break;
        }
    }

    private void jump2AddByNumberFragment() {
        MineFriendSearchFragment addByNumFragment = MineFriendSearchFragment.newInstance();
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), addByNumFragment, android.R.id.content, MineFriendInformationFragment.class.getSimpleName());
    }

    private void jump2AddFromContactFragment() {
        Bundle bundle = new Bundle();
        MineContactManagerFragment contactManagerFragment = MineContactManagerFragment.newInstance(bundle);
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, "");
        bundle.putInt("contactType", 1);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), contactManagerFragment, android.R.id.content, MineFriendInformationFragment.class.getSimpleName());
    }

    private void jump2ScanAddFragment() {
        MineFriendQRScanFragment scanAddFragment = MineFriendQRScanFragment.newInstance();
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(0, 0
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragment_container, scanAddFragment, "scanAddFragment")
                .addToBackStack("AddFlowStack")
                .commitAllowingStateLoss();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MineFriendAddFriendsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }


    //
    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onCameraPermissionDenied() {
        onNeverAskAgainCameraPermission();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    public void onNeverAskAgainCameraPermission() {
        AlertDialogManager.getInstance().showDialog(getActivity(),
                getString(R.string.permission_auth, getString(R.string.CAMERA)),
                getString(R.string.permission_auth, getString(R.string.CAMERA)),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    openSetting();
                },
                getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    getActivity().getSupportFragmentManager().popBackStack();
                });
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void onCameraPermission() {
        jump2ScanAddFragment();
    }


    @OnShowRationale(Manifest.permission.CAMERA)
    public void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show activity_cloud_live_mesg_call_out_item rationale to explain why the permission is needed, e.g. with activity_cloud_live_mesg_call_out_item dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        AppLogger.w(JConstant.LOG_TAG.PERMISSION + "showRationaleForCamera");
        onNeverAskAgainCameraPermission();
    }


    //
    @OnPermissionDenied(Manifest.permission.READ_CONTACTS)
    public void onContactsPermissionDenied() {
        onNeverAskAgainContactsPermission();
    }

    @OnNeverAskAgain(Manifest.permission.READ_CONTACTS)
    public void onNeverAskAgainContactsPermission() {
        AlertDialogManager.getInstance().showDialog(getActivity(),
                getString(R.string.permission_auth, getString(R.string.contacts_auth)),
                getString(R.string.permission_auth, getString(R.string.contacts_auth)),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    openSetting();
//                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                },
                getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    getActivity().getSupportFragmentManager().popBackStack();
                });
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    public void onContactsPermission() {
        jump2AddFromContactFragment();
    }


    @OnShowRationale(Manifest.permission.READ_CONTACTS)
    public void showRationaleForContacts(PermissionRequest request) {
        // NOTE: Show activity_cloud_live_mesg_call_out_item rationale to explain why the permission is needed, e.g. with activity_cloud_live_mesg_call_out_item dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        AppLogger.w(JConstant.LOG_TAG.PERMISSION + "showRationaleForCamera");
        onNeverAskAgainContactsPermission();
    }


    private void openSetting() {
        Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingIntent.setData(Uri.parse("package:" + getContext().getPackageName()));
        startActivity(settingIntent);
    }
}
