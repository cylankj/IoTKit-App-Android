package com.cylan.jiafeigou.n.view.mine;

import android.databinding.ObservableBoolean;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.FragmentMineFriendDetailBinding;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendInformationContact;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendInformationPresenter;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.JFGAccountURL;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineFriendInformationFragment extends IBaseFragment implements MineFriendInformationContact.View {

    private MineFriendsListShareDevicesFragment mineShareDeviceFragment;
    private MineSetRemarkNameFragment mineSetRemarkNameFragment;
    private MineLookBigImageFragment mineLookBigImageFragment;

    private MineFriendInformationContact.Presenter presenter;
    public FriendEventCallback eventCallback;
    private FriendContextItem friendItem;
    private FragmentMineFriendDetailBinding friendDetailBinding;
    private ObservableBoolean isFriend = new ObservableBoolean(false);
    private AlertDialog alertDialog;

    public interface FriendEventCallback {
        void onDeleteFriend(FriendContextItem friendItem);

        void onAddFriend(FriendContextItem friendItem);

        void onModifyMarkName(FriendContextItem friendItem);
    }


    public void setFriendEventCallback(FriendEventCallback eventCallback) {
        this.eventCallback = eventCallback;
    }

    public static MineFriendInformationFragment newInstance(Bundle bundle) {
        MineFriendInformationFragment fragment = new MineFriendInformationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        friendDetailBinding = FragmentMineFriendDetailBinding.inflate(inflater, container, false);
        initPresenter();
        initFriendView();
        return friendDetailBinding.getRoot();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    /**
     * desc:获取到传过来的数据
     */
    private void initFriendView() {
        Bundle bundle = getArguments();
        friendItem = bundle.getParcelable("friendItem");

        friendDetailBinding.customToolbar.setBackAction(this::onClick);
        friendDetailBinding.changeNameMessageTitle.setOnClickListener(this::onClick);
        friendDetailBinding.deleteFriendTitle.setOnClickListener(this::onClick);
        friendDetailBinding.shareDeleteFunction.setOnClickListener(this::onClick);
        friendDetailBinding.friendInfoPicture.setOnClickListener(this::onClick);

        String alias = friendItem.getAlias();
        String nick;
        String account;
        if (friendItem.childType == 0) {//request
            isFriend.set(false);
            nick = friendItem.friendRequest.account;
            account = friendItem.friendRequest.account;
            friendDetailBinding.setMessage(friendItem.friendRequest.sayHi);
        } else {//friend
            isFriend.set(true);
            nick = TextUtils.isEmpty(friendItem.friendAccount.alias) ? friendItem.friendAccount.account : friendItem.friendAccount.alias;
            account = friendItem.friendAccount.account;
        }

        friendDetailBinding.setIsFriend(isFriend);
        friendDetailBinding.setAlias(alias);
        friendDetailBinding.setAccount(nick);
        Glide.with(this).load(new JFGAccountURL(account))
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(friendDetailBinding.friendInfoPicture);
    }

    private void initPresenter() {
        presenter = new MineFriendInformationPresenter(this);
    }

    @Override
    public void setPresenter(MineFriendInformationContact.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack(MineFriendInformationFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
            case R.id.change_name_message_title:
                ViewUtils.deBounceClick(friendDetailBinding.changeNameMessageTitle);
                AppLogger.e("rl_change_name");
                enterEditAccountMarkNameFragment();
                break;
            case R.id.delete_friend_title:
                ViewUtils.deBounceClick(friendDetailBinding.deleteFriendTitle);
                AppLogger.e("rl_delete_relativeandfriend");
                showDeleteFriendAlert();
                break;
            case R.id.share_delete_function:
                ViewUtils.deBounceClick(friendDetailBinding.shareDeleteFunction);
                AppLogger.e("tv_share_device");
                if (isFriend.get()) {
                    AppLogger.d("将分享设备");
                    if (presenter.getOwnerDeviceCount() > 0) {
                        enterShareDeviceListFragment();
                    } else {
                        ToastUtil.showNegativeToast(getString(R.string.Tap1_Index_NoDevice));
                    }
                } else {//添加亲友
                    AppLogger.d("将添加亲友");
                    presenter.consentFriend(friendItem);
                }
//
                break;
            case R.id.friend_info_picture:
                ViewUtils.deBounceClick(friendDetailBinding.friendInfoPicture);
                AppLogger.e("iv_detail_user_head");
                enterUserBigPictureFragment();
                break;
        }
    }


    /**
     * 删除亲友对话框
     */
    public void showDeleteFriendAlert() {
        if (alertDialog != null && alertDialog.isShowing()) return;
        alertDialog = new AlertDialog.Builder(getContext())
                .setMessage(R.string.Tap3_Friends_DeleteFriends)
                .setPositiveButton(R.string.OK, (dialog, which) -> {
                    presenter.deleteFriend(friendItem);
                })
                .setNegativeButton(R.string.CANCEL, null)
                .setCancelable(false)
                .show();
    }

    @Override
    public void onDeleteResult(int code) {
        if (code == 0) {
            ToastUtil.showPositiveToast(getString(R.string.DELETED_SUC));
            getActivity().getSupportFragmentManager().popBackStack();
            if (eventCallback != null) {
                eventCallback.onDeleteFriend(friendItem);
            }
        } else if (code == -1) {
            // TODO: 2017/6/30 超时了
        } else {
            ToastUtil.showNegativeToast(getString(R.string.Tips_DeleteFail));
        }
    }

    @Override
    public void showLoading(int resId, String... args) {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(resId, (Object[]) args));
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    /**
     * desc:查看大头像
     */
    private void enterUserBigPictureFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("friendItem", friendItem);
        mineLookBigImageFragment = MineLookBigImageFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), mineLookBigImageFragment, android.R.id.content);
    }

    /**
     * desc:跳转到备注名称的界面；
     */
    private void enterEditAccountMarkNameFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("friendItem", friendItem);
        mineSetRemarkNameFragment = MineSetRemarkNameFragment.newInstance(bundle);
        mineSetRemarkNameFragment.setOnSetRemarkNameListener(name -> {
            friendDetailBinding.setAlias(name);
            friendItem.friendAccount.markName = name;
            if (eventCallback != null) {
                eventCallback.onModifyMarkName(friendItem);
            }
        });
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), mineSetRemarkNameFragment, android.R.id.content);
    }

    private void enterShareDeviceListFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("friendItem", friendItem);
        mineShareDeviceFragment = MineFriendsListShareDevicesFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), mineShareDeviceFragment, android.R.id.content);

    }

    private void enterAddRequestFragment() {
        Bundle addReqBundle = new Bundle();
        addReqBundle.putParcelable("friendItem", friendItem);
        MineAddFromContactFragment addReqFragment = MineAddFromContactFragment.newInstance(addReqBundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), addReqFragment, android.R.id.content, MineFriendInformationFragment.class.getSimpleName());
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideLoading();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void onRequestExpired(FriendContextItem item) {
        AppLogger.d("好友添加请求已过期!!!");
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.Tap3_FriendsAdd_ExpiredTips)
                .setPositiveButton(R.string.Tap3_FriendsAdd_Send, (dialog, which) -> {
                    friendItem.friendRequest.time = System.currentTimeMillis();
                    friendItem.friendRequest.sayHi = null;//这是为了跳转到添加请求页面
                    presenter.consentFriend(friendItem);
                })
                .setNegativeButton(R.string.CANCEL, null)
                .show();
    }

    @Override
    public void onRequestByOwner(FriendContextItem friendContextItem) {
        AppLogger.d("是主动添加请求,不是接受添加请求");
        enterAddRequestFragment();
    }

    @Override
    public void acceptItemRsp(FriendContextItem friendContextItem, int code) {
        AppLogger.d("接受好友添加请求的结果为:" + code);
        switch (code) {
            case -1:
                ToastUtil.showToast(getString(R.string.Request_TimeOut));
                break;
            case JError.ErrorOK:
                ToastUtil.showPositiveToast(getString(R.string.Tap3_FriendsAdd_Success));
                if (eventCallback != null) {
                    eventCallback.onAddFriend(friendItem);
                }
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case 240:
                ToastUtil.showToast(getString(R.string.RET_EFORGETPASS_ACCOUNT_NOT_EXIST));
                break;
            default:
                ToastUtil.showNegativeToast(getString(R.string.ADD_FAILED));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
        IMEUtils.hide(getActivity());
    }
}
