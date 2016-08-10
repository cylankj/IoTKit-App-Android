package com.cylan.jiafeigou.n.view.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.n.view.adapter.MagActivityAdapter;
import com.cylan.jiafeigou.n.view.mag.MagLiveFragment;
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
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mag_live);
        //实例化msgLiveFragment
        magLiveFragment = MagLiveFragment.newInstance(new Bundle());
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);

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
                magBean.setVisibleType(0);
            }else if(i==5){
                magBean.setVisibleType(1);
            }else if(i==6){
                magBean.setVisibleType(0);
            }else {
                magBean.setVisibleType(0);
            }
            magBean.setMagTime(System.currentTimeMillis() - RandomUtils.getRandom(24 * 3600));
            magList.add(magBean);
        }
    }

    /**
     * 获得当前日期的方法
     * @param
     */
    public String getDate(){
        SimpleDateFormat sdf=new SimpleDateFormat("dd/M");
        String nowDate=sdf.format(new Date());
        return nowDate;
    }

    /**
     * 初始化recycleView视图
     */
    private void initView() {
        LinearLayoutManager layoutManager =  new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        RvMagState.setLayoutManager(layoutManager);
        MagActivityAdapter adapter = new MagActivityAdapter(getApplication(),magList,null);
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

    /*class SimpleAdapter extends SuperAdapter<MagBean>{

        public TextView mTvDay;
        public TextView mTvTime;
        public FateLineView mFlv;
        public FateLineView mFlvInvisible;

        public SimpleAdapter(Context context, List<MagBean> items, int layoutResId) {
            super(context, items, layoutResId);
        }

        *//**
         * 绑定视图
         * @param parent
         * @param viewType
         * @return
         *//*
       *//* @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType==TYPE_VISIBLE){
                View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_mag_live_item,parent,false);
                return new ViewHolder(view);
            }else if(viewType==TYPE_INVISIBLE){
                View lineView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_mag_live_item_invisible,parent,false);
                return new ViewLineHolder(lineView);
            }
            return null;
        }

        *//**//**
         * 绑定数据
         * @param holder
         * @param position
         *//**//*
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof ViewHolder){
                //每条的第一个设置内外圈颜色
                if (position == 0) {
                    mFlv.setOuterCircleColor(R.color.color_bae3bc);
                    mFlv.setTag("toGreen");
                }
                //把数据插入其中
                mTvDay.setText(magList.get(position).magDate);
                if (magList.get(position).isOpen == true) {
                    mTvTime.setText(longToDate(magList.get(position).magTime) + " " + "打开");
                } else {
                    mTvTime.setText(longToDate(magList.get(position).magTime) + " " + "关闭");
                }
            }else if(holder instanceof ViewLineHolder){

            }
        }*//*

        *//**
         * 数据的总大小
         * @return
        @Override
        public int getItemCount() {
            return magList.size();
        }

        *//**//**
         * 数据的类型
         * @param
         * @return
         *//**//*
        @Override
        public int getItemViewType(int position) {
            if (0 == magList.get(position).getVisibleType()) {
                return TYPE_VISIBLE;//正常显示类型
            } else if (1 == magList.get(position).getVisibleType()) {
                return TYPE_INVISIBLE;//不显示类型
            } else {
                return 100;
            }
        }*//*

        @Override
        public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MagBean item) {
            if(viewType == TYPE_VISIBLE){

            }else if(viewType == TYPE_INVISIBLE){

            }
        }

        *//**
         * 正常显示的holder
         *//*
        public class ViewHolder extends RecyclerView.ViewHolder{

            public ViewHolder(View itemView) {
                super(itemView);
                mTvDay = (TextView) itemView.findViewById(R.id.tv_mag_live_day);
                mTvTime =  (TextView) itemView.findViewById(R.id.tv_mag_live_time);
                mFlv = (FateLineView) itemView.findViewById(R.id.flv_mag_live);
            }
        }

        *//**
         * 只显示一条虚线的holder
         *//*
        public class ViewLineHolder extends RecyclerView.ViewHolder{

            public ViewLineHolder(View itemView) {
                super(itemView);
                mFlvInvisible = (FateLineView) itemView.findViewById(R.id.flv_mag_live_invisible);
            }
        }
    }*/

}
