package com.cylan.jiafeigou.n.view.splash;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.OptionsImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    private Subscription subscription;

    @Override
    public void onResume() {
        super.onResume();
        if (RxBus.getCacheInstance().hasStickyEvent(RxEvent.InitFrom2x.class)) {
            if (AutoSignIn.getInstance().isNotEmpty()) {
                subscription = RxBus.getCacheInstance().toObservable(RxEvent.ResultLogin.class)
                        .subscribeOn(Schedulers.newThread())
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnError(throwable -> {
                            btnToLogin.setEnabled(true);
                            btnToRegister.setEnabled(true);
                            btnLookAround.setEnabled(true);
                            LoadingDialog.dismissLoading(getFragmentManager());
                        })
                        .subscribe(ret -> {
                            btnToLogin.setEnabled(true);
                            btnToRegister.setEnabled(true);
                            btnLookAround.setEnabled(true);
                            LoadingDialog.dismissLoading(getFragmentManager());
                            if (ret.code == JError.ErrorOK) {
                                startActivity(new Intent(getActivity(), NewHomeActivity.class));
                                getActivity().finish();
                                RxBus.getCacheInstance().removeStickyEvent(RxEvent.InitFrom2x.class);
                            }
                        }, AppLogger::e);
                AutoSignIn.getInstance().autoLogin();
                btnToLogin.setEnabled(false);
                btnToRegister.setEnabled(false);
                btnLookAround.setEnabled(false);
                LoadingDialog.showLoading(getFragmentManager(), getString(R.string.PLEASE_WAIT));
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

    @OnClick(R.id.rLayout_before_login)
    public void onClick(View v) {
        if (clickEvent == null) clickEvent = new Event();
        if (clickEvent.click(getContext())) {
            clickEvent = null;
            final EditText input = new EditText(getActivity());
            input.setText(OptionsImpl.getServer());
            input.setHint("服务器地址:yun.jfgou.com:443");
            input.setHintTextColor(getResources().getColor(R.color.color_888888));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivity());
            builder.setView(input);
            builder.setTitle("服务器地址");
            builder.setPositiveButton(getResources().getString(R.string.OK), (dialog, which) -> {
                if (!TextUtils.isEmpty(input.getText())) {
                    OptionsImpl.setServer(input.getText().toString().trim().toLowerCase());
                    Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
                    PendingIntent restartIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
                    AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 50, restartIntent);
                    v.postDelayed(() -> android.os.Process.killProcess(android.os.Process.myPid()), 500);

                }
            }).setNegativeButton(getString(R.string.CANCEL), null);
            builder.setCancelable(false);
            builder.show();
        }
    }

    private Event clickEvent;

    /**
     * 快速点击10次 弹出修改服务器域名
     */
    private static class Event {
        private int count;
        private long time;

        private boolean click(Context context) {
            if (time == 0) time = System.currentTimeMillis();
            if (System.currentTimeMillis() - time > 1000) {
                count = 0;
                time = 0;
            } else {
                time = System.currentTimeMillis();
                count++;
            }
            if (count > 9) {
                time = 0;
                count = 0;
                return true;
            }
            return false;
        }
    }
}
