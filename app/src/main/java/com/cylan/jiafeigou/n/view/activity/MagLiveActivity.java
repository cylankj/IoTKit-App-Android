package com.cylan.jiafeigou.n.view.activity;

import android.graphics.Color;
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
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.n.view.mag.MagLiveFragment;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.FateLineView;
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

    private static final int TYPE_COUNT = 2;

    private static final int TYPE_VISIBLE = 0;//正常显示类型

    private static final int TYPE_INVISIBLE = 1;//不显示类型

    private int currentType;//当前item类型

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
    private List<MagBean> magList;

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
        initData();
        initView();
    }

    private void initData() {
        magList = new ArrayList<>();

        for (int i=0;i<=10;i++){
            MagBean magBean = new MagBean();
            magBean.setIsOpen(i%2==0?false:true);
            if(i==0){
                magBean.setMagData("今天");
                magBean.setVisibleType(0);
            }else if(i==5){
                magBean.setVisibleType(1);
            }else if(i==6){
                magBean.setMagData(getDate()+"月");
                magBean.setVisibleType(0);
            }else {
                magBean.setMagData("");
                magBean.setVisibleType(0);
            }
            magBean.setMagTime(System.currentTimeMillis() - RandomUtils.getRandom(24 * 3600));
            magList.add(magBean);
        }
    }

    /**
     * 获得当前日期的方法
     */
    public String getDate(){
        SimpleDateFormat sdf=new SimpleDateFormat("dd/M");
        String nowDate=sdf.format(new Date());
        return nowDate;
    }

    /**
     * long类型转换为时间值类型
     */
    public static String longToDate(long lo){
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");
        return sd.format(date);
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
            return magList.size();
        }

        @Override
        public int getItemViewType(int position) {

            if (0 == magList.get(position).getVisibleType()) {
                    return TYPE_VISIBLE;//正常显示类型
            } else if (1 == magList.get(position).getVisibleType()) {
                    return TYPE_INVISIBLE;//不显示类型
            } else {
                    return 100;
            }
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            currentType = getItemViewType(position);
            if(currentType == TYPE_VISIBLE) {
                ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_mag_live_item, parent, false);
                    View.inflate(getApplicationContext(), R.layout.activity_mag_live_item, null);
                    holder.mTvDay = (TextView) convertView.findViewById(R.id.tv_mag_live_day);
                    holder.mTvTime = (TextView) convertView.findViewById(R.id.tv_mag_live_time);
                    holder.mFlv = (FateLineView) convertView.findViewById(R.id.flv_mag_live);
                    convertView.setTag(holder);
                    holder.mFlv.setTag("toGreen");
                } else {
                    holder = (ViewHolder) convertView.getTag();
                    holder.mFlv.setTag("toGreen");
                }

                //每条的第一个设置内外圈颜色
                if (position == 0) {
                        holder.mFlv.setOuterCircleColor(R.color.color_bae3bc);
                        holder.mFlv.setTag("toGreen");
                }
                //把数据插入其中
                holder.mTvDay.setText(magList.get(position).magDate);
                if (magList.get(position).isOpen == true) {
                    holder.mTvTime.setText(longToDate(magList.get(position).magTime) + " " + "打开");
                } else {
                    holder.mTvTime.setText(longToDate(magList.get(position).magTime) + " " + "关闭");
                }
            }else if(currentType == TYPE_INVISIBLE){
                viewVisibleHolder visibleHolder;
                if(convertView==null){
                    visibleHolder = new viewVisibleHolder();
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_mag_live_item_invisible, parent, false);
                    View.inflate(getApplicationContext(), R.layout.activity_mag_live_item_invisible, null);
                    visibleHolder.mFlv = (FateLineView) convertView.findViewById(R.id.flv_mag_live_invisible);
                    convertView.setTag(visibleHolder);
                }else {
                    visibleHolder = (viewVisibleHolder) convertView.getTag();
                }
            }
            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return magList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    /**
     * 正常显示的viewHodler
     */
    class ViewHolder{
        TextView mTvDay,mTvTime;
        FateLineView mFlv;
    }

    /**
     * 只显示一条直线的viewHolder
     */
    class viewVisibleHolder{
        FateLineView mFlv;
    }
}
