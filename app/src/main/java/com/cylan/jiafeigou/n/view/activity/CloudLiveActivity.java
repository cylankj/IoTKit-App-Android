package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveContract;
import com.cylan.jiafeigou.n.view.cloud.CloudLiveSettingFragment;
import com.cylan.jiafeigou.n.view.cloud.CloudVideoChatConnetionFragment;
import com.cylan.jiafeigou.utils.ViewUtils;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_live);
        ButterKnife.bind(this);
        initFragment();
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
    }

    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        finishExt();
    }
}
