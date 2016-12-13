package com.cylan.jiafeigou.widget.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.utils.DensityUtils;

/**
 * Created by cylan-hunt on 16-7-26.
 */
public class BaseDialog<T> extends DialogFragment {

    public static final String KEY_TITLE = "key_title";
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

    /**
     * @return {@link android.view.WindowManager.LayoutParams#WRAP_CONTENT},{@link android.view.WindowManager.LayoutParams#MATCH_PARENT},
     * 或者一个具体的数字
     */
    protected int getCustomHeight() {
        return minHeight;
    }

    protected int getCustomWidth() {
        return 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow()
                    .setLayout(getCustomWidth() == 0 ? maxWidth : getCustomWidth(), ViewGroup.LayoutParams.WRAP_CONTENT
                    );
    }

    public void setAction(BaseDialogAction action) {
        this.action = action;
    }

    protected BaseDialogAction action;

    protected T value;

    public void setValue(T value) {
        this.value = value;
    }

    public interface BaseDialogAction {
        void onDialogAction(int id, Object value);
    }
}
