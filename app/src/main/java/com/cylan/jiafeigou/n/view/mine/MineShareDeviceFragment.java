package com.cylan.jiafeigou.n.view.mine;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class MineShareDeviceFragment extends Fragment implements MineShareDeviceContract.View {

    @BindView(R.id.iv_home_mine_sharedevices_back)
    ImageView ivHomeMineSharedevicesBack;
    @BindView(R.id.tv_share_smartcamera)
    TextView tvShareSmartcamera;
    @BindView(R.id.tv_share_smartcamera1)
    TextView tvShareSmartcamera1;
    @BindView(R.id.tv_share_smartcloud)
    TextView tvShareSmartcloud;
    @BindView(R.id.tv_share_smartbell)
    TextView tvShareSmartbell;
    @BindView(R.id.rl_mine_share_smartcamera)
    RelativeLayout rlMineShareSmartcamera;

    private MineDevicesShareManagerFragment mineDevicesShareManagerFragment;
    private MineShareToRelativeAndFriendFragment shareToRelativeAndFriendFragment;
    private MineShareToContactFragment mineShareToContactFragment;
    private AlertDialog alertDialog;

    public static MineShareDeviceFragment newInstance() {
        return new MineShareDeviceFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mineDevicesShareManagerFragment = MineDevicesShareManagerFragment.newInstance();
        shareToRelativeAndFriendFragment = MineShareToRelativeAndFriendFragment.newInstance();
        mineShareToContactFragment = MineShareToContactFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mine_share_device, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.iv_home_mine_sharedevices_back, R.id.tv_share_smartcamera, R.id.tv_share_smartcamera1,
            R.id.tv_share_smartcloud, R.id.tv_share_smartbell,R.id.rl_mine_share_smartcamera})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_home_mine_sharedevices_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.tv_share_smartcamera:          //摄像头分享
                showShareDialog();
                break;

            case R.id.tv_share_smartcamera1:         //摄像头1分享
                showShareDialog();
                break;

            case R.id.tv_share_smartcloud:           //云相框分享
                showShareDialog();
                break;

            case R.id.tv_share_smartbell:            //门铃分享
                showShareDialog();
                break;

            case R.id.rl_mine_share_smartcamera:
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content,mineDevicesShareManagerFragment,"mineDevicesShareManagerFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
                break;
        }
    }

    @Override
    public void setPresenter(MineShareDeviceContract.Presenter presenter) {
    }

    @Override
    public void showShareDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = View.inflate(getContext(), R.layout.fragment_home_mine_share_devices_dialog, null);
        view.findViewById(R.id.tv_share_to_friends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content,shareToRelativeAndFriendFragment,"shareToRelativeAndFriendFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
                alertDialog.dismiss();
            }
        });
        view.findViewById(R.id.tv_share_to_contract).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content,mineShareToContactFragment,"mineShareToContactFragment")
                        .addToBackStack("mineHelpFragment")
                        .commit();
                alertDialog.dismiss();
            }
        });
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();
    }

}
