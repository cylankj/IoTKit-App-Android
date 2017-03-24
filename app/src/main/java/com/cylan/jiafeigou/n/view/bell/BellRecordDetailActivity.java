package com.cylan.jiafeigou.n.view.bell;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.BaseFullScreenActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskDispatcher;
import com.cylan.jiafeigou.cache.db.impl.BaseDPTaskException;
import com.cylan.jiafeigou.cache.db.module.DPEntity;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.cache.db.view.DBAction;
import com.cylan.jiafeigou.cache.db.view.DBOption;
import com.cylan.jiafeigou.cache.db.view.IDPEntity;
import com.cylan.jiafeigou.cache.db.view.IDPTaskResult;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.model.BellCallRecordBean;
import com.cylan.jiafeigou.n.view.home.ShareDialogFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
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

    private boolean isCollect = false;
    private long collectVersion = -1;

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
                .load(new JFGGlideURL(mUUID, mCallRecord.timeInLong / 1000 + ".jpg"))
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
        mShareDialog.setPictureURL(new JFGGlideURL(mUUID, mCallRecord.timeInLong / 1000 + ".jpg"));
        mShareDialog.show(getSupportFragmentManager(), ShareDialogFragment.class.getName());
    }

    @OnClick(R.id.act_bell_picture_opt_collection)
    public void collection() {
        mCollect.setEnabled(false);
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
    }

    private void collect() {
        Observable.create((Observable.OnSubscribe<IDPEntity>) subscriber -> {
            DpMsgDefine.DPWonderItem item = new DpMsgDefine.DPWonderItem();
            item.msgType = DpMsgDefine.DPWonderItem.TYPE_PIC;
            item.cid = mUUID;
            Device device = DataSourceManager.getInstance().getJFGDevice(mUUID);
            item.place = TextUtils.isEmpty(device.alias) ? device.uuid : device.alias;
            item.fileName = mCallRecord.timeInLong / 1000 + ".jpg";
            item.time = (int) (mCallRecord.timeInLong / 1000);
            IDPEntity entity = new DPEntity()
                    .setUuid(mUUID)
                    .setMsgId(DpMsgMap.ID_602_ACCOUNT_WONDERFUL_MSG)
                    .setVersion(System.currentTimeMillis())
                    .setAccount(DataSourceManager.getInstance().getAJFGAccount().getAccount())
                    .setAction(DBAction.SHARED)
                    .setBytes(item.toBytes());
            subscriber.onNext(entity);
            subscriber.onCompleted();
        })
                .subscribeOn(Schedulers.io())
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {
                        ToastUtil.showPositiveToast(getString(R.string.Tap3_FriendsAdd_Success));
                        mCollect.setImageResource(R.drawable.icon_collected);
                    }
                    mCollect.setEnabled(true);
                }, e -> {
                    mCollect.setEnabled(true);
                    if (e instanceof BaseDPTaskException) {
                        int code = ((BaseDPTaskException) e).getErrorCode();
                        if (code == 1050) {
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
                .flatMap(task -> BaseDPTaskDispatcher.getInstance().perform(task))
                .map(ret -> new DPEntity()
                        .setUuid(mUUID)
                        .setVersion(mCallRecord.timeInLong / 1000L)
                        .setAction(DBAction.DELETED)
                        .setMsgId(511))
                .flatMap(task -> BaseDPTaskDispatcher.getInstance().perform(task))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.getResultCode() == 0) {//成功了
                        AppLogger.d("取消收藏成功");
                        mCollect.setImageResource(R.drawable.icon_collection);
                    }
                    mCollect.setEnabled(true);
                }, e -> {
                    mCollect.setEnabled(true);
                    e.printStackTrace();
                    AppLogger.d(e.getMessage());
                });
    }

    private void alertOver50() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(getString(R.string.DailyGreatTips_Full))
                .setPositiveButton(R.string.OK, (dialog, which) -> {
                    dialog.dismiss();
                    finishExt();
                    RxBus.getCacheInstance().post(new RxEvent.ShowWonderPageEvent());
                    Intent intent = new Intent(BellRecordDetailActivity.this, NewHomeActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.CANCEL, (dialog, which) -> {
                    dialog.dismiss();
                    finishExt();
                }).show();
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
        Glide.with(this).load(new JFGGlideURL(mUUID, mCallRecord.timeInLong / 1000 + ".jpg")).
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
                .setUuid(mUUID)
                .setAction(DBAction.QUERY)
                .setVersion(mCallRecord.timeInLong / 1000L)
                .setOption(DBOption.SingleQueryOption.ONE_BY_TIME))
                .flatMap(entity -> BaseDPTaskDispatcher.getInstance().perform(entity))
                .map(ret -> {
                    DpMsgDefine.DPPrimary<Long> version = ret.getResultResponse();
                    if (version != null) {
                        isCollect = true;
                        collectVersion = version.value;
                    } else isCollect = false;
                    return ret;
                });

//        Observable.create((Observable.OnSubscribe<Long>) subscriber -> {
//            try {
//                ArrayList<JFGDPMsg> params = new ArrayList<>(1);
//                JFGDPMsg msg = new JFGDPMsg(511, mCallRecord.timeInLong / 1000L);
//                params.add(msg);
//                long seq = JfgCmdInsurance.getCmd().robotGetDataByTime(mUUID, params, 0);
//                AppLogger.d("正在检查511消息, seq 为:" + seq);
//                subscriber.onNext(seq);
//                subscriber.onCompleted();
//            } catch (JfgException e) {
//                e.printStackTrace();
//                AppLogger.e(e.getMessage());
//                subscriber.onError(e);
//            }
//        })
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io())
//                .flatMap(seq -> RxBus.getCacheInstance().toObservable(RobotoGetDataRsp.class).filter(rsp -> rsp.seq == seq).first())
//                .timeout(10, TimeUnit.SECONDS)
//                .map(result -> {
//                    if (result.map == null || result.map.size() == 0 || result.map.get(511) == null || result.map.get(511).size() == 0) {//未收藏
//                        isCollect = false;
//                    } else {
//                        isCollect = true;
//                    }
//                    return result;
//                });
    }
}
