package com.cylan.jiafeigou.n.view.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 登陆之前的界面，可以选择登陆，或者随便看看
 * Created by lxh on 16-6-12.
 */

public class BeforeLoginFragment extends android.support.v4.app.Fragment {

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
        RxBus.getInstance().send(new RxEvent.NeedLoginEvent(null));
    }

    /**
     * 清空所有多余的View，会有一个白色的窗口期。
     */
    private void clearChildren() {
        Activity activity = getActivity();
        if (activity != null) {
            ViewGroup v = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
            ViewGroup group = (ViewGroup) v.findViewById(android.R.id.content);
            if (group != null) {
                group.removeAllViews();
            }
        }
    }

}
