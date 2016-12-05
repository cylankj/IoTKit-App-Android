package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineAddFromContactPresenterImp;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineAddFromContactFragment extends Fragment implements MineAddFromContactContract.View {


    @BindView(R.id.iv_mine_add_from_contact_back)
    ImageView ivMineAddFromContactBack;
    @BindView(R.id.iv_mine_add_from_contact_send)
    ImageView ivMineAddFromContactSend;
    @BindView(R.id.et_mine_add_contact_mesg)
    EditText etMineAddContactMesg;
    @BindView(R.id.rl_send_pro_hint)
    RelativeLayout rlSendProHint;

    private MineAddFromContactContract.Presenter presenter;
    private String contactItem;

    public static MineAddFromContactFragment newInstance(Bundle bundle) {
        MineAddFromContactFragment fragment = new MineAddFromContactFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_add_from_contact, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        getIntentData();
        return view;
    }

    /**
     * desc:获取传递过来的数据
     */
    private void getIntentData() {
        Bundle bundle = getArguments();
        contactItem = bundle.getString("account");
    }

    private void initPresenter() {
        presenter = new MineAddFromContactPresenterImp(this);
    }

    @Override
    public void setPresenter(MineAddFromContactContract.Presenter presenter) {

    }

    @Override
    public void initEditText(String alids) {
        etMineAddContactMesg.setText("我是" + alids);
    }

    @Override
    public String getSendMesg() {
        String mesg = etMineAddContactMesg.getText().toString();
        if (TextUtils.isEmpty(mesg)) {
            return "我是" + presenter.getUserAlias();
        } else {
            return mesg;
        }
    }

    @Override
    public void showResultDialog() {
        ToastUtil.showToast(getString(R.string.Tap3_FriendsAdd_Contacts_InvitedTips));
    }

    @Override
    public void showSendReqHint() {
        rlSendProHint.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSendReqHint() {
        rlSendProHint.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    @OnClick({R.id.iv_mine_add_from_contact_send, R.id.iv_mine_add_from_contact_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_add_from_contact_send:
                showSendReqHint();
                presenter.sendRequest(contactItem, getSendMesg());
                hideSendReqHint();
                showResultDialog();
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_add_from_contact_back:
                getFragmentManager().popBackStack();
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
}
