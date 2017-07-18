package com.cylan.jiafeigou.n.view.cam;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

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

    public static void show(Activity activity, View anchor0, View anchor1) {
        if (!anchor0.isShown()) return;
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
        if (!anchor.isShown()) return;
        boolean result = PreferencesUtils.getBoolean(KEY_SHOW_HISTORY_CASE, true);
        PreferencesUtils.putBoolean(KEY_SHOW_HISTORY_CASE, false);
        if (result) {
            anchor.post(() -> {
                SimplePopupWindow right = new SimplePopupWindow(activity, R.drawable.collect_tips,
                        R.string.Tap1_Camera_BackLiveTips);
                right.showOnAnchor(anchor, RelativePopupWindow.VerticalPosition.ALIGN_TOP,
                        RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT);
            });
        }
    }

    public static void showSafeCase(Activity activity, View anchor) {
        if (!anchor.isShown()) return;
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
        boolean result = PreferencesUtils.getBoolean(JConstant.KEY_SHOW_HISTORY_WHEEL_CASE, true);
        PreferencesUtils.putBoolean(JConstant.KEY_SHOW_HISTORY_WHEEL_CASE, false);
        if (result /*|| true*/) {
            HistoryWheelShowCaseFragment fragment = new HistoryWheelShowCaseFragment();
            if (activity instanceof FragmentActivity) {
                fragment.setAnchor(handAnchor);
                ((FragmentActivity) activity).getSupportFragmentManager()
                        .beginTransaction()
                        .add(android.R.id.content, fragment, HistoryWheelShowCaseFragment.class.getSimpleName())
                        .addToBackStack(HistoryWheelShowCaseFragment.class.getSimpleName())
                        .commit();
            }
        }
    }

    public static void hideHistoryWheelCase(Activity activity) {
        if (activity instanceof FragmentActivity) {
            Fragment fragmentByTag = ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(HistoryWheelShowCaseFragment.class.getSimpleName());
            if (fragmentByTag != null) {
                ((FragmentActivity) activity).getSupportFragmentManager().beginTransaction().remove(fragmentByTag).commit();
            }
        }
    }
}
