package com.cylan.jiafeigou.n.view.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.NewHomeActivity;
import com.cylan.jiafeigou.n.view.login_ex.LoginContainerFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 登陆之前的界面，可以选择登陆，或者随便看看
 * Created by lxh on 16-6-12.
 */

public class BeforeLoginFragment extends Fragment {

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

    @OnClick(R.id.btn_look_around)
    public void toLookAround(View view) {
        //start home activity
        disableView(R.id.btn_look_around, R.id.btn_to_login);
        getContext().startActivity(new Intent(getContext(), NewHomeActivity.class));
        getActivity().finish();
    }

    @OnClick(R.id.btn_to_login)
    public void toLogin(View view) {
        //start login loginModelActivity
        disableView(R.id.btn_look_around, R.id.btn_to_login);
        getChildFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out)
                .replace(R.id.rLayout_before_login, LoginContainerFragment.newInstance(""))
                .commit();
    }

    private void disableView(int... id) {
        if (id != null && getView() != null)
            for (int i : id) {
                View v = getView().findViewById(i);
                if (v != null) v.setVisibility(View.GONE);
            }
    }
}
