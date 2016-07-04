package com.cylan.jiafeigou.widget.wheel;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cylan-hunt on 16-6-22.
 */

public class WheelViewDataSet {
    /**
     * 短，无数据。
     */
    static final int TYPE_SHORT_INVALID = 0;
    /**
     * 短，有数据。
     */
    static final int TYPE_SHORT_VALID = 1;
    /**
     * 长，无数据。
     */
    static final int TYPE_LONG_INVALID = 2;
    /**
     * 长，有数据。
     */
    static final int TYPE_LONG_VALID = 3;

    public long[] dataSet;

    public int[] dataTypeSet;

    public float[] dataStartXSet;
    /**
     * position
     * dateInStr
     */
    public Map<String, String> dateInStr = new HashMap<>();
    /**
     * 只保存{0,1}， 0：无数据，1:有数据
     */
    public int type0Color = Color.BLUE;
    /**
     * 有数据
     */
    public int type1Color = Color.RED;

}
