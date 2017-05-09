package com.cylan.jiafeigou.n.view.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.GreatDragView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-14.
 */
public class GuideFragment extends Fragment implements GreatDragView.ViewDisappearListener {
    @BindView(R.id.v_great_drag)
    GreatDragView vGreatDrag;
    private Subscription resultSub;

    public static GuideFragment newInstance() {
        return new GuideFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_guide_view, container, false);
        ButterKnife.bind(this, view);
        PreferencesUtils.putBoolean(JConstant.KEY_FRESH, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vGreatDrag.setViewDisappearListener(this);
    }

    @Override
    public void onViewDisappear(View view, int index) {
        if (PreferencesUtils.getBoolean(JConstant.UPDATAE_AUTO_LOGIN, false) && index == 3) {
            AppLogger.d("updata_login");
            resultSub = RxBus.getCacheInstance().toObservableSticky(RxEvent.ResultUpdateLogin.class)
                    .subscribeOn(Schedulers.newThread())
                    .timeout(3, TimeUnit.SECONDS, rx.Observable.just(null)
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(o -> {
                                enterLoginPage();
                                return null;
                            }))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(rsp -> {
                        if (rsp != null && rsp.code == JError.ErrorOK) {
                            //首页
                            getContext().startActivity(new Intent(getContext(), NewHomeActivity.class));
                            getActivity().finish();
                        } else {
                            //登录页
                            enterLoginPage();
                        }
                        PreferencesUtils.putBoolean(JConstant.UPDATAE_AUTO_LOGIN, false);
                        AppLogger.d("updata_login:" + rsp.code);
                    }, AppLogger::e);
        } else {
            //进入登陆页 login page//这里要用replace
            if (index == 3)
                enterLoginPage();
        }
    }

    private void enterLoginPage() {
        int containerId = getArguments().getInt(JConstant.KEY_ACTIVITY_FRAGMENT_CONTAINER_ID, R.id.welcome_frame_container);
        if (getActivity() != null)
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(containerId, BeforeLoginFragment.newInstance(null))
                    .commitAllowingStateLoss();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (resultSub != null && !resultSub.isUnsubscribed()) {
            resultSub.unsubscribe();
        }
    }
}



