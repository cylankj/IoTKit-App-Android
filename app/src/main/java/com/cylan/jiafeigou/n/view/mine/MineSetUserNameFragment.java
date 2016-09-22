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
        initEditText();
        initEditListener();
        return view;
    }

    private void initEditListener() {
        etMinePersonalInformationNewName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = TextUtils.isEmpty(getEditName());
                if(isEmpty){
                    ivMinePersonalSetnameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
                    ivMinePersonalSetnameBind.setEnabled(false);
                }else {
                    ivMinePersonalSetnameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
                    ivMinePersonalSetnameBind.setEnabled(true);
                }

            }
        });
    }

    private void initEditText() {
        etMinePersonalInformationNewName.setText(PreferencesUtils.getString(getActivity(), "username", ""));
    }

    private void initPresenter() {
        presenter = new MinePersionalInfomationSetNamePresenterImpl();
    }

    @Override
    public String getEditName() {
        return etMinePersonalInformationNewName.getText().toString().trim();
    }

    @Override
    public void setPresenter(MinePersionalInfomationSetNameContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_top_bar_left_back, R.id.iv_mine_personal_setname_bind, R.id.iv_mine_personal_information_new_name_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_top_bar_left_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_personal_setname_bind:
                if (presenter.isEditEmpty(getEditName())) {
                    ToastUtil.showToast(getContext(), "昵称不能为空");
                    return;
                } else {
                    PreferencesUtils.putString(getActivity(), getEditName(), "username");
                    ToastUtil.showToast(getContext(), "保存成功");
                    if (listener != null) {
                        listener.userNameChange(getEditName());
                    }
                    getFragmentManager().popBackStack();
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
        if(TextUtils.isEmpty(getEditName())){
            ivMinePersonalSetnameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
            ivMinePersonalSetnameBind.setEnabled(false);
        }else {
            ivMinePersonalSetnameBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
            ivMinePersonalSetnameBind.setEnabled(true);
        }
    }
}
