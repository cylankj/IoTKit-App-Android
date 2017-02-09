package com.cylan.jiafeigou.n.view.bell;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.entity.JfgEnum;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.n.view.home.ShareDialogFragment;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.photoview.PhotoView;
import com.cylan.photoview.PhotoViewAttacher;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

public class BellRecordDetailActivity extends BaseFullScreenActivity {

    private static final int REQ_DOWNLOAD = 20000;
    @BindView(R.id.act_bell_header_back)
    ImageView mBack;
    @BindView(R.id.act_bell_header_title)
    TextView mTitle;
    @BindView(R.id.act_bell_detail_picture)
    PhotoView mPictureDetail;
    @BindView(R.id.act_bell_picture_opt_download)
    ImageView mDownload;
    @BindView(R.id.act_bell_picture_opt_share)
    ImageView mShare;
    @BindView(R.id.act_bell_picture_opt_collection)
    ImageView mCollect;
    @BindView(R.id.act_bell_pic_option)
    FrameLayout mBellContainer;
    @BindView(R.id.activity_bell_record_detail)
    FrameLayout mBellDetail;
    @BindView(R.id.act_bell_header_container)
    RelativeLayout mHeadContainer;
    private BellCallRecordBean mCallRecord;
    private ShareDialogFragment mShareDialog;
    private File mDownloadFile;

    @Override
    protected JFGPresenter onCreatePresenter() {
        return new BasePresenter() {
        };
    }

    @Override
    protected int getContentViewID() {
        return R.layout.activity_bell_record_detail;
    }

    @Override
    protected void initViewAndListener() {
        super.initViewAndListener();
        ViewUtils.setViewPaddingStatusBar(mHeadContainer);
        mBellDetail.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        mCallRecord = getIntent().getParcelableExtra(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        mTitle.setText(TimeUtils.getMediaVideoTimeInString(mCallRecord.timeInLong));
        mPictureDetail.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                  slide();
            }

            @Override
            public void onOutsidePhotoTap() {
                 slide();
            }
        });

        Glide.with(this)
                .load(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, mCallRecord.type, mCallRecord.timeInLong / 1000 + ".jpg", mUUID))
                .into(mPictureDetail);
//        //mock
//        Glide.with(this)
//                .load("http://c.hiphotos.baidu.com/image/pic/item/0dd7912397dda1449fad6f63b6b7d0a20df486be.jpg")
//                .into(mPictureDetail);
    }


    private void slide(){
        if (mHeadContainer.isShown()){
            AnimatorUtils.slideOut(mHeadContainer,true);
            AnimatorUtils.slideOut(mBellContainer,false);
        }else{
            AnimatorUtils.slideIn(mHeadContainer,true);
            AnimatorUtils.slideIn(mBellContainer,false);
        }
    }


    @OnClick(R.id.act_bell_picture_opt_download)
    public void download() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            downloadFile();//已经获得了授权
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //需要重新提示用户授权
            ToastUtil.showNegativeToast("下载文件需要权限,请手动开启");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_DOWNLOAD);
        }
    }

    @OnClick(R.id.act_bell_picture_opt_share)
    public void share() {
        if (mShareDialog == null) {
            mShareDialog = ShareDialogFragment.newInstance();
        }
        mShareDialog.setGlideUrl(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, mCallRecord.type, mCallRecord.timeInLong / 1000 + ".jpg", mUUID));
        mShareDialog.show(getSupportFragmentManager(), ShareDialogFragment.class.getName());
    }

    @OnClick(R.id.act_bell_picture_opt_collection)
    public void collection() {


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_DOWNLOAD) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadFile();
            } else {
                Toast.makeText(this, getString(R.string.permission_download), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadFile() {
        mDownloadFile = new File(JConstant.MEDIA_DETAIL_PICTURE_DOWNLOAD_DIR,mCallRecord.timeInLong/1000+".jpg");

        if (mDownloadFile.exists()) {
            ToastUtil.showPositiveToast("文件已下载");
            return;
        }

        Glide.with(this).load(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, mCallRecord.type, mCallRecord.timeInLong / 1000 + ".jpg", mUUID)).
                downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        ToastUtil.showPositiveToast("下载完成");
                        FileUtils.copyFile(resource, mDownloadFile);
                        mDownloadFile = null;
                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        ToastUtil.showPositiveToast("开始下载");
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        ToastUtil.showNegativeToast("下载失败");
                        mDownloadFile = null;
                    }
                });
    }

}
