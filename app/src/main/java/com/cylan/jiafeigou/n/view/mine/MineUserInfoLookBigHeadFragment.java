package com.cylan.jiafeigou.n.view.mine;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineUserInfoLookBigHeadContract;
import com.cylan.jiafeigou.support.photoview.PhotoView;
import com.cylan.jiafeigou.support.photoview.PhotoViewAttacher;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.widget.LoadingDialog.dismissLoading;

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
        loadBigImage(iamgeUrl);
        return view;
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
                getFragmentManager().popBackStack();
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
        MyReqListener listener = new MyReqListener(getString(R.string.Item_LoadFail), getFragmentManager());
        Glide.with(getContext())
                .load(url)
                .asBitmap()
                .fitCenter()
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .listener(listener)
                .into(ivUserinfoBigImage);
    }


    private static class MyReqListener implements RequestListener<String, Bitmap> {
        private String totas;
        private WeakReference<FragmentManager> managerWeakReference;

        public MyReqListener(String totas, FragmentManager manager) {
            this.totas = totas;
            this.managerWeakReference = new WeakReference<>(manager);
        }

        @Override
        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
            if (managerWeakReference.get() == null) return false;

            LoadingDialog.dismissLoading(managerWeakReference.get());
            loadResult = false;
            ToastUtil.showNegativeToast(totas);
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
            if (managerWeakReference.get() == null) return false;
            loadResult = true;
            dismissLoading(managerWeakReference.get());
            return false;
        }
    }


    @OnClick({R.id.rl_root_view, R.id.iv_userinfo_big_image})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_root_view:
                if (loadResult) {
                    getFragmentManager().popBackStack();
                }
                break;
            case R.id.iv_userinfo_big_image:
                if (!loadResult) {
                    loadBigImage(iamgeUrl);
                } else {
                    getFragmentManager().popBackStack();
                }
                break;
        }
//        if (loadResult) {
//            getFragmentManager().popBackStack();
//        } else {
//            loadBigImage(iamgeUrl);
//        }
    }


    @Override
    public void showLoadImageProgress() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
    }

    @Override
    public void hideLoadImageProgress() {
        dismissLoading(getFragmentManager());
    }

    @Override
    public void setPresenter(MineUserInfoLookBigHeadContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }
}
