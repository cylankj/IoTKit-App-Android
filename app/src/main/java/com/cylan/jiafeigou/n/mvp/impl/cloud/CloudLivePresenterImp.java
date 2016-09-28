package com.cylan.jiafeigou.n.mvp.impl.cloud;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.widget.CloudLiveVoiceTalkView;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLivePresenterImp extends AbstractPresenter<CloudLiveContract.View> implements CloudLiveContract.Presenter {

    private ImageView iv_voice_delete;
    private CloudLiveVoiceTalkView left_voice;
    private CloudLiveVoiceTalkView right_voice;
    private int val1;
    private int val2;
    private Subscription talkSub;
    private boolean flag = false;

    public CloudLivePresenterImp(CloudLiveContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (talkSub != null){
            talkSub.unsubscribe();
        }
    }

    @Override
    public void showVoiceTalkDialog(Context context) {
        final Dialog dialog = new Dialog(context,R.style.Theme_Light_Dialog);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_cloud_voice_talk_dialog,null);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.dialogStyle);
        window.getDecorView().setPadding(0,0,0,0);
        android.view.WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        dialog.setContentView(dialogView);
        left_voice = (CloudLiveVoiceTalkView) dialogView.findViewById(R.id.voidTalkView_left);
        right_voice = (CloudLiveVoiceTalkView) dialogView.findViewById(R.id.voidTalkView_right);
        iv_voice_delete = (ImageView) dialogView.findViewById(R.id.iv_voice_delete);
        iv_voice_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = !flag;
                if(flag){
                    startTalk();
                }else {
                    talkSub.unsubscribe();
                }
            }
        });
        dialog.show();
    }

    private void startTalk() {
        talkSub = Observable.interval(1000,300,TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        val1=new Random().nextInt(12)+14;
                        val2=new Random().nextInt(12)+2;
                        left_voice.change_Status(true);
                        right_voice.change_Status(true);
                        left_voice.reFreshUpView(val1,val2);
                        right_voice.reFreshUpView(val1,val2);
                    }
                });
    }


}
