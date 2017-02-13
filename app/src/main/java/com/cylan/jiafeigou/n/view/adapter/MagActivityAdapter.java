package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/5 11:23
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MagActivityAdapter extends SuperAdapter<MagBean> {


    private static final int TYPE_COUNT = 2;

    private boolean currentState;             //true 为开， false为关

    private static final int TYPE_VISIBLE = 0;//正常显示类型

    private static final int TYPE_INVISIBLE = 1;//不显示类型

    public MagActivityAdapter(Context context, List<MagBean> items,
                              IMulItemViewType<MagBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    public void setCurrentState(boolean currentState) {
        this.currentState = currentState;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MagBean item) {

        if (viewType == TYPE_VISIBLE) {
            handleVisibleState(holder, layoutPosition, item);
        } else if (viewType == TYPE_INVISIBLE) {
            handleInVisibleState(holder, layoutPosition, item);
        }
    }

    private void handleInVisibleState(SuperViewHolder holder, int layoutPosition, MagBean item) {
        Drawable drawable = getContext().getResources().getDrawable(R.drawable.icon_dot_gary);
        int intrinsicWidth = drawable.getIntrinsicWidth();
        ImageView view = (ImageView) holder.getView(R.id.iv_mag_live);
        int width = view.getWidth();
        LinearLayout ll_container = holder.getView(R.id.ll_icon_container);
        ViewGroup.LayoutParams layoutParams = ll_container.getLayoutParams();
        layoutParams.width = intrinsicWidth;
        ll_container.setLayoutParams(layoutParams);
        view.setVisibility(View.GONE);
    }

    private void handleVisibleState(SuperViewHolder holder, int layoutPosition, MagBean bean) {
        //每条的第一个设置内外圈颜色
        ImageView view = (ImageView) holder.getView(R.id.iv_mag_live);
        if (bean.isFirst) {
            if (currentState) {
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.magnetometer_pic_dot_red));
            } else {
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.magnetometer_pic_dot_blue));
            }
        } else {
            view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.icon_dot_gary));
        }

        if (layoutPosition == 1) {
            if (checkIsToday(bean.getMagTime())) {
                holder.setText(R.id.tv_mag_live_day, ContextUtils.getContext().getString(R.string.TODAY));
            } else {
                holder.setText(R.id.tv_mag_live_day, converStr(getDate(bean.magTime) + ContextUtils.getContext().getString(R.string.MONTHS)));
            }
        } else if (layoutPosition > 1) {
            if (checkSame(bean.magTime, getList().get(layoutPosition - 1).magTime)) {
                holder.setText(R.id.tv_mag_live_day, "");
            } else {
                holder.setText(R.id.tv_mag_live_day, converStr(getDate(bean.magTime) + ContextUtils.getContext().getString(R.string.MONTHS)));
            }
        } else {
            holder.setText(R.id.tv_mag_live_day, "");
            holder.setText(R.id.tv_mag_live_time, "");
        }

        if (bean.isOpen) {
            holder.setText(R.id.tv_mag_live_time, longToDate(bean.magTime) + " " + ContextUtils.getContext().getString(R.string.MAGNETISM_ON));
        } else {
            holder.setText(R.id.tv_mag_live_time, longToDate(bean.magTime) + " " + ContextUtils.getContext().getString(R.string.MAGNETISM_OFF));
        }
    }

    /**
     * 检测是否是今天
     *
     * @param magTime
     * @return
     */
    public boolean checkIsToday(long magTime) {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        Date date = new Date(magTime);
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);

            if (diffDay == 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * 检测是否和前一天相等
     *
     * @return
     */
    public boolean checkSame(long thisTime, long lastTime) {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(thisTime);
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        Date date = new Date(lastTime);
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);

            if (diffDay == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected IMulItemViewType<MagBean> offerMultiItemViewType() {
        return new IMulItemViewType<MagBean>() {
            @Override
            public int getViewTypeCount() {
                return TYPE_COUNT;
            }

            @Override
            public int getItemViewType(int position, MagBean magBean) {
                return magBean.visibleType; //0.正常显示 ，1.只显示一条时间线的虚线
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType == TYPE_VISIBLE ?
                        R.layout.activity_mag_live_item :
                        R.layout.activity_mag_live_item_invisible;
            }
        };
    }

    /**
     * 获得当前日期的方法
     *
     * @param magDate
     */
    public String getDate(long magDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M");
        String nowDate = sdf.format(new Date(magDate));
        return nowDate;
    }

    public Spannable converStr(String str) {
        Spannable sb = new SpannableString(str);
        sb.setSpan(new AbsoluteSizeSpan(17, true), 0, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sb.setSpan(new AbsoluteSizeSpan(12, true), 3, str.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

    /**
     * long类型转换为时间值类型
     */
    public String longToDate(long lo) {
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");
        return sd.format(date);
    }

}
