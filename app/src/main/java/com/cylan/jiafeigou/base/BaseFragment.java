package com.cylan.jiafeigou.base;

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

public abstract class BaseFragment<T extends JFGPresenter<V>, V extends JFGView> extends Fragment implements JFGView {
    protected T mPresenter;

    private static Toast sToast;

    @Override
    public Context getViewContext() {
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
            mPresenter.onViewAttached((V) this);
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
        showLoadingMsg(getResources().getString(R.string.Msg_LoadDialog));
    }


    @Override
    public void showLoadingMsg(String msg) {
        LoadingDialog.dismissLoading(getChildFragmentManager());//以后有时间定义一个统一的样式
    }

    @Override
    public void showAlert(String title, String msg, String ok, String cancel) {

    }

    @Override
    public void showToast(String msg) {
        if (sToast == null) {
            sToast = new Toast(getActivity());
        }
        sToast.setDuration(Toast.LENGTH_SHORT);
        sToast.setText(msg);
        sToast.show();
    }

    protected abstract int getContentViewID();

    protected void initViewAndListener() {
    }
}
