package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.superlog.SLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by chen on 5/26/16.
 */
public class LoginFragment extends LoginModelFragment {


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


    @BindView(R.id.tv_login_forget_pwd)
    TextView tvForgetPwd;


    public static LoginFragment newInstance(Bundle bundle) {
        LoginFragment fragment = new LoginFragment();
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
        return view;
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
        if (true) {
            setViewEnableStyle(tvCommit, true);
            return;
        }
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
        if (true) {
            setViewEnableStyle(tvCommit, true);
            return;
        }
        boolean flag = TextUtils.isEmpty(s);
        ivLoginClearUsername.setVisibility(flag ? View.GONE : View.VISIBLE);
        String pwd = etLoginPwd.getText().toString().trim();
        if (flag) {
            setViewEnableStyle(tvCommit, false);
        } else if (!TextUtils.isEmpty(pwd) && pwd.length() >= 6) {
            setViewEnableStyle(tvCommit, true);
        }
    }


    @OnClick(R.id.iv_login_clear_pwd)
    public void clearPwd(View view) {
        etLoginPwd.getText().clear();
    }

    @OnClick(R.id.iv_login_clear_username)
    public void clearUserName(View view) {
        etLoginUsername.getText().clear();
    }


    @OnClick(R.id.tv_model_commit)
    public void loginCommit(View view) {
        showCommitView(false);
        showForGetView(false);
        AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, false, 100, 0, 1000, 0, 500);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                showCommitView(true);
                showForGetView(true);
                AnimatorUtils.viewTranslationY(rLayoutLoginThirdParty, true, 100, 1000, 0, -30, 500);
            }
        }, 2000);
    }

    @OnClick(R.id.tv_login_forget_pwd)
    public void forgetPwd(View view) {
        //忘记密码
        ForgetPwdFragment fragment = (ForgetPwdFragment) getFragmentManager().findFragmentByTag("forget");
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            fragment = ForgetPwdFragment.newInstance(null);
            ft.add(R.id.fLayout_login_container, fragment, "forget");
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

    private void initParentFragmentView() {
        LoginModel1Fragment fragment = (LoginModel1Fragment) getActivity()
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


    private void showRegisterFragment() {
        RegisterByPhoneFragment fragment = (RegisterByPhoneFragment) getFragmentManager().findFragmentByTag("register");
        if (fragment == null) {
            fragment = RegisterByPhoneFragment.newInstance(null);
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

//    private void showInputLayout(boolean orientation) {
//        ObjectAnimator an;
//        if (orientation) {
//            an = ObjectAnimator.ofFloat(lLayoutLoginInput, "translationY", 1000, -30, 30, 0);
//        } else {
//            an = ObjectAnimator.ofFloat(lLayoutLoginInput, "translationX", -800, 30, -30, 0);
//        }
//        an.addListener(new showViewListener(lLayoutLoginInput, true));
//        an.setInterpolator(new BounceInterpolator());
//        an.setDuration(500).start();
//    }


    private void showCommitView(boolean isShow) {
        AnimatorSet set = new AnimatorSet();
        if (isShow) {
            set.playTogether(ObjectAnimator.ofFloat(tvCommit, "scaleX", 1),
                    ObjectAnimator.ofFloat(tvCommit, "scaleY", 1),
                    ObjectAnimator.ofFloat(tvCommit, "alpha", 1f));
        } else {
            set.playTogether(ObjectAnimator.ofFloat(tvCommit, "scaleX", 0),
                    ObjectAnimator.ofFloat(tvCommit, "scaleY", 0),
                    ObjectAnimator.ofFloat(tvCommit, "alpha", 0f));
        }
        set.addListener(new showViewListener(tvCommit, isShow));
        set.setDuration(300).start();
    }


    private void showForGetView(boolean isShow) {
        AnimatorSet set = new AnimatorSet();
        if (isShow) {
            set.playTogether(ObjectAnimator.ofFloat(tvForgetPwd, "scaleX", 1),
                    ObjectAnimator.ofFloat(tvForgetPwd, "scaleY", 1),
                    ObjectAnimator.ofFloat(tvForgetPwd, "alpha", 1f));
        } else {
            set.playTogether(ObjectAnimator.ofFloat(tvForgetPwd, "scaleX", 0f),
                    ObjectAnimator.ofFloat(tvForgetPwd, "scaleY", 0f),
                    ObjectAnimator.ofFloat(tvForgetPwd, "alpha", 0f));
        }
        set.addListener(new showViewListener(tvForgetPwd, isShow));
        set.setDuration(300).start();
    }


//    private void showThirdPartyLayout(boolean isShow, long delay) {
//        ObjectAnimator an;
//        if (isShow) {
//            an = ObjectAnimator.ofFloat(rLayoutLoginThirdParty, "translationY", 1000, -30, 0);
//            an.setInterpolator(new BounceInterpolator());
//        } else {
//            an = ObjectAnimator.ofFloat(rLayoutLoginThirdParty, "translationY", 0, 1000);
//        }
//        an.addListener(new showViewListener(rLayoutLoginThirdParty, isShow));
//        if (delay > 0) {
//            an.setStartDelay(delay);
//        }
//        an.setDuration(500).start();
//    }


//    public void viewTranslationY(View view, boolean isShow, long delay, float start, float end, float offset, int duration) {
//        ObjectAnimator an;
//        if (isShow) {
//            an = ObjectAnimator.ofFloat(view, "translationY", start, offset, end);
//            an.setInterpolator(new BounceInterpolator());
//        } else {
//            an = ObjectAnimator.ofFloat(view, "translationY", start, end);
//        }
//        an.addListener(new showViewListener(view, isShow));
//        if (delay > 0) {
//            an.setStartDelay(delay);
//        }
//        an.setDuration(duration).start();
//    }


    /**
     * 动画监听器
     */
    class showViewListener implements Animator.AnimatorListener {

        private boolean isShow;
        private View view;

        public showViewListener(View view, boolean isShow) {
            this.isShow = isShow;
            this.view = view;
        }

        // 在开始时，先显示view
        @Override
        public void onAnimationStart(Animator animator) {
            if (isShow) {
                view.setVisibility(View.VISIBLE);
            }
        }

        //如果要隐藏的view 在结束时隐藏
        @Override
        public void onAnimationEnd(Animator animator) {
            if (!isShow) {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    }

}
