package com.cylan.jiafeigou.view.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.entity.msg.MsgData;
import com.cylan.jiafeigou.presenter.ShowPicPresenter;
import com.cylan.jiafeigou.presenter.compl.ShowPicPresenterImpl;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.view.ShowBigPicView;
import com.cylan.photoview.PhotoView;
import com.cylan.support.DswLog;
import com.cylan.utils.ListUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import support.uil.core.ImageLoader;

public class ShowBigPicActivity extends Activity implements ShowBigPicView {


    @BindView(R.id.imgvBack)
    ImageView imgvBack;
    @BindView(R.id.tvCursor)
    TextView tvCursor;
    @BindView(R.id.rLayoutTitleBar)
    RelativeLayout rLayoutTitleBar;
    @BindView(R.id.vpContent)
    ViewPager vpContent;
    @BindView(R.id.tvTime)
    TextView tvTime;
    @BindView(R.id.imgvDownload)
    ImageView imgvDownload;
    @BindView(R.id.imgvShare)
    ImageView imgvShare;
    @BindView(R.id.imgvMore)
    ImageView imgvMore;
    ShowPicPresenter showPicPresenter;

    private MsgData dataInfo;
    private int currentIndex = 0;
    public static final String TIME = "time";
    public static final String INDEX = "index";
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_messagedetail_pics);
        ButterKnife.bind(this);
        dataInfo = (MsgData) getIntent().getSerializableExtra(TIME);
        currentIndex = getIntent().getIntExtra(INDEX, 0);
        if (dataInfo == null) {
            DswLog.ex("data info is null ..");
            finish();
        }
        showPicPresenter = new ShowPicPresenterImpl(this);
    }

    @OnClick(R.id.imgvShare)
    public void onShare() {
        showPicPresenter.share();
    }

    @OnClick(R.id.imgvDownload)
    public void onDownload() {
        final int count = ListUtils.getSize(dataInfo.urllist);
        if (currentIndex >= count) {
            ToastUtil.showFailToast(this, "saved failed");
            return;
        }
        showPicPresenter.download(dataInfo.urllist.get(currentIndex));
    }

    @OnClick(R.id.imgvBack)
    public void onBack() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void shareFinish() {

    }

    @Override
    public void downloadFinish(final int state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.showSuccessToast(getContext(), getString(R.string.SAVED_PHOTOS));
            }
        });
    }


    @Override
    public void initView() {
        SamplePagerAdapter samplePagerAdapter = new SamplePagerAdapter(dataInfo);
        vpContent.setAdapter(samplePagerAdapter);
        vpContent.setCurrentItem(currentIndex);
        vpContent.addOnPageChangeListener(new OnPageChangeListeners() {
            @Override
            public void onPageSelected(int position) {
                currentIndex = position;
                showPicDate(position);
            }
        });
        showPicDate(currentIndex);
    }

    private void showPicDate(final int position) {
        final int count = ListUtils.getSize(dataInfo.urllist);
        if (count == 0 || position >= count)
            return;
        tvTime.setText(mSimpleDateFormat.format(new Date(dataInfo.time * 1000)));
    }

    @Override
    public Context getContext() {
        return this;
    }

    private static class SamplePagerAdapter extends PagerAdapter {

        MsgData msgData;

        public SamplePagerAdapter(MsgData msgData) {
            this.msgData = msgData;
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(msgData.urllist);
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            ImageLoader.getInstance().displayImage(msgData.urllist.get(position), photoView);
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    private static class OnPageChangeListeners implements ViewPager.OnPageChangeListener {
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
