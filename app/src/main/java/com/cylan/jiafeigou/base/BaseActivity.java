package com.cylan.jiafeigou.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.MyImageLoader;

public class BaseActivity extends RootActivity {


    private RelativeLayout rootLayout;
    private LinearLayout mContainerView;
    private RelativeLayout mTitlebarLayout;
    private ImageView mBackView;
    private TextView mTitleView;
    private TextView mRightView;
    private ImageView mRightImageView;
    private ImageView mRemindView;
    private ViewStub mViewStub;


    @Override
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = this.getLayoutInflater();
        rootLayout = (RelativeLayout) inflater.inflate(R.layout.baseview, null);
        mContainerView = (LinearLayout) rootLayout.findViewById(R.id.StandardView_Abstract);
        View v = inflater.inflate(layoutResID, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 0, 0);
        mContainerView.addView(v, params);
        super.setContentView(rootLayout);
        initItems();
    }

    private void initItems() {
        mTitlebarLayout = (RelativeLayout) rootLayout.findViewById(R.id.titleLayout_Abstract);
        ImageView mTitleBackgroundView = (ImageView) rootLayout.findViewById(R.id.title_background);
        ImageView mTitlebarCover = (ImageView) rootLayout.findViewById(R.id.title_cover);
        mBackView = (ImageView) rootLayout.findViewById(R.id.ico_back);
        mBackView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mTitleView = (TextView) rootLayout.findViewById(R.id.titleview);
        mRightView = (TextView) rootLayout.findViewById(R.id.right_btn);
        mRightImageView = (ImageView) rootLayout.findViewById(R.id.right_ico);
        mRemindView = (ImageView) findViewById(R.id.ico_remind);
        mViewStub = (ViewStub) findViewById(R.id.viewstub);
        mTitlebarCover.getBackground().setAlpha(100);

        MyImageLoader.loadTitlebarImage(this, mTitleBackgroundView);

    }

    public void setTitle(int id) {
        mTitleView.setText(getString(id));
    }


    public void setTitle(String str) {
        mTitleView.setText(str);
    }

    public void setTitleMagin(int px) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTitleView.getLayoutParams();
        params.setMargins(px, 0, px, 0);
        mTitleView.setLayoutParams(params);
    }

    public void setBackBtnOnClickListener(OnClickListener ocl) {
        mBackView.setOnClickListener(ocl);
    }

    public void setBackBtnImageRes(int res) {
        mBackView.setImageResource(res);
    }

    public void setRightBtn(int str, OnClickListener ocl) {
        mRightView.setVisibility(View.VISIBLE);
        mRightView.setText(getString(str));
        mRightView.setOnClickListener(ocl);
    }

    public void setRightBtn(int str) {
        mRightView.setVisibility(View.VISIBLE);
        mRightView.setText(getString(str));
    }


    public void setBaseTitlebarVisbitly(boolean boo) {
        mTitlebarLayout.setVisibility(boo ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void showUseGuideView(int v, boolean visibly) {
        mViewStub.inflate();
        MyImageLoader.loadImageFromDrawable("drawable://" + v, (ImageView) findViewById(R.id.guide));
        findViewById(R.id.guide).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.guide).setVisibility(View.GONE);
            }
        });

    }

    public void setRightImageView(int img, OnClickListener ocl) {
        mRightImageView.setVisibility(View.VISIBLE);
        mRightImageView.setImageResource(img);
        mRightImageView.setOnClickListener(ocl);
    }

    public void setRightImageViewVisibility(boolean boo) {
        if (boo) {
            mRightImageView.setVisibility(View.VISIBLE);
        } else {
            mRightImageView.setVisibility(View.GONE);
        }

    }

    public void setRightRemindVisiblty(boolean boo) {
        if (boo) {
            mRemindView.setVisibility(View.VISIBLE);
        } else {
            mRemindView.setVisibility(View.GONE);
        }
    }


    public void setContainerBackgroudNull() {
        mContainerView.setBackgroundResource(0);
    }

}