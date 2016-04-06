package com.cylan.jiafeigou.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.entity.TimeZoneBean;

import java.util.List;

public class TimeZoneAdapter extends BaseAdapter<TimeZoneBean> {

    public int pos = -1;

    public TimeZoneAdapter(Activity activity, List<TimeZoneBean> list) {
        super(activity, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.timezone_item, null);
            vh = new ViewHolder();

            vh.mTimezoune = (TextView) convertView.findViewById(R.id.timezone);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        TimeZoneBean bean = getItem(position);
        vh.mTimezoune.setText(bean.getTimezonename().trim() + "(" + bean.getTimezone() + ")");
        if (pos == position) {
            vh.mTimezoune.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ico_wifi_selected, 0);
        } else {
            bean.setIsChecked(false);
            vh.mTimezoune.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        return convertView;
    }

    class ViewHolder {
        TextView mTimezoune;
    }
}
