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

import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public class MineDevicesShareManagerFragment extends Fragment implements MineDevicesShareManagerContract.View, MineHasShareAdapter.OnCancelShareListener {
    private MineDevicesShareManagerContract.Presenter presenter;
    private MineHasShareAdapter hasShareAdapter;
    private int unShareSucNum = 0;
    private ArrayList<String> unShareAccount = new ArrayList<>();

    private OnUnShareChangeListener listener;
    private String uuid;
    private Device device;
    private FragmentMineDeviceShareManagerBinding shareManagerBinding;
    private ObservableBoolean empty = new ObservableBoolean(false);

    public interface OnUnShareChangeListener {
        void unShareChange(int number, ArrayList<String> account);
    }

    public void setOncancelChangeListener(OnUnShareChangeListener listener) {
        this.listener = listener;
    }

    public static MineDevicesShareManagerFragment newInstance(Bundle bundle) {
        MineDevicesShareManagerFragment fragment = new MineDevicesShareManagerFragment();
        fragment.setArguments(bundle);
        return fragment;
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
        hasShareAdapter = new MineHasShareAdapter(getContext(), null, null);
        shareManagerBinding.recyclerHadShareRelativesAndFriend.setAdapter(hasShareAdapter);
        hasShareAdapter.setOnCancelShareListener(this);
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
        return getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
    }

    @OnClick(R.id.tv_toolbar_icon)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void showCancelShareDialog(final JFGFriendAccount bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.Tap3_ShareDevice_CancleShare));
        builder.setPositiveButton(getString(R.string.DELETE), (dialog, which) -> {
            dialog.dismiss();
            presenter.cancelShare(device.uuid, bean);
        });
        builder.setNegativeButton(getString(R.string.CANCEL), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void showCancleShareProgress() {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.LOADING));
    }

    @Override
    public void hideCancleShareProgress() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }

        if (listener != null) {
            listener.unShareChange(unShareSucNum, unShareAccount);
        }
    }

    @Override
    public void onCancelShare(JFGFriendAccount item) {
        if (getView() != null) {
            if (NetUtils.getNetType(getContext()) == -1) {
                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
                return;
            }
            showCancelShareDialog(item);
        }
    }

    @Override
    public void deleteItems() {
//        hasShareAdapter.remove(tempBean);
        hasShareAdapter.notifyDataSetChanged();
    }

    /**
     * 取消分享的结果
     *
     * @param result
     */
    @Override
    public void showUnShareResult(RxEvent.UnShareDeviceCallBack result) {
        if (result.i == JError.ErrorOK) {
            ToastUtil.showToast(getString(R.string.Tap3_ShareDevice_DeleteSucces));
            deleteItems();
            unShareSucNum++;
            unShareAccount.add(result.account);
        } else {
            ToastUtil.showToast(getString(R.string.Tap3_ShareDevice_CancelShareTips));
            return;
        }
        empty.set(hasShareAdapter.getItemCount() == 0);
    }

    /**
     * 顶部标题
     *
     * @param name
     */
    @Override
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
            hideCancleShareProgress();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void onInitShareDeviceList(ArrayList<JFGFriendAccount> friends) {
        hasShareAdapter.clear();
        hasShareAdapter.addAll(friends);
        empty.set(hasShareAdapter.getCount() == 0);
    }


}
