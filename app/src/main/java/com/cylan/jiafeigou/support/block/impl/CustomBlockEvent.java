package com.cylan.jiafeigou.support.block.impl;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.cylan.jiafeigou.support.block.OnBlockEventInterceptor;

import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Created by hunt on 16-4-6.
 */
public class CustomBlockEvent implements OnBlockEventInterceptor {
    private static final String TAG = "[DEBUG]: cost much startTime ";
    //需要对应的
    /**
     * {@link com.cylan.jiafeigou.support.block.log.Block#timeStart}的格式
     */
    private static final SimpleDateFormat TIME_FORMATTER
            = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault());

    @Override
    public void onBlockEvent(Context context, final String timeStart, String blockContent, boolean needDisplay) {
        if (needDisplay) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BlockCanaryContext.get().getContext()
                            , TAG, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
//            MtaManager.customEvent(context, "blockContent", blockContent);
        }
    }
}
