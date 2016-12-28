package com.cylan.jiafeigou.n.view.cam;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamMessageListPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.view.adapter.CamMessageListAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.wheel.WheelView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

;

/**
 * A simple {@link Fragment} subclass.
 */
public class CamMessageListFragment extends IBaseFragment<CamMessageListContract.Presenter>
        implements CamMessageListContract.View, SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener {


    @BindView(R.id.tv_cam_message_list_date)
    TextView tvCamMessageListDate;
    @BindView(R.id.tv_cam_message_list_edit)
    TextView tvCamMessageListEdit;
    @BindView(R.id.rv_cam_message_list)
    RecyclerView rvCamMessageList;
    @BindView(R.id.srLayout_cam_list_refresh)
    SwipeRefreshLayout srLayoutCamListRefresh;
    @BindView(R.id.tv_time_line_pop)
    TextView tvTimeLinePop;
    @BindView(R.id.wv_wonderful_timeline)
    WheelView wvWonderfulTimeline;
    @BindView(R.id.fLayout_cam_message_list_timeline)
    RelativeLayout fLayoutCamMessageListTimeline;
    /**
     * 列表第一条可见item的position,用户刷新timeLine控件的位置。
     */
    private int currentPosition = -1;
    private CamMessageListAdapter camMessageListAdapter;
    private String uuid;

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
        DeviceBean bean = getArguments().getParcelable(JConstant.KEY_DEVICE_ITEM_BUNDLE);
        this.uuid = bean.uuid;
        basePresenter = new CamMessageListPresenterImpl(this, bean.uuid);
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
        rvCamMessageList.setAdapter(camMessageListAdapter);
        rvCamMessageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                AppLogger.d("newState: " + newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final int fPos = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                setCurrentPosition(fPos);
            }
        });
        camMessageListAdapter.setOnclickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (basePresenter != null) basePresenter.fetchMessageList();
    }

    /**
     * 更新顶部时间导航器
     *
     * @param position
     */
    private void setCurrentPosition(int position) {
        if (currentPosition == position && position < 0 || position > camMessageListAdapter.getCount() - 1)
            return;
        currentPosition = position;
        if (getView() != null) getView().post(() -> {
            long time = camMessageListAdapter.getList().get(currentPosition).time;
            boolean isToday = TimeUtils.isToday(time);
            String content = String.format(TimeUtils.getSuperString(time) + "%s", isToday ? "(" + getString(R.string.DOOR_TODAY) + ")" : "");
            tvCamMessageListDate.setText(content);
            Log.d("simpleDateFormat", "simpleDateFormat: " + simpleDateFormat.format(new Date(time)));
        });
        AppLogger.d("fPos: " + position);
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss", Locale.getDefault());

    @Override
    public void onMessageListRsp(ArrayList<CamMessageBean> beanArrayList) {
        srLayoutCamListRefresh.setRefreshing(false);
        if (beanArrayList == null || beanArrayList.size() == 0) {
//            ToastUtil.showNegativeToast("没有数据");
            AppLogger.i("没有数据");
            return;
        }
        camMessageListAdapter.addAll(beanArrayList);
        setCurrentPosition(0);
    }

    @Override
    public ArrayList<CamMessageBean> getList() {
        return (ArrayList<CamMessageBean>) camMessageListAdapter.getList();
    }

    @Override
    public void deviceInfoChanged(int id, Object o) {
        final int lPos = ((LinearLayoutManager) rvCamMessageList.getLayoutManager())
                .findLastVisibleItemPosition();
        switch (id) {
            case DpMsgMap.ID_204_SDCARD_STORAGE:
                camMessageListAdapter.notifySdcardStatus(o != null && ((DpMsgDefine.SdStatus) o).hasSdcard,
                        lPos);
                break;
            case DpMsgMap.ID_222_SDCARD_SUMMARY:
                camMessageListAdapter.notifySdcardStatus(o != null && ((DpMsgDefine.SdcardSummary) o).hasSdcard,
                        lPos);
                break;
            case DpMsgMap.ID_201_NET:
                camMessageListAdapter.notifyDeviceOnlineState(o != null && ((DpMsgDefine.MsgNet) o).net != 0,
                        lPos);
                break;
        }

    }

    @Override
    public void setPresenter(CamMessageListContract.Presenter presenter) {
        this.basePresenter = presenter;
    }

    @Override
    public void onRefresh() {
        srLayoutCamListRefresh.setRefreshing(true);
        if (basePresenter != null)
            basePresenter.fetchMessageList();
    }

    @OnClick({R.id.tv_cam_message_list_date,
            R.id.tv_cam_message_list_edit})
    public void onBindClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cam_message_list_date:
                if (camMessageListAdapter.getCount() == 0)
                    return;
                boolean show = fLayoutCamMessageListTimeline.isShown();
                fLayoutCamMessageListTimeline.setVisibility(show ? View.GONE : View.VISIBLE);
                break;
            case R.id.tv_cam_message_list_edit:
                final int lPos = ((LinearLayoutManager) rvCamMessageList.getLayoutManager())
                        .findLastVisibleItemPosition();
                camMessageListAdapter.reverseMode(lPos);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cam_message_item_delete: {
                int position = ViewUtils.getParentAdapterPosition(rvCamMessageList, v, R.id.lLayout_cam_msg_container);
                ToastUtil.showNegativeToast("delete:?" + position);
            }
            break;
            case R.id.lLayout_cam_msg_container: {
                int position = ViewUtils.getParentAdapterPosition(rvCamMessageList, v, R.id.lLayout_cam_msg_container);
                camMessageListAdapter.markItemSelected(position);
            }
            break;
            case R.id.tv_to_live:
                ToastUtil.showToast("直播?");
                break;
        }
    }
}
