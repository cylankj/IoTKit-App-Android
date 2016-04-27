package com.cylan.jiafeigou.activity.message;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.support.DswLog;
import com.cylan.jiafeigou.adapter.MyPagerAdapter;
import com.cylan.jiafeigou.base.BaseActivity;
import com.cylan.jiafeigou.entity.msg.MsgData;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.PathGetter;
import com.cylan.jiafeigou.utils.ThreadPoolUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.jiafeigou.widget.MyGridView;
import com.cylan.jiafeigou.widget.TouchImageView;
import com.cylan.jiafeigou.worker.SaveShotPhotoRunnable;

import java.io.File;
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
    private Dialog mShareDlg;
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
        mBackView = (ImageView) findViewById(R.id.back);
        mBackView.setOnClickListener(this);
        mCursorView = (TextView) findViewById(R.id.pic_cusor);
        mTimeView = (TextView) findViewById(R.id.time);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTimeView.setText(mSimpleDateFormat.format(new Date(info.time * 1000)));
        mCursorView.setText(mSimpleDateFormat.format(new Date(info.time * 1000)));
        mDownLoadView = (ImageView) findViewById(R.id.download);
        mDownLoadView.setOnClickListener(this);
        mShareView = (ImageView) findViewById(R.id.share);
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
            case R.id.back:
                onBackPressed();
                break;
            case R.id.download:
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                Drawable mDrawable = ((TouchImageView) views.get(mViewPager.getCurrentItem())).getDrawable();
                Bitmap mBitmap = BitmapUtil.drawableToBitmap(mDrawable);
                String path = PathGetter.getJiaFeiGouPhotos() + df.format(new Date()) + ".png";
                ThreadPoolUtils.execute(new SaveShotPhotoRunnable(mBitmap, path, mHandler, 0xff));
                break;
            case R.id.share:
                break;

            default:
                break;
        }

    }


    void share(String path) {
        if (mShareDlg == null) {
            mShareDlg = new Dialog(this, R.style.func_dialog);
            View content = View.inflate(this, R.layout.dialog_app_share, null);
            TextView cancel = (TextView) content.findViewById(R.id.btn_cancle);
            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mShareDlg.dismiss();
                }
            });
            MyGridView gridView = (MyGridView) content.findViewById(R.id.gridview);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));
            // intent.putExtra(Intent.EXTRA_TEXT,
            // "最近我在用一款手机应用，名字叫加菲狗，这款应用配合一个摄像头可以随时随地看到家里的老人、宝宝和财物，非常好用！推荐你也试试哦~~~"
            // + " http://yun.app8h.com/s?id=vau67n");
            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
            final AppAdater appAdater = new AppAdater(this);
            for (ResolveInfo info : list) {
                appAdater.add(info);
            }
            gridView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ResolveInfo info = appAdater.getItem(position);
                    intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    startActivity(intent);
                }
            });
            gridView.setAdapter(appAdater);
            mShareDlg.setContentView(content);
            mShareDlg.setCanceledOnTouchOutside(true);
        }
        try {
            mShareDlg.show();
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }
    }

    class ViewHolder {
        ImageView icon;
        TextView name;
        ResolveInfo info;
    }

    class AppAdater extends ArrayAdapter<ResolveInfo> {

        public AppAdater(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (null == convertView) {
                convertView = View.inflate(getContext(), R.layout.item_app_share, null);
                vh = new ViewHolder();
                vh.icon = (ImageView) convertView.findViewById(R.id.icon);
                vh.name = (TextView) convertView.findViewById(R.id.name);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            ResolveInfo info = getItem(position);
            PackageManager pm = getPackageManager();
            vh.name.setText(info.loadLabel(pm));
            vh.icon.setImageDrawable(info.loadIcon(pm));
            return convertView;
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