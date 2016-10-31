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
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendsPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.AddRelativesAndFriendsAdapter;
import com.cylan.jiafeigou.n.view.adapter.RelativesAndFriendsAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.superadapter.OnItemClickListener;
import com.cylan.superadapter.OnItemLongClickListener;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendsFragment extends Fragment implements MineFriendsContract.View, AddRelativesAndFriendsAdapter.OnAcceptClickLisenter {


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


    private MineFriendsContract.Presenter presenter;

    private MineFriendAddFriendsFragment friendsFragment;
    private MineFriendDetailFragment relativeAndFrienDetialFragment;
    private MineFriendAddReqDetailFragment addReqDetailFragment;
    private AddRelativesAndFriendsAdapter addReqListAdater;
    private RelativesAndFriendsAdapter friendsListAdapter;

    public static MineFriendsFragment newInstance() {
        return new MineFriendsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendsFragment = MineFriendAddFriendsFragment.newInstance();
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
        addReqDetailFragment = MineFriendAddReqDetailFragment.newInstance(bundle);
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

    /**
     * 添加请求列表添加一个条目
     * @param position
     * @param bean
     */
    @Override
    public void addReqDeleteItem(int position, JFGFriendRequest bean) {
        addReqListAdater.remove(bean);
        addReqListAdater.notifyDataSetHasChanged();
        if (addReqListAdater.getItemCount()==0){
            hideAddReqListTitle();
        }
    }

    /**
     * 好友列表添加一个条目
     * @param position
     * @param bean
     */
    @Override
    public void friendlistAddItem(int position, JFGFriendAccount bean) {
        friendsListAdapter.add(0,bean);
        friendsListAdapter.notifyDataSetHasChanged();
    }

    @Override
    public void showLongClickDialog(final int position, final JFGFriendRequest bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setPositiveButton("删除该请求", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addReqDeleteItem(position,bean);
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
        presenter = new MineFriendsPresenterImp(this);
    }

    @Override
    public void setPresenter(MineFriendsContract.Presenter presenter) {
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
    public void initFriendRecyList(ArrayList<JFGFriendAccount> list) {
        recyclerviewRelativesandfriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsListAdapter = new RelativesAndFriendsAdapter(getView().getContext(),list,null);
        recyclerviewRelativesandfriendsList.setAdapter(friendsListAdapter);
        initFriendAdaListener();
    }

    /**
     * desc:设置好友列表的监听
     */
    private void initFriendAdaListener() {
        friendsListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int viewType, int position) {
                if (getView() != null){
                    jump2FriendDetailFragment(position,friendsListAdapter.getList().get(position));
                }
            }
        });
    }

    @Override
    public void initAddReqRecyList(ArrayList<JFGFriendRequest> list) {
        recyclerviewRequestAdd.setLayoutManager(new LinearLayoutManager(getContext()));
        addReqListAdater = new AddRelativesAndFriendsAdapter(getView().getContext(),list,null);
        recyclerviewRequestAdd.setAdapter(addReqListAdater);
        initAddReqAdaListener();
    }

    /**
     * desc：设置添加请求列表监听
     */
    private void initAddReqAdaListener() {
        addReqListAdater.setOnAcceptClickLisenter(this);
        addReqListAdater.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int viewType, int position) {
                if (getView() != null){
                    jump2AddReqDetailFragment(position,addReqListAdater.getList().get(position));
                }
            }
        });

        addReqListAdater.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View itemView, int viewType, int position) {
                if (getView() != null){
                    showLongClickDialog(position,addReqListAdater.getList().get(position));
                }
            }
        });
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
        relativeAndFrienDetialFragment = MineFriendDetailFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, relativeAndFrienDetialFragment, "relativeAndFrienDetialFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * desc:点击同意按钮
     * @param holder
     * @param viewType
     * @param layoutPosition
     * @param item
     */
    @Override
    public void onAccept(SuperViewHolder holder, int viewType, int layoutPosition, JFGFriendRequest item) {
        if (presenter.checkAddRequestOutTime(item)){
                showReqOutTimeDialog();
        }else {
            ToastUtil.showToast(getView().getContext(),"添加成功");
            JFGFriendAccount account = new JFGFriendAccount(item.account,"",item.alias);
            friendlistAddItem(layoutPosition,account);
            addReqDeleteItem(layoutPosition,item);
        }
    }
}
