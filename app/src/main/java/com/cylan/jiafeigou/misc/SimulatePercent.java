package com.cylan.jiafeigou.misc;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.Random;

/**
 * Created by cylan-hunt on 16-8-27.
 */
public class SimulatePercent implements Handler.Callback {

    private static final String TAG = "SimulatePercent";
    private static final int MSG_STEP_0 = 0;
    private static final int MSG_STEP_1 = 1;
    private int index;
    private final int percent;
    private int animateStep;//动画执行的步骤,共有:发送bind 请求,收到服务器响应,收到设备上线;


    public SimulatePercent() {
        percent = new Random().nextInt(30) + 60;
        //选择方案，
        HandlerThread handlerThread = new HandlerThread("simulate");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), this);
    }

    private Handler handler;

    public void start() {
        index = 0;
        handler.removeCallbacksAndMessages(null);
        handler.sendEmptyMessage(MSG_STEP_0);
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
    }

    private OnAction onAction;
    private Runnable finishAction;

    public void setOnAction(OnAction onAction) {
        this.onAction = onAction;
    }

    public void setStep(int step) {
        if (step == 0) {//正在发送 bind 请求,完成度:0到30;

        }
    }

    public interface OnAction {
        void actionDone();

        void actionPercent(int percent);
    }

    /**
     * 从当前位置快速上升到100
     */
    public void boost() {
        handler.removeMessages(MSG_STEP_0);
        delay = index == 100 ? 1 : 2000 / (100 - index);//2s内完成
        handler.sendEmptyMessage(MSG_STEP_1);
    }

    public void boost(Runnable finishAction) {
        this.finishAction = finishAction;
        boost();
    }

    public int getProgress() {
        return index;
    }

    public void resume() {

    }

    public void pause() {

    }

    private int delay;

    private long getRandomDelay() {
        return new Random().nextInt(500) + 200;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_STEP_0:
                index++;
                if (index > percent) {
                    return true;
                }
                if (onAction != null) {
                    onAction.actionPercent(index);
                }
                handler.sendEmptyMessageDelayed(MSG_STEP_0, getRandomDelay());
                break;
            case MSG_STEP_1:
                index++;
                if (index >= 100) {
                    index = 100;
                    if (onAction != null) {
                        onAction.actionPercent(index);
                        onAction.actionDone();
                    }
                    if (finishAction != null) {
                        finishAction.run();
                    }
                    handler.removeMessages(MSG_STEP_1);
                    return true;
                }
                if (onAction != null) {
                    onAction.actionPercent(index);
                }
                if (delay == 0) {
                    delay = 50;
                }
                handler.sendEmptyMessageDelayed(MSG_STEP_1, delay);
                break;
        }
        return true;
    }
}
