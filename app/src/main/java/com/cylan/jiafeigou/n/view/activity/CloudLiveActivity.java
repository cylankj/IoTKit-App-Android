package com.cylan.jiafeigou.n.view.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGDevice;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.pool.GlobalDataProxy;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cloud.CloudLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.cloud.CloudLivePresenterImp;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveBaseDbBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveCallInBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveCallOutBean;
import com.cylan.jiafeigou.n.mvp.model.CloudLiveLeaveMesBean;
import com.cylan.jiafeigou.n.view.adapter.CloudLiveMesgListAdapter;
import com.cylan.jiafeigou.n.view.cloud.CloudLiveCallActivity;
import com.cylan.jiafeigou.n.view.cloud.CloudLiveSettingFragment;
import com.cylan.jiafeigou.n.view.cloud.CloudMesgBackListener;
import com.cylan.jiafeigou.n.view.cloud.LayoutIdMapCache;
import com.cylan.jiafeigou.n.view.cloud.ViewTypeMapCache;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CloudLiveVoiceTalkView;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.superadapter.OnItemClickListener;

import java.io.Serializable;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public class CloudLiveActivity extends BaseFullScreenFragmentActivity implements CloudMesgBackListener, CloudLiveContract.View {

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
    @BindView(R.id.ll_no_mesg)
    LinearLayout llNoMesg;
    @BindView(R.id.tv_device_name)
    TextView tvDeviceName;

    private ImageView iv_voice_delete;
    private CloudLiveVoiceTalkView left_voice;
    private CloudLiveVoiceTalkView right_voice;
    private TextView tv_show_mesg;

    private CloudLiveContract.Presenter presenter;
    private CloudLiveMesgListAdapter cloudLiveMesgAdapter;
    private CloudLiveSettingFragment cloudLiveSettingFragment;
    private Dialog dialog;
    private ImageView iv_cancle;
    private AnimationDrawable animationDrawable;
    private ImageView iv_play_voice;
    private String uuid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_live);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        ButterKnife.bind(this);
        initTitle();
        initFragment();
        initPresenter();
        if (presenter != null) presenter.start();
    }

    private void initTitle() {
        JFGDevice jfgDevice = GlobalDataProxy.getInstance().fetch(uuid);
        tvDeviceName.setText(TextUtils.isEmpty(jfgDevice.alias) ? jfgDevice.uuid : jfgDevice.alias);
//        CloudLiveCallActivity.setOnCloudMesgBackListener(this);
    }

    /**
     * 列表的条目点击，清空记录回调监听
     */
    private void initListener() {
        cloudLiveSettingFragment.setOnClearMesgRecordListener(new CloudLiveSettingFragment.OnClearMesgRecordListener() {
            @Override
            public void onClear() {
                cloudLiveMesgAdapter.clear();
                cloudLiveMesgAdapter.notifyDataSetChanged();
                showNoMesg();
            }
        });

        cloudLiveMesgAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int viewType, int position) {
                switch (viewType) {
                    case 0:
                        iv_play_voice = (ImageView) itemView.findViewById(R.id.iv_voice_play);
                        CloudLiveLeaveMesBean data = (CloudLiveLeaveMesBean) cloudLiveMesgAdapter.getItem(position).getData();
                        startPlayVoiceAnim(iv_play_voice);
                        presenter.playRecord(data.getLeaveMesgUrl());
                        break;
                    case 1:
                        CloudLiveCallInBean bean = (CloudLiveCallInBean) cloudLiveMesgAdapter.getItem(position).getData();
                        if (!bean.isHasConnet()) {
                            jump2CallOut();
                        }
                        break;
                }
            }
        });
    }

    private void initPresenter() {
        presenter = new CloudLivePresenterImp(this, uuid);
    }

    private void initFragment() {
        Bundle settingBundle = new Bundle();
        settingBundle.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        cloudLiveSettingFragment = CloudLiveSettingFragment.newInstance(settingBundle);
    }

    @Override
    public void setPresenter(CloudLiveContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return ContextUtils.getContext();
    }

    @OnClick({R.id.imgV_nav_back, R.id.imgV_cloud_live_top_setting, R.id.iv_cloud_share_pic, R.id.iv_cloud_videochat, R.id.iv_cloud_talk})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_nav_back:
                onBackPressed();
                break;
            case R.id.imgV_cloud_live_top_setting:                      //设置界面
                ViewUtils.deBounceClick(findViewById(R.id.imgV_cloud_live_top_setting));
                jump2SettingFragment();
                break;
            case R.id.iv_cloud_share_pic:                               //分享图片
                ViewUtils.deBounceClick(findViewById(R.id.iv_cloud_share_pic));
                ToastUtil.showToast("别点了，没有");
                break;
            case R.id.iv_cloud_videochat:                               //视频通话
                ViewUtils.deBounceClick(findViewById(R.id.iv_cloud_videochat));
                if (!presenter.isDeviceOnline()) {
                    ToastUtil.showToast(getString(R.string.NOT_ONLINE));
                    return;
                }
                jump2CallOut();
                break;
            case R.id.iv_cloud_talk:                                    //语音留言
                if (presenter.checkRecordPermission()) {
                    presenter.handlerLeveaMesg();
                } else {
                    ActivityCompat.requestPermissions(CloudLiveActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            1);
                }
                break;
        }
    }

    /**
     * 启动视频连接
     */
    private void jump2CallOut() {
        Intent intent = new Intent(ContextUtils.getContext(), CloudLiveCallActivity.class);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        intent.putExtra("call_in_or_out", false);
        startActivityForResult(intent,1);
    }

    /**
     * 跳转到设置界面
     */
    private void jump2SettingFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, cloudLiveSettingFragment)
                .addToBackStack("CloudLiveSettingFragment")
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
    protected void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }

        if (dialog != null) {
            dialog.dismiss();
        }
    }

    /**
     * 语音留言对话框
     *
     * @param isOnLine
     */
    @Override
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
            tv_show_mesg.setText(getString(R.string.EFAMILY_PUSH_TO_LEAVE_MSG));
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
                            presenter.startTalkAnimation();
                            return true;
                        }
                        case MotionEvent.ACTION_MOVE: {
                            return true;
                        }
                        case MotionEvent.ACTION_UP: {
                            tv_show_mesg.setText(getString(R.string.EFAMILY_PUSH_TO_LEAVE_MSG));
                            presenter.stopRecord();
                            CloudLiveBaseBean newBean = presenter.creatMesgBean();
                            newBean.setType(0);
                            CloudLiveLeaveMesBean newLeaveBean = new CloudLiveLeaveMesBean();
                            newLeaveBean.setLeaveMesgLength(presenter.getLeaveMesgLength());
                            newLeaveBean.setLeaveMesgUrl(leaveMesgUrl);
                            newLeaveBean.setRead(false);
                            newLeaveBean.setUserIcon(presenter.getUserIcon());
                            newLeaveBean.setLeveMesgTime(presenter.parseTime(System.currentTimeMillis()));
                            newBean.setData(newLeaveBean);
                            refreshRecycleView(newBean);

                            //保存到数据库
                            CloudLiveBaseDbBean dbBean = new CloudLiveBaseDbBean();
                            dbBean.setType(0);
                            dbBean.setUuid(uuid);
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
    public void refreshRecycleView(CloudLiveBaseBean bean) {
        if (cloudLiveMesgAdapter != null) {
            hideNoMesg();
            cloudLiveMesgAdapter.add(bean);
            cloudLiveMesgAdapter.notifyDataSetHasChanged();
        }
    }

    /**
     * 视频通话处理
     *
     * @param isOnline
     */
    @Override
    public void handlerVideoTalkResult(boolean isOnline) {
        if (isOnline) {
            jump2CallOut();
        } else {
            showDeviceDisOnlineDialog(1);
        }
    }

    @Override
    public void showReconnetProgress() {
        LoadingDialog.showLoading(getSupportFragmentManager(), getString(R.string.LOADING));
    }

    @Override
    public void hideReconnetProgress() {
        LoadingDialog.dismissLoading(getSupportFragmentManager());
    }

    @Override
    public void scrollToLast() {
        if (cloudLiveMesgAdapter.getItemCount() == 0) {
            return;
        }
        rcyCloudMesgList.smoothScrollToPosition(cloudLiveMesgAdapter.getItemCount() - 1);
    }

    /**
     * 初始化列表显示
     *
     * @param list
     */
    @Override
    public void initRecycleView(List<CloudLiveBaseBean> list) {
        //获取屏幕的高度设置列表的高度
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int height = metric.heightPixels;   // 屏幕高度（像素）
        ViewGroup.LayoutParams layoutParams = rcyCloudMesgList.getLayoutParams();
        layoutParams.height = height - ViewUtils.dp2px(121);
        rcyCloudMesgList.setLayoutParams(layoutParams);

        cloudLiveMesgAdapter = new CloudLiveMesgListAdapter(this, list, null);
        ViewTypeMapCache viewTypeMapCache = new ViewTypeMapCache();
        viewTypeMapCache.registerType(CloudLiveLeaveMesBean.class, 0);
        viewTypeMapCache.registerType(CloudLiveCallInBean.class, 1);
        viewTypeMapCache.registerType(CloudLiveCallOutBean.class, 2);
        cloudLiveMesgAdapter.setViewTypeCache(viewTypeMapCache);

        LayoutIdMapCache layoutIdMapCache = new LayoutIdMapCache();
        layoutIdMapCache.registerType(0, R.layout.activity_cloud_live_mesg_voice_item);
        layoutIdMapCache.registerType(1, R.layout.activity_cloud_live_mesg_call_in_item);
        layoutIdMapCache.registerType(2, R.layout.activity_cloud_live_mesg_call_out_item);
        cloudLiveMesgAdapter.setLayoutIdMapCache(layoutIdMapCache);
        rcyCloudMesgList.setLayoutManager(new LinearLayoutManager(this));
        rcyCloudMesgList.setAdapter(cloudLiveMesgAdapter);
        //列表监听
        initListener();
    }

    /**
     * 显示空视图
     */
    @Override
    public void showNoMesg() {
        llNoMesg.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏空视图
     */
    @Override
    public void hideNoMesg() {
        llNoMesg.setVisibility(View.GONE);
    }

    /**
     * 播放录音动画
     *
     * @param view
     */
    @Override
    public void startPlayVoiceAnim(ImageView view) {
        if (animationDrawable != null) {
            if (animationDrawable.isRunning()) {
                animationDrawable.stop();
                animationDrawable = null;
            }
        }
        view.setImageDrawable(null);
        view.setBackgroundResource(R.drawable.play_voice_record);
        animationDrawable = (AnimationDrawable) view.getBackground();
        animationDrawable.start();
    }

    /**
     * 停止播放录音动画
     */
    @Override
    public void stopPlayVoiceAnim() {
        animationDrawable.stop();
        iv_play_voice.clearAnimation();
        iv_play_voice.setImageDrawable(getResources().getDrawable(R.drawable.sound3));
    }

    /**
     * 设备不在线提示框
     *
     * @param whichshow
     */
    private void showDeviceDisOnlineDialog(final int whichshow) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.OFFLINE_ERR));
        builder.setPositiveButton(getString(R.string.TRY_AGAIN), new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.handlerLeveaMesg();
            } else {
                ToastUtil.showNegativeToast(getString(R.string.Tap0_Authorizationfailed));
            }
        }
    }

    /**
     * 添加一条消息
     * @param bean
     */
    @Override
    public void onCloudMesgBack(CloudLiveBaseBean bean) {
        refreshRecycleView(bean);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 1 && data != null){
            Bundle callback = data.getBundleExtra("callback");
            CloudLiveBaseBean callbackBean = (CloudLiveBaseBean) callback.getSerializable("callbackBean");
            refreshRecycleView(callbackBean);
        }
    }
}
