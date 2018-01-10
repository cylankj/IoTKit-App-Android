package com.cylan.jiafeigou.n.view.cam;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.PopupWindowCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.SimplePopupWindow;

/**
 * Created by hds on 17-6-28.
 */

public class LiveShowCase {
    private static final String KEY_SHOW_HISTORY_CASE = "key_show_history_case";
    private static final String KEY_SHOW_SAFE_CASE = "key_show_safe_case";
    private static PopupWindow popupWindow;

    public static void show(Activity activity, View anchor0, View anchor1) {
        if (!anchor0.isShown()) {
            return;
        }
        boolean result = PreferencesUtils.getBoolean(KEY_SHOW_HISTORY_CASE, true);
        PreferencesUtils.putBoolean(KEY_SHOW_HISTORY_CASE, false);
        if (result) {
            anchor0.post(() -> {
                SimplePopupWindow left = new SimplePopupWindow(activity, R.drawable.collect_tips_left,
                        R.string.Tap1_Camera_SetProtectionTips);
                left.showOnAnchor(anchor0, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                        RelativePopupWindow.HorizontalPosition.ALIGN_LEFT, 20, -anchor0.getHeight());
                SimplePopupWindow right = new SimplePopupWindow(activity, R.drawable.collect_tips,
                        R.string.Tap1_Camera_BackLiveTips);
                right.showOnAnchor(anchor1, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                        RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT);
            });
        }
    }

    public static void showHistoryCase(Activity activity, View anchor) {
        if (!anchor.isShown()) {
            return;
        }
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        PreferencesUtils.putBoolean(KEY_SHOW_HISTORY_CASE, false);
        if (popupWindow == null) {
            TextView contentView = (TextView) View.inflate(activity, R.layout.layout_tips_popup, null);
            contentView.setBackgroundResource(R.drawable.collect_tips);
            contentView.setText(R.string.Tap1_Camera_BackLiveTips);
            contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            // Disable default animation for circular reveal
            popupWindow = new PopupWindow(contentView, contentView.getMeasuredWidth(), contentView.getMeasuredWidth());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.setAnimationStyle(0);
            }
            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        anchor.post(() -> {
            View contentView = popupWindow.getContentView();
            Log.d("AAAAA", "a:" + anchor.getMeasuredWidth() + ",b:" + contentView.getMeasuredHeight());
            int yoff = anchor.getMeasuredHeight() + contentView.getMeasuredHeight();
            PopupWindowCompat.showAsDropDown(popupWindow, anchor, 0, -yoff, Gravity.TOP | Gravity.RIGHT);
        });
    }

    public static void showSafeCase(Activity activity, View anchor) {
        if (true) {
            return;//不再显示 安全防护 tips
        }
        if (!anchor.isShown()) {
            return;
        }
        boolean result = PreferencesUtils.getBoolean(KEY_SHOW_SAFE_CASE, true);
        PreferencesUtils.putBoolean(KEY_SHOW_SAFE_CASE, false);
        if (result) {
            anchor.post(() -> {
                SimplePopupWindow left = new SimplePopupWindow(activity, R.drawable.collect_tips_left,
                        R.string.Tap1_Camera_SetProtectionTips);
                left.showOnAnchor(anchor, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                        RelativePopupWindow.HorizontalPosition.ALIGN_LEFT, 20, -anchor.getHeight() + 10);
            });
        }
    }

    public static void showHistoryWheelCase(Activity activity, View handAnchor) {
        if (activity instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            HistoryWheelShowCaseFragment historyCaseFragment = (HistoryWheelShowCaseFragment) fragmentManager.findFragmentByTag(HistoryWheelShowCaseFragment.class.getSimpleName());
            if (historyCaseFragment == null) {
                historyCaseFragment = new HistoryWheelShowCaseFragment();
                historyCaseFragment.setAnchor(handAnchor);
                fragmentManager.beginTransaction()
                        .add(android.R.id.content, historyCaseFragment, HistoryWheelShowCaseFragment.class.getSimpleName())
                        .addToBackStack(HistoryWheelShowCaseFragment.class.getSimpleName())
                        .commit();
            } else {
                historyCaseFragment.setAnchor(handAnchor);
            }
        }
    }

    public static void hideHistoryWheelCase(Activity activity) {
        PreferencesUtils.putBoolean(JConstant.KEY_SHOW_HISTORY_WHEEL_CASE, false);
        if (activity instanceof FragmentActivity) {
            Fragment fragmentByTag = ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(HistoryWheelShowCaseFragment.class.getSimpleName());
            if (fragmentByTag != null) {
                ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction().remove(fragmentByTag).commit();
            }
        }
    }

    public static void hideHistoryCase(Activity context) {
        PreferencesUtils.putBoolean(KEY_SHOW_HISTORY_CASE, false);
        if (popupWindow != null) {
            popupWindow.dismiss();
        }

    }
}
