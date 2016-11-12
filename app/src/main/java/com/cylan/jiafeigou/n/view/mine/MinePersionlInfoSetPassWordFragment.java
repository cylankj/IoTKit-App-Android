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
import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersonalInfoSetPassWordContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MinePersionlInfoSetPassWordPresenterImp;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/20
 * 描述：
 */
public class MinePersionlInfoSetPassWordFragment extends Fragment implements MinePersonalInfoSetPassWordContract.View {

    @BindView(R.id.iv_mine_personal_setpassword_back)
    ImageView ivMinePersonalSetpasswordBack;
    @BindView(R.id.iv_mine_personal_setpassword_bind)
    ImageView ivMinePersonalSetpasswordBind;
    @BindView(R.id.et_mine_personal_information_old_password)
    EditText etMinePersonalInformationOldPassword;
    @BindView(R.id.view_mine_personal_information_old_password_line)
    View viewMinePersonalInformationOldPasswordLine;
    @BindView(R.id.iv_mine_personal_information_old_password_clear)
    ImageView ivMinePersonalInformationOldPasswordClear;
    @BindView(R.id.et_mine_personal_information_new_password)
    EditText etMinePersonalInformationNewPassword;
    @BindView(R.id.view_mine_personal_information_new_password_line)
    View viewMinePersonalInformationNewPasswordLine;
    @BindView(R.id.iv_mine_personal_information_new_password_clear)
    ImageView ivMinePersonalInformationNewPasswordClear;


    private MinePersonalInfoSetPassWordContract.Presenter presenter;

    public static MinePersionlInfoSetPassWordFragment newInstance(Bundle bundle) {
        MinePersionlInfoSetPassWordFragment fragment = new MinePersionlInfoSetPassWordFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_persion_info_set_password, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initEditLisenter();
        return view;
    }

    private void initPresenter() {
        presenter = new MinePersionlInfoSetPassWordPresenterImp(this);
    }

    private void initEditLisenter() {

        etMinePersonalInformationOldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = TextUtils.isEmpty(s);
                if (isEmpty || TextUtils.isEmpty(getNewPassword())) {
                    ivMinePersonalSetpasswordBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
                    ivMinePersonalSetpasswordBind.setEnabled(false);
                    ivMinePersonalSetpasswordBind.setClickable(false);
                } else {
                    ivMinePersonalSetpasswordBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
                    ivMinePersonalSetpasswordBind.setEnabled(true);
                    ivMinePersonalSetpasswordBind.setClickable(true);
                }
                ivMinePersonalInformationOldPasswordClear.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                viewMinePersonalInformationOldPasswordLine.setBackgroundColor(isEmpty ? getResources().getColor(R.color.color_f2f2f2) : getResources().getColor(R.color.color_36bdff));

            }
        });

        etMinePersonalInformationNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isEmpty = TextUtils.isEmpty(s);
                if (isEmpty || TextUtils.isEmpty(getOldPassword())) {
                    ivMinePersonalSetpasswordBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
                    ivMinePersonalSetpasswordBind.setClickable(false);
                    ivMinePersonalSetpasswordBind.setEnabled(false);
                } else {
                    ivMinePersonalSetpasswordBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
                    ivMinePersonalSetpasswordBind.setClickable(true);
                    ivMinePersonalSetpasswordBind.setEnabled(true);
                }
                ivMinePersonalInformationNewPasswordClear.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                viewMinePersonalInformationNewPasswordLine.setBackgroundColor(isEmpty ? getResources().getColor(R.color.color_f2f2f2) : getResources().getColor(R.color.color_36bdff));

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(getNewPassword()) || TextUtils.isEmpty(getOldPassword())) {
            ivMinePersonalSetpasswordBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
        } else {
            ivMinePersonalSetpasswordBind.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
        }
    }

    @Override
    public void setPresenter(MinePersonalInfoSetPassWordContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_mine_personal_setpassword_back, R.id.iv_mine_personal_information_old_password_clear,
            R.id.iv_mine_personal_information_new_password_clear, R.id.iv_mine_personal_setpassword_bind})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_personal_setpassword_back:
                getFragmentManager().popBackStack();
                break;

            case R.id.iv_mine_personal_information_old_password_clear:
                etMinePersonalInformationOldPassword.setText("");
                break;

            case R.id.iv_mine_personal_information_new_password_clear:
                etMinePersonalInformationNewPassword.setText("");
                break;

            case R.id.iv_mine_personal_setpassword_bind:
                saveNewPassword();
                break;
        }
    }

    /**
     * desc：保存新密码
     */
    private void saveNewPassword() {
        if (!presenter.checkOldPassword(getOldPassword())) {
            ToastUtil.showToast("原密码错误");
            return;
        }

        if (presenter.checkNewPassword(getOldPassword(), getNewPassword())) {
            ToastUtil.showToast("新密码与原密码相同");
            return;
        }

        if (presenter.checkNewPasswordLength(getNewPassword())) {
            ToastUtil.showToast("请输入6~12位密码");
            return;
        }

        ToastUtil.showToast("修改成功");
        getFragmentManager().popBackStack();
    }

    @Override
    public String getOldPassword() {
        return etMinePersonalInformationOldPassword.getText().toString();
    }

    @Override
    public String getNewPassword() {
        return etMinePersonalInformationNewPassword.getText().toString();
    }
}
