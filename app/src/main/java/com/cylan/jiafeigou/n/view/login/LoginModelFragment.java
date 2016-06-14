package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

import java.util.TimeZone;

/**
 * 登陆模块通用Fragment
 * Created by lxh on 16-6-13.
 */

public class LoginModelFragment extends Fragment {


    /**
     * 设置按钮的状态
     *
     * @param enable 是否可以点击
     * @param view   控件，可以是textview,btn 之类
     */
    public void setViewEnableStyle(TextView view, boolean enable) {
        if (enable == view.isEnabled()) return;
        view.setEnabled(enable);
        view.setTextColor(getResources().getColor(enable ?
                R.color.color_4b9fd5 : R.color.color_d8d8d8));
    }


    /**
     * 获取时区
     *
     * @return
     */
    public String getTimeZone() {
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
    }


    /**
     * 用来点击空白处隐藏键盘
     *
     * @param view
     */
    public void addOnTouchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (getActivity().getCurrentFocus() != null && getActivity().getCurrentFocus().getWindowToken() != null) {
                        manager.hideSoftInputFromWindow(
                                getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }
                return false;
            }
        });
    }


    /**
     * 显示密码
     *
     * @param et
     * @param isShow
     */
    public void showPwd(EditText et, boolean isShow) {
        et.setTransformationMethod(isShow ?
                HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
    }

}
