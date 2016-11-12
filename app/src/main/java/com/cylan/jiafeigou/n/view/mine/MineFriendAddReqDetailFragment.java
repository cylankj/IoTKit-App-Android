package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGFriendRequest;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.RxEvent;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddReqDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineFriendAddReqDetailPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.MineAddReqBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;

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

    private MineLookBigImageFragment lookBigImageFragment;

    private MineFriendAddReqDetailContract.Presenter presenter;
    private MineAddReqBean addRequestItems;

    public static MineFriendAddReqDetailFragment newInstance(Bundle bundle) {
        MineFriendAddReqDetailFragment fragment = new MineFriendAddReqDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", "");
        lookBigImageFragment = MineLookBigImageFragment.newInstance(bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relative_and_friend_add_req_detail, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initMegHight();
        initData();
        return view;
    }

    private void initPresenter() {
        presenter = new MineFriendAddReqDetailPresenterImp(this);
    }

    /**
     * desc：获取传递过来的数据
     */
    private void initData() {
        Bundle arguments = getArguments();
        boolean isFrome = arguments.getBoolean("isFrom");
        addRequestItems = (MineAddReqBean) arguments.getSerializable("addRequestItems");
        if ("".equals(addRequestItems.alias)){
            tvRelativeAndFriendName.setText("sjd172");
        }else {
            tvRelativeAndFriendName.setText(addRequestItems.alias);
        }
        tvRelativeAndFriendLikeName.setText(addRequestItems.account);
        tvAddRequestMesg.setText(addRequestItems.sayHi);
        showOrHideReqMesg(isFrome);
        //Glide.with(getContext()).load(addRequestItems.getIcon()).error(R.drawable.icon_mine_head_normal).into(ivDetailUserHead);
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
                if (presenter != null){
                   presenter.start();
                }
                break;
        }
    }

    /**
     * desc:查看大头像
     */
    private void jump2LookBigImage() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, lookBigImageFragment, "lookBigImageFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    @Override
    public void showOrHideReqMesg(boolean isFrom) {
        if (isFrom){
            rlAddRequestMesg.setVisibility(View.VISIBLE);
        }else {
            rlAddRequestMesg.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 添加请求过期弹出框
     */
    @Override
    public void showReqOutTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("当前消息已过期，是否向对方发送\n" + "添加好友验证？");
        builder.setPositiveButton("发送", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                presenter.sendAddReq(addRequestItems);
                showSendAddReqResult(true);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void showSendAddReqResult(boolean flag) {
        if(flag){
            getFragmentManager().popBackStack();
            ToastUtil.showPositiveToast("请求已发送");
        }else {
            getFragmentManager().popBackStack();
            ToastUtil.showNegativeToast("请求发送失败");
        }
    }

    @Override
    public void showAddedReult(boolean flag) {
        if (flag) {
            ToastUtil.showPositiveToast("添加成功");
        }else {
            ToastUtil.showNegativeToast("添加失败");
        }

    }

    /**
     * 跳转到添加请求页
     */
    @Override
    public void jump2AddReqFragment() {
        Bundle bundle = new Bundle();
        bundle.putString("account",addRequestItems.account);
        MineAddFromContactFragment fragment = MineAddFromContactFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, fragment, "addFromContactFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    /**
     * 是否存在该账号的结果
     * @param getAddReqList
     */
    @Override
    public void isHasAccountResult(RxEvent.GetAddReqList getAddReqList) {
        for (JFGFriendRequest bean:getAddReqList.arrayList){
            if (bean.account.equals(addRequestItems.account)){
                // 向我发送过请求
                MineAddReqBean addReqBean = new MineAddReqBean();
                addReqBean.account = bean.account;
                addReqBean.time = bean.time;
                presenter.checkAddReqOutTime(addReqBean);
                return;
            }else {
                //未向我发送过请求
                jump2AddReqFragment();
            }
        }

    }
}
