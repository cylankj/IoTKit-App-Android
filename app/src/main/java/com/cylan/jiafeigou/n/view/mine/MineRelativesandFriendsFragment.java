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

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativesFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativesandFriendsPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.AddRelativesAndFriendsAdapter;
import com.cylan.jiafeigou.n.view.adapter.RelativesAndFriendsAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;

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


    private MineRelativesFriendsContract.Presenter presenter;

    private MineRelativesAndFriendAddFriendsFragment friendsFragment;
    private MineRelativeAndFriendDetailFragment relativeAndFrienDetialFragment;
    private MineRelativeAndFriendAddReqDetailFragment addReqDetailFragment;

    private AddRelativesAndFriendsAdapter relativesAndFriendsAddAdapter;
    private RelativesAndFriendsAdapter relativesAndFriendsListAdapter;

    private ArrayList<SuggestionChatInfoBean> requestAddList;
    private ArrayList<SuggestionChatInfoBean> relativesAndFriendList;

    public static MineRelativesandFriendsFragment newInstance() {
        return new MineRelativesandFriendsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendsFragment = MineRelativesAndFriendAddFriendsFragment.newInstance();
        relativeAndFrienDetialFragment = MineRelativeAndFriendDetailFragment.newInstance();
        addReqDetailFragment = MineRelativeAndFriendAddReqDetailFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_relativesandfriends, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initData();
        return view;
    }

    private void initData() {
        requestAddList = new ArrayList<>();
        relativesAndFriendList = new ArrayList<>();
        requestAddList.addAll(presenter.initAddRequestData());
        relativesAndFriendList.addAll(presenter.initRelativatesAndFriendsData());

        if (presenter.initRelativatesAndFriendsData().size() == 0) {
            llRelativeAndFriend.setVisibility(View.GONE);
            llRelativeAndFriendNone.setVisibility(View.VISIBLE);
        } else {
            llRelativeAndFriend.setVisibility(View.VISIBLE);
            llRelativeAndFriendNone.setVisibility(View.GONE);
            showAddRequestList();
            showRelativesAndFriendsList();
            initListener();
        }

    }

    private void initListener() {
        relativesAndFriendsListAdapter.setItemClickListener(new RelativesAndFriendsAdapter.ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, relativeAndFrienDetialFragment, "relativeAndFrienDetialFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
            }
        });

        relativesAndFriendsAddAdapter.setItemLongClickLisenter(new AddRelativesAndFriendsAdapter.ItemLongClickLisenter() {
            @Override
            public void onItemLongClick(View view, int position) {
                showLongClickDialog(view,position);
            }
        });

        relativesAndFriendsAddAdapter.setItemClickListener(new AddRelativesAndFriendsAdapter.ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                presenter.doAddRequestClick(position,requestAddList,relativesAndFriendsAddAdapter,relativesAndFriendList,relativesAndFriendsListAdapter);
            }
        });

        relativesAndFriendsAddAdapter.setItemOutOfBtnClickListener(new AddRelativesAndFriendsAdapter.ItemOutOfBtnClickListener() {
            @Override
            public void onOutBtnClick(View view, int position) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, addReqDetailFragment, "addReqDetailFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
            }
        });
    }

    private void showLongClickDialog(View view, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton("删除该请求", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestAddList.remove(position);
                relativesAndFriendsAddAdapter.notifyDataSetChanged();
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

    @OnClick({R.id.iv_home_mine_relativesandfriends_back, R.id.tv_home_mine_relativesandfriends_add, R.id.btn_add_relative_and_friend})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.iv_home_mine_relativesandfriends_back:
                getFragmentManager().popBackStack();
                break;

            case R.id.tv_home_mine_relativesandfriends_add:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, friendsFragment, "friendsFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
                break;

        }

    }

    @Override
    public void showAddRequestList() {
        recyclerviewRequestAdd.setLayoutManager(new LinearLayoutManager(getContext()));
        relativesAndFriendsAddAdapter = new AddRelativesAndFriendsAdapter(requestAddList);
        recyclerviewRequestAdd.setAdapter(relativesAndFriendsAddAdapter);
    }

    @Override
    public void showRelativesAndFriendsList() {
        recyclerviewRelativesandfriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        relativesAndFriendsListAdapter = new RelativesAndFriendsAdapter(relativesAndFriendList);
        recyclerviewRelativesandfriendsList.setAdapter(relativesAndFriendsListAdapter);
    }

}
