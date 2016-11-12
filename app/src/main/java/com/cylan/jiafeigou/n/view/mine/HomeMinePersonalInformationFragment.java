package com.cylan.jiafeigou.n.view.mine;


import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.mine.MinePersonalInformationContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.GlideImageLoaderPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.mine.MinePersonalInformationPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.UserInfoBean;
import com.cylan.jiafeigou.support.galleryfinal.CoreConfig;
import com.cylan.jiafeigou.support.galleryfinal.FunctionConfig;
import com.cylan.jiafeigou.support.galleryfinal.GalleryFinal;
import com.cylan.jiafeigou.support.galleryfinal.ImageLoader;
import com.cylan.jiafeigou.support.galleryfinal.ThemeConfig;
import com.cylan.jiafeigou.support.galleryfinal.model.PhotoInfo;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.util.List;

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
public class HomeMinePersonalInformationFragment extends Fragment implements MinePersonalInformationContract.View {

    //拉取出照相机时，产生的状态码
    private final int REQUEST_CODE_CAMERA = 1000;
    private final int REQUEST_CODE_GALLERY = 1001;
    public FunctionConfig functionConfig;

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


    private HomeMinePersonalInformationMailBoxFragment mailBoxFragment;
    private MineBindPhoneFragment bindPhoneFragment;
    private MineUserInfoLookBigHeadFragment bigHeadFragment;
    private MineSetUserNameFragment setUserNameFragment;
    private MinePersionlInfoSetPassWordFragment setPassWordFragment;
    private MinePersonalInformationContract.Presenter presenter;
    private AlertDialog alertDialog;
    private JFGAccount argumentData;

    public static HomeMinePersonalInformationFragment newInstance(Bundle bundle) {
        HomeMinePersonalInformationFragment fragment = new HomeMinePersonalInformationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bigHeadFragment = MineUserInfoLookBigHeadFragment.newInstance(new Bundle());
        bindPhoneFragment = MineBindPhoneFragment.newInstance(new Bundle());
        setUserNameFragment = MineSetUserNameFragment.newInstance();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine_personal_information, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initGalleryFinal();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initPersonalInformation(getArgumentData());
    }

    /**
     * 初始化相册选择框架
     */
    private void initGalleryFinal() {
        //设置主题
        ThemeConfig theme = new ThemeConfig.Builder()
                .build();
        //配置功能
        functionConfig = new FunctionConfig.Builder()
                .setEnableCamera(true)
                .setEnableEdit(true)
                .setEnableCrop(true)
                .setEnableRotate(true)
                .setCropSquare(true)
                .setEnablePreview(true)
                .build();

        //配置imageloader
        ImageLoader imageloader = new GlideImageLoaderPresenterImpl();
        CoreConfig coreConfig = new CoreConfig.Builder(ContextUtils.getContext(), imageloader, theme)
                .setFunctionConfig(functionConfig)
                .build();
        GalleryFinal.init(coreConfig);
    }

    private void initPresenter() {
        presenter = new MinePersonalInformationPresenterImpl(this, getContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        String mailBoxText = PreferencesUtils.getString("邮箱", "未设置");
        mTvMailBox.setText(mailBoxText);

        //昵称回显
        tvUserName.setText(PreferencesUtils.getString("username", "未设置"));

        //presenter.getUserInfomation(url);              //初始化显示用户信息
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
                //TODO 信息数据的保存
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
        setPassWordFragment = MinePersionlInfoSetPassWordFragment.newInstance(bundle);
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
            //头像的回显
            Glide.with(getContext()).load(bean.getPhotoUrl())
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

            tvUserAccount.setText(bean.getAccount());

            tvUserName.setText(bean.getAlias());

            if (bean.getEmail() == null | "".equals(bean.getEmail())){
                mTvMailBox.setText("未设置");
            }else {
                mTvMailBox.setText(bean.getEmail());
            }

            if(bean.getPhone() == null && "".equals(bean.getPhone())){
                tvHomeMinePersonalPhone.setText("未设置");
            }else {
                tvHomeMinePersonalPhone.setText(bean.getPhone());
            }
        }
    }

    @Override
    public void jump2SetEmailFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("userinfo",argumentData);
        mailBoxFragment = HomeMinePersonalInformationMailBoxFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mailBoxFragment, "mailBoxFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
        if (getActivity() != null && getActivity().getFragmentManager() != null) {
            mailBoxFragment.setListener(new HomeMinePersonalInformationMailBoxFragment.OnBindMailBoxListener() {
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
                GalleryFinal.openCamera(REQUEST_CODE_CAMERA, functionConfig, null);
                alertDialog.dismiss();
            }
        });

        view.findViewById(R.id.tv_pick_from_grallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryFinal.openGallerySingle(REQUEST_CODE_GALLERY,functionConfig, null);
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

    private void jump2SetPhoneFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, bindPhoneFragment, "bindPhoneFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
    }

    @Override
    public void setPresenter(MinePersonalInformationContract.Presenter presenter) {
    }

    @Override
    public void showLogOutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("退出不会删除账号信息，你可以再次登录");
        builder.setPositiveButton("退出登录", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.logOut();
                getFragmentManager().popBackStack();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 获取传递过来的用户信息bean
     * @return
     */
    public JFGAccount getArgumentData() {
        Bundle arguments = getArguments();
        argumentData = (JFGAccount) arguments.getSerializable("userInfoBean");
        return argumentData;
    }
}
