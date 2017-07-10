package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
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

public class CustomToolbar extends LinearLayout implements ITheme {

    private TextView tvToolbarIcon;
    private TextView tvToolbarTitle;
    private HintTextView tvToolbarRight;
    private ViewGroup viewGroup;
    @LayoutRes
    private int customContentLayoutId = -1;
    private boolean fitSystemWindow;

    public CustomToolbar(Context context) {
        this(context, null);
    }

    public CustomToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        TypedArray at = context.obtainStyledAttributes(attrs, R.styleable.CustomToolbar);
        final String value = attrs.getAttributeValue(null, "layout");
        if (value != null && value.length() >= 8) {
            customContentLayoutId = context.getResources().getIdentifier(value.substring(8), "layout",
                    context.getPackageName());
        }
        int bgColor = at.getColor(R.styleable.CustomToolbar_ct_background_color, Color.TRANSPARENT);
        int titleColor = at.getColor(R.styleable.CustomToolbar_ct_title_color, Color.TRANSPARENT);
        int leftTitleColor = at.getColor(R.styleable.CustomToolbar_ct_left_title_color, Color.TRANSPARENT);
        ColorStateList rightTitleColor = at.getColorStateList(R.styleable.CustomToolbar_ct_right_title_color);
        String title = at.getString(R.styleable.CustomToolbar_ct_title);
        String leftTitle = at.getString(R.styleable.CustomToolbar_ct_left_title);
        String rightTitle = at.getString(R.styleable.CustomToolbar_ct_right_title);
        int iconResIdLeft = at.getResourceId(R.styleable.CustomToolbar_ct_icon, -1);
        int iconResIdRight = at.getResourceId(R.styleable.CustomToolbar_ct_icon_right, -1);
        boolean showShadow = at.getBoolean(R.styleable.CustomToolbar_ct_enable_shadow, false);
        boolean enableTheme = at.getBoolean(R.styleable.CustomToolbar_ct_enable_theme, false);
        int leftTextSize = at.getDimensionPixelSize(R.styleable.CustomToolbar_ct_left_title_size, -1);

        fitSystemWindow = at.getBoolean(R.styleable.CustomToolbar_ct_fit_system_window, true);
        at.recycle();
        View view = LayoutInflater.from(context).inflate(R.layout.layout_custom_tool_bar, this, true);
        if (showShadow) {
            findViewById(R.id.v_shadow).setVisibility(VISIBLE);
        }
        viewGroup = (ViewGroup) findViewById(R.id.fLayout_toolbar_content);
        if (enableTheme) {
            if (isInEditMode()) {
                bgColor = R.color.color_0ba8cf;//这是方便在 xml 布局中显示
            } else {
                bgColor = ToolbarTheme.getInstance().getCurrentTheme().getToolbarBackground() == 0 ? R.color.color_17AFD1 : R.color.color_263954;
            }
            viewGroup.setBackgroundColor(getResources().getColor(bgColor));
        }
        if (customContentLayoutId != -1) {
            View viewChild = LayoutInflater.from(context).inflate(customContentLayoutId, this, false);
            viewGroup.addView(viewChild);
        }
        //处理默认布局
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            tvToolbarIcon = (TextView) view.findViewById(R.id.tv_toolbar_icon);
            tvToolbarTitle = (TextView) view.findViewById(R.id.tv_toolbar_title);
            tvToolbarRight = (HintTextView) view.findViewById(R.id.tv_toolbar_right);
            if (rightTitleColor != null) tvToolbarRight.setTextColor(rightTitleColor);
            tvToolbarTitle.setVisibility(VISIBLE);
            tvToolbarTitle.setText(title);
            if (titleColor != 0)
                tvToolbarTitle.setTextColor(titleColor);
            if (iconResIdLeft != -1) {
                ViewUtils.setDrawablePadding(tvToolbarIcon, iconResIdLeft, 0);
            }
            if (leftTextSize != -1) {
                tvToolbarIcon.setTextSize(TypedValue.COMPLEX_UNIT_PX, leftTextSize);
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
        }
    }

    /**
     * 使用这个方法后，原来设置过的，title icon均失效。
     *
     * @param viewFactory
     */
    public void setViewFactory(ToolbarFactory viewFactory) {
        this.toolbarFactory = viewFactory;
        if (viewFactory != null) {
            viewGroup.removeAllViews();
            viewGroup.addView(viewFactory.createView());
            viewGroup.setBackgroundColor(viewFactory.getToolbarColor());
        }
    }

    private ToolbarFactory toolbarFactory;

    @Override
    public void onNextTheme(@ColorInt int color) {
        viewGroup.setBackgroundColor(color);
    }

    /**
     * toolbar自定义view
     */
    public interface ToolbarFactory {
        View createView();

        boolean showShadow();

        boolean gitSystemWindow();

        int getToolbarColor();
    }

    public static abstract class SimpleFactory implements ToolbarFactory {

        @Override
        public boolean showShadow() {
            return false;
        }

        @Override
        public boolean gitSystemWindow() {
            return false;
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
        if (clickListener != null && customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            tvToolbarIcon.setOnClickListener(clickListener);
        }
    }

    public void setRightAction(OnClickListener clickListener) {
        if (clickListener != null && customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            tvToolbarRight.setOnClickListener(clickListener);
        }
    }

    public void setRightEnable(boolean enable) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            tvToolbarRight.setEnabled(enable);
        }
    }

    public void setRightVisibility(int visibility) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            tvToolbarRight.setVisibility(visibility);
        }
    }

    public void setTvToolbarIcon(int resId) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar)
            ViewUtils.setDrawablePadding(tvToolbarIcon, resId, 2);
    }

    public void setTvToolbarRightIcon(int resId) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar)
            ViewUtils.setDrawablePadding(tvToolbarRight, resId, 2);
    }

    public void setToolbarTitle(int resId) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            if (!tvToolbarTitle.isShown()) tvToolbarTitle.setVisibility(View.VISIBLE);
            tvToolbarTitle.setText(resId);
        }
    }

    public void setToolbarTitle(String resContent) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            if (!tvToolbarTitle.isShown()) tvToolbarTitle.setVisibility(View.VISIBLE);
            tvToolbarTitle.setText(resContent);
        }
    }

    public void setToolbarLeftTitle(int resId) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            if (!tvToolbarIcon.isShown()) tvToolbarIcon.setVisibility(View.VISIBLE);
            tvToolbarIcon.setText(resId);
        }
    }

    public void setToolbarLeftTitle(String resId) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            if (!tvToolbarIcon.isShown()) tvToolbarIcon.setVisibility(View.VISIBLE);
            tvToolbarIcon.setText(resId);
        }
    }

    public void setToolbarRightTitle(int resId) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            if (!tvToolbarRight.isShown()) tvToolbarRight.setVisibility(View.VISIBLE);
            tvToolbarRight.setText(resId);
        }
    }

    public void setToolbarRightTitle(String resId) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            if (!tvToolbarRight.isShown()) tvToolbarRight.setVisibility(View.VISIBLE);
            tvToolbarRight.setText(resId);
        }
    }

    public void showToolbarRightHint(boolean show) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            tvToolbarRight.showHint(show);
        }
    }

    public void setTvToolbarRightColor(String color) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            if (!tvToolbarRight.isShown()) tvToolbarRight.setVisibility(View.VISIBLE);
            tvToolbarRight.setTextColor(Color.parseColor(color));
        }
    }

    public void setToolbarRightColor(int resId) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            if (!tvToolbarRight.isShown()) tvToolbarRight.setVisibility(View.VISIBLE);
            tvToolbarRight.setTextColor(getResources().getColorStateList(resId));
        }
    }

    public void setTvToolbarRightEnable(boolean isEnable) {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            tvToolbarRight.setEnabled(isEnable);
        }
    }

    public CharSequence getTitle() {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            return tvToolbarTitle.getText();
        }
        return null;
    }

    public CharSequence getSubTitle() {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            return tvToolbarRight.getText();
        }
        return null;
    }

    public TextView getTvToolbarRight() {
        if (customContentLayoutId == R.layout.layout_default_custom_tool_bar) {
            return tvToolbarRight;
        }
        return null;
    }

    public void setLeftTextSize(int textSize) {
        tvToolbarIcon.setTextSize(textSize);
    }
}
