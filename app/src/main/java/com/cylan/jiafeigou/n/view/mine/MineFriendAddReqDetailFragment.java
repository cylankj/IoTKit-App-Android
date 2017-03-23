package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddReqDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendAddReqDetailPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.photoselect.models.Image;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.roundedimageview.RoundedImageView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineFriendAddReqDetailFragment extends Fragment implements MineFriendAddReqDetailContract.View {

    @BindView(R.id.iv_top_bar_left_back)
    ImageView ivTopBarLeftBack;
    @BindView(R.id.iv_detail_user_head)
    ImageView ivDetailUserHead;
    @BindView(R.id.tv_relative_and_friend_name)
    TextView tvRelativeAndFriendName;
    @BindView(R.id.tv_relative_and_friend_account)
    TextView tvRelativeAndFriendLikeName;
    @BindView(R.id.tv_add_request_mesg)
    TextView tvAddRequestMesg;
    @BindView(R.id.tv_add_as_relative_and_friend)
    TextView tvAddAsRelativeAndFriend;
    @BindView(R.id.rl_add_request_mesg)
    RelativeLayout rlAddRequestMesg;
    @BindView(R.id.rl_title_bar)
    FrameLayout rlTitleBar;

    private MineLookBigImageFragment lookBigImageFragment;
    private MineFriendAddReqDetailContract.Presenter presenter;
    private MineAddReqBean addRequestItems;
    private MineAddFromContactFragment addReqFragment;

    private OnAcceptAddListener addListener;
    private boolean isFrom;

    public interface OnAcceptAddListener {
        void onAccept(MineAddReqBean backbean);
    }

    public void setOnAcceptAddListener(OnAcceptAddListener addListener) {
        this.addListener = addListener;
    }

    public static MineFriendAddReqDetailFragment newInstance(Bundle bundle) {
        MineFriendAddReqDetailFragment fragment = new MineFriendAddReqDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_add_req_detail, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initMegHight();
        initData();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewUtils.setViewPaddingStatusBar(rlTitleBar);
    }

    private void initPresenter() {
        presenter = new MineFriendAddReqDetailPresenterImp(this);
    }

    /**
     * desc：获取传递过来的数据
     */
    private void initData() {
        Bundle arguments = getArguments();
        isFrom = arguments.getBoolean("isFrom");
        addRequestItems = (MineAddReqBean) arguments.getSerializable("addRequestItems");
        if (TextUtils.isEmpty(addRequestItems.alias)) {
            tvRelativeAndFriendName.setText("sjd172");
        } else {
            tvRelativeAndFriendName.setText(addRequestItems.alias);
        }
        tvRelativeAndFriendLikeName.setText(addRequestItems.account);
        tvAddRequestMesg.setText(addRequestItems.sayHi);
        showOrHideReqMesg(isFrom);

        //显示头像
        MyImageTarget myImageTarget = new MyImageTarget(ivDetailUserHead,getContext().getResources());
        Glide.with(getContext()).load(addRequestItems.iconUrl)
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.icon_mine_head_normal)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.icon_mine_head_normal)
                .into(myImageTarget);
    }

    private static class MyImageTarget extends BitmapImageViewTarget{

        public final WeakReference<Resources> resourcesWeakReference;
        public final WeakReference<ImageView> roundedImageViewWeakReference;

        public MyImageTarget(ImageView view, Resources resources) {
            super(view);
            resourcesWeakReference = new WeakReference<Resources>(resources);
            roundedImageViewWeakReference = new WeakReference<ImageView>(view);
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

    /**
     * desc：设定验证信息的行高
     */
    private void initMegHight() {
        int lineCount = tvAddRequestMesg.getLineCount();
        if (lineCount == 2) {
            ViewGroup.LayoutParams layoutParams = tvAddRequestMesg.getLayoutParams();
            layoutParams.height = ViewUtils.dp2px(72);
            tvAddRequestMesg.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void setPresenter(MineFriendAddReqDetailContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @OnClick({R.id.iv_top_bar_left_back, R.id.iv_detail_user_head, R.id.tv_add_as_relative_and_friend})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_top_bar_left_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_detail_user_head:                      //查看大头像
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.iv_detail_user_head));
                AppLogger.e("iv_detail_user_head");
                jump2LookBigImage();
                break;
            case R.id.tv_add_as_relative_and_friend:            //添加为亲友
                if (presenter != null) {
                    presenter.start();
                }
                break;
        }
    }

    /**
     * desc:查看大头像
     */
    private void jump2LookBigImage() {
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", addRequestItems.iconUrl);
        lookBigImageFragment = MineLookBigImageFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, lookBigImageFragment, "lookBigImageFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    @Override
    public void showOrHideReqMesg(boolean isFrom) {
        if (isFrom) {
            rlAddRequestMesg.setVisibility(View.VISIBLE);
        } else {
            rlAddRequestMesg.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 添加请求过期弹出框
     */
    @Override
    public void showReqOutTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.Tap3_FriendsAdd_ExpiredTips));
        builder.setPositiveButton(getString(R.string.Tap3_FriendsAdd_Send), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                presenter.sendAddReq(addRequestItems);
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
    public void showSendAddReqResult(boolean flag) {
        if (flag) {
            ToastUtil.showPositiveToast(getString(R.string.Tap3_FriendsAdd_Contacts_InvitedTips));
            getFragmentManager().popBackStack();
        } else {
            ToastUtil.showNegativeToast("请求发送失败");
        }
    }

    @Override
    public void showAddedReult(boolean flag) {
        if (flag) {
            ToastUtil.showPositiveToast(getString(R.string.Tap3_FriendsAdd_Success));
            if (addListener != null) {
                addListener.onAccept(addRequestItems);
            }
            getFragmentManager().popBackStack();
        } else {
            ToastUtil.showNegativeToast(getString(R.string.ADD_FAILED));
        }
    }

    /**
     * 跳转到添加请求页
     */
    @Override
    public void jump2AddReqFragment() {
        Bundle addReqBundle = new Bundle();
        addReqBundle.putString("account", addRequestItems.account);
        addReqFragment = MineAddFromContactFragment.newInstance(addReqBundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, addReqFragment, "addFromContactFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * 是否存在该账号的结果
     *
     * @param getAddReqList
     */
    @Override
    public void isHasAccountResult(RxEvent.GetAddReqList getAddReqList) {
        for (JFGFriendRequest bean : getAddReqList.arrayList) {
            if (bean.account.equals(addRequestItems.account)) {
                // 向我发送过请求
                MineAddReqBean addReqBean = new MineAddReqBean();
                addReqBean.account = bean.account;
                addReqBean.time = bean.time;
                presenter.checkAddReqOutTime(addReqBean);
                return;
            } else {
                //未向我发送过请求
                jump2AddReqFragment();
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }
}
