package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.os.Bundle;
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
public class MineDevicesShareManagerFragment extends Fragment implements MineDevicesShareManagerContract.View {

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
        initPresenter();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.initHasShareListData(getIntentData());
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

    public ArrayList<RelAndFriendBean> getIntentData() {
        Bundle arguments = getArguments();
        ArrayList<RelAndFriendBean> shareDeviceFriendlist = arguments.getParcelableArrayList("shareDeviceFriendlist");
        return shareDeviceFriendlist;
    }

    @Override
    public void showHasShareListTitle() {
        tvHasShareTitle.setVisibility(View.GONE);
        recyclerHadShareRelativesAndFriend.setVisibility(View.GONE);
    }

    @Override
    public void hideHasShareListTitle() {
        tvHasShareTitle.setVisibility(View.VISIBLE);
        recyclerHadShareRelativesAndFriend.setVisibility(View.VISIBLE);
    }

    @Override
    public void inintHasShareFriendRecyView(MineHasShareAdapter adapter) {
        recyclerHadShareRelativesAndFriend.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerHadShareRelativesAndFriend.setAdapter(adapter);
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
                presenter.cancleShare(bean);
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
}
