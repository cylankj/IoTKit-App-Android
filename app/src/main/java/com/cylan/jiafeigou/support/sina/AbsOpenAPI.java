package com.cylan.jiafeigou.support.sina;

/*
 * Copyright (C) 2010-2013 The SINA WEIBO Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain activity_cloud_live_mesg_video_talk_item copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.text.TextUtils;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.net.WeiboParameters;
import com.sina.weibo.sdk.utils.LogUtil;

/**
 * 微博 OpenAPI 的基类
 *
 * @author SINA
 * @since 2013-11-05
 */
/*package*/abstract class AbsOpenAPI {
    private static final String TAG = AbsOpenAPI.class.getName();

    public static final String API_SERVER = "https://api.weibo.com/2";
    private static final String HTTPMETHOD_POST = "POST";
    protected static final String HTTPMETHOD_GET = "GET";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private Oauth2AccessToken mAccessToken;

    /*package*/ AbsOpenAPI(Oauth2AccessToken accessToken) {
        mAccessToken = accessToken;
    }

    protected void requestAsync(Context context, String url, WeiboParameters params, String httpMethod, RequestListener listener) {
        if (null == mAccessToken || TextUtils.isEmpty(url) || null == params || TextUtils.isEmpty(httpMethod) || null == listener) {
            LogUtil.e(TAG, "Argument error!");
            return;
        }

        params.put(KEY_ACCESS_TOKEN, mAccessToken.getToken());
        AsyncWeiboRunner asyncWeiboRunner = new AsyncWeiboRunner(context);
        asyncWeiboRunner.requestAsync(url, params, httpMethod, listener);
    }

    protected String requestSync(Context context,
                               String url, WeiboParameters params, String httpMethod) {
        if (null == mAccessToken || TextUtils.isEmpty(url) || null == params || TextUtils.isEmpty(httpMethod)) {
            LogUtil.e(TAG, "Argument error!");
            return "";
        }

        params.put(KEY_ACCESS_TOKEN, mAccessToken.getToken());
        return new AsyncWeiboRunner(context).request(url, params, httpMethod);
    }
}
