package com.cylan.jiafeigou.n.view.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeMinePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpFragment;
import com.cylan.jiafeigou.n.view.mine.HomeMineInfoFragment;
import com.cylan.jiafeigou.n.view.mine.MineFriendsFragment;
import com.cylan.jiafeigou.n.view.mine.MineInfoBindPhoneFragment;
import com.cylan.jiafeigou.n.view.mine.MineShareDeviceFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoselect.models.Image;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.HomeMineItemView;
import com.cylan.jiafeigou.widget.MsgBoxView;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HomeMineFragment extends IBaseFragment<HomeMineContract.Presenter>
        implements HomeMineContract.View {
    @BindView(R.id.iv_home_mine_portrait)
    RoundedImageView ivHomeMinePortrait;
    @BindView(R.id.tv_home_mine_nick)
    TextView tvHomeMineNick;
    @BindView(R.id.tv_home_mine_msg_count)
    MsgBoxView tvHomeMineMsgCount;
    @BindView(R.id.rLayout_home_mine_top)
    FrameLayout rLayoutHomeMineTop;
    @BindView(R.id.home_mine_item_friend)
    HomeMineItemView homeMineItemFriend;
    @BindView(R.id.home_mine_item_share)
    HomeMineItemView homeMineItemShare;
    @BindView(R.id.home_mine_item_help)
    HomeMineItemView homeMineItemHelp;
    @BindView(R.id.home_mine_item_settings)
    HomeMineItemView homeMineItemSettings;

    public static HomeMineFragment newInstance(Bundle bundle) {
        HomeMineFragment fragment = new HomeMineFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.basePresenter = new HomeMinePresenterImpl(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewMarginStatusBar(tvHomeMineMsgCount);
    }

    @Override
    public void onStart() {
//        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            //访客状态
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.me_bg_top_image);
            basePresenter.portraitBlur(bm);
            setAliasName(getString(R.string.Tap3_LogIn));
        }
        super.onStart();
    }

    /**
     * 点击个人头像
     */
    public void portrait() {
//        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }
        jump2UserInfoFrgment();
    }

    /**
     * 我的亲友
     *
     * @param view
     */
    public void friendItem(View view) {
//        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }

        if (basePresenter.checkOpenLogIn()) {
            if (TextUtils.isEmpty(basePresenter.getUserInfoBean().getEmail()) &&
                    TextUtils.isEmpty(basePresenter.getUserInfoBean().getPhone())) {
                showBindPhoneOrEmailDialog();
                return;
            }
        }
        MineFriendsFragment mineRelativesandFriendsFragment = MineFriendsFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineRelativesandFriendsFragment,
                        "mineRelativesandFriendsFragment")
                .addToBackStack("HomeMineFragment")
                .commit();
    }

    /**
     * 弹出绑定手机或者邮箱的提示框
     */
    private void showBindPhoneOrEmailDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setTitle(getString(R.string.Tap3_Friends_NoBindTips));
        b.setPositiveButton(getString(R.string.Tap2_Index_Open_NoDeviceOption), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                jump2SetPhoneFragment();
            }
        });
        b.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public void settingsItem(View view) {
//        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }
        HomeSettingFragment homeSettingFragment = HomeSettingFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, homeSettingFragment,
                        "homeSettingFragment")
                .addToBackStack("HomeMineFragment")
                .commit();
    }

    public void shareItem(View view) {
//        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }
        MineShareDeviceFragment mineShareDeviceFragment = MineShareDeviceFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineShareDeviceFragment, "mineShareDeviceFragment")
                .addToBackStack("HomeMineFragment")
                .commit();
    }

    public void blurPic(View view) {
        if (needStartLoginFragment()) return;
        AppLogger.i("It's Login,can do something!");
    }

    @Override
    public void setPresenter(HomeMineContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @Override
    public void onPortraitUpdate(String url) {
//        if (getActivity() != null) {
//            ivHomeMinePortrait.setImageResource(R.drawable.clouds);
//            if (basePresenter != null) basePresenter.portraitBlur(R.drawable.clouds);
//            tvHomeMineMsgCount.post(new Runnable() {
//                @Override
//                public void run() {
//                    tvHomeMineMsgCount.setText("99+");
//                }
//            });
//        }
    }

    @Override
    public void onBlur(Drawable drawable) {
        long time = System.currentTimeMillis();
        rLayoutHomeMineTop.setBackground(drawable);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed() && isVisibleToUser && getActivity() != null) {
        }
    }

    /**
     * 设置昵称
     *
     * @param name
     */
    @Override
    public void setAliasName(String name) {
        tvHomeMineNick.setText(name);
    }

    @Override
    public void setUserImageHeadByUrl(String url) {
        if (getContext() == null) return;
        MySimpleTarget mySimpleTarget = new MySimpleTarget(ivHomeMinePortrait, getResources().getDrawable(R.drawable.me_bg_top_image), rLayoutHomeMineTop, url, basePresenter);
        Glide.with(getContext()).load(url)
                .asBitmap()
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mySimpleTarget);
    }

    public static class MySimpleTarget extends SimpleTarget<Bitmap> {
        private final WeakReference<ImageView> image;
        private final WeakReference<HomeMineContract.Presenter> basePresenter;
        private final WeakReference<FrameLayout> mFrameLayout;
        private final WeakReference<Drawable> mDrawable;
        private String url;

        public MySimpleTarget(ImageView view, Drawable drawable, FrameLayout frameLayout, String url, HomeMineContract.Presenter presenter) {
            image = new WeakReference<ImageView>(view);
            mFrameLayout = new WeakReference<FrameLayout>(frameLayout);
            basePresenter = new WeakReference<HomeMineContract.Presenter>(presenter);
            mDrawable = new WeakReference<Drawable>(drawable);
            this.url = url;
        }

        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
            image.get().setImageBitmap(resource);
            if (!TextUtils.isEmpty(url)) {
                if (url.contains("default")) {
                    mFrameLayout.get().setBackground(mDrawable.get());
                } else {
                    Bitmap bitmap = Bitmap.createBitmap(resource);
                    basePresenter.get().portraitBlur(bitmap);
                }
            } else {
                mFrameLayout.get().setBackground(mDrawable.get());
            }
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            super.onLoadFailed(e, errorDrawable);
            mFrameLayout.get().setBackground(mDrawable.get());
        }
    }


    /**
     * 设置新消息的数量
     */
    @Override
    public void setMesgNumber(final int number) {
        if (number == 0) {
            return;
        }
        tvHomeMineMsgCount.post(new Runnable() {
            @Override
            public void run() {
                tvHomeMineMsgCount.setText(number + "+");
            }
        });
    }

    private boolean needStartLoginFragment() {
        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON && RxBus.getCacheInstance().hasObservers()) {
            ((NeedLoginActivity) getActivity()).signInFirst(null);
            return true;
        }
        return false;
    }

    @OnClick({R.id.home_mine_item_friend, R.id.home_mine_item_share,
            R.id.home_mine_item_help, R.id.home_mine_item_settings,
            R.id.shadow_layout, R.id.tv_home_mine_nick, R.id.tv_home_mine_msg_count})
    public void onButterKnifeClick(View view) {
        switch (view.getId()) {
            case R.id.home_mine_item_friend:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.home_mine_item_friend));
                AppLogger.e("home_mine_item_friend");
                friendItem(view);
                break;
            case R.id.home_mine_item_share:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.home_mine_item_share));
                AppLogger.e("home_mine_item_share");
                shareItem(view);
                break;
            case R.id.home_mine_item_help:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.home_mine_item_help));
                AppLogger.e("home_mine_item_help");
                helpItem(view);
                break;
            case R.id.home_mine_item_settings:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.home_mine_item_settings));
                AppLogger.e("home_mine_item_settings");
                settingsItem(view);
                break;
            case R.id.shadow_layout:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.shadow_layout));
                AppLogger.e("shadow_layout");
                portrait();
                break;
            case R.id.tv_home_mine_nick:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_home_mine_nick));
                AppLogger.e("tv_home_mine_nick");
                jump2UserInfo();
                break;

            case R.id.tv_home_mine_msg_count:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_home_mine_msg_count));
                AppLogger.e("tv_home_mine_msg_count");
                jump2MesgFragment();
                break;
        }
    }

    /**
     * 帮助与反馈
     *
     * @param view
     */
    private void helpItem(View view) {
//        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }
        HomeMineHelpFragment mineHelpFragment = HomeMineHelpFragment.newInstance(new Bundle());
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineHelpFragment, "mineHelpFragment")
                .addToBackStack("HomeMineFragment")
                .commit();
    }

    /**
     * 跳转到消息界面
     */
    private void jump2MesgFragment() {
//        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("hasNewMesg", basePresenter.hasUnReadMesg());
        HomeMineMessageFragment homeMineMessageFragment = HomeMineMessageFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, homeMineMessageFragment, "homeMineMessageFragment")
                .addToBackStack("HomeMineFragment")
                .commit();
    }

    /**
     * 点击个人昵称
     */
    private void jump2UserInfo() {
//        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }
        jump2UserInfoFrgment();
    }

    /**
     * 跳转个人信息页
     */
    private void jump2UserInfoFrgment() {
        HomeMineInfoFragment personalInformationFragment = HomeMineInfoFragment.newInstance();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, personalInformationFragment, "personalInformationFragment")
                .addToBackStack("HomeMineFragment")
                .commit();
    }

    /**
     * 跳转到绑定手机
     */
    private void jump2SetPhoneFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("show_login_fragment", "show_login_fragment");
        bundle.putBoolean(JConstant.KEY_SHOW_LOGIN_FRAGMENT_EXTRA, true);
        bundle.putBoolean(JConstant.OPEN_LOGIN_TO_BIND_PHONE, true);
        ((NeedLoginActivity) getActivity()).signInFirst(bundle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment mineInfoFragment = getFragmentManager().findFragmentByTag("personalInformationFragment");
        mineInfoFragment.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            AppLogger.e("onDetach:"+e.getLocalizedMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            AppLogger.e("onDetach:"+e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

}
