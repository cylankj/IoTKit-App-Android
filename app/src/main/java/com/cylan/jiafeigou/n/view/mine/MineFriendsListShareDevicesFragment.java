package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendListShareDevicesToContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendListShareDevicesPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.ChooseShareDeviceAdapter;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendsListShareDevicesFragment extends Fragment implements MineFriendListShareDevicesToContract.View {

    @BindView(R.id.iv_mine_friends_share_devices_back)
    ImageView ivMineFriendsShareDevicesBack;
    @BindView(R.id.iv_mine_friends_share_devices_ok)
    ImageView ivMineFriendsShareDevicesOk;
    @BindView(R.id.rcy_share_device_list)
    RecyclerView rcyShareDeviceList;
    @BindView(R.id.tv_share_to)
    TextView tvShareTo;
    @BindView(R.id.ll_no_device)
    LinearLayout llNoDevice;
    @BindView(R.id.tv_choose_device_title)
    TextView tvChooseDeviceTitle;

    private MineFriendListShareDevicesToContract.Presenter presenter;
    private RelAndFriendBean shareDeviceBean;
    private ChooseShareDeviceAdapter chooseShareDeviceAdapter;
    private ArrayList<DeviceBean> chooseList = new ArrayList<DeviceBean>();

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
        View view = inflater.inflate(R.layout.fragment_mine_friend_share_devices, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        initPresenter();
        initTitleView(shareDeviceBean);
        showLoadingDialog();
        return view;
    }

    /**
     * 获取到转送过来的数据
     */
    private void getArgumentData() {
        Bundle arguments = getArguments();
        shareDeviceBean = arguments.getParcelable("shareDeviceBean");
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
        presenter = new MineFriendListShareDevicesPresenterImp(shareDeviceBean.account,this);
    }

    @Override
    public void setPresenter(MineFriendListShareDevicesToContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_mine_friends_share_devices_back, R.id.iv_mine_friends_share_devices_ok})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_friends_share_devices_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_friends_share_devices_ok:
                if (chooseList.size() == 0)return;
                presenter.sendShareToReq(chooseList, shareDeviceBean);
                break;
        }
    }

    /**
     * 初始化头部标题显示
     *
     * @param bean
     */
    @Override
    public void initTitleView(RelAndFriendBean bean) {
        if (TextUtils.isEmpty(bean.markName.trim())) {
            tvShareTo.setText(String.format(getString(R.string.Tap3_Friends_Share),bean.alias));
        } else {
            tvShareTo.setText(String.format(getString(R.string.Tap3_Friends_Share),bean.markName));
        }
    }

    /**
     * 初始化列表的显示
     *
     * @param list
     */
    @Override
    public void initRecycleView(ArrayList<DeviceBean> list) {
        hideLoadingDialog();
        rcyShareDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        chooseShareDeviceAdapter = new ChooseShareDeviceAdapter(getContext(), list, null);
        rcyShareDeviceList.setAdapter(chooseShareDeviceAdapter);
        initAdaListener();
    }

    /**
     * 列表的监听器
     */
    private void initAdaListener() {
        chooseShareDeviceAdapter.setOnCheckClickListener(new ChooseShareDeviceAdapter.OnCheckClickListener() {
            @Override
            public void onCheckClick(DeviceBean item) {
                chooseList.clear();
                for (DeviceBean bean : chooseShareDeviceAdapter.getList()) {
                    if (bean.isChooseFlag == 1) {
                        chooseList.add(bean);
                    }
                }
                presenter.checkIsChoose(chooseList);
            }
        });
    }

    /**
     * 可分享设备为无
     */
    @Override
    public void showNoDeviceView() {
        llNoDevice.setVisibility(View.VISIBLE);
        tvChooseDeviceTitle.setVisibility(View.INVISIBLE);
    }

    /**
     * 可分享设备为有
     */
    @Override
    public void hideNoDeviceView() {
        llNoDevice.setVisibility(View.INVISIBLE);
        tvChooseDeviceTitle.setVisibility(View.VISIBLE);
    }

    /**
     * 网络状态变化
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1){
            hideLoadingDialog();
            hideSendReqProgress();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    /**
     * 显示完成按钮
     */
    @Override
    public void showFinishBtn() {
        ivMineFriendsShareDevicesOk.setClickable(true);
        ivMineFriendsShareDevicesOk.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
    }

    /**
     * 隐藏完成按钮
     */
    @Override
    public void hideFinishBtn() {
        ivMineFriendsShareDevicesOk.setClickable(false);
        ivMineFriendsShareDevicesOk.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
    }

    /**
     * 显示发送请求的进度提示
     */
    @Override
    public void showSendReqProgress() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
    }

    /**
     * 隐藏发送分享请求的进度提示
     */
    @Override
    public void hideSendReqProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    /**
     * 设置分享请求发送结果
     */
    @Override
    public void showSendReqFinishReuslt(ArrayList<RxEvent.ShareDeviceCallBack> callBacks) {

        int totalFriend = chooseList.size();
        Iterator iterators = chooseList.iterator();
        while(iterators.hasNext()){
            DeviceBean friendBean = (DeviceBean) iterators.next();
            for (RxEvent.ShareDeviceCallBack callBack : callBacks) {
                if (friendBean.uuid.equals(callBack.cid) && callBack.requestId == 0) {
                    iterators.remove();
                }
            }
        }

        if (chooseList.size() == 0) {
            ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_SuccessTips));
            getFragmentManager().popBackStack();
        } else if (chooseList.size() == totalFriend) {
            ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_FailTips));
        }else {
            ToastUtil.showPositiveToast(String.format(getString(R.string.Tap3_ShareDevice_Friends_FailTips),chooseList.size()));
        }
    }

    /**
     * 显示加载进度
     */
    @Override
    public void showLoadingDialog() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
    }

    /**
     * 隐藏加载进度
     */
    @Override
    public void hideLoadingDialog() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

}
