package com.cylan.jiafeigou.activity.efamily.facetime;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.publicApi.JniPlay;
import com.cylan.jiafeigou.activity.efamily.main.ClientCallEFamlStatusListener;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.entity.msg.MsgEFamilyCallStatus;
import com.cylan.jiafeigou.utils.PreferenceUtil;

/**
 * Created by yangc on 2015/12/10.
 *
 */
public class FaceTimeCallingActivity extends FaceTimeActivity implements ClientCallEFamlStatusListener {

    private long time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAnwser.setVisibility(View.GONE);
        bg_load.setVisibility(View.GONE);
        mLoadingAnimLayout.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_NO_RESPONSE);
        try {
            JniPlay.StartCamera(true);
            JniPlay.StartRendeLocalView(mLocalView);
        }catch (Exception e){
            showAuthDialog();
        }
        makeCall();
        mHandler.sendEmptyMessageDelayed(MSG_NO_ANSWER, VIDEO_CALLING_TIMEOUT);
        mIngorn.setText(R.string.DOOR_STOPPED);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            scaleWidth();
        time = System.currentTimeMillis() / 1000;
        setOnCallEFamlStatusListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()){
            case R.id.face_time_ignore:
                if (statusListener != null && !isConnected)
                    statusListener.missCallByCancel();
                break;
        }
    }

    @Override
    public void missCallByOverTime() {
        senCallEFamilyStatus(0, 0);
    }

    @Override
    public void missCallByCancel() {
        senCallEFamilyStatus(0, 0);
    }

    @Override
    public void haveAnswered(int timeDuration) {
        senCallEFamilyStatus(1, timeDuration);
    }

    private void senCallEFamilyStatus(int isOk, int timeDuration){
        MsgEFamilyCallStatus callStatus = new MsgEFamilyCallStatus(
                PreferenceUtil.getSessionId(this), cidData.cid);
        callStatus.isOk = isOk;
        callStatus.time = time;
        callStatus.timeDuration = timeDuration;
        callStatus.type = PreferenceUtil.getOssTypeKey(this);
        callStatus.callType = 1;
        MyApp.wsRequest(callStatus.toBytes());
    }
}
