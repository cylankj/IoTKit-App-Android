package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.entity.jniCall.JFGAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.module.DataSourceManager;
import com.cylan.jiafeigou.cache.db.module.Account;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.LinkManager;
import com.cylan.jiafeigou.module.GlideApp;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.zscan.Qrcode;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;

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
        if (isopenlogin) {
            Account account = DataSourceManager.getInstance().getAccount();
            tvUserAlias.setText(account == null ? "" : account.getAlias() == null ? account.getAccount() : account.getAlias());
            GlideApp.with(getContext()).load(PreferencesUtils.getString(JConstant.OPEN_LOGIN_USER_ICON))
                    .circleCrop()
                    .placeholder(R.drawable.icon_mine_head_normal)
                    .error(R.drawable.icon_mine_head_normal)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivUserIcon);
            return;
        }
        JFGAccount jfgAccount = DataSourceManager.getInstance().getJFGAccount();
        tvUserAlias.setText(jfgAccount.getAlias());
        GlideApp.with(getContext()).load(jfgAccount.getPhotoUrl())
                .circleCrop()
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ivUserIcon);
    }

    @OnClick(R.id.iv_close_dialog)
    public void onClick() {
        dismiss();
    }
}
