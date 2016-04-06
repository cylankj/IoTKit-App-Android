package com.cylan.jiafeigou.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;

/**
 * @deprecated
 */
public class PlaySoundBtn extends LinearLayout {
	private TextView mTimeView;
	private ImageView mAnimView;
	private Context mContext;

	AnimationDrawable mAnim;

	public PlaySoundBtn(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		LayoutInflater.from(context).inflate(R.layout.layout_playbtn, this, true);
		mTimeView = (TextView) findViewById(R.id.timelength);
		mAnimView = (ImageView) findViewById(R.id.time_ico);
	}

	public PlaySoundBtn(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PlaySoundBtn(Context context) {
		this(context, null);
	}

	public void setTextColor(int color) {
		mTimeView.setTextColor(color);
	}

	public void setBtnStyle(boolean isListener) {
		if (isListener) {
			setBackgroundResource(R.drawable.btn_doorbell_read_selector);
			mTimeView.setTextColor(mContext.getResources().getColor(R.color.agreeement_title_color));
			mAnimView.setImageResource(R.drawable.listener_sound);
		} else {
			setBackgroundResource(R.drawable.btn_doorbell_unread_selector);
			mTimeView.setTextColor(mContext.getResources().getColor(R.color.delete_color));
			mAnimView.setImageResource(R.drawable.ico_unread);
		}
	}

	@Override
	public void setBackgroundResource(int resid) {
		super.setBackgroundResource(resid);
	}

	public void clickPlayStart() {
		if (mAnimView.getDrawable() instanceof AnimationDrawable) {
			AnimationDrawable mAnim = (AnimationDrawable) mAnimView.getDrawable();
			if (mAnim != null) {

				mAnim.start();
			}
		}
	}

	public void clickPlayStop() {
		if (mAnimView.getDrawable() instanceof AnimationDrawable) {
			AnimationDrawable mAnim = (AnimationDrawable) mAnimView.getDrawable();
			if (mAnim != null) {
				if (mAnim.isRunning()) {
					mAnim.selectDrawable(0);
					mAnim.stop();
				}
			}
		}
	}
}
