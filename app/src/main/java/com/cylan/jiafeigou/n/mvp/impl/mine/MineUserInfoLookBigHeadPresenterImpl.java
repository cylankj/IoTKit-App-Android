package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.widget.ImageView;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineUserInfoLookBigHeadContract;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineUserInfoLookBigHeadPresenterIMpl implements MineUserInfoLookBigHeadContract.Presenter {

    private MineUserInfoLookBigHeadContract.View view;
    private Context context;

    public MineUserInfoLookBigHeadPresenterIMpl(MineUserInfoLookBigHeadContract.View view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void loadImage(ImageView imageView) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
