package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

/**
 * Created by cylan-hunt on 16-7-29.
 */
public class SettingItemView1 extends FrameLayout {

    /**
     * 主标题
     */
    TextView tvTitle;
    SafeSwitchButton switchButton;

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
        switchButton = (SafeSwitchButton) view.findViewById(R.id.btn_item_switch);
        switchButton.setVisibility(ta.getBoolean(R.styleable.SettingItemViewStyle_sv_switch_visibility, true)
                ? VISIBLE : GONE);
        final String title = ta.getString(R.styleable.SettingItemViewStyle_sv_title);
        final Drawable srcId = ta.getDrawable(R.styleable.SettingItemViewStyle_sv_image_src);
        ViewUtils.setDrawablePadding(tvTitle, srcId, 0);
        tvTitle.setText(title);
        findViewById(R.id.v_divider).setVisibility(ta.getBoolean(R.styleable.SettingItemViewStyle_sv_v_divider, false)
                ? VISIBLE : GONE);
        ta.recycle();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1.0f : 0.6f);
        switchButton.setEnabled(enabled);
    }

    public void setTvTitle(CharSequence charSequence) {
        tvTitle.setText(charSequence);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
        if (listener != null) {
            switchButton.setOnCheckedChangeListener(listener);
        }
    }

    public void setChecked(boolean checked) {
        switchButton.setChecked(checked, false);
    }

    public void setChecked(boolean checked, boolean toggle) {
        switchButton.setChecked(checked, toggle);
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        tvTitle.setAlpha(alpha);
    }
}
