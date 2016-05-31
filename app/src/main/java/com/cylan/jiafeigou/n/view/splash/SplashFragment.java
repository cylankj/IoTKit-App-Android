package com.cylan.jiafeigou.n.view.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.splash.SplashContract;
import com.cylan.jiafeigou.n.view.adapter.SimpleFregmentAdapter;
import com.cylan.viewindicator.CirclePageIndicator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by hunt on 16-5-14.
 */
public class SplashFragment extends Fragment implements SplashContract.View {

    @BindView(R.id.vp_splash_content)
    ViewPager vpSplashContent;
    @BindView(R.id.v_indicator)
    CirclePageIndicator vIndicator;
    private List<Fragment> fragmentList;

    public static SplashFragment newInstance(Bundle bundle) {
        SplashFragment fragment = new SplashFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * @param fragments : 提供接口,填充fragment.
     */
    public void setSubFragments(List<Fragment> fragments) {
        this.fragmentList = fragments;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_splash_layout, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vpSplashContent.setAdapter(new SimpleFregmentAdapter(getChildFragmentManager(), fragmentList));
        vIndicator.setViewPager(vpSplashContent);
        vIndicator.setOnPageChangeListener(new SimpleChangeListener() {
            @Override
            public void onPageSelected(int position) {

            }
        });
    }

    @Override
    public void setPresenter(SplashContract.Presenter presenter) {

    }


    private static class SimpleChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }
}



