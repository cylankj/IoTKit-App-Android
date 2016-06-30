package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
import com.cylan.jiafeigou.n.mvp.impl.RstPwdPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.jiafeigou.n.view.login_ex.ForgetPwdByEmailFragment;
import com.cylan.jiafeigou.n.view.login_ex.ResetPwdFragment;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.List;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

/**
 * 忘记密码
 * Created by lxh on 16-6-14.
 */

public class ForgetPwdFragment extends Fragment implements ForgetPwdContract.ForgetPwdView {

    public static final String KEY_ACCOUNT_TO_SEND_EMAIL = "key_to_send_email";
    public static final String ACCEPT_TYPE = "accept_type";

    @BindView(R.id.et_forget_username)
    EditText etForgetUsername;
    @BindView(R.id.iv_forget_clear_username)
    ImageView ivForgetClearUsername;
    @BindView(R.id.tv_forget_pwd_submit)
    TextView tvForgetPwdSubmit;
    @BindView(R.id.lLayout_register_input)
    LinearLayout lLayoutRegisterInput;
    @BindView(R.id.et_verification_input)
    EditText etVerificationInput;
    @BindView(R.id.tv_meter_get_code)
    TextView tvMeterGetCode;
    @BindView(R.id.fLayout_verification_code_input_box)
    FrameLayout fLayoutVerificationCodeInputBox;
    /**
     * {0}请输入手机号/邮箱 {1}请输入邮箱
     */
    private int acceptType = 0;
    private ForgetPwdContract.ForgetPwdPresenter presenter;
    private static final int TYPE_INVALID = -1;
    private static final int TYPE_PHONE = 0;
    private static final int TYPE_EMAIL = 1;

    private CountDownTimer countDownTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        acceptType = bundle.getInt(ACCEPT_TYPE);
        List<Fragment> fragmentList = getActivity().getSupportFragmentManager().getFragments();
        Log.d("", "" + fragmentList);
        initCountDownTimer();
    }

    private void initCountDownTimer() {
        countDownTimer = new CountDownTimer(10 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final String content = millisUntilFinished / 1000 + "s";
                tvMeterGetCode.setText(content);
            }

            @Override
            public void onFinish() {
                tvMeterGetCode.setText("重新发送");
                tvMeterGetCode.setEnabled(true);
            }
        };

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forget_pwd, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initTitleBar();
        initView(view);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null)
            countDownTimer.onFinish();
        if (presenter != null)
            presenter.stop();
    }

    private void initTitleBar() {
        FrameLayout layout = (FrameLayout) getView().findViewById(R.id.rLayout_login_top);
        layout.findViewById(R.id.tv_login_top_right).setVisibility(View.GONE);
        TextView tvTitle = (TextView) layout.findViewById(R.id.tv_login_top_center);
        tvTitle.setText("忘记密码");
        ImageView imgBackHandle = (ImageView) layout.findViewById(R.id.iv_login_top_left);
        imgBackHandle.setImageResource(R.drawable.btn_nav_back);
        imgBackHandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.justPop(getActivity());
            }
        });
    }

    public static ForgetPwdFragment newInstance(Bundle bundle) {
        ForgetPwdFragment fragment = new ForgetPwdFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    private void initView(View view) {
        if (acceptType == 1) {
            etForgetUsername.setHint("please input email address");
        }
        Bundle bundle = getArguments();
        if (bundle != null && !TextUtils.isEmpty(bundle.getString(LoginFragment.KEY_TEMP_ACCOUNT))) {
            etForgetUsername.setText(bundle.getString(LoginFragment.KEY_TEMP_ACCOUNT));
        }
    }

    private final static Pattern PHONE_REG = Pattern.compile("^1[3|4|5|7|8]\\d{9}$");

    private int checkInputType() {
        final String account = etForgetUsername.getText().toString();
        final boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(account).find();
        if (isEmail) {
            Toast.makeText(getActivity(), "请输入有效账号", Toast.LENGTH_SHORT).show();
            return TYPE_EMAIL;
        } else {
            final boolean isPhone = PHONE_REG.matcher(account).find();
            if (isPhone) {
                return TYPE_PHONE;
            }
            return TYPE_INVALID;
        }
    }

    @OnClick(R.id.tv_meter_get_code)
    public void reGetVerificationCode() {
        countDownTimer.start();
        tvMeterGetCode.setEnabled(false);
        if (presenter != null)
            presenter.executeSubmitAccount(etForgetUsername.getText().toString().trim());
    }

    @OnClick(R.id.iv_forget_clear_username)
    public void clearUsername(View view) {
        etForgetUsername.getText().clear();
    }

    @OnFocusChange(R.id.et_forget_username)
    public void onEtFocusChange(View view, boolean focused) {
        ivForgetClearUsername.setVisibility(focused ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * 这里有个动画，就是setVisibility(View.VISIBLE),加上
     * android:animateLayoutChanges="true",就有transition的效果。
     */
    private void start2HandleVerificationCode() {
        fLayoutVerificationCodeInputBox.setVisibility(View.VISIBLE);
        countDownTimer.start();
    }

    //判读是手机号还是邮箱
    private void next() {
        final int type = checkInputType();
        switch (type) {
            case TYPE_INVALID:
                Toast.makeText(getActivity(), "不合法", Toast.LENGTH_SHORT).show();
                enableEditTextCursor(true);
                return;
            case TYPE_PHONE:
                if (fLayoutVerificationCodeInputBox.getVisibility() == View.GONE) {
                    start2HandleVerificationCode();
                } else {
                    final String code = etVerificationInput.getText().toString();
                    if (!TextUtils.isEmpty(code) && code.length() != 6) {
                        Toast.makeText(getActivity(), "验证码有错", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (TextUtils.isEmpty(etForgetUsername.getText())) {
                        Toast.makeText(getActivity(), "手机号码为空", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "已发送", Toast.LENGTH_SHORT).show();
                        getArguments().putString(LoginFragment.KEY_TEMP_ACCOUNT, etForgetUsername.getText().toString());
                        countDownTimer.onFinish();
                        presenter.submitPhoneNumAndCode(etForgetUsername.getText().toString(), etVerificationInput.getText().toString());
                    }
                }
                break;
            case TYPE_EMAIL:
                if (presenter != null)
                    presenter.executeSubmitAccount(etForgetUsername.getText().toString().trim());
                break;
        }
        IMEUtils.hide(getActivity());
    }

    /**
     * 清除焦点。
     */
    private void enableEditTextCursor(boolean enable) {
        if (isResumed() && getActivity() != null) {
            ViewUtils.enableEditTextCursor(etForgetUsername, enable);
        }
    }

    @OnTextChanged(R.id.et_forget_username)
    public void userNameChange(CharSequence s, int start, int before, int count) {
        final boolean flag = TextUtils.isEmpty(s);
        ivForgetClearUsername.setVisibility(flag ? View.GONE : View.VISIBLE);
        boolean codeValid = checkInputType() != TYPE_INVALID;
        tvForgetPwdSubmit.setEnabled(!flag && codeValid);
    }

    @OnTextChanged(R.id.et_verification_input)
    public void verificationInputChange(CharSequence s, int start, int before, int count) {
        boolean self = !TextUtils.isEmpty(s) && TextUtils.isDigitsOnly(s);
        boolean user = !TextUtils.isEmpty(etForgetUsername.getText()) && TextUtils.isDigitsOnly(etForgetUsername.getText());
        tvForgetPwdSubmit.setEnabled(self && user);
    }

    @OnClick(R.id.tv_forget_pwd_submit)
    public void forgetPwdCommit(View v) {
        enableEditTextCursor(false);
        next();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    private void initTitle(final int ret) {
        if (ret == -1)
            return;
        TextView view = (TextView) getView().findViewById(R.id.tv_login_top_center);
        if (ret == 1)
            view.setText(getString(R.string.EMAIL));
        else if (ret == 2) {
            view.setText("新密码");
        }
    }

    @Override
    public void submitResult(RequestResetPwdBean bean) {
        final int ret = bean == null ? -1 : bean.ret;
        initTitle(ret);
        switch (ret) {
            case ForgetPwdContract.AUTHORIZE_MAIL:
                final String account = etForgetUsername.getText().toString().trim();
                Bundle bundle = getArguments();
                bundle.putString(KEY_ACCOUNT_TO_SEND_EMAIL, account);
                getChildFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_out_right
                                , R.anim.slide_out_right, R.anim.slide_out_right)
                        .replace(R.id.fLayout_forget_container,
                                ForgetPwdByEmailFragment.newInstance(getArguments()),
                                "mailFragment")
                        .commit();
                break;
            case ForgetPwdContract.AUTHORIZE_PHONE:
                //show timer
                ResetPwdFragment fragment = ResetPwdFragment.newInstance(getArguments());
                getChildFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_right_in, R.anim.slide_out_right
                                , R.anim.slide_out_right, R.anim.slide_out_right)
                        .replace(R.id.fLayout_forget_container,
                                fragment,
                                "rstPwdFragment")
                        .commit();
                new RstPwdPresenterImpl(fragment);
                break;
        }
    }

    @Override
    public void setPresenter(ForgetPwdContract.ForgetPwdPresenter presenter) {
        this.presenter = presenter;
    }
}
