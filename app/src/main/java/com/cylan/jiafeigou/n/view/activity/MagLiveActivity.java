package com.cylan.jiafeigou.n.view.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.mag.MagLiveFragment;
import com.cylan.jiafeigou.utils.ViewUtils;

import java.util.ArrayList;
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

    @BindView(R.id.imgV_msg_title_top_back)
    ImageView imgVTopBack;

    @BindView(R.id.imgV_msg_title_top_setting)
    ImageView imgVTopSetting;

    @BindView(R.id.imgV_msg_title_top_door)
    ImageView imgVTopDoor;

    @BindView(R.id.rLayout_mag_live_top_bar)
    RelativeLayout rLayoutMsgLiveTopBar;

    @BindView(R.id.lv_mag_state)
    ListView lvMagState;
    private MagLiveFragment magLiveFragment;
    private SimpleAdapter simpleAdapter;
    private List<String> timeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mag_live);
        //实例化msgLiveFragment
        magLiveFragment = MagLiveFragment.newInstance(new Bundle());
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);
        initTopBar();
        initView();
        timeList = new ArrayList<>();
        initData();
    }

    private void initData() {
        //模拟数据添加到listView
        for (int i=0; i<=20; i++){

        }
    }

    private void initView() {
        simpleAdapter = new SimpleAdapter();
        lvMagState.setAdapter(simpleAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
    }

    @OnClick(R.id.imgV_msg_title_top_back)
    public void onBack(){
        onBackPressed();
    }


    /**
     * 当点击右上角的螺母按钮时，跳转到设备设置页面
     */
    @OnClick(R.id.imgV_msg_title_top_setting)
    public void onClickSetting(){
        loadFragment(R.id.fLayout_msg_information, magLiveFragment);
    }

    /**
     * 用来加载fragment的方法。
     */
    private void loadFragment(int id,MagLiveFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id,fragment,"MsgLiveInformationFragment")
                .addToBackStack("MagLiveActivity")
                .commit();
    }

    public class SimpleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}
