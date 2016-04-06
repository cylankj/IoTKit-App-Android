package com.cylan.jiafeigou.entity;

import android.view.View;

public class ViewContainer {

	private View mView;
	private int tag;

	public View getmView() {
		return mView;
	}

	public void setmView(View mView) {
		this.mView = mView;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public ViewContainer(View mView, int tag) {
		this.mView = mView;
		this.tag = tag;
	}

}
