package com.cylan.jiafeigou.n.view.bell;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.base.injector.component.ActivityComponent;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskException;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoview.PhotoView;
import com.cylan.jiafeigou.support.photoview.PhotoViewAttacher;
import com.cylan.jiafeigou.support.share.ShareManager;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.JFGGlideURL;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

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
    LinearLayout mBellContainer;
    @BindView(R.id.activity_bell_record_detail)
    FrameLayout mBellDetail;
    @BindView(R.id.act_bell_header_container)
    RelativeLayout mHeadContainer;
    private BellCallRecordBean mCallRecord;
    private File mDownloadFile;

    private boolean isCollect = false;
    private long collectVersion = -1;
    private boolean canCollect = true;
    private CompositeSubscription compositeSubscription;
    @Inject
    IDPTaskDispatcher taskDispatcher;

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
                .load(new JFGGlideURL(uuid, mCallRecord.timeInLong / 1000 + ".jpg"))
                .placeholder(R.drawable.wonderful_pic_place_holder)
                .error(R.drawable.broken_image)
                .listener(new RequestListener<JFGGlideURL, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, JFGGlideURL model, Target<GlideDrawable> target, boolean isFirstResource) {
                        ToastUtil.showNegativeToast("图片加载失败");
                        canCollect = false;
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, JFGGlideURL model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(mPictureDetail);
        mCollect.setEnabled(false);
        check().observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
//                    this.result = result;
                    if (isCollect) {//没有收藏
                        AppLogger.d("已经收藏了啊");
                        mCollect.setImageResource(R.drawable.icon_collected);
                    } else {//已收藏
                        AppLogger.d("未收藏啊");
                        mCollect.setImageResource(R.drawable.icon_collection);
                    }
                    mCollect.setEnabled(true);
                }, e -> {
                    mCollect.setEnabled(true);
                    AppLogger.d(e.getMessage());
                    e.printStackTrace();
                });
        mShare.setVisibility(getResources().getBoolean(R.bool.show_share_btn) ? View.VISIBLE : View.GONE);
    }


    private void slide() {
        if (mHeadContainer.getTranslationY() == 0) {
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

    @Override
    protected void setActivityComponent(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @OnClick(R.id.act_bell_picture_opt_download)
    public void download() {
        BellRecordDetailActivityPermissionsDispatcher.downloadFileWithCheck(this);
    }

    @OnClick(R.id.act_bell_picture_opt_share)
    public void share() {
        new JFGGlideURL(uuid, mCallRecord.timeInLong / 1000 + ".jpg").fetch(localPath -> {
            ShareManager.byImg(BellRecordDetailActivity.this)
                    .withImg(localPath)
                    .share();
//
//            Intent intent = new Intent(this, ShareMediaActivity.class);
//            intent.putExtra(ShareConstant.SHARE_CONTENT, ShareConstant.SHARE_CONTENT_PICTURE);
//            intent.putExtra(ShareConstant.SHARE_CONTENT_PICTURE_EXTRA_IMAGE_PATH, localPath);
//            startActivity(intent);
        });
    }

    @OnClick(R.id.act_bell_picture_opt_collection)
    public void collection() {
        if (!canCollect()) return;
        mCollect.setEnabled(false);
        check()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    if (isCollect) {
                        mCollect.setImageResource(R.drawable.icon_collection);
                    } else {
                        mCollect.setImageResource(R.drawable.icon_collected);
                    }
                    if (!isCollect) {//未收藏
                        collect();
                    } else if (collectVersion != -1) {
                        unCollect(collectVersion);
                    }
                }, AppLogger::e);

    }

    private void collect() {
        Observable.create((Observable.OnSubscribe<IDPEntity>) subscriber -> {
            DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
            item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
            item.cid = uuid;
            Device device = sourceManager.getDevice(uuid);
            item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
            item.fileName = mCallRecord.timeInLong / 1000 + ".jpg";
            item.time = (int) (mCallRecord.timeInLong / 1000);
            FutureTarget<File> future = Glide.with(ContextUtils.getContext())
                    .load(new JFGGlideURL(uuid, item.fileName))
                    .downloadOnly(100, 100);
            String path = null;
            try {
                path = future.get().getAbsolutePath();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            IDPEntity entity = new DPEntity()
                    .setUuid(uuid)
                    .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                    .setVersion(System.currentTimeMillis())
                    .setAccount(sourceManager.getAccount().getAccount())
                    .setAction(DBAction.SHARED)
                    .setOption(new DBOption.SingleSharedOption(1, 1, path))
                    .setBytes(item.toBytes());
            subscriber.onNext(entity);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> taskDispatcher.perform(entity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {
                        ToastUtil.showPositiveToast(getString(R.string.Tap3_FriendsAdd_Success));
                        AppLogger.d("分享成功!");
                        mCollect.setImageResource(R.drawable.icon_collected);
                        isCollect = true;
                    }
                    mCollect.setEnabled(true);
                }, e -> {
                    mCollect.setEnabled(true);
                    if (e instanceof BaseDPTaskException) {
                        int code = ((BaseDPTaskException) e).getErrorCode();
                        if (code == 1050) {
                            mCollect.setImageResource(R.drawable.icon_collection);
                            alertOver50();
                        }
                    }
                    AppLogger.d(e.getMessage());

                });
    }

    private void unCollect(long ver) {
        Observable.just(ver)
                .observeOn(Schedulers.io())
                .map(version -> new DPEntity()
                        .setUuid("")
                        .setVersion(version)
                        .setAction(DBAction.DELETED)
                        .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG))
                .flatMap(task -> taskDispatcher.perform(task))
                .map(ret -> new DPEntity()
                        .setUuid(uuid)
                        .setVersion(mCallRecord.timeInLong)
                        .setAction(DBAction.DELETED)
                        .setMsgId(511))
                .flatMap(task -> taskDispatcher.perform(task))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {//成功了
                        AppLogger.d("取消收藏成功");
                        mCollect.setImageResource(R.drawable.icon_collection);
                        isCollect = false;
                    }
                    mCollect.setEnabled(true);
                }, e -> {
                    mCollect.setEnabled(true);
                    e.printStackTrace();
                    AppLogger.d(e.getMessage());
                });
    }

    private void alertOver50() {
        AlertDialogManager.getInstance().showDialog(this, getString(R.string.DailyGreatTips_Full), getString(R.string.DailyGreatTips_Full),
                getString(R.string.OK), (dialog, which) -> {
                    dialog.dismiss();
                    finishExt();
                    RxBus.getCacheInstance().post(new RxEvent.ShowWonderPageEvent());
                    Intent intent = new Intent(BellRecordDetailActivity.this, NewHomeActivity.class);
                    startActivity(intent);
                }, getString(R.string.CANCEL), (dialog, which) -> {
                    dialog.dismiss();
                    finishExt();
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BellRecordDetailActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void downloadFile() {
        mDownloadFile = new File(JConstant.MEDIA_PATH, mCallRecord.timeInLong / 1000 + ".jpg");

        if (mDownloadFile.exists()) {
            ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
            return;
        }
        Glide.with(this).load(new JFGGlideURL(uuid, mCallRecord.timeInLong / 1000 + ".jpg")).
                downloadOnly(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                        ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
                        FileUtils.copyFile(resource, mDownloadFile);
                        MediaScannerConnection.scanFile(BellRecordDetailActivity.this, new String[]{mDownloadFile.getAbsolutePath()}, null, null);
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
//        ToastUtil.showNegativeToast("下载文件需要权限,请手动开启");
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onDownloadPermissionNerverAskAgain() {
//        ToastUtil.showNegativeToast("下载文件需要权限,请手动开启");
    }

    @Override
    @OnClick(R.id.act_bell_header_back)
    public void onBackPressed() {
        super.onBackPressed();
    }

    public Observable<IDPTaskResult> check() {
        return Observable.just(new DPEntity()
                .setMsgId(511)
                .setUuid(uuid)
                .setAction(DBAction.QUERY)
                .setVersion(mCallRecord.timeInLong)
                .setOption(DBOption.SingleQueryOption.ONE_BY_TIME))
                .flatMap(entity -> taskDispatcher.perform(entity))
                .map(ret -> {
                    DpMsgDefine.DPPrimary<Long> version = ret.getResultResponse();
                    if (version != null) {
                        isCollect = true;
                        collectVersion = version.value;
                    } else isCollect = false;
                    return ret;
                });
    }

    private boolean canCollect() {
        return canCollect && NetUtils.isNetworkAvailable(this);
    }
}
