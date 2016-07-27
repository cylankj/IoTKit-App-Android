package com.cylan.jiafeigou.widget.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.cylan.jiafeigou.R;
import com.cylan.utils.DensityUtils;

/**
 * Created by cylan-hunt on 16-7-26.
 */
public class BaseDialog extends DialogFragment {

    private static final float MIN_HEIGHT = 0.17F;
    private static final float MAX_HEIGHT = 0.475F;
    private int minHeight = 0;
    private int maxWidth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.l_style_dialog);
        setCancelable(true);
        maxWidth = (int) (DensityUtils.getScreenWidth() * 0.78f);
        minHeight = (int) (DensityUtils.getScreenHeight() * MIN_HEIGHT);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow()
                .setLayout(maxWidth, minHeight);
    }
}
