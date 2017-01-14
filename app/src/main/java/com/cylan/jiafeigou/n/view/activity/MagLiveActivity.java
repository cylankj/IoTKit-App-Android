package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.contract.mag.MagLiveContract;
import com.cylan.jiafeigou.n.mvp.impl.mag.MagLivePresenterImp;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.n.view.adapter.MagActivityAdapter;
import com.cylan.jiafeigou.n.view.mag.MagLiveFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/26 13:51
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MagLiveActivity extends BaseFullScreenFragmentActivity implements MagLiveContract.View {


    @BindView(R.id.ll_no_mesg)
    LinearLayout llNoMesg;
    @BindView(R.id.imgV_msg_title_top_back)
    ImageView imgVTopBack;
    @BindView(R.id.imgV_msg_title_top_setting)
    ImageView imgVTopSetting;
    @BindView(R.id.imgV_msg_title_top_door)
    ImageView imgVTopDoor;
    @BindView(R.id.rLayout_mag_live_top_bar)
    RelativeLayout rLayoutMsgLiveTopBar;
    @BindView(R.id.rv_mag_state)
    RecyclerView RvMagState;

    private int currentType;//当前item类型
    private MagLiveFragment magLiveFragment;
    private MagActivityAdapter adapter;
    private MagLiveContract.Presenter presenter;
    private String uuid ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mag_live);
        ButterKnife.bind(this);
        this.uuid = getIntent().getStringExtra(JConstant.KEY_DEVICE_ITEM_UUID);
        initPresenter();
        //用来存放，所需要的bean对象
        initTopBar();
        initDoorState(presenter.getDoorCurrentState());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (presenter != null) presenter.stop();
    }

    private void initPresenter() {
        presenter = new MagLivePresenterImp(this);
    }

    private void initMagLiveFragmentLisenter() {
        magLiveFragment.setOnClearDoorOpenRecord(new MagLiveFragment.OnClearDoorOpenRecordLisenter() {
            @Override
            public void onClear() {
                adapter.clear();
                adapter.notifyDataSetHasChanged();
                showNoMesg();
            }
        });
    }

    /**
     * 初始化门的状态
     *
     * @param isOpen
     */
    private void initDoorState(boolean isOpen) {
        if (isOpen) {
            imgVTopDoor.setImageDrawable(getResources().getDrawable(R.drawable.icon_magnetometer_top_open));
            rLayoutMsgLiveTopBar.setBackground(getResources().getDrawable(R.drawable.bg_magnetometer_top_open));
        } else {
            imgVTopDoor.setImageDrawable(getResources().getDrawable(R.drawable.icon_magnetometer_top_close));
            rLayoutMsgLiveTopBar.setBackground(getResources().getDrawable(R.drawable.bg_magnetometer_top_close));
        }
    }

    /**
     * 获得当前日期的方法
     * @param
     */
    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M");
        String nowDate = sdf.format(new Date());
        return nowDate;
    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(rLayoutMsgLiveTopBar);
    }

    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        finishExt();
    }

    @OnClick(R.id.imgV_msg_title_top_back)
    public void onBack() {
        onBackPressed();
    }

    /**
     * 当点击右上角的螺母按钮时，跳转到设备设置页面
     */
    @OnClick(R.id.imgV_msg_title_top_setting)
    public void onClickSetting() {
        //实例化msgLiveFragment
        Bundle bundle = new Bundle();
        bundle.putString(KEY_DEVICE_ITEM_UUID, uuid);
        magLiveFragment = MagLiveFragment.newInstance(bundle);
        loadFragment(android.R.id.content, magLiveFragment);
        initMagLiveFragmentLisenter();
    }

    @OnClick(R.id.imgV_msg_title_top_door)
    public void onClickTest() {
        if (presenter != null) {
            presenter.getMesgFromMag();
        }
    }

    /**
     * 用来加载fragment的方法。
     */
    private void loadFragment(int id, MagLiveFragment fragment) {
        Fragment f = getSupportFragmentManager().findFragmentByTag("MsgLiveInformationFragment");
        if (f != null) {
            AppLogger.d("fragment is not null");
            return;
        }
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id, fragment, "MsgLiveInformationFragment")
                .addToBackStack("MagLiveActivity")
                .commit();
    }

    @Override
    public void setPresenter(MagLiveContract.Presenter presenter) {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    /**
     * 初始化消息列表显示
     */
    @Override
    public void initRecycleView(List<MagBean> list) {

        if (list != null && list.size() > 0) {
            // 先插入一条空白的
            MagBean nullBean = new MagBean();
            nullBean.visibleType = 1;

            //保证只有第一条的圈圈为彩色
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    list.get(i).isFirst = true;
                } else {
                    list.get(i).isFirst = false;
                }
            }
            list.add(0,nullBean);
            hideNoMesg();
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            RvMagState.setLayoutManager(layoutManager);
            adapter = new MagActivityAdapter(getContext(), list, null);
            adapter.setCurrentState(presenter.getDoorCurrentState());
            RvMagState.setAdapter(adapter);
        } else {
            List<MagBean> magBeanList = new ArrayList<>();
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            RvMagState.setLayoutManager(layoutManager);
            adapter = new MagActivityAdapter(getContext(), magBeanList, null);
            adapter.setCurrentState(presenter.getDoorCurrentState());
            RvMagState.setAdapter(adapter);
            showNoMesg();
        }
    }

    /**
     * 添加一条门磁消息
     */
    @Override
    public void addOneMagMesg(MagBean addBean) {
        // 以下为模拟测试 判断当前的时间和列表的第一条数据的时间是否相等 相等 直接添加为第一条， 不相等，先添加一条空白的
        hideNoMesg();
        if (adapter != null) {
            if (adapter.getItemCount() >= 2) {
                MagBean firstBean = adapter.getList().get(1);
                if (adapter.checkSame(addBean.magTime, firstBean.magTime)) {
                    adapter.getItem(1).isFirst = false;
                    adapter.add(1, addBean);
                    adapter.notifyItemRangeChanged(1, adapter.getItemCount());
                    RvMagState.smoothScrollToPosition(1);
                    presenter.saveIntoDb(addBean);
                } else {
                    // 先插入一条空白的
                    MagBean nullBean = new MagBean();
                    nullBean.magTime = addBean.magTime - 1;
                    nullBean.visibleType = 1;
                    presenter.saveIntoDb(nullBean);

                    adapter.getItem(1).isFirst = false;
                    adapter.add(1, nullBean);
                    adapter.notifyItemRangeChanged(1, adapter.getItemCount());
                    RvMagState.smoothScrollToPosition(1);

                    adapter.add(1, addBean);
                    adapter.notifyItemRangeChanged(1, adapter.getItemCount());
                    RvMagState.smoothScrollToPosition(1);
                    presenter.saveIntoDb(addBean);
                }

            } else {
                // 先插入一条空白的
                MagBean nullBean = new MagBean();
                nullBean.visibleType = 1;
                adapter.add(0, nullBean);
                adapter.notifyDataSetHasChanged();

                //第一条为空 直接插入
                adapter.add(1, addBean);
                adapter.notifyItemRangeChanged(1, adapter.getItemCount());
                RvMagState.smoothScrollToPosition(1);
                presenter.saveIntoDb(addBean);
            }

        }
    }

    /**
     * 没有消息记录
     */
    @Override
    public void showNoMesg() {
        llNoMesg.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏无消息
     */
    @Override
    public void hideNoMesg() {
        llNoMesg.setVisibility(View.GONE);
    }
}
