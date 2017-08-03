package com.cylan.jiafeigou.n.view.cam.item;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yanzhendong on 2017/8/1.
 */

public class AISelectionItem extends AbstractItem<AISelectionItem, AISelectionItem.ViewHolder> {

    public int objType;

    public String text;
    public int icon_hl;
    public int icon;

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return 0;
    }

    public AISelectionItem(int objType) {
        this.objType = objType;
    }

    public AISelectionItem(int objType, int icon_hl, int icon, String text) {
        this.objType = objType;
        this.icon_hl = icon_hl;
        this.icon = icon;
        this.text = text;
        this.mIdentifier = objType;
        this.withSelectable(objType != -1);
    }

    @Override
    public void bindView(ViewHolder holder, List<Object> payloads) {
        super.bindView(holder, payloads);
        if (objType == -1) {//empty
            holder.ai_icon.setVisibility(View.INVISIBLE);
            holder.ai_text.setVisibility(View.INVISIBLE);
        } else {
            holder.ai_text.setText(text);
            holder.ai_icon.setImageResource(isSelected() ? icon_hl : icon);
        }
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_ai_selection;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ai_icon)
        ImageView ai_icon;
        @BindView(R.id.ai_text)
        TextView ai_text;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
