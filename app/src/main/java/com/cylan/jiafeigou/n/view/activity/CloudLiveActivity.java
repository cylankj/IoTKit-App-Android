package com.cylan.jiafeigou.n.view.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudLivePresenterImp;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveLeaveMesBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveMesgBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveVideoTalkBean;
import com.cylan.jiafeigou.n.view.adapter.CloudLiveMesgListAdapter;
import com.cylan.jiafeigou.n.view.cloud.CloudLiveSettingFragment;
import com.cylan.jiafeigou.n.view.cloud.CloudVideoChatConnetionFragment;
import com.cylan.jiafeigou.n.view.cloud.LayoutIdMapCache;
import com.cylan.jiafeigou.n.view.cloud.ViewTypeMapCache;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CloudLiveVoiceTalkView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveActivity extends BaseFullScreenFragmentActivity implements CloudLiveContract.View {

    @BindView(R.id.imgV_nav_back)
    ImageView imgVNavBack;
    @BindView(R.id.imgV_cloud_live_top_setting)
    ImageView imgVCloudLiveTopSetting;
    @BindView(R.id.rcy_cloud_mesg_list)
    RecyclerView rcyCloudMesgList;
    @BindView(R.id.iv_cloud_share_pic)
    ImageView ivCloudSharePic;
    @BindView(R.id.iv_cloud_videochat)
    ImageView ivCloudVideochat;
    @BindView(R.id.iv_cloud_talk)
    ImageView ivCloudTalk;


    private ImageView iv_voice_delete;
    private CloudLiveVoiceTalkView left_voice;
    private CloudLiveVoiceTalkView right_voice;
    private TextView tv_show_mesg;


    private List<CloudLiveMesgBean> cloudMesgList;

    private CloudLiveContract.Presenter presenter;
    private List<CloudLiveBaseBean> mData;
    private CloudLiveMesgListAdapter cloudLiveMesgAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_live);
        ButterKnife.bind(this);
        getIntentData();
        initFragment();
        initPresenter();

    }

    @Override
    protected void onStart() {
        super.onStart();
        //initMesgData();
        initRecycleView();
    }

    /**
     * desc:初始化要显示的数据
     */
    private void initMesgData() {
        cloudMesgList = new ArrayList<>();
        cloudMesgList.addAll(presenter.getMesgData());
    }

    private void initPresenter() {
        presenter = new CloudLivePresenterImp(this);
    }

    private void initFragment() {

    }

    @Override
    public void setPresenter(CloudLiveContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @OnClick({R.id.imgV_nav_back, R.id.imgV_cloud_live_top_setting, R.id.iv_cloud_share_pic, R.id.iv_cloud_videochat, R.id.iv_cloud_talk})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_nav_back:
                finish();
                break;
            case R.id.imgV_cloud_live_top_setting:                      //设置界面
                ViewUtils.deBounceClick(findViewById(R.id.imgV_cloud_live_top_setting));
                jump2SettingFragment();
                break;
            case R.id.iv_cloud_share_pic:                               //分享图片
                ViewUtils.deBounceClick(findViewById(R.id.iv_cloud_share_pic));
                jump2SharePicFragment();
                break;
            case R.id.iv_cloud_videochat:                               //视频通话
                ViewUtils.deBounceClick(findViewById(R.id.iv_cloud_videochat));
                jump2VideoChatFragment();
                break;
            case R.id.iv_cloud_talk:                                    //语音留言
                showVoiceTalkDialog(CloudLiveActivity.this);
                break;
        }
    }

    private void jump2SettingFragment() {
        Bundle bundle = new Bundle();
        CloudLiveSettingFragment fragment = CloudLiveSettingFragment.newInstance(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, R.anim.slide_down_out
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, fragment)
                .addToBackStack("CloudLiveSettingFragment")
                .commit();
    }

    private void jump2SharePicFragment() {

    }

    private void jump2VideoChatFragment() {
        Bundle bundle = new Bundle();
        CloudVideoChatConnetionFragment fragment = CloudVideoChatConnetionFragment.newInstance(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, R.anim.slide_down_out
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, fragment)
                .addToBackStack("CloudVideoChatConnetionFragment")
                .commit();

        fragment.setOnIgnoreClickListener(new CloudVideoChatConnetionFragment.OnIgnoreClickListener() {
            @Override
            public void onIgnore() {
                CloudLiveBaseBean newBean = presenter.creatMesgBean();
                newBean.setType(1);
                CloudLiveVideoTalkBean newLeaveBean = new CloudLiveVideoTalkBean();
                newLeaveBean.setVideoLength("通话时长30''");
                newBean.setData(newLeaveBean);
                presenter.addMesgItem(newBean);
                //TODO 获取通话时长
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        finishExt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(presenter != null){
            presenter.stop();
        }
    }

    @Override
    public void showVoiceTalkDialog(final Context context) {
        final Dialog dialog = new Dialog(context,R.style.Theme_Light_Dialog);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.fragment_cloud_voice_talk_dialog,null);
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.dialogStyle);
        window.getDecorView().setPadding(0,0,0,0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        dialog.setContentView(dialogView);
        left_voice = (CloudLiveVoiceTalkView) dialogView.findViewById(R.id.voidTalkView_left);
        right_voice = (CloudLiveVoiceTalkView) dialogView.findViewById(R.id.voidTalkView_right);
        tv_show_mesg = (TextView) dialogView.findViewById(R.id.tv_show_mesg);
        tv_show_mesg.setText("按下留言");

        iv_voice_delete = (ImageView) dialogView.findViewById(R.id.iv_voice_delete);
        iv_voice_delete.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){

                    case MotionEvent.ACTION_DOWN:{
                        if (!presenter.checkSDCard()){
                            ToastUtil.showToast(context,"未检测到SD卡");
                            return false;
                        }
                        tv_show_mesg.setText("松开发送");
                        presenter.startRecord();
                        presenter.startTalk();
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE:{
                        return true;
                    }
                    case MotionEvent.ACTION_UP:{
                        //TODO 获取到留言时间
                        tv_show_mesg.setText("按下留言ovo");
                        CloudLiveBaseBean newBean = presenter.creatMesgBean();
                        newBean.setType(0);
                        CloudLiveLeaveMesBean newLeaveBean = new CloudLiveLeaveMesBean();
                        newLeaveBean.setLeaveMesgLength("99''");
                        newBean.setData(newLeaveBean);
                        presenter.addMesgItem(newBean);
                        presenter.stopRecord();
                        return true;
                    }
                    default:
                        return false;
                }

            }
        });
        dialog.show();
    }

    @Override
    public void refreshView(int leftVal, int rightVal) {
        left_voice.change_Status(true);
        right_voice.change_Status(true);
        left_voice.reFreshUpView(leftVal,rightVal);
        right_voice.reFreshUpView(leftVal,rightVal);
    }

    @Override
    public void initRecycleView() {
        if(mData == null){
            mData = new ArrayList<>();
        }
        mData.addAll(creatList());
        cloudLiveMesgAdapter = new CloudLiveMesgListAdapter(this, mData,null);
        ViewTypeMapCache viewTypeMapCache = new ViewTypeMapCache();
        viewTypeMapCache.registerType(CloudLiveLeaveMesBean.class, 0);
        viewTypeMapCache.registerType(CloudLiveVideoTalkBean.class, 1);
        cloudLiveMesgAdapter.setViewTypeCache(viewTypeMapCache);

        LayoutIdMapCache layoutIdMapCache = new LayoutIdMapCache();
        layoutIdMapCache.registerType(0, R.layout.activity_cloud_live_mesg_voice_item);
        layoutIdMapCache.registerType(1, R.layout.activity_cloud_live_mesg_video_talk_item);
        cloudLiveMesgAdapter.setLayoutIdMapCache(layoutIdMapCache);
        rcyCloudMesgList.setLayoutManager(new LinearLayoutManager(this));
        rcyCloudMesgList.setAdapter(cloudLiveMesgAdapter);
    }

    private List<CloudLiveBaseBean> creatList() {

        List<CloudLiveBaseBean> list = new ArrayList<>();
        CloudLiveBaseBean bean = new CloudLiveBaseBean();
        bean.setType(0);
        CloudLiveLeaveMesBean bb = new CloudLiveLeaveMesBean();
        bb.setLeaveMesgLength("77''");
        bean.setData(bb);
        list.add(bean);
        //TODO 网络获取消息记录
        return list;
    }

    @Override
    public void refreshRecycleView(CloudLiveBaseBean bean) {
        cloudLiveMesgAdapter.add(bean);
        cloudLiveMesgAdapter.notifyDataSetChanged();
    }

    public void getIntentData() {
        Bundle bundleExtra = getIntent().getExtras();
        Parcelable parcelable = bundleExtra.getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
    }
}
