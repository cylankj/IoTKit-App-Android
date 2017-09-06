package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

/**
 * Created by cylan-hunt on 16-7-29.
 */
public class SettingItemView0 extends RelativeLayout {

    /**
     * 主标题
     */
    TextView tvTitle;
    /**
     * 副标题
     */
    TextView tvSubTitle;

    ImageView imgvIcon;
    ImageView imgvRedHint;

    SafeSwitchButton switchButton;
    View v_divider;
    RadioButton rbRadioButton;
    private FrameLayout optionContainer;

    public SettingItemView0(Context context) {
        this(context, null);
    }

    public SettingItemView0(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView0(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingItemViewStyle);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_settings_item0, this, true);
        tvTitle = (TextView) view.findViewById(R.id.tv_settings_item_title);
        tvSubTitle = (TextView) view.findViewById(R.id.tv_settings_item_sub_title);
        imgvIcon = (ImageView) view.findViewById(R.id.imgv_item_icon);
        imgvRedHint = (ImageView) view.findViewById(R.id.imv_item_red_hint);
        rbRadioButton = (RadioButton) view.findViewById(R.id.rb_item_radio_button);
        optionContainer = (FrameLayout) view.findViewById(R.id.option_container);

        imgvIcon.setVisibility(ta.getBoolean(R.styleable.SettingItemViewStyle_sv_v_image_show, false)
                ? VISIBLE : GONE);
        switchButton = (SafeSwitchButton) view.findViewById(R.id.btn_item_switch);
        final String title = ta.getString(R.styleable.SettingItemViewStyle_sv_title);
        final String subTitle = ta.getString(R.styleable.SettingItemViewStyle_sv_sub_title);
        final Drawable srcId = ta.getDrawable(R.styleable.SettingItemViewStyle_sv_image_src);
        boolean show_red_hint = ta.getBoolean(R.styleable.SettingItemViewStyle_sv_red_hint, false);
        imgvRedHint.setVisibility(show_red_hint ? VISIBLE : GONE);
        imgvIcon.setImageDrawable(srcId);
        tvTitle.setText(title);
        tvSubTitle.setText(subTitle);
        tvSubTitle.setVisibility(TextUtils.isEmpty(tvSubTitle.getText()) ? GONE : VISIBLE);
        int subTitleGravity = ta.getInt(R.styleable.SettingItemViewStyle_sv_sub_title_gravity, -1);
        tvSubTitle.setGravity(subTitleGravity);
        boolean switchVisibility = ta.getBoolean(R.styleable.SettingItemViewStyle_sv_switch_visibility, false);
        switchButton.setVisibility(switchVisibility ? VISIBLE : GONE);
        if (switchVisibility) rbRadioButton.setVisibility(GONE);
        v_divider = findViewById(R.id.v_divider);
        v_divider.setVisibility(ta.getBoolean(R.styleable.SettingItemViewStyle_sv_v_divider, false)
                ? VISIBLE : GONE);
        float d = ta.getDimension(R.styleable.SettingItemViewStyle_sv_title_paddingEnd, 0);
        ViewUtils.setMargins(tvSubTitle, 0, 0, (int) d, 0);
        boolean radioButtonVisibility = ta.getBoolean(R.styleable.SettingItemViewStyle_sv_show_radio_button, false);
        rbRadioButton.setVisibility(radioButtonVisibility ? VISIBLE : GONE);
        if (radioButtonVisibility) switchButton.setVisibility(GONE);
        int radioButtonId = ta.getResourceId(R.styleable.SettingItemViewStyle_sv_radio_button_id, -1);
        if (radioButtonId != -1) {
            rbRadioButton.setId(radioButtonId);
        }
        ta.recycle();
    }

    /**
     * 设置副标题
     *
     * @param charSequence
     */
    public void setTvSubTitle(CharSequence charSequence) {
        tvSubTitle.setVisibility(VISIBLE);
        tvSubTitle.setText(charSequence);
    }

    /**
     * 设置副标题
     *
     * @param charSequence
     */
    public void setTvSubTitle(CharSequence charSequence, @ColorRes int color) {
        tvSubTitle.setVisibility(VISIBLE);
        tvSubTitle.setTextColor(getResources().getColor(color));
        tvSubTitle.setText(charSequence);
    }

    public void setSwitchButtonState(boolean state) {
        switchButton.setChecked(state);
    }

    public void setSwitcherVisibility(int visibility) {
        switchButton.setVisibility(visibility);
    }

    public CharSequence getSubTitle() {
        return tvSubTitle.getText();
    }

    public CharSequence getTitle() {
        return tvTitle.getText();
    }

    public void showRedHint(boolean show) {
        imgvRedHint.setVisibility(show ? VISIBLE : GONE);
    }

    public void showDivider(boolean show) {
        v_divider.setVisibility(show ? VISIBLE : GONE);
    }

    public void setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener listener) {
//        if (listener != null) {
        switchButton.setOnCheckedChangeListener(listener);
//        }
    }

    public void setChecked(boolean checked) {
        switchButton.setChecked(checked, false);
    }

    public void setChecked(boolean checked, boolean toggle) {
        switchButton.setChecked(checked, toggle);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1.0f : 0.6f);
        switchButton.setEnabled(enabled);
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        tvTitle.setAlpha(alpha);
        tvSubTitle.setAlpha(alpha);
        imgvIcon.setAlpha(alpha);
        imgvRedHint.setAlpha(alpha);
    }

    public void setCheckEnable(boolean enable) {
        switchButton.setEnabled(enable);
    }

    public void setShowRadioButton(boolean show) {
        rbRadioButton.setVisibility(show ? VISIBLE : GONE);
    }

    public void setRadioButtonChecked(boolean checked) {
        rbRadioButton.setChecked(checked);
    }
}
