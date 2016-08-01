package com.cylan.jiafeigou.n.view.activity;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.view.mag.MagLiveFragment;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.FateLineView;

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
    private List<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mag_live);
        //实例化msgLiveFragment
        magLiveFragment = MagLiveFragment.newInstance(new Bundle());
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);

        lvMagState.setDivider(null);
        lvMagState.setDividerHeight(0);
        //用来存放，所需要的bean对象
        initTopBar();
        initView();
        initData();
    }

    private void initData() {
        if(list==null){
            list = new ArrayList<>();
        }
    }


    private void initView() {
        SimpleAdapter adapter = new SimpleAdapter();
        lvMagState.setAdapter(adapter);
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
    public void onBack() {
        onBackPressed();
    }


    /**
     * 当点击右上角的螺母按钮时，跳转到设备设置页面
     */
    @OnClick(R.id.imgV_msg_title_top_setting)
    public void onClickSetting() {
        loadFragment(R.id.fLayout_msg_information, magLiveFragment);
    }

    /**
     * 用来加载fragment的方法。
     */
    private void loadFragment(int id, MagLiveFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id, fragment, "MsgLiveInformationFragment")
                .addToBackStack("MagLiveActivity")
                .commit();
    }

    public class SimpleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 20;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView==null){
                holder = new ViewHolder();
//                convertView = View.inflate(getApplicationContext(),R.layout.activity_mag_live_item,null);
                convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_mag_live_item,parent,false);
                        View.inflate(getApplicationContext(),R.layout.activity_mag_live_item,null);
                holder.mTvDay = (TextView) convertView.findViewById(R.id.tv_mag_live_day);
                holder.mTvTime = (TextView) convertView.findViewById(R.id.tv_mag_live_time);
                holder.mFlv = (FateLineView) convertView.findViewById(R.id.flv_mag_live);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            return convertView;
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

    class ViewHolder{
        TextView mTvDay,mTvTime;
        FateLineView mFlv;
    }
}
