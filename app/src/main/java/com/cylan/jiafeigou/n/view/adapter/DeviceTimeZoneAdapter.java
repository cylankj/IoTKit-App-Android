package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.TimeZoneBean;
import com.cylan.jiafeigou.support.superadapter.IMulItemViewType;
import com.cylan.jiafeigou.support.superadapter.SuperAdapter;
import com.cylan.jiafeigou.support.superadapter.internal.SuperViewHolder;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/15 18:06
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class DeviceTimeZoneAdapter extends SuperAdapter<TimeZoneBean> {

    public DeviceTimeZoneAdapter(Context context) {
        super(context, null, null);
    }

    private String chooseId;

    public void setChooseId(String chooseId) {
        this.chooseId = chooseId;
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, int layoutPosition, TimeZoneBean item) {
        holder.setText(R.id.tv_timezone_id, item.getName());
        holder.setText(R.id.tv_timezone_gmt, item.getGmt());
        holder.setVisibility(R.id.imv_item_check, TextUtils.equals(item.getId(), chooseId) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected IMulItemViewType<TimeZoneBean> offerMultiItemViewType() {
        return new IMulItemViewType<TimeZoneBean>() {


            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, TimeZoneBean timeZoneBean) {
                return 0;
            }


            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_edit_timezone_item;
            }
        };
    }
}
