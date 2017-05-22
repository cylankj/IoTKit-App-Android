package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoSetAliasContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoSetNamePresenterImpl;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
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
public class MineSetUserAliasFragment extends Fragment implements MineInfoSetAliasContract.View {

    @BindView(R.id.et_mine_personal_information_new_name)
    EditText etMinePersonalInformationNewName;
    @BindView(R.id.view_mine_personal_information_new_name_line)
    View viewMinePersonalInformationNewNameLine;
    @BindView(R.id.iv_mine_personal_information_new_name_clear)
    ImageView ivMinePersonalInformationNewNameClear;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;


    private MineInfoSetAliasContract.Presenter presenter;

    private OnSetUsernameListener listener;
    private JFGAccount userinfo;

    public interface OnSetUsernameListener {
        void userNameChange(String name);
    }

    public void setOnSetUsernameListener(OnSetUsernameListener listener) {
        this.listener = listener;
    }

    public static MineSetUserAliasFragment newInstance(Bundle bundle) {
        MineSetUserAliasFragment fragment = new MineSetUserAliasFragment();
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
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
            customToolbar.setTvToolbarRightEnable(false);
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);

            viewMinePersonalInformationNewNameLine.setBackgroundColor(Color.parseColor("#f2f2f2"));
            ivMinePersonalInformationNewNameClear.setVisibility(View.GONE);
        } else {
            customToolbar.setTvToolbarRightEnable(true);
            customToolbar.setTvToolbarRightIcon(R.drawable.me_icon_finish_normal);
            viewMinePersonalInformationNewNameLine.setBackgroundColor(Color.parseColor("#36bdff"));
            ivMinePersonalInformationNewNameClear.setVisibility(View.VISIBLE);
        }
    }

    private void initEditText() {
        etMinePersonalInformationNewName.setText(TextUtils.isEmpty(userinfo.getAlias()) ? "" : userinfo.getAlias());
    }

    private void initPresenter() {
        presenter = new MineInfoSetNamePresenterImpl(this);
    }

    @Override
    public String getEditName() {
        return etMinePersonalInformationNewName.getText().toString().trim().replace(" ", "");
    }

    @Override
    public void showSendHint() {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.LOADING));
    }

    @Override
    public void hideSendHint() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideSendHint();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void handlerResult(RxEvent.AccountArrived accountArrived) {
        if (!TextUtils.isEmpty(getEditName())) {
            hideSendHint();
            if (getEditName().equals(accountArrived.jfgAccount.getAlias())) {
                ToastUtil.showPositiveToast(getString(R.string.PWD_OK_2));
                RxBus.getCacheInstance().post(new RxEvent.LoginMeTab(true));
                getActivity().getSupportFragmentManager().popBackStack();
            } else {
                ToastUtil.showPositiveToast(getString(R.string.SUBMIT_FAIL));
            }
        }
    }

    @Override
    public void setPresenter(MineInfoSetAliasContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.tv_toolbar_right, R.id.iv_mine_personal_information_new_name_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.tv_toolbar_right:
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
            customToolbar.setTvToolbarRightEnable(false);
            customToolbar.setTvToolbarRightIcon(R.drawable.icon_finish_disable);
        } else {
            customToolbar.setTvToolbarRightEnable(true);
            customToolbar.setTvToolbarRightIcon(R.drawable.me_icon_finish_normal);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideSendHint();
        if (presenter != null) presenter.stop();
    }
}
