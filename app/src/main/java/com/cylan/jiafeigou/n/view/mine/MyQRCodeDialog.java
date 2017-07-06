package com.cylan.jiafeigou.n.view.mine;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.zscan.Qrcode;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2017/1/16
 * 描述：
 */
public class MyQRCodeDialog extends BaseDialog {

    @BindView(R.id.iv_user_icon)
    ImageView ivUserIcon;
    @BindView(R.id.tv_user_alias)
    TextView tvUserAlias;
    @BindView(R.id.iv_user_qrcode)
    ImageView ivUserQrcode;
    @BindView(R.id.iv_close_dialog)
    ImageView ivCloseDialog;
    private boolean isopenlogin;

    public static MyQRCodeDialog newInstance(Bundle bundle) {
        MyQRCodeDialog dialog = new MyQRCodeDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        Bundle arguments = getArguments();
        isopenlogin = (boolean) arguments.getSerializable("isopenlogin");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_dialog_my_qrcode, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void onResume() {
        super.onResume();
        rx.Observable.just("go")
                .subscribeOn(Schedulers.io())
                .map(ret -> Qrcode.createQRImage(LinkManager.getQrCodeLink(), ViewUtils.dp2px(78), ViewUtils.dp2px(78), null))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ret -> ivUserQrcode.setImageBitmap(ret), AppLogger::e);
    }

    private void initView() {
        MyViewTarget myViewTarget = new MyViewTarget(ivUserIcon, getResources());
        if (isopenlogin) {
            Account account = BaseApplication.getAppComponent().getSourceManager().getAccount();
            tvUserAlias.setText(account == null ? "" : account.getAlias() == null ? account.getAccount() : account.getAlias());
            Glide.with(getContext()).load(PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON))
                    .asBitmap()
                    .centerCrop()
                    .placeholder(R.drawable.icon_mine_head_normal)
                    .error(R.drawable.icon_mine_head_normal)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(myViewTarget);
            return;
        }
        JFGAccount jfgAccount = BaseApplication.getAppComponent().getSourceManager().getJFGAccount();
        tvUserAlias.setText(jfgAccount.getAlias());
        Glide.with(getContext()).load(jfgAccount.getPhotoUrl())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(myViewTarget);
    }


    private static class MyViewTarget extends BitmapImageViewTarget {
        private WeakReference<Resources> resourcesWeakReference;
        private WeakReference<ImageView> imageViewWeakReference;

        public MyViewTarget(ImageView view, Resources resources) {
            super(view);
            resourcesWeakReference = new WeakReference<Resources>(resources);
            imageViewWeakReference = new WeakReference<ImageView>(view);
        }

        @Override
        protected void setResource(Bitmap resource) {
            super.setResource(resource);
            RoundedBitmapDrawable circularBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(resourcesWeakReference.get(), resource);
            circularBitmapDrawable.setCircular(true);
            imageViewWeakReference.get().setImageDrawable(circularBitmapDrawable);
        }
    }


    @OnClick(R.id.iv_close_dialog)
    public void onClick() {
        dismiss();
    }
}
