package com.cylan.jiafeigou.n.view.home;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.home.HomeMineContract;
import com.cylan.jiafeigou.n.view.mine.HomeMineHelpFragment;
import com.cylan.jiafeigou.n.view.mine.HomeMinePersonalInformationFragment;
import com.cylan.jiafeigou.n.view.mine.MineRelativesandFriendsFragment;
import com.cylan.jiafeigou.n.view.mine.MineShareDeviceFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.rxbus.RxBus;
import com.cylan.jiafeigou.utils.ContinuityClickUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.HomeMineItemView;
import com.cylan.jiafeigou.widget.MsgTextView;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

;

public class HomeMineFragment extends Fragment
        implements HomeMineContract.View {
    @BindView(R.id.iv_home_mine_portrait)
    RoundedImageView ivHomeMinePortrait;

    @BindView(R.id.tv_home_mine_nick)
    TextView tvHomeMineNick;

    @BindView(R.id.rLayout_msg_box)
    FrameLayout fLayoutMsgBox;
    @BindView(R.id.tv_home_mine_msg_count)
    MsgTextView tvHomeMineMsgCount;
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

    private HomeMineContract.Presenter presenter;
    private HomeMineHelpFragment mineHelpFragment;
    private HomeMinePersonalInformationFragment personalInformationFragment;
    private HomeSettingFragment homeSettingFragment;
    private HomeMineMessageFragment homeMineMessageFragment;
    private MineShareDeviceFragment mineShareDeviceFragment;
    private MineRelativesandFriendsFragment mineRelativesandFriendsFragment;


    public static HomeMineFragment newInstance(Bundle bundle) {
        HomeMineFragment fragment = new HomeMineFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mineHelpFragment = HomeMineHelpFragment.newInstance(new Bundle());
        personalInformationFragment = HomeMinePersonalInformationFragment.newInstance(new Bundle());
        homeSettingFragment = HomeSettingFragment.newInstance();
        homeMineMessageFragment = HomeMineMessageFragment.newInstance();
        mineShareDeviceFragment = MineShareDeviceFragment.newInstance();
        mineRelativesandFriendsFragment = MineRelativesandFriendsFragment.newInstance();
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
        ViewUtils.setViewMarginStatusBar(fLayoutMsgBox);
    }

    @Override
    public void onStart() {
        super.onStart();
        initName();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void portrait() {
        if (needStartLoginFragment())
            return;
    }

    public void friendItem(View view) {
        if (needStartLoginFragment()) return;
        AppLogger.i("It's Login,can do something!");
    }

//    public void shareItem(View view) {
//        if (needStartLoginFragment()) return;
//        AppLogger.i("It's Login,can do something!");
//    }

    public void settingsItem(View view) {
        if (needStartLoginFragment()) return;
        AppLogger.i("It's Login,can do something!");
        //if (needStartLoginFragment()) return;
        //AppLogger.i("It's Login,can do something!");
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineRelativesandFriendsFragment,
                        "mineRelativesandFriendsFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

//    public void shareItem(View view) {
//        if (needStartLoginFragment()) return;
//        AppLogger.i("It's Login,can do something!");
//    }

    public void shareItem(View view) {
        //if (needStartLoginFragment()) return;
        //SLog.i("It's Login,can do something!");
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineShareDeviceFragment, "mineShareDeviceFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }
//    public void settingsItem(View view) {
//        //if (needStartLoginFragment()) return;
//        //SLog.i("It's Login,can do something!");
//        getFragmentManager().beginTransaction()
//                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
//                        , R.anim.slide_in_left, R.anim.slide_out_right)
//                .add(android.R.id.content, homeSettingFragment, "homeSettingFragment")
//                .addToBackStack("mineHelpFragment")
//                .commit();
//    }

    public void helpItem(View view) {
        if (needStartLoginFragment()) return;
        /*getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content,mineHelpFragment,"mineHelpFragment")
                .addToBackStack("mineHelpFragment")
                .commit();*/
        AppLogger.i("It's Login,can do something!");
    }

    public void blurPic(View view) {
        if (needStartLoginFragment()) return;
        AppLogger.i("It's Login,can do something!");
    }

    @Override
    public void setPresenter(HomeMineContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onPortraitUpdate(String url) {
        if (getActivity() != null) {
//            testBlurBackground(R.drawable.clouds);
            ivHomeMinePortrait.setImageResource(R.drawable.clouds);
            if (presenter != null) presenter.portraitBlur(R.drawable.clouds);
            //presenter.portraitUpdateByUrl(url);
            tvHomeMineMsgCount.post(new Runnable() {
                @Override
                public void run() {
                    tvHomeMineMsgCount.setText("99+");
                }
            });
        }
    }

    @Override
    public void onBlur(Drawable drawable) {
        long time = System.currentTimeMillis();
        rLayoutHomeMineTop.setBackground(drawable);
        AppLogger.e("usetime:%d ms", System.currentTimeMillis() - time);
    }

    @Override
    public void setUserImageHead(Drawable drawable) {
        ivHomeMinePortrait.setImageDrawable(drawable);
    }

    @Override
    public void initName() {
        tvHomeMineNick.setText(presenter.createRandomName());
    }


    private boolean needStartLoginFragment() {
        if (RxBus.getInstance().hasObservers()) {
            RxBus.getInstance().send(new RxEvent.NeedLoginEvent(null));
            return true;
        }
        return false;
    }

    @OnClick({R.id.home_mine_item_friend, R.id.home_mine_item_share,
            R.id.home_mine_item_help, R.id.home_mine_item_settings,
            R.id.shadow_layout, R.id.tv_home_mine_nick, R.id.rLayout_msg_box})
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
                /*if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.home_mine_item_help));
                AppLogger.e("home_mine_item_help");*/
            /*    helpItem(view);*/
                if (ContinuityClickUtils.isFastDoubleClick()) {
                    return;
                }
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, mineHelpFragment, "mineHelpFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();

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
                AppLogger.e("home_mine_item_settings");
                portrait();
                break;
            case R.id.tv_home_mine_nick:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, personalInformationFragment, "personalInformationFragment")
                        .addToBackStack("personalInformationFragment")
                        .commit();
                break;

            case R.id.rLayout_msg_box:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, homeMineMessageFragment, "homeMineMessageFragment")
                        .addToBackStack("personalInformationFragment")
                        .commit();
                break;
        }
    }

}
