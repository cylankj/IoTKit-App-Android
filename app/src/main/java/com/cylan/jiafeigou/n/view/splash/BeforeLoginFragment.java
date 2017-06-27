package com.cylan.jiafeigou.n.view.splash;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.cache.LogState.STATE_GUEST;


/**
 * 登陆之前的界面，可以选择登陆，或者随便看看
 * Created by lxh on 16-6-12.
 */

public class BeforeLoginFragment extends Fragment {

//    @BindView(R.id.imv_login_logo)
//    ImageView imvLoginLogo;
    @BindView(R.id.btn_to_login)
    TextView btnToLogin;
    @BindView(R.id.btn_to_register)
    TextView btnToRegister;
    @BindView(R.id.btn_look_around)
    TextView btnLookAround;
//    @BindView(R.id.rLayout_before_login)
//    RelativeLayout rLayoutBeforeLogin;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash_before_login, container, false);

        ButterKnife.bind(this, view);
        return view;
    }

    public static BeforeLoginFragment newInstance(Bundle bundle) {
        BeforeLoginFragment fragment = new BeforeLoginFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        view.setOnKeyListener((v, keyCode, event) -> {
            Log.d("", "");
            return false;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (RxBus.getCacheInstance().hasStickyEvent(RxEvent.InitFrom2x.class)) {
            if (AutoSignIn.getInstance().isNotEmpty()) {
                AutoSignIn.getInstance().autoLogin();
                btnToLogin.setEnabled(false);
                btnToRegister.setEnabled(false);
                btnLookAround.setEnabled(false);
            }
        }
    }

    @OnClick(R.id.btn_look_around)
    public void toLookAround(View view) {
        BaseApplication.getAppComponent().getSourceManager().setLoginState(new LogState(STATE_GUEST));
        BaseApplication.getAppComponent().getSourceManager().clear();
        if (getView() != null)
            ViewUtils.deBounceClick(getView().findViewById(R.id.btn_look_around));
//        clearChildren();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getActivity().startActivity(new Intent(getActivity(), NewHomeActivity.class),
                    ActivityOptionsCompat.makeCustomAnimation(getContext(), R.anim.activity_fade_in, R.anim.alpha_out).toBundle());
        } else {
            getActivity().startActivity(new Intent(getActivity(), NewHomeActivity.class));
        }
        getActivity().finish();
    }

    @OnClick(R.id.btn_to_login)
    public void toLogin(View view) {
        if (getView() != null)
            ViewUtils.deBounceClick(getView().findViewById(R.id.btn_to_login));
        Bundle bundle = new Bundle();
        bundle.putBoolean(JConstant.KEY_SHOW_LOGIN_FRAGMENT_EXTRA, true);
        ((NeedLoginActivity) getActivity()).signInFirst(bundle);
    }

    @OnClick(R.id.btn_to_register)
    public void toRegister(View view) {
        if (getView() != null)
            ViewUtils.deBounceClick(getView().findViewById(R.id.btn_to_register));
        Bundle bundle = new Bundle();
        bundle.putString("show_login_fragment", "show_login_fragment");
        bundle.putBoolean(JConstant.KEY_SHOW_LOGIN_FRAGMENT_EXTRA, true);
        bundle.putBoolean(JConstant.OPEN_LOGIN_TO_BIND_PHONE, false);
        ((NeedLoginActivity) getActivity()).signInFirst(bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
