package com.cylan.jiafeigou.n.view.mine;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareDeviceContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareDevicePresenterImp;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.view.adapter.MineShareDeviceAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/5
 * 描述：
 */
public class MineShareDeviceFragment extends Fragment implements MineShareDeviceContract.View {

    @BindView(R.id.recycle_share_device_list)
    RecyclerView recycleShareDeviceList;
    @BindView(R.id.iv_home_mine_sharedevices_back)
    ImageView ivHomeMineSharedevicesBack;

    private MineShareDeviceContract.Presenter presenter;
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
        shareToRelativeAndFriendFragment = MineShareToRelativeAndFriendFragment.newInstance();
        mineShareToContactFragment = MineShareToContactFragment.newInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_share_device, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null){
            presenter.start();
        }
    }

    private void initPresenter() {
        presenter = new MineShareDevicePresenterImp(this);
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
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_share_to_friends));
                AppLogger.e("tv_share_to_friends");
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, shareToRelativeAndFriendFragment, "shareToRelativeAndFriendFragment")
                        .addToBackStack("mineShareDeviceFragment")
                        .commit();
                alertDialog.dismiss();
            }
        });
        view.findViewById(R.id.tv_share_to_contract).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getView() != null)
                    ViewUtils.deBounceClick(getView().findViewById(R.id.tv_share_to_contract));
                AppLogger.e("tv_share_to_contract");
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .add(android.R.id.content, mineShareToContactFragment, "mineShareToContactFragment")
                        .addToBackStack("mineShareDeviceFragment")
                        .commit();
                alertDialog.dismiss();
            }
        });
        builder.setView(view);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void initRecycleView(MineShareDeviceAdapter adapter) {
        recycleShareDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        recycleShareDeviceList.setAdapter(adapter);
    }

    @Override
    public void jump2ShareDeviceMangerFragment(View itemView, int viewType, int position) {
        if (getView() != null)
            ViewUtils.deBounceClick(itemView);
        AppLogger.e("tv_share_device_manger");
        Bundle bundle = new Bundle();
        bundle.putParcelable("shareDeviceItem",presenter.getBean(position));
        mineDevicesShareManagerFragment = MineDevicesShareManagerFragment.newInstance(bundle);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(android.R.id.content, mineDevicesShareManagerFragment, "mineDevicesShareManagerFragment")
                .addToBackStack("mineShareDeviceFragment")
                .commit();
    }

    @OnClick(R.id.iv_home_mine_sharedevices_back)
    public void onClick() {

    }
}
