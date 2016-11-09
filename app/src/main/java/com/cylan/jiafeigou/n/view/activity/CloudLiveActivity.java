package com.cylan.jiafeigou.n.view.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudLivePresenterImp;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveLeaveMesBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveVideoTalkBean;
import com.cylan.jiafeigou.n.view.adapter.CloudLiveMesgListAdapter;
import com.cylan.jiafeigou.n.view.cloud.CloudLiveSettingFragment;
import com.cylan.jiafeigou.n.view.cloud.CloudVideoChatConnetionFragment;
import com.cylan.jiafeigou.n.view.cloud.LayoutIdMapCache;
import com.cylan.jiafeigou.n.view.cloud.ViewTypeMapCache;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CloudLiveVoiceTalkView;
import com.cylan.superadapter.OnItemClickListener;

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
    @BindView(R.id.progress_re_connet)
    ProgressBar progressReConnet;


    private ImageView iv_voice_delete;
    private CloudLiveVoiceTalkView left_voice;
    private CloudLiveVoiceTalkView right_voice;
    private TextView tv_show_mesg;

    private CloudLiveContract.Presenter presenter;
    private List<CloudLiveBaseBean> mData;
    private CloudLiveMesgListAdapter cloudLiveMesgAdapter;
    private CloudLiveSettingFragment cloudLiveSettingFragment;
    private CloudVideoChatConnetionFragment cloudVideoChatConnetionFragment;
    private Dialog dialog;
    private ImageView iv_cancle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_live);
        ButterKnife.bind(this);
        getIntentData();
        initFragment();
        initPresenter();
        initDataBase();
        initRecycleView();
        initListener();
    }

    private void initListener() {
        cloudLiveSettingFragment.setOnClearMesgRecordListener(new CloudLiveSettingFragment.OnClearMesgRecordListener() {
            @Override
            public void onClear() {
                cloudLiveMesgAdapter.clear();
                mData.clear();
                cloudLiveMesgAdapter.notifyDataSetChanged();
            }
        });

        cloudVideoChatConnetionFragment.setOnIgnoreClickListener(new CloudVideoChatConnetionFragment.OnIgnoreClickListener() {
            @Override
            public void onIgnore() {
                CloudLiveBaseBean newBean = presenter.creatMesgBean();
                newBean.setType(1);
                CloudLiveVideoTalkBean newLeaveBean = new CloudLiveVideoTalkBean();
                newLeaveBean.setVideoLength("00:00");
                newLeaveBean.setHasConnet(false);
                newLeaveBean.setVideoTime(presenter.parseTime(System.currentTimeMillis() + ""));
                newBean.setData(newLeaveBean);
                presenter.addMesgItem(newBean);

                //添加到数据库
                CloudLiveBaseDbBean dbBean = new CloudLiveBaseDbBean();
                dbBean.setType(1);
                dbBean.setData(presenter.getSerializedObject(newLeaveBean));
                presenter.saveIntoDb(dbBean);
                //TODO 获取通话时长
            }
        });

        cloudLiveMesgAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int viewType, int position) {
                switch (viewType) {
                    case 0:
                        CloudLiveLeaveMesBean data = (CloudLiveLeaveMesBean) mData.get(position).getData();
                        presenter.playRecord(data.getLeaveMesgUrl());
                        break;
                    case 1:
                        CloudLiveVideoTalkBean bean = (CloudLiveVideoTalkBean) mData.get(position).getData();
                        if (!bean.isHasConnet()) {
                            Intent intent = new Intent(CloudLiveActivity.this, CloudLiveReturnCallActivity.class);
                            startActivity(intent);
                        }
                        break;
                }
            }
        });
    }

    /**
     * desc:点击忽略更新界面
     */
    @Override
    public void ignoreRefreshView(String result) {
        CloudLiveBaseBean newBean = presenter.creatMesgBean();
        newBean.setType(1);
        CloudLiveVideoTalkBean newLeaveBean = new CloudLiveVideoTalkBean();
        newLeaveBean.setVideoLength("00:00");
        newLeaveBean.setHasConnet(false);
        newLeaveBean.setVideoTime(presenter.parseTime(System.currentTimeMillis() + ""));
        newBean.setData(newLeaveBean);
        presenter.addMesgItem(newBean);

        //添加到数据库
        CloudLiveBaseDbBean dbBean = new CloudLiveBaseDbBean();
        dbBean.setType(1);
        dbBean.setData(presenter.getSerializedObject(newLeaveBean));
        presenter.saveIntoDb(dbBean);
    }

    private void initDataBase() {
        presenter.getDBManger();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(presenter!=null)
            presenter.start();
    }

    private void initPresenter() {
        presenter = new CloudLivePresenterImp(this);
    }

    private void initFragment() {
        Bundle bundle = new Bundle();
        cloudLiveSettingFragment = CloudLiveSettingFragment.newInstance(bundle);

        Bundle videoBundle = new Bundle();
        cloudVideoChatConnetionFragment = CloudVideoChatConnetionFragment.newInstance(videoBundle);

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
                //jump2VideoChatFragment();
                presenter.handlerVideoTalk();
                break;
            case R.id.iv_cloud_talk:                                    //语音留言
                presenter.handlerLeveaMesg();
                break;
        }
    }

    private void jump2SettingFragment() {

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, R.anim.slide_down_out
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, cloudLiveSettingFragment)
                .addToBackStack("CloudLiveSettingFragment")
                .commit();

    }

    private void jump2SharePicFragment() {
        Intent intent = new Intent(this, CloudLiveCallInActivity.class);
        startActivityForResult(intent,1);
    }

    private void jump2VideoChatFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(0, R.anim.slide_down_out
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(android.R.id.content, cloudVideoChatConnetionFragment)
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.stop();
        }

        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void showVoiceTalkDialog(boolean isOnLine) {
        if (isOnLine) {
            dialog = new Dialog(CloudLiveActivity.this, R.style.Theme_Light_Dialog);
            View dialogView = LayoutInflater.from(CloudLiveActivity.this).inflate(R.layout.fragment_cloud_voice_talk_dialog, null);
            Window window = dialog.getWindow();
            window.setGravity(Gravity.BOTTOM);
            window.setWindowAnimations(R.style.dialogStyle);
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            dialog.setContentView(dialogView);
            left_voice = (CloudLiveVoiceTalkView) dialogView.findViewById(R.id.voidTalkView_left);
            right_voice = (CloudLiveVoiceTalkView) dialogView.findViewById(R.id.voidTalkView_right);
            iv_cancle = (ImageView) dialogView.findViewById(R.id.iv_cancle);
            tv_show_mesg = (TextView) dialogView.findViewById(R.id.tv_show_mesg);
            tv_show_mesg.setText("按下留言");
            iv_voice_delete = (ImageView) dialogView.findViewById(R.id.iv_voice_delete);

            iv_voice_delete.setOnTouchListener(new View.OnTouchListener() {

                private String leaveMesgUrl;                        //录音的地址

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {

                        case MotionEvent.ACTION_DOWN: {
                            if (!presenter.checkSDCard()) {
                                ToastUtil.showToast("未检测到SD卡");
                                return false;
                            }
                            tv_show_mesg.setText("松开发送");
                            leaveMesgUrl = presenter.startRecord();
                            presenter.startTalk();
                            return true;
                        }
                        case MotionEvent.ACTION_MOVE: {
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {
                            tv_show_mesg.setText("按下留言");
                            presenter.stopRecord();
                            CloudLiveBaseBean newBean = presenter.creatMesgBean();
                            newBean.setType(0);
                            CloudLiveLeaveMesBean newLeaveBean = new CloudLiveLeaveMesBean();
                            newLeaveBean.setLeaveMesgLength(presenter.getLeaveMesgLength());
                            newLeaveBean.setLeaveMesgUrl(leaveMesgUrl);
                            newLeaveBean.setRead(false);
                            newLeaveBean.setLeveMesgTime(presenter.parseTime(System.currentTimeMillis() + ""));
                            newBean.setData(newLeaveBean);
                            presenter.addMesgItem(newBean);

                            //保存到数据库
                            CloudLiveBaseDbBean dbBean = new CloudLiveBaseDbBean();
                            dbBean.setType(0);
                            dbBean.setData(presenter.getSerializedObject(newLeaveBean));
                            presenter.saveIntoDb(dbBean);
                            return true;
                        }
                        default:
                            return false;
                    }
                }
            });
            dialog.show();

            iv_cancle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        } else {
            showDeviceDisOnlineDialog(2);
        }
    }

    @Override
    public void refreshView(int leftVal, int rightVal) {
        left_voice.change_Status(true);
        right_voice.change_Status(true);
        left_voice.reFreshUpView(leftVal, rightVal);
        right_voice.reFreshUpView(leftVal, rightVal);
    }

    @Override
    public void initRecycleView() {
        //获取屏幕的高度设置列表的高度
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int height = metric.heightPixels;   // 屏幕高度（像素）
        ViewGroup.LayoutParams layoutParams = rcyCloudMesgList.getLayoutParams();
        layoutParams.height = height - ViewUtils.dp2px(121);
        rcyCloudMesgList.setLayoutParams(layoutParams);

        if (mData == null) {
            mData = new ArrayList<>();
        }
        mData.addAll(creatList());
        cloudLiveMesgAdapter = new CloudLiveMesgListAdapter(this, mData, null);
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
        List<CloudLiveBaseDbBean> fromAllDb = presenter.findFromAllDb();
        if (fromAllDb != null && fromAllDb.size() > 0) {
            for (CloudLiveBaseDbBean dBbean : fromAllDb) {
                CloudLiveBaseBean newBean = new CloudLiveBaseBean();
                newBean.setType(dBbean.getType());
                newBean.setData(presenter.readSerializedObject(dBbean.getData()));
                list.add(newBean);
            }
        }
        //TODO 网络获取消息记录
        return list;
    }

    @Override
    public void refreshRecycleView(CloudLiveBaseBean bean) {
        cloudLiveMesgAdapter.add(bean);
        mData.add(bean);
        cloudLiveMesgAdapter.notifyDataSetChanged();
    }

    /**
     * desc:挂断更新界面
     */
    @Override
    public void hangUpRefreshView(String result) {
        CloudLiveBaseBean newBean = presenter.creatMesgBean();
        newBean.setType(1);
        CloudLiveVideoTalkBean newLeaveBean = new CloudLiveVideoTalkBean();
        newLeaveBean.setVideoLength(result);
        newLeaveBean.setHasConnet(true);
        newLeaveBean.setVideoTime(presenter.parseTime(System.currentTimeMillis() + ""));
        newBean.setData(newLeaveBean);
        presenter.addMesgItem(newBean);

        //添加到数据库
        CloudLiveBaseDbBean dbBean = new CloudLiveBaseDbBean();
        dbBean.setType(1);
        dbBean.setData(presenter.getSerializedObject(newLeaveBean));
        presenter.saveIntoDb(dbBean);
    }

    @Override
    public void handlerVideoTalk(boolean isOnline) {
        if (isOnline) {
            Intent intent = new Intent(CloudLiveActivity.this, CloudLiveReturnCallActivity.class);
            startActivity(intent);
        } else {
            showDeviceDisOnlineDialog(1);
        }
    }

    @Override
    public void showReconnetProgress() {
        progressReConnet.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideReconnetProgress() {
        progressReConnet.setVisibility(View.INVISIBLE);
    }

    @Override
    public void scrollToLast() {
        if (cloudLiveMesgAdapter.getItemCount() == 0) {
            return;
        }
        rcyCloudMesgList.smoothScrollToPosition(cloudLiveMesgAdapter.getItemCount() - 1);
    }

    private void showDeviceDisOnlineDialog(final int whichshow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设备离线了");
        builder.setPositiveButton("重试", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (whichshow) {
                    case 1:
                        presenter.handlerVideoTalk();
                        break;
                    case 2:
                        presenter.handlerLeveaMesg();
                        break;
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public void getIntentData() {
        Bundle bundleExtra = getIntent().getExtras();
        Parcelable parcelable = bundleExtra.getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && data != null){
            ignoreRefreshView(data.getStringExtra("ignore"));
        }
    }

}
