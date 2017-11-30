package com.cylan.jiafeigou.n.view.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.support.v8.renderscript.ScriptIntrinsicResize;
import android.support.v8.renderscript.Short4;
import android.support.v8.renderscript.Type;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.signature.ObjectKey;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.helper.ScriptC_tint;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.GlideApp;
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

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

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
        presenter.fetchNewInfo();
        boolean needShowHelp = PreferencesUtils.getBoolean(JConstant.KEY_HELP_GUIDE, true);
        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
            GlideApp.with(this)
                    .load(R.drawable.me_bg_top_image)
                    .error(R.drawable.me_bg_top_image)
                    .placeholder(R.drawable.me_bg_top_image)
                    .transform(new BlurTransFormation(getContext(), "home_mine_default_background", 20, 0.5f, Color.parseColor("#40000000")))
                    .into(rLayoutHomeMineTop);
        }
        updateHint();
        homeMineItemHelp.showHint(needShowHelp);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewMarginStatusBar(tvHomeMineMsgCount);
    }

    /**
     * 点击个人头像
     */
    public void portrait() {
        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
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
        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
            needStartLoginFragment();
            return;
        }
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
        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
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
        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
            needStartLoginFragment();
            return;
        }

        HomeMineShareManagerFragment managerFragment = HomeMineShareManagerFragment.newInstance(null);
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, managerFragment, HomeMineShareManagerFragment.class.getSimpleName())
                .addToBackStack("HomeMineFragment")
                .commit();
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
        return DataSourceManager.getInstance().getAccount().getLoginType() >= 3;
    }

    @Override
    public void setUserImageHeadByUrl(String url) {
        AppLogger.w("user_img:" + url);
        Account account = DataSourceManager.getInstance().getAccount();
        url = isDefaultPhoto(url) && checkOpenLogin() ? PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON) : url;
        if (TextUtils.isEmpty(url)) {
            return;//空 不需要加载
        }
        GlideApp.with(this)
                .load(url)
                .error(R.drawable.icon_mine_head_normal)
                .placeholder(R.drawable.icon_mine_head_normal)
                .signature(new ObjectKey(TextUtils.isEmpty(account.getToken()) ? "account" : account.getToken()))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivHomeMinePortrait);
        portraitBlur(url);
    }

    private static class BlurTransFormation extends BitmapTransformation {
        private WeakReference<Context> contextWeakReference;
        private final String key;
        private final int radius;
        private final float scale;
        private final int tintColor;

        public BlurTransFormation(Context context, String key, int radius, float scale, int color) {
            this.contextWeakReference = new WeakReference<>(context);
            this.key = key;
            this.radius = radius;
            this.scale = scale;
            this.tintColor = color;
        }

        private static Short4 convertColor2Short4(int color) {
            short b = (short) (color & 0xFF);
            short g = (short) ((color >> 8) & 0xFF);
            short r = (short) ((color >> 16) & 0xFF);
            short a = (short) ((color >> 24) & 0xFF);
            return new Short4(r, g, b, a);
        }

        @Override
        protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            Context context = contextWeakReference.get();
            if (context == null) return null;
            RenderScript renderScript = RenderScript.create(context);
            Allocation sourceAllocation = Allocation.createFromBitmap(renderScript, toTransform);
            int width = (int) (toTransform.getWidth() * scale);
            int height = (int) (toTransform.getHeight() * scale);
            Type resizeType = Type.createXY(renderScript, sourceAllocation.getElement(), width, height);
            Allocation tempAllocation1 = Allocation.createTyped(renderScript, resizeType);
            Allocation tempAllocation2 = Allocation.createTyped(renderScript, resizeType);


            ScriptIntrinsicResize intrinsicResize = ScriptIntrinsicResize.create(renderScript);
            intrinsicResize.setInput(sourceAllocation);
            intrinsicResize.forEach_bicubic(tempAllocation1);

            ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
            intrinsicBlur.setInput(tempAllocation1);
            intrinsicBlur.setRadius(radius);
            intrinsicBlur.forEach(tempAllocation2);

            ScriptC_tint scriptC_tint = new ScriptC_tint(renderScript);
            scriptC_tint.set_maskColor(convertColor2Short4(tintColor));
            scriptC_tint.forEach_mask(tempAllocation2, tempAllocation1);
            Bitmap bitmap = pool.get(width, height, toTransform.getConfig());
            tempAllocation1.copyTo(bitmap);
            renderScript.destroy();
            return bitmap;
        }

        @Override
        public void updateDiskCacheKey(MessageDigest messageDigest) {
            messageDigest.update(key.getBytes());

        }
    }

    private void portraitBlur(String url) {
        Account account = DataSourceManager.getInstance().getAccount();
        GlideApp.with(this)
                .load(url)
                .error(R.drawable.me_bg_top_image)
                .placeholder(R.drawable.me_bg_top_image)
                .transform(new BlurTransFormation(getContext(),
                        TextUtils.isEmpty(account.getToken()) ? "account" : account.getToken(),
                        20, 0.5f, Color.parseColor("#40000000")))
                .into(rLayoutHomeMineTop);
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
        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
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
        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
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
        if (DataSourceManager.getInstance().getLoginState() != LogState.STATE_ACCOUNT_ON) {
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
}
