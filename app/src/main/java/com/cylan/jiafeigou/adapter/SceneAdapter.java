package com.cylan.jiafeigou.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MsgSceneData;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.publicApi.Constants;

import java.util.List;

public class SceneAdapter extends BaseAdapter<MsgSceneData> {


    public SceneAdapter(Activity activity, List<MsgSceneData> list) {
        super(activity, list);
    }

    public View getView(int arg0, View convertView, ViewGroup arg2) {
        Holder holder = null;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.menu_grid_item, null);
            convertView.setTag(holder);

            holder.mChecked = (ImageView) convertView.findViewById(R.id.item_check);
            holder.name = (TextView) convertView.findViewById(R.id.item_scene_name);
            holder.pic = (ImageView) convertView.findViewById(R.id.pic);
        } else {
            holder = (Holder) convertView.getTag();
        }
        final MsgSceneData bean = getItem(arg0);

        if (arg0 == getCount() - 1) {
            holder.pic.setImageResource(R.drawable.bg_add_selector);
            holder.name.setVisibility(View.GONE);
            holder.mChecked.setVisibility(View.GONE);
            holder.pic.setBackgroundDrawable(null);
        } else {
            holder.name.setVisibility(View.VISIBLE);
            if (bean.enable == ClientConstants.ENABLE_SCENE) {
                holder.mChecked.setVisibility(View.VISIBLE);
            } else {
                holder.mChecked.setVisibility(View.GONE);
            }
            holder.name.setText(bean.scene_name);
            if (bean.image_id != 0) {
                holder.pic.setImageResource(ClientConstants.covers[bean.image_id - 1]);
            } else {
                String url = "http://" + Constants.WEB_ADDR + ":" + Constants.WEB_PORT + "/index.php?sessid=" + PreferenceUtil.getSessionId(mContext)
                        + "&mod=client&act=get_scene_image&scene_id=" + bean.scene_id;
//                MyApp.getFinalBitmap().display(holder.pic, url);
                MyImageLoader.loadImageFromNet(url, holder.pic);
            }

        }

        return convertView;
    }

    class Holder {
        ImageView mChecked;
        TextView name;
        ImageView pic;

    }

}
