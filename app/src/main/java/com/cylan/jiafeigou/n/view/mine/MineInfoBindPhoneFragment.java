package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineBindPhoneContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineBindPhonePresenterImp;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * 作者：zsl
 * 创建时间：2016/9/1
 * 描述：
 */
public class MineInfoBindPhoneFragment extends Fragment implements MineBindPhoneContract.View {

    @BindView(R.id.et_input_userphone)
    EditText etInputUserphone;
    @BindView(R.id.tv_get_checkNumber)
    TextView tvGetCheckNumber;
    @BindView(R.id.tv_top_bar_center)
    TextView tvTopBarCenter;
    @BindView(R.id.iv_top_bar_left)
    ImageView ivTopBarLeft;
    @BindView(R.id.iv_forget_clear_username)
    ImageView ivForgetClearUsername;
    @BindView(R.id.tv_meter_get_code)
    TextView tvMeterGetCode;
    @BindView(R.id.fLayout_verification_code_input_box)
    FrameLayout fLayoutVerificationCodeInputBox;
    @BindView(R.id.et_verification_input)
    EditText etVerificationInput;

    private JFGAccount userinfo;

    private boolean isBindOrChange;
    private MineBindPhoneContract.Presenter presenter;
    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_mine_bind_phone, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        initPresenter();
        initEditListener();
        initCountDownTime();
        return view;
    }

    /**
     * 验证码输入框变化监听
     */
    @OnTextChanged(R.id.et_verification_input)
    public void initCheckCodeListener(CharSequence s, int start, int before, int count) {
         if (TextUtils.isEmpty(s)){
             tvGetCheckNumber.setEnabled(false);
         }else {
             tvGetCheckNumber.setEnabled(true);
         }
    }

    private void initCountDownTime() {
        countDownTimer = new CountDownTimer(90 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                final String content = millisUntilFinished / 1000 + "s";
                tvMeterGetCode.setText(content);
            }

            @Override
            public void onFinish() {
                tvMeterGetCode.setText("重新获取");
                tvMeterGetCode.setEnabled(true);
            }
        };
    }

    /**
     * 输入框监听
     */
    private void initEditListener() {
        etInputUserphone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    tvGetCheckNumber.setEnabled(false);
                    ivForgetClearUsername.setVisibility(View.INVISIBLE);
                } else {
                    tvGetCheckNumber.setEnabled(true);
                    ivForgetClearUsername.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initPresenter() {
        presenter = new MineBindPhonePresenterImp(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null){
            presenter.isBindOrChange(userinfo);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    /**
     * 获取传递过来的参数
     */
    private void getArgumentData() {
        Bundle arguments = getArguments();
        userinfo = (JFGAccount) arguments.getSerializable("userinfo");
    }

    public static MineInfoBindPhoneFragment newInstance(Bundle bundle) {
        MineInfoBindPhoneFragment fragment = new MineInfoBindPhoneFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void setPresenter(MineBindPhoneContract.Presenter presenter) {

    }

    @OnClick({R.id.tv_get_checkNumber, R.id.iv_top_bar_left})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_get_checkNumber:
                if (isBindOrChange) {
                    if (false){
                        // TODO 三方登录
                        jump2SetpassFragment(userinfo.getAccount());
                    }else {
                        // 非三方登录
                        handlerChangeOrBindPhone();
                    }
                }else {
                    // 更改手机号
                    handlerChangeOrBindPhone();
                }
                break;

            case R.id.iv_top_bar_left:
                getFragmentManager().popBackStack();
                break;

            case R.id.iv_forget_clear_username:
                etInputUserphone.setText("");
                break;

            case R.id.tv_meter_get_code:
                //重新获取验证码
                ToastUtil.showToast("验证码已发送");
                countDownTimer.start();
                tvMeterGetCode.setEnabled(false);
                presenter.getCheckCode(getInputPhone());
                break;
        }

    }

    /**
     * 跳转到设置密码界面
     */
    private void jump2SetpassFragment(String account) {
        // TODO　跳转到设置密码界面
    }

    /**
     * 处理绑定者更改手机号
     */
    private void handlerChangeOrBindPhone() {
        //绑定手机号
        if (tvGetCheckNumber.getText().equals("获取验证码")) {
            if (JConstant.PHONE_REG.matcher(getInputPhone()).find()) {
                presenter.checkPhoneIsBind(getInputPhone());
            } else {
                ToastUtil.showToast("请求输入有效手机号");
            }
        } else {
            // 发送修改用户属性请求
            if (getInputPhone().equals(PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN))){
                if (!tvMeterGetCode.getText().toString().equals("重新获取")){
                    userinfo.setPhone(getInputPhone(),getInputCheckCode());
                    userinfo.resetFlag();
                    presenter.sendChangePhoneReq(userinfo);
                }else {
                    ToastUtil.showToast("验证码已失效");
                }
            }else {
                ToastUtil.showToast("验证码错误");
            }
        }
    }

    @Override
    public void initToolbarTitle(String title) {
        tvTopBarCenter.setText(title);
        if (title.equals("更改手机号")){
            isBindOrChange = false;
        }else {
            isBindOrChange = true;
        }
    }

    @Override
    public String getInputPhone() {
        return etInputUserphone.getText().toString().trim();
    }

    /**
     * 获取到输入的验证码
     * @return
     */
    @Override
    public String getInputCheckCode() {
        return etVerificationInput.getText().toString().trim();
    }

    /**
     * 检测账号是否已经注册的结果
     *
     * @param checkAccountCallback
     */
    @Override
    public void handlerCheckPhoneResult(RxEvent.CheckAccountCallback checkAccountCallback) {
        if (getInputPhone().equals(checkAccountCallback.s)) {
            AlertDialog.Builder hasBineDialog = new AlertDialog.Builder(getContext())
                    .setTitle("该手机号已经被绑定")
                    .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            hasBineDialog.show();
        } else {
            //显示倒计时
            ToastUtil.showToast("验证码已发送");
            fLayoutVerificationCodeInputBox.setVisibility(View.VISIBLE);
            countDownTimer.start();
            tvMeterGetCode.setEnabled(false);
            tvGetCheckNumber.setText("继续");
            tvGetCheckNumber.setEnabled(false);
            //发送验证码
            presenter.getCheckCode(getInputPhone());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            tvMeterGetCode.setVisibility(View.INVISIBLE);
            countDownTimer.onFinish();
        }
        if (presenter != null)presenter.stop();
    }
}
