package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.NewHomeActivity;
import com.cylan.jiafeigou.n.model.BeanInfoLogin;
import com.cylan.jiafeigou.n.mvp.contract.login.LoginContract;
import com.cylan.jiafeigou.n.mvp.impl.login.LoginPresenterImpl;
import com.cylan.jiafeigou.support.sina.SinaWeiboUtil;
import com.cylan.jiafeigou.support.tencent.TencentLoginUtils;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.superlog.SLog;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by chen on 5/26/16.
 */
public class LoginParentFragment extends LoginBaseFragment implements LoginContract.ViewRequiredOps {

    @BindView(R.id.et_login_username)
    EditText etLoginUsername;

    @BindView(R.id.iv_login_clear_username)
    ImageView ivLoginClearUsername;

    @BindView(R.id.et_login_pwd)
    EditText etLoginPwd;

    @BindView(R.id.iv_login_clear_pwd)
    ImageView ivLoginClearPwd;

    @BindView(R.id.cb_show_pwd)
    CheckBox rbShowPwd;

    @BindView(R.id.tv_model_commit)
    TextView tvCommit;

    @BindView(R.id.lLayout_login_input)
    LinearLayout lLayoutLoginInput;

    @BindView(R.id.rLayout_login_third_party)
    RelativeLayout rLayoutLoginThirdParty;

    @BindView(R.id.rLayout_login)
    RelativeLayout rLayoutLogin;

    @BindView(R.id.tv_qqLogin_commit)
    TextView tvQqLoginCommit;

    @BindView(R.id.tv_xlLogin_commit)
    TextView tvXlLoginCommit;

    @BindView(R.id.tv_login_forget_pwd)
    TextView tvForgetPwd;

    @BindView(R.id.pb_login_loading)
    ProgressBar pbLoginLoading;

    private LoginContract.PresenterOps mPresenter;
    private BeanInfoLogin beanInfoLogin;


    private final int LOGIN_QQ_TYPE = 1;
    private final int LOGIN_XL_TYPE = 2;


    public static LoginParentFragment newInstance(Bundle bundle) {
        LoginParentFragment fragment = new LoginParentFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login_layout, container, false);
        ButterKnife.bind(this, view);
        addOnTouchListener(view);
        initView();
        editTextLimitMaxInput(etLoginPwd, 12);
        editTextLimitMaxInput(etLoginUsername, 60);
        SLog.e("onCreateView");
        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SLog.e("onCreate");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SLog.e("onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        List<Fragment> list = getFragmentManager().getFragments();
        SLog.e("onStart : list size " + list.size());
        for (Fragment f : list) {
            SLog.e(f.toString());

        }
        boolean first = false;
        if (getArguments() != null) {
            first = this.getArguments().getBoolean("first", false);
        }
        final boolean flag = first;
        lLayoutLoginInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                showAllLayout(flag);
            }
        }, flag ? 500 : 10);


    }

    @Override
    public void onPause() {
        super.onPause();
        SLog.e("onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        SLog.e("onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        SLog.e("onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SLog.e("onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        SLog.e("onDetach");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter = new LoginPresenterImpl(this);
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


    private void initView() {
        lLayoutLoginInput.setVisibility(View.INVISIBLE);
        rLayoutLoginThirdParty.setVisibility(View.INVISIBLE);
    }

    /**
     * 密码变化
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @OnTextChanged(R.id.et_login_pwd)
    public void onPwdChange(CharSequence s, int start, int before, int count) {
//        if (true) {
//            setViewEnableStyle(tvCommit, true);
//            return;
//        }
        boolean flag = TextUtils.isEmpty(s);
        ivLoginClearPwd.setVisibility(flag ? View.GONE : View.VISIBLE);
        if (flag || s.length() < 6) {
            setViewEnableStyle(tvCommit, false);
        } else if (!TextUtils.isEmpty(etLoginUsername.getText().toString())) {
            setViewEnableStyle(tvCommit, true);
        }

    }


    /***
     * 账号变化
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */

    @OnTextChanged(R.id.et_login_username)
    public void onUserNameChange(CharSequence s, int start, int before, int count) {
//        if (true) {
//            setViewEnableStyle(tvCommit, true);
//            return;
//        }
        boolean flag = TextUtils.isEmpty(s);
        ivLoginClearUsername.setVisibility(flag ? View.GONE : View.VISIBLE);
        String pwd = etLoginPwd.getText().toString().trim();
        if (flag) {
            setViewEnableStyle(tvCommit, false);
        } else if (!TextUtils.isEmpty(pwd) && pwd.length() >= 6) {
            setViewEnableStyle(tvCommit, true);
        }
    }

    @OnClick({
            R.id.tv_qqLogin_commit,
            R.id.tv_xlLogin_commit,
            R.id.iv_login_clear_pwd,
            R.id.iv_login_clear_username,
            R.id.tv_login_forget_pwd
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_login_clear_pwd:
                etLoginPwd.getText().clear();
                break;
            case R.id.iv_login_clear_username:
                etLoginUsername.getText().clear();
                break;
            case R.id.tv_login_forget_pwd:
                forgetPwd(null);
                break;
            case R.id.tv_qqLogin_commit:
                mPresenter.thirdLogin(getActivity(), LOGIN_QQ_TYPE);
                break;
            case R.id.tv_xlLogin_commit:
                mPresenter.thirdLogin(getActivity(), LOGIN_XL_TYPE);
                break;
        }
    }


    @OnClick(R.id.tv_model_commit)
    public void loginCommit(View view) {
        AnimatorUtils.ViewScaleCenter(tvCommit, false, 300, 0);
        AnimatorUtils.ViewScaleCenter(tvForgetPwd, false, 300, 0);
        AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, false, 100, 0, 1000, 0, 500);
        beanInfoLogin = new BeanInfoLogin();
        beanInfoLogin.userName = "TianChao";
        beanInfoLogin.pwd = "hello world";
        pbLoginLoading.setVisibility(View.VISIBLE);
        mPresenter.executeLogin(getActivity(), beanInfoLogin);
    }

    private void forgetPwd(View view) {
        //忘记密码
        ForgetPwdParentFragment fragment = (ForgetPwdParentFragment) getFragmentManager().findFragmentByTag("forget");
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            fragment = ForgetPwdParentFragment.newInstance(null);
            ft.replace(R.id.fLayout_login_container, fragment, "forget");
        }
        ft.hide(this).show(fragment).commit();
    }

    @Override
    public void onAttach(Context context) {
        initParentFragmentView();
        super.onAttach(context);
    }


    @Override
    public void onResume() {
        super.onResume();
        SLog.e("onResume");
    }

    private void initParentFragmentView() {
        LoginModelParentFragment fragment = (LoginModelParentFragment) getActivity()
                .getSupportFragmentManager().getFragments().get(0);
        fragment.tvTopRight.setText("注册");
        fragment.tvTopCenter.setText("登录");
        fragment.tvTopRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterFragment();
            }
        });
    }


    /**
     * 判断时区，如果是中国的就首选手机注册
     */
    private void showRegisterFragment() {
        Fragment fragment;
        fragment = getFragmentManager().findFragmentByTag("register");
        if (inChina()) {
            if (fragment == null) {
                fragment = RegisterByPhoneParentFragment.newInstance(null);
            }
        } else {
            if (fragment == null) {
                fragment = RegisterByMailParentFragment.newInstance(null);
            }
        }
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fLayout_login_container, fragment, "register").commit();

    }


    private void showAllLayout(boolean orientation) {
//        ViewGroup parent = (ViewGroup) lLayoutLoginInput.getParent();
//        int distance = parent.getHeight() - lLayoutLoginInput.getTop();
//        showInputLayout(true);
        if (orientation) {
            AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, true, 100, 1000, 0, -30, 500);
            AnimatorUtils.viewTranslationY(lLayoutLoginInput, true, 0, 1000, 0, -30, 500);
        } else {
            AnimatorUtils.viewTranslationX(rLayoutLoginThirdParty, true, 100, -800, 0, 30, 500);
            AnimatorUtils.viewTranslationX(lLayoutLoginInput, true, 0, -800, 0, 30, 500);
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //在某些低端机上调用登录后，由于内存紧张导致APP被系统回收，登录成功后无法成功回传数据
        if (requestCode == Constants.REQUEST_API) {
            if (resultCode == Constants.REQUEST_LOGIN) {
                TencentLoginUtils curTencent = mPresenter.getTencentObj();
                if (curTencent != null)
                    curTencent.getMyTencent().handleLoginData(data, new BaseUiListener());
            }
        } else {
            SinaWeiboUtil curSina = mPresenter.getSinaObj();
            if (curSina != null && curSina.getMySsoHandler() != null)
                curSina.getMySsoHandler().authorizeCallBack(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class BaseUiListener implements IUiListener {


        @Override
        public void onComplete(Object response) {
            if (null == response) {
                showFailedError("获取qq信息失败");
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                showFailedError("获取qq信息失败");
                return;
            }

            String alias = "";
            try {
                if (jsonResponse.has("nickname"))
                    alias = jsonResponse.getString("nickname");
                PreferencesUtils.setThirDswLoginPicUrl(getContext(), jsonResponse.getString("figureurl_qq_1"));
            } catch (JSONException e) {
                SLog.e(e.toString());
            }
            LoginExecuted("success");
        }

        @Override
        public void onError(UiError uiError) {

        }

        @Override
        public void onCancel() {

        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        SLog.e("onHiddenChanged" + hidden);
        super.onHiddenChanged(hidden);
    }

    @Override
    public void LoginExecuted(String msg) {
        if (!msg.equals("success")) {
            ToastUtil.showFailToast(getContext(), msg);
//            AnimatorUtils.ViewScaleCenter(tvCommit, true, 300, 3000);
//            AnimatorUtils.ViewScaleCenter(tvForgetPwd, true, 300, 3000);
            AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, true, 3000, 1000, 0, -30, 500);
            pbLoginLoading.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AnimatorUtils.ViewScaleCenter(tvCommit, true, 300, 0);
                    AnimatorUtils.ViewScaleCenter(tvForgetPwd, true, 300, 0);
                    pbLoginLoading.setVisibility(View.GONE);
                }
            }, 3000);
            return;
        }
        getContext().startActivity(new Intent(getContext(), NewHomeActivity.class));
        getActivity().finish();
    }

    public void showFailedError(String error) {
        ToastUtil.showFailToast(getContext(), error);
    }
}
