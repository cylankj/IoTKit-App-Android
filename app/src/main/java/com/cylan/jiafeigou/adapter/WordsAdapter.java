package com.cylan.jiafeigou.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.entity.WordsBean;
import com.cylan.jiafeigou.widget.NumberProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by HeBin on 2015/2/27.
 */
public class WordsAdapter extends BaseAdapter<WordsBean> {

    public List<WordsBean> mList;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
    private SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("mm:ss");

    private PlaySoundListener mListener;
    private OnResendListener mRListener;

    public WordsAdapter(Activity activity, List<WordsBean> list) {
        super(activity, list);
        this.mList = list;
    }


    public void setPlay(int pos) {
        for (int i = 0; i < mList.size(); i++) {
            if (i == pos) {
                mList.get(i).setPlay(true);
            } else {
                mList.get(i).setPlay(false);
            }
        }
        notifyDataSetChanged();
    }

    public void resetProgress() {
        for (int i = 0; i < mList.size(); i++) {
            mList.get(i).setProgress(0);
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_recordsound_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final WordsBean bean = getItem(position);
        holder.time.setText(mSimpleDateFormat.format(new Date(bean.getTimeBegin() * 1000)));
        holder.leng.setText(mSimpleTimeFormat.format(new Date(bean.getTimeDuration() * 1000)));

        switch (bean.getSendState()) {
            case WordsBean.HAS_SEND:
//                holder.getTextView().setText(R.string.EFAMILY_SEND_ALREADY);
                break;
            case WordsBean.SOUND_DOWNLOAD:
            case WordsBean.SENDING:
                holder.getProgressBar();
                break;
            case WordsBean.HAS_READ:
                holder.getTextView().setText(R.string.EFAMILY_READ_ALREADY);
                break;
            case WordsBean.SEND_FAIL:
                holder.getImageView();
                holder.getImageView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mRListener != null)
                            mRListener.resend(position);
                    }
                });
                break;
        }


        holder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.play(position);

            }
        });


        holder.play.setImageResource(bean.isPlay() ? R.drawable.btn_record_stop_selector : R.drawable.btn_record_play_selector);
        holder.progressBar.setProgress(bean.getProgress());

        return convertView;
    }

    class ViewHolder {
        TextView time;
        RelativeLayout layout;
        List<View> mViewList = new ArrayList<View>();
        ImageView play;
        TextView leng;
        NumberProgressBar progressBar;


        ViewHolder(View view) {
            time = (TextView) view.findViewById(R.id.time);
            layout = (RelativeLayout) view.findViewById(R.id.layout);
            for (int i = 0; i < layout.getChildCount(); i++) {
                if (!(layout.getChildAt(i) instanceof LinearLayout)) {
                    mViewList.add(layout.getChildAt(i));
                }
            }
            play = (ImageView) view.findViewById(R.id.play_btn);
            leng = (TextView) view.findViewById(R.id.duration);
            progressBar = (NumberProgressBar) view.findViewById(R.id.progress);
        }

        public TextView getTextView() {
            TextView view = null;
            for (int i = 0; i < mViewList.size(); i++) {
                if (mViewList.get(i) instanceof TextView) {
                    mViewList.get(i).setVisibility(View.VISIBLE);
                    view = (TextView) mViewList.get(i);
                } else mViewList.get(i).setVisibility(View.GONE);
            }
            return view;
        }

        public ImageView getImageView() {
            ImageView view = null;
            for (int i = 0; i < mViewList.size(); i++) {
                if (mViewList.get(i) instanceof ImageView) {
                    mViewList.get(i).setVisibility(View.VISIBLE);
                    view = (ImageView) mViewList.get(i);
                } else mViewList.get(i).setVisibility(View.GONE);
            }
            return view;
        }

        public ProgressBar getProgressBar() {
            ProgressBar view = null;
            for (int i = 0; i < mViewList.size(); i++) {
                if (mViewList.get(i) instanceof ProgressBar) {
                    mViewList.get(i).setVisibility(View.VISIBLE);
                    view = (ProgressBar) mViewList.get(i);
                } else mViewList.get(i).setVisibility(View.GONE);
            }
            return view;
        }
    }

    public interface PlaySoundListener {
        void play(int pos);
    }

    public void setOnPlaySoundListener(PlaySoundListener listener) {
        mListener = listener;
    }


    public interface OnResendListener {
        void resend(int pos);
    }

    public void setOnResendListener(OnResendListener listener) {
        mRListener = listener;
    }


}