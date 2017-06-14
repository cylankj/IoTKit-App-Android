package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsAddFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendsAddFriendPresenterImp;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
@RuntimePermissions
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
                getActivity().getSupportFragmentManager().popBackStack();
                break;

            case R.id.tv_scan_add:                                      //扫一扫添加
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_scan_add));
                AppLogger.d("tv_scan_add");
                MineFriendAddFriendsFragmentPermissionsDispatcher.onCameraPermissionWithCheck(this);
                break;
            case R.id.tv_add_from_contract:                             //从通讯录添加
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_add_from_contract));
                MineFriendAddFriendsFragmentPermissionsDispatcher.onContactsPermissionWithCheck(this);
                break;
            case R.id.et_friend_phonenumber:                            //根据对方账号添加
                ViewUtils.deBounceClick(view);//防重复点击
                jump2AddByNumberFragment();
                break;
        }
    }

    private void jump2AddByNumberFragment() {
        MineFriendAddByNumFragment addByNumFragment = MineFriendAddByNumFragment.newInstance();
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, addByNumFragment, addByNumFragment.getClass().getName())
                .addToBackStack("AddFlowStack")
                .commit();
    }

    private void jump2AddFromContactFragment() {
        AddFriendsFragment addFromContactFragment = AddFriendsFragment.newInstance();
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, addFromContactFragment, "addFromContactFragment")
                .addToBackStack("AddFlowStack")
                .commitAllowingStateLoss();
    }

    private void jump2ScanAddFragment() {
        MineFriendScanAddFragment scanAddFragment = MineFriendScanAddFragment.newInstance();
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
        if (permissions.length == 1) {
            if (TextUtils.equals(permissions[0], CAMERA) && grantResults[0] > -1) {
//                MineFriendAddFriendsFragmentPermissionsDispatcher.onCameraPermissionWithCheck(this);
            } else if (TextUtils.equals(permissions[0], READ_CONTACTS) && grantResults[0] > -1) {
                MineFriendAddFriendsFragmentPermissionsDispatcher.onContactsPermissionWithCheck(this);
            }
        }
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
//                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
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
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showRationaleForCamera");
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
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showRationaleForCamera");
        onNeverAskAgainContactsPermission();
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
