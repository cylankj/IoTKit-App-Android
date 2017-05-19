package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.AlertDialogManager;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineDevicesShareManagerContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineDevicesShareManagerPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.MineHasShareAdapter;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.CustomToolbar;
import com.cylan.jiafeigou.widget.LoadingDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/8
 * 描述：
 */
public class MineDevicesShareManagerFragment extends Fragment implements MineDevicesShareManagerContract.View, MineHasShareAdapter.OnCancleShareListenter {

    @BindView(R.id.recycler_had_share_relatives_and_friend)
    RecyclerView recyclerHadShareRelativesAndFriend;
    @BindView(R.id.tv_has_share_title)
    TextView tvHasShareTitle;
    @BindView(R.id.ll_no_share_friend)
    LinearLayout llNoShareFriend;
    @BindView(R.id.custom_toolbar)
    CustomToolbar customToolbar;


    private MineDevicesShareManagerContract.Presenter presenter;
    private MineHasShareAdapter hasShareAdapter;
    private DeviceBean devicebean;
    private RelAndFriendBean tempBean;
    private ArrayList<RelAndFriendBean> hasShareFriendlist;
    private int unShareSucNum = 0;
    private ArrayList<String> unShareAccount = new ArrayList<>();

    private OnUnShareChangeListener listener;

    public interface OnUnShareChangeListener {
        void unShareChange(int number, ArrayList<String> account);
    }

    public void setOncancleChangeListener(OnUnShareChangeListener listener) {
        this.listener = listener;
    }

    public static MineDevicesShareManagerFragment newInstance(Bundle bundle) {
        MineDevicesShareManagerFragment fragment = new MineDevicesShareManagerFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_device_share_manager, container, false);
        ButterKnife.bind(this, view);
        getIntentData();
        initPresenter();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.start();
        }
    }

    private void initPresenter() {
        presenter = new MineDevicesShareManagerPresenterImp(this, hasShareFriendlist);
    }

    @Override
    public void setPresenter(MineDevicesShareManagerContract.Presenter presenter) {

    }

    @Override
    public String getUuid() {
        return null;
    }

    @OnClick(R.id.tv_toolbar_icon)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getFragmentManager().popBackStack();
                break;
        }
    }

    public void getIntentData() {
        Bundle arguments = getArguments();
        devicebean = arguments.getParcelable("devicebean");
        hasShareFriendlist = arguments.getParcelableArrayList("friendlist");
        setTopTitle(TextUtils.isEmpty(devicebean.alias) ? devicebean.uuid : devicebean.alias);
    }

    @Override
    public void showHasShareListTitle() {
        tvHasShareTitle.setVisibility(View.VISIBLE);
        recyclerHadShareRelativesAndFriend.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideHasShareListTitle() {
        tvHasShareTitle.setVisibility(View.INVISIBLE);
        recyclerHadShareRelativesAndFriend.setVisibility(View.INVISIBLE);
    }

    /**
     * desc:初始化列表显示
     *
     * @param list
     */
    @Override
    public void initHasShareFriendRecyView(ArrayList<RelAndFriendBean> list) {
        recyclerHadShareRelativesAndFriend.setLayoutManager(new LinearLayoutManager(getContext()));
        hasShareAdapter = new MineHasShareAdapter(getView().getContext(), list, null);
        recyclerHadShareRelativesAndFriend.setAdapter(hasShareAdapter);
        initAdaListener();
    }

    /**
     * 设置列表的监听器
     */
    private void initAdaListener() {
        hasShareAdapter.setOnCancleShareListenter(this);
    }

    @Override
    public void showNoHasShareFriendNullView() {
        llNoShareFriend.setVisibility(View.VISIBLE);
    }

    @Override
    public void showCancleShareDialog(final RelAndFriendBean bean) {
        AlertDialog.Builder builder = AlertDialogManager.getInstance().getCustomDialog(getActivity());
        builder.setTitle(getString(R.string.Tap3_ShareDevice_CancleShare));
        builder.setPositiveButton(getString(R.string.DELETE), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                presenter.cancleShare(devicebean.uuid, bean);
            }
        });
        builder.setNegativeButton(getString(R.string.CANCEL), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialogManager.getInstance().showDialog("showCancleShareDialog", getActivity(), builder);
    }

    @Override
    public void showCancleShareProgress() {
        if (getFragmentManager() != null) {
            LoadingDialog.showLoading(getFragmentManager(), ContextUtils.getContext().getString(R.string.LOADING));
        }
    }

    @Override
    public void hideCancleShareProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }

        if (listener != null) {
            listener.unShareChange(unShareSucNum, unShareAccount);
        }
    }

    @Override
    public void onCancleShare(RelAndFriendBean item) {
        tempBean = item;
        if (getView() != null) {
            if (NetUtils.getNetType(getContext()) == -1) {
                ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
                return;
            }
            showCancleShareDialog(item);
        }
    }

    @Override
    public void deleteItems() {
        hasShareAdapter.remove(tempBean);
        hasShareAdapter.notifyDataSetHasChanged();
    }

    /**
     * 取消分享的结果
     *
     * @param result
     */
    @Override
    public void showUnShareResult(RxEvent.UnshareDeviceCallBack result) {
        if (result.i == JError.ErrorOK) {
            ToastUtil.showToast(getString(R.string.Tap3_ShareDevice_DeleteSucces));
            deleteItems();
            unShareSucNum++;
            unShareAccount.add(result.account);
        } else {
            ToastUtil.showToast(getString(R.string.Tap3_ShareDevice_CancelShareTips));
            return;
        }

        if (hasShareAdapter.getItemCount() == 0) {
            hideHasShareListTitle();
            showNoHasShareFriendNullView();
        }
    }

    /**
     * 顶部标题
     *
     * @param name
     */
    @Override
    public void setTopTitle(String name) {
        customToolbar.setToolbarLeftTitle(name);
    }

    /**
     * 网络状态变化
     *
     * @param state
     */
    @Override
    public void onNetStateChanged(int state) {
        if (state == -1) {
            hideCancleShareProgress();
            ToastUtil.showNegativeToast(getString(R.string.NO_NETWORK_1));
        }
    }


}
