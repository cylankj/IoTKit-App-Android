package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendDetailPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineFriendDetailFragment extends Fragment implements MineFriendDetailContract.View {

    @BindView(R.id.iv_top_bar_left_back)
    ImageView ivTopBarLeftBack;
    @BindView(R.id.iv_detail_user_head)
    RoundedImageView ivDetailUserHead;
    @BindView(R.id.tv_relative_and_friend_name)
    TextView tvRelativeAndFriendName;
    @BindView(R.id.tv_relative_and_friend_account)
    TextView tvRelativeAndFriendLikeName;
    @BindView(R.id.rl_change_name)
    RelativeLayout rlChangeName;
    @BindView(R.id.rl_delete_relativeandfriend)
    RelativeLayout rlDeleteRelativeandfriend;
    @BindView(R.id.tv_share_device)
    TextView tvShareDevice;

    private MineFriendsListShareDevicesFragment mineShareDeviceFragment;
    private MineSetRemarkNameFragment mineSetRemarkNameFragment;
    private MineLookBigImageFragment mineLookBigImageFragment;

    private MineFriendDetailContract.Presenter presenter;

    public OnDeleteClickLisenter lisenter;
    private RelAndFriendBean frienditembean;

    public interface OnDeleteClickLisenter {
        void onDelete(int position);
    }

    public void setOnDeleteClickLisenter(OnDeleteClickLisenter lisenter) {
        this.lisenter = lisenter;
    }

    public static MineFriendDetailFragment newInstance(Bundle bundle) {
        MineFriendDetailFragment fragment = new MineFriendDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_friend_detail, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initData();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null)presenter.start();
    }

    /**
     * desc:获取到传过来的数据
     */
    private void initData() {
        Bundle bundle = getArguments();
        frienditembean = (RelAndFriendBean) bundle.getParcelable("frienditembean");
        tvRelativeAndFriendName.setText(frienditembean.markName);
        tvRelativeAndFriendLikeName.setText(frienditembean.alias);
        //头像显示
        Glide.with(getContext()).load(frienditembean.iconUrl)
                .asBitmap()
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BitmapImageViewTarget(ivDetailUserHead) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivDetailUserHead.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    private void initListener() {
        mineSetRemarkNameFragment.setOnSetRemarkNameListener(new MineSetRemarkNameFragment.OnSetRemarkNameListener() {
            @Override
            public void remarkNameChange(String name) {
                tvRelativeAndFriendName.setText(name);
            }
        });
    }

    private void initPresenter() {
        presenter = new MineFriendDetailPresenterImp(this);
    }

    @Override
    public void setPresenter(MineFriendDetailContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_top_bar_left_back, R.id.rl_change_name, R.id.rl_delete_relativeandfriend,
            R.id.tv_share_device, R.id.iv_detail_user_head})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_top_bar_left_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.rl_change_name:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.rl_change_name));
                AppLogger.e("rl_change_name");
                jump2SetRemarkNameFragment();
                break;
            case R.id.rl_delete_relativeandfriend:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.rl_delete_relativeandfriend));
                AppLogger.e("rl_delete_relativeandfriend");
                showDeleteDialog(frienditembean);
                break;
            case R.id.tv_share_device:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_share_device));
                AppLogger.e("tv_share_device");
                jump2ShareDeviceFragment();
                break;
            case R.id.iv_detail_user_head:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.iv_detail_user_head));
                AppLogger.e("iv_detail_user_head");
                jump2LookBigImageFragment();
                break;
        }
    }

    private void showDeleteDialog(final RelAndFriendBean bean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.Tap3_Friends_DeleteFriends));
        builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.sendDeleteFriendReq(bean.account);
                hideDeleteProgress();
                handlerDelCallBack();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void handlerDelCallBack() {
        if (lisenter != null) {
            Bundle arguments = getArguments();
            int position = arguments.getInt("position");
            lisenter.onDelete(position);
            ToastUtil.showToast(getString(R.string.DELETED_SUC));
        }
        getFragmentManager().popBackStack();
    }

    @Override
    public void showDeleteProgress() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.DELETEING));
    }

    @Override
    public void hideDeleteProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    /**
     * desc:查看大头像
     */
    private void jump2LookBigImageFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", frienditembean.iconUrl);
        mineLookBigImageFragment = MineLookBigImageFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineLookBigImageFragment, "mineLookBigImageFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * desc:跳转到备注名称的界面；
     */
    private void jump2SetRemarkNameFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("friendBean", frienditembean);
        mineSetRemarkNameFragment = MineSetRemarkNameFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineSetRemarkNameFragment, "mineSetRemarkNameFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
        initListener();                     //修改备注回调监听
    }

    private void jump2ShareDeviceFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("shareDeviceBean", frienditembean);
        mineShareDeviceFragment = MineFriendsListShareDevicesFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineShareDeviceFragment, "mineShareDeviceFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * 网络状态变化
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1){
            hideDeleteProgress();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }
    }
}
