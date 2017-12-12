package com.cylan.jiafeigou.n.view.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.page.EViewPager;
import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hunt on 16-5-14.
 */
public class GuideFragmentV3_2 extends Fragment {
    @BindView(R.id.v_pager)
    EViewPager viewPager;
    @BindView(R.id.pageIndicatorView)
    PageIndicatorView pageIndicatorView;

    public static GuideFragmentV3_2 newInstance() {
        return new GuideFragmentV3_2();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_guide_view, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SimpleAdapter viewAdapter = new SimpleAdapter(getFragmentManager());
        viewPager.setOffscreenPageLimit(4);
        viewPager.setEnabled(false);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (position == 3) {
                    PreferencesUtils.putBoolean(JConstant.KEY_FRESH, false);
                    /**
                     * 锁住
                     */
                    viewPager.setLocked(true);
                    pageIndicatorView.setVisibility(View.GONE);
                }
            }
        });
        viewPager.setAdapter(viewAdapter);
        pageIndicatorView.setAnimationType(AnimationType.WORM);
        pageIndicatorView.setViewPager(viewPager);
        pageIndicatorView.setCount(3);
        pageIndicatorView.setRadius(getResources().getDimension(R.dimen.y4));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private class SimpleAdapter extends FragmentPagerAdapter {

        public SimpleAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 3) {
                return BeforeLoginFragment.newInstance(null);
            }
            return V.newInstance(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            super.destroyItem(container, position, object);
            //避免回收
        }

    }

    /**
     * 简单显示一张图片而已
     */
    public static class V extends Fragment {
        private static final int[] resArray = {R.drawable.pic_welcome_page_1, R.drawable.pic_welcome_page_2, R.drawable.pic_welcome_page_3};

        public static V newInstance(int index) {
            Bundle bundle = new Bundle();
            bundle.putInt("index", index);
            V v = new V();
            v.setArguments(bundle);
            return v;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return new ImageView(inflater.getContext());
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            int index = getArguments().getInt("index");
            view.setBackground(view.getResources().getDrawable(resArray[index]));
        }
    }
}



