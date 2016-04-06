package com.cylan.jiafeigou.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.utils.StringUtils;

import java.util.List;

public class CidDataAdapter extends BaseAdapter<MsgCidData> {


    public CidDataAdapter(Activity activity, List<MsgCidData> list) {
        super(activity, list);
    }

    private class ViewHolder {
        ImageView thumb;
        TextView name;
        TextView isOnline;
        ImageView itemShare;
        ImageView unRead;
        ImageView lowPower;
        TextView magStatus;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder vh;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.video_page_list_item, null);
            vh = new ViewHolder();
            convertView.setTag(vh);
            vh.thumb = (ImageView) convertView.findViewById(R.id.item_pic);
            vh.name = (TextView) convertView.findViewById(R.id.item_video_name);
            vh.isOnline = (TextView) convertView.findViewById(R.id.item_video_isonline);
            vh.itemShare = (ImageView) convertView.findViewById(R.id.item_share);
            vh.lowPower = (ImageView) convertView.findViewById(R.id.item_power);
            vh.unRead = (ImageView) convertView.findViewById(R.id.item_new_record);
            vh.magStatus = (TextView) convertView.findViewById(R.id.item_magnet_status);
        } else {
            vh = (ViewHolder) convertView.getTag();
            vh.unRead.setVisibility(View.INVISIBLE);
            vh.lowPower.setVisibility(View.INVISIBLE);
            vh.magStatus.setVisibility(View.INVISIBLE);
        }
        final MsgCidData info = getItem(position);
        if (info == null) {
            return convertView;
        }

        vh.name.setText(info.mName());

        if (!StringUtils.isEmptyOrNull(info.share_account)) {
            vh.name.setText(info.cid);
            vh.itemShare.setVisibility(View.VISIBLE);
        } else {
            vh.itemShare.setVisibility(View.GONE);
        }


        if (info.net == MsgCidData.CID_NET_WIFI) {
            vh.isOnline.setText(R.string.DEVICE_WIFI_ONLINE);
            setThumbImageOn(vh, info);
        } else if (info.net == MsgCidData.CID_NET_3G) {
            vh.isOnline.setText(R.string.DEVICE_3G_ONLINE);
            setThumbImageOn(vh, info);
        } else if (info.net == MsgCidData.CID_NET_OFFLINE) {
            vh.isOnline.setText(R.string.OFF_LINE);
            setThumbImageOff(vh, info);
        } else if (info.net == MsgCidData.CID_NET_CONNECT) {
            vh.isOnline.setText(R.string.NET_CONNECT);
            setThumbImageOff(vh, info);
        }

        if (info.os == Constants.OS_DOOR_BELL && info.net == MsgCidData.CID_NET_WIFI && StringUtils.isEmptyOrNull(info.share_account)) {
            vh.lowPower.setVisibility(info.battery < 20 ? View.VISIBLE : View.INVISIBLE);
        }else if (info.os == Constants.OS_MAGNET && StringUtils.isEmptyOrNull(info.share_account)){
            vh.lowPower.setVisibility(info.battery < 5 ? View.VISIBLE : View.INVISIBLE);
        }

        if (info.os == Constants.OS_MAGNET) {
            vh.thumb.setImageResource(R.drawable.ico_magnet_online);
            vh.isOnline.setText(R.string.DEVICE_BLUETOOTH_ONLINE);
            vh.magStatus.setVisibility(View.VISIBLE);
            if (info.noAnswerBC != 0 ) {
                vh.unRead.setVisibility(View.VISIBLE);
            }
            if (info.magstate == 0) {
                vh.magStatus.setText(R.string.MAGNETISM_OFF);
                vh.magStatus.setBackgroundResource(R.drawable.mag_off_shape);
            } else{
                vh.magStatus.setText(R.string.MAGNETISM_ON);
                vh.magStatus.setBackgroundResource(R.drawable.mag_on_shape);
            }

        }

        return convertView;

    }

    private void setThumbImageOff(ViewHolder vh, MsgCidData info) {
        if (info.os == Constants.OS_EFAML) {
            vh.thumb.setImageResource(R.drawable.ico_efamily_offline);
        } else if (info.os == Constants.OS_DOOR_BELL) {
            vh.thumb.setImageResource(R.drawable.ico_doorbell_offline);
        } else {
            vh.thumb.setImageResource(R.drawable.ico_video_offline);
        }
    }

    private void setThumbImageOn(ViewHolder vh, MsgCidData info) {
        if (info.os == Constants.OS_EFAML) {
            vh.thumb.setImageResource(R.drawable.ico_efamily_online);
            if (info.noAnswerBC != 0 ) {
                vh.unRead.setVisibility(View.VISIBLE);
            }
        } else if (info.os == Constants.OS_DOOR_BELL || info.os == Constants.OS_DOOR_BELL_V2) {
            vh.thumb.setImageResource(R.drawable.ico_doorbell_online);
            if (info.noAnswerBC != 0 && StringUtils.isEmptyOrNull(info.share_account)) {
                vh.unRead.setVisibility(View.VISIBLE);
            }
        } else {
            vh.thumb.setImageResource(R.drawable.ico_video_online);
        }
    }


    public static String getContactNameByPhoneNumber(Context context, String address) {
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};

        // 灏嗚嚜宸辨坊鍔犲埌 msPeers 涓�
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, // Which
                // columns
                // to
                // return.
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + address + "'", // WHERE
                // clause.
                null, // WHERE clause value substitution
                null); // Sort order.

        if (cursor == null) {

            return null;
        }
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            // 鍙栧緱鑱旂郴浜哄悕瀛�
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
            String name = cursor.getString(nameFieldColumnIndex);
            if (!cursor.isClosed())
                cursor.close();
            return name;

        }
        if (!cursor.isClosed())
            cursor.close();
        return null;

    }

    public void disappearDot(int index, ListView listView, boolean isShow) {
        int visableFirstPosi = listView.getFirstVisiblePosition();
        int visableLastPosi = listView.getLastVisiblePosition();
        if (index >= visableFirstPosi && index <= visableLastPosi) {
            View view = listView.getChildAt(index - visableFirstPosi);
            ViewHolder holder = (ViewHolder) view.getTag();
            if (isShow)
                holder.unRead.setVisibility(View.VISIBLE);
            else
                holder.unRead.setVisibility(View.INVISIBLE);
        }
    }

}
