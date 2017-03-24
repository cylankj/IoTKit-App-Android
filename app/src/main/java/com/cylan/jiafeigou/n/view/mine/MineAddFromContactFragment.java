package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineAddFromContactPresenterImp;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/7
 * 描述：
 */
public class MineAddFromContactFragment extends Fragment implements MineAddFromContactContract.View {

    @BindView(R.id.et_mine_add_contact_mesg)
    EditText etMineAddContactMesg;

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
        etMineAddContactMesg.setText(String.format(getString(R.string.Tap3_FriendsAdd_StuffContents), alids));
    }

    @Override
    public String getSendMesg() {
        String mesg = etMineAddContactMesg.getText().toString();
        if (TextUtils.isEmpty(mesg)) {
            return String.format(getString(R.string.Tap3_FriendsAdd_StuffContents), presenter.getUserAlias());
        } else {
            return mesg;
        }
    }

    @Override
    public void showResultDialog(RxEvent.CheckAccountCallback callback) {
        if (callback.i == JError.ErrorFriendAlready | callback.b) {
            ToastUtil.showToast(getString(R.string.Tap3_Added));
            getFragmentManager().popBackStack();
        } else if (callback.i == JError.ErrorFriendToSelf) {
            ToastUtil.showToast(getString(R.string.Tap3_FriendsAdd_NotYourself));
        } else {
            presenter.sendRequest(contactItem, getSendMesg());
        }
    }

    @Override
    public void showSendReqHint() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.submiting));
    }

    @Override
    public void hideSendReqHint() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideSendReqHint();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    /**
     * 发送添加请求的结果
     *
     * @param code
     */
    @Override
    public void sendReqBack(int code) {
        if (code == JError.ErrorOK) {
            ToastUtil.showToast(getString(R.string.Tap3_FriendsAdd_Contacts_InvitedTips));
            getFragmentManager().popBackStack();
        } else {
            ToastUtil.showNegativeToast("请求发送失败");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    @OnClick({R.id.tv_toolbar_right, R.id.tv_toolbar_icon})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_right:
                presenter.checkAccount(contactItem);
                break;
            case R.id.tv_toolbar_icon:
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
