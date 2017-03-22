package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ShareGridView extends GridView {

	public ShareGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);	
	}

	public ShareGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShareGridView(Context context) {
		super(context);
	}

	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

	}
}
