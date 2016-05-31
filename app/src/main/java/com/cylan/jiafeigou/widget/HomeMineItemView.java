package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by lxh on 16-5-30.
 */

public class HomeMineItemView extends RelativeLayout {

    ImageView iv;
    TextView tv;

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
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.HomeMineItemViewStyle, defStyleAttr, 0);
        Drawable leftDrawable = array.getDrawable(R.styleable.HomeMineItemViewStyle_leftDrawable);
        Drawable rightDrawable = array.getDrawable(R.styleable.HomeMineItemViewStyle_rightDrawable);
//        int textSize = array.getDimensionPixelSize(R.styleable.HomeMineItemViewStyle_textSize, 16);
        String str = array.getString(R.styleable.HomeMineItemViewStyle_text);
        tv.setText(str);
//        tv.setTextSize(textSize);
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

}
