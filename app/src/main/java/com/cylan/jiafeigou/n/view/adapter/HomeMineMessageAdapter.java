package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.MineMessageBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;
import com.cylan.jiafeigou.utils.ContextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeMineMessageAdapter extends SuperAdapter<MineMessageBean> {

    public boolean isShowCheck;
    public boolean checkAll;

    private static Map<Integer, Integer> resMap = new HashMap<>();

    static {
        //bell
        resMap.put(JConstant.OS_DOOR_BELL, R.drawable.me_icon_head_ring);
        //camera
        resMap.put(JConstant.OS_CAMARA_ANDROID_SERVICE, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.OS_CAMERA_ANDROID, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.OS_CAMERA_ANDROID_4G, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.OS_CAMERA_CC3200, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.OS_CAMERA_PANORAMA_GUOKE, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.OS_CAMERA_PANORAMA_HAISI, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.OS_CAMERA_PANORAMA_QIAOAN, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.OS_CAMERA_UCOS_V3, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.OS_CAMERA_UCOS_V2, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.OS_CAMERA_UCOS, R.drawable.me_icon_head_camera);
        resMap.put(JConstant.PID_CAMERA_ANDROID_3_0, R.drawable.me_icon_head_camera);

        //MAG
        resMap.put(JConstant.OS_MAGNET, R.drawable.me_icon_head_magnetometer);
        //E_FAMILY
        resMap.put(JConstant.OS_EFAML, R.drawable.me_icon_head_album);
    }

    public OnDeleteCheckChangeListener listener;

    public interface OnDeleteCheckChangeListener {
        void deleteCheck(boolean isCheck, MineMessageBean item);
    }

    public void setOnDeleteCheckChangeListener(OnDeleteCheckChangeListener listener) {
        this.listener = listener;
    }

    public HomeMineMessageAdapter(Context context, List<MineMessageBean> items, IMulItemViewType<MineMessageBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MineMessageBean item) {
        //处理消息时间
        holder.setText(R.id.item_time, parseTime(item.getTime()));

        if (isShowCheck) {
            holder.setVisibility(R.id.delete_check, View.VISIBLE);
        } else {
            holder.setVisibility(R.id.delete_check, View.GONE);
        }

        CheckBox deleteCheck = holder.getView(R.id.delete_check);
        deleteCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (listener != null) {
                    listener.deleteCheck(isChecked, item);
                }
            }
        });

        if (checkAll) {
            deleteCheck.setChecked(true);
        } else {
            deleteCheck.setChecked(false);
        }

        //头像icon
        if (item.type == 701) {
            //处理消息显示
            holder.setText(R.id.tv_device_name, ContextUtils.getContext().getString(R.string.Tap3_UserMessage_System));
            holder.setImageDrawable(R.id.iv_mesg_icon, getContext().getResources().getDrawable(R.drawable.pic_head));
        } else {
            //处理消息显示
            holder.setText(R.id.tv_device_name, item.getContent());
            char c = item.content.charAt(0);
            if (c == '5') {
                int iconRes = resMap.get(6);
                holder.setImageDrawable(R.id.iv_mesg_icon, getContext().getResources().getDrawable(iconRes));
            } else if (c == '7') {
                int iconRes = resMap.get(11);
                holder.setImageDrawable(R.id.iv_mesg_icon, getContext().getResources().getDrawable(iconRes));
            } else {
                int iconRes = resMap.get(4);
                holder.setImageDrawable(R.id.iv_mesg_icon, getContext().getResources().getDrawable(iconRes));
            }
        }

        if (item.isDone == 1) {
            switch (item.type) {
                case 601:
                    String name = item.getName();
                    if (name == null) return;
                    String fName = null;
                    if (name.matches("^\\d{11}$")) {//前三后四
                        fName = name.replace(name.substring(3, name.length() - 4), "****");
                    } else if (name.matches("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?")) {
                        String[] split = name.split("@");
                        if (split[0].length() == 1) {
                            fName = name;
                        } else if (split[0].length() == 2) {
                            fName = split[0].replace(split[0].substring(1, 2), "*") + "@" + split[1];
                        } else if (split[0].length() == 3) {
                            fName = split[0].replace(split[0].substring(1, 3), "**") + "@" + split[1];
                        } else if (split[0].length() > 3 && split[0].length() <= 8) {
                            fName = split[0].replace(split[0].substring(2, split[0].length() - 1), "****") + "@" + split[1];
                        } else if (split[0].length() > 8) {
                            fName = split[0].replace(split[0].substring(3, split[0].length() - 1), "****") + "@" + split[1];
                        }
                    } else {//第三方登录
                        if (name.length() == 1) {
                            fName = name;
                        } else if (name.length() == 2) {
                            fName = name.replace(name.substring(1), "*");
                        } else if (name.length() == 3) {
                            fName = name.replace(name.substring(1), "**");
                        } else if (name.length() == 4) {
                            fName = name.replace(name.substring(1, name.length() - 1), "***");
                        } else if (name.length() > 4 && name.length() <= 8) {
                            fName = name.replace(name.substring(2, name.length() - 1), "***");
                        } else if (name.length() > 8 && name.length() <= 16) {
                            fName = name.replace(name.substring(3, name.length() - 1), "***");
                        } else if (name.length() > 16) {
                            fName = name.replace(name.substring(4, name.length() - 4), "****");
                        }
                    }
                    if (fName == null) fName = name;

                    holder.setText(R.id.mesg_item_content, String.format(ContextUtils.getContext().getString(R.string.MSG_REBIND), fName));
                    break;
                case 603:
                    holder.setText(R.id.mesg_item_content, "该设备已分享");
                    break;
                case 604:
                    holder.setText(R.id.mesg_item_content, "亲友分享了改设备");
                    break;
            }
        } else {
            switch (item.type) {
                case 601:
                    holder.setText(R.id.mesg_item_content, ContextUtils.getContext().getString(R.string.MSG_UNBIND));
                    break;
                case 603:
                    holder.setText(R.id.mesg_item_content, ContextUtils.getContext().getString(R.string.Tap1_shareDevice_canceledshare));
                    break;
                case 604:
                    holder.setText(R.id.mesg_item_content, "亲友取消了分享该设备");
                    break;
                case 701:
                    holder.setText(R.id.mesg_item_content, Html.fromHtml(item.getName()).toString().trim());
                    break;
            }
        }

    }

    @Override
    protected IMulItemViewType<MineMessageBean> offerMultiItemViewType() {
        return new IMulItemViewType<MineMessageBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, MineMessageBean mineMessageBean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
//                return R.layout.fragment_mine_message_system_items;
                return R.layout.fragment_mine_message_items;
            }
        };
    }

    public String parseTime(String times) {
        long timem = Long.parseLong(times);
        Date time = new Date(timem);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateString = formatter.format(time);
        return dateString;
    }

    public boolean compareTime(String preStrTime, String nowStrTime) {
        long preTime = Long.parseLong(preStrTime);
        long nowTime = Long.parseLong(nowStrTime);
        return (nowTime - preTime >= 20000);
    }
}
