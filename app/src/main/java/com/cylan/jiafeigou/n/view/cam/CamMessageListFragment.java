package com.cylan.jiafeigou.n.view.cam;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamMessageListContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamMessageListPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.jiafeigou.n.view.activity.CamSettingActivity;
import com.cylan.jiafeigou.n.view.activity.CameraLiveActivity;
import com.cylan.jiafeigou.n.view.adapter.CamMessageListAdapter;
import com.cylan.jiafeigou.n.view.cam.item.FaceItem;
import com.cylan.jiafeigou.n.view.media.CamMediaActivity;
import com.cylan.jiafeigou.n.view.panorama.PanoramaDetailActivity;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.server.cache.CacheHolderKt;
import com.cylan.jiafeigou.server.cache.KeyValueStringItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.objectbox.Box;

import static com.cylan.jiafeigou.n.view.media.CamMediaActivity.KEY_BUNDLE;
import static com.cylan.jiafeigou.n.view.media.CamMediaActivity.KEY_INDEX;
import static com.cylan.jiafeigou.support.photoselect.helpers.Constants.REQUEST_CODE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CamMessageListFragment extends IBaseFragment<CamMessageListContract.Presenter>
        implements CamMessageListContract.View, SwipeRefreshLayout.OnRefreshListener,
        View.OnClickListener, OnItemClickListener {


    private static final int BAR_TYPE_FACE_COMMON = 0;
    private static final int BAR_TYPE_NORMAL = 1;
    private static final int BAR_TYPE_STRANGER = 2;
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
    NestedScrollView lLayoutNoMessage;
    @BindView(R.id.rLayout_cam_message_list_top)
    RelativeLayout rLayoutCamMessageListTop;
    @BindView(R.id.tv_msg_full_select)
    TextView tvMsgFullSelect;
    @BindView(R.id.tv_msg_delete)
    TextView tvMsgDelete;
    @BindView(R.id.apl_cam_message_appbar_layout)
    AppBarLayout aplCamMessageAppbar;
    @BindView(R.id.iv_cam_message_arrow)
    ImageView arrow;
    @BindView(R.id.iv_back)
    TextView barBack;
    @BindView(R.id.c_layout_parent)
    CoordinatorLayout parent;
    @BindView(R.id.cl_header_container)
    ViewGroup clHeaderContainer;
    /**
     * 列表第一条可见item的position,用户刷新timeLine控件的位置。
     */
    private CamMessageListAdapter camMessageListAdapter;
    /**
     * 加载更多
     */
    private boolean endlessLoading = false;
    private boolean mIsLastLoadFinish = true;
    private LinearLayoutManager layoutManager;
    private VisitorListFragmentV2 visitorFragment;

    private boolean hasFaceHeader = false;
    private int pageType = FaceItem.FACE_TYPE_ALL;
    private String personId;

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
        presenter = new CamMessageListPresenterImpl(this, uuid);
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
        if (mockView != null) {
            this.tvCamMessageListEdit.setVisibility(View.INVISIBLE);
            this.tvCamMessageListEdit = mockView;
        }
        this.tvCamMessageListEdit.setEnabled(false);
        srLayoutCamListRefresh.setColorSchemeColors(getResources().getColor(R.color.color_36BDFF));
        srLayoutCamListRefresh.setOnRefreshListener(this);
        camMessageListAdapter = new CamMessageListAdapter(this.uuid, getContext(), null, null);
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Exception e) {
                    AppLogger.e("homepageList" + e.getMessage());
                }
            }
        };
        rvCamMessageList.setLayoutManager(layoutManager);
        rvCamMessageList.setAdapter(camMessageListAdapter);
        rvCamMessageList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                if (visibleItemPosition >= 0) {
                    List<CamMessageBean> beanList = camMessageListAdapter.getList();
                    if (visibleItemPosition < beanList.size()) {
                        CamMessageBean bean = beanList.get(visibleItemPosition);
                        if (visibleItemPosition >= 0) {
                            setCurrentPosition(visibleItemPosition);
                        }
                    }

                    if (dy > 0) { //check for scroll down
                        visibleItemCount = layoutManager.getChildCount();
                        totalItemCount = layoutManager.getItemCount();
                        if (!endlessLoading && mIsLastLoadFinish) {
                            if ((visibleItemCount + visibleItemPosition) >= totalItemCount) {
                                endlessLoading = true;
                                mIsLastLoadFinish = false;
                                Log.d("tag", "tag.....load more");
                                startRequest(false, true);
                            }
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //TODO: 2017/10/17  更新refresh可用性

                }
            }
        });
        camMessageListAdapter.setOnclickListener(this);
        tvCamMessageListEdit.setVisibility(JFGRules.isShareDevice(uuid) && !JFGRules.isPan720(getDevice().pid) ? View.INVISIBLE : View.VISIBLE);
        hasFaceHeader = JFGRules.isFaceFragment(getDevice().pid);
        initFaceHeader();

    }

    private void refreshFaceHeader() {
        if (visitorFragment != null) {
            visitorFragment.refreshContent();
        }
    }

    private void initFaceHeader() {
        if (hasFaceHeader) {
            aplCamMessageAppbar.addOnOffsetChangedListener(this::onMessageAppbarScrolled);
            tvCamMessageListDate.setClickable(false);
            layoutBarMenu(BAR_TYPE_FACE_COMMON);
            aplCamMessageAppbar.setExpanded(true, false);

            if (visitorFragment != null) {
                return;//do nothing
            }

            visitorFragment = VisitorListFragmentV2.Companion.newInstance(uuid());
            visitorFragment.setItemClickListener(new VisitorListFragmentV2.ItemClickListener() {
                @Override
                public void itemClick(@NotNull FaceItem item, int globalPosition, int position, int pageIndex) {
                    changeContentByHeaderClick(item);
                }

                @Override
                public void itemLongClick(int globalPosition, int _p, @NotNull View _v, int faceType, int pageIndex) {

                }
            });
            visitorFragment.setVisitorReadyListener(new VisitorListFragmentV2.VisitorReadyListener() {
                @Override
                public void onStrangerVisitorReady(@NotNull List<FaceItem> visitorList) {

                    camMessageListAdapter.onStrangerInformationReady(visitorList);
                    if (visitorList.size() > 0) {
                        FaceItem faceItem = visitorList.get(0);
                        if (faceItem.getStrangerVisitor() != null) {
                            personId = faceItem.getStrangerVisitor().faceId;
                            startRequest(true, true);
                        }
                    }

                }

                @Override
                public void onVisitorReady(@NotNull List<FaceItem> visitorList) {
                    camMessageListAdapter.onVisitorInformationReady(visitorList);
                }
            });
            //显示 所有面孔列表
            ActivityUtils.replaceFragment(getFragmentManager(),
                    visitorFragment, R.id.fLayout_message_face, "visitorFragment", false);

        } else {
//            ViewUtils.setDrawablePadding(tvCamMessageListDate, R.drawable.wonderful_arrow_down, 2);

            arrow.setVisibility(View.GONE);
            aplCamMessageAppbar.setExpanded(false);
            layoutBarMenu(BAR_TYPE_NORMAL);
            rLayoutCamMessageListTop.setVisibility(View.GONE);
        }
    }

    private void layoutBarMenu(int barType) {

        camMessageListAdapter.clear();
        if (barType == BAR_TYPE_FACE_COMMON) {
            barBack.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams;
            if (tvCamMessageListEdit.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                layoutParams = (RelativeLayout.LayoutParams) tvCamMessageListEdit.getLayoutParams();
                layoutParams.addRule(RelativeLayout.START_OF, R.id.iv_cam_message_arrow);
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
                tvCamMessageListEdit.setLayoutParams(layoutParams);
            }
            rLayoutCamMessageListTop.setVisibility(View.VISIBLE);
        } else if (barType == BAR_TYPE_NORMAL) {
            barBack.setVisibility(View.GONE);
            RelativeLayout.LayoutParams layoutParams;
            if (tvCamMessageListEdit.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                layoutParams = (RelativeLayout.LayoutParams) tvCamMessageListEdit.getLayoutParams();
                layoutParams.removeRule(RelativeLayout.START_OF);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                tvCamMessageListEdit.setLayoutParams(layoutParams);
            }
            rLayoutCamMessageListTop.setVisibility(View.GONE);
        } else if (barType == BAR_TYPE_STRANGER) {
            barBack.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams;
            if (tvCamMessageListEdit.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                layoutParams = (RelativeLayout.LayoutParams) tvCamMessageListEdit.getLayoutParams();
                layoutParams.addRule(RelativeLayout.START_OF, R.id.iv_cam_message_arrow);
                layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
                tvCamMessageListEdit.setLayoutParams(layoutParams);
            }
            rLayoutCamMessageListTop.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.iv_back)
    public void clickStrangerBack() {
        AppLogger.w("clickStrangerBack");
        this.pageType = FaceItem.FACE_TYPE_ALL;
        layoutBarMenu(BAR_TYPE_FACE_COMMON);
        if (visitorFragment != null) {
            visitorFragment.exitStranger();
        }
        presenter.fetchMessageListByFaceId(0, true, true);
        exitEditMode();
        AppLogger.d("还需要重新选中All");
    }

    public boolean handleViewPagerState() {
        return false;
    }

    private void changeContentByHeaderClick(FaceItem faceItem) {
        int faceType = faceItem.getFaceType();
        this.pageType = faceType;
        camMessageListAdapter.clear();
        lLayoutNoMessage.setVisibility(View.VISIBLE);
        if (faceType == FaceItem.FACE_TYPE_STRANGER) {
//            // TODO: 2017/10/10 点击了陌生人,需要刷新陌生人列表
//            this.personId = " ";
//            startRequest(true, false);
            layoutBarMenu(BAR_TYPE_STRANGER);

        } else if (faceType == FaceItem.FACE_TYPE_ACQUAINTANCE) {
            // TODO: 2017/10/10 点击的是熟人,但具体是哪个人还不知道
            layoutBarMenu(BAR_TYPE_FACE_COMMON);
            DpMsgDefine.Visitor visitor = faceItem.getVisitor();
            if (visitor != null) {
                this.personId = visitor.personId;
                startRequest(true, false);
            } else {
                AppLogger.w("personid is null");
            }
        } else if (faceType == FaceItem.FACE_TYPE_ALL) {
            // TODO: 2017/10/10 点击的是全部 ,需要刷新所有
            layoutBarMenu(BAR_TYPE_FACE_COMMON);
            startRequest(true, false);
        } else if (faceType == FaceItem.FACE_TYPE_STRANGER_SUB) {
            DpMsgDefine.StrangerVisitor visitor = faceItem.getStrangerVisitor();
            this.personId = visitor.faceId;
            layoutBarMenu(BAR_TYPE_STRANGER);
            if (visitor != null) {
                startRequest(true, false);
            } else {
                AppLogger.w("personid is null");
            }
        }

    }

    private void onMessageAppbarScrolled(AppBarLayout appBarLayout, int offset) {
        Log.i("onMessageAppbarScrolled", "offset is:" + offset + ",total is:" + appBarLayout.getTotalScrollRange());
        srLayoutCamListRefresh.setEnabled(offset == 0 && !rvCamMessageList.canScrollVertically(-1));
        if (Math.abs(offset) == appBarLayout.getTotalScrollRange()) {
            // TODO: 2017/9/29 更新箭头
            arrow.setImageResource(R.drawable.btn_put_away);
        } else {
            arrow.setImageResource(R.drawable.btn_unfolded);
        }
    }

    private void setupFootView() {
        CamMessageBean bean = new CamMessageBean();
        bean.viewType = CamMessageBean.ViewType.FOOT;
        if (camMessageListAdapter.getCount() > 0) {
//            bean.version = camMessageListAdapter.getItem(camMessageListAdapter.getCount() - 1).version + 1;
        }
        rvCamMessageList.post(() -> camMessageListAdapter.add(bean));
    }

    @Override
    public void onStart() {
        super.onStart();
        //从通知栏跳进来
        boolean needRefresh = false;
        if (getActivity() != null && getActivity().getIntent() != null) {
            if (getActivity().getIntent().hasExtra(JConstant.KEY_JUMP_TO_MESSAGE)) {//需要每次进来都刷新数据,
                //客户端绑定门铃一代设备之后，门铃按呼叫键呼叫生成呼叫记录；
                //客户端进入“功能设置”界面点击“清空呼叫记录”，之后返回门铃的“消息”界面，呼叫记录依旧存在；iOS客户端的呼叫记录已经被清空；

                Intent intent = getActivity().getIntent();
                if (intent != null) {
                    intent.removeExtra(JConstant.KEY_JUMP_TO_MESSAGE);
                }
                AppLogger.w("刷新数据中...");

                needRefresh = true;
            }
        }
        if (RxBus.getCacheInstance().hasStickyEvent(RxEvent.ClearDataEvent.class)) {
            needRefresh = true;
            camMessageListAdapter.clear();
            RxBus.getCacheInstance().removeStickyEvent(RxEvent.ClearDataEvent.class);
        }
        if (getArguments() != null && getArguments().getBoolean(JConstant.KEY_JUMP_TO_MESSAGE, false)) {
            //需要刷新
            needRefresh = true;
        }
        if (needRefresh) {
            startRequest(true, true);

        }

        Box<KeyValueStringItem> boxFor = BaseApplication.getBoxStore().boxFor(KeyValueStringItem.class);
        KeyValueStringItem stringItem = boxFor.get(CacheHolderKt.longHash(CamMessageListFragment.class.getName() + ":" + uuid + ":cachedItems"));
        if (stringItem != null) {
            Map<String, List<CamMessageBean>> json = new Gson().fromJson(stringItem.getValue(), new TypeToken<Map<String, List<CamMessageBean>>>() {
            }.getType());
            camMessageListAdapter.restoreCachedItems(json);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        //刚刚进入页面，尽量少点加载

        if (isVisibleToUser && presenter != null && getActivity() != null && isResumed()) {
//            if (camMessageListAdapter.getCount() == 0)
            startRequest(true, true);//需要每次刷新,而不是第一次刷新
            ViewUtils.setRequestedOrientation(getActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        }
    }

    /**
     * @param asc         ：true:向前，false:向后(loadMore)
     * @param showRefresh
     */
    private void startRequest(boolean asc, boolean showRefresh) {
        long time = 0;
        if (asc) {//移动带 onRefresh 中
            srLayoutCamListRefresh.setRefreshing(showRefresh);
            if (camMessageListAdapter.getCount() > 0) {
                time = camMessageListAdapter.getItem(0).message.getVersion();
            }
        } else {
            if (!camMessageListAdapter.hasFooter()) {
                setupFootView();
            }
            if (camMessageListAdapter.getCount() > 1) {
                time = camMessageListAdapter.getItem(camMessageListAdapter.getCount() - 2).message.getVersion();
            }
        }
        if (presenter != null) {
            if (hasFaceHeader) {
                boolean success;
                switch (pageType) {
                    case FaceItem.FACE_TYPE_STRANGER:
                        success = camMessageListAdapter.showCachedVisitorList("stranger");
                        lLayoutNoMessage.setVisibility(success ? View.INVISIBLE : View.VISIBLE);
                        tvCamMessageListEdit.setEnabled(success);
                        presenter.fetchVisitorMessageList(1, "", time, asc);
                        break;
                    case FaceItem.FACE_TYPE_ACQUAINTANCE:
                        success = camMessageListAdapter.showCachedVisitorList(personId);
                        lLayoutNoMessage.setVisibility(success ? View.INVISIBLE : View.VISIBLE);
                        tvCamMessageListEdit.setEnabled(success);
                        presenter.fetchVisitorMessageList(2, personId, time, asc);
                        break;
                    case FaceItem.FACE_TYPE_STRANGER_SUB:
                        success = camMessageListAdapter.showCachedVisitorList(personId);
                        lLayoutNoMessage.setVisibility(success ? View.INVISIBLE : View.VISIBLE);
                        tvCamMessageListEdit.setEnabled(success);
                        presenter.fetchVisitorMessageList(1, personId, time, asc);
                        break;
                    case FaceItem.FACE_TYPE_ALL:
                        if (showRefresh) {
                            refreshFaceHeader();
                        }
                        presenter.fetchMessageListByFaceId(time, asc, false);
                        break;
                    default:
                        presenter.fetchMessageListByFaceId(time, asc, false);

                }
            } else {
                lLayoutNoMessage.setVisibility(camMessageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
                presenter.fetchMessageListByFaceId(time, asc, false);
            }
        }

    }


    @Override
    public void onStop() {
        super.onStop();
        Map<String, List<CamMessageBean>> cachedItems = camMessageListAdapter.getCachedItems();
        String toJson = new Gson().toJson(cachedItems);
        BaseApplication.getBoxStore().boxFor(KeyValueStringItem.class)
                .put(new KeyValueStringItem(CacheHolderKt.longHash(CamMessageListFragment.class.getName() + ":" + uuid + ":cachedItems"), toJson));
    }

    /**
     * 更新顶部时间导航器
     *
     * @param position
     */
    private void setCurrentPosition(int position) {
        if (getView() != null && isAdded()) {
            getView().post(() -> {
                if (camMessageListAdapter.getCount() == 0) {
                    return;
                }
                long time = camMessageListAdapter.getList().get(position).message.getVersion();
                if (time == 0) time = System.currentTimeMillis();
                boolean isToday = TimeUtils.isToday(time);
                String content = String.format(TimeUtils.getSpecifiedDate(time) + "%s", isToday ? "(" + getString(R.string.DOOR_TODAY) + ")" : "");
                tvCamMessageListDate.setText(content);
            });
        }
    }

    @Override
    public void onDateMapRsp(List<WonderIndicatorWheelView.WheelItem> dateMap) {
        fLayoutCamMessageListTimeline.init(dateMap);
        boolean reset = tvCamMessageListDate.getTag() == null ||
                ((int) tvCamMessageListDate.getTag() == R.drawable.wonderful_arrow_down);
        fLayoutCamMessageListTimeline.setListener(time -> {
            AppLogger.d("scroll date： " + TimeUtils.getDayInMonth(time));
            if (presenter != null) {
                presenter.fetchMessageListByFaceId(TimeUtils.getSpecificDayEndTime(time), false, false);
            }
            LoadingDialog.showLoading(getActivity());
            boolean isToday = TimeUtils.isToday(time);
            String content = String.format(TimeUtils.getSuperString(time) + "%s", isToday ? "(" + getString(R.string.DOOR_TODAY) + ")" : "");

            tvCamMessageListDate.setText(content);
            tvCamMessageListEdit.setEnabled(camMessageListAdapter.getCount() > 0 && reset);
        });

        ViewUtils.setDrawablePadding(tvCamMessageListDate, reset ? R.drawable.wonderful_arrow_down : R.drawable.wonderful_arrow_up, 2);
        LoadingDialog.dismissLoading();
    }

    @Override
    public void onListAppend(ArrayList<CamMessageBean> beanArrayList) {
        endlessLoading = false;
        mIsLastLoadFinish = true;
        srLayoutCamListRefresh.setRefreshing(false);
        LoadingDialog.dismissLoading();
        lLayoutNoMessage.post(() -> {
            makeSureRemoveFoot();
            camMessageListAdapter.addAll(beanArrayList);
            int itemPosition = layoutManager.findFirstVisibleItemPosition();
            setCurrentPosition(Math.max(0, itemPosition));
            lLayoutNoMessage.setVisibility(camMessageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
            rLayoutCamMessageListTop.setVisibility(camMessageListAdapter.getCount() == 0 && !hasFaceHeader ? View.GONE : View.VISIBLE);
            boolean reset = tvCamMessageListDate.getTag() == null ||
                    ((int) tvCamMessageListDate.getTag() == R.drawable.wonderful_arrow_down);
            tvCamMessageListEdit.setEnabled(camMessageListAdapter.getCount() > 0 && reset);
            final int count = beanArrayList == null ? 0 : beanArrayList.size();
            if (count == 0) {
                ToastUtil.showToast(getString(R.string.Loaded));
            }
        });
    }

    private void makeSureRemoveFoot() {
        if (camMessageListAdapter.hasFooter() && getView() != null) {
            camMessageListAdapter.remove(camMessageListAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onListInsert(ArrayList<CamMessageBean> beanArrayList, int position) {
        endlessLoading = false;
        mIsLastLoadFinish = true;
        srLayoutCamListRefresh.setRefreshing(false);
        lLayoutNoMessage.post(() -> {
            makeSureRemoveFoot();
            int size = ListUtils.getSize(beanArrayList);
            for (int i = size - 1; i >= 0; i--) {
                camMessageListAdapter.add(0, beanArrayList.get(i));//beanArrayList是一个降序
            }
            lLayoutNoMessage.setVisibility(camMessageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
            rLayoutCamMessageListTop.setVisibility(camMessageListAdapter.getCount() == 0 && !hasFaceHeader ? View.GONE : View.VISIBLE);
        });
    }


    @Override
    public ArrayList<CamMessageBean> getList() {
        return (ArrayList<CamMessageBean>) camMessageListAdapter.getList();
    }

    @Override
    public void deviceInfoChanged(int id, JFGDPMsg o) throws IOException {
        // TODO: 2017/8/17 为什么消息页会需要监听同步消息? #118120
        // TODO:Android（1.1.0.534）局域网（公网），设备插入坏卡（显示读写失败-22），在报警消息页面刷新，会一直发初始化的消息，如图
        final int lPos = ((LinearLayoutManager) rvCamMessageList.getLayoutManager())
                .findLastVisibleItemPosition();
        switch (id) {
            case DpMsgMap.ID_201_NET:
                DpMsgDefine.DPNet net = DpUtils.unpackData(o.packValue, DpMsgDefine.DPNet.class);
                if (net == null) {
                    net = new DpMsgDefine.DPNet();
                }
                camMessageListAdapter.notifyDeviceOnlineState(net.net > 0, lPos);
                break;
            case DpMsgMap.ID_222_SDCARD_SUMMARY:
//                rvCamMessageList.postDelayed(() -> {
                // TODO: 2017/8/17 只标志下当前有没有 SD 卡,而不把这条消息加入到 adapter 里去,@see:#118120
                try {
                    DpMsgDefine.DPSdcardSummary summary = DpUtils.unpackData(o.packValue, DpMsgDefine.DPSdcardSummary.class);
                    camMessageListAdapter.notifySdcardStatus(summary != null && summary.errCode == 0 && summary.hasSdcard, lPos);
//                    if (summary != null) {
//                        camMessageListAdapter.setCurrentSDcardSummary(summary);
//                    }
////                    if (summary == null) summary = new DpMsgDefine.DPSdcardSummary();
////                    camMessageListAdapter.notifySdcardStatus(summary.hasSdcard, lPos);
////                    CamMessageBean bean = new CamMessageBean();
////                    bean.sdcardSummary = summary;
////                    bean.id = DpMsgMap.ID_222_SDCARD_SUMMARY;
////                    bean.version = o.version;
////                    ArrayList<CamMessageBean> totalList = camMessageListAdapter.getSelectedItems();
////                    if (totalList != null && totalList.contains(bean)) {
////                        return;//重复了
////                    }
////                    camMessageListAdapter.add(0, bean);
////                    rvCamMessageList.scrollToPosition(0);
////                    lLayoutNoMessage.setVisibility(View.GONE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

//        }
//                , 200);
                break;
            default:
        }

    }

    @Override
    public void onErr() {
        srLayoutCamListRefresh.post(() -> {
            srLayoutCamListRefresh.setRefreshing(false);
            makeSureRemoveFoot();
        });
        if (getView() != null && getActivity() != null) {
            LoadingDialog.dismissLoading();
        }
    }

    @Override
    public void onMessageDeleteSuc() {
        ToastUtil.showPositiveToast(getString(R.string.DELETED_SUC));
        if (getView() != null && getActivity() != null) {
            LoadingDialog.dismissLoading();
        }
        boolean reset = tvCamMessageListDate.getTag() == null ||
                ((int) tvCamMessageListDate.getTag() == R.drawable.wonderful_arrow_down);
        ViewUtils.setDrawablePadding(tvCamMessageListDate, reset ? R.drawable.wonderful_arrow_down : R.drawable.wonderful_arrow_up, 2);
        tvCamMessageListEdit.setEnabled(camMessageListAdapter.getCount() > 0 && reset);
    }

    @Override
    public void loadingDismiss() {
        makeSureRemoveFoot();
        LoadingDialog.dismissLoading();
    }

    @Override
    public boolean isUserVisible() {
        return getUserVisibleHint();
    }

    @Override
    public void onVisitorListAppend(ArrayList<CamMessageBean> beanArrayList) {
        endlessLoading = false;
        mIsLastLoadFinish = true;
        srLayoutCamListRefresh.setRefreshing(false);
        LoadingDialog.dismissLoading();

        lLayoutNoMessage.post(() -> {

            camMessageListAdapter.appendVisitorList(personId, beanArrayList);
            int itemPosition = layoutManager.findFirstVisibleItemPosition();
            setCurrentPosition(Math.max(0, itemPosition));
            lLayoutNoMessage.setVisibility(camMessageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
            rLayoutCamMessageListTop.setVisibility(View.VISIBLE);
            boolean reset = tvCamMessageListDate.getTag() == null ||
                    ((int) tvCamMessageListDate.getTag() == R.drawable.wonderful_arrow_down);
            tvCamMessageListEdit.setEnabled(camMessageListAdapter.getCount() > 0 && reset);
            final int count = beanArrayList == null ? 0 : beanArrayList.size();
            if (count == 0) {
                AppLogger.w("没有数据");
                ToastUtil.showToast(getString(R.string.Loaded));
            }
        });
    }

    @Override
    public void onVisitorListInsert(ArrayList<CamMessageBean> beans) {
        endlessLoading = false;
        mIsLastLoadFinish = true;
        srLayoutCamListRefresh.setRefreshing(false);
        LoadingDialog.dismissLoading();
        camMessageListAdapter.clear();
        camMessageListAdapter.insertVisitorList(personId, beans);
        int itemPosition = layoutManager.findFirstVisibleItemPosition();
        setCurrentPosition(Math.max(0, itemPosition));
        lLayoutNoMessage.setVisibility(camMessageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
        rLayoutCamMessageListTop.setVisibility(View.VISIBLE);
        tvCamMessageListEdit.setEnabled(camMessageListAdapter.getCount() > 0);
        final int count = beans == null ? 0 : beans.size();
        if (count == 0) {
            AppLogger.w("没有数据");
            ToastUtil.showToast(getString(R.string.Loaded));
            makeSureRemoveFoot();
        }
    }

    @Override
    public void onRefresh() {
        if (NetUtils.getJfgNetType(getContext()) == 0) {
            srLayoutCamListRefresh.setRefreshing(false);
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }
//        srLayoutCamListRefresh.setRefreshing(true);
        startRequest(true, true);
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
                if (JFGRules.isFaceFragment(getDevice().pid)) {
                    return;
                }
//                if (camMessageListAdapter.getCount() == 0&&)
//                    return;//呼入呼出
                if (TextUtils.equals(getString(R.string.CANCEL), tvCamMessageListEdit.getText())) {
                    return;
                }
                if (presenter != null && presenter.getDateList().size() == 0) {
                    LoadingDialog.showLoading(getActivity(), getString(R.string.LOADING), true);
                    AppLogger.d("日期加载中...");
                    presenter.refreshDateList(true);
                }
                boolean reset = tvCamMessageListDate.getTag() == null ||
                        ((int) tvCamMessageListDate.getTag() == R.drawable.wonderful_arrow_down);
                tvCamMessageListDate.setTag(reset ? R.drawable.wonderful_arrow_up : R.drawable.wonderful_arrow_down);
                ViewUtils.setDrawablePadding(tvCamMessageListDate, reset ? R.drawable.wonderful_arrow_up : R.drawable.wonderful_arrow_down, 2);
                if (reset) {
                    AnimatorUtils.slideIn(fLayoutCamMessageListTimeline, false);
                } else {
                    AnimatorUtils.slideOut(fLayoutCamMessageListTimeline, false);
                }
                tvCamMessageListEdit.setEnabled(!reset);
                break;
            case R.id.tv_cam_message_list_edit:
                if (camMessageListAdapter.getCount() == 0) {
                    return;
                }
                String content = ((TextView) view).getText().toString();
                boolean toEdit = TextUtils.equals(content, getString(R.string.EDIT_THEME));
                if (toEdit) {
                    tvMsgFullSelect.setText(getString(R.string.SELECT_ALL));
                }
                camMessageListAdapter.reverseMode(toEdit, lPos);
                if (camMessageListAdapter.isEditMode()) {
                    AnimatorUtils.slideIn(fLayoutCamMsgEditBar, false);
                } else {
                    AnimatorUtils.slideOut(fLayoutCamMsgEditBar, false);
                }
                if (visitorFragment != null) {
                    visitorFragment.disable(toEdit);
                }
                ((TextView) view).setText(getString(toEdit ? R.string.CANCEL
                        : R.string.EDIT_THEME));
                break;
            case R.id.tv_msg_full_select://全选
                boolean selectAll = TextUtils.equals(tvMsgFullSelect.getText(), getString(R.string.SELECT_ALL));
                if (selectAll) {
                    camMessageListAdapter.markAllAsSelected(true, lPos);
                    tvMsgFullSelect.setText(getString(R.string.CANCEL));
                } else {
                    camMessageListAdapter.markAllAsSelected(false, lPos);
                    tvMsgFullSelect.setText(getString(R.string.SELECT_ALL));
                }
                tvMsgDelete.setEnabled(camMessageListAdapter.getSelectedItems().size() > 0);
                break;
            case R.id.tv_msg_delete://删除
                final ArrayList<CamMessageBean> list = new ArrayList<>(camMessageListAdapter.getSelectedItems());
                if (ListUtils.isEmpty(list)) {
                    return;
                }
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.Tips_SureDelete)
                        .setPositiveButton(R.string.OK, (dialog, which) -> {
                            camMessageListAdapter.removeAll(list);
                            if (presenter != null) {
                                presenter.removeItems(list);
                            }
                            camMessageListAdapter.reverseMode(false, camMessageListAdapter.getCount());
                            AnimatorUtils.slideOut(fLayoutCamMsgEditBar, false);
                            tvCamMessageListEdit.setText(getString(R.string.EDIT_THEME));
                            LoadingDialog.showLoading(getActivity(), getString(R.string.DELETEING), false, null);
                        })
                        .setNegativeButton(R.string.CANCEL, null)
                        .setCancelable(false)
                        .show();
                break;
            default:
        }
    }


    @Override
    public void onClick(View v) {
        final int position = ViewUtils.getParentAdapterPosition(rvCamMessageList, v,
                R.id.lLayout_cam_msg_container);
        switch (v.getId()) {
            case R.id.tv_cam_message_item_delete: {//删除选中
                getAlertDialogManager().showDialog(getActivity(), getString(R.string.Tips_SureDelete), getString(R.string.Tips_SureDelete),
                        getString(R.string.OK), (DialogInterface dialog, int which) -> {
                            ArrayList<CamMessageBean> list = new ArrayList<>(camMessageListAdapter.getSelectedItems());
                            if (camMessageListAdapter.getCount() > position) {
                                list.add(camMessageListAdapter.getItem(position));
                            }
                            camMessageListAdapter.removeAll(list);
                            if (presenter != null) {
                                presenter.removeItems(list);
                            }
                            LoadingDialog.showLoading(getActivity(), getString(R.string.DELETEING), false, null);
                        }, getString(R.string.CANCEL), null, false);
            }
            break;
            case R.id.lLayout_cam_msg_container: {//点击item,选中
                if (!camMessageListAdapter.isEditMode()) {
                    return;
                }
                boolean itemSelected = camMessageListAdapter.markItemSelected(position);
                int size = ListUtils.getSize(camMessageListAdapter.getSelectedItems());
                tvMsgDelete.setEnabled(size > 0);
                if (!itemSelected) {
                    tvMsgFullSelect.setText(getString(R.string.SELECT_ALL));
                }
            }
            break;
            case R.id.imgV_cam_message_pic0:
                if (!camMessageListAdapter.isEditMode()) {//编辑模式下点击不应该进入详情页
                    startActivity(getIntent(position, 0));
                } else {
                    boolean itemSelected = camMessageListAdapter.markItemSelected(position);
                    int size = ListUtils.getSize(camMessageListAdapter.getSelectedItems());
                    tvMsgDelete.setEnabled(size > 0);
                    if (!itemSelected) {
                        tvMsgFullSelect.setText(getString(R.string.SELECT_ALL));
                    }
                }
                break;
            case R.id.imgV_cam_message_pic1:
                if (!camMessageListAdapter.isEditMode()) {//编辑模式下点击不应该进入详情页
                    startActivity(getIntent(position, 1));
                } else {
                    boolean itemSelected = camMessageListAdapter.markItemSelected(position);
                    int size = ListUtils.getSize(camMessageListAdapter.getSelectedItems());
                    tvMsgDelete.setEnabled(size > 0);
                    if (!itemSelected) {
                        tvMsgFullSelect.setText(getString(R.string.SELECT_ALL));
                    }
                }
                break;
            case R.id.imgV_cam_message_pic2:
                if (!camMessageListAdapter.isEditMode()) {//编辑模式下点击不应该进入详情页
                    startActivity(getIntent(position, 2));
                } else {
                    boolean itemSelected = camMessageListAdapter.markItemSelected(position);
                    int size = ListUtils.getSize(camMessageListAdapter.getSelectedItems());
                    tvMsgDelete.setEnabled(size > 0);
                    if (!itemSelected) {
                        tvMsgFullSelect.setText(getString(R.string.SELECT_ALL));
                    }
                }
                break;
            case R.id.tv_jump_next: {
                try {
                    if (!JFGRules.isDeviceOnline(uuid)) {
                        ToastUtil.showToast(getString(R.string.NOT_ONLINE));
                        return;
                    }
                    CamMessageBean bean = camMessageListAdapter.getItem(position);
                    boolean jumpNext = bean != null && bean.message != null && (bean.message.getMsgId() == DpMsgMap.ID_505_CAMERA_ALARM_MSG || bean.message.getMsgId() == DpMsgMap.ID_401_BELL_CALL_STATE);
                    if (jumpNext) {
                        if (JFGRules.isPan720(getDevice().pid)) {
                            startActivity(getIntent(position, 0));
                        } else {
                            Activity activity = getActivity();
                            if (activity != null && activity instanceof CameraLiveActivity) {
                                Bundle bundle = new Bundle();
//                            long time = bean.alarmMsg.version;//1498194000
                                long time = MiscUtils.getVersion(bean);
                                bundle.putLong(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_TIME, time);
                                bundle.putBoolean(JConstant.KEY_CAM_LIVE_PAGE_PLAY_HISTORY_INIT_WHEEL, true);
                                ((CameraLiveActivity) activity).addPutBundle(bundle);
                            }
                        }
                        AppLogger.d("alarm: " + bean);
                    } else {
                        Intent intent = new Intent(getActivity(), CamSettingActivity.class);
                        intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
//                        intent.putExtra(JConstant.KEY_JUMP_TO_CAM_DETAIL, true);
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
            default:
        }
    }

    private Intent getIntent(int position, int index) {
        //720 和 普通报警消息进入的页面不一样
        Intent intent = null;
        CamMessageBean item = camMessageListAdapter.getItem(position);
        if (JFGRules.isPan720(getDevice().pid)) {
            // TODO: 2017/8/3 720 消息详情页
// intent= new Intent(getActivity(), PanoramaDetailActivity.class);
            intent = PanoramaDetailActivity.getIntentFromMessage(getActivity(), uuid, item, position, index + 1);
            return intent;
        } else {

            intent = new Intent(getActivity(), CamMediaActivity.class);
            intent.putExtra(KEY_INDEX, index);
            intent.putExtra(KEY_BUNDLE, item);
            intent.putExtra(JConstant.KEY_DEVICE_ITEM_UUID, uuid);
            Log.d("imgV_cam_message_pic_0", "imgV_cam_:" + position + " " + camMessageListAdapter.getItem(position));
        }

        return intent;
    }

    @Override
    public void onItemClick(View itemView, int viewType, int position) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    @OnClick(R.id.tv_msg_delete)
    public void onClick() {
    }

    @OnClick(R.id.iv_cam_message_arrow)
    public void onMessageArrowClick() {
        if (TextUtils.equals((CharSequence) arrow.getTag(), "arrow_down")) {
            arrow.setImageResource(R.drawable.btn_unfolded);
            arrow.setTag("arrow_up");
            if (JFGRules.isFaceFragment(getDevice().pid)) {
                aplCamMessageAppbar.setExpanded(false, true);

            }

        } else {
            arrow.setImageResource(R.drawable.btn_put_away);
            arrow.setTag("arrow_down");
            if (JFGRules.isFaceFragment(getDevice().pid)) {
                aplCamMessageAppbar.setExpanded(true, true);
                layoutManager.scrollToPosition(0);
            }
        }
    }


    public void hookEdit(TextView tvToolbarRight) {
        this.mockView = tvToolbarRight;
        this.mockView.setId(R.id.tv_cam_message_list_edit);
        this.mockView.setOnClickListener(this::onBindClick);
    }

    private TextView mockView;

    @Override
    public boolean performBackIntercept(boolean willExit) {
        if (isUserVisible() && isResumed()) {
            return exitEditMode();
        }
        return super.performBackIntercept(willExit);
    }

    private boolean exitEditMode() {
        if (camMessageListAdapter.isEditMode()) {
            AnimatorUtils.slideOut(fLayoutCamMsgEditBar, false);
            tvCamMessageListEdit.setText(getString(R.string.EDIT_THEME));
            final int lPos = ((LinearLayoutManager) rvCamMessageList.getLayoutManager())
                    .findLastVisibleItemPosition();
            camMessageListAdapter.reverseMode(false, lPos);
            if (visitorFragment != null) {
                visitorFragment.disable(false);
            }
            return true;//拦截掉
        }
        return false;
    }
}
