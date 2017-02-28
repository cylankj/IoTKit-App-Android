package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineUserInfoLookBigHeadContract;
import com.cylan.jiafeigou.utils.ToastUtil;
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
    ImageView ivUserinfoBigImage;

    private boolean loadResult = false;
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
        WindowManager wm = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ivUserinfoBigImage.getLayoutParams());
        lp.height = (int) (height * 0.47);
        lp.setMargins(0, (int) (height * 0.23), 0, 0);
        ivUserinfoBigImage.setLayoutParams(lp);
    }

    /**
     * 获取传递过来的参数
     */
    private void getArgumentData() {
        Bundle arguments = getArguments();
        iamgeUrl = arguments.getString("imageUrl");
    }

    private void loadBigImage(String url) {
        if (TextUtils.isEmpty(url)){
            return;
        }
        showLoadImageProgress();
        Glide.with(getContext())
                .load(url)
                .asBitmap()
                .centerCrop()
                .skipMemoryCache(true)
                .error(R.drawable.icon_mine_head_normal)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        hideLoadImageProgress();
                        loadResult = false;
                        ToastUtil.showNegativeToast(getString(R.string.Item_LoadFail));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        loadResult = true;
                        hideLoadImageProgress();
                        return false;
                    }
                })
                .into(ivUserinfoBigImage);
    }

    @OnClick(R.id.iv_userinfo_big_image)
    public void onClick() {
        if (loadResult) {
            getFragmentManager().popBackStack();
        } else {
            loadBigImage(iamgeUrl);
        }
    }

    @Override
    public void showLoadImageProgress() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
    }



    @Override
    public void hideLoadImageProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void setPresenter(MineUserInfoLookBigHeadContract.Presenter presenter) {

    }
}
