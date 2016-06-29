package com.cylan.jiafeigou.n.view.login;

import android.support.v4.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.IMEUtils;

import java.util.TimeZone;

/**
 * Created by lxh on 16-6-17.
 */
public class LoginBaseFragment extends Fragment {


    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

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
     * 是否在中国
     *
     * @return
     */
    public boolean inChina() {
        // 方法A：地区（中国）或时区（中国或东八区）
        // 方法B：语言（简体中文） 选此方法
        return getResources().getConfiguration().locale.getLanguage().equals("zh");
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
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    IMEUtils.hide(getActivity());
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
