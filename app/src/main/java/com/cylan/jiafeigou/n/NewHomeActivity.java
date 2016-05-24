package com.cylan.jiafeigou.n;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Button;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.base.NewBaseActivity;
import com.cylan.jiafeigou.n.view.home.HomeDiscoveryFragment;
import com.cylan.jiafeigou.n.view.home.HomeMineFragment;
import com.cylan.jiafeigou.n.view.home.HomePageListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewHomeActivity extends NewBaseActivity implements
        ViewPager.OnPageChangeListener {

    @BindView(R.id.vp_home_content)
    ViewPager vpHomeContent;
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
    }

    private void initBottomMenu() {
        viewAdapter = new HomeViewAdapter(getSupportFragmentManager());
        vpHomeContent.setAdapter(viewAdapter);
        btnHomeList.setEnabled(false);
        vpHomeContent.addOnPageChangeListener(this);
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
            case INDEX_0:
                return HomePageListFragment.newInstance(new Bundle());
            case INDEX_1:
                return HomeDiscoveryFragment.newInstance(new Bundle());
            case INDEX_2:
                return HomeMineFragment.newInstance(new Bundle());
        }
        return HomePageListFragment.newInstance(new Bundle());
    }

    @Override
    public int getCount() {
        return 3;
    }
}
