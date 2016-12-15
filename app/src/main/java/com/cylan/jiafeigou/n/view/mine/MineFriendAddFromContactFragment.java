package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendAddFromContactPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.FriendAddFromContactAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.superadapter.OnItemClickListener;

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
public class MineFriendAddFromContactFragment extends Fragment implements MineFriendAddFromContactContract.View, FriendAddFromContactAdapter.onContactItemClickListener {

    @BindView(R.id.iv_home_mine_relativesandfriends_add_from_contact_back)
    ImageView ivHomeMineRelativesandfriendsAddFromContactBack;
    @BindView(R.id.et_add_phone_number)
    EditText etAddPhoneNumber;
    @BindView(R.id.rcy_contact_list)
    RecyclerView rcyContactList;
    @BindView(R.id.ll_no_contact)
    LinearLayout llNoContact;

    private MineFriendAddFromContactContract.Presenter presenter;
    private MineAddFromContactFragment mineAddFromContactFragment;
    private FriendAddFromContactAdapter contactListAdapter;

    private String friendAccount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static MineFriendAddFromContactFragment newInstance() {
        return new MineFriendAddFromContactFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.getFriendListData();
            presenter.start();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_friend_add_from_contact, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }
    @OnTextChanged(R.id.et_add_phone_number)
    public void initEditTextListenter(CharSequence s, int start, int before, int count) {
        presenter.filterPhoneData(s.toString());
    }

    private void initPresenter() {
        presenter = new MineFriendAddFromContactPresenterImp(this);
    }

    @Override
    public void setPresenter(MineFriendAddFromContactContract.Presenter presenter) {

    }

    @OnClick(R.id.iv_home_mine_relativesandfriends_add_from_contact_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_relativesandfriends_add_from_contact_back:
                getFragmentManager().popBackStack();
                break;
        }
    }

    @Override
    public void initContactRecycleView(ArrayList<RelAndFriendBean> list) {
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
        contactListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int viewType, int position) {
                //跳转到联系人的详情界面去
            }
        });
    }

    @Override
    public void jump2SendAddMesgFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("account", friendAccount);
        mineAddFromContactFragment = MineAddFromContactFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineAddFromContactFragment, "mineAddFromContactFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * desc：显示空视图
     */
    @Override
    public void showNoContactView() {
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
        LoadingDialog.showLoading(getFragmentManager(),getString(R.string.getting));
    }

    /**
     * 隐藏进度浮层
     */
    @Override
    public void hideLoadingPro() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    /**
     * 发送短信邀请
     */
    @Override
    public void openSendSms() {
        if (presenter.checkSmsPermission()){
            sendSms();
        }else {
            //申请权限
            MineFriendAddFromContactFragment.this.requestPermissions(
                    new String[]{Manifest.permission.SEND_SMS},
                    1);
        }
    }

    /**
     * 发送短信
     */
    private void sendSms() {
        Uri smsToUri = Uri.parse("smsto:" + friendAccount);
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        mIntent.putExtra("sms_body", getString(R.string.Tap1_share_tips));
        startActivity(mIntent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    @Override
    public void onAddClick(View view, int position, final RelAndFriendBean item) {
        friendAccount = item.account;
        if (getView() != null && presenter != null) {
            showLoadingPro();
            getView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    presenter.checkFriendAccount(item.account);
                }
            },2000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendSms();
            } else {
                ToastUtil.showNegativeToast(getString(R.string.Tap0_Authorizationfailed));
            }
        }
    }
}
