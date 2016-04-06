package com.cylan.jiafeigou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created by yangc on 2015/7/23.
 */
public class PhoneBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "PhoneReceiver";
    private boolean isInComingFlag = false;
    private String inComingNum = null;
    private Intent send = null;

    public static final String SEND_ACTION = "send_action";

    public static final String CALL_STATE_RINGING = "CALL_STATE_RINGING";
    public static final String CALL_STATE_OFFHOOK = "CALL_STATE_OFFHOOK";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            isInComingFlag = false;
            String phoneNum = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG, "Call Num:" + phoneNum);
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                    Context.TELEPHONY_SERVICE);
            switch (telephonyManager.getCallState()) {
                case TelephonyManager.CALL_STATE_RINGING:
                    isInComingFlag = true;
                    inComingNum = intent.getStringExtra("incoming_number");
                    Log.d(TAG, "InComingNum:" + inComingNum);
                    send = new Intent();
                    send.setAction(SEND_ACTION);
                    send.putExtra(TAG, CALL_STATE_RINGING);
                    context.sendBroadcast(send);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (isInComingFlag) {
                        Log.d(TAG, "inComing accept:" + inComingNum);
                    }
                    send = new Intent();
                    send.setAction(SEND_ACTION);
                    send.putExtra(TAG, CALL_STATE_OFFHOOK);
                    context.sendBroadcast(send);
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (isInComingFlag) {
                        Log.d(TAG, "inComing idle");
                    }
                    break;
            }
        }
    }
}
