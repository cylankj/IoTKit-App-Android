package com.cylan.jiafeigou.n.view.bell;

import android.Manifest;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.cylan.entity.JfgEnum;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.module.JFGDPDevice;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPAction;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.n.view.home.ShareDialogFragment;
import com.cylan.jiafeigou.support.photoview.PhotoView;
import com.cylan.jiafeigou.support.photoview.PhotoViewAttacher;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

@RuntimePermissions
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

    private CompositeSubscription compositeSubscription;

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
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .error(R.drawable.broken_image)
                .listener(new RequestListener<JFGGlideURL, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, JFGGlideURL model, Target<GlideDrawable> target, boolean isFirstResource) {
                        ToastUtil.showNegativeToast("图片加载失败");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, JFGGlideURL model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(mPictureDetail);
    }


    private void slide() {
        if (mHeadContainer.isShown()) {
            AnimatorUtils.slideOut(mHeadContainer, true);
            AnimatorUtils.slideOut(mBellContainer, false);
        } else {
            AnimatorUtils.slideIn(mHeadContainer, true);
            AnimatorUtils.slideIn(mBellContainer, false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (compositeSubscription != null && compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
            compositeSubscription = null;
        }
    }

    @OnClick(R.id.act_bell_picture_opt_download)
    public void download() {
        BellRecordDetailActivityPermissionsDispatcher.downloadFileWithCheck(this);
    }

    @OnClick(R.id.act_bell_picture_opt_share)
    public void share() {
        if (mShareDialog == null) {
            mShareDialog = ShareDialogFragment.newInstance();
        }
        mShareDialog.setPictureURL(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, mCallRecord.type, mCallRecord.timeInLong / 1000 + ".jpg", mUUID));
        mShareDialog.show(getSupportFragmentManager(), ShareDialogFragment.class.getName());
    }

    @OnClick(R.id.act_bell_picture_opt_collection)
    public void collection() {
        Observable.create((Observable.OnSubscribe<IDPEntity>) subscriber -> {
            DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
            item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
            item.cid = mUUID;
            JFGDPDevice device = DataSourceManager.getInstance().getJFGDevice(mUUID);
            item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
            item.fileName = mCallRecord.timeInLong / 1000 + "_1.jpg";
            item.time = (int) (mCallRecord.timeInLong / 1000);
            IDPEntity entity = new DPEntity()
                    .setUuid(mUUID)
                    .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                    .setVersion(mCallRecord.timeInLong)
                    .setAction(new IDPAction.DPSharedAction(1, 1))
                    .setBytes(item.toBytes());
            subscriber.onNext(entity);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 200) {
                        ToastUtil.showPositiveToast(getString(R.string.Tap1_BigPic_FavoriteTips));
                    } else if (result.getResultCode() == 1050) {
                        ToastUtil.showNegativeToast(getString(R.string.DailyGreatTips_Full));
                    }
                }, e -> e.printStackTrace());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BellRecordDetailActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void downloadFile() {
        mDownloadFile = new File(JConstant.MEDIA_DETAIL_PICTURE_DOWNLOAD_DIR, mCallRecord.timeInLong / 1000 + ".jpg");

        if (mDownloadFile.exists()) {
            ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
            return;
        }
        Glide.with(this).load(new JFGGlideURL(JfgEnum.JFG_URL.WARNING, mCallRecord.type, mCallRecord.timeInLong / 1000 + ".jpg", mUUID)).
                downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
                        FileUtils.copyFile(resource, mDownloadFile);
                        mDownloadFile = null;
                    }

                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        mDownloadFile = null;
                    }
                });
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onDownloadPermissionDenied() {
        ToastUtil.showNegativeToast("下载文件需要权限,请手动开启");
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onDownloadPermissionNerverAskAgain() {
        ToastUtil.showNegativeToast("下载文件需要权限,请手动开启");
    }

    @Override
    @OnClick(R.id.act_bell_header_back)
    public void onBackPressed() {
        super.onBackPressed();
    }
}
