package com.cylan.jiafeigou.utils;

import com.cylan.entity.jniCall.JFGDPMsg;

import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    @Test
    public void testWechat() {


//        // APP_ID和APP_Secret在微信开发平台添加应用的时候会生成，grant_type 用默认的"authorization_code"即可.
//        String urlStr = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" +
//                "wx3081bcdae8a842cf" + "&secret=" + "d93676ab7db1876c06800dee3f33fbc2" +
//                "&code=" + code + "&grant_type=authorization_code";
//
        Request request = new Request.Builder()
                .url("https://dl-sh-ctc-2.pchome.net/0n/aq/yyb703.apk?key=55855a194d1b8246c59944917c67796c&tmp=1496915726025")
                .build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            long l = response.body().contentLength();
            System.out.println("what?" + l);
        } catch (IOException e) {
        }
    }
}