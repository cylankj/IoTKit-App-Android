package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
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

    private ImageView clientImage;

    public boolean isFirstItem = true;

    public HomeMineHelpSuggestionAdapter(Context context,
                                         List<MineHelpSuggestionBean> items,
                                         IMulItemViewType<MineHelpSuggestionBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MineHelpSuggestionBean item) {
        if (viewType == 1){     //客户端
            holder.setText(R.id.tv_mine_suggestion_client_time, getNowDate(item.getDate()));
            holder.setText(R.id.tv_mine_suggestion_client_speak, getNowDate(item.getText()));

            if (isFirstItem){
                holder.setVisibility(R.id.tv_mine_suggestion_client_time,View.VISIBLE);
            }else {
                holder.setVisibility(R.id.tv_mine_suggestion_client_time,View.INVISIBLE);
            }

            if (checkIsOverTime(item.getDate()) && !isFirstItem){
                holder.setVisibility(R.id.tv_mine_suggestion_client_time,View.VISIBLE);
            }else {
                holder.setVisibility(R.id.tv_mine_suggestion_client_time,View.INVISIBLE);
            }

            clientImage = holder.getView(R.id.iv_mine_suggestion_client);
            Glide.with(getContext()).load(item.getIcon())
                    .asBitmap().centerCrop()
                    .error(R.drawable.icon_mine_head_normal)
                    .into(new BitmapImageViewTarget(clientImage) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            clientImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });

        }else {     //服务端

            if (isFirstItem){
                holder.setVisibility(R.id.tv_mine_suggestion_server_time,View.VISIBLE);
            }else {
                holder.setVisibility(R.id.tv_mine_suggestion_server_time,View.INVISIBLE);
            }

            if (checkIsOverTime(item.getDate()) && !isFirstItem){
                holder.setVisibility(R.id.tv_mine_suggestion_server_time,View.VISIBLE);
            }else {
                holder.setVisibility(R.id.tv_mine_suggestion_server_time,View.INVISIBLE);
            }

            holder.setText(R.id.tv_mine_suggestion_server_speak,item.getText());

            holder.setText(R.id.tv_mine_suggestion_server_time,getNowDate(item.getDate()));

            holder.setBackgroundResource(R.id.iv_mine_suggestion_server, R.drawable.pic_head);
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
        String nowDate = sdf.format(new Date(magDate));
        return nowDate;
    }

    /**
     * 检测是否超时
     * @param time
     * @return
     */
    public boolean checkIsOverTime(String time){
        long lastItemTime = Long.parseLong(time);
        if (System.currentTimeMillis() - lastItemTime > 5){
            return true;
        }else {
            return false;
        }
    }
}
