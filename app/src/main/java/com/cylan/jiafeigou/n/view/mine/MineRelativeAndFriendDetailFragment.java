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

import com.bumptech.glide.Glide;
import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendDetailContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativeAndFriendDetailPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.sina.weibo.sdk.utils.LogUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/21
 * 描述：
 */
public class MineRelativeAndFriendDetailFragment extends Fragment implements MineRelativeAndFriendDetailContract.View {


    @BindView(R.id.iv_top_bar_left_back)
    ImageView ivTopBarLeftBack;
    @BindView(R.id.iv_detail_user_head)
    ImageView ivDetailUserHead;
    @BindView(R.id.tv_relative_and_friend_name)
    TextView tvRelativeAndFriendName;
    @BindView(R.id.tv_relative_and_friend_like_name)
    TextView tvRelativeAndFriendLikeName;
    @BindView(R.id.rl_change_name)
    RelativeLayout rlChangeName;
    @BindView(R.id.rl_delete_relativeandfriend)
    RelativeLayout rlDeleteRelativeandfriend;
    @BindView(R.id.tv_share_device)
    TextView tvShareDevice;

    private MineRelativesAndFriendsListShareDevicesFragment mineShareDeviceFragment;
    private MineSetRemarkNameFragment mineSetRemarkNameFragment;
    private MineLookBigImageFragment mineLookBigImageFragment;

    private MineRelativeAndFriendDetailContract.Presenter presenter;

    public OnDeleteClickLisenter lisenter;

    public interface OnDeleteClickLisenter{
        void onDelete(int position);
    }

    public void setOnDeleteClickLisenter(OnDeleteClickLisenter lisenter){
        this.lisenter = lisenter;
    }

    public static MineRelativeAndFriendDetailFragment newInstance(Bundle bundle) {
        MineRelativeAndFriendDetailFragment fragment = new MineRelativeAndFriendDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mineShareDeviceFragment = MineRelativesAndFriendsListShareDevicesFragment.newInstance();
        mineSetRemarkNameFragment = MineSetRemarkNameFragment.newInstance(new Bundle());

        Bundle bundle = new Bundle();
        bundle.putString("imageUrl","");
        mineLookBigImageFragment = MineLookBigImageFragment.newInstance(bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativeandfriend_detail, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initListener();
        initData();
        return view;
    }

    /**
     * desc:获取到传过来的数据
     */
    private void initData() {
        Bundle bundle = getArguments();
        JFGFriendAccount frienditembean = (JFGFriendAccount) bundle.getSerializable("frienditembean");
        tvRelativeAndFriendName.setText(frienditembean.markName);
        tvRelativeAndFriendLikeName.setText(frienditembean.alias);

        //TODO　头像获取
        //Glide.with(getContext()).load(frienditembean.getIcon()).error(R.drawable.icon_mine_head_normal).into(ivDetailUserHead);
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
        presenter = new MineRelativeAndFriendDetailPresenterImp(this);
    }

    @Override
    public void setPresenter(MineRelativeAndFriendDetailContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_top_bar_left_back, R.id.rl_change_name, R.id.rl_delete_relativeandfriend,
            R.id.tv_share_device,R.id.iv_detail_user_head})
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
                    showDeleteDialog();
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

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("是否删除亲友");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(lisenter != null){
                    Bundle arguments = getArguments();
                    int position = arguments.getInt("position");
                    lisenter.onDelete(position);
                    ToastUtil.showToast(getContext(),"删除成功");
                }
                getFragmentManager().popBackStack();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * desc:查看大头像
     */
    private void jump2LookBigImageFragment() {
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
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineSetRemarkNameFragment, "mineSetRemarkNameFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }

    private void jump2ShareDeviceFragment() {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineShareDeviceFragment, "mineShareDeviceFragment")
                .addToBackStack("mineHelpFragment")
                .commit();
    }
}
