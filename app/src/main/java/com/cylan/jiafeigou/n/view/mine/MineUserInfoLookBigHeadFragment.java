package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineUserInfoLookBigHeadContract;
import com.cylan.jiafeigou.support.photoview.PhotoView;
import com.cylan.jiafeigou.support.photoview.PhotoViewAttacher;
import com.cylan.jiafeigou.widget.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineUserInfoLookBigHeadFragment extends Fragment implements MineUserInfoLookBigHeadContract.View {

    @BindView(R.id.iv_userinfo_big_image)
    PhotoView ivUserinfoBigImage;
    @BindView(R.id.rl_root_view)
    RelativeLayout rlRootView;

    private static boolean loadResult = false;
    private String iamgeUrl;

    public static MineUserInfoLookBigHeadFragment newInstance(Bundle bundle) {
        MineUserInfoLookBigHeadFragment fragment = new MineUserInfoLookBigHeadFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_userinfo_lookbigimagehead, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        initImageViewSize();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBigImage(iamgeUrl);
    }

    /**
     * 初始化大图大小
     */
    private void initImageViewSize() {
//        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
//        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ivUserinfoBigImage.getLayoutParams());
//        lp.height = (int) (height * 0.47);
//        lp.setMargins(0, (int) (height * 0.23), 0, 0);
//        ivUserinfoBigImage.setLayoutParams(lp);
        ivUserinfoBigImage.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onOutsidePhotoTap() {

            }
        });
    }

    /**
     * 获取传递过来的参数
     */
    private void getArgumentData() {
        Bundle arguments = getArguments();
        iamgeUrl = arguments.getString("imageUrl");
    }

    private void loadBigImage(String url) {
        if (TextUtils.isEmpty(url) || getContext() == null) {
            return;
        }
        showLoadImageProgress();
        Glide.with(getContext())
                .load(url)
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        hideLoadImageProgress();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        hideLoadImageProgress();
                        return false;
                    }
                })
                .into(ivUserinfoBigImage);
    }

    @OnClick({R.id.rl_root_view, R.id.iv_userinfo_big_image})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_root_view:
                if (loadResult) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                break;
            case R.id.iv_userinfo_big_image:
                if (!loadResult) {
                    loadBigImage(iamgeUrl);
                } else {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
                break;
        }
    }


    @Override
    public void showLoadImageProgress() {
        ivUserinfoBigImage.post(() -> LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.LOADING)));
    }

    @Override
    public void hideLoadImageProgress() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    @Override
    public void setPresenter(MineUserInfoLookBigHeadContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }
}
