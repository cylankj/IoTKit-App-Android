package com.cylan.jiafeigou.n.view.mine;

import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.FragmentMineFriendShareDevicesBinding;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendListShareDevicesToContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendListShareDevicesPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.view.adapter.ChooseShareDeviceAdapter;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendsListShareDevicesFragment extends Fragment implements MineFriendListShareDevicesToContract.View {
    private MineFriendListShareDevicesToContract.Presenter presenter;
    private FriendContextItem friendItem;
    private ChooseShareDeviceAdapter chooseShareDeviceAdapter;
    private ArrayList<DeviceBean> chooseList = new ArrayList<>();
    private FragmentMineFriendShareDevicesBinding shareDevicesBinding;
    private ObservableBoolean empty = new ObservableBoolean(false);

    public static MineFriendsListShareDevicesFragment newInstance(Bundle bundle) {
        MineFriendsListShareDevicesFragment fragment = new MineFriendsListShareDevicesFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        friendItem = getArguments().getParcelable("friendItem");
        shareDevicesBinding = FragmentMineFriendShareDevicesBinding.inflate(inflater, container, false);
        shareDevicesBinding.setEmpty(empty);
        initPresenter();
        initTitleView(friendItem);
        shareDevicesBinding.rcyShareDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        chooseShareDeviceAdapter = new ChooseShareDeviceAdapter(getContext(), null, R.layout.fragment_friend_share_device_items);
        shareDevicesBinding.rcyShareDeviceList.setAdapter(chooseShareDeviceAdapter);
        ButterKnife.bind(this, shareDevicesBinding.customToolbar);
        chooseShareDeviceAdapter.setOnCheckClickListener(this::checkDevice);
        return shareDevicesBinding.getRoot();
    }

    private void checkDevice(DeviceBean deviceBean, boolean over) {
        if (over) {
            ToastUtil.showNegativeToast(getString(R.string.Tap3_ShareDevice_Tips));
            return;
        }
        chooseList.clear();
        for (DeviceBean bean : chooseShareDeviceAdapter.getList()) {
            if (bean.isChooseFlag == 1) {
                chooseList.add(bean);
            }
        }
        presenter.checkIsChoose(chooseList);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

    private void initPresenter() {
        presenter = new MineFriendListShareDevicesPresenterImp(friendItem.friendAccount.account, this);
    }

    @Override
    public void setPresenter(MineFriendListShareDevicesToContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.tv_toolbar_right:
                if (chooseList.size() == 0) return;
                presenter.sendShareToReq(chooseList, friendItem);
                break;
        }
    }

    public void initTitleView(FriendContextItem friendItem) {
        if (TextUtils.isEmpty(friendItem.friendAccount.markName) || TextUtils.isEmpty(friendItem.friendAccount.markName.trim())) {
            shareDevicesBinding.customToolbar.setToolbarLeftTitle((String.format(getString(R.string.Tap3_Friends_Share), friendItem.friendAccount.alias)));
        } else {
            shareDevicesBinding.customToolbar.setToolbarLeftTitle((String.format(getString(R.string.Tap3_Friends_Share), friendItem.friendAccount.markName)));
        }
    }

    /**
     * 初始化列表的显示
     *
     * @param list
     */
    @Override
    public void initRecycleView(ArrayList<DeviceBean> list) {
        chooseShareDeviceAdapter.clear();
        chooseShareDeviceAdapter.addAll(list);
    }

    /**
     * 可分享设备为无
     */
    @Override
    public void showNoDeviceView() {
        empty.set(true);
    }

    /**
     * 可分享设备为有
     */
    @Override
    public void hideNoDeviceView() {
        empty.set(false);
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideLoading();
            hideSendReqProgress();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    /**
     * 显示完成按钮
     */
    @Override
    public void showFinishBtn() {
        shareDevicesBinding.customToolbar.setTvToolbarRightEnable(true);
        shareDevicesBinding.customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_normal);
    }

    /**
     * 隐藏完成按钮
     */
    @Override
    public void hideFinishBtn() {
        shareDevicesBinding.customToolbar.setTvToolbarRightEnable(false);
        shareDevicesBinding.customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
    }

    /**
     * 显示发送请求的进度提示
     */
    @Override
    public void showSendReqProgress() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.LOADING));
    }

    /**
     * 隐藏发送分享请求的进度提示
     */
    @Override
    public void hideSendReqProgress() {
        LoadingDialog.dismissLoading();
    }

    /**
     * 设置分享请求发送结果
     */
    @Override
    public void showSendReqFinishReuslt(ArrayList<RxEvent.ShareDeviceCallBack> callBacks) {

        int totalFriend = chooseList.size();
        Iterator iterators = chooseList.iterator();
        while (iterators.hasNext()) {
            DeviceBean friendBean = (DeviceBean) iterators.next();
            for (RxEvent.ShareDeviceCallBack callBack : callBacks) {
                if (friendBean.uuid.equals(callBack.cid) && callBack.requestId == 0) {
                    iterators.remove();
                }
            }
        }

        if (chooseList.size() == 0) {
            ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_SuccessTips));
            getActivity().getSupportFragmentManager().popBackStack();
        } else if (chooseList.size() == totalFriend) {
            ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_FailTips));
        } else {
            ToastUtil.showPositiveToast(String.format(getString(R.string.Tap3_ShareDevice_Friends_FailTips), chooseList.size()));
        }
    }

    @Override
    public void showLoading(int resId, String... args) {
        LoadingDialog.showLoading(getActivity(), getString(resId, (Object[]) args));
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading();
    }
}
