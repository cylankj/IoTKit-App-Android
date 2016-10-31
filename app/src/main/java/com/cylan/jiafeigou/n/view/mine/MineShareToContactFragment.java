package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareToContactPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.n.view.adapter.ShareToContactAdapter;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    @BindView(R.id.pro_share_hint)
    ProgressBar proShareHint;
    @BindView(R.id.tv_share_hint)
    TextView tvShareHint;
    @BindView(R.id.rl_share_pro_hint)
    RelativeLayout rlShareProHint;

    private MineShareToContactContract.Presenter presenter;
    private ShareToContactAdapter shareToContactAdapter;

    public static MineShareToContactFragment newInstance() {
        return new MineShareToContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frgment_mine_share_to_contact, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
        }
        initEditListener();
    }

    /**
     * desc；监听搜索输入的变化
     */
    private void initEditListener() {

        etSearchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                presenter.handleSearchResult(s.toString().trim());
            }
        });
    }

    private void initPresenter() {
        presenter = new MineShareToContactPresenterImp(this);
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
    public void initContactReclyView(ArrayList<SuggestionChatInfoBean> list) {
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
    public void showShareDeviceDialog(final SuggestionChatInfoBean bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("分享设备");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                presenter.shareToContact(bean);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void showShareingProHint() {
        rlShareProHint.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideShareingProHint() {
        rlShareProHint.setVisibility(View.INVISIBLE);
    }

    @Override
    public void changeShareingProHint(String finish) {
        tvShareHint.setText(finish);
    }

    @Override
    public void showPersonOverDialog(String content) {
        ToastUtil.showToast(content);
    }

    @Override
    public void startSendMesgActivity(SuggestionChatInfoBean info) {
        Uri smsToUri = Uri.parse("smsto:"+info.getContent());
        Intent mIntent = new Intent(Intent.ACTION_SENDTO, smsToUri );
        mIntent.putExtra("sms_body", "邀请你成为我的好友，点击XXXXXXXXX下载安装【加菲狗】");
        startActivity( mIntent );
    }

    /**
     * 点击分享按钮
     *
     * @param item
     */
    @Override
    public void isShare(SuggestionChatInfoBean item) {
        if (presenter != null) {
            presenter.handlerShareClick(item);
        }
    }
}
