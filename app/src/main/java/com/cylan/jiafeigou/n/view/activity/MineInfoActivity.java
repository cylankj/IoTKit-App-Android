package com.cylan.jiafeigou.n.view.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoPresenterImpl;
import com.cylan.jiafeigou.n.view.mine.BindMailFragment;
import com.cylan.jiafeigou.n.view.mine.MineInfoBindPhoneFragment;
import com.cylan.jiafeigou.n.view.mine.MineInfoSetPassWordFragment;
import com.cylan.jiafeigou.n.view.mine.MineSetUserAliasFragment;
import com.cylan.jiafeigou.n.view.mine.MineUserInfoLookBigHeadFragment;
import com.cylan.jiafeigou.n.view.mine.MyQRCodeDialog;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoselect.ClipImageActivity;
import com.cylan.jiafeigou.support.photoselect.activities.AlbumSelectActivity;
import com.cylan.jiafeigou.support.photoselect.helpers.Constants;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.LocaleUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.PickImageFragment;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MineInfoActivity extends BaseFullScreenFragmentActivity<MineInfoContract.Presenter>
        implements MineInfoContract.View {

    //拉取出照相机时，产生的状态码
    private static final int REQUEST_CROP_PHOTO = 102;
    private static final int OPEN_CAMERA = 101;

    @BindView(R.id.tv_home_mine_personal_mailbox)
    TextView mTvMailBox;
    @BindView(R.id.RLayout_home_mine_personal_phone)
    RelativeLayout mPhoneNum;
    @BindView(R.id.user_ImageHead)
    RoundedImageView userImageHead;
    @BindView(R.id.tv_user_name)
    TextView tvUserName;
    @BindView(R.id.rLayout_home_mine_personal_name)
    RelativeLayout rLayoutHomeMinePersonalName;
    @BindView(R.id.tv_user_account)
    TextView tvUserAccount;
    @BindView(R.id.tv_home_mine_personal_phone)
    TextView tvHomeMinePersonalPhone;
    @BindView(R.id.rl_change_password)
    RelativeLayout rlChangePassword;
    @BindView(R.id.rl_my_QRCode)
    RelativeLayout rlMyQRCode;
    @BindView(R.id.tv_my_number)
    TextView tvMyNumber;
    @BindView(R.id.btn_home_mine_personal_information)
    TextView btnHomeMinePersonalInformation;
    @BindView(R.id.ll_container)
    LinearLayout llContainer;

    private Uri outPutUri;
    private File tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_mine_info);
        ButterKnife.bind(this);
        basePresenter = new MineInfoPresenterImpl(this, getContext());
        createCameraTempFile(savedInstanceState);
        rlChangePassword.setVisibility(RxBus.getCacheInstance().hasStickyEvent(RxEvent.ThirdLoginTab.class)
                ? View.VISIBLE : View.INVISIBLE);
    }


    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void onCameraPermissionDenied() {
        onNeverAskAgainCameraPermission();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    public void onNeverAskAgainCameraPermission() {
        AlertDialogManager.getInstance().showDialog(this,
                getString(R.string.permission_auth, getString(R.string.CAMERA)),
                getString(R.string.permission_auth, getString(R.string.CAMERA)),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                },
                getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    setPermissionDialog(getString(R.string.CAMERA));
                });
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void onOpenCameraPermissionGrant() {
        openCamera();
    }

    @OnShowRationale(Manifest.permission.CAMERA)
    public void showRationaleForCamera(PermissionRequest request) {
        AppLogger.d(JConstant.LOG_TAG.PERMISSION + "showRationaleForCamera");
        onNeverAskAgainCameraPermission();
    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        MineInfoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("tempFile", tempFile);
    }

    @Override
    public void onStart() {
        super.onStart();
        initView();
        initPersonalInformation(BaseApplication.getAppComponent().getSourceManager().getJFGAccount());
    }

    @Override
    protected boolean theLastActivity() {
        return false;
    }

    /**
     * 判断是否大陆用户显示绑定手机号码一栏
     */
    private void initView() {
        int way = LocaleUtils.getLanguageType(this);
        if (way != JConstant.LOCALE_SIMPLE_CN) {
            mPhoneNum.setVisibility(View.GONE);
        } else {
            mPhoneNum.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (basePresenter != null) basePresenter.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (basePresenter != null) basePresenter.stop();
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.btn_home_mine_personal_information,
            R.id.lLayout_home_mine_personal_mailbox, R.id.rLayout_home_mine_personal_pic,
            R.id.RLayout_home_mine_personal_phone, R.id.user_ImageHead,
            R.id.rLayout_home_mine_personal_name, R.id.rl_change_password, R.id.rl_my_QRCode})
    public void onClick(View view) {
        switch (view.getId()) {
            //点击回退到Mine的fragment
            case R.id.tv_toolbar_icon:
                finishExt();
                break;
            //点击退出做相应的逻辑
            case R.id.btn_home_mine_personal_information:
                showLogOutDialog(view);
                break;
            //点击邮箱跳转到相应的页面
            case R.id.lLayout_home_mine_personal_mailbox:
                jump2SetEmailFragment();
                break;

            case R.id.rLayout_home_mine_personal_pic:           //更换头像
                pickImageDialog(view);
                break;

            case R.id.RLayout_home_mine_personal_phone:         //跳转到设置手机号界面
                ViewUtils.deBounceClick(view);
                AppLogger.d("RLayout_home_mine_personal_phone");
                jump2SetPhoneFragment();
                break;

            case R.id.user_ImageHead:                           //点击查看大头像
                ViewUtils.deBounceClick(view);
                AppLogger.d("user_ImageHead");
                lookBigImageHead();
                break;

            case R.id.rLayout_home_mine_personal_name:          //更改昵称
                ViewUtils.deBounceClick(view);
                AppLogger.d("rLayout_home_mine_personal_name");
                jump2SetUserNameFragment();
                break;

            case R.id.rl_change_password:                       //修改密码
                ViewUtils.deBounceClick(view);
                AppLogger.d("rl_change_password");
                jump2ChangePasswordFragment();
                break;

            case R.id.rl_my_QRCode:
                //我的二维码
                ViewUtils.deBounceClick(view);
                showQrCodeDialog();
                break;
        }
    }

    /**
     * 弹出我的二维码对话框
     */
    private void showQrCodeDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isopenlogin", basePresenter.checkOpenLogin());
        MyQRCodeDialog.newInstance(bundle).show(getSupportFragmentManager(), "myqrcode");
    }

    /**
     * 修改密码的界面
     */
    private void jump2ChangePasswordFragment() {
        Bundle bundle = new Bundle();
        MineInfoSetPassWordFragment setPassWordFragment = MineInfoSetPassWordFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                setPassWordFragment, android.R.id.content);
    }

    /**
     * 跳转到修改昵称界面
     */
    private void jump2SetUserNameFragment() {
        Bundle bundle = new Bundle();
        MineSetUserAliasFragment setUserNameFragment = MineSetUserAliasFragment.newInstance(bundle);
        setUserNameFragment.setCallBack(t -> {
            initPersonalInformation(BaseApplication.getAppComponent().getSourceManager().getJFGAccount());
        });
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(), setUserNameFragment,
                android.R.id.content);
    }

    /**
     * desc: 查看大头像
     */
    private void lookBigImageHead() {
        Bundle bundle = new Bundle();
        JFGAccount jfgAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        if (jfgAccount != null)
            bundle.putString("imageUrl", isDefaultPhoto(jfgAccount.getPhotoUrl()) && basePresenter.checkOpenLogin() ? PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON) : jfgAccount.getPhotoUrl());
        MineUserInfoLookBigHeadFragment bigHeadFragment = MineUserInfoLookBigHeadFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                bigHeadFragment, android.R.id.content);
    }


    private boolean isDefaultPhoto(String photoUrl) {
        return TextUtils.isEmpty(photoUrl) || photoUrl.contains("image/default.jpg");
    }

    @Override
    public void initPersonalInformation(JFGAccount bean) {
        MyViewTarget myViewTarget = new MyViewTarget(userImageHead, getContext().getResources());
        String photoUrl;
        if (bean != null) {
            //头像的回显
            photoUrl = bean.getPhotoUrl();
            if (isDefaultPhoto(photoUrl)) {
                if (basePresenter.checkOpenLogin()) {
                    photoUrl = PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON);
                }
            }
            Account account = BaseApplication.getAppComponent().getSourceManager().getAccount();
            if (!TextUtils.isEmpty(photoUrl) && getContext() != null || account != null) {
                Glide.with(getContext()).load(photoUrl)
                        .asBitmap()
                        .centerCrop()
//                        .placeholder(R.drawable.icon_mine_head_normal)//不需要placehole,因为开始时会设置这个id.如果频繁调用,则会闪烁.
                        .error(R.drawable.icon_mine_head_normal)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .signature(new StringSignature(TextUtils.isEmpty(account.getToken()) ? "" : account.getToken()))
                        .into(myViewTarget);
            }

            if (basePresenter.checkOpenLogin() && TextUtils.isEmpty(bean.getAlias())) {
                String alias = PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ALIAS);
                tvUserName.setText(TextUtils.isEmpty(alias) ? getString(R.string.NO_SET) : alias.trim());
            } else {
                tvUserName.setText(TextUtils.isEmpty(bean.getAlias()) ? getString(R.string.NO_SET) : bean.getAlias());
            }

            if (bean.getEmail() == null | TextUtils.isEmpty(bean.getEmail())) {
                mTvMailBox.setText(getString(R.string.NO_SET));
            } else {
                mTvMailBox.setText(bean.getEmail());
            }

            if (TextUtils.isEmpty(bean.getPhone())) {
                tvHomeMinePersonalPhone.setText(getString(R.string.NO_SET));
            } else {
                tvHomeMinePersonalPhone.setText(bean.getPhone());
            }

            basePresenter.loginType(bean.getAccount(), bean.getPhone(), bean.getEmail());
            showSetPwd(!basePresenter.checkOpenLogin());
        }
    }

    private static class MyViewTarget extends BitmapImageViewTarget {
        private final WeakReference<ImageView> image;
        private final WeakReference<Resources> resources;

        public MyViewTarget(ImageView view, Resources resource) {
            super(view);
            image = new WeakReference<>(view);
            resources = new WeakReference<>(resource);
        }

        @Override
        protected void setResource(Bitmap resource) {
            if (resource == null || image.get() == null)
                return;
            RoundedBitmapDrawable circularBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(resources.get(), resource);
            circularBitmapDrawable.setCircular(true);
            image.get().setImageDrawable(circularBitmapDrawable);
        }
    }

    @Override
    public void jump2SetEmailFragment() {
        Bundle bundle = new Bundle();
        BindMailFragment mailBoxFragment = BindMailFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                mailBoxFragment, android.R.id.content);
    }

    @Override
    public void showSetPwd(boolean isVisible) {
        rlChangePassword.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setAccount(String account, String phone, String email, int type) {
        if (!TextUtils.isEmpty(phone)) {
            tvUserAccount.setText(phone);
        } else if (!TextUtils.isEmpty(email)) {
            tvUserAccount.setText(email);
        } else if (type == 3) {
            tvUserAccount.setText(TextUtils.isEmpty(phone) ? (TextUtils.isEmpty(email) ? getString(R.string.LOGIN_QQ) : email) : phone);
        } else if (type == 4) {
            tvUserAccount.setText(TextUtils.isEmpty(phone) ? (TextUtils.isEmpty(email) ? getString(R.string.LOGIN_WEIBO) : email) : phone);
        } else if (type == 6) {
            tvUserAccount.setText(TextUtils.isEmpty(phone) ? (TextUtils.isEmpty(email) ? "Twitter LOGIN" : email) : phone);
        } else if (type == 7) {
            tvUserAccount.setText(TextUtils.isEmpty(phone) ? (TextUtils.isEmpty(email) ? "FaceBook LOGIN" : email) : phone);
        } else {
            tvUserAccount.setText(account);
        }
    }

    /**
     * 弹出选择头像的对话框
     */
    private void pickImageDialog(View v) {
        ViewUtils.deBounceClick(v);
        PickImageFragment fragment = PickImageFragment.newInstance(null);
        fragment.setClickListener(vv -> {
            //打开相机
            MineInfoActivityPermissionsDispatcher.onOpenCameraPermissionGrantWithCheck(this);
        }, cc -> {
            openGallery();
        });
        fragment.show(getSupportFragmentManager(), "pickImageDialog");
    }

    /**
     * 打开相册
     */
    private void openGallery() {
        Intent intent = new Intent(getContext(), AlbumSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 3);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    /**
     * 启动相机
     */
    private void openCamera() {
        outPutUri = Uri.fromFile(tempFile);
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
        startActivityForResult(intent, OPEN_CAMERA);
    }

    private void jump2SetPhoneFragment() {
        Bundle bundle = new Bundle();
        MineInfoBindPhoneFragment bindPhoneFragment = MineInfoBindPhoneFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                bindPhoneFragment, android.R.id.content);
        bindPhoneFragment.setOnChangePhoneListener(phone -> tvHomeMinePersonalPhone.setText(phone));
    }


    @Override
    public void setPresenter(MineInfoContract.Presenter basePresenter) {

    }

    @Override
    public String getUuid() {
        return "";
    }

    /**
     * 删除亲友对话框
     */
    public void showLogOutDialog(View v) {
        AlertDialogManager.getInstance().showDialog(this,
                "showLogOutDialog", getString(R.string.LOGOUT_INFO),
                getString(R.string.LOGOUT), (DialogInterface dialog, int which) -> {
                    JFGAccount jfgAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
                    if (jfgAccount != null) {
                        basePresenter.logOut(jfgAccount.getAccount());
                        //进入登陆页 login page
                        setResult(RESULT_OK);
                        finishExt();
                    }
                }, getString(R.string.CANCEL), null, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == Constants.REQUEST_CODE && data != null) {
                gotoClipActivity(Uri.parse(data.getStringExtra(Constants.INTENT_EXTRA_IMAGES)));
            } else if (requestCode == REQUEST_CROP_PHOTO && data != null) {
                final Uri uri = data.getData();
                if (uri == null) {
                    return;
                }
                String cropImagePath = getRealFilePathFromUri(getContext(), uri);
                PreferencesUtils.putString("UserImageUrl", cropImagePath);
                AppLogger.d("upload_succ");
            } else if (requestCode == OPEN_CAMERA) {
                if (resultCode == RESULT_OK) {
                    gotoClipActivity(outPutUri);
                }
            }
        }
    }

    /**
     * 打开截图界面
     *
     * @param uri
     */
    public void gotoClipActivity(Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent();
        intent.setClass(getContext(), ClipImageActivity.class);
        intent.putExtra("type", 1);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CROP_PHOTO);
    }

    /**
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePathFromUri(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 创建调用系统照相机待存储的临时文件
     *
     * @param savedInstanceState
     */
    private void createCameraTempFile(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("tempFile")) {
            tempFile = (File) savedInstanceState.getSerializable("tempFile");
        } else {
            tempFile = new File(basePresenter.checkFileExit(Environment.getExternalStorageDirectory().getPath() + "/image/"),
                    System.currentTimeMillis() + ".jpg");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && PermissionUtils.hasSelfPermissions(getContext(), Manifest.permission.CAMERA)) {
                openCamera();
            } else {
                setPermissionDialog(getString(R.string.camera_auth));
            }
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                setPermissionDialog(getString(R.string.photo));
            }
        }
    }

    public void setPermissionDialog(String permission) {
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(this);
        builder.setMessage(getString(R.string.permission_auth, permission))
                .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.SETTINGS), (DialogInterface dialog, int which) -> {
                    openSetting();
                });
        AlertDialogManager.getInstance().showDialog("setPermissionDialog", this, builder);
    }

    private void openSetting() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getContext().getPackageName());
        }
        startActivity(localIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
