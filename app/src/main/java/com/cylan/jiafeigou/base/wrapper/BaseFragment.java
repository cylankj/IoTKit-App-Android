package com.cylan.jiafeigou.base.wrapper;

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
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.ButterKnife;

/**
 * Created by yzd on 16-12-28.
 */

public abstract class BaseFragment<P extends JFGPresenter> extends Fragment implements JFGView {
    protected P mPresenter;

    protected String mUUID;

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
        if (getArguments() != null) {
            mUUID = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);//在基類裏獲取uuid,便於統一管理
            if (mUUID == null) mUUID = "300000008496";
        }
        mPresenter = onCreatePresenter();
        if (mPresenter != null) {
            mPresenter.onSetViewUUID(mUUID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(getContentViewID(), container, false);
        ButterKnife.bind(this, contentView);
        return contentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewAndListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPresenter != null) {
            mPresenter.onSetContentView();//有些view会根据一定的条件显示不同的view,可以在这个方法中进行条件判断
        }
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

    protected abstract P onCreatePresenter();

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

    protected void onBackPressed() {
    }

    protected CallBack callBack;

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
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
        showToast("还未登录");
    }

    /**
     * 一个回调接口,可以向view中传递数据
     */
    public void onViewAction(int action, String handler, Object extra) {
        if (mPresenter != null) {
            mPresenter.onViewAction(action, handler, extra);
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
