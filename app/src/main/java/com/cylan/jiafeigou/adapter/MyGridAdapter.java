package com.cylan.jiafeigou.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;

public class MyGridAdapter extends BaseAdapter {

	private int pos = -1;
	private Context mContext;
	private int[] covers;

	public MyGridAdapter(Context ctx, int[] covers) {
		this.mContext = ctx;
		this.covers = covers;
	}

	@Override
	public int getCount() {
		return covers.length;
	}

	@Override
	public Object getItem(int position) {

		return position;
	}

	public void setPosition(int position) {
		pos = position;
	}

	public int getPosition() {
		return pos;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder vh;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.cover_grid_item, null);
			vh = new ViewHolder();
			convertView.setTag(vh);
			vh.cover = (ImageView) convertView.findViewById(R.id.grid_item_pic);
			vh.check = (ImageView) convertView.findViewById(R.id.grid_item_checked);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		vh.cover.setImageResource(covers[position]);
		if (pos - 1 == position) {
			vh.check.setVisibility(View.VISIBLE);
		} else {
			vh.check.setVisibility(View.GONE);
		}

		return convertView;
	}

	private class ViewHolder {
		ImageView cover;
		ImageView check;
	}

}
