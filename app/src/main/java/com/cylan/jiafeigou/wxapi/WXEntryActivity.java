package com.cylan.jiafeigou.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.utils.PackageUtils;
import com.google.gson.Gson;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    IWXAPI wxApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果分享的时候，该界面没有开启，那么微信开始这个activity时，
        // 会调用onCreate，所以这里要处理微信的返回结果
        String appId = PackageUtils.getMetaString(ContextUtils.getContext(), "weChatAppId");
        if (TextUtils.isEmpty(appId)) {
            AppLogger.i("wechat app id is null");
        }
        Log.d("WechatShare", "WechatShare: " + appId);
        // 第三个参数作用:checkSignature
        //这个context 必须是Activity，或者Service
        wxApi = WXAPIFactory.createWXAPI(this, appId, true);
        wxApi.registerApp(appId);
        wxApi.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // 如果分享的时候，该已经开启，那么微信开始这个activity时，
        // 会调用onNewIntent，所以这里要处理微信的返回结果
        wxApi.handleIntent(intent, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onReq(BaseReq req) {
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                break;
            default:
                break;
        }
        finish();
        Log.e("WXEntryActivity", "WXEntryActivity: " + new Gson().toJson(req));
    }


    @Override
    public void onResp(BaseResp resp) {
        int result = 0;
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                break;
            default:
                break;
        }
        finish();
        Log.e("WXEntryActivity", "WXEntryActivity: " + new Gson().toJson(resp));
    }
}