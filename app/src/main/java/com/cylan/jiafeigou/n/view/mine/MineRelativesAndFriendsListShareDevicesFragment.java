package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativesAndFriendListShareDevicesToContract;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineRelativesAndFriendsListShareDevicesFragment extends Fragment implements MineRelativesAndFriendListShareDevicesToContract.View {

    @BindView(R.id.iv_mine_friends_share_devices_back)
    ImageView ivMineFriendsShareDevicesBack;
    @BindView(R.id.iv_mine_friends_share_devices_ok)
    ImageView ivMineFriendsShareDevicesOk;
    @BindView(R.id.rl_share_smartcamera)
    RelativeLayout rlShareSmartcamera;


    public static MineRelativesAndFriendsListShareDevicesFragment newInstance() {
        return new MineRelativesAndFriendsListShareDevicesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativeandfriend_share_devices, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(MineRelativesAndFriendListShareDevicesToContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_mine_friends_share_devices_back, R.id.iv_mine_friends_share_devices_ok, R.id.rl_share_smartcamera})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_friends_share_devices_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_friends_share_devices_ok:
                ToastUtil.showToast(getContext(), "分享成功。。。");
                break;

            case R.id.rl_share_smartcamera:

                break;
        }
    }

}
