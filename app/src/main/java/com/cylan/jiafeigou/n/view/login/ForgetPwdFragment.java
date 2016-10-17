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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.JCache;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.utils.NetUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

/**
 * 忘记密码
 * Created by lxh on 16-6-14.
 */

public class ForgetPwdFragment extends Fragment implements ForgetPwdContract.View {


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

    @Nullable
    @BindView(R.id.et_new_pwd_input)
    EditText etNewPwdInput;
    @Nullable
    @BindView(R.id.iv_new_clear_pwd)
    ImageView ivNewClearPwd;
    @Nullable
    @BindView(R.id.tv_new_pwd_submit)
    TextView tvNewPwdSubmit;
    @Nullable
    @BindView(R.id.rLayout_new_pwd_input_box)
    FrameLayout rLayoutRstPwdInputBox;

    @Nullable
    @BindView(R.id.cb_show_pwd)
    CheckBox cbShowPwd;
    @BindView(R.id.vs_set_account_pwd)
    ViewSwitcher vsSetAccountPwd;

    @BindView(R.id.tv_top_bar_center)
    TextView tvLoginTopCenter;

    /**
     * {0}请输入手机号/邮箱 {1}请输入邮箱
     */
    private int acceptType = 0;
    private ForgetPwdContract.Presenter presenter;


    private CountDownTimer countDownTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        acceptType = bundle.getInt(JConstant.KEY_LOCALE);
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
    public void onResume() {
        super.onResume();
        if (presenter != null)
            presenter.start();
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
        layout.findViewById(R.id.tv_top_bar_right).setVisibility(View.GONE);
        TextView tvTitle = (TextView) layout.findViewById(R.id.tv_top_bar_center);
        tvTitle.setText("忘记密码");
        ImageView imgBackHandle = (ImageView) layout.findViewById(R.id.iv_top_bar_left);
        imgBackHandle.setImageResource(R.drawable.icon_nav_back_white);
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
            ivForgetClearUsername.setVisibility(View.GONE);
        }
    }


    private int checkInputType() {
        final String account = ViewUtils.getTextViewContent(etForgetUsername);
        final boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(account).find();
        if (isEmail) {
            return JConstant.TYPE_EMAIL;
        } else {
            final boolean isPhone = JConstant.PHONE_REG.matcher(account).find();
            if (isPhone) {
                return JConstant.TYPE_PHONE;
            }
            return JConstant.TYPE_INVALID;
        }
    }

    @OnClick(R.id.tv_meter_get_code)
    public void reGetVerificationCode() {
        countDownTimer.start();
        tvMeterGetCode.setEnabled(false);
        Toast.makeText(getActivity(), "已发送", Toast.LENGTH_SHORT).show();
        if (presenter != null)
            presenter.submitAccount(ViewUtils.getTextViewContent(etForgetUsername));
    }

    @OnClick(R.id.iv_forget_clear_username)
    public void clearUsername(View view) {
        etForgetUsername.setText("");
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
            case JConstant.TYPE_INVALID:
                Toast.makeText(getActivity(), "不合法", Toast.LENGTH_SHORT).show();
                enableEditTextCursor(true);
                return;
            case JConstant.TYPE_PHONE:
                if (fLayoutVerificationCodeInputBox.getVisibility() == View.GONE) {
//                    tvLoginTopCenter.setText("忘记密码(手机)");
                    start2HandleVerificationCode();
                    if (presenter != null) {
                        presenter.submitAccount(ViewUtils.getTextViewContent(etForgetUsername));
                    }
                } else {
                    final String code = ViewUtils.getTextViewContent(etVerificationInput);
                    if (!TextUtils.isEmpty(code) && code.length() != JConstant.VALID_VERIFICATION_CODE_LEN) {
                        Toast.makeText(getActivity(), "验证码有错", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!JConstant.PHONE_REG.matcher(etForgetUsername.getText()).find()) {
                        Toast.makeText(getActivity(), "手机号码有错", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //验证码过期
                    if (!TextUtils.isEmpty(code) && code.length() == JConstant.VALID_VERIFICATION_CODE_LEN
                            && JConstant.PHONE_REG.matcher(etForgetUsername.getText()).find()) {
                        if (TextUtils.equals(getString(R.string.item_reget_verification_code),
                                ViewUtils.getTextViewContent(tvMeterGetCode))) {
                            Toast.makeText(getActivity(), "验证码已过期", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    Toast.makeText(getActivity(), "已发送", Toast.LENGTH_SHORT).show();
                    getArguments().putString(LoginFragment.KEY_TEMP_ACCOUNT, etForgetUsername.getText().toString());
                    if (presenter != null)
                        presenter.submitPhoneNumAndCode(etForgetUsername.getText().toString(), ViewUtils.getTextViewContent(etVerificationInput));
                }
                break;
            case JConstant.TYPE_EMAIL:
                enableEditTextCursor(false);
                Toast.makeText(getActivity(), "已发送", Toast.LENGTH_SHORT).show();
//                tvLoginTopCenter.setText("忘记密码(邮箱)");
                if (presenter != null)
                    presenter.submitAccount(ViewUtils.getTextViewContent(etForgetUsername));
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
        boolean codeValid = checkInputType() != JConstant.TYPE_INVALID;
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
        if (NetUtils.getJfgNetType(getContext()) == 0) {
            Toast.makeText(getContext(), "网络不通", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!JCache.isOnline) {
            Toast.makeText(getContext(), "连接服务器失败", Toast.LENGTH_SHORT).show();
            return;
        }
        next();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    private void initTitle(final int ret) {
        if (ret == -1)
            return;
        if (ret == JConstant.TYPE_EMAIL)
            tvLoginTopCenter.setText(getString(R.string.EMAIL));
        else if (ret == JConstant.TYPE_PHONE) {
            tvLoginTopCenter.setText("新密码");
        }
    }

    /**
     * 提交邮箱,修改密码，服务端响应之后的view
     */
    private void prepareMailView() {
        View view = vsSetAccountPwd.findViewById(R.id.layout_to_be_update);
        if (view != null) {
            vsSetAccountPwd.removeView(view);
        }
        View mailView = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_forget_pwd_by_email, null);
        if (mailView == null) {
            return;
        }
        final String content = String.format(getString(R.string.send_email_tip_content),
                ViewUtils.getTextViewContent(etForgetUsername));
        ((TextView) mailView.findViewById(R.id.tv_send_email_content)).setText(content);
        View btn = mailView.findViewById(R.id.tv_email_confirm);
        btn.setEnabled(true);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "yes?", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
                RxBus.getInstance().send(new RxEvent.LoginPopBack(etForgetUsername.getText().toString()));
            }
        });
        vsSetAccountPwd.addView(mailView);
        vsSetAccountPwd.showNext();
    }

    /**
     * 设置新手机账号的密码。
     */
    private void preparePhoneView() {
//        View view = vsSetAccountPwd.findViewById(R.id.layout_to_be_update);
//        if (view != null) {
//            vsSetAccountPwd.removeView(view);
//        }
//        View phoneNewPwdView = LayoutInflater.from(getContext())
//                .inflate(R.layout.fragment_set_new_pwd, null);
//        if (phoneNewPwdView == null) {
//            return;
//        }
        initTitle(JConstant.TYPE_PHONE);
//        vsSetAccountPwd.addView(phoneNewPwdView);
        vsSetAccountPwd.showNext();
    }

    @Override
    public void submitResult(RequestResetPwdBean bean) {
        final int ret = bean == null ? -1 : bean.ret;
        switch (ret) {
            case JConstant.THIS_ACCOUNT_NOT_REGISTERED:
                tvForgetPwdSubmit.setEnabled(true);
                Toast.makeText(getContext(), "账号未注册", Toast.LENGTH_SHORT).show();
                break;
            case JConstant.AUTHORIZE_MAIL:
                if (bean != null && !TextUtils.equals(bean.content, etForgetUsername.getText())) {
                    tvForgetPwdSubmit.setEnabled(true);
                    Toast.makeText(getActivity(), "账号未注册", Toast.LENGTH_SHORT).show();
                    break;
                }
                prepareMailView();
                break;
            case JConstant.AUTHORIZE_PHONE:
                tvForgetPwdSubmit.setEnabled(true);
                preparePhoneView();
                break;
        }
    }

    @Override
    public void setPresenter(ForgetPwdContract.Presenter presenter) {
        this.presenter = presenter;
    }

    //    lazy load 的view
    @Nullable
    @OnTextChanged(R.id.et_new_pwd_input)
    public void newPwdInputBoxChanged(CharSequence s, final int before, final int count, final int len) {
        final boolean empty = TextUtils.isEmpty(s);
        final boolean isNewPswValid = !empty
                && s.length() >= JConstant.PWD_LEN_MIN
                && s.length() <= JConstant.PWD_LEN_MAX;
        if (tvNewPwdSubmit != null) tvNewPwdSubmit.setEnabled(isNewPswValid);
        if (ivNewClearPwd != null) {
            ivNewClearPwd.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    @OnCheckedChanged(R.id.cb_new_pwd_show)
    public void onNewPwdCheckBoxState(CompoundButton view, boolean isChecked) {
        ViewUtils.showPwd(etNewPwdInput, isChecked);
        if (etNewPwdInput != null) etNewPwdInput.setSelection(etNewPwdInput.length());
    }

    @OnClick({R.id.iv_new_clear_pwd, R.id.tv_new_pwd_submit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_new_clear_pwd:
                if (etNewPwdInput != null) etNewPwdInput.setText("");
                break;
            case R.id.tv_new_pwd_submit:
                Toast.makeText(getActivity(), "yes?", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
                break;
        }
    }
}
