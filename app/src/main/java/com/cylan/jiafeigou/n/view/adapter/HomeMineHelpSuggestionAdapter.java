package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MineHelpSuggestionBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/18 15:43
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class HomeMineHelpSuggestionAdapter extends SuperAdapter<MineHelpSuggestionBean> {

    private static final int TYPE_COUNT = 2;

    private static final int TYPE_SERVER = 0;//服务端类型

    private static final int TYPE_Client = 1;//客户端类型

    public HomeMineHelpSuggestionAdapter(Context context,
                                         List<MineHelpSuggestionBean> items,
                                         IMulItemViewType<MineHelpSuggestionBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MineHelpSuggestionBean item) {
        if (viewType == TYPE_SERVER) {
            initServer(holder, layoutPosition);
            handleServerState(holder, layoutPosition, item);
        } else if (viewType == TYPE_Client) {
            initClient(holder, layoutPosition);
            handleClientState(holder, layoutPosition, item);
        }
    }

    private void initServer(SuperViewHolder holder, final int layoutPosition) {
        setupPosition2View(holder, R.id.tv_mine_suggestion_server_time, layoutPosition);
        setupPosition2View(holder, R.id.tv_mine_suggestion_server_speak, layoutPosition);
        setupPosition2View(holder, R.id.iv_mine_suggestion_server, layoutPosition);
    }

    private void initClient(SuperViewHolder holder, final int layoutPosition) {
        setupPosition2View(holder, R.id.tv_mine_suggestion_client_time, layoutPosition);
        setupPosition2View(holder, R.id.tv_mine_suggestion_client_speak, layoutPosition);
        setupPosition2View(holder, R.id.iv_mine_suggestion_client, layoutPosition);
    }

    private void setupPosition2View(SuperViewHolder holder, final int viewId, final int position) {
        final View view = holder.getView(viewId);
        if (view != null) {
            view.setTag(position);
        }
    }

    private void handleServerState(SuperViewHolder holder, int layoutPosition, MineHelpSuggestionBean bean) {
        if (layoutPosition != 0) {
            holder.itemView.setPadding(0, 34, 0, 10);
        } else {
            holder.itemView.setPadding(0, 20, 0, 0);
        }

        if (bean.isShowTime) {
            holder.setText(R.id.tv_mine_suggestion_server_time, getNowDate(bean.getDate()));
//            holder.setBackgroundResource(R.id.iv_mine_suggestion_server, bean.getIcon());
            holder.setText(R.id.tv_mine_suggestion_server_speak, bean.getText());
        } else {
            holder.setVisibility(R.id.tv_mine_suggestion_server_time, View.GONE);
//            holder.setBackgroundResource(R.id.iv_mine_suggestion_server, bean.getIcon());
            holder.setText(R.id.tv_mine_suggestion_server_speak, bean.getText());
        }
    }

    private void handleClientState(SuperViewHolder holder, int layoutPosition, MineHelpSuggestionBean bean) {
        if (layoutPosition != 0) {
            holder.itemView.setPadding(0, 34, 0, 10);
        } else {
            holder.itemView.setPadding(0, 20, 0, 0);
        }

        if (bean.isShowTime) {
            holder.setText(R.id.tv_mine_suggestion_client_time, getNowDate(bean.getDate()));
//            holder.setBackgroundResource(R.id.iv_mine_suggestion_client, bean.getIcon());
            holder.setText(R.id.tv_mine_suggestion_client_speak, bean.getText());
        } else {
            holder.setVisibility(R.id.tv_mine_suggestion_client_time, View.GONE);
//            holder.setBackgroundResource(R.id.iv_mine_suggestion_client, bean.getIcon());
            holder.setText(R.id.tv_mine_suggestion_client_speak, bean.getText());
        }
    }

    @Override
    protected IMulItemViewType<MineHelpSuggestionBean> offerMultiItemViewType() {
        return new IMulItemViewType<MineHelpSuggestionBean>() {
            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position, MineHelpSuggestionBean bean) {
                return bean.type; //0.显示服务端 ，1.显示客户端
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType == TYPE_SERVER ?
                        R.layout.fragment_mine_suggestion_server :
                        R.layout.fragment_mine_suggestion_client;
            }
        };
    }

    /**
     * 获得当前日期的方法
     *
     * @param magDate
     */
    public String getNowDate(String magDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String nowDate = sdf.format(new Date());
        return nowDate;
    }
}
