package com.cylan.jiafeigou.n.view.home;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeMinePresenterImpl;
import com.cylan.jiafeigou.n.view.activity.MineInfoActivity;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.mine.BindMailFragment;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpActivity;
import com.cylan.jiafeigou.n.view.mine.HomeMineShareManagerFragment;
import com.cylan.jiafeigou.n.view.mine.MineFriendsFragment;
import com.cylan.jiafeigou.n.view.mine.MineInfoBindPhoneFragment;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.badge.TreeHelper;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.HomeMineItemView;
import com.cylan.jiafeigou.widget.MsgBoxView;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;
import static com.cylan.jiafeigou.n.base.BaseApplication.getAppComponent;

@Badge(parentTag = "NewHomeActivity")
public class HomeMineFragment extends IBaseFragment<HomeMineContract.Presenter>
        implements HomeMineContract.View {
    @BindView(R.id.iv_home_mine_portrait)
    RoundedImageView ivHomeMinePortrait;
    @BindView(R.id.tv_home_mine_nick)
    TextView tvHomeMineNick;
    @BindView(R.id.tv_home_mine_msg_count)
    MsgBoxView tvHomeMineMsgCount;
    @BindView(R.id.rLayout_home_mine_top)
    ImageView rLayoutHomeMineTop;
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
        this.presenter = new HomeMinePresenterImpl(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_mine, container, false);
        ButterKnife.bind(this, view);
        isPrepared = true;

        return view;
    }

    @Override
    protected void lazyLoad() {
        super.lazyLoad();
        if (isPrepared) {
            presenter.fetchNewInfo();
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewMarginStatusBar(tvHomeMineMsgCount);
    }


    @Override
    public void onStart() {
        super.onStart();
        //查询好友列表.
        presenter.fetchNewInfo();
        boolean needShowHelp = PreferencesUtils.getBoolean(JConstant.KEY_HELP_GUIDE, true);
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            Schedulers.io().createWorker().schedule(() -> {
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.me_bg_top_image);
                presenter.portraitBlur(bm);
            });
        }

        updateHint();
        homeMineItemHelp.showHint(needShowHelp);
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

//        if (true) {//just for test
//            showBindPhoneOrEmailDialog(getString(R.string.Tap3_Friends_NoBindTips));
//            return;
//        }

        if (presenter.checkOpenLogIn()) {
            if (TextUtils.isEmpty(presenter.getUserInfoBean().getEmail()) &&
                    TextUtils.isEmpty(presenter.getUserInfoBean().getPhone())) {
                showBindPhoneOrEmailDialog(getString(R.string.Tap3_Friends_NoBindTips));
                return;
            }
        }

        BaseApplication.getAppComponent().getTreeHelper().markNodeRead(MineFriendsFragment.class.getSimpleName());
        MineFriendsFragment mineFriendsFragment = MineFriendsFragment.newInstance();
        ActivityUtils.addFragmentSlideInFromRight(getFragmentManager(), mineFriendsFragment,
                android.R.id.content);
        PreferencesUtils.putLong(JConstant.FRIEND_LAST_VISABLE_TIME, System.currentTimeMillis());
        updateHint();
    }

    /**
     * 弹出绑定手机或者邮箱的提示框
     */
    private void showBindPhoneOrEmailDialog(String title) {
        Fragment f = getActivity().getSupportFragmentManager().findFragmentByTag("bindphone");
        if (f == null) {
            AlertDialogManager.getInstance().showDialog(getActivity(), title, title,
                    getString(R.string.Tap2_Index_Open_NoDeviceOption),
                    (DialogInterface dialog, int which) -> {
                        Bundle bundle = new Bundle();
                        Fragment fragment = null;
                        Locale locale = getResources().getConfiguration().locale;
                        String language = locale.getLanguage();
                        if (language.endsWith("zh")) {
                            fragment = MineInfoBindPhoneFragment.newInstance(bundle);
                        } else {
                            fragment = BindMailFragment.newInstance(bundle);
                        }
                        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(), fragment, android.R.id.content, "bindStack");
                    }, getString(R.string.CANCEL), null, false);
        }
    }

    public void settingsItem(View view) {
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }
        HomeSettingFragment homeSettingFragment = HomeSettingFragment.newInstance();
        ActivityUtils.addFragmentSlideInFromRight(getFragmentManager(), homeSettingFragment,
                android.R.id.content);
        homeSettingFragment.setCallBack(t -> {
            BaseApplication.getAppComponent().getTreeHelper().markNodeRead(this.getClass().getSimpleName());
            updateHint();
            //更新 home tab mine
            RxBus.getCacheInstance().postSticky(new RxEvent.InfoUpdate());
        });
    }

    public void shareItem(View view) {
        if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//        if (PreferencesUtils.getInt(JConstant.IS_lOGINED, 0) == 0) {
            needStartLoginFragment();
            return;
        }
//
//        MineShareDeviceFragment mineShareDeviceFragment = MineShareDeviceFragment.newInstance();
//        getActivity().getSupportFragmentManager().beginTransaction()
//                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
//                        , R.anim.slide_in_left, R.anim.slide_out_right)
//                .add(android.R.id.content, mineShareDeviceFragment, "mineShareDeviceFragment")
//                .addToBackStack("HomeMineFragment")
//                .commit();
        HomeMineShareManagerFragment managerFragment = HomeMineShareManagerFragment.newInstance(null);
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, managerFragment, HomeMineShareManagerFragment.class.getSimpleName())
                .addToBackStack("HomeMineFragment")
                .commit();
    }


    @Override
    public void onPortraitUpdate(String url) {
    }

    @Override
    public void onBlur(Drawable drawable) {
        rLayoutHomeMineTop.setBackground(drawable);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser && isAdded()) {
//            if (getAppComponent().getSourceManager().getLoginState() != LogState.STATE_ACCOUNT_ON) {
//                //访客状态
//                Observable.just("go")
//                        .subscribeOn(Schedulers.io())
//                        .subscribe(ret -> {
//                            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.me_bg_top_image);
//                            presenter.portraitBlur(bm);
//                        }, AppLogger::e);
//            }
//            lazyLoad();
//            //查询好友列表.
//            updateHint();
//        }
    }


    /**
     * 设置昵称
     *
     * @param name
     */
    @Override
    public void setAliasName(String name) {
        AppLogger.d("用户昵称:" + name + "add:" + isVisible());
        tvHomeMineNick.setText(name);
    }

    private boolean isDefaultPhoto(String photoUrl) {
        return TextUtils.isEmpty(photoUrl) || photoUrl.contains("image/default.jpg");
    }

    public boolean checkOpenLogin() {
        return BaseApplication.getAppComponent().getSourceManager().getAccount().getLoginType() >= 3;
    }

    @Override
    public void setUserImageHeadByUrl(String url) {
        AppLogger.w("user_img:" + url);
//        MySimpleTarget mySimpleTarget = new MySimpleTarget(ivHomeMinePortrait, getContext().getResources().getDrawable(R.drawable.me_bg_top_image), rLayoutHomeMineTop, url, presenter);
        Account account = BaseApplication.getAppComponent().getSourceManager().getAccount();
//        if (account != null) {
        url = isDefaultPhoto(url) && checkOpenLogin() ? PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON) : url;
        if (TextUtils.isEmpty(url)) {
            return;//空 不需要加载
        }
        Glide.with(getContext()).load(url)
                .asBitmap()
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .signature(new StringSignature(TextUtils.isEmpty(account.getToken()) ? "account" : account.getToken()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new ImageViewTarget<Bitmap>(ivHomeMinePortrait) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        view.setImageBitmap(resource);
                        presenter.portraitBlur(Bitmap.createBitmap(resource));
                    }
                });
//        }
//                .into(mySimpleTarget);
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
                if (getView() != null) {
                    ViewUtils.deBounceClick(getView().findViewById(R.id.home_mine_item_friend));
                }
                friendItem(view);
                break;
            case R.id.home_mine_item_share:
                if (getView() != null) {
                    ViewUtils.deBounceClick(getView().findViewById(R.id.home_mine_item_share));
                }
                shareItem(view);
                break;
            case R.id.home_mine_item_help:
                if (getView() != null) {
                    ViewUtils.deBounceClick(getView().findViewById(R.id.home_mine_item_help));
                }
                helpItem(view);
                break;
            case R.id.home_mine_item_settings:
                if (getView() != null) {
                    ViewUtils.deBounceClick(getView().findViewById(R.id.home_mine_item_settings));
                }
                settingsItem(view);
                break;
            case R.id.shadow_layout:
                if (getView() != null) {
                    ViewUtils.deBounceClick(getView().findViewById(R.id.shadow_layout));
                }
                portrait();
                break;
            case R.id.tv_home_mine_nick:
                if (getView() != null) {
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_home_mine_nick));
                }
                jump2UserInfo();
                break;

            case R.id.tv_home_mine_msg_count:
                if (getView() != null) {
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_home_mine_msg_count));
                }
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
        Intent intent = new Intent(getContext(), HomeMineHelpActivity.class);
        startActivity(intent, ActivityOptionsCompat.makeCustomAnimation(getContext(),
                R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
//        ActivityUtils.addFragmentSlideInFromRight(getFragmentManager(), HomeMineHelpActivity.newInstance(new Bundle()),
//                android.R.id.content);
        homeMineItemHelp.showHint(false);
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
        SystemMessageFragment systemMessageFragment = SystemMessageFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getFragmentManager(),
                systemMessageFragment, android.R.id.content);
        tvHomeMineMsgCount.setText("");
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
        //注意activity的启动模式.
        startActivityForResult(new Intent(getActivity(), MineInfoActivity.class), 10000);
    }

    /**
     * 跳转到绑定手机
     */
    @Override
    public void jump2SetPhoneFragment() {
        Bundle bundle = new Bundle();
        MineInfoBindPhoneFragment bindPhoneFragment = MineInfoBindPhoneFragment.newInstance(bundle);
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                bindPhoneFragment, android.R.id.content);
    }

    /**
     * 跳转到绑定邮箱界面
     */
    @Override
    public void jump2BindMailFragment() {
        ActivityUtils.addFragmentSlideInFromRight(getActivity().getSupportFragmentManager(),
                BindMailFragment.newInstance(null), android.R.id.content);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateHint();
    }

    @Override
    public void updateHint() {
        TreeHelper helper = BaseApplication.getAppComponent().getTreeHelper();
        TreeNode node = helper.findTreeNodeByName(MineFriendsFragment.class.getSimpleName());
        long aLong = PreferencesUtils.getLong(JConstant.FRIEND_LAST_VISABLE_TIME);
        //好友
        ArrayList<JFGFriendRequest> list = DataSourceManager.getInstance().getFriendsReqList();
        int count = 0;
        if (list != null) {
            for (JFGFriendRequest bean : list) {
                if (bean.time >= aLong / 1000) {
                    count++;
                }
            }
        }
//        int count = node == null ? 0 : node.getNodeCount();
        if (count == 0) {
            homeMineItemFriend.showHint(false);
        } else {
            homeMineItemFriend.showNumber(count);//count ==0 dismiss
        }
        //系统消息未读数
        node = helper.findTreeNodeByName(SystemMessageFragment.class.getSimpleName());
        count = node == null ? 0 : node.getNodeCount();
        tvHomeMineMsgCount.setText(count == 0 ? null : count > 99 ? "99+" : String.valueOf(count));
        //意见反馈
        node = helper.findTreeNodeByName(HomeMineHelpActivity.class.getSimpleName());
        count = node == null ? 0 : node.getNodeCount();
        homeMineItemHelp.showHint(count > 0);
        //分享管理

        //设置
        node = helper.findTreeNodeByName(WechatGuideFragment.class.getSimpleName());
        homeMineItemSettings.showHint(node != null && node.getTraversalCount() > 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Fragment mineInfoFragment = getActivity().getSupportFragmentManager().findFragmentByTag("personalInformationFragment");
//        mineInfoFragment.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 10000:
                    Intent intent = new Intent(getContext(), SmartcallActivity.class);
                    intent.putExtra(JConstant.FROM_LOG_OUT, true);
                    startActivity(intent);
                    getActivity().finish();
                    break;
            }
        }
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
