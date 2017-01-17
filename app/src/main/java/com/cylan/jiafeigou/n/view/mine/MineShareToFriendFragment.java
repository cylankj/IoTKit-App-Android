package com.cylan.jiafeigou.n.view.mine;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineShareToFriendContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineShareToFriendPresenterImp;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.jiafeigou.n.view.adapter.ShareToFriendsAdapter;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/13
 * 描述：
 */
public class MineShareToFriendFragment extends Fragment implements MineShareToFriendContract.View {


    @BindView(R.id.iv_mine_share_to_relative_friend_back)
    ImageView ivMineShareToRelativeFriendBack;
    @BindView(R.id.tv_mine_share_to_relative_friend_true)
    TextView tvMineShareToRelativeFriendTrue;
    @BindView(R.id.rcy_mine_share_to_relative_and_friend_list)
    RecyclerView rcyMineShareToRelativeAndFriendList;
    @BindView(R.id.ll_no_friend)
    LinearLayout llNoFriend;

    private MineShareToFriendContract.Presenter presenter;
    private ShareToFriendsAdapter shareToFriendsAdapter;
    private int hasShareNum;
    private int shareSucceedNum;
    private ArrayList<RelAndFriendBean> shareSucceedFriend = new ArrayList<>();

    private ArrayList<RelAndFriendBean> isChooseToShareList = new ArrayList<>();
    private DeviceBean deviceinfo;
    private ArrayList<RelAndFriendBean> hasSharefriend;

    private OnShareSucceedListener listener;

    public interface OnShareSucceedListener {
        void shareSucceed(int num, ArrayList<RelAndFriendBean> list);
    }

    public void setOnShareSucceedListener(OnShareSucceedListener listener) {
        this.listener = listener;
    }

    public static MineShareToFriendFragment newInstance(Bundle bundle) {
        MineShareToFriendFragment fragment = new MineShareToFriendFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_share_to_friend, container, false);
        ButterKnife.bind(this, view);
        getArgumentData();
        initPresenter();
        return view;
    }

    /**
     * 获取传递过来的参数
     */
    private void getArgumentData() {
        Bundle arguments = getArguments();
        deviceinfo = arguments.getParcelable("deviceinfo");
        hasSharefriend = arguments.getParcelableArrayList("hasSharefriend");
    }

    private void initPresenter() {
        presenter = new MineShareToFriendPresenterImp(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        //第一次进入以分享数显示灰色
        setHasShareFriendNum(false, hasSharefriend.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.getAllShareFriend(deviceinfo.uuid);
            presenter.start();
        }
    }

    @Override
    public void setPresenter(MineShareToFriendContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_mine_share_to_relative_friend_back, R.id.tv_mine_share_to_relative_friend_true})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_share_to_relative_friend_back:
                getFragmentManager().popBackStack();
                if (listener != null) {
                    listener.shareSucceed(shareSucceedNum, shareSucceedFriend);
                }
                break;

            case R.id.tv_mine_share_to_relative_friend_true:
                if (presenter.checkNetConnetion()) {
                    presenter.sendShareToFriendReq(deviceinfo.uuid, isChooseToShareList);
                } else {
                    ToastUtil.showNegativeToast(getString(R.string.Item_ConnectionFail));
                }
                break;
        }
    }

    @Override
    public void initRecycleView(ArrayList<RelAndFriendBean> list) {
        rcyMineShareToRelativeAndFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        shareToFriendsAdapter = new ShareToFriendsAdapter(getContext(), list, null);
        rcyMineShareToRelativeAndFriendList.setAdapter(shareToFriendsAdapter);
        initAdaListener();
    }

    @Override
    public void showNoFriendNullView() {
        llNoFriend.setVisibility(View.VISIBLE);
    }

    @Override
    public void setHasShareFriendNum(boolean isChange, int number) {
        if (number == 0) {
            tvMineShareToRelativeFriendTrue.setClickable(false);
            tvMineShareToRelativeFriendTrue.setText(getString(R.string.OK) + "（0/5）");
            tvMineShareToRelativeFriendTrue.setTextColor(Color.parseColor("#d8d8d8"));
        } else if (isChange) {
            tvMineShareToRelativeFriendTrue.setClickable(true);
            tvMineShareToRelativeFriendTrue.setTextColor(Color.WHITE);
            tvMineShareToRelativeFriendTrue.setText(getString(R.string.OK) + "（" + number + "/5）");
        } else {
            tvMineShareToRelativeFriendTrue.setClickable(false);
            tvMineShareToRelativeFriendTrue.setTextColor(Color.parseColor("#d8d8d8"));
            tvMineShareToRelativeFriendTrue.setText(getString(R.string.OK) + "（" + number + "/5）");
        }
    }

    @Override
    public void showShareAllSuccess() {
        //TODO 完善
        ToastUtil.showPositiveToast(getString(R.string.Tap3_ShareDevice_SuccessTips));
    }

    @Override
    public void showShareSomeFail(int some) {
        //TODO 完善
        showShareResultDialog(some + getString(R.string.Tap3_ShareDevice_Friends_FailTips));
    }

    @Override
    public void showShareAllFail() {
        //TODO 完善
        showShareResultDialog(getString(R.string.Tap3_ShareDevice_FailTips));
    }

    @Override
    public void showSendProgress() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
    }

    @Override
    public void hideSendProgress() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void showNumIsOverDialog(SuperViewHolder holder) {
        //当人数超过5人时选中 松开手之后弹起
        holder.setChecked(R.id.checkbox_is_share_check, false);
        ToastUtil.showToast(getString(R.string.Tap3_ShareDevice_Tips));
    }

    /**
     * 处理分享后的结果
     *
     * @param callbackList
     */
    @Override
    public void handlerAfterSendShareReq(ArrayList<RxEvent.ShareDeviceCallBack> callbackList) {
        hideSendProgress();
        int totalFriend = isChooseToShareList.size();
        Iterator iterators = isChooseToShareList.iterator();
        while (iterators.hasNext()) {
            RelAndFriendBean friendBean = (RelAndFriendBean) iterators.next();
            for (RxEvent.ShareDeviceCallBack callBack : callbackList) {
                if (friendBean.account.equals(callBack.account) && callBack.requestId == 0) {
                    iterators.remove();
                    shareSucceedNum++;
                    shareSucceedFriend.add(friendBean);
                }
            }
        }

        if (isChooseToShareList.size() == 0) {
            //全部分享成功
            showShareAllSuccess();
        } else if (isChooseToShareList.size() == totalFriend) {
            //全部分享失败
            showShareAllFail();
        } else {
            //部分分享失败
            showShareSomeFail(isChooseToShareList.size());
        }

    }

    /**
     * 弹出分享
     *
     * @param title
     */
    private void showShareResultDialog(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setPositiveButton(getString(R.string.TRY_AGAIN), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //TODO　部分分享失败 等待SDK返回的数据
                presenter.sendShareToFriendReq(deviceinfo.uuid, isChooseToShareList);
            }
        });
        builder.setNegativeButton(getString(R.string.MAGNETISM_OFF), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    /**
     * 列表的监听器
     */
    private void initAdaListener() {
        shareToFriendsAdapter.setOnShareCheckListener(new ShareToFriendsAdapter.OnShareCheckListener() {
            @Override
            public void onCheck(boolean isCheckFlag, SuperViewHolder holder, RelAndFriendBean item) {
                hasShareNum = hasSharefriend.size();
                boolean numIsChange = false;
                isChooseToShareList.clear();
                for (RelAndFriendBean bean : shareToFriendsAdapter.getList()) {
                    if (bean.isCheckFlag == 1) {
                        hasShareNum++;
                        numIsChange = true;
                        isChooseToShareList.add(bean);
                    }
                }
                presenter.checkShareNumIsOver(holder, numIsChange, hasShareNum);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if (presenter != null) {
            presenter.stop();
        }

        if (listener != null) {
            listener.shareSucceed(shareSucceedNum, shareSucceedFriend);
        }
    }
}
