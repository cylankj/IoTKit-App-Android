package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.CamMessageBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

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

    public CamMessageListAdapter(Context context, List<CamMessageBean> items, IMulItemViewType<CamMessageBean> mulItemViewType) {
        super(context, items, mulItemViewType);
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
                handleOnePicLayout(holder, layoutPosition, item);
                break;
            case 2:
                handleTwoPicLayout(holder, layoutPosition, item);
                break;
            case 3:
                handleThreePicLayout(holder, layoutPosition, item);
                break;
        }
    }

    private void handleTextContentLayout(SuperViewHolder holder, int layoutPosition, CamMessageBean item) {

    }

    private void handleOnePicLayout(SuperViewHolder holder, int layoutPosition, CamMessageBean item) {

    }

    private void handleTwoPicLayout(SuperViewHolder holder, int layoutPosition, CamMessageBean item) {

    }

    private void handleThreePicLayout(SuperViewHolder holder, int layoutPosition, CamMessageBean item) {

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
                    case 2:
                        return R.layout.layout_item_cam_msg_list_2;
                    case 3:
                        return R.layout.layout_item_cam_msg_list_3;
                    default:
                        return R.layout.layout_wonderful_empty;
                }
            }
        };
    }
}
