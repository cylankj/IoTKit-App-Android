package com.cylan.jiafeigou.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.entity.msg.MagStatusList;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by yangc on 2015/12/16.
 *
 */
public class MagneticAdapter extends BaseAdapter<MagStatusList>{

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
    private SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public MagneticAdapter(Activity activity, List<MagStatusList> list) {
        super(activity, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder = null;
        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.magnetic_list_item, null);
            mHolder = new ViewHolder(convertView);
            convertView.setTag(mHolder);
        }else {
            mHolder = (ViewHolder) convertView.getTag();
            mHolder.horView.setVisibility(View.VISIBLE);
        }
        final MagStatusList magData = getItem(position);
        String currentDate = getDate(magData.time);
        if (currentDate.compareTo(getNowDay()) == 0){
            mHolder.mData.setText(R.string.DOOR_TODAY);
            mHolder.txtMonth.setVisibility(View.GONE);
        }else {
            String[] spilt = currentDate.split("/");
            mHolder.mData.setText(spilt[0] + "/");
            mHolder.txtMonth.setText(spilt[1] + mContext.getString(R.string.MONTHS));
        }
        mHolder.mTime.setText(mSimpleTimeFormat.format(new Date(magData.time * 1000)));
        if (magData.status == 0){
            if (position == 0){
                mHolder.imgStatus.setImageResource(R.drawable.ic_door_close_big);
            }else {
                mHolder.imgStatus.setImageResource(R.drawable.ic_door_close_small);
            }
            mHolder.txtStatus.setText(R.string.MAGNETISM_OFF);
        }else {
            if (position == 0){
                mHolder.imgStatus.setImageResource(R.drawable.ic_door_open_big);
            }else {
                mHolder.imgStatus.setImageResource(R.drawable.ic_door_open_small);
            }
            mHolder.txtStatus.setText(R.string.MAGNETISM_ON);
        }
        String previewDate = (position - 1) >= 0 ? getDate(getItem(position - 1).time) : "";
        if (previewDate.equals(currentDate)){
            mHolder.mData.setVisibility(View.GONE);
            mHolder.txtMonth.setVisibility(View.GONE);
        }else {
            mHolder.mData.setVisibility(View.VISIBLE);
            mHolder.horView.setVisibility(View.INVISIBLE);
            if (!currentDate.equals(getNowDay()) && position == 0){
                mHolder.txtMonth.setVisibility(View.VISIBLE);
            }
            if (position != 0){
                mHolder.txtMonth.setVisibility(View.VISIBLE);
            }
        }
        String nextData = (position + 1) < getCount() ? getDate(getItem(position + 1).time) : "";
        if (!nextData.equals(currentDate)){
            mHolder.horView1.setVisibility(View.INVISIBLE);
        }else {
            mHolder.horView1.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    private class ViewHolder{
        public TextView mData;
        public ImageView imgStatus;
        public TextView mTime;
        public TextView txtStatus;
        public TextView txtMonth;
        public View horView;
        public View horView1;

        public ViewHolder(View view){
            mData = (TextView)view.findViewById(R.id.magnetic_item_date);
            imgStatus = (ImageView)view.findViewById(R.id.magnetic_item_statue);
            mTime = (TextView) view.findViewById(R.id.magnetic_item_time);
            txtStatus = (TextView) view.findViewById(R.id.magnetic_item_open);
            txtMonth = (TextView) view.findViewById(R.id.magnetic_item_month);
            horView = view.findViewById(R.id.magnetic_view);
            horView1 = view.findViewById(R.id.magnetic_view1);
        }
    }

    private String getDate(long timeBegin){
        String d = mSimpleDateFormat.format(new Date(timeBegin * 1000));
        String td = mSimpleDateFormat.format(new Date(System.currentTimeMillis() / 1000));
        return d.equals(td) ? mContext.getString(R.string.DOOR_TODAY) : d;
    }

    private String getNowDay() {
        String date;
        Date d = new Date();
        date = mSimpleDateFormat.format(d);
        return date;
    }

}
