package com.cylan.jiafeigou.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.Constants;
import com.cylan.jiafeigou.activity.efamily.audio.AudioLoadCallback;
import com.cylan.jiafeigou.activity.efamily.audio.AudioLoader;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.EfamlMsg;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.CircleImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-09
 * Time: 09:28
 */

public class EFamilyMainAdapter extends BaseAdapter<EfamlMsg> {


    private static final String TAG = "EFamilyMainAdapter";
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("MM-dd", Locale.getDefault());
    private SimpleDateFormat mSimpleTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat mSecondFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());


    private AnimationDrawable animationDrawable = null;
    private SoundClick mSoundClick = null;
    private CallBackClick mCallBackClick = new CallBackClick();
    private ReSendMsg mReSendMsg = null;
    private ListView listview;
    private String mCid;
    private int playpos = -1;
    private boolean onClick = true;
    private AudioLoader mAudioLoader;
    private OnReSendListener mOnReSendListener = null;
    private OnCallBackListener mOnCallBackListener = null;

    public EFamilyMainAdapter(Context ctx, List<EfamlMsg> list) {
        super((Activity) ctx, list);
    }


    public void setListView(ListView listview) {
        this.listview = listview;
    }


    public void setCid(String cid) {
        this.mCid = cid;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        ViewHolder1 vh1 = null;
        ViewHolder2 vh2 = null;
        ViewHolder3 vh3 = null;
        if (convertView == null) {
            if (viewType == EfamlMsg.PASSIVE_CALL) {
                convertView = mInflater.inflate(R.layout.layout_efamily_left_video_call, parent, false);
                vh1 = new ViewHolder1(convertView);
                convertView.setTag(vh1);
            } else if (viewType == EfamlMsg.ACTIVE_CALL) {
                convertView = View.inflate(mContext, R.layout.layout_efamily_right_video_call, null);
                vh2 = new ViewHolder2(convertView);
                convertView.setTag(vh2);
            } else {
                convertView = View.inflate(mContext, R.layout.layout_efamily_right_word, null);
                vh3 = new ViewHolder3(convertView);
                convertView.setTag(vh3);
            }
        } else {
            if (viewType == EfamlMsg.PASSIVE_CALL) {
                vh1 = (ViewHolder1) convertView.getTag();
            } else if (viewType == EfamlMsg.ACTIVE_CALL) {
                vh2 = (ViewHolder2) convertView.getTag();
            } else {
                vh3 = (ViewHolder3) convertView.getTag();
            }
        }

        EfamlMsg msg = getItem(position);

        if (viewType == EfamlMsg.PASSIVE_CALL) {
            vh1.time.setText(getDate(msg.timeBegin));
            vh1.answer.setEnabled(onClick);
            vh1.missed.setEnabled(onClick);
//            mCallBackClick = new CallBackClick();
            if (msg.isRead == 1) {
                vh1.answer.setVisibility(View.VISIBLE);
                vh1.answer.setText(getCallDuration(msg.timeDuration));
                vh1.answer.setOnClickListener(mCallBackClick);
                vh1.missed.setVisibility(View.GONE);
                vh1.missed.setOnClickListener(null);
            } else {
                vh1.answer.setVisibility(View.GONE);
                vh1.missed.setVisibility(View.VISIBLE);
                vh1.missed.setText(R.string.EFAMILY_MISSED_CALL);
                vh1.missed.setOnClickListener(mCallBackClick);
            }
        } else if (viewType == EfamlMsg.ACTIVE_CALL) {
            vh2.time.setText(getDate(msg.timeBegin));
            MyImageLoader.loadImageFromNet(Utils.getAccountHeadPicUrl(), vh2.headpic);
            vh2.answer.setEnabled(onClick);
            vh2.missed.setEnabled(onClick);
//            mCallBackClick = new CallBackClick();
            if (msg.isRead == 1) {
                vh2.answer.setVisibility(View.VISIBLE);
                vh2.answer.setText(getCallDuration(msg.timeDuration));
                vh2.answer.setOnClickListener(mCallBackClick);
                vh2.missed.setVisibility(View.GONE);
                vh2.missed.setOnClickListener(null);
            } else {
                vh2.answer.setVisibility(View.GONE);
                vh2.missed.setVisibility(View.VISIBLE);
                vh2.missed.setText(R.string.DOOR_UNCALL);
                vh2.missed.setOnClickListener(mCallBackClick);
            }
        } else {
            mSoundClick = new SoundClick(position);
            vh3.mWordLayout.setOnClickListener(mSoundClick);
            vh3.mWordLayout.setClickable(onClick);
            vh3.time.setText(getDate(msg.timeBegin));
            vh3.length.setText(String.format("%d\"", msg.timeDuration));
            resetPlayViewState(vh3.sound, msg);
            MyImageLoader.loadImageFromNet(Utils.getAccountHeadPicUrl(), vh3.headpic);
            if (msg.send_state == EfamlMsg.SENDFAIL) {
                Log.d("EFamilyMainAdapter", "SENDFAIL" + " position-->" + position);
                vh3.sendFailImageView.setVisibility(View.VISIBLE);
                vh3.isReadTextView.setVisibility(View.GONE);
                vh3.progressBar.setVisibility(View.GONE);
            } else if (msg.send_state == EfamlMsg.SENDING) {
                vh3.sendFailImageView.setVisibility(View.GONE);
                vh3.isReadTextView.setVisibility(View.GONE);
                vh3.progressBar.setVisibility(View.VISIBLE);
            } else {
                vh3.sendFailImageView.setVisibility(View.GONE);
                vh3.isReadTextView.setVisibility(View.GONE);
                vh3.progressBar.setVisibility(View.GONE);
            }

            if (msg.isRead == 1) {
                vh3.isReadTextView.setVisibility(View.VISIBLE);
            } else {
                vh3.isReadTextView.setVisibility(View.GONE);
            }
            mReSendMsg = new ReSendMsg(msg, position);
            vh3.sendFailImageView.setOnClickListener(mReSendMsg);
            vh3.sendFailImageView.setClickable(onClick);
        }


        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        if (getData().get(position).msgType <= EfamlMsg.PASSIVE_CALL) {
            return getData().get(position).msgType;
        } else {
            return 0;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    public class ViewHolder1 {
        TextView time;
        TextView name;
        TextView answer;
        TextView missed;
        CircleImageView headpic;

        public ViewHolder1(View itemView) {
            time = (TextView) itemView.findViewById(R.id.time);
            name = (TextView) itemView.findViewById(R.id.name);
            answer = (TextView) itemView.findViewById(R.id.answer_view);
            missed = (TextView) itemView.findViewById(R.id.missed_view);
            headpic = (CircleImageView) itemView.findViewById(R.id.headpic);
        }
    }

    public class ViewHolder2 {
        TextView time;
        TextView name;
        TextView answer;
        TextView missed;
        CircleImageView headpic;


        public ViewHolder2(View itemView) {
            time = (TextView) itemView.findViewById(R.id.time);
            name = (TextView) itemView.findViewById(R.id.name);
            answer = (TextView) itemView.findViewById(R.id.answer_view);
            missed = (TextView) itemView.findViewById(R.id.missed_view);
            headpic = (CircleImageView) itemView.findViewById(R.id.headpic);
        }
    }

    public class ViewHolder3 {
        TextView time;
        TextView length;
        ImageView sound;
        CircleImageView headpic;
        ProgressBar progressBar;
        ImageView sendFailImageView;
        TextView isReadTextView;
        LinearLayout mWordLayout;


        public ViewHolder3(View itemView) {
            time = (TextView) itemView.findViewById(R.id.time);
            length = (TextView) itemView.findViewById(R.id.length_view);
            sound = (ImageView) itemView.findViewById(R.id.sound_view);
            headpic = (CircleImageView) itemView.findViewById(R.id.headpic);
            progressBar = (ProgressBar) itemView.findViewById(R.id.word_sending);
            sendFailImageView = (ImageView) itemView.findViewById(R.id.word_sendfail);
            isReadTextView = (TextView) itemView.findViewById(R.id.word_read);
            mWordLayout = (LinearLayout) itemView.findViewById(R.id.layout_word);
        }
    }

    private class SoundClick implements View.OnClickListener {

        private int mPos;

        public SoundClick(int pos) {
            this.mPos = pos;
        }

        @Override
        public void onClick(View v) {
            getData().get(mPos).isPlay = !getData().get(mPos).isPlay;
            if (playpos != -1 && playpos < getData().size()) {
                if (getData().get(playpos).isPlay && getData().get(mPos).isPlay && playpos != mPos) {
                    getData().get(playpos).isPlay = false;
                    updataWordPlayView(playpos, listview);
                    updataWordPlayState(playpos);
                }
            }
            if (getData().get(mPos).isPlay) {
                playpos = mPos;
            }
            updataWordPlayView(mPos, listview);
            if (playpos != -1)
                updataWordPlayState(playpos);
        }
    }


    private class CallBackClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mOnCallBackListener != null)
                mOnCallBackListener.callback();
        }

    }

    private class ReSendMsg implements View.OnClickListener {
        private EfamlMsg mEfamlMsg;
        private int position;

        public ReSendMsg(EfamlMsg efamlMsg, int position) {
            this.mEfamlMsg = efamlMsg;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (mOnReSendListener != null) {
                mOnReSendListener.resend(mEfamlMsg, position);
            }
        }

    }

    private String getDate(long timebegin) {
        String d = mSimpleDateFormat.format(new Date(timebegin * 1000));
        String td = mSimpleDateFormat.format(new Date(System.currentTimeMillis()));
        String data = mDateFormat.format(new Date(timebegin * 1000));
        String time = mSimpleTimeFormat.format(new Date(timebegin * 1000));
        return d.equals(td) ? time : data;
    }


    private String getCallDuration(int timelength) {
        StringBuilder builder = new StringBuilder();
        builder.append(mContext.getString(R.string.EFAMILY_CALL_DURATION));
        builder.append("\t");
        int min = timelength / 60;
        int sec = timelength % 60;
        if (Utils.getLanguageType(mContext) == Constants.LANGUAGE_TYPE_CHINESE) {
            if (sec == 0) {
                builder.append(mContext.getString(R.string.EFAMILY_CALL_DURATION_M, min));
            } else if (timelength < 60) {
                builder.append(mContext.getString(R.string.EFAMILY_CALL_DURATION_S, timelength));
            } else {
                builder.append(mContext.getString(R.string.EFAMILY_CALL_DURATION_M, min));
                builder.append(mContext.getString(R.string.EFAMILY_CALL_DURATION_S, sec));
            }
        } else {
            builder.append(mContext.getString(R.string.EFAMILY_CALL_DURATION_M, min));
            builder.append(mContext.getString(R.string.EFAMILY_CALL_DURATION_S, sec));
        }
        return builder.toString();
    }


    public void updataWordPlayView(int posi, ListView listView) {
        if (listView == null)
            return;
        int visibleFirstPosi = listView.getFirstVisiblePosition();
        int visibleLastPosi = listView.getLastVisiblePosition();
        if (posi >= visibleFirstPosi && posi <= visibleLastPosi) {
            View view = listView.getChildAt(posi - visibleFirstPosi);
            if (getItemViewType(posi) == EfamlMsg.MSG_WORD) {
                ViewHolder3 holder = (ViewHolder3) view.getTag();
                resetPlayViewState(holder.sound, getData().get(posi));
            }
        }
    }

    public void resetPlayViewState(ImageView img, EfamlMsg mEfamlMsg) {
        if (mEfamlMsg.isPlay) {
            img.setImageResource(R.drawable.efamily_word_anim);
            animationDrawable = (AnimationDrawable) img.getDrawable();
            animationDrawable.start();
        } else {
            if (img.getDrawable() instanceof AnimationDrawable) {
                ((AnimationDrawable) img.getDrawable()).stop();
            }
            img.setImageResource(R.drawable.ico_efamily_sound3);
        }
    }

    public void updataWordPlayState(int posi) {
        if (getData().get(posi).isPlay) {
            onWordStart(getData().get(posi));
        } else {
            if (mAudioLoader != null) {
                mAudioLoader.stopPlaying();
            }
        }

    }

    private void onWordStart(EfamlMsg mEfamlMsg) {
        if (mCid == null)
            return;
        if (mAudioLoader == null) {
            mAudioLoader = new AudioLoader(mContext, mCid, new AudioLoadCallback() {
                @Override
                public void start(EfamlMsg mEfamlMsg) {
                }

                @Override
                public void stopOther() {
                    resetAll();
                }

                @Override
                public void stopNoFile() {
                    if (!MyApp.getIsLogin()){
                        ToastUtil.showFailToast(mContext, "(-" + MyApp.getMsgID() + ")" + mContext.getString(R.string.GLOBAL_NO_NETWORK));
                    }
                }
            });
        }
        mAudioLoader.loadAudio(mEfamlMsg);
    }

    private void resetAll() {
        for (int i = 0, count = getData().size(); i < count; i++) {
            getData().get(i).isPlay = false;
//            getData().get(i).send_state=EfamlMsg.SENDSUC;
            updataWordPlayView(i, listview);
        }
        notifyDataSetChanged();
    }

    /**
     * 停止播放并停止动画
     */
    public void distory() {
        if (mAudioLoader != null) {
            mAudioLoader.stopPlaying();
        }
        if (animationDrawable != null && animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        resetAll();
    }

    public interface OnReSendListener {
        void resend(EfamlMsg mEfamlMsg, int position);
    }

    public void setOnReSendListener(OnReSendListener rslistener) {
        mOnReSendListener = rslistener;
    }

    public interface OnCallBackListener {
        void callback();
    }

    public void setOnCallBackListener(OnCallBackListener rslistener) {
        mOnCallBackListener = rslistener;
    }

    public void setOnClick(boolean onClick){
        this.onClick = onClick;
        notifyDataSetChanged();
    }
}
