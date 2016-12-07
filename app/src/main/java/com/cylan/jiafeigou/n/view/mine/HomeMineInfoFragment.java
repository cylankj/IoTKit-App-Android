package com.cylan.jiafeigou.n.view.mine;


import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineInfoPresenterImpl;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoselect.ClipImageActivity;
import com.cylan.jiafeigou.support.photoselect.activities.AlbumSelectActivity;
import com.cylan.jiafeigou.support.photoselect.helpers.Constants;
import com.cylan.jiafeigou.utils.LocaleUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



/**
 * 创建者     谢坤
 * 创建时间   2016/8/9 10:02
 * 描述	      ${TODO}
 * <p/>
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

    private HomeMineInfoMailBoxFragment mailBoxFragment;
    private MineInfoBindPhoneFragment bindPhoneFragment;
    private MineUserInfoLookBigHeadFragment bigHeadFragment;
    private MineSetUserNameFragment setUserNameFragment;
    private MineInfoSetPassWordFragment setPassWordFragment;
    private MineInfoContract.Presenter presenter;
    private AlertDialog alertDialog;
    private JFGAccount argumentData;
    private Uri outPutUri;
    private File tempFile;

    public static HomeMineInfoFragment newInstance() {
        HomeMineInfoFragment fragment = new HomeMineInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_info, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        createCameraTempFile(savedInstanceState);
        return view;
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

    /**
     * 判断是否大陆用户显示绑定手机号码一栏
     */
    private void initView() {
        int way = LocaleUtils.getLanguageType(getActivity());
        if (way != JConstant.LOCALE_SIMPLE_CN) {
            mRlayout_setPersonPhone.setVisibility(View.GONE);
        }else {
            mRlayout_setPersonPhone.setVisibility(View.VISIBLE);
        }

        if (presenter.checkOpenLogin()){
            rlChangePassword.setVisibility(View.GONE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null)presenter.start();
    }

    private void initPresenter() {
        presenter = new MineInfoPresenterImpl(this, getContext());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null)presenter.stop();
    }

    @OnClick({R.id.iv_home_mine_personal_back, R.id.btn_home_mine_personal_information,
            R.id.lLayout_home_mine_personal_mailbox, R.id.rLayout_home_mine_personal_pic,
            R.id.RLayout_home_mine_personal_phone, R.id.user_ImageHead,
            R.id.rLayout_home_mine_personal_name, R.id.rl_change_password})
    public void onClick(View view) {
        switch (view.getId()) {
            //点击回退到Mine的fragment
            case R.id.iv_home_mine_personal_back:
                getFragmentManager().popBackStack();
                break;
            //点击退出做相应的逻辑
            case R.id.btn_home_mine_personal_information:
                showLogOutDialog();
                break;
            //点击邮箱跳转到相应的页面
            case R.id.lLayout_home_mine_personal_mailbox:
                presenter.bindPersonEmail();
                break;

            case R.id.rLayout_home_mine_personal_pic:           //更换头像
                showChooseImageDialog();
                break;

            case R.id.RLayout_home_mine_personal_phone:         //跳转到设置手机号界面
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.RLayout_home_mine_personal_phone));
                AppLogger.e("RLayout_home_mine_personal_phone");
                jump2SetPhoneFragment();
                break;

            case R.id.user_ImageHead:                           //点击查看大头像
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.user_ImageHead));
                AppLogger.e("user_ImageHead");
                lookBigImageHead();
                break;

            case R.id.rLayout_home_mine_personal_name:          //更改昵称
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.rLayout_home_mine_personal_name));
                AppLogger.e("rLayout_home_mine_personal_name");
                jump2SetUserNameFragment();
                break;

            case R.id.rl_change_password:                       //修改密码
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.rl_change_password));
                AppLogger.e("rl_change_password");
                jump2ChangePasswordFragment();
                break;
        }
    }

    /**
     * 修改密码的界面
     */
    private void jump2ChangePasswordFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("userinfo",argumentData);
        setPassWordFragment = MineInfoSetPassWordFragment.newInstance(bundle);
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
        bundle.putSerializable("userinfo",argumentData);
        setUserNameFragment = MineSetUserNameFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, setUserNameFragment, "setUserNameFragment")
                .addToBackStack("personalInformationFragment")
                .commit();

        if (getActivity() != null && getActivity().getFragmentManager() != null) {
            setUserNameFragment.setOnSetUsernameListener(new MineSetUserNameFragment.OnSetUsernameListener() {
                @Override
                public void userNameChange(String name) {
                    tvUserName.setText(name);
                }
            });
        }
    }

    /**
     * desc: 查看大头像
     */
    private void lookBigImageHead() {
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl",argumentData.getPhotoUrl());
        bigHeadFragment = MineUserInfoLookBigHeadFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, bigHeadFragment, "bigHeadFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
    }

    @Override
    public void initPersonalInformation(JFGAccount bean) {
        if (bean != null){
            argumentData = bean;
            //头像的回显
            String photoUrl = bean.getPhotoUrl();
            if ("".equals(bean.getPhotoUrl())){
                photoUrl = PreferencesUtils.getString("UserImageUrl");
            }

            Glide.with(getContext()).load(photoUrl)
                    .asBitmap()
                    .centerCrop()
                    .placeholder(R.drawable.icon_mine_head_normal)
                    .error(R.drawable.icon_mine_head_normal)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(userImageHead) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            userImageHead.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            tvUserAccount.setText(bean.getAccount());

            tvUserName.setText(bean.getAlias());

            if (bean.getEmail() == null | "".equals(bean.getEmail())){
                mTvMailBox.setText(getString(R.string.NO_SET));
            }else {
                mTvMailBox.setText(bean.getEmail());
            }

            if(bean.getPhone() == null && "".equals(bean.getPhone())){
                tvHomeMinePersonalPhone.setText(getString(R.string.NO_SET));
            }else {
                tvHomeMinePersonalPhone.setText(bean.getPhone());
            }
        }else {
            Glide.with(getContext()).load(PreferencesUtils.getString("UserImageUrl"))
                    .asBitmap().centerCrop()
                    .error(R.drawable.icon_mine_head_normal)
                    .into(new BitmapImageViewTarget(userImageHead) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            userImageHead.setImageDrawable(circularBitmapDrawable);
                        }
                    });

        }
    }

    @Override
    public void jump2SetEmailFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("userinfo",argumentData);
        mailBoxFragment = HomeMineInfoMailBoxFragment.newInstance(bundle);
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

    @Override
    public void showChooseImageDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = View.inflate(getContext(), R.layout.layout_dialog_pick_imagehead, null);
        view.findViewById(R.id.tv_pick_from_canmera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开相机
                if (presenter.checkHasCamera()){

                    if(presenter.checkCameraPermission()){

                        if (presenter.cameraIsCanUse()){
                            openCamera();
                            alertDialog.dismiss();
                        }else {
                            ToastUtil.showToast("相机不可用");
                        }

                    }else {
                        //申请权限
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                1);
                    }

                }else {
                    ToastUtil.showToast("没有相机");
                }
            }
        });

        view.findViewById(R.id.tv_pick_from_grallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter.checkExternalStorePermission()){
                    openGallery();
                }else {
                    //申请权限
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            2);
                }
                alertDialog.dismiss();
            }
        });

        view.findViewById(R.id.tv_pick_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();
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
        startActivityForResult(intent,OPEN_CAMERA);
    }

    private void jump2SetPhoneFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("userinfo",argumentData);
        bindPhoneFragment = MineInfoBindPhoneFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, bindPhoneFragment, "bindPhoneFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
    }

    @Override
    public void setPresenter(MineInfoContract.Presenter presenter) {

    }

    @Override
    public void showLogOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.LOGOUT_INFO));
        builder.setPositiveButton(getString(R.string.LOGOUT), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
//                LoadingDialog.showLoading(getFragmentManager(),"正在退出...");
                if (getView() != null){
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            presenter.logOut();
                        }
                    },2000);
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

    /**
     * 退出登录结果
     */
    @Override
    public void logOutResult(int logout) {
        LoadingDialog.dismissLoading(getFragmentManager());
        if (logout == JError.ErrorOK){
            getFragmentManager().popBackStack();
        }else {
            ToastUtil.showNegativeToast("退出登录失败");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE && data != null) {
            gotoClipActivity(Uri.parse(data.getStringExtra(Constants.INTENT_EXTRA_IMAGES)));
        }else if (requestCode == REQUEST_CROP_PHOTO && data != null){
            final Uri uri = data.getData();
            if (uri == null) {
                return;
            }
            String cropImagePath = getRealFilePathFromUri(getContext(), uri);
            PreferencesUtils.putString("UserImageUrl",cropImagePath);
        }else if (requestCode == OPEN_CAMERA){
            if (resultCode == getActivity().RESULT_OK) {
                gotoClipActivity(outPutUri);
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
        if (requestCode == 1){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                ToastUtil.showNegativeToast(getString(R.string.Tap0_Authorizationfailed));
            }
        }else if (requestCode == 2){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                ToastUtil.showNegativeToast(getString(R.string.Tap0_Authorizationfailed));
            }
        }
    }

}
