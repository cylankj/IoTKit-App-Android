package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bind.BindGuideActivity;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BindAnimationActivity extends BaseBindActivity {

    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.tv_main_content)
    TextView tvMainContent;
    @BindView(R.id.tv_sub_title)
    TextView tvSubTitle;
    @BindView(R.id.imv_gif_container)
    ImageView imvGifContainer;
    @BindView(R.id.tv_next_step)
    TextView tvNextStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_animation);
        ButterKnife.bind(this);
        int gifId = getIntent().getIntExtra(JConstant.KEY_ANIM_GIF, -1);
        GlideDrawableImageViewTarget imageViewTarget =
                new GlideDrawableImageViewTarget(imvGifContainer);
        Glide.with(this).load(gifId).into(imageViewTarget);
        customToolbar.setBackAction(v -> finishExt());
        tvMainContent.setText(getIntent().getStringExtra(JConstant.KEY_ANIM_TITLE));
        tvSubTitle.setText(getIntent().getStringExtra(JConstant.KEY_ANIM_SUB_TITLE));
        tvNextStep.setText(getIntent().getStringExtra(JConstant.KEY_NEXT_STEP));
    }

    @Override
    protected int[] getOverridePendingTransition() {
        return new int[]{R.anim.slide_in_right, R.anim.slide_out_left};
    }

    @OnClick(R.id.tv_next_step)
    public void onClick(View view) {
        ViewUtils.deBounceClick(view);
        Intent intent = getIntent();//需要一路传下去.
        intent.setClass(this, BindGuideActivity.class);
        intent.putExtra(JConstant.KEY_COMPONENT_NAME, this.getClass().getName());
        startActivity(intent);
    }
}
