package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.databinding.FragmentMineShareToContactBinding;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareToContactPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.n.view.adapter.item.ShareContactItem;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ClickEventHook;
import com.mikepenz.fastadapter.listeners.ItemFilterListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
@RuntimePermissions
public class MineContactManagerFragment extends IBaseFragment implements MineShareToContactContract.View {
    private MineShareToContactContract.Presenter presenter;
    private FastItemAdapter<ShareContactItem> shareFriendItemItemAdapter;
    private FragmentMineShareToContactBinding shareToContactBinding;
    private ObservableBoolean empty = new ObservableBoolean(true);
    private boolean searchMode = false;
    //Custom Toolbar Views
    @BindView(R.id.iv_mine_share_to_contact_search)
    ImageView searchContact;
    @BindView(R.id.tv_top_title)
    TextView searchBarTitle;
    @BindView(R.id.et_search_contact)
    EditText searchInput;
    int contactType = 0;//0:分享给联系人; 1:添加联系人;

    public static MineContactManagerFragment newInstance(Bundle bundle) {
        MineContactManagerFragment fragment = new MineContactManagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID, "");
        contactType = getArguments().getInt("contactType", 0);
        shareToContactBinding = FragmentMineShareToContactBinding.inflate(inflater, container, false);
        shareToContactBinding.rcyMineShareToContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        ButterKnife.bind(this, shareToContactBinding.customToolbar);
        shareToContactBinding.setEmpty(empty);
        shareFriendItemItemAdapter = new FastItemAdapter<>();
        shareFriendItemItemAdapter.withMultiSelect(false);
        shareFriendItemItemAdapter.withSelectable(false);
        shareFriendItemItemAdapter.withEventHook(new ClickEventHook<ShareContactItem>() {
            @Override
            public void onClick(View v, int position, FastAdapter<ShareContactItem> fastAdapter, ShareContactItem item) {
                AppLogger.d("点击了联系人");
                Account account = BaseApplication.getAppComponent().getSourceManager().getAccount();
                if (account != null && TextUtils.equals(account.getAccount(), item.getAccount())) {
                    ToastUtil.showToast(contactType == 0 ? getString(R.string.Tap3_ShareDevice_NotYourself) : getString(R.string.Tap3_FriendsAdd_NotYourself));
                    return;
                }
                if (contactType == 0) {//分享联系人
                    showShareDeviceDialog(item);
                } else if (contactType == 1) {//添加联系人
                    //不能添加自己为好友
//                        showPersonOverDialog(getString(R.string.Tap3_FriendsAdd_NotYourself));
                    presenter.checkFriendAccount(item);
                }
            }

            @Nullable
            @Override
            public View onBind(@NonNull RecyclerView.ViewHolder viewHolder) {
                return viewHolder.itemView.findViewById(R.id.tv_contactshare);
            }
        });
        shareFriendItemItemAdapter.getItemFilter().withFilterPredicate((item, constraint) -> !TextUtils.isEmpty(constraint)
                && !("" + item.name + item.phone + item.email).toUpperCase().contains(constraint.toString().toUpperCase()));

        shareFriendItemItemAdapter.getItemFilter().withItemFilterListener(new ItemFilterListener<ShareContactItem>() {
            @Override
            public void itemsFiltered(@Nullable CharSequence constraint, @Nullable List<ShareContactItem> results) {
                empty.set(shareFriendItemItemAdapter.getItemCount() == 0);
            }

            @Override
            public void onReset() {

            }
        });
        shareToContactBinding.rcyMineShareToContactList.setAdapter(shareFriendItemItemAdapter);
        shareToContactBinding.customToolbar.setOnKeyListener(this::keyEventListener);
        modifySearchMode(searchMode = contactType == 1, contactType == 1);
        initPresenter();
        return shareToContactBinding.getRoot();
    }

    private boolean keyEventListener(View view, int keyCode, KeyEvent keyEvent) {
        boolean ret = keyCode == KeyEvent.KEYCODE_BACK && searchMode;
        modifySearchMode(searchMode = false, contactType == 1);
        return ret;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.checkAndInitContactList(contactType);
        }
    }

    /**
     * desc；监听搜索输入的变化
     */
    @OnTextChanged(R.id.et_search_contact)
    public void initEditListener(CharSequence s, int start, int before, int count) {
        shareFriendItemItemAdapter.filter(s.toString().trim());
    }

    private void initPresenter() {
        presenter = new MineShareToContactPresenterImp(this);
    }

    @OnClick({R.id.iv_mine_share_to_contact_back, R.id.iv_mine_share_to_contact_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_share_to_contact_back:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_share_to_contact_search:
                modifySearchMode(searchMode = true, contactType == 1);
                break;
        }
    }

    private void modifySearchMode(boolean enter, boolean hideSearchIcon) {
        searchContact.setVisibility(hideSearchIcon ? View.GONE : View.VISIBLE);
        searchBarTitle.setVisibility(enter ? View.GONE : View.VISIBLE);
        searchInput.setVisibility(enter ? View.VISIBLE : View.GONE);
        if (enter) {
            searchInput.requestFocus();
            searchInput.setFocusable(true);
            searchInput.setFocusableInTouchMode(true);
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(searchInput, InputMethodManager.SHOW_FORCED);
        } else {
            IMEUtils.hide(getActivity());
        }
    }

    @Override
    public void onInitContactFriends(List<ShareContactItem> friendItems) {
        shareFriendItemItemAdapter.set(friendItems);
        shareFriendItemItemAdapter.filter(null);
    }

    public void showShareDeviceDialog(ShareContactItem shareContactItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.Tap3_ShareDevice))
                .setPositiveButton(getString(R.string.Button_Sure), (dialog, which) -> {
                    dialog.dismiss();
                    presenter.shareDeviceToContact(shareContactItem);
                });

        builder.setNegativeButton(getString(R.string.CANCEL), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public void showPersonOverDialog(String content) {
        ToastUtil.showToast(content);
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
    public void onCheckFriendAccountResult(FriendContextItem friendContextItem, ShareContactItem shareContactItem, boolean accountExist) {
        if (friendContextItem == null && accountExist) {//超时了
            // TODO: 2017/7/3 超时处理
        } else if (accountExist) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("friendItem", friendContextItem);
            MineAddFromContactFragment mineAddFromContactFragment = MineAddFromContactFragment.newInstance(bundle);
            ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), mineAddFromContactFragment, android.R.id.content, MineFriendInformationFragment.class.getSimpleName());
        } else {
            invitationFriend(shareContactItem);
        }
    }

    @Override
    public void onShareDeviceResult(ShareContactItem shareContactItem, RxEvent.ShareDeviceCallBack result) {
        if (result == null) {
            // TODO: 2017/7/3 超时了
        } else if (result.requestId == JError.ErrorOK) { //分享成功
            ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_SuccessTips));
        } else if (result.requestId == JError.ErrorShareExceedsLimit) {  //已注册 未分享但人数达到5人
            showPersonOverDialog(getString(R.string.SHARE_ERR));
        } else if (result.requestId == JError.ErrorShareAlready) { //已注册 已分享
            showPersonOverDialog(getString(R.string.RET_ESHARE_REPEAT));
        } else if (result.requestId == JError.ErrorShareInvalidAccount) {  //未注册
            invitationFriend(shareContactItem);
        } else if (result.requestId == JError.ErrorShareToSelf) {   //不能分享给自己
            showPersonOverDialog(getString(R.string.RET_ESHARE_NOT_YOURSELF));

        }

    }

    private void invitationFriend(ShareContactItem friendContextItem) {
        if (JConstant.EMAIL_REG.matcher(friendContextItem.getAccount()).find()) {
            sendEmail(friendContextItem);
        } else {
            MineContactManagerFragmentPermissionsDispatcher.sendSmsWithPermissionWithCheck(this, friendContextItem);
        }
    }

    @NeedsPermission(Manifest.permission.SEND_SMS)
    public void sendSmsWithPermission(ShareContactItem friendContextItem) {
        if (PermissionUtils.hasSelfPermissions(getContext(), Manifest.permission.SEND_SMS)) {//双重检查
            Uri smsToUri = Uri.parse("smsto:" + friendContextItem.phone);
            Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
            mIntent.putExtra("sms_body", LinkManager.getSmsContent());
            startActivity(mIntent);
        } else {
            showSetPermissionDialog();
        }
    }

    @OnNeverAskAgain(Manifest.permission.SEND_SMS)
    @OnPermissionDenied(Manifest.permission.SEND_SMS)
    public void showSetPermissionDialog() {
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.permission_auth, getString(R.string.SMS)))
                .setNegativeButton(getString(R.string.CANCEL), null)
                .setPositiveButton(getString(R.string.SETTINGS), (DialogInterface dialog, int which) -> {
                    openSetting();
                })
                .create()
                .show();
    }

    private void sendEmail(ShareContactItem friendContextItem) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{friendContextItem.email});
        intent.putExtra(Intent.EXTRA_CC, friendContextItem.email); // 抄送人
        intent.putExtra(Intent.EXTRA_TEXT, LinkManager.getSmsContent()); // 正文
        startActivity(Intent.createChooser(intent, getString(R.string.Mail_Class_Application)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MineContactManagerFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void openSetting() {
        Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        settingIntent.setData(Uri.parse("package:" + getContext().getPackageName()));
        startActivity(settingIntent);
    }
}
