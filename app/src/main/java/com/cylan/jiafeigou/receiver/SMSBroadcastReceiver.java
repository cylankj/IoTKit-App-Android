package com.cylan.jiafeigou.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.util.Log;

import com.cylan.support.DswLog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 短信监听
 * Created by achievo on 2015/4/24.
 */
public class SMSBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "SMSBroadcastReceiver";
    private MessageListener mMessageListener;
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";

    public SMSBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "SMSBroadcastReceiver Received");
        try {
            if (intent.getAction().equals(SMS_RECEIVED_ACTION)) {
                Object[] pdus = (Object[]) intent.getExtras().get("pdus");
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String sender = smsMessage.getDisplayOriginatingAddress();
                    //短信内容
                    String content = smsMessage.getDisplayMessageBody();
                    //过滤不需要读取的短信的发送号码
                    if (content != null && (content.contains("加菲狗") || content.contains("Doby")
                            || content.contains("Clever Dog"))) {
                        mMessageListener.onReceived(getDynamicPass(content));
                        abortBroadcast();
                    }
                }
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }

    }

    //回调接口
    public interface MessageListener {
        void onReceived(String message);
    }

    public void setOnReceivedMessageListener(MessageListener messageListener) {
        this.mMessageListener = messageListener;
    }

    /**
     * 从字符串中截取连续6位数字组合 ([0-9]{" + 6 + "})截取六位数字 进行前后断言不能出现数字 用于从短信中获取动态密码
     *
     * @param str 短信内容
     * @return 截取得到的6位动态密码
     */
    public static String getDynamicPass(String str) {
        // 6是验证码的位数一般为六位
        Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{"
                + 6 + "})(?![0-9])");
        Matcher m = continuousNumberPattern.matcher(str);
        String dynamicPassword = "";
        while (m.find()) {
            dynamicPassword = m.group();
        }

        return dynamicPassword;
    }
}
