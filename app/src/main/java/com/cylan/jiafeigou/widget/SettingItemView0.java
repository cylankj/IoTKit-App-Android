package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by cylan-hunt on 16-7-29.
 */
public class SettingItemView0 extends LinearLayout {

    /**
     * 主标题
     */
    TextView tvTitle;
    /**
     * 副标题
     */
    TextView tvSubTitle;

    ImageView imgvIcon;

    public SettingItemView0(Context context) {
        this(context, null);
    }

    public SettingItemView0(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView0(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.HORIZONTAL);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SettingItemViewStyle);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_settings_item, this, true);
        tvTitle = (TextView) view.findViewById(R.id.tv_settings_item_title);
        tvSubTitle = (TextView) view.findViewById(R.id.tv_settings_item_sub_title);
        imgvIcon = (ImageView) view.findViewById(R.id.imgv_item_icon);
        final String title = ta.getString(R.styleable.SettingItemViewStyle_sv_title);
        final String subTitle = ta.getString(R.styleable.SettingItemViewStyle_sv_sub_title);
        final Drawable srcId = ta.getDrawable(R.styleable.SettingItemViewStyle_sv_image_src);
        imgvIcon.setImageDrawable(srcId);
        tvTitle.setText(title);
        tvSubTitle.setText(subTitle);
        ta.recycle();
    }

    /**
     * 设置副标题
     *
     * @param charSequence
     */
    public void setTvSubTitle(CharSequence charSequence) {
        tvSubTitle.setText(charSequence);
    }
}
