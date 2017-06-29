package com.cylan.jiafeigou.base.wrapper;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.cylan.jfgapp.interfases.AppCmd;
import com.cylan.jiafeigou.base.injector.component.DaggerFragmentComponent;
import com.cylan.jiafeigou.base.injector.component.FragmentComponent;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.widget.LoadingDialog;

import javax.inject.Inject;

import butterknife.ButterKnife;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseFragment<P extends JFGPresenter> extends Fragment implements JFGView, View.OnKeyListener {
    @Inject
    protected P presenter;
    @Inject
    protected JFGSourceManager sourceManager;
    @Inject
    protected AppCmd appCmd;
    protected String mUUID;
    protected AlertDialog alert;
    protected static Toast sToast;

    protected FragmentComponent component;

    @Override
    public Context getAppContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public Activity getActivityContext() {
        return getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUUID = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);//在基類裏獲取uuid,便於統一管理
        }
        if (presenter != null) {
            presenter.onSetViewUUID(mUUID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = null;
        if (getContentViewID() != -1) {//!=-1 会启动 butterknife ,==-1:自己设置 view, 可以使用 databinding
            contentView = inflater.inflate(getContentViewID(), container, false);
            ButterKnife.bind(this, contentView);
        } else if (getContentRootView() != null) {
            contentView = getContentRootView();
        }
        if (contentView != null) {
            contentView.setOnKeyListener(this);
        }
        return contentView;
    }

    protected View getContentRootView() {
        return null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.component = DaggerFragmentComponent.builder().appComponent(BaseApplication.getAppComponent()).build();
        if (this.component != null) {
            setFragmentComponent(component);
        }
        if (presenter != null) {
            presenter.onViewAttached(this);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (presenter != null) {
            presenter.onViewDetached();
        }
    }

    protected abstract void setFragmentComponent(FragmentComponent fragmentComponent);

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (presenter != null) {
            presenter.onSetContentView();//有些view会根据一定的条件显示不同的view,可以在这个方法中进行条件判断
        }
        initViewAndListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.onStart();
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter || nextAnim == 0) return null;

        final Animation animator = AnimationUtils.loadAnimation(getActivityContext(), nextAnim);
        animator.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animation.setAnimationListener(null);
                onEnterAnimationFinished();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        return animator;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.onStop();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
//            presenter.onViewDetached();
        }
    }

    @Override
    public void showLoading(int resId, String... args) {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(resId, args));
    }

    @Override
    public void hideLoading() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    @Override
    public AlertDialog getAlert() {
        if (alert != null) {
            alert.dismiss();
            alert = new AlertDialog.Builder(getContext()).create();
        }
        return alert;
    }

    @Override
    public void showToast(String msg) {
        if (sToast == null) {
            sToast = Toast.makeText(getActivity().getApplicationContext(), "", Toast.LENGTH_SHORT);
        }
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setText(msg);
        sToast.show();
    }

    protected int getContentViewID() {
        return -1;
    }

    protected void initViewAndListener() {
    }

    @Override
    public void onScreenRotationChanged(boolean land) {

    }

    protected boolean onBackPressed() {
        return false;
    }

    protected CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }


    protected void onEnterAnimationFinished() {

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && onBackPressed();
    }


    public interface CallBack {
        void callBack(Object t);
    }

    @Override
    public String onResolveViewLaunchType() {
        return getArguments().getString(JConstant.VIEW_CALL_WAY);
    }

    @Override
    public void onLoginStateChanged(boolean online) {
//        ToastUtil.showNegativeToast(getString(R.string.UNLOGIN));
    }

    /**
     * 一个回调接口,可以向view中传递数据
     */
    public void onViewAction(int action, String handler, Object extra) {
        if (presenter != null) {
            presenter.onViewAction(action, handler, extra);
        }
    }

    /**
     * fragment回调activity的方法,可以通过此方法向activity传递信息
     */
    protected void onViewActionToActivity(int action, String handler, Object extra) {
        if (getActivity() != null && getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).onViewAction(action, handler, extra);
        }
    }
}
