package com.cylan.jiafeigou.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.jiafeigou.activity.main.WebViewActivity;
import com.cylan.jiafeigou.activity.video.CallOrConf;
import com.cylan.jiafeigou.activity.video.HistoryVideoActivity;
import com.cylan.jiafeigou.activity.video.setting.DeviceSettingActivity;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.entity.msg.MsgCidData;
import com.cylan.jiafeigou.entity.msg.MsgData;
import com.cylan.jiafeigou.entity.msg.rsp.MsgCidlistRsp;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.StringUtils;
import com.cylan.jiafeigou.utils.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class MessageDetailAdapter extends BaseAdapter<MsgData> {

    private boolean isShowCheckbox = false;
    private List<Boolean> mCheckList = null;

    private static final int CHECK_VIDEO = 0x00;
    private static final int CHECK_HISTORYVIDEO = 0x01;

    private static final int item_type_1 = 0x00;
    private static final int item_type_2 = 0x01;

    private static final int IMAGE_WIDTH = 0;

    private List<MsgData> mList;
    private SimpleDateFormat mSimpleDateFormat;
    private String cid = null;

    private Dialog mCallDialog;

    private ClickdelWarmListener l;

    private SelectChangeListener mListener;

    public int getCheckedCount() {
        if (mCheckList == null)
            return 0;
        int count = 0;
        for (int i = 0; i < mCheckList.size(); i++) {
            if (mCheckList.get(i))
                count += 1;
        }
        return count;
    }

    public List<Boolean> getCheckedList() {
        if (mCheckList == null)
            return null;
        return mCheckList;
    }

    public boolean isShowCheckbox() {
        return isShowCheckbox;
    }

    public boolean isAllChecked() {
        for (int i = 0; i < mCheckList.size(); i++) {
            if (!mCheckList.get(i)) {
                return false;
            }
        }
        return true;
    }

    public void setShowCheckbox(boolean ic) {
        this.isShowCheckbox = ic;
    }


    public void setCid(String cid) {
        this.cid = cid;
    }

    public void resetSelectList() {
        if (mCheckList == null)
            return;
        for (int i = 0; i < mCheckList.size(); i++) {
            mCheckList.set(i, false);
        }
    }

    public void setSelectAll() {
        if (mCheckList == null)
            return;
        for (int i = 0; i < mCheckList.size(); i++) {
            mCheckList.set(i, true);
        }
    }

    public void removeSelect() {
        if (mCheckList == null)
            return;
        for (Boolean b : mCheckList) {
            if (b)
                mCheckList.remove(b);
        }
    }

    public MessageDetailAdapter(Activity activity, List<MsgData> list) {
        super(activity, list);
        mSimpleDateFormat = new SimpleDateFormat("M-d HH:mm");
        this.mList = list;
        mCheckList = new ArrayList<Boolean>();
        for (int i = 0; i < list.size(); i++) {
            mCheckList.add(false);
        }
    }

    public void addAll(Collection<? extends MsgData> collection, boolean isDeleteAll) {
        super.addAll(collection);
        if (isDeleteAll) {
            for (int i = 0; i < collection.size(); i++) {
                mCheckList.add(true);
            }
        } else {
            for (int i = 0; i < collection.size(); i++) {
                mCheckList.add(false);
            }
        }

    }


    @Override
    public int getItemViewType(int position) {

        return getItem(position).push_type == ClientConstants.PUSH_TYPE_WARN ? item_type_2 : item_type_1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @SuppressWarnings("null")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        ViewHolder1 vh1 = null;
        ViewHolder2 vh2 = null;
        if (convertView == null) {
            if (type == item_type_1) {
                convertView = View.inflate(mContext, R.layout.layout_msgdetail_item1, null);
                vh1 = new ViewHolder1(convertView);
                convertView.setTag(vh1);
            } else {
                convertView = View.inflate(mContext, R.layout.layout_msgdetail_item2, null);
                vh2 = new ViewHolder2(convertView);
                convertView.setTag(vh2);
            }
        } else {
            if (type == item_type_1) {
                vh1 = (ViewHolder1) convertView.getTag();
            } else {
                vh2 = (ViewHolder2) convertView.getTag();
            }
        }

        if (isShowCheckbox) {
            if (vh1 != null) {
                vh1.cb.setVisibility(View.VISIBLE);
                if (mCheckList.get(position)) {
                    vh1.cb.setChecked(mCheckList.get(position));
                }
                vh1.pic.setVisibility(View.GONE);
            }
            if (vh2 != null) {
                vh2.cb.setVisibility(View.VISIBLE);
                if (mCheckList.get(position)) {
                    vh2.cb.setChecked(mCheckList.get(position));
                }
                vh2.pic.setVisibility(View.GONE);
            }
        } else {
            if (vh1 != null) {
                vh1.cb.setVisibility(View.GONE);
                vh1.pic.setVisibility(View.VISIBLE);
            }
            if (vh2 != null) {
                vh2.cb.setVisibility(View.GONE);
                vh2.pic.setVisibility(View.VISIBLE);
            }
        }

        final MsgData info = getItem(position);
        ClickListener mClickListener = new ClickListener(position);
        if (type == item_type_2) {
            vh2.time.setText(mSimpleDateFormat.format(new Date(info.time * 1000)));
            if (System.currentTimeMillis() / 1000 - info.time > 1800 && info.video_time > 0) {
                vh2.btn.setVisibility(View.VISIBLE);
                vh2.btn.setBackgroundResource(R.drawable.bg_message_historyvideo_selector);
                vh2.btn.setText(R.string.RECORD);
                vh2.btn.setTag(CHECK_HISTORYVIDEO);
            } else if (System.currentTimeMillis() / 1000 - info.time < 600 && position == 0) {
                vh2.btn.setVisibility(View.VISIBLE);
                vh2.btn.setBackgroundResource(R.drawable.bg_checkdetail_selector);
                vh2.btn.setText(R.string.MSG_VIDEO);
                vh2.btn.setTag(CHECK_VIDEO);
            } else {
//                vh2.btn.setBackgroundResource(R.drawable.bg_checkdetail_selector);
//                vh2.btn.setText(R.string.MSG_VIDEO);
                vh2.btn.setTag(CHECK_VIDEO);
                vh2.btn.setVisibility(View.GONE);
            }
            MyLookLisitener mLookLisitener = new MyLookLisitener(info.cid, info.video_time);
            vh2.btn.setOnClickListener(mLookLisitener);
            vh2.cb.setChecked(mCheckList.get(position));
            vh2.cb.setOnClickListener(mClickListener);


            for (int i = info.urllist.size() - 1; i >= 0; i--) {
                if (StringUtils.isEmptyOrNull(info.urllist.get(i))) {
                    info.urllist.remove(i);
                }
            }

            for (int i = 0; i < info.urllist.size(); i++) {
                MyImageLoader.loadMsgImageFromNet(info.urllist.get(i), ((ImageView) vh2.mLayout.getChildAt(i)));

                vh2.mLayout.getChildAt(i).setVisibility(View.VISIBLE);
                vh2.mLayout.getChildAt(i).setOnClickListener(new CheckPicListener(position, i));
            }

        } else {
            vh1.time.setText(mSimpleDateFormat.format(new Date(info.time * 1000)));
            if (info.os == Constants.OS_EFAML) {
                vh1.pic.setImageResource(R.drawable.ico_efamily_msgdetails);
            } else if (info.os == Constants.OS_TEMP_HUMI) {
                vh1.pic.setImageResource(R.drawable.ico_efamily_temperaturemsg);
            } else if (info.os == Constants.OS_MAGNET) {
                vh1.pic.setImageResource(R.drawable.ico_efamily_doorwindowmsg);
            } else if (info.os== Constants.OS_IR) {
                vh1.pic.setImageResource(R.drawable.ico_efamily_infraredmsg);
            } else if (info.os == Constants.OS_SERVER) {
                vh1.pic.setImageResource(R.drawable.ico_system_message);
            } else if (info.os == Constants.OS_DOOR_BELL) {
                vh1.pic.setImageResource(R.drawable.ico_msgdetail_doorbell);
            } else {
                vh1.pic.setImageResource(R.drawable.ico_warm_message);
            }

            if (info.push_type == ClientConstants.PUSH_TYPE_SYSTEM) {
                vh1.title.setVisibility(View.GONE);
                vh1.content.setVisibility(View.VISIBLE);

                vh1.title.setText(info.title);
                vh1.content.getSettings().setDefaultTextEncodingName("UTF -8");
                vh1.content.setBackgroundColor(0); // 设置背景色
                vh1.content.setBackgroundResource(R.color.msg_detail_webview_color);
                vh1.content.loadData(info.systemContent, "text/html; charset=UTF-8", null);
                vh1.content.getBackground().setAlpha(0);
                vh1.content.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView view, final String url) { // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                        if (url.startsWith("tel:")) {
                            showCallDialog(mContext, url);
                        } else if (url.startsWith("mailto:")) {
                            try {
                                Intent data = new Intent(Intent.ACTION_SENDTO);
                                data.setData(Uri.parse(url));
                                mContext.startActivity(data);
                            } catch (Exception e) {
                            }
                        } else {
                            Intent it = new Intent(mContext, WebViewActivity.class).putExtra(WebViewActivity.TITLE, url).putExtra(WebViewActivity.URL, url);
                            mContext.startActivity(it);
                        }
                        return true;
                    }
                });
            } else {

                if (info.push_type == ClientConstants.PUSH_TYPE_SDCARD_ON && info.err != 0) {
                    vh1.btn.setVisibility(View.VISIBLE);
                    vh1.btn.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MsgCidData mVideoInfo = MsgCidlistRsp.getInstance().getVideoInfoFromCacheByCid(info.cid);
                            if (mVideoInfo == null)
                                return;
                            mContext.startActivity(new Intent(mContext, DeviceSettingActivity.class).putExtra(ClientConstants.CIDINFO, mVideoInfo));
                        }
                    });
                } else {
                    vh1.btn.setVisibility(View.GONE);
                }
                vh1.title.setVisibility(View.VISIBLE);
                vh1.content.setVisibility(View.GONE);
                vh1.title.setText(info.getContent());
            }
            vh1.cb.setChecked(mCheckList.get(position));
            vh1.cb.setOnClickListener(mClickListener);
        }

        convertView.setOnClickListener(mClickListener);


        return convertView;

    }

    class ViewHolder2 {
        TextView time;
        ImageView pic;
        ImageView img;
        ImageView img1;
        ImageView img2;
        Button btn;
        LinearLayout mLayout;
        CheckBox cb;

        public ViewHolder2(View view) {
            time = (TextView) view.findViewById(R.id.item_msgdetail_time);
            pic = (ImageView) view.findViewById(R.id.item_msgdetail_pic);
            img = (ImageView) view.findViewById(R.id.item_msgdetail_content_pic);
            img1 = (ImageView) view.findViewById(R.id.item_msgdetail_content_pic1);
            img2 = (ImageView) view.findViewById(R.id.item_msgdetail_content_pic2);
            btn = (Button) view.findViewById(R.id.look);
            mLayout = (LinearLayout) view.findViewById(R.id.piclayout);
            cb = (CheckBox) view.findViewById(R.id.select);


        }

    }

    class ViewHolder1 {
        TextView time;
        ImageView pic;
        TextView title;
        WebView content;
        Button btn;
        CheckBox cb;

        public ViewHolder1(View view) {
            time = (TextView) view.findViewById(R.id.item_msgdetail_time);
            pic = (ImageView) view.findViewById(R.id.item_msgdetail_pic);
            title = (TextView) view.findViewById(R.id.item_msgdetail_content_title);
            content = (WebView) view.findViewById(R.id.item_msgdetail_content_text);
            btn = (Button) view.findViewById(R.id.look);
            cb = (CheckBox) view.findViewById(R.id.select);

        }

    }

    void showCallDialog(final Context ctx, final String str) {
        try {
            if (mCallDialog == null) {
                mCallDialog = new Dialog(ctx, R.style.func_dialog);
                View content = View.inflate(ctx, R.layout.dialog_callto, null);
                mCallDialog.setContentView(content);
                mCallDialog.setCanceledOnTouchOutside(true);
            }

            TextView title = (TextView) mCallDialog.findViewById(R.id.textinfo);
            Button call = (Button) mCallDialog.findViewById(R.id.btn_call);
            Button copy = (Button) mCallDialog.findViewById(R.id.btn_copy);
            TextView cancel = (TextView) mCallDialog.findViewById(R.id.btn_cancel);
            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCallDialog.dismiss();
                }
            });

            call.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCallDialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(str));
                    mContext.startActivity(intent);

                }
            });
            copy.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallDialog.dismiss();
                    ClipboardManager cmb = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(str.replaceAll("tel:", ""));
                    ToastUtil.showSuccessToast(ctx, ctx.getString(R.string.has_copy));
                }
            });
            title.setText(String.format(ctx.getString(R.string.dialog_callto_info), str.replaceAll("tel:", "")));
            mCallDialog.show();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }


    class MyLookLisitener implements OnClickListener {
        private long mTime;
        private String mCid;

        public MyLookLisitener(String cid, long time) {
            this.mCid = cid;
            this.mTime = time;
        }

        @Override
        public void onClick(View v) {
            MsgCidData mVideoInfo = MsgCidlistRsp.getInstance().getVideoInfoFromCacheByCid(mCid);
            if (mVideoInfo == null) {
                ToastUtil.showFailToast(mContext, mContext.getString(R.string.MSG_UNBIND_INFO));
                return;
            }
            if ((Integer) v.getTag() == CHECK_VIDEO) {
                mContext.startActivity(new Intent(mContext, CallOrConf.class).putExtra(ClientConstants.CIDINFO, mVideoInfo));
            } else {
                mContext.startActivity(new Intent(mContext, HistoryVideoActivity.class).putExtra(ClientConstants.CIDINFO, mVideoInfo).putExtra("time", mTime));
            }
        }
    }


    class ClickListener implements View.OnClickListener {

        private int mPosition;

        public ClickListener(int position) {
            this.mPosition = position;
        }

        @Override
        public void onClick(View v) {
            if (isShowCheckbox) {
                mCheckList.set(mPosition, !mCheckList.get(mPosition));
                notifyDataSetChanged();
                if (mListener != null)
                    mListener.selectChange(getCheckedCount());
            }
        }
    }

    class CheckPicListener implements OnClickListener {

        private int mPos;
        private int mIndex;

        public CheckPicListener(int position, int index) {
            this.mPos = position;
            this.mIndex = index;
        }

        @Override
        public void onClick(View v) {
            if (l != null)
                l.delPic(mPos, mIndex);
        }
    }

    public interface ClickdelWarmListener {
        void delPic(int position, int index);
    }

    public void addDelListener(ClickdelWarmListener li) {
        l = li;
    }


    public interface SelectChangeListener {
        void selectChange(int num);

    }

    public void setOnSelectChangeListener(SelectChangeListener opl) {
        this.mListener = opl;
    }


    @Override
    public void clear() {
        if (mCheckList != null) {
            mCheckList.clear();
        }
        super.clear();

    }
}
