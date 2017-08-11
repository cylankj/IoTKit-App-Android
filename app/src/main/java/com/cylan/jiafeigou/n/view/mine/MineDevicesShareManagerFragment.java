package com.cylan.jiafeigou.n.view.mine;

import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.databinding.FragmentMineDeviceShareManagerBinding;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineDevicesShareManagerContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineDevicesShareManagerPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.MineHasShareAdapter;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.ArrayList;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public class MineDevicesShareManagerFragment extends Fragment implements MineDevicesShareManagerContract.View {
    private MineDevicesShareManagerContract.Presenter presenter;
    private MineHasShareAdapter shareFriendsAdapter;
    private String uuid;
    private Device device;
    private FragmentMineDeviceShareManagerBinding shareManagerBinding;
    private ObservableBoolean empty = new ObservableBoolean(false);
    private Runnable callback;

    public static MineDevicesShareManagerFragment newInstance(Bundle bundle) {
        MineDevicesShareManagerFragment fragment = new MineDevicesShareManagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        shareManagerBinding = FragmentMineDeviceShareManagerBinding.inflate(inflater);
        shareManagerBinding.setEmpty(empty);
        uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        device = DataSourceManager.getInstance().getDevice(uuid);
        setTopTitle(TextUtils.isEmpty(device.alias) ? device.uuid : device.alias);
        shareManagerBinding.recyclerHadShareRelativesAndFriend.setLayoutManager(new LinearLayoutManager(getContext()));
        shareFriendsAdapter = new MineHasShareAdapter(getContext(), null, null);
        shareManagerBinding.recyclerHadShareRelativesAndFriend.setAdapter(shareFriendsAdapter);
        shareFriendsAdapter.setOnCancelShareListener(this::onCancelShare);
        shareManagerBinding.customToolbar.setBackAction(this::onClick);
        initPresenter();
        return shareManagerBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.start();
            presenter.initShareDeviceList(uuid);
        }
    }

    private void initPresenter() {
        presenter = new MineDevicesShareManagerPresenterImp(this);
    }

    @Override
    public void setPresenter(MineDevicesShareManagerContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return uuid;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void showCancelShareProgress() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.LOADING));
    }

    @Override
    public void hideCancelShareProgress() {
        LoadingDialog.dismissLoading();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    public void onCancelShare(int position, JFGFriendAccount item) {
        if (getView() != null) {
            if (NetUtils.getNetType(getContext()) == -1) {
                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.Tap3_ShareDevice_CancleShare));
            builder.setPositiveButton(getString(R.string.DELETE), (dialog, which) -> {
                presenter.cancelShare(position);
                dialog.dismiss();
            });
            builder.setNegativeButton(getString(R.string.CANCEL), null);
            builder.show();
        }
    }


    /**
     * 取消分享的结果
     *
     * @param result
     */
    @Override
    public void showUnShareResult(int position, RxEvent.UnShareDeviceCallBack result) {
        if (result != null && result.i == JError.ErrorOK) {
            ToastUtil.showToast(getString(R.string.Tap3_ShareDevice_DeleteSucces));
            shareFriendsAdapter.remove(position);
            if (callback != null) {
                callback.run();
            }
        } else {
            ToastUtil.showToast(getString(R.string.Tap3_ShareDevice_CancelShareTips));
            return;
        }
        empty.set(shareFriendsAdapter.getItemCount() == 0);
    }

    public void setTopTitle(String name) {
        shareManagerBinding.customToolbar.setToolbarLeftTitle(name);
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideCancelShareProgress();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void onInitShareDeviceList(ArrayList<JFGFriendAccount> friends) {
        shareFriendsAdapter.clear();
        shareFriendsAdapter.addAll(friends);
        empty.set(shareFriendsAdapter.getCount() == 0);
    }

}
