package com.cylan.jiafeigou.misc;

import android.content.Context;
import android.view.OrientationEventListener;

/**
 * Created by yanzhendong on 2017/8/16.
 */

public class OrientationListener extends OrientationEventListener {
    public OrientationListener(Context context) {
        super(context);
    }

    @Override
    public void onOrientationChanged(int orientation) {

    }

//
//    public OrientationListener(Context context) {
//        super(context);
//    }
//
//    public OrientationListener(Context context, int rate) {
//        super(context, rate);
//    }
//
//    @Override
//    public void onOrientationChanged(int orientation) {
//        Log.d(TAG, "orention" + orientation);
//        int screenOrientation = getResources().getConfiguration().orientation;
//        if (((orientation >= 0) && (orientation < 45)) || (orientation > 315)) {//设置竖屏
//            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && orientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
//                Log.d(TAG, "设置竖屏");
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                oriBtn.setText("竖屏");
//            }
//        } else if (orientation > 225 && orientation < 315) { //设置横屏
//            Log.d(TAG, "设置横屏");
//            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                oriBtn.setText("横屏");
//            }
//        } else if (orientation > 45 && orientation < 135) {// 设置反向横屏
//            Log.d(TAG, "反向横屏");
//            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//                oriBtn.setText("反向横屏");
//            }
//        } else if (orientation > 135 && orientation < 225) {
//            Log.d(TAG, "反向竖屏");
//            if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
//                oriBtn.setText("反向竖屏");
//            }
//        }
//    }
}
