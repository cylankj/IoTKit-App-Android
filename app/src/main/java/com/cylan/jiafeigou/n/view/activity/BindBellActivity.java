package com.cylan.jiafeigou.n.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.bind.BindGuideActivity;
import com.cylan.jiafeigou.widget.CustomToolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class BindBellActivity extends BaseBindActivity {
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;
    @BindView(R.id.imv_anima)
    ImageView imvAnima;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_bell);
        ButterKnife.bind(this);
        customToolbar.setBackAction(v -> finishExt());
        int gifId = getIntent().getIntExtra(JConstant.KEY_ANIM_GIF, -1);
        GlideDrawableImageViewTarget imageViewTarget =
                new GlideDrawableImageViewTarget(imvAnima);
        Glide.with(this).load(R.raw.add_ring).into(imageViewTarget);
    }

    @OnClick(R.id.tv_bind_doorbell_tip)
    public void onClick() {
        Intent intent = getIntent();
        intent.setClass(this, BindGuideActivity.class);
        intent.putExtra(JConstant.KEY_BIND_DEVICE, getString(R.string.Smart_bell_Battery));
        intent.putExtra(JConstant.KEY_COMPONENT_NAME, this.getClass().getName());
        intent.putExtra(JConstant.KEY_CONNECT_AP_GIF, R.raw.dog_doby);
        startActivity(intent);
    }

}
