package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativeAndFriendDetailPresenterImp;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineRelativeAndFriendDetailFragment extends Fragment implements MineRelativeAndFriendDetailContract.View {


    @BindView(R.id.iv_top_bar_left_back)
    ImageView ivTopBarLeftBack;
    @BindView(R.id.iv_detail_user_head)
    ImageView ivDetailUserHead;
    @BindView(R.id.tv_relative_and_friend_name)
    TextView tvRelativeAndFriendName;
    @BindView(R.id.tv_relative_and_friend_like_name)
    TextView tvRelativeAndFriendLikeName;
    @BindView(R.id.rl_change_name)
    RelativeLayout rlChangeName;
    @BindView(R.id.rl_delete_relativeandfriend)
    RelativeLayout rlDeleteRelativeandfriend;
    @BindView(R.id.tv_share_device)
    TextView tvShareDevice;

    private MineRelativesAndFriendsListShareDevicesFragment mineShareDeviceFragment;
    private MineRelativeAndFriendDetailContract.Presenter presenter;

    public static MineRelativeAndFriendDetailFragment newInstance() {
        return new MineRelativeAndFriendDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mineShareDeviceFragment = MineRelativesAndFriendsListShareDevicesFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativeandfriend_detail, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    private void initPresenter() {
        presenter = new MineRelativeAndFriendDetailPresenterImp(this);
    }

    @Override
    public void setPresenter(MineRelativeAndFriendDetailContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_top_bar_left_back, R.id.rl_change_name, R.id.rl_delete_relativeandfriend, R.id.tv_share_device})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_top_bar_left_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.rl_change_name:
                jump2SetRemarkNameFragment();
                break;
            case R.id.rl_delete_relativeandfriend:

                break;
            case R.id.tv_share_device:
                jump2ShareDeviceFragment();
                break;
        }
    }

    /**
     * desc:跳转到备注名称的界面；
     */
    private void jump2SetRemarkNameFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineShareDeviceFragment, "mineShareDeviceFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    private void jump2ShareDeviceFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineShareDeviceFragment, "mineShareDeviceFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }
}
