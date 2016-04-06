package com.cylan.jiafeigou.widget.wheel.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * Created by hebin on 2015/7/23.
 */
public class MyNumercWheelAdapter extends NumericWheelAdapter {

    private Context mContext;
    private int current;

    public MyNumercWheelAdapter(Context context, int minValue, int maxValue, String format) {
        super(context, minValue, maxValue, format);
        this.mContext = context;
        setItemResource(R.layout.item_wheel);
    }

    public MyNumercWheelAdapter(Context context, int minValue, int maxValue) {
        super(context, minValue, maxValue);
        this.mContext = context;
        setItemResource(R.layout.item_wheel);
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    @Override
    public View getItem(int index, View convertView, ViewGroup parent) {
        convertView = super.getItem(index, convertView, parent);
        if (index == current) {
            ((TextView) convertView).setTextColor(mContext.getResources().getColor(R.color.current_wheel_color));
        } else {
            ((TextView) convertView).setTextColor(mContext.getResources().getColor(R.color.default_wheel_color));
        }
        return convertView;
    }

    public void notifyDataSetChanged() {
        notifyDataChangedEvent();
    }
}
