package com.cylan.jiafeigou.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.StringUtils;

import java.util.List;

public class ShareAdapter extends android.widget.BaseAdapter {

    private Activity mContext;
    public List<String> mList;

    public ShareAdapter(Activity activity, List<String> list) {
        this.mContext = activity;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public String getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(mContext, R.layout.near_share_list_item, null);
        String name = null;
        try {
            name = StringUtils.isEmptyOrNull(getContactNameByPhoneNumber(mContext, mList.get(position))) ? "" : ("("
                    + getContactNameByPhoneNumber(mContext, mList.get(position)) + ")");
        } catch (Exception e) {
            name ="";
        }
        ((TextView) convertView).setText(mList.get(position) + name);
        return convertView;
    }

    /**
     * 2 * 根据电话号码取得联系人姓名 3
     */
    public static String getContactNameByPhoneNumber(Context context, String address) {
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Email.ADDRESS};
        // 将自己添加到 msPeers 中
        Cursor cursor = null;
        if (StringUtils.isEmail(address)){
            cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection, // Which
                    ContactsContract.CommonDataKinds.Email.ADDRESS + " = '" + address + "'", // WHERE
                    // clause.
                    null, // WHERE clause value substitution
                    null); // Sort order.
        }else {
            cursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, // Which
                    ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + address + "'", // WHERE
                    // clause.
                    null, // WHERE clause value substitution
                    null); // Sort order.
        }

        if (cursor == null) {
            return null;
        }
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);

            // 取得联系人名字
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
}
