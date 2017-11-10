package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.databinding.FragmentMineLookBigImageBinding;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineLookBigImageContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineLookBigImagePresenterImp;
import com.cylan.jiafeigou.n.view.adapter.item.FriendContextItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.FileUtils;
import com.cylan.jiafeigou.utils.JFGAccountURL;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.io.File;
import java.util.concurrent.TimeUnit;

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
                    FutureTarget<File> submit = GlideApp.with(MineLookBigImageFragment.this)
                            .downloadOnly()
                            .load(new JFGAccountURL(account))
                            .submit();
                    Schedulers.io().createWorker().schedule(() -> {
                        try {
                            File file = submit.get(10, TimeUnit.SECONDS);
                            FileUtils.copyFile(file, new File(JConstant.MEDIA_PATH + account + ".jpg"));
                        }catch (Exception e){
                            AppLogger.e(MiscUtils.getErr(e));
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
        GlideApp
                .with(this)
                .load(new JFGAccountURL(account))
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(bigImageBinding.bigPicture);
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
        LoadingDialog.showLoading(getActivity(), getString(R.string.LOADING), true);
    }


    public void hideLoadImageProgress() {
        LoadingDialog.dismissLoading();
    }
}
