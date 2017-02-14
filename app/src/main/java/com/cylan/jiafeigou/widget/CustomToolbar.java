package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

/**
 * Created by cylan-hunt on 17-2-12.
 */

public class CustomToolbar extends FrameLayout {

    TextView tvToolbarIcon;
    TextView tvToolbarTitle;
    TextView tvToolbarRight;

    public CustomToolbar(Context context) {
        this(context, null);
    }

    public CustomToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray
                at = context.obtainStyledAttributes(attrs, R.styleable.CustomToolbarStyle);
        int bgColor = at.getColor(R.styleable.CustomToolbarStyle_ct_background_color, Color.TRANSPARENT);
        int titleColor = at.getColor(R.styleable.CustomToolbarStyle_ct_title_color, Color.TRANSPARENT);
        int leftTitleColor = at.getColor(R.styleable.CustomToolbarStyle_ct_left_title_color, Color.TRANSPARENT);
        String title = at.getString(R.styleable.CustomToolbarStyle_ct_title);
        String leftTitle = at.getString(R.styleable.CustomToolbarStyle_ct_left_title);
        int iconResId = at.getResourceId(R.styleable.CustomToolbarStyle_ct_icon, -1);
        at.recycle();
        setBackgroundColor(bgColor);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_custom_tool_bar, this, true);
        tvToolbarIcon = (TextView) view.findViewById(R.id.tv_toolbar_icon);
        tvToolbarTitle = (TextView) view.findViewById(R.id.tv_toolbar_title);
        tvToolbarRight = (TextView) view.findViewById(R.id.tv_toolbar_right);
        tvToolbarTitle.setText(title);
        tvToolbarTitle.setTextColor(titleColor);
        if (iconResId != -1) {
            ViewUtils.setDrawablePadding(tvToolbarIcon, iconResId, 0);
        }
        tvToolbarIcon.setText(leftTitle);
        tvToolbarIcon.setTextColor(leftTitleColor);
    }

    public void setBackAction(OnClickListener clickListener) {
        if (clickListener != null) {
            tvToolbarIcon.setOnClickListener(clickListener);
        }
    }

    public void setTvToolbarIcon(int resId) {
        ViewUtils.setDrawablePadding(tvToolbarIcon, resId, 2);
    }

    public void setToolbarTitle(int resId) {
        if (!tvToolbarTitle.isShown()) tvToolbarTitle.setVisibility(View.VISIBLE);
        tvToolbarTitle.setText(resId);
    }

    public void setToolbarLeftTitle(int resId) {
        if (!tvToolbarIcon.isShown()) tvToolbarIcon.setVisibility(View.VISIBLE);
        tvToolbarIcon.setText(resId);
    }

    public void setToolbarRightTitle(int resId) {
        if (!tvToolbarRight.isShown()) tvToolbarRight.setVisibility(View.VISIBLE);
        tvToolbarRight.setText(resId);
    }

}
