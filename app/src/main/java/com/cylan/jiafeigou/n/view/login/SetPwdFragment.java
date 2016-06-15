package com.cylan.jiafeigou.n.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.NewHomeActivity;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.superlog.SLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by lxh on 16-6-8.
 */

public class SetPwdFragment extends LoginModelFragment {

    @BindView(R.id.et_login_pwd)
    EditText etLoginPwd;
    @BindView(R.id.iv_login_clear_pwd)
    ImageView ivLoginClearPwd;
    @BindView(R.id.cb_show_pwd)
    CheckBox cbShowPwd;
    @BindView(R.id.tv_model_commit)
    TextView tvCommit;
    @BindView(R.id.lLayout_register_input)
    LinearLayout lLayoutRegisterInput;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_set_pwd, container, false);
        ButterKnife.bind(this, view);
        initView(view);
        addOnTouchListener(view);
        editTextLimitMaxInput(etLoginPwd, 12);
        return view;
    }


    public static SetPwdFragment newInstance(Bundle bundle) {
        SetPwdFragment fragment = new SetPwdFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private void initView(View view) {

    }


    /***
     * 账号变化
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */

    @OnTextChanged(R.id.et_login_pwd)
    public void onUserNameChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        setViewEnableStyle(tvCommit, !flag);
        ivLoginClearPwd.setVisibility(flag ? View.GONE : View.VISIBLE);
    }


    @OnClick(R.id.iv_login_clear_pwd)
    public void clearUserName(View view) {
        etLoginPwd.getText().clear();
    }


    @OnClick(R.id.tv_model_commit)
    public void registerCommit(View view) {
        String pwd = etLoginPwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd) || pwd.length() < 6) {
            ToastUtil.showToast(getContext(), "请输入6-12位密码");
            return;
        }
        getActivity().startActivity(new Intent(getContext(), NewHomeActivity.class));
        getActivity().finish();
    }

    /**
     * 明文/密文 密码
     *
     * @param buttonView
     * @param isChecked
     */
    @OnCheckedChanged(R.id.cb_show_pwd)
    public void onShowPwd(CompoundButton buttonView, boolean isChecked) {
        showPwd(etLoginPwd, isChecked);
        etLoginPwd.setSelection(etLoginPwd.length());
    }

}
