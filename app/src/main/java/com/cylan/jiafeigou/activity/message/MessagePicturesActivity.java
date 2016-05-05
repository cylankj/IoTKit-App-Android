package com.cylan.jiafeigou.activity.message;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.adapter.MyPagerAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.entity.msg.MsgData;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.ThreadPoolUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.TouchImageView;
import com.cylan.jiafeigou.worker.SaveShotPhotoRunnable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessagePicturesActivity extends BaseActivity implements OnClickListener {

    protected static final String PIC_CID = "cid";
    protected static final String PIC_LIST = "list";
    protected static final String PIC_INDEX = "index";
    public static final String TIME = "time";
    private ImageView mBackView;
    private TextView mCursorView;
    private TextView mTimeView;
    private ViewPager mViewPager;
    private ImageView mDownLoadView;
    private ImageView mShareView;
    private List<View> views;


    private int index;
    private MsgData info;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SaveShotPhotoRunnable.SaveShotPhoto ssp = (SaveShotPhotoRunnable.SaveShotPhoto) msg.obj;
            ToastUtil.showSuccessToast(MessagePicturesActivity.this, getString(R.string.SAVED_PHOTOS));
            Utils.sendBroad2System(MessagePicturesActivity.this, ssp.mPath);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messagedetail_pics);
        setBaseTitlebarVisbitly(false);
        setContainerBackgroudNull();

        info = (MsgData) getIntent().getSerializableExtra(TIME);
        index = getIntent().getIntExtra("index", 0);
        initView();

    }

    private void initView() {
        mBackView = (ImageView) findViewById(R.id.imgvBack);
        mBackView.setOnClickListener(this);
        mCursorView = (TextView) findViewById(R.id.tvCursor);
        mTimeView = (TextView) findViewById(R.id.tvTime);
        mViewPager = (ViewPager) findViewById(R.id.vpContent);
        mTimeView.setText(mSimpleDateFormat.format(new Date(info.time * 1000)));
        mCursorView.setText(mSimpleDateFormat.format(new Date(info.time * 1000)));
        mDownLoadView = (ImageView) findViewById(R.id.imgvDownload);
        mDownLoadView.setOnClickListener(this);
        mShareView = (ImageView) findViewById(R.id.imgvShare);
        mShareView.setOnClickListener(this);
        mShareView.setEnabled(false);
        setBtnEnable(true);


        views = new ArrayList<>();
        for (int i = 0; i < info.urllist.size(); i++) {
            TouchImageView mImageView = new TouchImageView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            mImageView.setLayoutParams(params);
            mImageView.setTag(i);
            views.add(mImageView);
        }
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(views);
        mViewPager.setAdapter(myPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                MyImageLoader.loadImageFromNet(info.urllist.get(i), (TouchImageView) views.get(i));
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mViewPager.setCurrentItem(index);
        MyImageLoader.loadImageFromNet(info.urllist.get(index), (TouchImageView) views.get(index));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgvBack:
                onBackPressed();
                break;
            case R.id.imgvDownload:
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                Drawable mDrawable = ((TouchImageView) views.get(mViewPager.getCurrentItem())).getDrawable();
                Bitmap mBitmap = BitmapUtil.drawableToBitmap(mDrawable);
                String path = PathGetter.getJiaFeiGouPhotos() + df.format(new Date()) + ".png";
                ThreadPoolUtils.execute(new SaveShotPhotoRunnable(mBitmap, path, mHandler, 0xff));
                break;
            case R.id.imgvShare:
                break;

            default:
                break;
        }

    }

    private void setBtnEnable(Boolean is) {
        mDownLoadView.setEnabled(is);
    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}