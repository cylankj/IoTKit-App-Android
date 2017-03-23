package com.cylan.jiafeigou.utils;

import com.cylan.entity.jniCall.JFGDPMsg;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by holy on 2017/3/22.
 */
public class MiscUtilsTest {
    @Test
    public void getCamDateVersionList() throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        List<JFGDPMsg> list = MiscUtils.getCamDateVersionList(calendar.getTimeInMillis());
        for (int i = 0; i < 45; i++)
            System.out.println("" + format.format(new Date(list.get(i).version)));
    }

}