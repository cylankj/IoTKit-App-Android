package com.cylan.jiafeigou.n.view.media;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.HackyViewPager;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_SHARED_ELEMENT_LIST;

public class CamMediaActivity extends BaseFullScreenFragmentActivity {

    public static final String KEY_BUNDLE = "key_bundle";
    public static final String KEY_TIME = "key_time";
    public static final String KEY_INDEX = "key_index";
    @BindView(R.id.vp_container)
    HackyViewPager vpContainer;
    @BindView(R.id.tv_big_pic_title)
    TextView tvBigPicTitle;
    @BindView(R.id.fLayout_details_title)
    FrameLayout fLayoutBigPicTitle;

    private long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_media);
        ButterKnife.bind(this);
        ArrayList<String> contents = getIntent().getStringArrayListExtra(KEY_BUNDLE);
        CustomAdapter customAdapter = new CustomAdapter(getSupportFragmentManager());
        customAdapter.setContents(contents);
        this.time = getIntent().getLongExtra(KEY_TIME, 0);
        vpContainer.setAdapter(customAdapter);
        vpContainer.setCurrentItem(getIntent().getIntExtra(KEY_INDEX, 0));
        ViewUtils.setViewMarginStatusBar(fLayoutBigPicTitle);
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
            R.id.imgV_big_pic_collect
            , R.id.tv_big_pic_close})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_big_pic_download:
                break;
            case R.id.imgV_big_pic_share:
                break;
            case R.id.imgV_big_pic_collect:
                break;
            case R.id.tv_big_pic_close:
                finish();
                break;
        }
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        private ArrayList<String> contents;

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
            return BigPicFragment.newInstance(bundle);
        }

        @Override
        public int getCount() {
            return this.contents.size();
        }
    }
}
