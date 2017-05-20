package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareToContactPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.ShareToContactAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
    public void initEditListener(CharSequence s, int start, int before, int count) {
        presenter.handlerSearchResult(s.toString().trim());
    }

    private void initPresenter() {
        presenter = new MineShareToContactPresenterImp(this, hasSharefriend);
    }

    @Override
    public void setPresenter(MineShareToContactContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
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
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearchContact.getWindowToken(), 0);
    }

    @Override
    public void initContactReclyView(ArrayList<RelAndFriendBean> list) {
        rcyMineShareToContactList.setVisibility(View.VISIBLE);
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
        rcyMineShareToContactList.setVisibility(View.INVISIBLE);
        llNoContact.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoContactNullView() {
        llNoContact.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideTopTitle() {
        tvTopTitle.setVisibility(View.GONE);
    }

    @Override
    public void showSearchInputEdit() {
        etSearchContact.setVisibility(View.VISIBLE);
        etSearchContact.requestFocus();
        etSearchContact.setFocusable(true);
        etSearchContact.setFocusableInTouchMode(true);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etSearchContact, InputMethodManager.SHOW_FORCED);
        //TODO 弹出键盘
    }

    @Override
    public void hideSearchInputEdit() {
        etSearchContact.setVisibility(View.GONE);
    }

    @Override
    public void showShareDeviceDialog(final String account) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());//不能用 manager 里的,存在缓存复用,导致 account 不是最新的
        builder.setTitle(getString(R.string.Tap3_ShareDevice));
        builder.setPositiveButton(getString(R.string.Button_Sure), (dialog, which) -> {
            showShareingProHint();
            changeShareingProHint("loading");
            if (getView() != null && presenter != null) {
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AppLogger.e("分享给:" + account);
                        dialog.dismiss();
                        presenter.handlerShareClick(deviceinfo.uuid, account);
                    }
                }, 2000);
            }
        });

        builder.setNegativeButton(getString(R.string.CANCEL), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void showShareingProHint() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
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
        Uri smsToUri = Uri.parse("smsto:" + account);
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        mIntent.putExtra("sms_body", getString(R.string.Tap1_share_tips, JConstant.EFAMILY_URL_PREFIX, ContextUtils.getContext().getPackageName()));
        startActivity(mIntent);
    }

    /**
     * 分享结果的处理
     *
     * @param requtestId
     */
    @Override
    public void handlerCheckRegister(int requtestId, String item) {
        switch (requtestId) {
            case JError.ErrorOK:                                           //分享成功
                ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_SuccessTips));
                break;

            case JError.ErrorShareExceedsLimit:                             //已注册 未分享但人数达到5人
                if (getView() != null) {
                    showPersonOverDialog(getString(R.string.SHARE_ERR));
                }
                break;

            case JError.ErrorShareAlready:                                    //已注册 已分享
                if (getView() != null) {
                    showPersonOverDialog(getString(R.string.RET_ESHARE_REPEAT));
                }
                break;
            case JError.ErrorShareInvalidAccount:                             //未注册
                if (JConstant.EMAIL_REG.matcher(contractPhone).find()) {
                    sendEmail();
                    return;
                }
                if (presenter.checkSendSmsPermission()) {
                    startSendMesgActivity(contractPhone);
                } else {
                    MineShareToContactFragment.this.requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
                }
                break;

            case JError.ErrorShareToSelf:                                     //不能分享给自己
                if (getView() != null) {
                    showPersonOverDialog(getString(R.string.RET_ESHARE_NOT_YOURSELF));
                }
                break;
        }
    }

    private void sendEmail() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{contractPhone});
        intent.putExtra(Intent.EXTRA_CC, contractPhone); // 抄送人
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.Tap1_share_tips, JConstant.EFAMILY_URL_PREFIX, ContextUtils.getContext().getPackageName())); // 正文
        startActivity(Intent.createChooser(intent, getString(R.string.Mail_Class_Application)));
    }

    /**
     * 点击分享按钮
     *
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
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSendMesgActivity(contractPhone);
            } else {
                setPermissionDialog(getString(R.string.Tap3_ShareDevice_Contacts));
            }
        }
    }


    public void setPermissionDialog(String permission) {
        new android.app.AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.permission_auth, permission))
                .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.SETTINGS), (DialogInterface dialog, int which) -> {
                    openSetting();
                })
                .create()
                .show();
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
