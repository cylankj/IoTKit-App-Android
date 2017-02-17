package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

import static com.cylan.jiafeigou.utils.ViewUtils.getCompatStatusBarHeight;

/**
 * Created by cylan-hunt on 17-2-12.
 */

public class CustomToolbar extends LinearLayout {

    TextView tvToolbarIcon;
    TextView tvToolbarTitle;
    TextView tvToolbarRight;
    ViewGroup viewGroup;
    private boolean fitSystemWindow;

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
        int rightTitleColor = at.getColor(R.styleable.CustomToolbarStyle_ct_right_title_color, Color.TRANSPARENT);
        String title = at.getString(R.styleable.CustomToolbarStyle_ct_title);
        String leftTitle = at.getString(R.styleable.CustomToolbarStyle_ct_left_title);
        String rightTitle = at.getString(R.styleable.CustomToolbarStyle_ct_right_title);
        int iconResIdLeft = at.getResourceId(R.styleable.CustomToolbarStyle_ct_icon, -1);
        int iconResIdRight = at.getResourceId(R.styleable.CustomToolbarStyle_ct_icon_right, -1);
        boolean showShadow = at.getBoolean(R.styleable.CustomToolbarStyle_ct_enable_shadow, false);
        fitSystemWindow = at.getBoolean(R.styleable.CustomToolbarStyle_ct_fit_system_window, true);
        at.recycle();
        View view = LayoutInflater.from(context).inflate(R.layout.layout_custom_tool_bar, this, true);
        viewGroup = (ViewGroup) findViewById(R.id.fLayout_toolbar_content);
        if (bgColor != 0)
            viewGroup.setBackgroundColor(bgColor);
        tvToolbarIcon = (TextView) view.findViewById(R.id.tv_toolbar_icon);
        tvToolbarTitle = (TextView) view.findViewById(R.id.tv_toolbar_title);
        tvToolbarRight = (TextView) view.findViewById(R.id.tv_toolbar_right);
        if (rightTitleColor != 0) tvToolbarTitle.setTextColor(rightTitleColor);
        tvToolbarTitle.setVisibility(VISIBLE);
        tvToolbarTitle.setText(title);
        if (titleColor != 0)
            tvToolbarTitle.setTextColor(titleColor);
        if (iconResIdLeft != -1) {
            ViewUtils.setDrawablePadding(tvToolbarIcon, iconResIdLeft, 0);
        }
        if (!TextUtils.isEmpty(rightTitle)) {
            tvToolbarRight.setVisibility(VISIBLE);
            tvToolbarRight.setText(rightTitle);
        }
        if (iconResIdRight != -1) {
            tvToolbarRight.setVisibility(VISIBLE);
            ViewUtils.setDrawablePadding(tvToolbarRight, iconResIdRight, 0);
        }
        tvToolbarIcon.setText(leftTitle);
        if (leftTitleColor != 0)
            tvToolbarIcon.setTextColor(leftTitleColor);
        if (showShadow) {
            findViewById(R.id.v_shadow).setVisibility(VISIBLE);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (fitSystemWindow) {
            final int height = getCompatStatusBarHeight(getContext());
            viewGroup.setPadding(viewGroup.getPaddingLeft(),
                    viewGroup.getPaddingTop() + height,
                    viewGroup.getPaddingRight(),
                    viewGroup.getPaddingBottom());
        }
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

    public CharSequence getTitle() {
        return tvToolbarTitle.getText();
    }

    public CharSequence getSubTitle() {
        return tvToolbarRight.getText();
    }

    public TextView getTvToolbarRight() {
        return tvToolbarRight;
    }
}
