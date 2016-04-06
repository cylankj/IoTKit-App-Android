package com.cylan.jiafeigou.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.entity.MyScanResult;

public class WifiListAdapter extends ArrayAdapter<MyScanResult> {
    private Context mContext;

    public WifiListAdapter(Context context) {
        super(context, 0);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.search_wifi_result_list_item_zhongxing, null);

            vh = new ViewHolder();
            convertView.setTag(vh);
            vh.name = (TextView) convertView.findViewById(R.id.wifi_name);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        final MyScanResult info = getItem(position);
        if (info == null) {
            return convertView;
        }
        vh.name.setText(info.scanResult.SSID);

        return convertView;
    }

    class ViewHolder {
        TextView name;
    }
}
