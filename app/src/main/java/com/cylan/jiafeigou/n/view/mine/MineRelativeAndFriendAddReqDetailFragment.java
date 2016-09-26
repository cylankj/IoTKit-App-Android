package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativeAndFriendAddReqDetailContract;
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
public class MineRelativeAndFriendAddReqDetailFragment extends Fragment implements MineRelativeAndFriendAddReqDetailContract.View {

    @BindView(R.id.iv_top_bar_left_back)
    ImageView ivTopBarLeftBack;
    @BindView(R.id.iv_detail_user_head)
    ImageView ivDetailUserHead;
    @BindView(R.id.tv_relative_and_friend_name)
    TextView tvRelativeAndFriendName;
    @BindView(R.id.tv_relative_and_friend_like_name)
    TextView tvRelativeAndFriendLikeName;
    @BindView(R.id.tv_add_request_mesg)
    TextView tvAddRequestMesg;
    @BindView(R.id.tv_add_as_relative_and_friend)
    TextView tvAddAsRelativeAndFriend;

    private MineLookBigImageFragment lookBigImageFragment;

    public static MineRelativeAndFriendAddReqDetailFragment newInstance() {
        return new MineRelativeAndFriendAddReqDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl","");
        lookBigImageFragment = MineLookBigImageFragment.newInstance(bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relative_and_friend_add_req_detail, container, false);
        ButterKnife.bind(this, view);
        initMegHight();
        return view;
    }

    /**
     * desc：设定验证信息的行高
     */
    private void initMegHight() {
        int lineCount = tvAddRequestMesg.getLineCount();
        if (lineCount == 2){
            ViewGroup.LayoutParams layoutParams = tvAddRequestMesg.getLayoutParams();
            layoutParams.height = ViewUtils.dp2px(72);
            tvAddRequestMesg.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void setPresenter(MineRelativeAndFriendAddReqDetailContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_top_bar_left_back, R.id.iv_detail_user_head, R.id.tv_add_as_relative_and_friend})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_top_bar_left_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_detail_user_head:                      //查看大头像
                jump2LookBigImage();
                break;
            case R.id.tv_add_as_relative_and_friend:            //添加为亲友
                ToastUtil.showToast(getContext(),"添加成功");
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


}
