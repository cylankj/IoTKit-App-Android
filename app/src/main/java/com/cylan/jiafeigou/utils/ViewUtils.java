package com.cylan.jiafeigou.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-6-12.
 */

public class ViewUtils {

    private static int height;

    // A method to find height of the status bar
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getCompatStatusBarHeight(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && height == 0) {
            height = getStatusBarHeight(context);
            return height;
//            return getStatusBarHeight(context);
        } else return height;
    }

    /**
     * @param v
     * @param l
     * @param t
     * @param r
     * @param b
     */
    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(p.leftMargin + l, p.topMargin + t, p.rightMargin + r, p.bottomMargin + b);
            v.requestLayout();
        }
    }

    public static void setViewMarginStatusBar(View v) {
        final int height = getCompatStatusBarHeight(v.getContext());
        setMargins(v, 0, height, 0, 0);
    }

    /**
     * 不在需要marginStatusBar的高度
     *
     * @param v
     */
    public static void clearViewMarginStatusBar(View v) {
        final int height = getCompatStatusBarHeight(v.getContext());
        setMargins(v, 0, -height, 0, 0);
    }

    public static void setViewPaddingStatusBar(View v) {
        final int height = getCompatStatusBarHeight(v.getContext());
        v.setPadding(v.getPaddingLeft(), v.getPaddingTop() + height, v.getPaddingRight(), v.getPaddingBottom());
    }

    public static void showPwd(EditText text, boolean show) {
        text.setTransformationMethod(show ?
                HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance());
    }

    /**
     * 重新获取焦点，显示游标。
     *
     * @param editText
     * @param enable
     */
    public static void enableEditTextCursor(EditText editText, boolean enable) {
        editText.setFocusable(enable);
        editText.setFocusableInTouchMode(enable);
    }

    public static void setTextViewMaxFilter(final TextView textView, final int maxLen) {
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(maxLen);
        textView.setFilters(filterArray);
    }

    public static String getTextViewContent(TextView textView) {
        if (textView != null) {
            final CharSequence text = textView.getText();
            return text != null ? text.toString().trim() : "";
        }
        return "";
    }

    public static void deBounceClick(final View view) {
        if (view == null)
            return;
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view != null)
                    view.setEnabled(true);
            }
        }, 1000);
    }

    public static void updateViewHeight(View view, float ratio) {
        final int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = (int) (width * ratio);
        view.setLayoutParams(lp);
    }

    public static void updateViewMatchScreenHeight(View view) {
        final int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.height = height;
        view.setLayoutParams(lp);
    }


    public static void setRequestedOrientation(Activity activity, int orientation) {
        activity.setRequestedOrientation(orientation);
    }

    /**
     * @param tv
     * @param resId     :-1 means null
     * @param dimension left(0) top(1) right(2) bottom(3)
     */
    public static void setDrawablePadding(TextView tv, int resId, int dimension) {
        if (dimension < 0 || dimension > 3)
            throw new IllegalArgumentException("wow ,not so good");
        Drawable[] dimen = {null, null, null, null};
        Drawable d = resId == -1 ? null : tv.getContext().getResources().getDrawable(resId);
        if (d != null)
            d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
        //right
        dimen[dimension] = d;
        tv.setCompoundDrawables(dimen[0], dimen[1], dimen[2], dimen[3]);
    }

    /**
     * @param tv
     * @param drawable  :-1 means null
     * @param dimension left(0) top(1) right(2) bottom(3)
     */
    public static void setDrawablePadding(TextView tv, Drawable drawable, int dimension) {
        if (dimension < 0 || dimension > 3)
            throw new IllegalArgumentException("wow ,not so good");
        Drawable[] dimen = {null, null, null, null};
        if (drawable != null)
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        dimen[dimension] = drawable;
        tv.setCompoundDrawables(dimen[0], dimen[1], dimen[2], dimen[3]);
    }

    public static int dp2px(float dp) {
        return (int) (Resources.getSystem().getDisplayMetrics().density * dp + 0.5f);
    }

    /**
     * recyclerView的item中的某一个view,获取其最外层的viewParent，也就是item对应的layout在adapter中的position
     *
     * @param recyclerView
     * @param view
     * @return
     */
    public static int getParentAdapterPosition(RecyclerView recyclerView, View view, int parentId) {
        if (view.getId() == parentId)
            return recyclerView.getChildAdapterPosition(view);
        View viewGroup = (View) view.getParent();
        if (viewGroup != null && viewGroup.getId() == parentId) {
            return recyclerView.getChildAdapterPosition(viewGroup);
        }
        return getParentAdapterPosition(recyclerView, viewGroup, parentId);
    }

    public static void removeActivityFromTransitionManager(Activity activity) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        Class transitionManagerClass = TransitionManager.class;
        try {
            Field runningTransitionsField = transitionManagerClass.getDeclaredField("sRunningTransitions");
            runningTransitionsField.setAccessible(true);
            //noinspection unchecked
            ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>> runningTransitions
                    = (ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>>)
                    runningTransitionsField.get(transitionManagerClass);
            if (runningTransitions.get() == null || runningTransitions.get().get() == null) {
                return;
            }
            ArrayMap map = runningTransitions.get().get();
            View decorView = activity.getWindow().getDecorView();
            if (map.containsKey(decorView)) {
                map.remove(decorView);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setFitsSystemWindowsCompat(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setFitsSystemWindows(true);
        } else {
            view.setFitsSystemWindows(false);
        }
    }

    public static void setChineseExclude(TextView textView, final int maxLength) {
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (isChineseChar(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            }
        };
        textView.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(maxLength)});
    }

    /**
     * 汉字，不包含字符。
     * textView.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(maxLength)});
     * }
     * <p/>
     * /**
     *
     * @param c
     * @return
     */
    private static boolean isChineseChar(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS;
//                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
//                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
//                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
//                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
//                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
//                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }
}

