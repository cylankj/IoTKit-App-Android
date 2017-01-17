package com.cylan.jiafeigou.n.view.mine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.softkeyboard.util.ViewUtil;
import com.cylan.jiafeigou.support.zscan.Qrcode;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.sina.weibo.sdk.utils.LogUtil;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    private JFGAccount jfgaccount;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

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
        jfgaccount = (JFGAccount) arguments.getSerializable("jfgaccount");
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
        ivUserQrcode.setImageBitmap(Qrcode.createQRImage(jfgaccount.getAccount(),ViewUtils.dp2px(137),ViewUtils.dp2px(137),null));
    }

    private void initView() {
        tvUserAlias.setText(jfgaccount.getAlias());
        Glide.with(getContext()).load(jfgaccount.getPhotoUrl())
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BitmapImageViewTarget(ivUserIcon) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivUserIcon.setImageDrawable(circularBitmapDrawable);
                    }

                });
    }

    @OnClick(R.id.iv_close_dialog)
    public void onClick() {
        dismiss();
    }
}
