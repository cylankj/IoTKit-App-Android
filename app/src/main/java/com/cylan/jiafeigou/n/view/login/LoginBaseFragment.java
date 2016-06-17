package com.cylan.jiafeigou.n.view.login;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

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

    /**
     * 限制输入字符数
     *
     * @param editText 输入的控件
     * @param count    字符数
     */
    public void editTextLimitMaxInput(EditText editText, final int count) {

        editText.setFilters(new InputFilter[]{new InputFilter() {

            @Override

            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                return dest.toString().length() >= count ? "" : source;
            }

        }});
    }
}
