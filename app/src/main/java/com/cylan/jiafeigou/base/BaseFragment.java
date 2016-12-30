package com.cylan.jiafeigou.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.ButterKnife;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseFragment<T extends JFGPresenter> extends Fragment implements JFGView {
    protected T mPresenter;

    private static Toast sToast;

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
        if (mPresenter == null) {
            mPresenter = onCreatePresenter();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(getContentViewID(), container, false);
        ButterKnife.bind(this, contentView);
        initViewAndListener();
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mPresenter != null) {
            mPresenter.onViewAttached(this);
            mPresenter.onStart();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.onStop();
            mPresenter.onViewDetached();
        }
    }


    protected abstract T onCreatePresenter();

    @Override
    public void showLoading() {
        showLoadingMsg(getResources().getString(R.string.LOADING));
    }


    @Override
    public void showLoadingMsg(String msg) {
        LoadingDialog.dismissLoading(getChildFragmentManager());//以后有时间定义一个统一的样式
    }

    @Override
    public String showAlert(String title, String msg, String ok, String cancel) {
        return null;
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

    protected abstract int getContentViewID();

    protected void initViewAndListener() {
    }

    @Override
    public void onScreenRotationChanged(boolean land) {

    }
}
