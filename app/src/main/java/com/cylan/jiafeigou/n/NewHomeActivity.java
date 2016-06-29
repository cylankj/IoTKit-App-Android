package com.cylan.jiafeigou.n;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.home.NewHomeActivityContract;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeMinePresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.home.HomePageListPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.home.HomeWonderfulPresenterImpl;
import com.cylan.jiafeigou.n.mvp.impl.home.NewHomeActivityPresenterImpl;
import com.cylan.jiafeigou.n.view.home.HomeMineFragment;
import com.cylan.jiafeigou.n.view.home.HomePageListFragment;
import com.cylan.jiafeigou.n.view.home.HomeWonderfulFragment;
import com.cylan.jiafeigou.widget.CustomViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewHomeActivity extends BaseFullScreenFragmentActivity implements
        NewHomeActivityContract.View {
    @BindView(R.id.vp_home_content)
    CustomViewPager vpHomeContent;
    @BindView(R.id.rgLayout_home_bottom_menu)
    RadioGroup rgLayoutHomeBottomMenu;

    private HomeViewAdapter viewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home);
        ButterKnife.bind(this);
        initBottomMenu();
        initMainContentAdapter();
        new NewHomeActivityPresenterImpl(this);
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();
    }


    private void initMainContentAdapter() {
        viewAdapter = new HomeViewAdapter(getSupportFragmentManager());
        vpHomeContent.setPagingEnabled(true);
        vpHomeContent.setAdapter(viewAdapter);
        vpHomeContent.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final int id = position == 0 ? R.id.btn_home_list
                        : (position == 1 ? R.id.btn_home_wonderful : R.id.btn_home_mine);
                rgLayoutHomeBottomMenu.check(id);
            }
        });
    }

    private void initBottomMenu() {
        rgLayoutHomeBottomMenu.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.btn_home_list:
                        vpHomeContent.setCurrentItem(0);
                        break;
                    case R.id.btn_home_wonderful:
                        vpHomeContent.setCurrentItem(1);
                        break;
                    case R.id.btn_home_mine:
                        vpHomeContent.setCurrentItem(2);
                        break;
                }
            }
        });
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
                HomeWonderfulFragment fragment = HomeWonderfulFragment.newInstance(new Bundle());
                new HomeWonderfulPresenterImpl(fragment);

                return fragment;
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

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);
        //复写这个韩函数,以免回收fragment.
    }
}
