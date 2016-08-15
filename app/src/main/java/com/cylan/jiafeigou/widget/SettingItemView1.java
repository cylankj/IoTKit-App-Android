package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.kyleduo.switchbutton.SwitchButton;

/**
 * Created by cylan-hunt on 16-7-29.
 */
public class SettingItemView1 extends FrameLayout {

    /**
     * 主标题
     */
    TextView tvTitle;
    com.kyleduo.switchbutton.SwitchButton switchButton;

    public SettingItemView1(Context context) {
        this(context, null);
    }

    public SettingItemView1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView1(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingItemViewStyle);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_settings_item1, this, true);
        tvTitle = (TextView) view.findViewById(R.id.tv_settings_item_title);
        switchButton = (SwitchButton) view.findViewById(R.id.btn_item_switch);
        final String title = ta.getString(R.styleable.SettingItemViewStyle_sv_title);
        final Drawable srcId = ta.getDrawable(R.styleable.SettingItemViewStyle_sv_image_src);
        ViewUtils.setDrawablePadding(tvTitle, srcId, 0);
        tvTitle.setText(title);
        ta.recycle();
    }

    public void setTvTitle(CharSequence charSequence) {
        tvTitle.setText(charSequence);
    }

    public void setSwitchButtonState(boolean checked) {
        switchButton.setChecked(checked);
    }
}
