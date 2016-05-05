package com.cylan.jiafeigou.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.entity.msg.CallListData;
import com.cylan.jiafeigou.listener.CheckDoorbellHeadPicListener;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.widget.CircleImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DoorBellAdapter extends BaseAdapter<CallListData> {

    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
    private SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private CheckDoorbellHeadPicListener mListener;

    public DoorBellAdapter(Activity activity, List<CallListData> list) {
        super(activity, list);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_doorbell_list_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final CallListData dbb = getItem(position);

        String currentDate = getData(dbb.timeBegin);

        holder.date.setText(currentDate);

        holder.time.setText(mSimpleTimeFormat.format(new Date(dbb.timeBegin * 1000)));

        if (dbb.isOK != 1) {
            holder.sub.setTextColor(mContext.getResources().getColor(R.color.delete_color));
            holder.sub.setText(R.string.DOOR_UNCALL);
        } else {
            holder.sub.setTextColor(mContext.getResources().getColor(R.color.del_message_unenable));
            holder.sub.setText(R.string.DOOR_CALL);
        }

        String previewDate = (position - 1) >= 0 ? getData(getItem(position - 1).timeBegin) : " ";

        if (!previewDate.equals(currentDate)) {
            holder.date.setVisibility(View.VISIBLE);
            holder.line1.setVisibility(View.VISIBLE);
        } else {
            holder.date.setVisibility(View.GONE);
            holder.line1.setVisibility(View.GONE);

        }
        String nextData = (position + 1) < getCount() ? getData(getItem(position + 1).timeBegin) : " ";
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.line2.getLayoutParams();
        if (!nextData.equals(currentDate) || position == getCount() - 1) {
            params.setMargins(0, 0, 0, 0);
        } else {
            params.setMargins(DensityUtil.dip2px(mContext, 82), 0, 0, 0);
        }
        holder.line2.setLayoutParams(params);
        holder.pic.setImageDrawable(null);
        MyImageLoader.loadImageFromNet(dbb.url, holder.pic);


        holder.pic.setTag(position);
        holder.pic.setOnClickListener(mClick);
        return convertView;
    }

    class ViewHolder {

        public CircleImageView pic;
        public TextView date;
        public TextView time;
        public TextView sub;
        public View line1;
        public View line2;

        public ViewHolder(View v) {
            date = (TextView) v.findViewById(R.id.date);
            pic = (CircleImageView) v.findViewById(R.id.headpic);
            time = (TextView) v.findViewById(R.id.tvTime);
            sub = (TextView) v.findViewById(R.id.sub);
            line1 = v.findViewById(R.id.line1);
            line2 = v.findViewById(R.id.line2);
        }
    }

    private String getData(long timebegin) {
        String d = mSimpleDateFormat.format(new Date(timebegin * 1000));
        String td = mSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        return d.equals(td) ? mContext.getString(R.string.DOOR_TODAY) : d;
    }


    public void setOnCheckHeadPicListener(CheckDoorbellHeadPicListener listener) {
        mListener = listener;
    }

    OnClickListener mClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            if (mListener != null)
                mListener.check(position);
        }
    };

}
