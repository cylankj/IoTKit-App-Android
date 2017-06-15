package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * home mine 界面的item 组合view
 * Created by lxh on 16-5-30.
 */

public class HomeMineItemView extends RelativeLayout {

    ImageView iv;
    TextView tv;
    NumberBadge badge;

    public HomeMineItemView(Context context) {
        this(context, null);
    }

    public HomeMineItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeMineItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_home_mine_item, this);
        iv = (ImageView) findViewById(R.id.iv_home_mine_item_friend);
        tv = (TextView) findViewById(R.id.tv_home_mine_item_friend);
        badge = (NumberBadge) findViewById(R.id.number_badge);
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.HomeMineItemViewStyle, defStyleAttr, 0);
        Drawable leftDrawable = array.getDrawable(R.styleable.HomeMineItemViewStyle_leftDrawable);
        Drawable rightDrawable = array.getDrawable(R.styleable.HomeMineItemViewStyle_rightDrawable);
        final int textColor = array.getColor(R.styleable.HomeMineItemViewStyle_textColor, Color.WHITE);
        String str = array.getString(R.styleable.HomeMineItemViewStyle_text);
        tv.setText(str);
        tv.setTextColor(textColor);
        tv.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);
        iv.setImageDrawable(rightDrawable);
        array.recycle();
    }

    public void setRightImageViewDrawable(Drawable drawable) {
        iv.setImageDrawable(drawable);
        invalidate();
    }

    public void setLeftImageDrawable(Drawable drawable) {
        tv.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        invalidate();
    }

    public void setText(String str) {
        tv.setText(str);
        invalidate();
    }


    public void showNumber(int number) {
        badge.showNumber(number);
    }

    public void showHint(boolean show) {
        badge.showHint(show);
    }
}
