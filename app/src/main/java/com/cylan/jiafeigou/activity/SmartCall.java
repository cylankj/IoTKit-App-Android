
package com.cylan.jiafeigou.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.activity.login.LoginActivity;
import com.cylan.jiafeigou.activity.login.RegisterByEmail;
import com.cylan.jiafeigou.activity.login.RegisterByPhone;
import com.cylan.jiafeigou.adapter.MyPagerAdapter;
import com.cylan.jiafeigou.base.MyApp;
import com.cylan.jiafeigou.engine.ClientConstants;
import com.cylan.jiafeigou.utils.AppManager;
import com.cylan.jiafeigou.utils.BitmapUtil;
import com.cylan.jiafeigou.utils.DensityUtil;
import com.cylan.jiafeigou.utils.MyImageLoader;
import com.cylan.jiafeigou.utils.PreferenceUtil;
import com.cylan.jiafeigou.utils.Utils;
import com.cylan.publicApi.Config;
import com.cylan.publicApi.Constants;
import com.cylan.publicApi.DswLog;
import com.cylan.publicApi.JniPlay;

import java.util.ArrayList;
import java.util.List;

public class SmartCall extends Activity implements OnPageChangeListener, OnClickListener {

    private ImageView mOriginalView;
    private ImageView mLogoView;
    private int[] imgs = null;
    private List<View> mList;
    private ImageView[] mCursorList;

    private int clickTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_smartcall);
        mOriginalView = (ImageView) findViewById(R.id.original_pic);
        ImageView mMaxBlurView = (ImageView) findViewById(R.id.maxbiur_pic);
        mLogoView = (ImageView) findViewById(R.id.welcome_logo);
        mLogoView.setOnClickListener(this);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.welcome_viewpager);
        mViewPager.setOnPageChangeListener(this);
        LinearLayout mCusorLayout = (LinearLayout) findViewById(R.id.cusor_layout);
        Button mLoginBtn = (Button) findViewById(R.id.login);
        Button mRegisterBtn = (Button) findViewById(R.id.register);

        try {
            if (PreferenceUtil.needShowGuide(this)) {
                MyImageLoader.loadWelcomeBitmap(R.drawable.bg_welcome, mOriginalView);
                mMaxBlurView.setImageBitmap(BitmapUtil.readBitMap(this, R.drawable.bg_welcome_dark, DensityUtil.getScreenWidth(this), DensityUtil.getScreenHeight(this)));
            } else {
                mOriginalView.setImageBitmap(BitmapUtil.readBitMap(this, R.drawable.bg_welcome, DensityUtil.getScreenWidth(this), DensityUtil.getScreenHeight(this)));
                MyImageLoader.loadWelcomeBitmap(R.drawable.bg_welcome_dark, mMaxBlurView);
            }


            if (imgs == null) {
                imgs = new int[]{0, R.drawable.guide1_bg, R.drawable.guide2_bg, R.drawable.guide3_bg};
            }
            mList = new ArrayList<>();

            mCursorList = new ImageView[4];

            for (int i = 0; i < imgs.length; i++) {
                ImageView img = new ImageView(this);
                img.setScaleType(ScaleType.FIT_CENTER);
                if (imgs[i] == 0) {
                    img.setImageResource(imgs[i]);
                } else {
                    if (i == 1 && PreferenceUtil.needShowGuide(this)) {
                        img.setImageBitmap(BitmapUtil.readBitMap(this, imgs[i], DensityUtil.getScreenWidth(this), DensityUtil.getScreenHeight(this)));
                    } else {
                        MyImageLoader.loadWelcomeBitmap(imgs[i], img);
                    }
                }

                LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                img.setLayoutParams(params);
                mList.add(img);


                ImageView imageView = new ImageView(this);
                imageView.setScaleType(ScaleType.FIT_XY);
                mCursorList[i] = imageView;

                if (i == 0) {
                    // 默认选中第一张图片
                    mCursorList[i].setImageResource(R.drawable.cursor_posint_enter);
                } else {
                    mCursorList[i].setImageResource(R.drawable.cursor_posint_default);
                }

                mCusorLayout.addView(mCursorList[i]);
                MarginLayoutParams pa = (MarginLayoutParams) imageView.getLayoutParams();
                pa.width = 16;
                pa.height = 16;
                pa.setMargins(10, 0, 10, 0);
                imageView.setLayoutParams(pa);

            }
            if (mList != null) {
                MyPagerAdapter mAdapter = new MyPagerAdapter(mList);
                mViewPager.setAdapter(mAdapter);
                mLoginBtn.setOnClickListener(this);
                mRegisterBtn.setOnClickListener(this);
                if (PreferenceUtil.needShowGuide(this)) {
                    PreferenceUtil.needShowGuide(this, false);
                    mViewPager.setCurrentItem(1);
                    mOriginalView.setAlpha(1.0f);
                    mLogoView.setAlpha(1.0f);
                    mOriginalView.setVisibility(View.GONE);
                    mLogoView.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            DswLog.ex(e.toString());
        }


    }

    @Override
    protected void onDestroy() {
        AppManager.getAppManager().finishActivity(this);
        super.onDestroy();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        if (arg0 == 0) {
            if (mOriginalView.getVisibility() == View.GONE) {
                mOriginalView.setVisibility(View.VISIBLE);
                mLogoView.setVisibility(View.VISIBLE);
            }
            mOriginalView.setAlpha(1.0f - arg1);
            mLogoView.setAlpha(1.0f - arg1);
        } else {
            if (mOriginalView.getVisibility() == View.VISIBLE) {
                mOriginalView.setVisibility(View.GONE);
                mLogoView.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onPageSelected(int i) {
        for (int j = 0; j < mCursorList.length; j++) {
            if (mCursorList[j] != null) {
                if (i == j) {
                    mCursorList[j].setImageResource(R.drawable.cursor_posint_enter);
                } else {
                    mCursorList[j].setImageResource(R.drawable.cursor_posint_default);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                startActivity(new Intent(SmartCall.this, LoginActivity.class));
                break;
            case R.id.register:
                Class cla = null;
                if (Utils.getLanguageType(this) == Constants.LANGUAGE_TYPE_CHINESE)
                    cla = RegisterByPhone.class;
                else
                    cla = RegisterByEmail.class;
                startActivity(new Intent(SmartCall.this, cla));

                break;
            case R.id.welcome_logo:
                if (!Config.ADDR.equals(ClientConstants.YUN_ADDR)) {
                    if (clickTimes == 9) {
                        showDialog();
                        return;
                    }
                    clickTimes++;
                }
                break;
            default:
                break;
        }

    }

    private void showDialog() {

        final View DialogView = LayoutInflater
                .from(SmartCall.this).inflate(
                        R.layout.dialog_change_ipport, null);
        final EditText addr = (EditText) DialogView.findViewById(R.id.addr);
        addr.setText(PreferenceUtil.getIP(this));
        final EditText port = (EditText) DialogView.findViewById(R.id.port);
        port.setText(String.valueOf(PreferenceUtil.getPort(this)));
        AlertDialog.Builder builder = new AlertDialog.Builder(
                SmartCall.this);
        builder.setTitle("请输入");
        builder.setView(DialogView);
        builder.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (addr.getText().toString().isEmpty() || port.getText().toString().isEmpty()) {
                            return;
                        }

                        dialog.dismiss();
                        PreferenceUtil.setIP(SmartCall.this, addr.getText().toString());
                        PreferenceUtil.setPort(SmartCall.this, Integer.parseInt(port.getText().toString()));
                        MyApp.logout(SmartCall.this);
                        JniPlay.DisconnectFromServer();
                        MyApp.startActivityToSmartCall(SmartCall.this);

                    }
                });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dlg = builder.create();
        dlg.show();
    }

}
