package com.cylan.jiafeigou.n.view.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.superlog.SLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by lxh on 16-6-8.
 */

public class FindPwdByPhoneFragment extends LoginModelFragment {


    @BindView(R.id.et_register_username)
    EditText etRegisterUsername;
    @BindView(R.id.iv_register_clear_username)
    ImageView ivRegisterClearUsername;
    @BindView(R.id.et_register_code)
    EditText etRegisterCode;
    @BindView(R.id.tv_register_reciprocal_time)
    TextView tvRegisterReciprocalTime;
    @BindView(R.id.lLayout_input_code)
    LinearLayout lLayoutInputCode;
    @BindView(R.id.tv_model_commit)
    TextView tvCommit;
    @BindView(R.id.tv_register_user_agreement)
    TextView tvRegisterUserAgreement;
    @BindView(R.id.lLayout_register_input)
    LinearLayout lLayoutRegisterInput;
    @BindView(R.id.tv_register_switch)
    TextView tvRegisterSwitch;
    @BindView(R.id.rLayout_register)
    RelativeLayout rLayoutRegister;

    private boolean isVerifyTime = false; //验证码有效时间内

    public static FindPwdByPhoneFragment newInstance(Bundle bundle) {
        FindPwdByPhoneFragment fragment = new FindPwdByPhoneFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_by_phone_layout, container, false);
        ButterKnife.bind(this, view);
        addOnTouchListener(view);
        initView(view);
        editTextLimitMaxInput(etRegisterUsername, 11);
        editTextLimitMaxInput(etRegisterCode, 6);
        return view;
    }


    private void initView(View view) {
        tvRegisterSwitch.setVisibility(View.GONE);
        //设置手机号
        etRegisterUsername.setText("13800138000");
        lLayoutInputCode.setVisibility(View.VISIBLE);
        tvCommit.setText("继续");
    }


//    @OnClick(R.id.iv_login_top_left)
//    public void exit(View view) {
//        ForgetPwdFragment fragment = (ForgetPwdFragment) getFragmentManager().findFragmentByTag("forget");
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        if (fragment == null) {
//            fragment = ForgetPwdFragment.newInstance(null);
//            ft.add(R.id.fLayout_login_model_container, fragment, "forget");
//        } else {
//            ft.hide(this).show(fragment);
//        }
//        ft.commit();
//    }


    @OnClick(R.id.tv_model_commit)
    public void regCommit(View view) {
//        SetPwdFragment fragment = SetPwdFragment.newInstance(null);
//        ActivityUtils.addFragmentToActivity(getChildFragmentManager(), fragment, R.id.rLayout_register);
//        新密码（设置密码页？）
    }


    /***
     * 账号变化
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */

    @OnTextChanged(R.id.et_register_username)
    public void onUserNameChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        if (flag) {
            setViewEnableStyle(tvCommit, false);
        } else if (etRegisterCode.getText().toString().trim().length() != 6 || s.length() != 11) {
            setViewEnableStyle(tvCommit, false);
        } else {
            setViewEnableStyle(tvCommit, true);
        }
        ivRegisterClearUsername.setVisibility(flag ? View.GONE : View.VISIBLE);
    }

    @OnTextChanged(R.id.et_register_code)
    public void onCodeChange(CharSequence s, int start, int before, int count) {
        boolean flag = TextUtils.isEmpty(s);
        if (flag) {
            setViewEnableStyle(tvCommit, false);
        } else if (etRegisterUsername.getText().toString().trim().length() != 11 || s.length() != 6) {
            setViewEnableStyle(tvCommit, false);
        } else {
            setViewEnableStyle(tvCommit, true);
        }
    }

    @OnClick(R.id.iv_register_clear_username)
    public void clearUserName(View view) {
        etRegisterUsername.getText().clear();
    }


    /**
     * 重新获取验证码
     *
     * @param view
     */
    @OnClick(R.id.tv_register_reciprocal_time)
    public void reGetPhoneVerifyCode(View view) {
        //
//        tvRegisterSwitch.post(new Runnable() {
//            @Override
//            public void run() {
//                tvRegisterReciprocalTime.setTextColor(getResources().getColor(R.color.color_999999));
//                for (int i = 0; i < 89; i++) {
//                    tvRegisterReciprocalTime.setText(i + "s");
//
//                }
//                tvRegisterReciprocalTime.setTextColor(getResources().getColor(R.color.color_4b9fd5));
//                tvRegisterReciprocalTime.setText("重新获取");
//            }
//        });
    }


}
