package com.cylan.jiafeigou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

public class HeadsetPlugObserver {
	private Context mContext;
	private IntentFilter mIntentFilter;
	private OnHeadsetPlugListener mOnHeadsetPlugListener;
	private HeadsetPlugReceiver mHeadsetPlugReceiver;

	public HeadsetPlugObserver(Context context) {
		this.mContext = context;
	}

	// 注册广播接收者
	public void startListen() {
		mIntentFilter = new IntentFilter("android.intent.action.HEADSET_PLUG");
		mHeadsetPlugReceiver = new HeadsetPlugReceiver();
		mContext.registerReceiver(mHeadsetPlugReceiver, mIntentFilter);
		System.out.println("----> 开始监听");
		firstGetHeadsetPlug();
	}

	// 取消广播接收者
	public void stopListen() {
		if (mHeadsetPlugReceiver != null) {
			mContext.unregisterReceiver(mHeadsetPlugReceiver);
			System.out.println("----> 停止监听");

		}
	}

	private void firstGetHeadsetPlug() {
		AudioManager localAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		if (localAudioManager.isWiredHeadsetOn()) {
			if (mOnHeadsetPlugListener != null)
				mOnHeadsetPlugListener.onHeadsetPlugOn();
			Log.d("MSG_JSON", "headset connected");
		} else {
			if (mOnHeadsetPlugListener != null)
				mOnHeadsetPlugListener.onHeadsetPlugOff();
			Log.d("MSG_JSON", "headset not connected");
		}
	}

	// 对外暴露接口
	public void setHeadsetPlugListener(OnHeadsetPlugListener homeKeyListener) {
		mOnHeadsetPlugListener = homeKeyListener;
	}

	// 回调接口
	public interface OnHeadsetPlugListener {
		void onHeadsetPlugOn();

		void onHeadsetPlugOff();
	}

	class HeadsetPlugReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra("state")) {
				if (intent.getIntExtra("state", 0) == 0) {
					Log.d("MSG_JSON", "headset not connected");
					mOnHeadsetPlugListener.onHeadsetPlugOff();

				} else if (intent.getIntExtra("state", 0) == 1) {
					Log.d("MSG_JSON", "headset connected");
					mOnHeadsetPlugListener.onHeadsetPlugOn();
				}
			}
		}
	}

}