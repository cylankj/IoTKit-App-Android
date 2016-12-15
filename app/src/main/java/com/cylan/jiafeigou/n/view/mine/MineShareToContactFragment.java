package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareToContactPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.ShareToContactAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToContactFragment extends Fragment implements MineShareToContactContract.View, ShareToContactAdapter.onShareLisenter {

    @BindView(R.id.iv_mine_share_to_contact_back)
    ImageView ivMineShareToContactBack;
    @BindView(R.id.iv_mine_share_to_contact_search)
    ImageView ivMineShareToContactSearch;
    @BindView(R.id.rcy_mine_share_to_contact_list)
    RecyclerView rcyMineShareToContactList;
    @BindView(R.id.ll_no_contact)
    LinearLayout llNoContact;
    @BindView(R.id.tv_top_title)
    TextView tvTopTitle;
    @BindView(R.id.et_search_contact)
    EditText etSearchContact;


    private MineShareToContactContract.Presenter presenter;
    private ShareToContactAdapter shareToContactAdapter;
    private DeviceBean deviceinfo;
    private String contractPhone;
    private ArrayList<RelAndFriendBean> hasSharefriend;

    public static MineShareToContactFragment newInstance(Bundle bundle) {
        MineShareToContactFragment fragment = new MineShareToContactFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frgment_mine_share_to_contact, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        initPresenter();
        return view;
    }

    /**
     * 获取到传递过来的参数
     */
    private void getArgumentData() {
        Bundle arguments = getArguments();
        deviceinfo = arguments.getParcelable("deviceinfo");
        hasSharefriend = arguments.getParcelableArrayList("sharefriend");
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
//            presenter.getHasShareContract(deviceinfo.uuid);
            presenter.start();
        }
    }

    /**
     * desc；监听搜索输入的变化
     */
    @OnTextChanged(R.id.et_search_contact)
    public void initEditListener(CharSequence s, int start, int before, int count){
        presenter.handlerSearchResult(s.toString().trim());
    }

    private void initPresenter() {
        presenter = new MineShareToContactPresenterImp(this,hasSharefriend);
    }

    @Override
    public void setPresenter(MineShareToContactContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_mine_share_to_contact_back, R.id.iv_mine_share_to_contact_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_share_to_contact_back:
                getFragmentManager().popBackStack();
                break;

            case R.id.iv_mine_share_to_contact_search:
                hideTopTitle();
                showSearchInputEdit();
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    @Override
    public void initContactReclyView(ArrayList<RelAndFriendBean> list) {
        rcyMineShareToContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        shareToContactAdapter = new ShareToContactAdapter(getView().getContext(), list, null);
        rcyMineShareToContactList.setAdapter(shareToContactAdapter);
        initAdaListener();
    }

    /**
     * 设置列表的监听器
     */
    private void initAdaListener() {
        shareToContactAdapter.setOnShareLisenter(this);
    }

    @Override
    public void showNoContactNullView() {
        llNoContact.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideTopTitle() {
        tvTopTitle.setVisibility(View.GONE);
    }

    @Override
    public void showSearchInputEdit() {
        etSearchContact.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSearchInputEdit() {
        etSearchContact.setVisibility(View.GONE);
    }

    @Override
    public void showShareDeviceDialog(final String account) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.Tap3_ShareDevice));
        builder.setPositiveButton(getString(R.string.Button_Sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showShareingProHint();
                changeShareingProHint("loading");
                if (getView() != null && presenter != null) {
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            presenter.handlerShareClick(deviceinfo.uuid,account);
                        }
                    },2000);
                }
            }
        });

        builder.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void showShareingProHint() {
        LoadingDialog.showLoading(getFragmentManager(),getString(R.string.LOADING));
    }

    @Override
    public void hideShareingProHint() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void changeShareingProHint(String finish) {
    }

    @Override
    public void showPersonOverDialog(String content) {
        ToastUtil.showToast(content);
    }

    @Override
    public void startSendMesgActivity(String account) {
        Uri smsToUri = Uri.parse("smsto:"+account);
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri );
        mIntent.putExtra("sms_body", getString(R.string.Tap1_share_tips));
        startActivity( mIntent );
    }

    /**
     * 分享结果的处理
     * @param requtestId
     */
    @Override
    public void handlerCheckRegister(int requtestId, String item) {
        switch (requtestId){
            case JError.ErrorOK:                                           //分享成功
                ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_SuccessTips));
                break;

            case JError.ErrorShareExceedsLimit:                             //已注册 未分享但人数达到5人
                if (getView() != null){
                    showPersonOverDialog(getString(R.string.SHARE_ERR));
                }
                break;

            case JError.ErrorShareAlready:                                    //已注册 已分享
                if (getView() != null){
                    showPersonOverDialog(getString(R.string.RET_ESHARE_REPEAT));
                }
                break;
            case JError.ErrorShareInvalidAccount:                             //未注册
                if (presenter.checkSendSmsPermission()){
                    startSendMesgActivity(contractPhone);
                }else {
                    MineShareToContactFragment.this.requestPermissions(new String[]{Manifest.permission.SEND_SMS},1);
                }
                break;

            case JError.ErrorShareToSelf:                                     //不能分享给自己
                if (getView() != null){
                    showPersonOverDialog(getString(R.string.RET_ESHARE_NOT_YOURSELF));
                }
                break;
        }
    }

    /**
     * 点击分享按钮
     * @param item
     */
    @Override
    public void isShare(RelAndFriendBean item) {
        contractPhone = item.account;
        showShareDeviceDialog(item.account);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSendMesgActivity(contractPhone);
            } else {
                ToastUtil.showNegativeToast(getString(R.string.Tap0_Authorizationfailed));
            }
        }
    }
}
