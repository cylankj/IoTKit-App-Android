package com.cylan.jiafeigou.n.view.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.bumptech.glide.signature.StringSignature;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeMinePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpFragment;
import com.cylan.jiafeigou.n.view.mine.HomeMineInfoFragment;
import com.cylan.jiafeigou.n.view.mine.HomeMineInfoMailBoxFragment;
import com.cylan.jiafeigou.n.view.mine.MineFriendsFragment;
import com.cylan.jiafeigou.n.view.mine.MineInfoBindPhoneFragment;
import com.cylan.jiafeigou.n.view.mine.MineShareDeviceFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.HomeMineItemView;
import com.cylan.jiafeigou.widget.MsgBoxView;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.n.base.BaseApplication.getAppComponent;


public class HomeMineFragment extends IBaseFragment<HomeMineContract.Presenter>
        implements HomeMineContract.View, BaseDialog.BaseDialogAction {
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
    private boolean isPrepared = false;
    private boolean isVisible = false;

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
        isPrepared = true;
        lazyLoad();
        return view;
    }

    private void lazyLoad() {
        if (isPrepared) {
            basePresenter.getUnReadMesg();


        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewMarginStatusBar(tvHomeMineMsgCount);
    }


    @Override
    public void onStart() {
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
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
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
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
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }

        if (basePresenter.checkOpenLogIn()) {
            if (TextUtils.isEmpty(basePresenter.getUserInfoBean().getEmail()) &&
                    TextUtils.isEmpty(basePresenter.getUserInfoBean().getPhone())) {
                showBindPhoneOrEmailDialog(getString(R.string.Tap3_Friends_NoBindTips));
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
    private void showBindPhoneOrEmailDialog(String title) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("bindphone");
        if (f == null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialog.KEY_TITLE, title);
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CANCEL));
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.Tap2_Index_Open_NoDeviceOption));
            bundle.putBoolean(SimpleDialogFragment.KEY_TOUCH_OUT_SIDE_DISMISS, false);
            SimpleDialogFragment dialogFragment = SimpleDialogFragment.newInstance(bundle);
            dialogFragment.setAction(this);
            dialogFragment.show(getActivity().getSupportFragmentManager(), "bindphone");
        }
    }

    public void settingsItem(View view) {
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
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
                .commitAllowingStateLoss();
    }

    public void shareItem(View view) {
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }

        if (basePresenter.checkOpenLogIn()) {
            if (TextUtils.isEmpty(basePresenter.getUserInfoBean().getEmail()) &&
                    TextUtils.isEmpty(basePresenter.getUserInfoBean().getPhone())) {
                showBindPhoneOrEmailDialog(getString(R.string.Tap3_Friends_NoBindTips));
                return;
            }
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
        if (isVisibleToUser && isAdded()) {
            if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
                //访客状态
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.me_bg_top_image);
                basePresenter.portraitBlur(bm);
                setAliasName(getString(R.string.Tap3_LogIn));
                Account account = getAppComponent().getSourceManager().getAccount();
                if (account != null) {
                    setUserImageHeadByUrl(account.getPhotoUrl());
                }
            }
            lazyLoad();
        }
        JFGAccount account = getAppComponent().getSourceManager().getJFGAccount();
        if (account != null && TextUtils.isEmpty(account.getPhotoUrl())) {
            getAppComponent().getCmd().getAccount();
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
        AppLogger.d("user_img:" + url);
        MySimpleTarget mySimpleTarget = new MySimpleTarget(ivHomeMinePortrait, getContext().getResources().getDrawable(R.drawable.me_bg_top_image), rLayoutHomeMineTop, url, basePresenter);
        Account account = getAppComponent().getSourceManager().getAccount();
        if (account == null || getContext() == null) return;
        AppLogger.d("account Token:" + account.getToken());
        Glide.with(getContext()).load(url)
                .asBitmap()
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .signature(new StringSignature(account.getToken()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mySimpleTarget);

    }

    public static class MySimpleTarget extends SimpleTarget<Bitmap> {
        private final WeakReference<ImageView> image;
        private final WeakReference<HomeMineContract.Presenter> basePresenter;
        private final WeakReference<FrameLayout> mFrameLayout;
        private final WeakReference<Drawable> mDrawable;
        private String url;

        public MySimpleTarget(ImageView view, Drawable drawable, FrameLayout frameLayout, String url, HomeMineContract.Presenter presenter) {
            image = new WeakReference<>(view);
            mFrameLayout = new WeakReference<>(frameLayout);
            basePresenter = new WeakReference<>(presenter);
            mDrawable = new WeakReference<>(drawable);
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
        AppLogger.d("ssss" + number);
        tvHomeMineMsgCount.setText(number == 0 ? null : number > 99 ? "99+" : String.valueOf(number));
    }

    private boolean needStartLoginFragment() {
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON && RxBus.getCacheInstance().hasObservers()) {
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
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }
        if (!NetUtils.isNetworkAvailable(getContext())) {
            ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
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
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
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
        homeMineMessageFragment.setOnClearMsgCountListener(() -> tvHomeMineMsgCount.setText(""));
    }

    /**
     * 点击个人昵称
     */
    private void jump2UserInfo() {
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
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
    @Override
    public void jump2SetPhoneFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("userinfo", getAppComponent().getSourceManager().getJFGAccount());
        MineInfoBindPhoneFragment bindPhoneFragment = MineInfoBindPhoneFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, bindPhoneFragment, "bindPhoneFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
    }

    /**
     * 跳转到绑定邮箱界面
     */
    @Override
    public void jump2BindMailFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("userinfo", getAppComponent().getSourceManager().getJFGAccount());
        HomeMineInfoMailBoxFragment bindMailFragment = HomeMineInfoMailBoxFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, bindMailFragment, "bindMailFragment")
                .addToBackStack("personalInformationFragment")
                .commit();
    }

    @Override
    public void onDialogAction(int id, Object value) {
        if (id == R.id.tv_dialog_btn_right) {
            basePresenter.loginType();
        }
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
            AppLogger.e("onDetach:" + e.getLocalizedMessage());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            AppLogger.e("onDetach:" + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

}
