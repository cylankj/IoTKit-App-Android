package com.cylan.jiafeigou.n.view.mine;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendDetailPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.IMEUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.dialog.BaseDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineFriendDetailFragment extends Fragment implements MineFriendDetailContract.View {

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
        getNavigationHeigth();
        initData();
        return view;
    }

    /**
     * 导航栏高度
     */
    private void getNavigationHeigth() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        int navigationHeight = getResources().getDimensionPixelSize(resourceId);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) presenter.start();
    }

    /**
     * desc:获取到传过来的数据
     */
    private void initData() {
        Bundle bundle = getArguments();
        frienditembean = bundle.getParcelable("frienditembean");
        if (TextUtils.isEmpty(frienditembean.markName)) {
            tvRelativeAndFriendName.setVisibility(View.GONE);
        } else {
            tvRelativeAndFriendName.setVisibility(View.VISIBLE);
            tvRelativeAndFriendName.setText(frienditembean.markName);
        }

        tvRelativeAndFriendLikeName.setText(getString(R.string.ALIAS) + ": " + frienditembean.alias);

        //头像显示
        MyImageTarget myImageTarget = new MyImageTarget(ivDetailUserHead, getContext().getResources());
        Glide.with(getContext()).load(frienditembean.iconUrl)
                .asBitmap()
                .placeholder(R.drawable.icon_mine_head_normal)
                .error(R.drawable.icon_mine_head_normal)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(myImageTarget);
    }

    private static class MyImageTarget extends BitmapImageViewTarget {

        public final WeakReference<Resources> resourcesWeakReference;
        public final WeakReference<RoundedImageView> roundedImageViewWeakReference;

        public MyImageTarget(RoundedImageView view, Resources resources) {
            super(view);
            resourcesWeakReference = new WeakReference<Resources>(resources);
            roundedImageViewWeakReference = new WeakReference<RoundedImageView>(view);
        }

        @Override
        protected void setResource(Bitmap resource) {
            super.setResource(resource);
            RoundedBitmapDrawable circularBitmapDrawable =
                    RoundedBitmapDrawableFactory.create(resourcesWeakReference.get(), resource);
            circularBitmapDrawable.setCircular(true);
            roundedImageViewWeakReference.get().setImageDrawable(circularBitmapDrawable);
        }
    }

    private void initListener() {
        mineSetRemarkNameFragment.setOnSetRemarkNameListener(new MineSetRemarkNameFragment.OnSetRemarkNameListener() {
            @Override
            public void remarkNameChange(String name) {
                frienditembean.markName = name;
                tvRelativeAndFriendName.setVisibility(View.VISIBLE);
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

    @Override
    public String getUuid() {
        return null;
    }

    @OnClick({R.id.tv_toolbar_icon, R.id.rl_change_name, R.id.rl_delete_relativeandfriend,
            R.id.tv_share_device, R.id.iv_detail_user_head})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getActivity().getSupportFragmentManager().popBackStack();
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
                showDelFriendDialog(frienditembean);
                break;
            case R.id.tv_share_device:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_share_device));
                AppLogger.e("tv_share_device");
                List<Device> devices = BaseApplication.getAppComponent().getSourceManager().getAllDevice();
                int deviceCount = 0;
                if (devices != null) {
                    for (Device device : devices) {
                        if (TextUtils.isEmpty(device.shareAccount)) deviceCount++;
                    }
                }
                if (deviceCount == 0) {//当前账号没有任何设备
                    ToastUtil.showNegativeToast(getString(R.string.Tap1_Index_NoDevice));
                } else {
                    jump2ShareDeviceFragment();
                }
                break;
            case R.id.iv_detail_user_head:
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.iv_detail_user_head));
                AppLogger.e("iv_detail_user_head");
                jump2LookBigImageFragment();
                break;
        }
    }


    /**
     * 删除亲友对话框
     *
     * @param bean
     */
    public void showDelFriendDialog(RelAndFriendBean bean) {
        Bundle bundle = new Bundle();
        bundle.putString(BaseDialog.KEY_TITLE, getString(R.string.Tap3_Friends_DeleteFriends));
        bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
        SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
        simpleDialogFragment.setAction((int id, Object value) -> {
            if (id == R.id.tv_dialog_btn_left) {
                if (NetUtils.getNetType(ContextUtils.getContext()) == -1) {
                    ToastUtil.showToast(getString(R.string.NO_NETWORK_4));
                    return;
                }
                presenter.sendDeleteFriendReq(bean.account);
            }
        });
        simpleDialogFragment.show(getActivity().getSupportFragmentManager(), "simpleDialogFragment");
    }

    @Override
    public void handlerDelCallBack(int code) {
        if (code != JError.ErrorOK) {
            ToastUtil.showNegativeToast(getString(R.string.Tips_DeleteFail));
            return;
        }
        if (lisenter != null) {
            Bundle arguments = getArguments();
            int position = arguments.getInt("position");
            if (!(position == -1)) {
                lisenter.onDelete(position);
                ToastUtil.showPositiveToast(getString(R.string.DELETED_SUC));
            }

        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void showDeleteProgress() {
        LoadingDialog.showLoading(getActivity().getSupportFragmentManager(), getString(R.string.DELETEING));
    }

    @Override
    public void hideDeleteProgress() {
        LoadingDialog.dismissLoading(getActivity().getSupportFragmentManager());
    }

    /**
     * desc:查看大头像
     */
    private void jump2LookBigImageFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", frienditembean.iconUrl);
        mineLookBigImageFragment = MineLookBigImageFragment.newInstance(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
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
        getActivity().getSupportFragmentManager().beginTransaction()
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
        getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineShareDeviceFragment, "mineShareDeviceFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
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
        IMEUtils.hide(getActivity());
    }
}
