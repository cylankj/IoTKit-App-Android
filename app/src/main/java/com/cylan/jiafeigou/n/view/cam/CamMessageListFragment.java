package com.cylan.jiafeigou.n.view.cam;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.impl.BaseDBHelper;
import com.cylan.jiafeigou.cache.db.module.KeyValue;
import com.cylan.jiafeigou.cache.db.module.KeyValueDao;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
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
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.support.superadapter.OnItemClickListener;
import com.cylan.jiafeigou.utils.ActivityUtils;
import com.cylan.jiafeigou.utils.AnimatorUtils;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.TimeUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.CameraMoreTextDialog;
import com.cylan.jiafeigou.widget.InterceptSwipeRefreshLayout;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.wheel.WonderIndicatorWheelView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    InterceptSwipeRefreshLayout srLayoutCamListRefresh;
    @BindView(R.id.fLayout_cam_message_list_timeline)
    WonderIndicatorWheelView fLayoutCamMessageListTimeline;
    @BindView(R.id.fLayout_cam_msg_edit_bar)
    FrameLayout fLayoutCamMsgEditBar;
    @BindView(R.id.lLayout_no_message)
    View lLayoutNoMessage;
    @BindView(R.id.rLayout_cam_message_list_top)
    RelativeLayout rLayoutCamMessageListTop;
    @BindView(R.id.tv_msg_full_select)
    TextView tvMsgFullSelect;
    @BindView(R.id.tv_msg_delete)
    TextView tvMsgDelete;
    //    @BindView(R.id.iv_cam_message_arrow)
//    ImageView arrow;
    @BindView(R.id.iv_back)
    TextView barBack;
    @BindView(R.id.c_layout_parent)
    CoordinatorLayout parent;
    @BindView(R.id.message_appbar)
    AppBarLayout aplCamMessageAppbar;
    @BindView(R.id.quick_top)
    ImageButton ibQuickTop;
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
    private int pageType = FaceItem.FACE_TYPE_DP;
    private String personId;
    private boolean hasFirstRequested = false;
    private Rect appbarRect = new Rect();
    private Rect messageRect = new Rect();
    private Rect headerRect = new Rect();
    private boolean hasExpanded = false;

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
        hasFaceHeader = JFGRules.isFaceFragment(getDevice().pid);
        pageType = hasFaceHeader ? FaceItem.FACE_TYPE_ALL : FaceItem.FACE_TYPE_DP;
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

//        this.tvCamMessageListEdit.setEnabled(false);
        srLayoutCamListRefresh.setColorSchemeColors(getResources().getColor(R.color.color_36BDFF));
        srLayoutCamListRefresh.setOnRefreshListener(this);
        srLayoutCamListRefresh.setInterceptListener(ev -> {
            int rawX = (int) ev.getRawX();
            int rawY = (int) ev.getRawY();
            messageRect.set(rvCamMessageList.getLeft(), rvCamMessageList.getTop(), rvCamMessageList.getRight(), rvCamMessageList.getBottom());
            headerRect.set(rLayoutCamMessageListTop.getLeft(), rLayoutCamMessageListTop.getTop(), rLayoutCamMessageListTop.getRight(), rLayoutCamMessageListTop.getBottom());
            appbarRect.set(aplCamMessageAppbar.getLeft(), aplCamMessageAppbar.getTop(), aplCamMessageAppbar.getRight(), aplCamMessageAppbar.getBottom());
            aplCamMessageAppbar.getGlobalVisibleRect(appbarRect);
            int appbarTop = aplCamMessageAppbar.getTop();
            if (messageRect.contains(rawX, rawY) || headerRect.contains(rawX, rawY)) {
                boolean scrollVertically = rvCamMessageList.canScrollVertically(-1);
                return !scrollVertically && appbarTop >= 0;
            }
            if (appbarRect.contains(rawX, rawY)) {
                return appbarTop >= 0 && !(visitorFragment != null && visitorFragment.canScrollVertically(-1));
            }
            return true;
        });
        camMessageListAdapter = new CamMessageListAdapter(this.uuid, getContext(), null, null);
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false) {
            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                try {
                    super.onLayoutChildren(recycler, state);
                } catch (Exception e) {
                    AppLogger.e("homepageList" + e.getMessage());
                    e.printStackTrace();
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
                                startRequest(false);
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
        rLayoutCamMessageListTop.setVisibility(View.GONE);
        if (!hasFaceHeader) {
//            arrow.setVisibility(View.GONE);
//            aplCamMessageAppbar.setExpanded(false);
            layoutBarMenu(BAR_TYPE_NORMAL);
        }
    }

    private void refreshFaceHeader() {
        if (visitorFragment == null) {
            //init Fragment 并添加 Fragment 是异步的
            initFaceHeader();

        } else {
            visitorFragment.refreshContent();
        }
    }

    private void justForDemo(boolean hasMessage) {
        if (hasFaceHeader) {
            // #123596 AI-隐藏原图，具体看附件红色所指部分，仅仅留头像即可。--应用于测试演示使用
            tvCamMessageListEdit.setVisibility(hasMessage ? View.VISIBLE : View.INVISIBLE);
            tvCamMessageListDate.setVisibility(hasMessage ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void initFaceHeader() {
        aplCamMessageAppbar.addOnOffsetChangedListener(this::onMessageAppbarScrolled);
        tvCamMessageListDate.setClickable(false);
        layoutBarMenu(BAR_TYPE_FACE_COMMON);
//        aplCamMessageAppbar.setExpanded(true, false);

        if (visitorFragment != null) {
            return;//do nothing
        }

        visitorFragment = VisitorListFragmentV2.Companion.newInstance(uuid());
        visitorFragment.setVisitorListener(new VisitorListFragmentV2.VisitorListener() {
            @Override
            public void onLoadItemInformation(int faceType, @NotNull String personOrFaceId) {
                changeContentByHeaderClick(faceType);
                personId = personOrFaceId;
                startRequest(false);
            }

            @Override
            public void onExpanded(boolean expanded) {
                hasExpanded = expanded;
                ViewGroup.LayoutParams layoutParams = aplCamMessageAppbar.getLayoutParams();
                layoutParams.height = expanded ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT;
                aplCamMessageAppbar.setLayoutParams(layoutParams);
//                srLayoutCamListRefresh.setEnabled(!expanded);
            }

            @Override
            public void onStrangerVisitorReady(@NotNull List<FaceItem> visitorList) {
                camMessageListAdapter.onStrangerInformationReady(visitorList);
                pageType = FaceItem.FACE_TYPE_STRANGER_SUB;
                if (hasExpanded) {
                    srLayoutCamListRefresh.setRefreshing(false);
                }
//                justForDemo(false);

            }

            @Override
            public void onVisitorReady(@NotNull List<FaceItem> visitorList) {
                camMessageListAdapter.onVisitorInformationReady(visitorList);
                if (hasExpanded) {
                    srLayoutCamListRefresh.setRefreshing(false);
                }
//                justForDemo(true);
            }
        });
        //显示 所有面孔列表
        ActivityUtils.replaceFragment(getFragmentManager(),
                visitorFragment, R.id.fLayout_message_face, "visitorFragment", false);
    }

    private void layoutBarMenu(int barType) {
        if (camMessageListAdapter != null) {
            camMessageListAdapter.clear();
        }
        if (barType == BAR_TYPE_FACE_COMMON) {
            barBack.setVisibility(View.GONE);
            rLayoutCamMessageListTop.setVisibility(camMessageListAdapter.getItemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
            tvCamMessageListDate.setVisibility(camMessageListAdapter.getItemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
            tvCamMessageListEdit.setVisibility(camMessageListAdapter.getItemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        } else if (barType == BAR_TYPE_NORMAL) {
            barBack.setVisibility(View.GONE);
            rLayoutCamMessageListTop.setVisibility(View.GONE);
        } else if (barType == BAR_TYPE_STRANGER) {
            barBack.setVisibility(View.VISIBLE);
            rLayoutCamMessageListTop.setVisibility(View.VISIBLE);
            tvCamMessageListDate.setVisibility(camMessageListAdapter.getItemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
            tvCamMessageListEdit.setVisibility(camMessageListAdapter.getItemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
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
        startRequest(true);
        exitEditMode();
        AppLogger.d("还需要重新选中All");
    }

    public boolean handleViewPagerState() {
        return false;
    }

    private void changeContentByHeaderClick(int faceType) {
        this.pageType = faceType;
        camMessageListAdapter.clear();
        lLayoutNoMessage.setVisibility(View.VISIBLE);
        if (faceType == FaceItem.FACE_TYPE_STRANGER) {
//            // TODO: 2017/10/10 点击了陌生人,需要刷新陌生人列表
            layoutBarMenu(BAR_TYPE_STRANGER);
        } else if (faceType == FaceItem.FACE_TYPE_ACQUAINTANCE) {
            // TODO: 2017/10/10 点击的是熟人,但具体是哪个人还不知道
            layoutBarMenu(BAR_TYPE_FACE_COMMON);
        } else if (faceType == FaceItem.FACE_TYPE_ALL) {
            // TODO: 2017/10/10 点击的是全部 ,需要刷新所有
            layoutBarMenu(BAR_TYPE_FACE_COMMON);
        } else if (faceType == FaceItem.FACE_TYPE_STRANGER_SUB) {
            layoutBarMenu(BAR_TYPE_STRANGER);
        }

    }

    private boolean isPendendingAnimationFinished = true;

    private void onMessageAppbarScrolled(AppBarLayout appBarLayout, int offset) {
        Log.i("onMessageAppbarScrolled", "offset is:" + offset + ",total is:" + appBarLayout.getTotalScrollRange());
//        srLayoutCamListRefresh.setEnabled(offset == 0 && !rvCamMessageList.canScrollVertically(-1));

        if (Math.abs(offset) == appBarLayout.getTotalScrollRange()) {
            // TODO: 2017/9/29 更新箭头
            if (hasFaceHeader && isPendendingAnimationFinished && ibQuickTop.getTranslationY() != 0) {
                isPendendingAnimationFinished = false;
                YoYo.with(Techniques.SlideInUp).duration(200).withListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        ibQuickTop.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isPendendingAnimationFinished = true;
                    }
                }).playOn(ibQuickTop);
            }
//            arrow.setImageResource(R.drawable.btn_put_away);
        } else if (Math.abs(offset) == 0) {
            if (hasFaceHeader && isPendendingAnimationFinished && ibQuickTop.getTranslationY() == 0) {
                isPendendingAnimationFinished = false;
                YoYo.with(Techniques.SlideOutDown).duration(200)
                        .withListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                ibQuickTop.setVisibility(View.INVISIBLE);
                                isPendendingAnimationFinished = true;
                            }
                        })
                        .playOn(ibQuickTop);
            }
//            arrow.setImageResource(R.drawable.btn_unfolded);
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

    private void decideRefresh() {
        srLayoutCamListRefresh.setRefreshing(true);
        if (hasFaceHeader && hasExpanded) {
            if (visitorFragment != null) {
                visitorFragment.refreshContent();
            }
            return;
        }
        if (hasFaceHeader && pageType == FaceItem.FACE_TYPE_ALL) {
            //这里不能调用 startRequest ,因为需要先等 header 的数据回来才能请求下面的数据,
            //等 header 数据回来后会自动调用 startRequest 的
            refreshFaceHeader();
        } else {
            startRequest(true);
        }
    }

    @Override
    protected void lazyLoad() {
        super.lazyLoad();
        //从通知栏跳进来
        if (hasFirstRequested) return;

        ViewUtils.setRequestedOrientation(getActivity(), ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (RxBus.getCacheInstance().hasStickyEvent(RxEvent.ClearDataEvent.class)) {
            camMessageListAdapter.clear();
            RxBus.getCacheInstance().removeStickyEvent(RxEvent.ClearDataEvent.class);
        }

        decideRefresh();//需要每次刷新,而不是第一次刷新

        if (NetUtils.getNetType(getContext()) == -1) {
//            Box<KeyValueStringItem> boxFor = BaseApplication.getBoxStore().boxFor(KeyValueStringItem.class);
            KeyValueDao keyValueDao = BaseDBHelper.getInstance().getDaoSession().getKeyValueDao();
            KeyValue keyValue = keyValueDao.loadByRowId(CacheHolderKt.longHash(CamMessageListFragment.class.getName() + ":" + uuid + ":cachedItems"));
//            KeyValueStringItem stringItem =  boxFor.get(CacheHolderKt.longHash(CamMessageListFragment.class.getName() + ":" + uuid + ":cachedItems"));
            if (keyValue != null) {
                try {
                    Map<String, List<CamMessageBean>> json = new ObjectMapper().readValue(keyValue.getValue(), new TypeReference<Map<String, List<CamMessageBean>>>() {
                    });
                    camMessageListAdapter.restoreCachedItems(json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     */
    private void startRequest(boolean refresh) {
        hasFirstRequested = true;
        long time = 0;
        if (!camMessageListAdapter.hasFooter() && camMessageListAdapter.getCount() > 0) {
            setupFootView();
        }
        if (camMessageListAdapter.getCount() > 1) {
            time = camMessageListAdapter.getItem(camMessageListAdapter.getCount() - 2).message.getVersion();
        }
//        if (hasFaceHeader && pageType != FaceItem.FACE_TYPE_ALL) {
//            // #123596 AI-IOS-隐藏原图，具体看附件红色所指部分，仅仅留头像即可。--应用于测试演示使用
//            srLayoutCamListRefresh.setRefreshing(false);
//            return;
//        }
        if (presenter != null) {
            boolean success;
            switch (pageType) {
                case FaceItem.FACE_TYPE_STRANGER:
                    srLayoutCamListRefresh.setRefreshing(refresh);
                    success = camMessageListAdapter.showCachedVisitorList("stranger");
                    lLayoutNoMessage.setVisibility(success ? View.INVISIBLE : View.VISIBLE);
                    tvCamMessageListEdit.setEnabled(success);
//                    presenter.fetchVisitorMessageList(1, "", time, refresh);
                    break;
                case FaceItem.FACE_TYPE_ACQUAINTANCE:
                    srLayoutCamListRefresh.setRefreshing(refresh);
                    success = camMessageListAdapter.showCachedVisitorList(personId);
                    lLayoutNoMessage.setVisibility(success ? View.INVISIBLE : View.VISIBLE);
                    tvCamMessageListEdit.setEnabled(success);
                    if (!TextUtils.isEmpty(personId)) {
                        presenter.fetchVisitorMessageList(2, personId, time, refresh);
                    }
                    break;
                case FaceItem.FACE_TYPE_STRANGER_SUB:
                    srLayoutCamListRefresh.setRefreshing(refresh);
                    success = camMessageListAdapter.showCachedVisitorList(personId);
                    lLayoutNoMessage.setVisibility(success ? View.INVISIBLE : View.VISIBLE);
                    tvCamMessageListEdit.setEnabled(success);
                    if (!TextUtils.isEmpty(personId)) {
                        presenter.fetchVisitorMessageList(1, personId, time, refresh);
                    }
                    break;
                case FaceItem.FACE_TYPE_ALL:
                    srLayoutCamListRefresh.setRefreshing(refresh);
                    lLayoutNoMessage.setVisibility(camMessageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
                    presenter.fetchVisitorMessageList(3, "", time, refresh);
                    break;
                case FaceItem.FACE_TYPE_DP:
                    presenter.fetchMessageListByFaceId(time, false, refresh);
                    break;
                default:
                    presenter.fetchMessageListByFaceId(time, false, refresh);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Map<String, List<CamMessageBean>> cachedItems = camMessageListAdapter.getCachedItems();
        try {
            String toJson = new ObjectMapper().writeValueAsString(cachedItems);
            KeyValueDao keyValueDao = BaseDBHelper.getInstance().getDaoSession().getKeyValueDao();
            keyValueDao.insertOrReplace(new KeyValue(CacheHolderKt.longHash(CamMessageListFragment.class.getName() + ":" + uuid + ":cachedItems"), toJson));
//            keyValueDao.save(new KeyValue(CacheHolderKt.longHash(CamMessageListFragment.class.getName() + ":" + uuid + ":cachedItems"), toJson));
//            BaseApplication.getBoxStore().boxFor(KeyValueStringItem.class)
//                    .put(new KeyValueStringItem(CacheHolderKt.longHash(CamMessageListFragment.class.getName() + ":" + uuid + ":cachedItems"), toJson));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
                String content = String.format(TimeUtils.getSpecifiedDate(time) + "%s", isToday ? "(" + ContextUtils.getContext().getString(R.string.DOOR_TODAY) + ")" : "");
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
            String content = String.format(TimeUtils.getSuperString(time) + "%s", isToday ? "(" + ContextUtils.getContext().getString(R.string.DOOR_TODAY) + ")" : "");

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
            camMessageListAdapter.notifyDataSetHasChanged();
            final int count = beanArrayList == null ? 0 : beanArrayList.size();
            if (count == 0 && camMessageListAdapter.getCount() > 0) {
                ToastUtil.showToast(ContextUtils.getContext().getString(R.string.Loaded));
                makeSureRemoveFoot();
            }
            decideEmptyViewLayout();
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
            decideEmptyViewLayout();
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
                // TODO: 2017/8/17 只标志下当前有没有 SD 卡,而不把这条消息加入到 adapter 里去,@see:#118120
                try {
                    DpMsgDefine.DPSdcardSummary summary = DpUtils.unpackData(o.packValue, DpMsgDefine.DPSdcardSummary.class);
                    camMessageListAdapter.notifySdcardStatus(summary != null && summary.errCode == 0 && summary.hasSdcard, lPos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        if (visitorFragment != null) {
            visitorFragment.disable(false);
        }
        decideEmptyViewLayout();
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
            makeSureRemoveFoot();
            camMessageListAdapter.appendVisitorList(personId, beanArrayList);
            int itemPosition = layoutManager.findFirstVisibleItemPosition();
            setCurrentPosition(Math.max(0, itemPosition));
            final int count = beanArrayList == null ? 0 : beanArrayList.size();
            if (count == 0 && camMessageListAdapter.getCount() > 0) {
                AppLogger.w("没有数据");
                ToastUtil.showToast(getString(R.string.Loaded));
                makeSureRemoveFoot();
            }
            decideEmptyViewLayout();
        });

    }

    private void decideEmptyViewLayout() {
        lLayoutNoMessage.setVisibility(camMessageListAdapter.getCount() > 0 ? View.GONE : View.VISIBLE);
        rLayoutCamMessageListTop.setVisibility(camMessageListAdapter.getCount() > 0 || pageType == FaceItem.FACE_TYPE_STRANGER_SUB || pageType == FaceItem.FACE_TYPE_STRANGER ? View.VISIBLE : View.GONE);
        tvCamMessageListDate.setVisibility(camMessageListAdapter.getItemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        tvCamMessageListEdit.setVisibility(camMessageListAdapter.getItemCount() > 0 ? View.VISIBLE : View.INVISIBLE);
        boolean reset = tvCamMessageListDate.getTag() == null ||
                ((int) tvCamMessageListDate.getTag() == R.drawable.wonderful_arrow_down);
        if (!hasFaceHeader) {
            ViewUtils.setDrawablePadding(tvCamMessageListDate, reset ? R.drawable.wonderful_arrow_down : R.drawable.wonderful_arrow_up, 2);
        }
        tvCamMessageListEdit.setEnabled(camMessageListAdapter.getCount() > 0 && reset);
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
        final int count = beans == null ? 0 : beans.size();
        if (count == 0 && camMessageListAdapter.getCount() > 0) {
            AppLogger.w("没有数据");
            ToastUtil.showToast(getString(R.string.Loaded));
            makeSureRemoveFoot();
        }
        decideEmptyViewLayout();
    }

    @Override
    public void onRefresh() {
        if (NetUtils.getJfgNetType(getContext()) == 0) {
            srLayoutCamListRefresh.setRefreshing(false);
            ToastUtil.showToast(getString(R.string.OFFLINE_ERR_1));
            return;
        }
        decideRefresh();
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
                setEditerMode(toEdit);
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
                            setEditerMode(false);
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
            case R.id.tv_cam_message_item_more_text: {
                if (JFGRules.isFaceFragment(getDevice().pid)) {
                    //面孔消息人名很多,需要弹窗查看
                    CameraMoreTextDialog dialog = CameraMoreTextDialog.newInstance(v.getTag().toString());
                    FragmentManager fragmentManager = getFragmentManager();
                    if (fragmentManager != null) {
                        dialog.show(getFragmentManager(), "CameraMoreTextDialog");
                    }
                }
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

    @OnClick(R.id.tv_msg_delete)
    public void onClick() {
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
            return exitEditMode() || exitDateSelectMode();
        }
        return super.performBackIntercept(willExit);
    }

    private boolean setEditerMode(boolean editerMode) {
        boolean editMode = camMessageListAdapter.isEditMode();
        camMessageListAdapter.reverseMode(editerMode, 0);
        tvCamMessageListEdit.setText(editerMode ? getString(R.string.CANCEL) : getString(R.string.EDIT_THEME));
        tvMsgFullSelect.setText(camMessageListAdapter.getSelectedItems().size() > 0 ? getString(R.string.CANCEL) : getString(R.string.SELECT_ALL));
        tvMsgDelete.setEnabled(camMessageListAdapter.getSelectedItems().size() > 0);
        tvMsgFullSelect.setEnabled(editerMode);
        visitorFragment.disable(editerMode);
        ibQuickTop.setVisibility(editerMode ? View.GONE : View.VISIBLE);
        if (editerMode && !editMode) {
            AnimatorUtils.slideIn(fLayoutCamMsgEditBar, false);
        } else if (!editerMode && editMode) {
            AnimatorUtils.slideOut(fLayoutCamMsgEditBar, false);
        }
        return editerMode != editMode;
    }

    private boolean exitEditMode() {
        return setEditerMode(false);
    }

    private boolean exitDateSelectMode() {
        if (hasFaceHeader) {
            return false;
        }
        boolean reset = tvCamMessageListDate.getTag() == null ||
                ((int) tvCamMessageListDate.getTag() == R.drawable.wonderful_arrow_down);
        tvCamMessageListDate.setTag(reset ? R.drawable.wonderful_arrow_up : R.drawable.wonderful_arrow_down);
        tvCamMessageListEdit.setEnabled(!reset);
        if (!reset) {
            ViewUtils.setDrawablePadding(tvCamMessageListDate, R.drawable.wonderful_arrow_down, 2);
            AnimatorUtils.slideOut(fLayoutCamMessageListTimeline, false);
            return true;
        }
        return false;
    }

    @OnClick(R.id.quick_top)
    public void clickedQuickTop() {
        AppLogger.w("clickedQuickTop");
        aplCamMessageAppbar.setExpanded(true);
        layoutManager.scrollToPosition(0);
    }
}
