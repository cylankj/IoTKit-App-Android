package com.cylan.jiafeigou.widget.wheel;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cylan-hunt on 16-7-23.
 */

public class SDataStack {

    ArrayList<Long> naturalDateSet;
    /**
     * 日期
     */
    float[] naturalDateSetPosition;

    /**
     * 整点 需要draw text  {0: 整点 ，1:普通}
     */
    int[] naturalDateType;

    HashMap<String, String> dateStringMap;
    /**
     * 含有录像的数据 {0,1} {2,3},{4,5}...
     * ,此数据总数必须是偶数
     */
    long[] recordTimeSet;

    float[] recordTimePositionSet;
}
