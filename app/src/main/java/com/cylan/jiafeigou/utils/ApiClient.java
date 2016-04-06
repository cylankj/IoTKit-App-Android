package com.cylan.jiafeigou.utils;

import android.content.Context;

import com.cylan.publicApi.DswLog;
import com.cylan.jiafeigou.entity.Update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class ApiClient {
    /**
     * get请求URL
     *
     * @param url
     */
    public String _get(String url) {
        if (StringUtils.isEmpty(url))
            return "";
        try {
            StringBuilder resultData = new StringBuilder("");
            URL mUrl = new URL(url);
            HttpURLConnection urlConn = (HttpURLConnection) mUrl.openConnection();
            urlConn.setRequestMethod("GET");
            //inputStreamReader一个个字节读取转为字符,可以一个个字符读也可以读到一个buffer
            //getInputStream是真正去连接网络获取数据
            InputStreamReader isr = new InputStreamReader(urlConn.getInputStream());

            //使用缓冲一行行的读入，加速InputStreamReader的速度
            BufferedReader buffer = new BufferedReader(isr);
            String inputLine = null;

            while ((inputLine = buffer.readLine()) != null) {
                resultData.append(inputLine);
                resultData.append("\n");
            }
            buffer.close();
            isr.close();
            urlConn.disconnect();
            return resultData.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            DswLog.ex(e.toString());
        }
        return "";

    }

    /**
     * 检查版本更新
     *
     * @param url
     * @return
     */
    public Update checkVersion(Context ctx, String url) {
        try {
            String str = _get(url);

            DswLog.i("checkVersion return data--->" + str);
            return Update.parse(ctx, str);
        } catch (Exception e) {
            e.getStackTrace();
        } catch (Throwable e) {
            e.getStackTrace();
        }
        return null;
    }

}
