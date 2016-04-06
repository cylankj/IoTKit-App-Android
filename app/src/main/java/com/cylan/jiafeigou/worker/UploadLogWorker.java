package com.cylan.jiafeigou.worker;

import com.cylan.jiafeigou.utils.Utils;

/**
 * User: hope(hebin@cylan.com.cn)
 * Date: 2015-12-30
 * Time: 17:56
 */

public class UploadLogWorker implements Runnable {

    private String mUrl;

    public UploadLogWorker(String url) {
        this.mUrl = url;
    }

    @Override
    public void run() {
        Utils.sendLog(mUrl);
    }
}
