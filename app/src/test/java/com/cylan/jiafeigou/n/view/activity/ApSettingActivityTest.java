package com.cylan.jiafeigou.n.view.activity;


import android.widget.TextView;

import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.RandomUtils;

/**
 * Created by hds on 17-9-5.
 */
public class ApSettingActivityTest {


    public void test() {
        TextView tv = new TextView(ContextUtils.getContext());
        tv.setOnClickListener(v -> {
            int r = RandomUtils.getRandom(20);
            if (r > 20) {
                System.out.println("");
                return;
            }
            System.out.println("");
        });
    }
}