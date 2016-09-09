package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineDevicesShareManagerContract;

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

    public static MineDevicesShareManagerFragment newInstance() {
        return new MineDevicesShareManagerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_device_share_manager, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(MineDevicesShareManagerContract.Presenter presenter) {
    }

    @OnClick(R.id.iv_home_mine_share_devices_manager_back)
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_home_mine_share_devices_manager_back:
                getFragmentManager().popBackStack();
                break;
        }
    }
}
