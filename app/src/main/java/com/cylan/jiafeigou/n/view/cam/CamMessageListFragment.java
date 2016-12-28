package com.cylan.jiafeigou.n.view.cam;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;
import com.cylan.jiafeigou.widget.wheel.WheelView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.widget.dialog.BaseDialog.KEY_TITLE;
import static com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment.KEY_LEFT_CONTENT;
import static com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment.KEY_RIGHT_CONTENT;

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
    @BindView(R.id.fLayout_cam_msg_edit_bar)
    FrameLayout fLayoutCamMsgEditBar;
    @BindView(R.id.lLayout_no_message)
    LinearLayout lLayoutNoMessage;

    private SimpleDialogFragment simpleDialogFragment;
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
        if (basePresenter != null) basePresenter.fetchMessageList(false);
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
        });
    }


    @Override
    public void setRefresh(boolean refresh) {
        srLayoutCamListRefresh.setRefreshing(refresh);
    }

    @Override
    public void onMessageListRsp(ArrayList<CamMessageBean> beanArrayList) {
        srLayoutCamListRefresh.setRefreshing(false);
        final int count = beanArrayList == null ? 0 : beanArrayList.size();
        lLayoutNoMessage.post(() -> {
            lLayoutNoMessage.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
        });
        if (count == 0) {
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
            basePresenter.fetchMessageList(true);
    }

    @OnClick({R.id.tv_cam_message_list_date,
            R.id.tv_cam_message_list_edit,
            R.id.tv_msg_full_select,
            R.id.tv_msg_delete})
    public void onBindClick(View view) {
//        ViewUtils.deBounceClick(view);
        final int lPos = ((LinearLayoutManager) rvCamMessageList.getLayoutManager())
                .findLastVisibleItemPosition();
        switch (view.getId()) {
            case R.id.tv_cam_message_list_date:
                if (camMessageListAdapter.getCount() == 0)
                    return;//呼入呼出
                AnimatorUtils.slideAuto(fLayoutCamMessageListTimeline, false);
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
                if (initDialog()) {
                    simpleDialogFragment.show(getActivity().getSupportFragmentManager(), "simpleDialogFragment");
                    simpleDialogFragment.setAction((int id, Object value) -> {
                        ArrayList<CamMessageBean> list = new ArrayList<>(camMessageListAdapter.getSelectedItems());
                        camMessageListAdapter.removeAll(list);
                        if (basePresenter != null)
                            basePresenter.removeItems(list);
                        camMessageListAdapter.reverseMode(false, camMessageListAdapter.getCount());
                        AnimatorUtils.slideOut(fLayoutCamMsgEditBar, false);
                        tvCamMessageListEdit.setText(getString(R.string.EDIT_THEME));
                    });
                }
                break;
        }
    }

    /**
     * 初始化对话框
     *
     * @return
     */
    private boolean initDialog() {
        if (simpleDialogFragment == null) {
            Bundle bundle = new Bundle();
            bundle.putString(KEY_TITLE, getString(R.string.Tips_SureDelete));
            bundle.putString(KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
            bundle.putString(KEY_LEFT_CONTENT, getString(R.string.OK));
            simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
        }
        return !simpleDialogFragment.isResumed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_cam_message_item_delete: {//删除选中
                if (initDialog()) {
                    final int pos = ViewUtils.getParentAdapterPosition(rvCamMessageList, v,
                            R.id.lLayout_cam_msg_container);
                    simpleDialogFragment.show(getActivity().getSupportFragmentManager(), "simpleDialogFragment");
                    simpleDialogFragment.setAction((int id, Object value) -> {
                        camMessageListAdapter.remove(pos);
                        ArrayList<CamMessageBean> list = new ArrayList<>();
                        CamMessageBean bean = camMessageListAdapter.getItem(pos);
                        list.add(bean);
                        if (basePresenter != null)
                            basePresenter.removeItems(list);
                    });
                }
            }
            break;
            case R.id.lLayout_cam_msg_container: {//点击item,选中
                if (!camMessageListAdapter.isEditMode()) return;
                int position = ViewUtils.getParentAdapterPosition(rvCamMessageList, v,
                        R.id.lLayout_cam_msg_container);
                camMessageListAdapter.markItemSelected(position);
            }
            break;
            case R.id.tv_to_live:
                break;
        }
    }
}
