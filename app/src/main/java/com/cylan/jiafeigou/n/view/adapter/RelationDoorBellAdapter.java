package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.BellBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/10/14
 * 描述：
 */
public class RelationDoorBellAdapter extends SuperAdapter<BellBean> {

    public OnUnRelaItemClickListener listener;

    public interface OnUnRelaItemClickListener {
        void unRelativeClick(SuperViewHolder holder, int viewType, int layoutPosition, BellBean item);
    }

    public void setOnUnRelaItemClickListener(OnUnRelaItemClickListener listener) {
        this.listener = listener;
    }

    public RelationDoorBellAdapter(Context context, List<BellBean> items, IMulItemViewType<BellBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }


    public void onBind(final SuperViewHolder holder, final int viewType,
                       final int layoutPosition, final BellBean item) {
        holder.setText(R.id.tv_door_bell_name, item.nickName);
        holder.setOnClickListener(R.id.tv_btn_relative, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.unRelativeClick(holder, viewType, layoutPosition, item);
                }
            }
        });
    }

    @Override
    protected IMulItemViewType<BellBean> offerMultiItemViewType() {
        return new IMulItemViewType<BellBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, BellBean bellInfoBean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_cloud_correlation_door_bell_items;
            }
        };
    }
}
