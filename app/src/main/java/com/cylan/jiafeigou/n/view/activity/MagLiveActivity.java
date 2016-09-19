package com.cylan.jiafeigou.n.view.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.n.view.adapter.MagActivityAdapter;
import com.cylan.jiafeigou.n.view.home.HomeSettingAboutFragment;
import com.cylan.jiafeigou.n.view.mag.MagLiveFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.utils.RandomUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 创建者     谢坤
 * 创建时间   2016/7/26 13:51
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MagLiveActivity extends BaseFullScreenFragmentActivity {


    private int currentType;//当前item类型

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
    private MagLiveFragment magLiveFragment;
    private List<MagBean> magList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mag_live);
        //实例化msgLiveFragment
        magLiveFragment = MagLiveFragment.newInstance(new Bundle());
        ButterKnife.bind(this);
        //用来存放，所需要的bean对象
        initTopBar();
        initData();
        initView();
    }

    private void initData() {
        magList = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            MagBean magBean = new MagBean();
            magBean.setIsOpen(i % 2 == 0 ? false : true);
            if (i == 0) {
                magBean.setVisibleType(0);
            } else if (i == 5) {
                magBean.setVisibleType(1);
            } else if (i == 6) {
                magBean.setVisibleType(0);
            } else {
                magBean.setVisibleType(0);
            }
            magBean.setMagTime(System.currentTimeMillis() - RandomUtils.getRandom(24 * 3600));
            magList.add(magBean);
        }
    }

    /**
     * 获得当前日期的方法
     *
     * @param
     */
    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M");
        String nowDate = sdf.format(new Date());
        return nowDate;
    }

    /**
     * 初始化recycleView视图
     */
    private void initView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        RvMagState.setLayoutManager(layoutManager);
        MagActivityAdapter adapter = new MagActivityAdapter(getApplication(), magList, null);
        RvMagState.setAdapter(adapter);
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
        loadFragment(android.R.id.content, magLiveFragment);
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

}
