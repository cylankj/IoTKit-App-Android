package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;
import com.cylan.utils.DensityUtils;

import java.util.List;

/**
 * Created by cylan-hunt on 16-7-13.
 */
public class CamMessageListAdapter extends SuperAdapter<CamMessageBean> {

    /**
     * 一张图片，两张图片，三张图片，只有文字。
     */
    private static final int MAX_TYPE = 4;

    /**
     * 0： 正常，1:编辑
     */
    private int mode;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_EDIT = 1;

    //图片Container的总体宽度,可能有3条,可能有2条.
    private final int pic_container_width;
    private final int pic_container_height;

    public CamMessageListAdapter(Context context, List<CamMessageBean> items, IMulItemViewType<CamMessageBean> mulItemViewType) {
        super(context, items, mulItemViewType);
        //这个40是根据layout中的marginStart,marginEnd距离提取出来,如果需要修改,参考这个layout
        pic_container_width = Resources.getSystem().getDisplayMetrics().widthPixels - DensityUtils.dip2px(40);
        pic_container_height = DensityUtils.dip2px(225 - 48 - 36 - 5);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, CamMessageBean item) {
        switch (viewType) {
            case 0:
                handleTextContentLayout(holder, layoutPosition, item);
                break;
            case 1:
                handlePicsLayout(holder, layoutPosition, item);
                break;
        }
    }

    private void handleTextContentLayout(SuperViewHolder holder,
                                         int layoutPosition,
                                         CamMessageBean item) {

    }

    private void handlePicsLayout(SuperViewHolder holder,
                                  int layoutPosition,
                                  CamMessageBean item) {
        final int count = item.urlList.size();
        //根据图片总数,设置view的Gone属性
        for (int i = 2; i >= 0; i--) {
            holder.setVisibility(R.id.imgV_cam_message_pic_0 + i,
                    count - 1 >= i ? View.VISIBLE : View.GONE);
        }
        for (int i = 0; i < item.urlList.size(); i++) {
            Glide.with(getContext())
                    .load(item.urlList.get(i))
                    .placeholder(R.drawable.wonderful_pic_place_holder)
                    .override(pic_container_width / count, pic_container_height)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into((ImageView) holder.getView(R.id.imgV_cam_message_pic_0 + i));
        }
    }

    @Override
    protected IMulItemViewType<CamMessageBean> offerMultiItemViewType() {
        return new IMulItemViewType<CamMessageBean>() {
            @Override
            public int getViewTypeCount() {
                return MAX_TYPE;
            }

            @Override
            public int getItemViewType(int position, CamMessageBean camMessageBean) {
                return camMessageBean.viewType;
            }

            @Override
            public int getLayoutId(int viewType) {
                switch (viewType) {
                    case 0:
                        return R.layout.layout_item_cam_msg_list_0;
                    case 1:
                        return R.layout.layout_item_cam_msg_list_1;
                    default:
                        return R.layout.layout_wonderful_empty;
                }
            }
        };
    }
}
