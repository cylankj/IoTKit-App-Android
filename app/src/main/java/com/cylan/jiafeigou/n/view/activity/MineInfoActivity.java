package com.cylan.jiafeigou.n.view.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.databinding.FragmentHomeMineInfoBinding;
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

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MineInfoActivity extends BaseFullScreenFragmentActivity<MineInfoContract.Presenter>
        implements MineInfoContract.View {

    //拉取出照相机时，产生的状态码
    private static final int REQUEST_CROP_PHOTO = 102;
    private static final int OPEN_CAMERA = 101;
    private Uri outPutUri;
    private File tempFile;
    private FragmentHomeMineInfoBinding homeMineInfoBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeMineInfoBinding = DataBindingUtil.setContentView(this, R.layout.fragment_home_mine_info);
        ButterKnife.bind(this);
        basePresenter = new MineInfoPresenterImpl(this, getContext());
        createCameraTempFile(savedInstanceState);
        homeMineInfoBinding.changePsw.setVisibility(RxBus.getCacheInstance().hasStickyEvent(RxEvent.ThirdLoginTab.class) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;

        finishExt();
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    @OnPermissionDenied(Manifest.permission.CAMERA)
    public void showOpenCameraPermissionDialog() {
        showPermissionDialog(getString(R.string.CAMERA));
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void openCameraWithPermission() {
        if (PermissionUtils.hasSelfPermissions(this, Manifest.permission.CAMERA)) {
            outPutUri = Uri.fromFile(tempFile);
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
            startActivityForResult(intent, OPEN_CAMERA);
        } else {
            showPermissionDialog(getString(R.string.camera_auth));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("tempFile", tempFile);
    }

    @Override
    public void onStart() {
        super.onStart();
        initView();
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
            homeMineInfoBinding.svPhone.setVisibility(View.GONE);
        } else {
            homeMineInfoBinding.svPhone.setVisibility(View.VISIBLE);
        }
        basePresenter.monitorPersonInformation();
    }

    @OnClick({R.id.tv_toolbar_icon,
            R.id.sv_email, R.id.profile_photo,
            R.id.sv_phone, R.id.user_ImageHead,
            R.id.sv_alias, R.id.change_psw, R.id.my_qr_code_text})
    public void onClick(View view) {
        switch (view.getId()) {
            //点击回退到Mine的fragment
            case R.id.tv_toolbar_icon:
                finishExt();
                break;
            //点击邮箱跳转到相应的页面
            case R.id.sv_email:
                jump2SetEmailFragment();
                break;

            case R.id.profile_photo:           //更换头像
                pickImageDialog(view);
                break;

            case R.id.sv_phone:         //跳转到设置手机号界面
                ViewUtils.deBounceClick(view);
                AppLogger.d("RLayout_home_mine_personal_phone");
                jump2SetPhoneFragment();
                break;

            case R.id.user_ImageHead:                           //点击查看大头像
                ViewUtils.deBounceClick(view);
                AppLogger.d("user_ImageHead");
                lookBigImageHead();
                break;

            case R.id.sv_alias:          //更改昵称
                ViewUtils.deBounceClick(view);
                AppLogger.d("rLayout_home_mine_personal_name");
                jump2SetUserNameFragment();
                break;

            case R.id.change_psw:                       //修改密码
                ViewUtils.deBounceClick(view);
                AppLogger.d("rl_change_password");
                jump2ChangePasswordFragment();
                break;

            case R.id.my_qr_code_text:
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
            initPersonalInformation(BaseApplication.getAppComponent().getSourceManager().getAccount());
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
        if (jfgAccount != null) {
            bundle.putString("imageUrl", isDefaultPhoto(jfgAccount.getPhotoUrl()) && basePresenter.checkOpenLogin() ? PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON) : jfgAccount.getPhotoUrl());
        }
        MineUserInfoLookBigHeadFragment bigHeadFragment = MineUserInfoLookBigHeadFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                bigHeadFragment, android.R.id.content);
    }


    private boolean isDefaultPhoto(String photoUrl) {
        return TextUtils.isEmpty(photoUrl) || photoUrl.contains("image/default.jpg");
    }

    @Override
    public void initPersonalInformation(Account account) {
        Glide.with(this).load(basePresenter.checkOpenLogin() ? PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON) : account.getPhotoUrl())
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(homeMineInfoBinding.userImageHead);
        if (!TextUtils.isEmpty(account.getPhone())) {
            homeMineInfoBinding.tvMyId.setTvSubTitle(account.getPhone());
        } else if (!TextUtils.isEmpty(account.getEmail())) {
            homeMineInfoBinding.tvMyId.setTvSubTitle(account.getEmail());
        } else if (account.getLoginType() == 3) {
            homeMineInfoBinding.tvMyId.setTvSubTitle(TextUtils.isEmpty(account.getPhone()) ? (TextUtils.isEmpty(account.getEmail()) ? getString(R.string.LOGIN_QQ) : account.getEmail()) : account.getPhone());
        } else if (account.getLoginType() == 4) {
            homeMineInfoBinding.tvMyId.setTvSubTitle(TextUtils.isEmpty(account.getPhone()) ? (TextUtils.isEmpty(account.getEmail()) ? getString(R.string.LOGIN_WEIBO) : account.getEmail()) : account.getPhone());
        } else if (account.getLoginType() == 6) {
            homeMineInfoBinding.tvMyId.setTvSubTitle(TextUtils.isEmpty(account.getPhone()) ? (TextUtils.isEmpty(account.getEmail()) ? "Twitter LOGIN" : account.getEmail()) : account.getPhone());
        } else if (account.getLoginType() == 7) {
            homeMineInfoBinding.tvMyId.setTvSubTitle(TextUtils.isEmpty(account.getPhone()) ? (TextUtils.isEmpty(account.getEmail()) ? "FaceBook LOGIN" : account.getEmail()) : account.getPhone());
        } else {
            homeMineInfoBinding.tvMyId.setTvSubTitle(account.getAccount());
        }

        if (basePresenter.checkOpenLogin() && TextUtils.isEmpty(account.getAlias())) {
            String alias = PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ALIAS);
            homeMineInfoBinding.svAlias.setTvSubTitle(TextUtils.isEmpty(alias) ? getString(R.string.NO_SET) : alias.trim());
        } else {
            homeMineInfoBinding.svAlias.setTvSubTitle(TextUtils.isEmpty(account.getAlias()) ? getString(R.string.NO_SET) : account.getAlias());
        }

        if (TextUtils.isEmpty(account.getEmail())) {
            homeMineInfoBinding.svEmail.setTvSubTitle(getString(R.string.NO_SET));
        } else {
            homeMineInfoBinding.svEmail.setTvSubTitle(account.getEmail());
        }

        if (TextUtils.isEmpty(account.getPhone())) {
            homeMineInfoBinding.svPhone.setTvSubTitle(getString(R.string.NO_SET));
        } else {
            homeMineInfoBinding.svPhone.setTvSubTitle(account.getPhone());
        }
        showSetPwd(!basePresenter.checkOpenLogin());
    }

    public void jump2SetEmailFragment() {
        Bundle bundle = new Bundle();
        BindMailFragment mailBoxFragment = BindMailFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                mailBoxFragment, android.R.id.content);
    }

    public void showSetPwd(boolean isVisible) {
        homeMineInfoBinding.changePsw.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * 弹出选择头像的对话框
     */
    private void pickImageDialog(View v) {
        ViewUtils.deBounceClick(v);
        PickImageFragment fragment = PickImageFragment.newInstance(null);
        fragment.setClickListener(vv -> {
            //打开相机
            MineInfoActivityPermissionsDispatcher.openCameraWithPermissionWithCheck(MineInfoActivity.this);
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


    private void jump2SetPhoneFragment() {
        Bundle bundle = new Bundle();
        MineInfoBindPhoneFragment bindPhoneFragment = MineInfoBindPhoneFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getSupportFragmentManager(),
                bindPhoneFragment, android.R.id.content);
        bindPhoneFragment.setOnChangePhoneListener(phone -> homeMineInfoBinding.svPhone.setTvSubTitle(phone));
    }


    @Override
    public void setPresenter(MineInfoContract.Presenter basePresenter) {

    }

    @Override
    public String getUuid() {
        return "";
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
            tempFile = new File(Environment.getExternalStorageDirectory().getPath() + "/image/", System.currentTimeMillis() + ".jpg");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MineInfoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    public void showPermissionDialog(String permission) {
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(this);
        builder.setMessage(getString(R.string.permission_auth, permission))
                .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.SETTINGS), (DialogInterface dialog, int which) -> {
                    openSetting();
                });
        AlertDialogManager.getInstance().showDialog("showSetPermissionDialog", this, builder);
    }

    private void openSetting() {
        Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        settingIntent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(settingIntent);
    }
}
