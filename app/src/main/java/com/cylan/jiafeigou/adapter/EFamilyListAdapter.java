package com.cylan.jiafeigou.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.entity.msg.EfamilyData;

import java.util.List;


public class EFamilyListAdapter extends BaseAdapter<EfamilyData> {

    private boolean isOnline = true;

    public EFamilyListAdapter(Activity context, List<EfamilyData> data) {
        super(context, data);
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    private class ViewHolder {
        ImageView mImage;
        TextView mName, mState;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder vh;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_efamily, null);
            vh = new ViewHolder();
            convertView.setTag(vh);
            vh.mImage = (ImageView) convertView.findViewById(R.id.iv_efamily_list);
            vh.mName = (TextView) convertView.findViewById(R.id.tv_efamily_list_name);
            vh.mState = (TextView) convertView.findViewById(R.id.tv_efamily_list_state);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        EfamilyData info = getItem(position);
        if (info == null) {
            return convertView;
        }
        vh.mName.setText(info.alias);
        if (info.os == Constants.OS_IR) {
            vh.mImage.setBackgroundResource(R.drawable.ico_efamily_infrared);
            if (info.mag_ir == 0) {
//                vh.mState.setText(mContext.getString(R.string.EFAMILY_NORMAL));//mContext.getString(R.string.close)
                vh.mState.setTextColor(mContext.getResources().getColor(R.color.list_close));
            } else {
//                vh.mState.setText(mContext.getString(R.string.EFAMILY_ABNORMAL));
                vh.mState.setTextColor(mContext.getResources().getColor(R.color.list_open));
            }
        } else if (info.os == Constants.OS_MAGNET) {
            vh.mImage.setBackgroundResource(R.drawable.ico_efamily_door);
            if (info.mag_ir == 0) {
//                vh.mState.setText(mContext.getString(R.string.EFAMILY_PROTECT_OFF));
                vh.mState.setTextColor(mContext.getResources().getColor(R.color.list_close));
            } else {
//                vh.mState.setText(mContext.getString(R.string.EFAMILY_PROTECT_ON));
                vh.mState.setTextColor(mContext.getResources().getColor(R.color.list_open));
            }
        }

        if (!isOnline) {
            vh.mState.setVisibility(View.GONE);
        } else {
            vh.mState.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

}
