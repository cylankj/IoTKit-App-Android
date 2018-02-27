package com.cylan.jiafeigou.n.view.login;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.impl.SetupPwdPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.LoginAccountBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterPwdFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterPwdFragment extends SetupPwdFragment
        implements BaseDialog.BaseDialogAction {
    private static final String DIALOG_KEY = "dialogFragment";

    public RegisterPwdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new SetupPwdPresenterImpl(this);
    }

    /**
     * Use this factory method to create activity_cloud_live_mesg_call_out_item new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SetupPwdFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterPwdFragment newInstance(Bundle args) {
        RegisterPwdFragment fragment = new RegisterPwdFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        customToolbar.setBackAction((View v) -> showSimpleDialog(getString(R.string.Tap3_logout_tips), getString(R.string.Button_Yes), getString(R.string.Button_No), false));
        try {
            View view = getView();
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    showSimpleDialog(getString(R.string.Tap3_logout_tips), getString(R.string.Button_Yes), getString(R.string.Button_No), false);
                    return true;
                }
                return false;
            });
        } catch (Exception e) {
        }
    }

    @Override
    public void doAction(String account, String pwd, String code) {
        if (NetUtils.getJfgNetType(getContext()) == -1) {
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }
        boolean validPhoneNum = JConstant.PHONE_REG.matcher(account).find();
        AppLogger.i("account:" + account + ",pwd:" + pwd + ",code:" + code);
        boolean isPhone =
                (validPhoneNum && !TextUtils.isEmpty(code) && code.length() == JConstant.VALID_VERIFICATION_CODE_LEN);
        presenter.register(account,
                pwd,
                isPhone ? JConstant.TYPE_PHONE : JConstant.TYPE_EMAIL,
                isPhone ? PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN) : "");

        PreferencesUtils.putString(JConstant.AUTO_LOGIN_ACCOUNT, account);
        PreferencesUtils.putString(JConstant.AUTO_LOGIN_PWD, pwd);
    }

    @Override
    public void onDialogAction(int id, Object value) {
        Fragment f = getActivity()
                .getSupportFragmentManager()
                .findFragmentByTag(DIALOG_KEY);
        if (f != null && f.isVisible()) {
            ((SimpleDialogFragment) f).dismiss();
        }
        if (id == R.id.tv_dialog_btn_left) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void submitResult(int code) {
        if (!isAdded() && getView() != null) {
            getView().postDelayed(() -> switchResult(code), 100);
        } else {
            switchResult(code);
        }
    }

    private void switchResult(int code) {
        switch (code) {
            case JError.ErrorAccountAlreadyExist:
//                ToastUtil.showToast(getString(R.string.RET_EREGISTER_PHONE_EXIST));
                autoLogin();//#115668 手机注册 在设置密码页面输入密码点击确定后，轻提示注册成功，应进入到首页
                break;
            case JError.ErrorSMSCodeNotMatch:
                ToastUtil.showToast(getString(R.string.CODE_ERR));
                break;
            case JError.ErrorSMSCodeTimeout:
                showSimpleDialog(getString(R.string.INVALID_CODE), getString(R.string.I_KNOW), " ", false);
                break;
            case JError.ErrorOK:
                autoLogin();
                break;
            default:
                ToastUtil.showToast("error:" + code);
        }
        if (code != JError.ErrorOK) {
            PreferencesUtils.putString(JConstant.AUTO_LOGIN_ACCOUNT, "");
            PreferencesUtils.putString(JConstant.AUTO_LOGIN_PWD, "");
        }
    }

    private void autoLogin() {
        LoginAccountBean login = new LoginAccountBean();
        login.userName = PreferencesUtils.getString(JConstant.AUTO_LOGIN_ACCOUNT);
        login.pwd = PreferencesUtils.getString(JConstant.AUTO_LOGIN_PWD);//为什么要 MD5?存的时候没有 MD5
        boolean validEmailNum = JConstant.EMAIL_REG.matcher(login.userName).find();
        if (validEmailNum) {
            //发送验证邮件
            afterSendMailView(login.userName);
            return;
        }

        if (!TextUtils.isEmpty(login.userName) || !TextUtils.isEmpty(login.pwd)) {
            if (NetUtils.getNetType(ContextUtils.getContext()) != -1) {
                ToastUtil.showToast(getString(R.string.RIGN_SUC));
                presenter.executeLogin(login);
            } else {
                ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_4));
            }
        }
    }

    /**
     * 发送验证邮件后view
     */
    private void afterSendMailView(String account) {
        vsSetAccountPwd.removeAllViews();
        customToolbar.setToolbarTitle(R.string.Tap0_register_EmailVerification);
        customToolbar.setTvToolbarIcon(-1);
        flInputContainer.setVisibility(View.GONE);
        View mailView = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_forget_pwd_by_email, null);
        if (mailView == null) {
            return;
        }

        ((TextView) mailView.findViewById(R.id.tv_send_email_content)).setText(account);
        TextView view = (TextView) mailView.findViewById(R.id.tv_send_email_info);
        view.setText(R.string.Tap0_Click_ActivateAccount);
        TextView btn = (TextView) mailView.findViewById(R.id.tv_email_confirm);
        btn.setText(getString(R.string.Tap0_register_GoToLogin));
        btn.setEnabled(true);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getActivity().getSupportFragmentManager().popBackStack();
                // TODO 去登录
                PreferencesUtils.putBoolean(JConstant.REG_SWITCH_BOX, true);
                RxBus.getCacheInstance().post(new RxEvent.LoginPopBack(account));
                RxBus.getCacheInstance().post(new RxEvent.SwitchBox());
                ActivityUtils.justPop(getActivity());
            }
        });
        vsSetAccountPwd.addView(mailView);
        vsSetAccountPwd.showNext();
    }

    /**
     * 弹框，{fragment}
     */
    private void showSimpleDialog(String title,
                                  String lContent,
                                  String rContent,
                                  boolean dismiss) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_KEY);
        if (f == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, title);
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, lContent);
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, rContent);
            bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, dismiss);
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
            dialogFragment.setAction(this);
            dialogFragment.show(getActivity().getSupportFragmentManager(), DIALOG_KEY);
        }
    }

    @Override
    public void loginResult(int code) {
        if (code == JError.ErrorOK) {
            if ((getActivity() instanceof NewHomeActivity)) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            getActivity().finish();
            getContext().startActivity(new Intent(getContext(), NewHomeActivity.class));
            PreferencesUtils.putString(JConstant.AUTO_LOGIN_ACCOUNT, "");
            PreferencesUtils.putString(JConstant.AUTO_LOGIN_PWD, "");
        } else {
//            ToastUtil.showToast(getString(R.string.LOGIN_ERR));
        }
    }

    @Override
    public boolean isLoginViewVisible() {
        final long time = System.currentTimeMillis();
        boolean notNull = getActivity() != null && getActivity().getWindow().getDecorView() != null;
        if (notNull) {
            View v = getActivity().getWindow().getDecorView().findViewById(android.R.id.content);
            if (v != null && v instanceof ViewGroup) {
                final int count = ((ViewGroup) v).getChildCount();
                if (count > 0) {
                    View thisLayout = ((ViewGroup) v).getChildAt(count - 1);
                    //yes this fragment is in top
                    notNull = (thisLayout != null && thisLayout.getId() == R.id.rLayout_login);
                }
            }
        }
        Log.d("perform", "perform: " + (System.currentTimeMillis() - time));
        return notNull;
    }
}
