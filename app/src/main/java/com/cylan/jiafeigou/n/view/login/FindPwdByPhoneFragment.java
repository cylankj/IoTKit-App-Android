package com.cylan.jiafeigou.n.view.login;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
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
import com.superlog.SLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by lxh on 16-6-8.
 */

public class FindPwdByPhoneFragment extends LoginBaseFragment {


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
//        editTextLimitMaxInput(etRegisterUsername, 11);
//        editTextLimitMaxInput(etRegisterCode, 6);
        return view;
    }


    private void initView(View view) {
        tvRegisterSwitch.setVisibility(View.GONE);
        //设置手机号
        if (getArguments() != null) {
            String phone = getArguments().getString("phone");
            etRegisterUsername.setText(phone);
        }
        lLayoutInputCode.setVisibility(View.VISIBLE);
        tvCommit.setText("继续");
        timer.start();
    }


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
        if (tvRegisterReciprocalTime.isEnabled()) {
            tvRegisterReciprocalTime.setEnabled(false);
            tvRegisterReciprocalTime.setTextColor(getResources().getColor(R.color.color_d8d8d8)); //
            timer.start();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SLog.e("onDetach");
        timer.cancel();
    }


    CountDownTimer timer = new CountDownTimer(90 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tvRegisterReciprocalTime.setText(millisUntilFinished / 1000 + "S");
            SLog.i("millisUntilFinished:" + millisUntilFinished / 1000);
        }

        @Override
        public void onFinish() {
            tvRegisterReciprocalTime.setTextColor(getResources().getColor(R.color.color_4b9fd5)); //改为蓝色
            tvRegisterReciprocalTime.setText("重新获取");
            tvRegisterReciprocalTime.setEnabled(true);
        }
    };


}
