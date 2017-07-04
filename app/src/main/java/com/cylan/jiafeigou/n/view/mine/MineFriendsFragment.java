package com.cylan.jiafeigou.n.view.mine;

import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.FragmentHomeMineFriendsBinding;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendsPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextHeader;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
@Badge(parentTag = "HomeMineFragment")
@SuppressWarnings("unchecked")
public class MineFriendsFragment extends IBaseFragment<MineFriendsContract.Presenter> implements MineFriendsContract.View, MineFriendInformationFragment.FriendEventCallback {

    @BindView(R.id.tv_toolbar_right)
    TextView addFriend;
    private MineFriendInformationFragment friendInformationFragment;
    private FragmentHomeMineFriendsBinding mineFriendsBinding;
    private ObservableBoolean empty = new ObservableBoolean(false);

    private FastItemAdapter friendsAdapter;


    public static MineFriendsFragment newInstance() {
        return new MineFriendsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mineFriendsBinding = FragmentHomeMineFriendsBinding.inflate(inflater, container, false);
        ButterKnife.bind(this, mineFriendsBinding.customToolbar);
        return mineFriendsBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        friendsAdapter = new FastItemAdapter();
        mineFriendsBinding.setEmpty(empty);
        mineFriendsBinding.friends.setAdapter(friendsAdapter);
        mineFriendsBinding.friends.setLayoutManager(new LinearLayoutManager(getContext()));

        friendsAdapter.withOnLongClickListener(this::onFriendItemLongClick);
        friendsAdapter.withOnClickListener(this::onFriendItemClick);
        friendsAdapter.withEventHook(new ClickEventHook() {
            @Override
            public void onClick(View v, int position, FastAdapter fastAdapter, IItem item) {
                if (item instanceof FriendContextItem) {
                    AppLogger.d("点击了接受按钮");
                    basePresenter.acceptFriendRequest((FriendContextItem) item);
                }
            }

            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder instanceof FriendContextItem.ViewHolder) {
                    return ((FriendContextItem.ViewHolder) viewHolder).accept;
                }
                return super.onBind(viewHolder);
            }
        });


    }

    private boolean onFriendItemClick(View view, IAdapter iAdapter, IItem item, int position) {
        if (!(item instanceof FriendContextItem)) return false;
        FriendContextItem friendContextItem = (FriendContextItem) item;
        Bundle bundle = new Bundle();
        if (friendContextItem.friendRequest != null) {
            basePresenter.deleteFriendRequest(friendContextItem, false);
            friendContextItem.friendRequest.sayHi = null;
            friendContextItem.friendRequest.time = System.currentTimeMillis();
        }
        bundle.putParcelable("friendItem", friendContextItem);
        friendInformationFragment = MineFriendInformationFragment.newInstance(bundle);
        friendInformationFragment.setFriendEventCallback(this);
        friendInformationFragment.setCallBack(t -> friendsAdapter.notifyDataSetChanged());
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), friendInformationFragment,
                android.R.id.content);
        return true;
    }

    private boolean onFriendItemLongClick(View view, IAdapter iAdapter, IItem item, int position) {
        int childType = -1;
        if (item instanceof FriendContextItem) {
            childType = ((FriendContextItem) item).childType;
            if (childType == 0) {
                AppLogger.d("长按了请求条目");
                AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivity());
                builder.setTitle(R.string.Tips_SureDelete)
                        .setPositiveButton(getString(R.string.DELETE), (dialog, which) -> {
                            basePresenter.deleteFriendRequest((FriendContextItem) item, true);
                        })
                        .setNegativeButton(getString(R.string.CANCEL), null);
                AlertDialogManager.getInstance().showDialog("showLongClickDialog", getActivity(), builder);
            }
        }
        return childType == 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        basePresenter.initRequestAndFriendList();
    }

    @Override
    public void showLoading(int resId, String... args) {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(resId, (Object[]) args));
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onRequestExpired(FriendContextItem item) {
        //请求过期
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.Tap3_FriendsAdd_ExpiredTips));
        builder.setPositiveButton(getString(R.string.Tap3_FriendsAdd_Send), (dialog, which) -> {
            dialog.dismiss();
            Bundle bundle = new Bundle();
            bundle.putParcelable("friendItem", item);
            MineFriendInformationFragment informationFragment = MineFriendInformationFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), informationFragment, android.R.id.content);
        });
        builder.setNegativeButton(getString(R.string.CANCEL), (dialog, which) -> {
            basePresenter.deleteFriendRequest(item, true);
            dialog.dismiss();
        }).show();
    }

    @Override
    public void deleteItemRsp(FriendContextItem item, int code, boolean alert) {
        if (code == JError.ErrorOK) {
            FriendContextHeader parent = item.parent;
            if (parent != null && parent.children != null) {
                parent.children.remove(item);
                if (parent.children.size() == 0) {
                    int position = friendsAdapter.getPosition(parent);
                    friendsAdapter.remove(position);
                }
            }
            int position = friendsAdapter.getPosition(item);
            friendsAdapter.remove(position);
            if (alert) {
                ToastUtil.showToast(getString(R.string.DELETED_SUC));
            }

        } else if (code == -1) {
            // TODO: 2017/6/30 超时了
        } else {
            if (alert) {
                ToastUtil.showToast(getString(R.string.Tips_DeleteFail));
            }
        }
        empty.set(friendsAdapter.getItemCount() == 0);
    }

    @Override
    public void acceptItemRsp(FriendContextItem item, int code) {
        switch (code) {
            case -1:
                ToastUtil.showToast(getString(R.string.Request_TimeOut));
                break;
            case JError.ErrorOK:
                FriendContextHeader parent = item.parent;
                if (parent != null && parent.children != null) {
                    parent.children.remove(item);
                    if (parent.children.size() == 0) {
                        int position = friendsAdapter.getPosition(parent);
                        friendsAdapter.remove(position);
                    }
                }
                item.friendAccount = new JFGFriendAccount(item.friendRequest.account, null, item.friendRequest.alias);
                item.childType = 1;
                item.friendRequest = null;
                int position = friendsAdapter.getPosition(item);
                friendsAdapter.notifyItemChanged(position);
                friendsAdapter.move(position, friendsAdapter.getItemCount() - 1);
                ToastUtil.showToast(getString(R.string.Tap3_FriendsAdd_Success));
                break;
            case 240:
                ToastUtil.showToast(getString(R.string.RET_EFORGETPASS_ACCOUNT_NOT_EXIST));
                break;
        }
    }


    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == 0) {
            hideLoading();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void onInitRequestAndFriendList(List<FriendContextItem> request, List<FriendContextItem> friends) {
        friendsAdapter.clear();
        if (request != null && request.size() > 0) {
            FriendContextHeader requestHeader = new FriendContextHeader().withHeader(getString(R.string.Tap3_FriendsAdd_Request));
            requestHeader.withChildren(request);
            friendsAdapter.add(requestHeader);
            friendsAdapter.add(request);
        }

        if (friends != null && friends.size() > 0) {
            FriendContextHeader friendHeader = new FriendContextHeader().withHeader(getString(R.string.Tap3_FriendsList));
            friendHeader.withChildren(friends);
            friendsAdapter.add(friendHeader);
            friendsAdapter.add(friends);
        }

        empty.set(friendsAdapter.getItemCount() == 0);
    }

    private void initPresenter() {
        basePresenter = new MineFriendsPresenterImp(this);
    }

    @Override
    public void setPresenter(MineFriendsContract.Presenter basePresenter) {
    }

    @Override
    public String getUuid() {
        return "";
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;

            case R.id.tv_toolbar_right:
                ViewUtils.deBounceClick(addFriend);
                AppLogger.d("tv_toolbar_right");
                jump2AddReAndFriendFragment();
                break;
        }
    }

    private void jump2AddReAndFriendFragment() {
        MineFriendAddFriendsFragment friendAddFriendsFragment = MineFriendAddFriendsFragment.newInstance();
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), friendAddFriendsFragment, android.R.id.content);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
        TreeNode node = helper.findTreeNodeByName(MineFriendsFragment.class.getSimpleName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDeleteFriend(FriendContextItem friendItem) {
        deleteItemRsp(friendItem, 0, false);
    }

    @Override
    public void onAddFriend(FriendContextItem friendItem) {
        acceptItemRsp(friendItem, 0);
    }

    @Override
    public void onModifyMarkName(FriendContextItem friendItem) {
        friendsAdapter.notifyItemChanged(friendsAdapter.getPosition(friendItem));
    }

}
