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
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;

import java.util.Arrays;
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
    private ObservableBoolean empty = new ObservableBoolean(true);

    private FastAdapter fastAdapter;
    private ItemAdapter<FriendContextItem> friendRequestAdapter;
    private ItemAdapter<FriendContextItem> friendAccountAdapter;


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
//        fastAdapter = new FastAdapter();
        mineFriendsBinding.setEmpty(empty);
        mineFriendsBinding.friends.setLayoutManager(new LinearLayoutManager(getContext()));
        friendRequestAdapter = new ItemAdapter() {
            @Override
            public int getOrder() {
                return 1500;
            }
        };
        friendRequestAdapter.withUseIdDistributor(true);
        friendAccountAdapter = new ItemAdapter() {
            @Override
            public int getOrder() {
                return 3000;
            }
        };
        friendAccountAdapter.withUseIdDistributor(true);
        fastAdapter = FastAdapter.with(Arrays.asList(friendRequestAdapter, friendAccountAdapter));
        fastAdapter.withOnLongClickListener(this::onFriendItemLongClick);
        fastAdapter.withOnClickListener(this::onFriendItemClick);
        fastAdapter.withEventHook(new ClickEventHook() {
            @Override
            public void onClick(View v, int position, FastAdapter fastAdapter, IItem item) {
                if (item instanceof FriendContextItem) {
                    AppLogger.d("点击了接受按钮");
                    presenter.acceptFriendRequest((FriendContextItem) item);
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
        mineFriendsBinding.friends.setAdapter(fastAdapter);
    }

    private boolean onFriendItemClick(View view, IAdapter iAdapter, IItem item, int position) {
        if (!(item instanceof FriendContextItem)) {
            return false;
        }
        FriendContextItem friendContextItem = (FriendContextItem) item;
        Bundle bundle = new Bundle();
        if (friendContextItem.friendRequest != null) {
            if (!presenter.checkRequestAvailable(friendContextItem)) {
                AppLogger.d("请求已过期,将删除过期请求");
                presenter.deleteFriendRequest(friendContextItem, false);
                friendContextItem.friendRequest.sayHi = "";
                friendContextItem.friendRequest.time = System.currentTimeMillis();
            }
        }
        bundle.putParcelable("friendItem", friendContextItem);
        friendInformationFragment = MineFriendInformationFragment.newInstance(bundle);
        friendInformationFragment.setFriendEventCallback(this);
        friendInformationFragment.setCallBack(t -> fastAdapter.notifyDataSetChanged());
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
                            presenter.deleteFriendRequest((FriendContextItem) item, true);
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
        presenter.initRequestAndFriendList();
    }

    @Override
    public void showLoading(int resId, String... args) {
        LoadingDialog.showLoading(getActivity(), getString(resId, (Object[]) args), true);
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading();
    }

    @Override
    public void onRequestExpired(FriendContextItem item, boolean alert) {
        //请求过期
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.Tap3_FriendsAdd_ExpiredTips));
        builder.setPositiveButton(getString(R.string.Tap3_FriendsAdd_Send), (dialog, which) -> {
            presenter.deleteFriendRequest(item, true);
            dialog.dismiss();
            Bundle bundle = new Bundle();
            bundle.putParcelable("friendItem", item);
            MineFriendInformationFragment informationFragment = MineFriendInformationFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), informationFragment, android.R.id.content);
        });
        builder.setNegativeButton(getString(R.string.CANCEL), (dialog, which) -> {
            presenter.deleteFriendRequest(item, alert);
            dialog.dismiss();
        }).show();
    }

    @Override
    public void deleteItemRsp(FriendContextItem item, int code, boolean alert) {
        if (code == JError.ErrorOK) {
            modifyAdapter(item, 1);
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
        empty.set(fastAdapter.getItemCount() == 0);
    }

    @Override
    public void acceptItemRsp(FriendContextItem item, int code) {
        switch (code) {
            case -1:
                ToastUtil.showToast(getString(R.string.Request_TimeOut));
                break;
            case JError.ErrorOK:
                modifyAdapter(item, 0);
                ToastUtil.showToast(getString(R.string.Tap3_FriendsAdd_Success));
                break;
            case 240:
                ToastUtil.showToast(getString(R.string.RET_EFORGETPASS_ACCOUNT_NOT_EXIST));
                break;
            case JError.ErrorFriendInvalidRequest:
                onRequestExpired(item, false);
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
        friendRequestAdapter.clear();
        friendAccountAdapter.clear();
        if (request != null && request.size() > 0) {
            FriendContextHeader requestHeader = new FriendContextHeader().withHeader(getString(R.string.Tap3_FriendsAdd_Request));
            ((ItemAdapter) friendRequestAdapter).add(requestHeader);
            friendRequestAdapter.add(request);
        }

        if (friends != null && friends.size() > 0) {
            FriendContextHeader friendHeader = new FriendContextHeader().withHeader(getString(R.string.Tap3_FriendsList));
            ((ItemAdapter) friendAccountAdapter).add(friendHeader);
            friendAccountAdapter.add(friends);
        }
        empty.set(fastAdapter.getItemCount() == 0);
    }

    private void initPresenter() {
        presenter = new MineFriendsPresenterImp(this);
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
        modifyAdapter(friendItem, 2);
    }

    @Override
    public void onAddFriend(FriendContextItem friendItem) {
        modifyAdapter(friendItem, 0);
    }

    /**
     * @param modifyType 0:acceptRequest;1:deleteRequest;2:deleteFriend
     */
    private void modifyAdapter(FriendContextItem item, int modifyType) {//0:accept;1:deleteRequest;2:deleteFriend;
        switch (modifyType) {
            case 0:
                friendRequestAdapter.remove(friendRequestAdapter.getAdapterPosition(item));
                if (friendRequestAdapter.getAdapterItemCount() == 1) {//说明只剩下 header 了
                    friendRequestAdapter.clear();
                }
                FriendContextItem friendContextItem = new FriendContextItem(new JFGFriendAccount(item.friendRequest.account, null, item.friendRequest.alias));
                if (friendAccountAdapter.getAdapterItemCount() == 0) {
                    FriendContextHeader friendHeader = new FriendContextHeader().withHeader(getString(R.string.Tap3_FriendsList));
                    ((ItemAdapter) friendAccountAdapter).add(friendHeader);
                }
                friendAccountAdapter.add(friendAccountAdapter.getGlobalPosition(1), friendContextItem);
                break;
            case 1:
                friendRequestAdapter.remove(friendRequestAdapter.getAdapterPosition(item));
                if (friendRequestAdapter.getAdapterItemCount() == 1) {//说明只剩下 header 了
                    friendRequestAdapter.clear();
                }
                break;
            case 2:
                friendAccountAdapter.remove(friendAccountAdapter.getAdapterPosition(item));
                if (friendAccountAdapter.getAdapterItemCount() == 1) {
                    friendAccountAdapter.clear();
                }
                break;
        }
        empty.set(fastAdapter.getItemCount() == 0);
    }

    @Override
    public void onModifyMarkName(FriendContextItem friendItem) {
        int position = fastAdapter.getPosition(friendItem);
        IItem item = fastAdapter.getItem(position);
        if (item instanceof FriendContextItem && ((FriendContextItem) item).friendAccount != null) {
            ((FriendContextItem) item).friendAccount.markName = friendItem.friendAccount.markName;
            fastAdapter.notifyItemChanged(position);
        }
    }

}
