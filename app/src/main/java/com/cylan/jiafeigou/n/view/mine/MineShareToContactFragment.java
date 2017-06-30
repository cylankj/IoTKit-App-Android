package com.cylan.jiafeigou.n.view.mine;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.cylan.jiafeigou.databinding.FragmentMineShareToContactBinding;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareToContactPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.ShareContactItem;
import com.cylan.jiafeigou.support.log.AppLogger;
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

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToContactFragment extends Fragment implements MineShareToContactContract.View {
    private MineShareToContactContract.Presenter presenter;
    private FastItemAdapter<ShareContactItem> shareFriendItemItemAdapter;
    private FragmentMineShareToContactBinding shareToContactBinding;
    private String uuid;
    private ObservableBoolean empty = new ObservableBoolean(false);
    private ShareContactItem currentShareContact;
    private ObservableBoolean searchMode = new ObservableBoolean(false) {

    };
    //Custom Toolbar Views
    @BindView(R.id.iv_mine_share_to_contact_search)
    ImageView searchContact;
    @BindView(R.id.tv_top_title)
    TextView searchBarTitle;
    @BindView(R.id.et_search_contact)
    EditText searchInput;

    public static MineShareToContactFragment newInstance(Bundle bundle) {
        MineShareToContactFragment fragment = new MineShareToContactFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID, "");
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
                showShareDeviceDialog(item);
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
        searchMode.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                modifySearchMode(((ObservableBoolean) sender).get());
            }
        });
        initPresenter();
        return shareToContactBinding.getRoot();
    }

    private boolean keyEventListener(View view, int keyCode, KeyEvent keyEvent) {
        boolean ret = keyCode == KeyEvent.KEYCODE_BACK && searchMode.get();
        searchMode.set(false);
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
            presenter.start();
            presenter.checkAndInitContactList();
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

    @Override
    public void setPresenter(MineShareToContactContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @OnClick({R.id.iv_mine_share_to_contact_back, R.id.iv_mine_share_to_contact_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_share_to_contact_back:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_share_to_contact_search:
                searchMode.set(true);
                break;
        }
    }

    private void modifySearchMode(boolean enter) {
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
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
        IMEUtils.hide(getActivity());
    }

    @Override
    public void onInitContactFriends(List<ShareContactItem> friendItems) {
        shareFriendItemItemAdapter.set(friendItems);
        shareFriendItemItemAdapter.filter(null);
    }

    public void showShareDeviceDialog(ShareContactItem shareContactItem) {
        currentShareContact = shareContactItem;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.Tap3_ShareDevice))
                .setPositiveButton(getString(R.string.Button_Sure), (dialog, which) -> {
                    dialog.dismiss();
                    presenter.shareDeviceToContact(shareContactItem);
                });

        builder.setNegativeButton(getString(R.string.CANCEL), (dialog, which) -> dialog.dismiss());
        builder.show();
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
        mIntent.putExtra("sms_body", LinkManager.getSmsContent());
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
//                if (JConstant.EMAIL_REG.matcher(contractPhone).find()) {
//                    sendEmail();
//                    return;
//                }
                if (presenter.checkSendSmsPermission()) {
//                    startSendMesgActivity(contractPhone);
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

    @Override
    public void showLoading(int resId, Object... args) {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(resId, args));
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    private void sendEmail() {
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("message/rfc822");
//        intent.putExtra(Intent.EXTRA_EMAIL,
//                new String[]{contractPhone});
//        intent.putExtra(Intent.EXTRA_CC, contractPhone); // 抄送人
//        intent.putExtra(Intent.EXTRA_TEXT, LinkManager.getSmsContent()); // 正文
//        startActivity(Intent.createChooser(intent, getString(R.string.Mail_Class_Application)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startSendMesgActivity(contractPhone);
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
        Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        settingIntent.setData(Uri.parse("package:" + getContext().getPackageName()));
        startActivity(settingIntent);
    }
}
