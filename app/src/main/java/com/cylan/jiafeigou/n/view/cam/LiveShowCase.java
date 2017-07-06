package com.cylan.jiafeigou.n.view.cam;

import android.app.Activity;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.pop.RelativePopupWindow;
import com.cylan.jiafeigou.widget.pop.SimplePopupWindow;

/**
 * Created by hds on 17-6-28.
 */

public class LiveShowCase {
    private static final String KEY_SHOW_CASE = "key_show_case";

    public static void show(Activity activity, View anchor0, View anchor1) {
        boolean result = PreferencesUtils.getBoolean(KEY_SHOW_CASE, true);
        PreferencesUtils.putBoolean(KEY_SHOW_CASE, false);
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
}
