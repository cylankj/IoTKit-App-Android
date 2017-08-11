package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.FragmentMineLookBigImageBinding;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineLookBigImageContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineLookBigImagePresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.JFGAccountURL;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.io.File;

import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineLookBigImageFragment extends IBaseFragment implements MineLookBigImageContract.View {
    private MineLookBigImageContract.Presenter presenter;
    private FriendContextItem friendContextItem;
    private FragmentMineLookBigImageBinding bigImageBinding;
    private boolean success = false;

    public static MineLookBigImageFragment newInstance(Bundle bundle) {
        MineLookBigImageFragment fragment = new MineLookBigImageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        friendContextItem = arguments.getParcelable("friendItem");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        bigImageBinding = FragmentMineLookBigImageBinding.inflate(inflater, container, false);
        bigImageBinding.bigPicture.setOnLongClickListener(this::showSaveImageDialog);
        bigImageBinding.bigPicture.setOnClickListener(this::onClick);
        initPresenter();
        return bigImageBinding.getRoot();
    }

    private boolean showSaveImageDialog(View view) {
        AlertDialogManager.getInstance().showDialog(getActivity(), "ave", getString(R.string.Tap3_SavePic),
                getString(R.string.Tap3_SavePic), (DialogInterface dialog, int which) -> {
                    dialog.dismiss();
                    ToastUtil.showToast(getString(R.string.SAVED_PHOTOS));
                    String account = friendContextItem.friendRequest == null ? friendContextItem.friendAccount.account : friendContextItem.friendRequest.account;
                    Glide
                            .with(MineLookBigImageFragment.this)
                            .load(new JFGAccountURL(account))
                            .downloadOnly(new SimpleTarget<File>() {
                                @Override
                                public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
                                    Schedulers.io().createWorker().schedule(() -> {
                                        String account = friendContextItem.friendRequest == null ? friendContextItem.friendAccount.account : friendContextItem.friendRequest.account;
                                        FileUtils.copyFile(resource, new File(JConstant.MEDIA_PATH + account + ".jpg"));
                                    });
                                }
                            });
                }, getString(R.string.CANCEL), null, false);
        return true;
    }

    private void initPresenter() {
        presenter = new MineLookBigImagePresenterImp(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadPicture();
    }

    private void loadPicture() {
        String account = friendContextItem.friendRequest == null ? friendContextItem.friendAccount.account : friendContextItem.friendRequest.account;
        Glide
                .with(this)
                .load(new JFGAccountURL(account))
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new ImageViewTarget<GlideDrawable>(bigImageBinding.bigPicture) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
//                        showLoadImageProgress();
                    }

                    @Override
                    protected void setResource(GlideDrawable resource) {
                        success = true;
                        view.setImageDrawable(resource);
//                        hideLoadImageProgress();
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
//                        hideLoadImageProgress();
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
//                        hideLoadImageProgress();
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    public void setPresenter(MineLookBigImageContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    public void onClick(View view) {
        //点击大图退出全屏
        if (success) {
            getActivity().getSupportFragmentManager().popBackStack();
        } else {
            loadPicture();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void showLoadImageProgress() {
        LoadingDialog.showLoading(getActivity(), getString(R.string.LOADING));
    }


    public void hideLoadImageProgress() {
        LoadingDialog.dismissLoading();
    }
}
