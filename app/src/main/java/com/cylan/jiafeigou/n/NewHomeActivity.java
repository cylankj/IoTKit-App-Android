package com.cylan.jiafeigou.n;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Button;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.NewHomeActivityContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeDiscoveryPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeMinePresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.home.HomePageListPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.home.NewHomeActivityPresenterImpl;
import com.cylan.jiafeigou.n.view.home.HomeDiscoveryFragment;
import com.cylan.jiafeigou.n.view.home.HomeMineFragment;
import com.cylan.jiafeigou.n.view.home.HomePageListFragment;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomViewPager;
import com.cylan.utils.ListUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewHomeActivity extends FragmentActivity implements
        ViewPager.OnPageChangeListener, NewHomeActivityContract.View {
    @BindView(R.id.vp_home_content)
    CustomViewPager vpHomeContent;
    @BindView(R.id.btn_home_list)
    Button btnHomeList;
    @BindView(R.id.btn_home_discovery)
    Button btnHomeDiscover;
    @BindView(R.id.btn_home_mine)
    Button btnHomeMine;

    private HomeViewAdapter viewAdapter;
    Button[] bottomBtn = new Button[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home);
        ButterKnife.bind(this);
        initBottomMenu();
        new NewHomeActivityPresenterImpl(this);
    }

    private void initBottomMenu() {
        viewAdapter = new HomeViewAdapter(getSupportFragmentManager());
        vpHomeContent.setPagingEnabled(false);
        vpHomeContent.setAdapter(viewAdapter);
        vpHomeContent.addOnPageChangeListener(this);
        btnHomeList.setEnabled(false);
        bottomBtn[0] = btnHomeList;
        bottomBtn[1] = btnHomeDiscover;
        bottomBtn[2] = btnHomeMine;
    }


    @OnClick(R.id.btn_home_list)
    public void onClickBtnList() {
        onPageSelected(0);
        vpHomeContent.setCurrentItem(0);
    }

    @OnClick(R.id.btn_home_discovery)
    public void onClickBtnDiscovery() {
        onPageSelected(1);
        vpHomeContent.setCurrentItem(1);
    }

    @OnClick(R.id.btn_home_mine)
    public void onClickBtnMine() {
        onPageSelected(2);
        vpHomeContent.setCurrentItem(2);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //实现逻辑
    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < HomeViewAdapter.TOTAL_COUNT; i++) {
            if (i == position)
                bottomBtn[position].setEnabled(false);
            else bottomBtn[i].setEnabled(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        Log.d("hunt", "state: " + state);
    }

    private static long time = 0;

    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        if (System.currentTimeMillis() - time < 1500) {
            super.onBackPressed();
        } else {
            time = System.currentTimeMillis();
            ToastUtil.showToast(this,
                    String.format(getString(R.string.click_back_again_exit),
                            getString(R.string.app_name)));
        }
    }

    private boolean checkExtraChildFragment() {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> list = fm.getFragments();
        if (ListUtils.isEmpty(list))
            return false;
        for (Fragment frag : list) {
            if (frag != null && frag.isVisible()) {
                FragmentManager childFm = frag.getChildFragmentManager();
                if (childFm != null && childFm.getBackStackEntryCount() > 0) {
                    childFm.popBackStack();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkExtraFragment() {
        final int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        } else return false;
    }

    @UiThread
    @Override
    public void initView() {
    }

    @Override
    public void setPresenter(NewHomeActivityContract.Presenter presenter) {
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}

/**
 * 主页的三个页面
 */
class HomeViewAdapter extends FragmentPagerAdapter {
    private static final int INDEX_0 = 0;
    private static final int INDEX_1 = 1;
    private static final int INDEX_2 = 2;
    public static final int TOTAL_COUNT = 3;

    public HomeViewAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case INDEX_0: {
                HomePageListFragment fragment = HomePageListFragment.newInstance(new Bundle());
                new HomePageListPresenterImpl(fragment);
                return fragment;
            }
            case INDEX_1: {
                HomeDiscoveryFragment fragment = HomeDiscoveryFragment.newInstance(new Bundle());
                new HomeDiscoveryPresenterImpl(fragment);
                return HomeDiscoveryFragment.newInstance(new Bundle());
            }
            case INDEX_2:
                HomeMineFragment fragment = HomeMineFragment.newInstance(new Bundle());
                new HomeMinePresenterImpl(fragment);
                return fragment;
        }
        return HomePageListFragment.newInstance(new Bundle());
    }

    @Override
    public int getCount() {
        return 3;
    }
}
