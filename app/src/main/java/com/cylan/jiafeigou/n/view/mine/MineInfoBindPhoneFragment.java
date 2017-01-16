package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
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
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineBindPhoneContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineBindPhonePresenterImp;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

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

    @BindView(R.id.iv_mine_bind_phone_back)
    ImageView ivMineBindPhoneBack;
    @BindView(R.id.iv_mine_info_bind_phone)
    ImageView ivMineInfoBindPhone;
    @BindView(R.id.et_mine_bind_phone)
    EditText etMineBindPhone;
    @BindView(R.id.iv_mine_bind_phone_clear)
    ImageView ivMineBindPhoneClear;
    @BindView(R.id.et_verification_input)
    EditText etVerificationInput;
    @BindView(R.id.tv_meter_get_code)
    TextView tvMeterGetCode;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.fLayout_verification_code_input_box)
    FrameLayout fLayoutVerificationCodeInputBox;
    private JFGAccount userinfo;
    private boolean isBindOrChange;
    private MineBindPhoneContract.Presenter presenter;
    private CountDownTimer countDownTimer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_info_bind_phone, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        initPresenter();
        initCountDownTime();
        return view;
    }

    /**
     * 验证码输入框变化监听
     */
    @OnTextChanged(R.id.et_verification_input)
    public void initCheckCodeListener(CharSequence s, int start, int before, int count) {
        if (TextUtils.isEmpty(s)) {
            ivMineInfoBindPhone.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
            ivMineInfoBindPhone.setEnabled(false);
        } else {
            ivMineInfoBindPhone.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish));
            ivMineInfoBindPhone.setEnabled(true);
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
                tvMeterGetCode.setText(getString(R.string.Button_ReObtain));
                tvMeterGetCode.setEnabled(true);
            }
        };
    }

    /**
     * 输入框监听
     */
    @OnTextChanged(R.id.et_mine_bind_phone)
    public void initEditListener(CharSequence s, int start, int before, int count) {
        if (s.length() == 0) {
            tvMeterGetCode.setEnabled(false);
            ivMineBindPhoneClear.setVisibility(View.INVISIBLE);
        } else {
            tvMeterGetCode.setEnabled(true);
            ivMineBindPhoneClear.setVisibility(View.VISIBLE);
        }
    }

    private void initPresenter() {
        presenter = new MineBindPhonePresenterImp(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
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

    @OnClick({R.id.iv_mine_bind_phone_back, R.id.tv_meter_get_code, R.id.iv_mine_bind_phone_clear, R.id.iv_mine_info_bind_phone})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_meter_get_code:
                //获取验证码点击
                if (JConstant.PHONE_REG.matcher(getInputPhone()).find()) {
                    presenter.checkPhoneIsBind(getInputPhone());
                } else {
                    ToastUtil.showToast(getString(R.string.PHONE_NUMBER_2));
                }
                break;

            case R.id.iv_mine_bind_phone_back:
                getFragmentManager().popBackStack();
                break;

            case R.id.iv_mine_bind_phone_clear:
                etMineBindPhone.setText("");
                break;

            case R.id.iv_mine_info_bind_phone:
                //点击完成
                if (getInputCheckCode().length() != 6) {
                    ToastUtil.showToast(getString(R.string.Tap0_wrongcode));
                } else {
                    presenter.CheckVerifyCode(getInputCheckCode(), PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
                }
                break;
        }

    }

    /**
     * 跳转到设置密码界面
     */
    private void jump2SetpassFragment(String account) {
        Bundle bundle = new Bundle();
        bundle.putString("useraccount", account);
        bundle.putString("token", getInputCheckCode());
        MineInfoSetNewPwdFragment fragment = MineInfoSetNewPwdFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, fragment, "mailBoxFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
    }

    /**
     * 处理绑定者更改手机号
     */
    private void handlerChangeOrBindPhone() {
        //绑定手机号
        if (tvMeterGetCode.getText().equals(getString(R.string.GET_CODE))) {
            if (JConstant.PHONE_REG.matcher(getInputPhone()).find()) {
                presenter.checkPhoneIsBind(getInputPhone());
            } else {
                ToastUtil.showToast(getString(R.string.PHONE_NUMBER_2));
            }
        } else {
            // 发送修改用户属性请求
            if (getInputPhone().equals(PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN))) {
                if (!tvMeterGetCode.getText().toString().equals(getString(R.string.Button_ReObtain))) {
                    userinfo.setPhone(getInputPhone(), getInputCheckCode());
                    userinfo.resetFlag();
                    presenter.sendChangePhoneReq();
                } else {
                    ToastUtil.showToast(getString(R.string.Tap0_invaildcode));
                }
            } else {
                ToastUtil.showToast(getString(R.string.Tap0_wrongcode));
            }
        }
    }

    @Override
    public void initToolbarTitle(String title) {
        tvTitle.setText(title);
        if (title.equals(getString(R.string.CHANGE_PHONE_NUM))) {
            isBindOrChange = false;
        } else {
            isBindOrChange = true;
        }
    }

    @Override
    public String getInputPhone() {
        return etMineBindPhone.getText().toString().trim();
    }

    /**
     * 获取到输入的验证码
     *
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
                    .setTitle(getString(R.string.RET_EEDITUSERINFO_SMS_PHONE))
                    .setPositiveButton(getString(R.string.I_KNOW), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            hasBineDialog.show();
        } else {
            //显示倒计时
            fLayoutVerificationCodeInputBox.setVisibility(View.VISIBLE);
            countDownTimer.start();
            tvMeterGetCode.setEnabled(false);
            //发送验证码
            presenter.getCheckCode(getInputPhone());
        }
    }

    /**
     * 校验短信验证码的结果
     *
     * @param resultVerifyCode
     */
    @Override
    public void handlerCheckCodeResult(RxEvent.ResultVerifyCode resultVerifyCode) {
        if (resultVerifyCode.code == JError.ErrorOK) {
            if (isBindOrChange) {
                if (presenter.isOpenLogin() && TextUtils.isEmpty(userinfo.getPhone()) && TextUtils.isEmpty(userinfo.getEmail())) {
                    //是三方登录
                    jump2SetpassFragment(userinfo.getAccount());
                    return;
                }
            }
            showLoadingDialog();
            presenter.sendChangePhoneReq();
        } else {
            hideLoadingDialog();
            ToastUtil.showNegativeToast(getString(R.string.RET_ESMS_CODE_FALSE));
        }
    }

    /**
     * 处理修改结果
     *
     * @param getUserInfo
     */
    @Override
    public void handlerResetPhoneResult(RxEvent.GetUserInfo getUserInfo) {
        hideLoadingDialog();
        if (!TextUtils.isEmpty(getInputPhone())) {
            if (getInputPhone().equals(getUserInfo.jfgAccount.getPhone())) {
                ToastUtil.showPositiveToast(getString(R.string.SCENE_SAVED));
                if (getView() != null) {
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getFragmentManager().popBackStack();
                        }
                    }, 2000);
                }
            } else {
                ToastUtil.showPositiveToast(getString(R.string.SUBMIT_FAIL));
            }
        }
    }

    /**
     * 显示loading
     */
    @Override
    public void showLoadingDialog() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
    }

    /**
     * 隐藏loading
     */
    @Override
    public void hideLoadingDialog() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideLoadingDialog();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            tvMeterGetCode.setVisibility(View.INVISIBLE);
            countDownTimer.onFinish();
        }
        if (presenter != null) presenter.stop();
    }
}
