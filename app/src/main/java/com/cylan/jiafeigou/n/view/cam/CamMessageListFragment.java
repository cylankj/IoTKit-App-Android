package com.cylan.jiafeigou.n.view.cam;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.Convertor;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamMessageListPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.view.adapter.CamMessageListAdapter;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.wheel.WheelView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

;

/**
 * A simple {@link Fragment} subclass.
 */
public class CamMessageListFragment extends IBaseFragment<CamMessageListContract.Presenter>
        implements CamMessageListContract.View, SwipeRefreshLayout.OnRefreshListener {


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
    private int currentPosition = 0;
    private CamMessageListAdapter camMessageListAdapter;

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
        basePresenter = new CamMessageListPresenterImpl(this, Convertor.convert(bean));
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
        srLayoutCamListRefresh.setOnRefreshListener(this);
        camMessageListAdapter = new CamMessageListAdapter(getContext(), null, null);
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
    }


    private void setCurrentPosition(int position) {
        if (currentPosition == position)
            return;
        currentPosition = position;
        AppLogger.d("fPos: " + position);
    }

    @OnClick({R.id.tv_cam_message_list_date, R.id.tv_cam_message_list_edit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cam_message_list_date:
                if (camMessageListAdapter.getCount() == 0)
                    return;
                boolean show = fLayoutCamMessageListTimeline.isShown();
                fLayoutCamMessageListTimeline.setVisibility(show ? View.GONE : View.VISIBLE);
//                ViewUtils.s
                break;
            case R.id.tv_cam_message_list_edit:

                break;
        }
    }

    @Override
    public void onMessageListRsp(ArrayList<CamMessageBean> beanArrayList) {
        srLayoutCamListRefresh.setRefreshing(false);
        if (beanArrayList == null || beanArrayList.size() == 0) {
//            ToastUtil.showNegativeToast("没有数据");
            AppLogger.i("没有数据");
            return;
        }
        camMessageListAdapter.addAll(beanArrayList);
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
}
