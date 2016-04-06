package com.cylan.jiafeigou.entity;

import com.cylan.jiafeigou.entity.msg.EfamlMsg;

import java.util.List;

/**
 * Created by HeBin on 2015/5/8.
 */
public class HttpRequest {

    private EfamlMsg mEfamlMsg;
    private int requestid;

    public HttpRequest(EfamlMsg timeBegin, int requestid) {
        this.mEfamlMsg = timeBegin;
        this.requestid = requestid;
    }

    public EfamlMsg getTimeBegin() {
        return mEfamlMsg;
    }

    public void setTimeBegin(EfamlMsg timeBegin) {
        this.mEfamlMsg = timeBegin;
    }

    public int getRequestid() {
        return requestid;
    }

    public void setRequestid(int requestid) {
        this.requestid = requestid;
    }


    public static EfamlMsg queryTimeBeginByRequestid(List<HttpRequest> mList, int rId) {
        if (mList == null)
            return null;
        for (HttpRequest hr : mList) {
            if (hr.getRequestid() == rId) {
                return hr.mEfamlMsg;
            }
        }
        return null;
    }
}
