package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineLookBigImageContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineLookBigImagePresenterImp;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineLookBigImageFragment extends Fragment implements MineLookBigImageContract.View {

    @BindView(R.id.iv_look_big_image)
    ImageView ivLookBigImage;

    private MineLookBigImageContract.Presenter presenter;
    private static boolean loadResult = false;
    private String imageUrl;
    private static Bitmap bitmapSource;

    public static MineLookBigImageFragment newInstance(Bundle bundle) {
        MineLookBigImageFragment fragment = new MineLookBigImageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        imageUrl = arguments.getString("imageUrl");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_look_big_image, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initImageViewSize();
        initLongClickListener();
        return view;
    }

    private void initPresenter() {
        presenter = new MineLookBigImagePresenterImp(this);
    }

    private void initLongClickListener() {
        ivLookBigImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showSaveImageDialog();
                return false;
            }
        });
    }

    /**
     * 初始化大图大小
     */
    private void initImageViewSize() {
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ivLookBigImage.getLayoutParams());
        lp.height = (int) (height * 0.47);
        lp.setMargins(0, (int) (height * 0.23), 0, 0);
        ivLookBigImage.setLayoutParams(lp);
    }

    /**
     * desc:保存图片
     */
    private void showSaveImageDialog() {
        AlertDialogManager.getInstance().showDialog(getActivity(), "ave", getString(R.string.Tap3_SavePic),
                getString(R.string.Tap3_SavePic), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                    ToastUtil.showToast(getString(R.string.SAVED_PHOTOS));
                    if (presenter != null) {
                        presenter.saveImage(bitmapSource);
                    }
                }, getString(R.string.CANCEL), null, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadImage();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.stop();
    }

    /**
     * desc:加载图片
     */
    private void loadImage() {
        myViewTarget = new MyViewTarget(ivLookBigImage, ContextUtils.getContext(), getActivity().getSupportFragmentManager());
        showLoadImageProgress();
        Glide.with(getContext())
                .load(imageUrl)
                .asBitmap()
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(myViewTarget);
    }

    private MyViewTarget myViewTarget;

    private static class MyViewTarget extends BitmapImageViewTarget {
        private final WeakReference<ImageView> image;
        private final WeakReference<Context> mContext;
        private final WeakReference<FragmentManager> fragmentManager;

        public MyViewTarget(ImageView view, Context context, FragmentManager manager) {
            super(view);
            image = new WeakReference<ImageView>(view);
            mContext = new WeakReference<Context>(context);
            fragmentManager = new WeakReference<FragmentManager>(manager);

        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            super.onLoadStarted(placeholder);
        }

        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
            super.onResourceReady(resource, glideAnimation);
            bitmapSource = resource;
            LoadingDialog.dismissLoading(fragmentManager.get());
            loadResult = true;
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            super.onLoadFailed(e, errorDrawable);
            loadResult = false;
            LoadingDialog.dismissLoading(fragmentManager.get());
//            ToastUtil.showNegativeToast(getString(R.string.Item_LoadFail));
        }
    }

    @Override
    public void setPresenter(MineLookBigImageContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    @OnClick({R.id.iv_look_big_image})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_look_big_image:                //点击大图退出全屏
                if (loadResult) {
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    loadImage();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ivLookBigImage.setImageBitmap(null);
        if (bitmapSource != null && bitmapSource.isRecycled()) {
            bitmapSource.recycle();
            bitmapSource = null;
        }
    }

    public void showLoadImageProgress() {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.LOADING));
    }

    public void hideLoadImageProgress() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }
}
