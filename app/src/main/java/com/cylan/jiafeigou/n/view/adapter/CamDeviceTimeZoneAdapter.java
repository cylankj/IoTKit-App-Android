package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.widget.FateLineView;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/15 18:06
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class CamDeviceTimeZoneAdapter extends SuperAdapter<TimeZoneBean> {

    private static final int TYPE_COUNT = 2;

    private static final int TYPE_VISIBLE = 0;//正常显示类型

    private static final int TYPE_INVISIBLE = 1;//不显示类型

    public CamDeviceTimeZoneAdapter(Context context,
                                    List<TimeZoneBean> items,
                                    IMulItemViewType<TimeZoneBean> mulItemViewType) {
        super(context,items,mulItemViewType);
    }

    /**
     * 定义接口，方法，变量。用来实现
     */
    private OnRecyclerViewListener onRecyclerViewListener;

    public interface OnRecyclerViewListener{ //回调点击
        void onItemClick(View view,int position);
    }

    public void setOnRecyclerViewListener(OnRecyclerViewListener mOnItemClickListener){
        this.onRecyclerViewListener = mOnItemClickListener;
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, int layoutPosition, TimeZoneBean item) {
        if(viewType == TYPE_VISIBLE){
            initVisible(holder, layoutPosition);
            handleVisibleState(holder,layoutPosition, item);
            if(onRecyclerViewListener!=null){
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getLayoutPosition();
                        onRecyclerViewListener.onItemClick(holder.itemView,pos);
                    }
                });
            }
        }else if(viewType == TYPE_INVISIBLE){
            initInvisible(holder,layoutPosition);
        }
    }


    private void initVisible(SuperViewHolder holder, final int layoutPosition) {
        setupPosition2View(holder, R.id.tv_timezone_item, layoutPosition);
    }

    private void initInvisible(SuperViewHolder holder, final int layoutPosition) {
        setupPosition2View(holder, R.id.tv_information_timezone_noresult, layoutPosition);
    }

    private void setupPosition2View(SuperViewHolder holder, final int viewId, final int position) {
        final View view = holder.getView(viewId);
        if (view != null) {
            view.setTag(position);
        }
    }


    private void handleVisibleState(SuperViewHolder holder, int layoutPosition, TimeZoneBean bean) {
        holder.setText(R.id.tv_timezone_item,bean.getName());
    }

    @Override
    protected IMulItemViewType<TimeZoneBean> offerMultiItemViewType() {
        return new IMulItemViewType<TimeZoneBean>() {

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position, TimeZoneBean timeZoneBean) {
                return timeZoneBean.visibleType; //0.正常显示 ，1.
            }


            @Override
            public int getLayoutId(int viewType) {
                return viewType == TYPE_VISIBLE ?
                        R.layout.fragment_edit_timezone_item:
                        R.layout.fragment_edit_timezone_none;
            }
        };
    }
}
