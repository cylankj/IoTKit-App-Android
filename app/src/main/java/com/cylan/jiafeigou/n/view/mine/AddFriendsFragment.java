package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.AddFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.AddFriendsContactImp;
import com.cylan.jiafeigou.n.view.adapter.FriendAddFromContactAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class AddFriendsFragment extends IBaseFragment<AddFriendContract.Presenter> implements AddFriendContract.View, FriendAddFromContactAdapter.onContactItemClickListener {

    @BindView(R.id.iv_home_mine_friends_add_from_contact_back)
    ImageView ivHomeMineRelativesandfriendsAddFromContactBack;
    @BindView(R.id.et_add_phone_number)
    EditText etAddPhoneNumber;
    @BindView(R.id.rcy_contact_list)
    RecyclerView rcyContactList;
    @BindView(R.id.ll_no_contact)
    LinearLayout llNoContact;


    private FriendAddFromContactAdapter contactListAdapter;
    private String friendAccount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPresenter();
    }

    public static AddFriendsFragment newInstance() {
        return new AddFriendsFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (basePresenter != null) {
            basePresenter.getFriendListData();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_friend_add_from_contact, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        ViewUtils.setViewPaddingStatusBar(rlHomeMineRelativesandfriendsScanAdd);
//        ViewUtils.setChineseExclude(etAddPhoneNumber, 65);
    }

    @OnTextChanged(R.id.et_add_phone_number)
    public void initEditTextListenter(CharSequence s, int start, int before, int count) {
        basePresenter.filterPhoneData(s.toString());
    }

    private void initPresenter() {
        basePresenter = new AddFriendsContactImp(this);
    }


    @Override
    public String getUuid() {
        return null;
    }

    @OnClick(R.id.iv_home_mine_friends_add_from_contact_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_friends_add_from_contact_back:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void initContactRecycleView(ArrayList<RelAndFriendBean> list) {
        rcyContactList.setVisibility(View.VISIBLE);
        rcyContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        contactListAdapter = new FriendAddFromContactAdapter(getView().getContext(), list, null);
        rcyContactList.setAdapter(contactListAdapter);
        initAdaListener();
    }

    /**
     * 设置列表监听
     */
    private void initAdaListener() {
        contactListAdapter.setOnContactItemClickListener(this);
        contactListAdapter.setOnItemClickListener((itemView, viewType, position) -> {
            //跳转到联系人的详情界面去(不用)
        });
    }

    @Override
    public void jump2SendAddMesgFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("account", friendAccount);
        MineAddFromContactFragment mineAddFromContactFragment = MineAddFromContactFragment.newInstance(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineAddFromContactFragment, "mineAddFromContactFragment")
                .addToBackStack("AddFlowStack")
                .commit();
    }

    /**
     * desc：显示空视图
     */
    @Override
    public void showNoContactView() {
        rcyContactList.setVisibility(View.INVISIBLE);
        llNoContact.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoContactView() {
        llNoContact.setVisibility(View.INVISIBLE);
    }

    /**
     * 显示进度浮层
     */
    @Override
    public void showLoadingPro() {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.getting));
    }

    /**
     * 隐藏进度浮层
     */
    @Override
    public void hideLoadingPro() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == 0) {
            hideLoadingPro();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    /**
     * 发送短信邀请
     */
    @Override
    public void openSendSms() {
        if (JConstant.EMAIL_REG.matcher(friendAccount).find()) {
            sendEmail();
            return;
        }
        if (basePresenter.checkSmsPermission()) {
            sendSms();
        } else {
            //申请权限
            AddFriendsFragment.this.requestPermissions(
                    new String[]{Manifest.permission.SEND_SMS},
                    1);
        }
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{friendAccount});
        intent.putExtra(Intent.EXTRA_CC, friendAccount); // 抄送人
        intent.putExtra(Intent.EXTRA_TEXT, LinkManager.getSmsContent());
        startActivity(Intent.createChooser(intent, getString(R.string.Mail_Class_Application)));
    }

    /**
     * 发送短信
     */
    private void sendSms() {
        Uri smsToUri = Uri.parse("smsto:" + friendAccount);
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        mIntent.putExtra("sms_body", LinkManager.getSmsContent());
        startActivity(mIntent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (basePresenter != null) {
            basePresenter.stop();
        }
    }

    @Override
    public void onAddClick(View view, int position, final RelAndFriendBean item) {
        friendAccount = item.account;
        if (getView() != null && basePresenter != null) {
            showLoadingPro();
            getView().postDelayed(() -> basePresenter.checkFriendAccount(item.account), 2000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSms();
            } else {
                setPermissionDialog(getString(R.string.SMS));
            }
        }
    }

    public void setPermissionDialog(String permission) {
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivity());
        builder.setMessage(getString(R.string.permission_auth, permission))
                .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.SETTINGS), (DialogInterface dialog, int which) -> {
                    openSetting();
                });
        AlertDialogManager.getInstance().showDialog("setPermissionDialog", getActivity(), builder);
    }

    private void openSetting() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getContext().getPackageName());
        }
        startActivity(localIntent);
    }
}
