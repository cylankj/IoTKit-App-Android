package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.BellInfoBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * 作者：zsl
 * 创建时间：2016/10/14
 * 描述：
 */
public class RelationDoorBellAdapter extends SuperAdapter<BellInfoBean> {

    public RelationDoorBellAdapter(Context context, List<BellInfoBean> items, IMulItemViewType<BellInfoBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, BellInfoBean item) {
        holder.setText(R.id.tv_door_bell_name,item.nickName);
    }

    @Override
    protected IMulItemViewType<BellInfoBean> offerMultiItemViewType() {
        return new IMulItemViewType<BellInfoBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }
            @Override
            public int getItemViewType(int position, BellInfoBean bellInfoBean) {
                return 0;
            }
            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_cloud_correlation_door_bell_items;
            }
        };
    }
}
