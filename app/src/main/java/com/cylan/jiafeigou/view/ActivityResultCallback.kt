package com.cylan.jiafeigou.view

import android.content.Intent

/**
 * Created by yanzhendong on 2017/10/28.
 */
interface ActivityResultCallback {

    fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent)
}