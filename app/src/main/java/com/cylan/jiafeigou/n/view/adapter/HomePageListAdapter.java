package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.model.BaseBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * Created by hunt on 16-5-24.
 */

public class HomePageListAdapter extends SuperAdapter<BaseBean> {


    public HomePageListAdapter(Context context, List<BaseBean> items, IMulItemViewType<BaseBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, BaseBean item) {
        holder.setText(R.id.tv_content, item.id + "");
    }

    @Override
    protected IMulItemViewType<BaseBean> offerMultiItemViewType() {
        return new IMulItemViewType<BaseBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, BaseBean baseBean) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.layout_item_home_page_list;
            }
        };
    }
}
