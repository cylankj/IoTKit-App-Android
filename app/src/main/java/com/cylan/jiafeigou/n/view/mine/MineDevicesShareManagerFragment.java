package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineDevicesShareManagerContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineDevicesShareManagerPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.MineHasShareAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public class MineDevicesShareManagerFragment extends Fragment implements MineDevicesShareManagerContract.View, MineHasShareAdapter.OnCancleShareListenter {

    @BindView(R.id.iv_home_mine_share_devices_manager_back)
    ImageView ivHomeMineShareDevicesManagerBack;
    @BindView(R.id.tv_home_mine_share_devices_name)
    TextView tvHomeMineShareDevicesName;
    @BindView(R.id.recycler_had_share_relatives_and_friend)
    RecyclerView recyclerHadShareRelativesAndFriend;
    @BindView(R.id.tv_has_share_title)
    TextView tvHasShareTitle;
    @BindView(R.id.ll_no_share_friend)
    LinearLayout llNoShareFriend;
    @BindView(R.id.rl_cancle_share_progress)
    RelativeLayout rlCancleShareProgress;

    private MineDevicesShareManagerContract.Presenter presenter;
    private MineHasShareAdapter hasShareAdapter;
    private DeviceBean devicebean;
    private RelAndFriendBean tempBean;

    public static MineDevicesShareManagerFragment newInstance(Bundle bundle) {
        MineDevicesShareManagerFragment fragment = new MineDevicesShareManagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_device_share_manager, container, false);
        ButterKnife.bind(this, view);
        getIntentData();
        initPresenter();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.getHasShareList(devicebean.uuid);
            presenter.start();
        }
    }

    private void initPresenter() {
        presenter = new MineDevicesShareManagerPresenterImp(this);
    }

    @Override
    public void setPresenter(MineDevicesShareManagerContract.Presenter presenter) {

    }

    @OnClick(R.id.iv_home_mine_share_devices_manager_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_share_devices_manager_back:
                getFragmentManager().popBackStack();
                break;
        }
    }

    public void getIntentData() {
        Bundle arguments = getArguments();
        devicebean = arguments.getParcelable("devicebean");
        setTopTitle("".equals(devicebean.alias)?devicebean.uuid:devicebean.alias);
    }

    @Override
    public void showHasShareListTitle() {
        tvHasShareTitle.setVisibility(View.VISIBLE);
        recyclerHadShareRelativesAndFriend.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideHasShareListTitle() {
        tvHasShareTitle.setVisibility(View.INVISIBLE);
        recyclerHadShareRelativesAndFriend.setVisibility(View.INVISIBLE);
    }

    /**
     * desc:初始化列表显示
     * @param list
     */
    @Override
    public void inintHasShareFriendRecyView(ArrayList<RelAndFriendBean> list) {
        recyclerHadShareRelativesAndFriend.setLayoutManager(new LinearLayoutManager(getContext()));
        hasShareAdapter = new MineHasShareAdapter(getView().getContext(),list,null);
        recyclerHadShareRelativesAndFriend.setAdapter(hasShareAdapter);
        initAdaListener();
    }

    /**
     * 设置列表的监听器
     */
    private void initAdaListener() {
        hasShareAdapter.setOnCancleShareListenter(this);
    }

    @Override
    public void showNoHasShareFriendNullView() {
        llNoShareFriend.setVisibility(View.VISIBLE);
    }

    @Override
    public void showCancleShareDialog(final RelAndFriendBean bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("是否删除对该用户的分享？");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                presenter.cancleShare(devicebean.uuid,bean);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void showCancleShareProgress() {
        rlCancleShareProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideCancleShareProgress() {
        rlCancleShareProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null){
            presenter.stop();
        }
    }

    @Override
    public void onCancleShare(RelAndFriendBean item) {
        tempBean = item;
        if (getView() != null){
            showCancleShareDialog(item);
        }
    }

    @Override
    public void deleteItems() {
        hasShareAdapter.remove(tempBean);
        hasShareAdapter.notifyDataSetHasChanged();
    }

    /**
     * 取消分享的结果
     * @param result
     */
    @Override
    public void showUnShareResult(String result) {
        ToastUtil.showToast(result);
    }

    /**
     * 顶部标题
     * @param name
     */
    @Override
    public void setTopTitle(String name) {
        tvHomeMineShareDevicesName.setText(name);
    }

}
