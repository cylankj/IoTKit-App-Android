package com.cylan.jiafeigou.utils;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/8 15:47
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class ContinuityClickUtils {

    /**
     * 防止用户连续的点击操作所造成的程序崩溃退出
     */
    private static long lastClickTime;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 2000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
