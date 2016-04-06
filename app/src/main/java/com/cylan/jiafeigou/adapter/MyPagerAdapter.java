package com.cylan.jiafeigou.adapter;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MyPagerAdapter extends PagerAdapter {
	public List<View> mListViews;
	
	public MyPagerAdapter(List<View> mListViews) {
		this.mListViews = mListViews;
	}

	public MyPagerAdapter(List<View> mNewsListViewList, int k) {
		this.mListViews = new ArrayList<View>();
		for (int i = 0; i < mNewsListViewList.size(); i++) {
			mListViews.add(mNewsListViewList.get(i));
		}
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView(mListViews.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public int getCount() {
		return mListViews.size();
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(mListViews.get(arg1), 0);
		return mListViews.get(arg1);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}
}