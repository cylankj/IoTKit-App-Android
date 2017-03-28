package com.cylan.jiafeigou.n.view.mine;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.NewHomeActivity;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.BindDeviceActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoselect.ClipImageActivity;
import com.cylan.jiafeigou.support.photoselect.activities.AlbumSelectActivity;
import com.cylan.jiafeigou.support.photoselect.helpers.Constants;
import com.cylan.jiafeigou.utils.LocaleUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.PackageUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.io.File;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 创建者     谢坤
 * 创建时间   2016/8/9 10:02
 * 描述	      ${TODO}
 * <p>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineInfoFragment extends Fragment implements MineInfoContract.View {

    //拉取出照相机时，产生的状态码
    private static final int REQUEST_CROP_PHOTO = 102;
    private static final int OPEN_CAMERA = 101;

    @BindView(R.id.tv_home_mine_personal_mailbox)
    TextView mTvMailBox;
    @BindView(R.id.RLayout_home_mine_personal_phone)
    RelativeLayout mRlayout_setPersonPhone;
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

    private MineInfoContract.Presenter presenter;
    private JFGAccount argumentData;
    private Uri outPutUri;
    private File tempFile;
    private PopupWindow popupWindow;
    private int navigationHeight;
    private boolean resetPhoto;
    private WeakReference<MyQRCodeDialog> myQrcodeDialog;

    public static HomeMineInfoFragment newInstance() {
        HomeMineInfoFragment fragment = new HomeMineInfoFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_info, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        createCameraTempFile(savedInstanceState);
        getNavigationHeigth();
        return view;
    }

    /**
     * 导航栏高度
     */
    private void getNavigationHeigth() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        navigationHeight = getResources().getDimensionPixelSize(resourceId);
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
        if(NetUtils.getNetType(getContext()) == -1){
            initPersonalInformation(DataSourceManager.getInstance().getJFGAccount());
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * 判断是否大陆用户显示绑定手机号码一栏
     */
    private void initView() {
        int way = LocaleUtils.getLanguageType(getActivity());
        if (way != JConstant.LOCALE_SIMPLE_CN) {
            mRlayout_setPersonPhone.setVisibility(View.GONE);
        } else {
            mRlayout_setPersonPhone.setVisibility(View.VISIBLE);
        }

        if (presenter.checkOpenLogin()) {
            rlChangePassword.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) presenter.start();
    }

    private void initPresenter() {
        presenter = new MineInfoPresenterImpl(this, getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
        if (resetPhoto) {
            //我的界面刷新显示头像
            RxBus.getCacheInstance().post(new RxEvent.LoginMeTab(true));
            resetPhoto = false;
        }
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.btn_home_mine_personal_information,
            R.id.lLayout_home_mine_personal_mailbox, R.id.rLayout_home_mine_personal_pic,
            R.id.RLayout_home_mine_personal_phone, R.id.user_ImageHead,
            R.id.rLayout_home_mine_personal_name, R.id.rl_change_password, R.id.rl_my_QRCode})
    public void onClick(View view) {
        switch (view.getId()) {
            //点击回退到Mine的fragment
            case R.id.tv_toolbar_icon:
                getFragmentManager().popBackStack();
                break;
            //点击退出做相应的逻辑
            case R.id.btn_home_mine_personal_information:
                showLogOutDialog(view);
                break;
            //点击邮箱跳转到相应的页面
            case R.id.lLayout_home_mine_personal_mailbox:
                presenter.bindPersonEmail();
                break;

            case R.id.rLayout_home_mine_personal_pic:           //更换头像
                pickImageDialog(view);
                break;

            case R.id.RLayout_home_mine_personal_phone:         //跳转到设置手机号界面
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.RLayout_home_mine_personal_phone));
                AppLogger.d("RLayout_home_mine_personal_phone");
                jump2SetPhoneFragment();
                break;

            case R.id.user_ImageHead:                           //点击查看大头像
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.user_ImageHead));
                AppLogger.d("user_ImageHead");
                lookBigImageHead();
                break;

            case R.id.rLayout_home_mine_personal_name:          //更改昵称
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.rLayout_home_mine_personal_name));
                AppLogger.d("rLayout_home_mine_personal_name");
                jump2SetUserNameFragment();
                break;

            case R.id.rl_change_password:                       //修改密码
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.rl_change_password));
                AppLogger.d("rl_change_password");
                jump2ChangePasswordFragment();
                break;

            case R.id.rl_my_QRCode:                             //我的二维码
                showMyQrcodeDialog();
                break;
        }
    }

    /**
     * 弹出我的二维码对话框
     */
    private void showMyQrcodeDialog() {
        if (myQrcodeDialog == null || myQrcodeDialog.get() == null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isopenlogin", presenter.checkOpenLogin());
            bundle.putSerializable("jfgaccount", argumentData);
            myQrcodeDialog = new WeakReference<>(MyQRCodeDialog.newInstance(bundle));
        }
        MyQRCodeDialog qRCodeDialog = myQrcodeDialog.get();
        qRCodeDialog.show(getFragmentManager(), "myqrcode");
    }

    /**
     * 修改密码的界面
     */
    private void jump2ChangePasswordFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("userinfo", argumentData);
        MineInfoSetPassWordFragment setPassWordFragment = MineInfoSetPassWordFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, setPassWordFragment, "setPassWordFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
    }

    /**
     * 跳转到修改昵称界面
     */
    private void jump2SetUserNameFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("userinfo", argumentData);
        MineSetUserAliasFragment setUserNameFragment = MineSetUserAliasFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, setUserNameFragment, "setUserNameFragment")
                .addToBackStack("personalInformationFragment")
                .commit();

        if (getActivity() != null && getActivity().getFragmentManager() != null) {
            setUserNameFragment.setOnSetUsernameListener(new MineSetUserAliasFragment.OnSetUsernameListener() {
                @Override
                public void userNameChange(String name) {
                    tvUserName.setText(name);
                    argumentData.setAlias(name);
                }
            });
        }
    }

    /**
     * desc: 查看大头像
     */
    private void lookBigImageHead() {
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", argumentData.getPhotoUrl());
        MineUserInfoLookBigHeadFragment bigHeadFragment = MineUserInfoLookBigHeadFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, bigHeadFragment, "bigHeadFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
    }

    @Override
    public void initPersonalInformation(JFGAccount bean) {
        MyViewTarget myViewTarget = new MyViewTarget(userImageHead, getContext().getResources());
        String photoUrl = "";
        if (bean != null) {
            argumentData = bean;
            //头像的回显
            photoUrl = bean.getPhotoUrl();

            if (presenter.checkOpenLogin()) {
                photoUrl = PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON);
            }

            if (!TextUtils.isEmpty(photoUrl) && getContext() != null) {
                Glide.with(getContext()).load(photoUrl)
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.icon_mine_head_normal)
                        .error(R.drawable.icon_mine_head_normal)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(myViewTarget);
            }

            tvUserAccount.setText(bean.getAccount());

            if (presenter.checkOpenLogin()) {
                String alias = PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ALIAS);
                tvUserName.setText(TextUtils.isEmpty(alias) ? getString(R.string.NO_SET) : alias);
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
        }
    }

    private static class MyViewTarget extends BitmapImageViewTarget {
        private final WeakReference<ImageView> image;
        private final WeakReference<Resources> resources;

        public MyViewTarget(ImageView view, Resources resource) {
            super(view);
            image = new WeakReference<ImageView>(view);
            resources = new WeakReference<Resources>(resource);
        }

        @Override
        protected void setResource(Bitmap resource) {
            if (resource == null)
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
        bundle.putSerializable("userinfo", argumentData);
        HomeMineInfoMailBoxFragment mailBoxFragment = HomeMineInfoMailBoxFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mailBoxFragment, "mailBoxFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
        if (getActivity() != null && getActivity().getFragmentManager() != null) {
            mailBoxFragment.setListener(new HomeMineInfoMailBoxFragment.OnBindMailBoxListener() {
                @Override
                public void mailBoxChange(String content) {
                    mTvMailBox.setText(content);
                }
            });
        }
    }

    /**
     * 弹出选择头像的对话框
     */
    private void pickImageDialog(View v) {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        //设置PopupWindow的View
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_pick_image_popupwindow, null);
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        //设置背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //设置点击弹窗外隐藏自身
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        //设置动画
        popupWindow.setAnimationStyle(R.style.PopupWindow);
        //设置位置
        popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, navigationHeight - 50);
        //设置消失监听
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(1);
            }
        });
        //设置PopupWindow的View点击事件
        setOnPopupViewClick(view);
        //设置背景色
        setBackgroundAlpha(0.4f);
    }

    /**
     * popupwindow条目点击
     *
     * @param view
     */
    private void setOnPopupViewClick(View view) {
        TextView tv_pick_phone, tv_pick_zone, tv_cancel;
        tv_pick_phone = (TextView) view.findViewById(R.id.tv_pick_gallery);
        tv_pick_zone = (TextView) view.findViewById(R.id.tv_pick_camera);
        tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);
        tv_pick_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter.checkExternalStorePermission()) {
                    openGallery();
                } else {
                    //申请权限
                    HomeMineInfoFragment.this.requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            2);
                }
                popupWindow.dismiss();
            }
        });

        tv_pick_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开相机
                if (presenter.checkHasCamera()) {

                    if (presenter.checkCameraPermission()) {
                        if (presenter.cameraIsCanUse()) {
                            openCamera();
                        } else {
                            ToastUtil.showToast(getString(R.string.Tap3_Userinfo_NoCamera));
                        }

                    } else {
                        //申请权限
                        HomeMineInfoFragment.this.requestPermissions(
                                new String[]{Manifest.permission.CAMERA},
                                1);
                    }

                } else {
                    ToastUtil.showToast(getString(R.string.Tap3_Userinfo_NoCamera));
                }
                popupWindow.dismiss();
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
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
        bundle.putSerializable("userinfo", argumentData);
        MineInfoBindPhoneFragment bindPhoneFragment = MineInfoBindPhoneFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, bindPhoneFragment, "bindPhoneFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
    }

    /**
     * 跳转到登录页
     */
    private void jump2LoginFragment() {
        //进入登陆页 login page
        Intent intent = new Intent(getContext(), SmartcallActivity.class);
        intent.putExtra(JConstant.FROM_LOG_OUT, true);
        getActivity().startActivity(intent);
        getActivity().finish();

    }

    @Override
    public void setPresenter(MineInfoContract.Presenter presenter) {

    }

    /**
     * 删除亲友对话框
     */
    public void showLogOutDialog(View v) {
        if (popupWindow != null && popupWindow.isShowing()) {
            return;
        }
        //设置PopupWindow的View
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_del_friend_popupwindow, null);
        popupWindow = new PopupWindow(view, RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        //设置背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //设置点击弹窗外隐藏自身
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        //设置动画
        popupWindow.setAnimationStyle(R.style.PopupWindow);
        //设置位置
        popupWindow.showAtLocation(v, Gravity.BOTTOM, 0, navigationHeight - 50);
        //设置消失监听
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(1);
            }
        });
        //设置PopupWindow的View点击事件
        setOnLogoutClick(view);
        //设置背景色
        setBackgroundAlpha(0.4f);
    }

    private void setOnLogoutClick(View view) {
        TextView tv_is_del, tv_pick_zone, tv_cancel;
        tv_is_del = (TextView) view.findViewById(R.id.tv_is_del_frined);
        tv_pick_zone = (TextView) view.findViewById(R.id.tv_del_friend);
        tv_cancel = (TextView) view.findViewById(R.id.tv_cancel);

        tv_is_del.setTextSize(14);
        tv_is_del.setText(getString(R.string.LOGOUT_INFO));

        tv_pick_zone.setText(getString(R.string.LOGOUT));

        tv_pick_zone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                if (getView() != null && argumentData != null) {
                    presenter.logOut(argumentData.getAccount());
                    jump2LoginFragment();
                    if (getActivity() != null && getActivity() instanceof NewHomeActivity) {
                        ((NewHomeActivity) getActivity()).finishExt();
                    }
                }
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

    }


    //设置屏幕背景透明效果
    public void setBackgroundAlpha(float alpha) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = alpha;
        getActivity().getWindow().setAttributes(lp);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != getActivity().RESULT_CANCELED) {
            if (requestCode == Constants.REQUEST_CODE && data != null) {
                gotoClipActivity(Uri.parse(data.getStringExtra(Constants.INTENT_EXTRA_IMAGES)));
            } else if (requestCode == REQUEST_CROP_PHOTO && data != null) {
                final Uri uri = data.getData();
                if (uri == null) {
                    return;
                }
                String cropImagePath = getRealFilePathFromUri(getContext(), uri);
                PreferencesUtils.putString("UserImageUrl", cropImagePath);
                resetPhoto = true;
                AppLogger.d("upload_succ");
            } else if (requestCode == OPEN_CAMERA) {
                if (resultCode == getActivity().RESULT_OK) {
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
            tempFile = new File(presenter.checkFileExit(Environment.getExternalStorageDirectory().getPath() + "/image/"),
                    System.currentTimeMillis() + ".jpg");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.permission_auth, "", permission))
                .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(getString(R.string.SETTINGS), (DialogInterface dialog, int which) -> {
                    openSetting();
                })
                .create()
                .show();
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
        if (popupWindow != null) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }
}