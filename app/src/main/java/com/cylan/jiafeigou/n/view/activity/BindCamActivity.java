package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.n.view.bind.BindGuideActivity;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BindCamActivity extends BaseBindActivity {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;

    @BindView(R.id.tv_main_content)
    TextView tvMainContent;
    @BindView(R.id.tv_sub_title)
    TextView tvSubTitle;
    @BindView(R.id.tv_bind_camera_tip)
    TextView tvBindCameraTip;
    @BindView(R.id.imv_anima)
    ImageView imvAnima;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_cam);
        ButterKnife.bind(this);
        customToolbar.setBackAction(v -> finishExt());
        tvMainContent.setText(getIntent().getStringExtra(JConstant.KEY_ANIM_TITLE));
        tvSubTitle.setText(getIntent().getStringExtra(JConstant.KEY_ANIM_SUB_TITLE));
        tvBindCameraTip.setText(getIntent().getStringExtra(JConstant.KEY_NEXT_STEP));
        GlideApp.with(this).load(R.raw.add_cam).into(imvAnima);
    }

    @Override
    protected int[] getOverridePendingTransition() {
        return new int[]{R.anim.slide_in_right, R.anim.slide_out_left};
    }


    @OnClick(R.id.tv_bind_camera_tip)
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        Intent intent = getIntent();//需要一路传下去.
        intent.setClass(this, BindGuideActivity.class);
        intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.DOG_CAMERA_NAME));
        intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.dog_doby);
        intent.putExtra(JConstant.KEY_COMPONENT_NAME, this.getClass().getName());
        startActivity(intent);
    }

}
