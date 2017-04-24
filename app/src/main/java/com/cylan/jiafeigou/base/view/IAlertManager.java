package com.cylan.jiafeigou.base.view;

import android.support.v7.app.AlertDialog;

/**
 * Created by yanzhendong on 2017/4/24.
 */

public interface IAlertManager {
    //不需要传入 context 获取 builder 防止没有 token 错误
    AlertDialog.Builder getAlertBuilder();
}
