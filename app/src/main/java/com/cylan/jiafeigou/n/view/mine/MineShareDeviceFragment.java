package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.cylan.entity.jniCall.JFGShareListInfo;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareDevicePresenterImp;
import com.cylan.jiafeigou.n.view.adapter.MineShareDeviceAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
@RuntimePermissions
public class MineShareDeviceFragment extends Fragment implements MineShareDeviceContract.View {
    @BindView(R.id.recycle_share_device_list)
    RecyclerView recycleShareDeviceList;
    @BindView(R.id.ll_no_device)
    LinearLayout llNoDevice;
    private MineShareDeviceContract.Presenter presenter;
    private MineDevicesShareManagerFragment mineDevicesShareManagerFragment;
    private MineShareToFriendFragment shareToRelativeAndFriendFragment;
    private MineShareDeviceAdapter adapter;
    private int position;

    public static MineShareDeviceFragment newInstance(Bundle bundle) {
        return new MineShareDeviceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_share_device, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initAdapter();
        return view;
    }

    private void initAdapter() {
        recycleShareDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MineShareDeviceAdapter(getContext(), null, null);
        adapter.setOnShareClickListener((holder, viewType, layoutPosition, item) -> {
            this.position = layoutPosition;
            ViewUtils.deBounceClick(holder.itemView);
            AppLogger.d("setOnShareClickListener");
            showShareMenu();
        });
        adapter.setOnItemClickListener((itemView, viewType, position1) -> {
            this.position = position1;
            ViewUtils.deBounceClick(itemView);
            AppLogger.e("tv_share_device_manger");
            jump2ShareDeviceMangerFragment();
        });
        recycleShareDeviceList.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.initShareList();
    }

    private void initPresenter() {
        presenter = new MineShareDevicePresenterImp(this);
    }

    @Override
    public void setPresenter(MineShareDeviceContract.Presenter presenter) {
    }

    @Override
    public String getUuid() {
        return null;
    }

    public void showShareDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = View.inflate(getContext(), R.layout.fragment_home_mine_share_devices_dialog, null);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.tv_share_to_timeline).setOnClickListener(v -> {
            ViewUtils.deBounceClick(v);
            AppLogger.e("tv_share_to_friends");
            alertDialog.dismiss();
            jump2ShareToFriendFragment();
            AlertDialogManager.getInstance().dismissOtherDialog("showShareDialog");
        });
        view.findViewById(R.id.tv_share_to_contract).setOnClickListener(v -> {
            ViewUtils.deBounceClick(v);
            AppLogger.d("tv_share_to_contract");
            alertDialog.dismiss();
            MineShareDeviceFragmentPermissionsDispatcher.onReadContactsPermissionWithCheck(MineShareDeviceFragment.this);
            AlertDialogManager.getInstance().dismissOtherDialog("showShareDialog");
        });
        alertDialog.show();
    }

    /**
     * desc；跳转到通过联系人分享的界面
     */
    private void jump2ShareToContractFragment() {
        JFGShareListInfo adapterItem = adapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, adapterItem.cid);
        MineContactManagerFragment mineShareToContactFragment = MineContactManagerFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), mineShareToContactFragment, android.R.id.content);
    }

    /**
     * desc:跳转到通过亲友分享
     */
    private void jump2ShareToFriendFragment() {
        JFGShareListInfo adapterItem = adapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, adapterItem.cid);
        shareToRelativeAndFriendFragment = MineShareToFriendFragment.newInstance(bundle);
        shareToRelativeAndFriendFragment.setCallBack(() -> adapter.notifyItemChanged(position));
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), shareToRelativeAndFriendFragment, android.R.id.content);
    }

    @Override
    public void onInitShareList(ArrayList<JFGShareListInfo> list) {
        adapter.clear();
        adapter.addAll(list);
        llNoDevice.setVisibility(adapter.getCount() > 0 ? View.GONE : View.VISIBLE);
    }

    private void showShareMenu() {
        if (NetUtils.getNetType(getContext()) == -1) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }
        showShareDialog();

    }

    /**
     * 跳转到分享管理界面
     */
    public void jump2ShareDeviceMangerFragment() {
        JFGShareListInfo adapterItem = adapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, adapterItem.cid);
        mineDevicesShareManagerFragment = MineDevicesShareManagerFragment.newInstance(bundle);
        mineDevicesShareManagerFragment.setCallback(() -> adapter.notifyItemChanged(position));
        ActivityUtils.addFragmentSlideInFromRight(getFragmentManager(), mineDevicesShareManagerFragment, android.R.id.content);
    }

    /**
     * 显示加载进度
     */
    @Override
    public void showLoadingDialog() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.LOADING), true);
    }

    /**
     * 隐藏加载进度
     */
    @Override
    public void hideLoadingDialog() {
        LoadingDialog.dismissLoading();
    }

    @OnClick(R.id.tv_toolbar_icon)
    public void onClick() {
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MineShareDeviceFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied(Manifest.permission.READ_CONTACTS)
    public void onReadContactsPermissionDenied() {
        setPermissionDialog(getString(R.string.Tap3_ShareDevice_Contacts));
    }

    @OnNeverAskAgain(Manifest.permission.READ_CONTACTS)
    public void onNeverAskAgainReadContactsPermission() {
        setPermissionDialog(getString(R.string.Tap3_ShareDevice_Contacts));
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    public void onReadContactsPermission() {
        jump2ShareToContractFragment();
    }


    @OnShowRationale(Manifest.permission.READ_CONTACTS)
    public void showRationaleForCamera(PermissionRequest request) {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showRationaleForCamera");
        onNeverAskAgainReadContactsPermission();
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
        AlertDialogManager.getInstance().showDialog("showSetPermissionDialog", getActivity(), builder);
    }

    private void openSetting() {
        Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        settingIntent.setData(Uri.parse("package:" + getContext().getPackageName()));
        startActivity(settingIntent);
    }
}
