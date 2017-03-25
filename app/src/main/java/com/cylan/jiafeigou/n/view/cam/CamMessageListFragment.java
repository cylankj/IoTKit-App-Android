package com.cylan.jiafeigou.n.view.cam;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamMessageListPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.n.view.activity.CamSettingActivity;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.adapter.CamMessageListAdapter;
import com.cylan.jiafeigou.n.view.media.CamMediaActivity;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.n.mvp.contract.cam.CamLiveContract.TYPE_HISTORY;
import static com.cylan.jiafeigou.n.view.media.CamMediaActivity.KEY_BUNDLE;
import static com.cylan.jiafeigou.n.view.media.CamMediaActivity.KEY_INDEX;
import static com.cylan.jiafeigou.n.view.media.CamMediaActivity.KEY_TIME;
import static com.cylan.jiafeigou.support.photoselect.helpers.Constants.REQUEST_CODE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CamMessageListFragment extends IBaseFragment<CamMessageListContract.Presenter>
        implements CamMessageListContract.View, SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener, OnItemClickListener {


    @BindView(R.id.tv_cam_message_list_date)
    TextView tvCamMessageListDate;
    @BindView(R.id.tv_cam_message_list_edit)
    TextView tvCamMessageListEdit;
    @BindView(R.id.rv_cam_message_list)
    RecyclerView rvCamMessageList;
    @BindView(R.id.srLayout_cam_list_refresh)
    SwipeRefreshLayout srLayoutCamListRefresh;
    @BindView(R.id.fLayout_cam_message_list_timeline)
    WonderIndicatorWheelView fLayoutCamMessageListTimeline;
    @BindView(R.id.fLayout_cam_msg_edit_bar)
    FrameLayout fLayoutCamMsgEditBar;
    @BindView(R.id.lLayout_no_message)
    LinearLayout lLayoutNoMessage;
    @BindView(R.id.rLayout_cam_message_list_top)
    FrameLayout rLayoutCamMessageListTop;

//    private SimpleDialogFragment simpleDialogFragment;
    /**
     * 列表第一条可见item的position,用户刷新timeLine控件的位置。
     */
    private int currentPosition = -1;
    private CamMessageListAdapter camMessageListAdapter;
    private String uuid;

    /**
     * 加载更多
     */
    private boolean endlessLoading = false;
    private boolean mIsLastLoadFinish = true;

    public CamMessageListFragment() {
        // Required empty public constructor
    }

    public static CamMessageListFragment newInstance(Bundle bundle) {
        CamMessageListFragment fragment = new CamMessageListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.uuid = getArguments().getString(JConstant.KEY_DEVICE_ITEM_UUID);
        basePresenter = new CamMessageListPresenterImpl(this, uuid);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cam_message_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        srLayoutCamListRefresh.setColorSchemeColors(R.color.COLOR_CACACA);
        srLayoutCamListRefresh.setOnRefreshListener(this);
        camMessageListAdapter = new CamMessageListAdapter(this.uuid, getContext(), null, null);
        rvCamMessageList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
//        camMessageListAdapter.setOnItemClickListener(this);
        rvCamMessageList.setAdapter(camMessageListAdapter);
        rvCamMessageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int pastVisibleItems, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final int fPos = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                setCurrentPosition(fPos);
                if (dy > 0) { //check for scroll down
                    visibleItemCount = camMessageListAdapter.getLayoutManager().getChildCount();
                    totalItemCount = camMessageListAdapter.getLayoutManager().getItemCount();
                    pastVisibleItems = ((LinearLayoutManager) camMessageListAdapter.getLayoutManager()).findFirstVisibleItemPosition();
                    if (!endlessLoading && mIsLastLoadFinish) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            endlessLoading = true;
                            mIsLastLoadFinish = false;
                            Log.d("tag", "tag.....load more");
                            startRequest(false);
                        }
                    }
                }
            }
        });
        camMessageListAdapter.setOnclickListener(this);
    }

    private void setupFootView() {
        CamMessageBean bean = new CamMessageBean();
        bean.viewType = 2;
        if (camMessageListAdapter.getCount() > 0)
            bean.time = camMessageListAdapter.getItem(camMessageListAdapter.getCount() - 1).time;
        camMessageListAdapter.add(bean);
    }

    @Override
    public void onStart() {
        super.onStart();
        //刚刚进入页面，尽量少点加载

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && basePresenter != null && getActivity() != null && isResumed()) {
            if (camMessageListAdapter.getCount() == 0)
                startRequest(true);
        }
    }

    /**
     * @param asc：true:向前，false:向后(loadMore)
     */
    private void startRequest(boolean asc) {
        long time = 0;
        if (asc) {
            srLayoutCamListRefresh.setRefreshing(true);
            if (camMessageListAdapter.getCount() > 0)
                time = camMessageListAdapter.getItem(0).time;
        } else {
            if (!camMessageListAdapter.hasFooter())
                setupFootView();
            if (camMessageListAdapter.getCount() > 0)
                time = camMessageListAdapter.getItem(camMessageListAdapter.getCount() - 1).time;
        }
        if (basePresenter != null) basePresenter.fetchMessageList(time, asc);
    }

    /**
     * 更新顶部时间导航器
     *
     * @param position
     */
    private void setCurrentPosition(int position) {
        if (currentPosition == position || position < 0 || position > camMessageListAdapter.getCount() - 1)
            return;
        currentPosition = position;
        if (getView() != null) getView().post(() -> {
            long time = camMessageListAdapter.getList().get(currentPosition).time;
            boolean isToday = TimeUtils.isToday(time);
            String content = String.format(TimeUtils.getSuperString(time) + "%s", isToday ? "(" + getString(R.string.DOOR_TODAY) + ")" : "");
            tvCamMessageListDate.setText(content);
        });
    }


    @Override
    public void onDateMapRsp(List<WonderIndicatorWheelView.WheelItem> dateMap) {
        fLayoutCamMessageListTimeline.init(dateMap);
        if (!fLayoutCamMessageListTimeline.hasInit())
            fLayoutCamMessageListTimeline.setListener(time -> {
                AppLogger.d("scroll date： " + TimeUtils.getDayInMonth(time));
            });
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void onListAppend(ArrayList<CamMessageBean> beanArrayList) {
        endlessLoading = false;
        mIsLastLoadFinish = true;
        srLayoutCamListRefresh.setRefreshing(false);
        final int count = beanArrayList == null ? 0 : beanArrayList.size();
        if (count == 0) {
            AppLogger.i("没有数据");
            return;
        }
        camMessageListAdapter.addAll(beanArrayList);
        setCurrentPosition(0);
        lLayoutNoMessage.post(() -> {
            lLayoutNoMessage.setVisibility(camMessageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
            rLayoutCamMessageListTop.setVisibility(camMessageListAdapter.getCount() == 0 ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onListInsert(ArrayList<CamMessageBean> beanArrayList, int position) {
        if (beanArrayList != null && beanArrayList.size() > 0) {
            for (CamMessageBean bean : beanArrayList)
                camMessageListAdapter.add(0, bean);
        }
        endlessLoading = false;
        mIsLastLoadFinish = true;
        srLayoutCamListRefresh.setRefreshing(false);
        lLayoutNoMessage.post(() -> {
            lLayoutNoMessage.setVisibility(camMessageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
            rLayoutCamMessageListTop.setVisibility(camMessageListAdapter.getCount() == 0 ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public ArrayList<CamMessageBean> getList() {
        return (ArrayList<CamMessageBean>) camMessageListAdapter.getList();
    }

    @Override
    public void deviceInfoChanged(int id, JFGDPMsg o) throws IOException {
        final int lPos = ((LinearLayoutManager) rvCamMessageList.getLayoutManager())
                .findLastVisibleItemPosition();
        switch (id) {
            case DpMsgMap.ID_201_NET:
                DpMsgDefine.DPNet net = DpUtils.unpackData(o.packValue, DpMsgDefine.DPNet.class);
                if (net == null) net = DpMsgDefine.EMPTY.NET;
                camMessageListAdapter.notifyDeviceOnlineState(net.net > 0, lPos);
                break;
            case DpMsgMap.ID_222_SDCARD_SUMMARY:
                DpMsgDefine.DPSdcardSummary summary = DpUtils.unpackData(o.packValue, DpMsgDefine.DPSdcardSummary.class);
                if (summary == null) summary = DpMsgDefine.EMPTY.SDCARD_SUMMARY;
                camMessageListAdapter.notifySdcardStatus(summary.hasSdcard, lPos);
                CamMessageBean bean = new CamMessageBean();
                bean.sdcardSummary = summary;
                bean.id = DpMsgMap.ID_222_SDCARD_SUMMARY;
                bean.version = bean.time = o.version;
                camMessageListAdapter.add(0, bean);
                rvCamMessageList.scrollToPosition(0);
                lLayoutNoMessage.setVisibility(View.GONE);
                break;
        }

    }

    @Override
    public void onErr() {
        srLayoutCamListRefresh.post(() -> srLayoutCamListRefresh.setRefreshing(false));
        if (getView() != null && getActivity() != null) {
            getView().postDelayed(() -> LoadingDialog.dismissLoading(getFragmentManager()), 100);
        }
    }

    @Override
    public void onMessageDeleteSuc() {
        ToastUtil.showPositiveToast(getString(R.string.DELETED_SUC));
        if (getView() != null && getActivity() != null) {
            getView().postDelayed(() -> LoadingDialog.dismissLoading(getFragmentManager()), 100);
        }
    }

    @Override
    public void loadingDismiss() {
        if (camMessageListAdapter.hasFooter() && getView() != null)
            camMessageListAdapter.remove(camMessageListAdapter.getItemCount() - 1);
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void setPresenter(CamMessageListContract.Presenter presenter) {
        this.basePresenter = presenter;
    }

    @Override
    public void onRefresh() {
        if (NetUtils.getJfgNetType(getContext()) == 0) {
            srLayoutCamListRefresh.setRefreshing(false);
            ToastUtil.showToast(getString(R.string.NoNetworkTips));
            return;
        }
        srLayoutCamListRefresh.setRefreshing(true);
        startRequest(true);
    }

    @OnClick({R.id.tv_cam_message_list_date,
            R.id.tv_cam_message_list_edit,
            R.id.tv_msg_full_select,
            R.id.tv_msg_delete})
    public void onBindClick(View view) {
        final int lPos = ((LinearLayoutManager) rvCamMessageList.getLayoutManager())
                .findLastVisibleItemPosition();
        switch (view.getId()) {
            case R.id.tv_cam_message_list_date:
                if (camMessageListAdapter.getCount() == 0)
                    return;//呼入呼出
                if (basePresenter != null && basePresenter.getDateList().size() == 0) {
                    LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
                    AppLogger.d("日起加载中...");
                    basePresenter.refreshDateList();
                }
                boolean reset = tvCamMessageListDate.getTag() == null ||
                        ((int) tvCamMessageListDate.getTag() == R.drawable.wonderful_arrow_down);
                tvCamMessageListDate.setTag(reset ? R.drawable.wonderful_arrow_up : R.drawable.wonderful_arrow_down);
                ViewUtils.setDrawablePadding(tvCamMessageListDate, reset ? R.drawable.wonderful_arrow_up : R.drawable.wonderful_arrow_down, 2);
                if (reset)
                    AnimatorUtils.slideIn(fLayoutCamMessageListTimeline, false);
                else AnimatorUtils.slideOut(fLayoutCamMessageListTimeline, false);
                tvCamMessageListEdit.setEnabled(!reset);
                break;
            case R.id.tv_cam_message_list_edit:
                if (camMessageListAdapter.getCount() == 0) return;
                String content = ((TextView) view).getText().toString();
                boolean toEdit = TextUtils.equals(content, getString(R.string.EDIT_THEME));
                camMessageListAdapter.reverseMode(toEdit, lPos);
                if (camMessageListAdapter.isEditMode())
                    AnimatorUtils.slideIn(fLayoutCamMsgEditBar, false);
                else {
                    AnimatorUtils.slideOut(fLayoutCamMsgEditBar, false);
                }
                ((TextView) view).setText(getString(toEdit ? R.string.CANCEL
                        : R.string.EDIT_THEME));
                break;
            case R.id.tv_msg_full_select://全选
                camMessageListAdapter.markAllAsSelected(true, lPos);
                break;
            case R.id.tv_msg_delete://删除
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.Tips_SureDelete))
                        .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                            ArrayList<CamMessageBean> list = new ArrayList<>(camMessageListAdapter.getSelectedItems());
                            camMessageListAdapter.removeAll(list);
                            if (basePresenter != null)
                                basePresenter.removeItems(list);
                            camMessageListAdapter.reverseMode(false, camMessageListAdapter.getCount());
                            AnimatorUtils.slideOut(fLayoutCamMsgEditBar, false);
                            tvCamMessageListEdit.setText(getString(R.string.EDIT_THEME));
                            LoadingDialog.showLoading(getFragmentManager(), getString(R.string.DELETEING));
                        })
                        .setNegativeButton(getString(R.string.CANCEL), null)
                        .create().show();
                break;
        }
    }


    @Override
    public void onClick(View v) {
        final int position = ViewUtils.getParentAdapterPosition(rvCamMessageList, v,
                R.id.lLayout_cam_msg_container);
        switch (v.getId()) {
            case R.id.tv_cam_message_item_delete: {//删除选中
                new AlertDialog.Builder(getActivity())
                        .setMessage(getString(R.string.Tips_SureDelete))
                        .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                            ArrayList<CamMessageBean> list = new ArrayList<>(camMessageListAdapter.getSelectedItems());
                            if (camMessageListAdapter.getCount() > position) {
                                list.add(camMessageListAdapter.getItem(position));
                            }
                            camMessageListAdapter.removeAll(list);
                            if (basePresenter != null)
                                basePresenter.removeItems(list);
                            LoadingDialog.showLoading(getFragmentManager(), getString(R.string.DELETEING));
                        })
                        .setNegativeButton(getString(R.string.CANCEL), null)
                        .create().show();
            }
            break;
            case R.id.lLayout_cam_msg_container: {//点击item,选中
                if (!camMessageListAdapter.isEditMode()) return;
                camMessageListAdapter.markItemSelected(position);
            }
            break;
            case R.id.imgV_cam_message_pic_0:
                startActivity(getIntent(position, 0));
                break;
            case R.id.imgV_cam_message_pic_1:
                startActivity(getIntent(position, 1));
                break;
            case R.id.imgV_cam_message_pic_2:
                startActivity(getIntent(position, 2));
                break;
            case R.id.tv_jump_next: {
                try {
                    CamMessageBean bean = camMessageListAdapter.getItem(position);
                    boolean jumpNext = bean != null && bean.alarmMsg != null && bean.sdcardSummary == null;
                    if (jumpNext) {
                        Activity activity = getActivity();
                        if (activity != null && activity instanceof CameraLiveActivity) {
                            Bundle bundle = new Bundle();
                            bundle.putInt(JConstant.KEY_CAM_LIVE_PAGE_PLAY_TYPE, TYPE_HISTORY);
                            bundle.putLong(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME, bean.alarmMsg.time);
                            ((CameraLiveActivity) activity).setCurrentBundle(bundle);
                        }
                        AppLogger.d("alarm: " + bean);
                    } else {
                        Intent intent = new Intent(getActivity(), CamSettingActivity.class);
                        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
                        intent.putExtra(JConstant.KEY_JUMP_TO_CAM_DETAIL, true);
                        startActivityForResult(intent, REQUEST_CODE,
                                ActivityOptionsCompat.makeCustomAnimation(getActivity(),
                                        R.anim.slide_in_right, R.anim.slide_out_left).toBundle());
                    }
                    AppLogger.d("jump next: " + jumpNext);
                } catch (Exception e) {
                    AppLogger.e("err: " + e.getLocalizedMessage());
                }
                break;
            }
        }
    }

    private Intent getIntent(int position, int index) {
        Intent intent = new Intent(getActivity(), CamMediaActivity.class);
        intent.putExtra(KEY_INDEX, index);
        intent.putExtra(KEY_BUNDLE, camMessageListAdapter.getItem(position).alarmMsg);
        intent.putExtra(KEY_TIME, camMessageListAdapter.getItem(position).time);
        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
        Log.d("imgV_cam_message_pic_0", "imgV_cam_:" + position + " " + camMessageListAdapter.getItem(position).alarmMsg);
        return intent;
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {

    }
}
