package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersionalInfomationSetNameContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MinePersionalInfomationSetNamePresenterImpl;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineSetUserNameFragment extends Fragment implements MinePersionalInfomationSetNameContract.View {

    @BindView(R.id.tv_top_bar_center)
    TextView tvTopBarCenter;
    @BindView(R.id.et_input_username)
    EditText etInputUsername;
    @BindView(R.id.tv_save_username)
    TextView tvSaveUsername;
    @BindView(R.id.iv_top_bar_left)
    ImageView ivTopBarLeft;

    private MinePersionalInfomationSetNameContract.Presenter presenter;


    private OnSetUsernameListener listener;

    public interface OnSetUsernameListener {

        void userNameChange(String name);

    }

    public void setOnSetUsernameListener(OnSetUsernameListener listener) {
        this.listener = listener;
    }

    public static MineSetUserNameFragment newInstance() {
        return new MineSetUserNameFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine__set_name, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        setTitleBarName();
        initEditText();
        return view;
    }

    private void initEditText() {
        etInputUsername.setText(PreferencesUtils.getString(getActivity(), "username", ""));
    }

    private void initPresenter() {
        presenter = new MinePersionalInfomationSetNamePresenterImpl();
    }

    @Override
    public String getEditName() {
        return etInputUsername.getText().toString().trim();
    }

    @Override
    public void setTitleBarName() {
        tvTopBarCenter.setText("修改昵称");
    }

    @Override
    public void setPresenter(MinePersionalInfomationSetNameContract.Presenter presenter) {

    }

    @OnClick({R.id.tv_save_username, R.id.iv_top_bar_left})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.tv_save_username:
                if (presenter.isEditEmpty(getEditName())) {
                    ToastUtil.showToast(getContext(), "昵称不能为空");
                    return;
                } else {
                    PreferencesUtils.putString(getActivity(), getEditName(), "username");
                    ToastUtil.showToast(getContext(), "保存成功");
                    listener.userNameChange(getEditName());
                    getFragmentManager().popBackStack();
                }
                break;

            case R.id.iv_top_bar_left:
                getFragmentManager().popBackStack();
                break;
        }
    }
}
