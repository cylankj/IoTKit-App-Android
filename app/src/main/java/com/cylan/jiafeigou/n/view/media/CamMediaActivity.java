package com.cylan.jiafeigou.n.view.media;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.HackyViewPager;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMediaContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamMediaPresenterImpl;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_SHARED_ELEMENT_LIST;

public class CamMediaActivity extends BaseFullScreenFragmentActivity<CamMediaContract.Presenter> implements
        CamMediaContract.View {

    public static final String KEY_BUNDLE = "key_bundle";
    public static final String KEY_TIME = "key_time";
    public static final String KEY_INDEX = "key_index";
    @BindView(R.id.vp_container)
    HackyViewPager vpContainer;
    @BindView(R.id.tv_big_pic_title)
    TextView tvBigPicTitle;
    @BindView(R.id.fLayout_details_title)
    FrameLayout fLayoutBigPicTitle;
    @BindView(R.id.fLayout_cam_handle_bar)
    FrameLayout fLayoutCamHandleBar;

    private long time;
    private ArrayList<String> urlList;
    private int currentIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_media);
        ButterKnife.bind(this);
        basePresenter = new CamMediaPresenterImpl(this);
        urlList = getIntent().getStringArrayListExtra(KEY_BUNDLE);
        CustomAdapter customAdapter = new CustomAdapter(getSupportFragmentManager());
        customAdapter.setContents(urlList);
        this.time = getIntent().getLongExtra(KEY_TIME, 0);
        vpContainer.setAdapter(customAdapter);
        vpContainer.setCurrentItem(currentIndex = getIntent().getIntExtra(KEY_INDEX, 0));
        ViewUtils.setViewMarginStatusBar(fLayoutBigPicTitle);
        customAdapter.setCallback(() -> {
            AnimatorUtils.slideAuto(fLayoutBigPicTitle, true);
            AnimatorUtils.slideAuto(fLayoutCamHandleBar, false);
        });
        vpContainer.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvBigPicTitle.setText(TimeUtils.getMediaPicTimeInString(time));
    }

    @OnClick({R.id.imgV_big_pic_download,
            R.id.imgV_big_pic_share,
            R.id.imgV_big_pic_collect,
            R.id.tv_big_pic_close})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_big_pic_download:
                if (basePresenter != null) basePresenter.saveImage(urlList.get(currentIndex));
                break;
            case R.id.imgV_big_pic_share:
                break;
            case R.id.imgV_big_pic_collect:
                if (basePresenter != null) basePresenter.collect(time);
                break;
            case R.id.tv_big_pic_close:
                finish();
                break;
        }
    }

    @Override
    public void setPresenter(CamMediaContract.Presenter presenter) {
        basePresenter = presenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void savePic(boolean state) {
        if (state) {
            ToastUtil.showPositiveToast(getString(R.string.SAVED_PHOTOS));
        } else ToastUtil.showNegativeToast(getString(R.string.set_failed));
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        private ArrayList<String> contents;
        private BigPicFragment.CallBack callBack;

        public void setContents(ArrayList<String> contents) {
            this.contents = contents;
        }

        public CustomAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_SHARED_ELEMENT_LIST, contents.get(position));
            BigPicFragment fragment = BigPicFragment.newInstance(bundle);
            fragment.setCallBack(this.callBack);
            return fragment;
        }

        @Override
        public int getCount() {
            return this.contents.size();
        }

        private void setCallback(BigPicFragment.CallBack callback) {
            this.callBack = callback;
        }
    }
}
