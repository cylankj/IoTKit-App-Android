package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetNameContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoSetNamePresenterImpl;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineSetUserNameFragment extends Fragment implements MineInfoSetNameContract.View {

    @BindView(R.id.iv_top_bar_left_back)
    ImageView ivTopBarLeftBack;
    @BindView(R.id.iv_mine_personal_setname_bind)
    ImageView ivMinePersonalSetnameBind;
    @BindView(R.id.et_mine_personal_information_new_name)
    EditText etMinePersonalInformationNewName;
    @BindView(R.id.view_mine_personal_information_new_name_line)
    View viewMinePersonalInformationNewNameLine;
    @BindView(R.id.iv_mine_personal_information_new_name_clear)
    ImageView ivMinePersonalInformationNewNameClear;


    private MineInfoSetNameContract.Presenter presenter;

    private OnSetUsernameListener listener;
    private JFGAccount userinfo;

    public interface OnSetUsernameListener {
        void userNameChange(String name);
    }

    public void setOnSetUsernameListener(OnSetUsernameListener listener) {
        this.listener = listener;
    }

    public static MineSetUserNameFragment newInstance(Bundle bundle) {
        MineSetUserNameFragment fragment = new MineSetUserNameFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine__set_name, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        getArgumentData();
        initEditText();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null)presenter.start();
    }

    /**
     * 获取传递过来的参数
     */
    private void getArgumentData() {
        Bundle arguments = getArguments();
        userinfo = (JFGAccount) arguments.getSerializable("userinfo");
    }

    @OnTextChanged(R.id.et_mine_personal_information_new_name)
    public void onEditChange(CharSequence s, int start, int before, int count) {
        boolean isEmpty = TextUtils.isEmpty(getEditName());
        if (isEmpty) {
            ivMinePersonalSetnameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
            ivMinePersonalSetnameBind.setEnabled(false);
        } else {
            ivMinePersonalSetnameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
            ivMinePersonalSetnameBind.setEnabled(true);
        }
    }

    private void initEditText() {
        etMinePersonalInformationNewName.setText(PreferencesUtils.getString("username", ""));
    }

    private void initPresenter() {
        presenter = new MineInfoSetNamePresenterImpl(this);
    }

    @Override
    public String getEditName() {
        return etMinePersonalInformationNewName.getText().toString().trim();
    }

    @Override
    public void showSendHint() {
        LoadingDialog.showLoading(getFragmentManager(),getString(R.string.LOADING));
    }

    @Override
    public void hideSendHint() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    /**
     * 网络状态变化
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1){
            hideSendHint();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    /**
     * 处理互调的结果
     * @param getUserInfo
     */
    @Override
    public void handlerResult(RxEvent.GetUserInfo getUserInfo) {
        if (!TextUtils.isEmpty(getEditName())) {
            hideSendHint();
            if (getEditName().equals(getUserInfo.jfgAccount.getAlias())) {
                ToastUtil.showPositiveToast(getString(R.string.PWD_OK_2));
                getFragmentManager().popBackStack();
            } else {
                ToastUtil.showPositiveToast(getString(R.string.SUBMIT_FAIL));
            }
        }
    }

    @Override
    public void setPresenter(MineInfoSetNameContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_top_bar_left_back, R.id.iv_mine_personal_setname_bind, R.id.iv_mine_personal_information_new_name_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_top_bar_left_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_personal_setname_bind:
                if (presenter.isEditEmpty(getEditName())) {
                    return;
                } else {
                    if (presenter != null) presenter.saveName(getEditName());
                }
                break;
            case R.id.iv_mine_personal_information_new_name_clear:
                etMinePersonalInformationNewName.setText("");
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(getEditName())) {
            ivMinePersonalSetnameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
            ivMinePersonalSetnameBind.setEnabled(false);
        } else {
            ivMinePersonalSetnameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
            ivMinePersonalSetnameBind.setEnabled(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideSendHint();
        if (presenter != null)presenter.stop();
    }
}
