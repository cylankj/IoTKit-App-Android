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
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativesFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativesandFriendsPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.AddRelativesAndFriendsAdapter;
import com.cylan.jiafeigou.n.view.adapter.RelativesAndFriendsAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineRelativesandFriendsFragment extends Fragment implements MineRelativesFriendsContract.View {


    @BindView(R.id.iv_home_mine_relativesandfriends_back)
    ImageView ivHomeMineRelativesandfriendsBack;
    @BindView(R.id.recyclerview_request_add)
    RecyclerView recyclerviewRequestAdd;
    @BindView(R.id.recyclerview_relativesandfriends_list)
    RecyclerView recyclerviewRelativesandfriendsList;
    @BindView(R.id.tv_home_mine_relativesandfriends_add)
    TextView tvHomeMineRelativesandfriendsAdd;
    @BindView(R.id.ll_relative_and_friend)
    LinearLayout llRelativeAndFriend;
    @BindView(R.id.ll_relative_and_friend_none)
    LinearLayout llRelativeAndFriendNone;
    @BindView(R.id.tv_add_request_title)
    TextView tvAddRequestTitle;
    @BindView(R.id.tv_friend_list_title)
    TextView tvFriendListTitle;
    @BindView(R.id.btn_add_relative_and_friend)
    TextView btnAddRelativeAndFriend;


    private MineRelativesFriendsContract.Presenter presenter;

    private MineRelativesAndFriendAddFriendsFragment friendsFragment;
    private MineRelativeAndFriendDetailFragment relativeAndFrienDetialFragment;
    private MineRelativeAndFriendAddReqDetailFragment addReqDetailFragment;

    public static MineRelativesandFriendsFragment newInstance() {
        return new MineRelativesandFriendsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendsFragment = MineRelativesAndFriendAddFriendsFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_relativesandfriends, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null){
            presenter.start();
        }
    }

    @Override
    public void jump2AddReqDetailFragment(int position, JFGFriendRequest bean) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("addRequestItems", bean);
        addReqDetailFragment = MineRelativeAndFriendAddReqDetailFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, addReqDetailFragment, "addReqDetailFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * desc：点击同意按钮弹出对话框
     */
    @Override
    public void showReqOutTimeDialog() {
        //请求过期
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getView().getContext());
        builder.setMessage("当前消息已过期，是否向对方发送添加好友验证？");
        builder.setPositiveButton("发送", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ToastUtil.showToast(getView().getContext(),"请求已发送");
                //TODO SDK 向对方发送请求
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
    public void showNullView() {
        llRelativeAndFriendNone.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLongClickDialog(final int position, final JFGFriendRequest bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton("删除该请求", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.addReqDeleteItem(position,bean);
                dialog.dismiss();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void initPresenter() {
        presenter = new MineRelativesandFriendsPresenterImp(this);
    }

    @Override
    public void setPresenter(MineRelativesFriendsContract.Presenter presenter) {
    }

    @OnClick({R.id.iv_home_mine_relativesandfriends_back, R.id.tv_home_mine_relativesandfriends_add})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.iv_home_mine_relativesandfriends_back:
                getFragmentManager().popBackStack();
                break;

            case R.id.tv_home_mine_relativesandfriends_add:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_home_mine_relativesandfriends_add));
                AppLogger.e("tv_home_mine_relativesandfriends_add");
                jump2AddReAndFriendFragment();
                break;
        }
    }

    private void jump2AddReAndFriendFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, friendsFragment, "friendsFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    @Override
    public void initFriendRecyList(RelativesAndFriendsAdapter adapter) {
        recyclerviewRelativesandfriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerviewRelativesandfriendsList.setAdapter(adapter);
    }

    @Override
    public void initAddReqRecyList(AddRelativesAndFriendsAdapter adapter) {
        recyclerviewRequestAdd.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerviewRequestAdd.setAdapter(adapter);
    }

    @Override
    public void showFriendListTitle() {
        tvFriendListTitle.setVisibility(View.VISIBLE);
        recyclerviewRelativesandfriendsList.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideFriendListTitle() {
        tvFriendListTitle.setVisibility(View.GONE);
        recyclerviewRelativesandfriendsList.setVisibility(View.GONE);
    }

    @Override
    public void showAddReqListTitle() {
        tvAddRequestTitle.setVisibility(View.VISIBLE);
        recyclerviewRequestAdd.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideAddReqListTitle() {
        tvAddRequestTitle.setVisibility(View.GONE);
        recyclerviewRequestAdd.setVisibility(View.GONE);
    }

    @Override
    public void jump2FriendDetailFragment(int position, JFGFriendAccount account) {
        Bundle bundle = new Bundle();
        bundle.putInt("position",position);
        bundle.putSerializable("frienditembean",account);
        relativeAndFrienDetialFragment = MineRelativeAndFriendDetailFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, relativeAndFrienDetialFragment, "relativeAndFrienDetialFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

}
