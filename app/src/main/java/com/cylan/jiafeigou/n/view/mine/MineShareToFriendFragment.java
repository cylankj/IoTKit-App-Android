package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToRelativeAndFriendContract;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.ShareToFriendsAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToFriendFragment extends Fragment implements MineShareToRelativeAndFriendContract.View {


    @BindView(R.id.iv_mine_share_to_relative_friend_back)
    ImageView ivMineShareToRelativeFriendBack;
    @BindView(R.id.tv_mine_share_to_relative_friend_true)
    TextView tvMineShareToRelativeFriendTrue;
    @BindView(R.id.rcy_mine_share_to_relative_and_friend_list)
    RecyclerView rcyMineShareToRelativeAndFriendList;
    @BindView(R.id.ll_no_friend)
    LinearLayout llNoFriend;
    private ShareToFriendsAdapter shareToFriendsAdapter;

    public static MineShareToFriendFragment newInstance() {
        return new MineShareToFriendFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_share_to_relative_and_friend, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void setPresenter(MineShareToRelativeAndFriendContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_mine_share_to_relative_friend_back, R.id.tv_mine_share_to_relative_friend_true})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.iv_mine_share_to_relative_friend_back:
                getFragmentManager().popBackStack();
                break;

            case R.id.tv_mine_share_to_relative_friend_true:
                //TODO 确定分享
                ToastUtil.showToast(getContext(), "分享成功");
                break;
        }
    }

    @Override
    public void initRecycleView(ArrayList<RelAndFriendBean> list) {
        rcyMineShareToRelativeAndFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        shareToFriendsAdapter = new ShareToFriendsAdapter(getContext(), list, null);
        rcyMineShareToRelativeAndFriendList.setAdapter(shareToFriendsAdapter);
        initAdaListener();
    }

    @Override
    public void showNoFriendNullView() {
        llNoFriend.setVisibility(View.VISIBLE);
    }

    /**
     * 列表的监听器
     */
    private void initAdaListener() {

    }
}
