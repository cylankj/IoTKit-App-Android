package com.cylan.jiafeigou.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MsgData;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.Utils;

import java.util.List;


public class MessageAdapter extends BaseAdapter {

    public List<MsgData> mList;
    private Context mContext;

    public MessageAdapter(Context context, List<MsgData> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public MsgData getItem(int position) {

        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder vh;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.msg_list_item, null);
            vh = new ViewHolder();
            convertView.setTag(vh);
            vh.mPicLayout = (RelativeLayout) convertView.findViewById(R.id.msg_item_pic);
            vh.mPicView = (ImageView) convertView.findViewById(R.id.msg_item_shotpic);
            vh.mInfoNum = (TextView) convertView.findViewById(R.id.msg_item_info);
            vh.mTitleView = (TextView) convertView.findViewById(R.id.msg_item_title);
            vh.mTimeView = (TextView) convertView.findViewById(R.id.msg_item_time);
            vh.mContentView = (TextView) convertView.findViewById(R.id.msg_item_content);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        MsgData msg = getItem(position);

        if (StringUtils.toInt(msg.count) > 0) {
            vh.mInfoNum.setVisibility(View.VISIBLE);
            if (StringUtils.toInt(msg.count) < 10) {
                vh.mInfoNum.setBackgroundResource(R.drawable.bg_msg_info1);
            } else {
                vh.mInfoNum.setBackgroundResource(R.drawable.bg_msg_info2);
            }

            if (StringUtils.toInt(msg.count) > 99) {
                vh.mInfoNum.setText("99+");
            } else {
                vh.mInfoNum.setText(String.valueOf(msg.count));
            }
        } else {
            vh.mInfoNum.setVisibility(View.GONE);
        }
        vh.mTitleView.setText(StringUtils.isEmptyOrNull(msg.alias) ? msg.cid : msg.alias);
        if (msg.time == 0) {
            vh.mTimeView.setVisibility(View.GONE);
        } else {
            vh.mTimeView.setVisibility(View.VISIBLE);
        }
        vh.mTimeView.setText(StringUtils.friendly_time(mContext.getString(R.string.JUST_NOW), String.valueOf(msg.time * 1000)));

        if (msg.os == Constants.OS_SERVER) {
            vh.mPicView.setImageResource(R.drawable.ico_system_msg);
            vh.mContentView.setText(StringUtils.isEmptyOrNull(msg.title) ? getContent(mContext, msg) : msg.title);
        } else {
            if (msg.os == Constants.OS_DOOR_BELL) {
                vh.mPicView.setImageResource(R.drawable.ico_doorbell_msg);
            } else if (msg.os == Constants.OS_EFAML) {
                vh.mPicView.setImageResource(R.drawable.ico_efamily_msg);
            } else if (msg.os == Constants.OS_CAMERA_UCOS_V3) {
                //todo v3 gou
            } else if (msg.os == Constants.OS_MAGNET){
                vh.mPicView.setImageResource(R.drawable.ico_magnet_online);
            }else {
                vh.mPicView.setImageResource(R.drawable.ico_video_online);
            }
            vh.mContentView.setText(getContent(mContext, msg));
        }


        return convertView;
    }

    private class ViewHolder {
        RelativeLayout mPicLayout;
        ImageView mPicView;
        TextView mInfoNum;
        TextView mTitleView;
        TextView mTimeView;
        TextView mContentView;
    }

    public static String getContent(Context ctx, MsgData msg) {
        String content = null;
        switch (msg.push_type) {
            case ClientConstants.PUSH_TYPE_WARN:
                content = ctx.getString(R.string.MSG_WARNING);
                break;

            case ClientConstants.PUSH_TYPE_TEMP_HUMI:
                break;
            case ClientConstants.PUSH_TYPE_WARN_ON:
                content = ctx.getString(R.string.MSG_WARN_ON, "");
                break;
            case ClientConstants.PUSH_TYPE_HELLO:
                content = ctx.getString((msg.os == com.cylan.publicApi.Constants.OS_DOOR_BELL) ? R.string.DOOR_BIND : R.string.MSG_BIND);
                break;
            case ClientConstants.PUSH_TYPE_WARN_OFF:
                content = ctx.getString(R.string.MSG_WARN_OFF, "");
                break;

            case ClientConstants.PUSH_TYPE_SYSTEM:
                break;

            case ClientConstants.PUSH_TYPE_LOW_BATTERY:
                content = ctx.getString(R.string.MSG_LOW_BATTERY, "");
                break;
            case ClientConstants.PUSH_TYPE_SDCARD_OFF:
                content = ctx.getString(R.string.MSG_SD_OFF, "");
                break;
            case ClientConstants.PUSH_TYPE_UNHELLO:
                content = String.format(ctx.getString(R.string.MSG_UNBIND), msg.cid);
                break;
            case ClientConstants.PUSH_TYPE_NEW_VERSION:

                content = String.format(ctx.getString(R.string.MSG_SYSTEM_UPGRADE), msg.version);

                break;
            case ClientConstants.PUSH_TYPE_WARN_REPORT:

//                String time = mSimpleDateFormat1.format(new Date(msg.getReport_time() * 1000));
//                content = String.format((time + ctx.getString(R.string.day_report)), msg.getReport_num());

                break;
            case ClientConstants.PUSH_TYPE_SDCARD_ON:
                content = ctx.getString(msg.err == 0 ? R.string.MSG_SD_ON : R.string.MSG_SD_ON_1);
                break;
            case ClientConstants.PUSH_TYPE_REBIND:
                content = String.format(ctx.getString(R.string.MSG_REBIND), Utils.phoneNumchange(msg.binding_account));
                break;
            case ClientConstants.PUSH_TYPE_SHARE:
                content = String.format(ctx.getString(R.string.MSG_SHARE), Utils.phoneNumchange(msg.share_account));
                break;
            case ClientConstants.PUSH_TYPE_UNSHARE:
                content = String.format(ctx.getString(R.string.MSG_UNSHARE), Utils.phoneNumchange(msg.share_account));
                break;
            case ClientConstants.PUSH_TYPE_MAGNET_ON:
                content = String.format(ctx.getString(R.string.MAGNETISM_ON_PUSH), msg.alias);
                break;
            case ClientConstants.PUSH_TYPE_MAGNET_OFF:
                content = String.format(ctx.getString(R.string.MAGNETISM_OFF_PUSH), msg.alias);
                break;
            case ClientConstants.PUSH_TYPE_IR:
//                content = String.format(ctx.getString(R.string.push_type_ir), msg.alias);
                break;
            case ClientConstants.PUSH_TYPE_AIR_DETECTOR:
                //content = String.format(ctx.getString(R.string.push_type_ir_off), msg.getAlias());
                break;
        }

        return content;
    }
}
