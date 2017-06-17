package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.FriendBean;
import com.cylan.jiafeigou.cache.db.module.FriendsReqBean;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendsContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendsPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.AddRelativesAndFriendsAdapter;
import com.cylan.jiafeigou.n.view.adapter.RelativesAndFriendsAdapter;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.utils.ContextUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
@Badge(parentTag = "HomeMineFragment")
public class MineFriendsFragment extends IBaseFragment<MineFriendsContract.Presenter> implements MineFriendsContract.View, AddRelativesAndFriendsAdapter.OnAcceptClickListener {

    @BindView(R.id.recyclerview_request_add)
    RecyclerView rvReqAdd;
    @BindView(R.id.rv_friends_list)
    RecyclerView rvFriendsList;
    @BindView(R.id.ll_relative_and_friend)
    LinearLayout llRelativeAndFriend;
    @BindView(R.id.ll_relative_and_friend_none)
    RelativeLayout llRelativeAndFriendNone;
    @BindView(R.id.tv_add_request_title)
    TextView tvAddRequestTitle;
    @BindView(R.id.tv_friend_list_title)
    TextView tvFriendListTitle;


    private MineFriendDetailFragment friendDetailFragment;
    private AddFriendReqDetailFragment addReqDetailFragment;
    private AddRelativesAndFriendsAdapter addReqListAdapter;
    private RelativesAndFriendsAdapter friendsListAdapter;
    private FriendsReqBean tempReqBean;

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
        View view = inflater.inflate(R.layout.fragment_home_mine_friends, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        showLoadingDialog();
        friendsListAdapter = new RelativesAndFriendsAdapter(getContext(), null, null);
        rvFriendsList.setAdapter(friendsListAdapter);
        addReqListAdapter = new AddRelativesAndFriendsAdapter(getContext(), null, null);
        rvReqAdd.setAdapter(addReqListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void jump2AddReqDetailFragment(int position, FriendsReqBean bean) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFrom", true);
        bundle.putSerializable("addRequestItems", bean);
        addReqDetailFragment = AddFriendReqDetailFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                addReqDetailFragment, android.R.id.content);

        addReqDetailFragment.setOnAcceptAddListener(backbean -> {
            addReqDeleteItem(position, backbean);
            FriendBean rBean = new FriendBean();
            rBean.account = backbean.account;
            rBean.alias = backbean.alias;
            rBean.iconUrl = backbean.iconUrl;
            rBean.markName = "";
            friendListAddItem(rBean);
        });
    }

    /**
     * desc：点击同意按钮弹出对话框
     */
    @Override
    public void showReqOutTimeDialog(final FriendsReqBean item) {
        //请求过期
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.Tap3_FriendsAdd_ExpiredTips));
        builder.setPositiveButton(getString(R.string.Tap3_FriendsAdd_Send), (dialog, which) -> {
            tempReqBean = item;
            dialog.dismiss();
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFrom", false);
            bundle.putSerializable("addRequestItems", item);
            AddFriendReqDetailFragment addReqDetailFragment = AddFriendReqDetailFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                    addReqDetailFragment, android.R.id.content);
            basePresenter.deleteAddReq(item.account);
            //向对方发送请求
        });
        builder.setNegativeButton(getString(R.string.CANCEL), (dialog, which) -> {
            basePresenter.deleteAddReq(item.account);
            addReqListAdapter.remove(item);
            dialog.dismiss();
        }).show();
    }

    public void showNullView() {
        llRelativeAndFriendNone.setVisibility(View.VISIBLE);
    }

    /**
     * 添加请求列表删除一个条目
     *
     * @param bean
     */
    public void addReqDeleteItem(int position, FriendsReqBean bean) {
        if (position < addReqListAdapter.getCount()) {
            addReqListAdapter.remove(position);
        }
        if (addReqListAdapter.getItemCount() == 0) {
            hideAddReqListTitle();
        }
        basePresenter.removeCache(bean.account);
    }

    /**
     * 长按删除添加请求条目
     */
    @Override
    public void deleteItemRsp(final String account, int code) {
        if (isAdded() && getView() != null) {
            getView().post(() -> {
                if (code != JError.ErrorOK) {
                    ToastUtil.showToast(getString(R.string.Tips_DeleteFail));
                    return;
                }
                addReqListAdapter.remove(tempReqBean);
                if (addReqListAdapter.getItemCount() == 0) {
                    hideAddReqListTitle();
                    if (friendsListAdapter.getItemCount() == 0) {
                        showNullView();
                    }
                }
            });
        }
    }

    @Override
    public void consentRsp(final String account, int code) {
        if (isAdded() && getView() != null)
            getView().post(() -> {
                hideLoadingDialog();
                switch (code) {
                    case -1:
                        ToastUtil.showToast(getString(R.string.Request_TimeOut));
                        break;
                    case JError.ErrorOK:
                        ToastUtil.showToast(getString(R.string.Tap3_FriendsAdd_Success));
                        //更新好友列表
                        FriendBean bean = getAndRemoveBean(account);
                        friendListAddItem(bean);
                        break;
                    case 240:
                        ToastUtil.showToast(getString(R.string.RET_EFORGETPASS_ACCOUNT_NOT_EXIST));
                        break;
                }
            });
    }

    /**
     * 综合处理
     *
     * @param account
     * @return
     */
    private FriendBean getAndRemoveBean(final String account) {
        ArrayList<FriendsReqBean> list = (ArrayList<FriendsReqBean>) addReqListAdapter.getList();
        if (list != null) {
            for (FriendsReqBean bean : list) {
                if (bean != null && TextUtils.equals(bean.account, account)) {
                    FriendBean friendBean = new FriendBean();
                    friendBean.account = bean.account;
                    friendBean.alias = bean.alias;
                    friendBean.iconUrl = bean.iconUrl;
                    friendBean.markName = "";
                    addReqListAdapter.remove(bean);
                    return friendBean;
                }
            }
        }
        if (addReqListAdapter != null && addReqListAdapter.getItemCount() == 0) {
            hideAddReqListTitle();
        }
        return null;
    }

    /**
     * 好友列表添加一个条目
     *
     * @param bean
     */
    public void friendListAddItem(FriendBean bean) {
        if (friendsListAdapter == null || bean == null) return;
        if (friendsListAdapter.getItemCount() == 0) {
            showFriendListTitle();
        }
        friendsListAdapter.add(0, bean);
    }

    /**
     * 显示加载进度
     */
    public void showLoadingDialog() {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.LOADING));
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == 0) {
            hideLoadingDialog();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    /**
     * 隐藏加载进度
     */
    public void hideLoadingDialog() {
    }

    public void showLongClickDialog(final int position, final FriendsReqBean bean) {
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivity());
        builder.setTitle(R.string.Tips_SureDelete)
                .setPositiveButton(getString(R.string.DELETE), (dialog, which) -> {
                    tempReqBean = bean;
                    basePresenter.deleteAddReq(bean.account);
                })
                .setNegativeButton(getString(R.string.CANCEL), null);
        AlertDialogManager.getInstance().showDialog("showLongClickDialog", getActivity(), builder);

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
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_toolbar_right));
                AppLogger.d("tv_toolbar_right");
                jump2AddReAndFriendFragment();
                break;
        }
    }

    private void jump2AddReAndFriendFragment() {
        ActivityUtils.addFragmentSlideInFromRight(
                getActivity().getSupportFragmentManager(),
                MineFriendAddFriendsFragment.newInstance(), android.R.id.content);
    }

    @Override
    public void initFriendList(ArrayList<FriendBean> list) {
        hideLoadingDialog();
        rvFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsListAdapter = new RelativesAndFriendsAdapter(getContext(), list, null);
        rvFriendsList.setAdapter(friendsListAdapter);
        friendsListAdapter.setOnItemClickListener((itemView, viewType, position) -> {
            if (getView() != null) {
                jump2FriendDetailFragment(position, friendsListAdapter.getList().get(position));
            }
        });
        showFriendListTitle();
        tvFriendListTitle.setVisibility(ListUtils.isEmpty(list) ? View.GONE : View.VISIBLE);
        rvFriendsList.setVisibility(ListUtils.isEmpty(list) ? View.GONE : View.VISIBLE);
        if (!ListUtils.isEmpty(list)) {
            llRelativeAndFriendNone.setVisibility(View.GONE);
        }
    }

    @Override
    public void initAddReqReqList(ArrayList<FriendsReqBean> list) {
        hideLoadingDialog();
        rvReqAdd.setLayoutManager(new LinearLayoutManager(getContext()));
        addReqListAdapter = new AddRelativesAndFriendsAdapter(getContext(), list, null);
        rvReqAdd.setAdapter(addReqListAdapter);
        addReqListAdapter.setOnAcceptClickListener(this);
        addReqListAdapter.setOnItemClickListener((itemView, viewType, position) -> {
            if (getView() != null) {
                jump2AddReqDetailFragment(position, addReqListAdapter.getList().get(position));
            }
        });
        addReqListAdapter.setOnItemLongClickListener((itemView, viewType, position) -> {
            if (getView() != null) {
                showLongClickDialog(position, addReqListAdapter.getList().get(position));
            }
        });
        if (!ListUtils.isEmpty(list)) {
            llRelativeAndFriendNone.setVisibility(View.GONE);
        }
        tvAddRequestTitle.setVisibility(ListUtils.isEmpty(list) ? View.GONE : View.VISIBLE);
        rvReqAdd.setVisibility(ListUtils.isEmpty(list) ? View.GONE : View.VISIBLE);
    }

    public void showFriendListTitle() {
        tvFriendListTitle.setVisibility(View.VISIBLE);
        rvFriendsList.setVisibility(View.VISIBLE);
    }

    public void hideFriendListTitle() {
        tvFriendListTitle.setVisibility(View.GONE);
        rvFriendsList.setVisibility(View.GONE);
    }

    public void showAddReqListTitle() {
        tvAddRequestTitle.setVisibility(View.VISIBLE);
        rvReqAdd.setVisibility(View.VISIBLE);
    }

    public void hideAddReqListTitle() {
        tvAddRequestTitle.setVisibility(View.GONE);
        rvReqAdd.setVisibility(View.GONE);
    }

    public void jump2FriendDetailFragment(int position, FriendBean account) {
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        bundle.putParcelable("frienditembean", account);
        friendDetailFragment = MineFriendDetailFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), friendDetailFragment,
                android.R.id.content);
        initFriendDeleteListener();
    }

    /**
     * 好友详情界面点击删除回调
     */
    private void initFriendDeleteListener() {
        friendDetailFragment.setOnDeleteClickLisenter(position -> {
            friendsListAdapter.remove(position);
            if (friendsListAdapter != null && friendsListAdapter.getItemCount() == 0) {
                hideFriendListTitle();
                if (addReqListAdapter == null || addReqListAdapter.getItemCount() == 0) {
                    showNullView();
                }
            }
        });
    }

    /**
     * desc:点击同意按钮
     *
     * @param holder
     * @param viewType
     * @param layoutPosition
     * @param item
     */
    @Override
    public void onAccept(SuperViewHolder holder, int viewType, int layoutPosition, FriendsReqBean item) {
        if (basePresenter.checkAddRequestOutTime(item)) {
            showReqOutTimeDialog(item);

        } else {
            //调用添加成功
            if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {
                ToastUtil.showToast(getString(R.string.NO_NETWORK_4));
                return;
            }
            basePresenter.acceptAddSDK(item.account);
            showLoadingDialog();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
//        TreeNode node = helper.findTreeNodeByName(MineFriendsFragment.class.getSimpleName());
//        if (node != null)
//            node.setCacheData(addReqListAdapter == null ? 0 : addReqListAdapter.getCount());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @OnClick(R.id.btn_to_add)
    public void onClickToAdd() {
        jump2AddReAndFriendFragment();
    }
}
