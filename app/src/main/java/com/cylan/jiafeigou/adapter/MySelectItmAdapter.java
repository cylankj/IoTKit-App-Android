package com.cylan.jiafeigou.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.entity.SelectItem;

public class MySelectItmAdapter extends ArrayAdapter<SelectItem> {
	public int index = -1;

	private boolean isRepeatSelect = false;

	public MySelectItmAdapter(Context context) {
		super(context, 0);
	}

	public void setIsRepeatSelect(boolean boo) {
		isRepeatSelect = boo;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = View.inflate(getContext(), R.layout.single_text_list_item, null);
		}
		SelectItem item = getItem(position);
		((TextView) convertView).setText(TextUtils.isEmpty(item.diaplay) ? item.value : item.value + " " + item.diaplay);

		if (!isRepeatSelect) {
			if (index != -1 && index == position) {
				((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ico_wifi_selected, 0);
			} else {
				item.isSelected = false;
				((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
		} else {
			if (item.isSelected) {
				((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ico_wifi_selected, 0);
			} else {
				((TextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
		}
		return convertView;
	}
}
