package com.cylan.jiafeigou;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cylan.jiafeigou.cache.LogState;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.OnActivityReenterListener;
import com.cylan.jiafeigou.misc.SharedElementCallBackListener;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.engine.AfterLoginService;
import com.cylan.jiafeigou.n.mvp.contract.home.NewHomeActivityContract;
import com.cylan.jiafeigou.n.mvp.impl.home.NewHomeActivityPresenterImpl;
import com.cylan.jiafeigou.n.view.activity.NeedLoginActivity;
import com.cylan.jiafeigou.n.view.home.HomeMineFragment;
import com.cylan.jiafeigou.n.view.home.HomePageListFragmentExt;
import com.cylan.jiafeigou.n.view.home.HomeWonderfulFragmentExt;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.badge.Badge;
import com.cylan.jiafeigou.support.badge.TreeNode;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomViewPager;
import com.cylan.jiafeigou.widget.HintRadioButton;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Badge(parentTag = "NewHomeActivity")
public class NewHomeActivity extends NeedLoginActivity<NewHomeActivityContract.Presenter> implements
        NewHomeActivityContract.View {
    @BindView(R.id.vp_home_content)
    CustomViewPager vpHomeContent;
    @BindView(R.id.rgLayout_home_bottom_menu)
    RadioGroup rgLayoutHomeBottomMenu;

    public static final String KEY_ENTER_ANIM_ID = "key_enter_anim_id";
    public static final String KEY_EXIT_ANIM_ID = "key_exit_anim_id";

    @BindView(R.id.btn_home_list)
    RadioButton btnHomeList;
    @BindView(R.id.btn_home_wonderful)
    RadioButton btnHomeWonderful;
    @BindView(R.id.btn_home_mine)
    HintRadioButton btnHomeMine;

    private SharedElementCallBackListener sharedElementCallBackListener;

    private Subscription subscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(getResources().getDrawable(android.R.color.white));
        super.onCreate(savedInstanceState);
        PerformanceUtils.startTrace("NewHomeActivityStart");
        IMEUtils.fixFocusedViewLeak(getApplication());
        setContentView(R.layout.activity_new_home);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            initSharedElementCallback();
//            setExitSharedElementCallback(mCallback);
        }
        initBottomMenu();
        initMainContentAdapter();
        initShowWonderPageSub();
        basePresenter = new NewHomeActivityPresenterImpl(this);
        AfterLoginService.resumeTryCheckVersion();
    }

    private void initShowWonderPageSub() {
        subscribe = RxBus.getCacheInstance().toObservable(RxEvent.ShowWonderPageEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> vpHomeContent.setCurrentItem(1), e -> AppLogger.d(e.getMessage()));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.hasExtra("NewHomeActivity_intent")) {
            ToastUtil.showToast(intent.getStringExtra("NewHomeActivity_intent"));
        }
        if (intent != null && intent.hasExtra(JConstant.KEY_JUMP_TO_WONDER)) {
            int cIndex = vpHomeContent.getCurrentItem();
            if (cIndex != 1) {
                vpHomeContent.setCurrentItem(1);
            }
        }
    }

    /**
     * TYPE_MOBILE
     * Dispatch onFinish() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (BaseApplication.getAppComponent().getSourceManager().getLoginState() == LogState.STATE_ACCOUNT_OFF) {
            finish();
            Intent intent = new Intent(this, SmartcallActivity.class);
            intent.putExtra(JConstant.FROM_LOG_OUT, true);
            intent.putExtra("PSWC", true);
            startActivity(intent);
            return;
        }
        RxBus.getCacheInstance().toObservableSticky(RxEvent.NeedUpdateGooglePlayService.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> {
                    GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
                    int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
                    apiAvailability.getErrorDialog(this, resultCode, 9000).show();
                    RxBus.getCacheInstance().removeStickyEvent(RxEvent.NeedUpdateGooglePlayService.class);
                    PreferencesUtils.putLong(JConstant.SHOW_GCM_DIALOG, System.currentTimeMillis());
                }, AppLogger::e);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TreeNode node = BaseApplication.getAppComponent().getTreeHelper().findTreeNodeByName("NewHomeActivity");
        refreshHint(node != null && node.getNodeCount() > 0);
    }

    public void showHomeListFragment() {
        if (vpHomeContent != null)
            vpHomeContent.setCurrentItem(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (subscribe != null && subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
            subscribe = null;
        }
    }

    protected int[] getOverridePendingTransition() {
        Bundle bundle = getIntent().getExtras();
        final int enterAnimId = bundle == null ? -1 : bundle.getInt(KEY_ENTER_ANIM_ID, -1);
        final int exitAnimId = bundle == null ? -1 : bundle.getInt(KEY_EXIT_ANIM_ID, -1);
        return new int[]{enterAnimId == -1 ? R.anim.alpha_in : enterAnimId,
                exitAnimId == -1 ? R.anim.alpha_out : exitAnimId};
    }

    private void initMainContentAdapter() {
        HomeViewAdapter viewAdapter = new HomeViewAdapter(getSupportFragmentManager());

        vpHomeContent.setPagingEnabled(true);
        vpHomeContent.setAdapter(viewAdapter);
        vpHomeContent.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final int id = position == 0 ? R.id.btn_home_list
                        : (position == 1 ? R.id.btn_home_wonderful : R.id.btn_home_mine);
                rgLayoutHomeBottomMenu.check(id);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    final int index = vpHomeContent.getCurrentItem();
                    if (index == 0 || index == 2) {
                        if (RxBus.getCacheInstance().hasObservers())
                            RxBus.getCacheInstance().post(new RxEvent.PageScrolled(index));
                    }
                }
            }
        });
    }

    private void initBottomMenu() {
        rgLayoutHomeBottomMenu.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.btn_home_list:
                    if (vpHomeContent.getCurrentItem() != 0) {
                        vpHomeContent.setCurrentItem(0);
                    }
                    break;
                case R.id.btn_home_wonderful:
                    if (vpHomeContent.getCurrentItem() != 1) {
                        vpHomeContent.setCurrentItem(1);
                    }
                    break;
                case R.id.btn_home_mine:
                    if (vpHomeContent.getCurrentItem() != 2) {
                        vpHomeContent.setCurrentItem(2);
                    }
                    break;
            }
        });
        //自定义的RadioButton,放在RadioGroup中不能被选中
        findViewById(R.id.btn_home_mine)
                .setOnClickListener(v -> {
                    if (vpHomeContent.getCurrentItem() != 2) {
                        vpHomeContent.setCurrentItem(2);
                    }
                });
    }

    @UiThread
    @Override
    public void initView() {
    }

    @Override
    public void needUpdate(@RxEvent.UpdateType int type, String desc, String filePath, int force) {
        getAlertDialogManager().showDialog(this, getString(R.string.UPGRADE), getString(R.string.UPGRADE),
                getString(R.string.OK), (DialogInterface dialog, int which) -> {
                    if (type == RxEvent.UpdateType.GOOGLE_PLAY) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ContextUtils.getContext().getPackageName()));
                        startActivity(intent);
                        return;
                    }
                    /**
                     * 安装apk
                     */
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
                    startActivity(i);
                }, force == 1 ? "" : getString(R.string.CANCEL), null, false);
    }

    @Override
    public void refreshHint(boolean show) {
        AppLogger.d("显示? " + show);
        ((HintRadioButton) findViewById(R.id.btn_home_mine))
                .showRedHint(show);
    }

    @Override
    public void setPresenter(NewHomeActivityContract.Presenter presenter) {
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onActivityReenter(int requestCode, Intent data) {
        super.onActivityReenter(requestCode, data);
        if (onActivityReenterListener != null)
            onActivityReenterListener.onActivityReenter(requestCode, data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initSharedElementCallback() {
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                if (sharedElementCallBackListener != null)
                    sharedElementCallBackListener.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
            }

            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                if (sharedElementCallBackListener != null)
                    sharedElementCallBackListener.onSharedElementCallBack(names, sharedElements);
            }

        });
    }

    //    private SharedElementCallBackListener sharedElementCallBackListener;
    private OnActivityReenterListener onActivityReenterListener;

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
    }

    /**
     * 主页的三个页面
     */
    class HomeViewAdapter extends FragmentPagerAdapter {
        private static final int INDEX_0 = 0;
        private static final int INDEX_1 = 1;
        private static final int INDEX_2 = 2;

        public HomeViewAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case INDEX_0: {
                    HomePageListFragmentExt fragment = HomePageListFragmentExt.newInstance(new Bundle());

                    if (fragment != null && fragment.getContext() != null)
                        Toast.makeText(fragment.getContext(), "重新new了。。。1", Toast.LENGTH_SHORT).show();
                    return fragment;
                }
                case INDEX_1: {
                    final int bottomMenuContainerId = R.id.fLayout_new_home_bottom_menu;
                    Bundle bundle = new Bundle();
                    bundle.putInt(JConstant.KEY_NEW_HOME_ACTIVITY_BOTTOM_MENU_CONTAINER_ID,
                            bottomMenuContainerId);
                    HomeWonderfulFragmentExt fragment = HomeWonderfulFragmentExt.newInstance(bundle);
                    sharedElementCallBackListener = fragment;
                    onActivityReenterListener = fragment;

                    if (fragment != null && fragment.getContext() != null)
                        Toast.makeText(fragment.getContext(), "重新new了。。。2", Toast.LENGTH_SHORT).show();
                    return fragment;
                }
                case INDEX_2:
                    HomeMineFragment fragment = HomeMineFragment.newInstance(new Bundle());
                    if (fragment != null && fragment.getContext() != null)
                        Toast.makeText(fragment.getContext(), "重新new了。。。3", Toast.LENGTH_SHORT).show();
                    return fragment;
            }
            return HomePageListFragmentExt.newInstance(new Bundle());
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //super.destroyItem(container, position, object);
            //复写这个函数,以免回收fragment.
        }

    }

    public void setOnBackPreseedListener() {

    }
}