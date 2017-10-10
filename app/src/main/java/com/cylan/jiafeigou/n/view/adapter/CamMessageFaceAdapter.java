package com.cylan.jiafeigou.n.view.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.widget.ImageViewTip;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yanzhendong on 2017/9/29.
 */

public class CamMessageFaceAdapter extends PagerAdapter {

    private List faceItems = new ArrayList();

    private List<View> cachedViews = new ArrayList<>();
    private FaceItemEventListener listener;

    public void setFaceItems(List faceItems) {
        this.faceItems = faceItems;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return faceItems.size() / 8 + (faceItems.size() % 8 == 0 ? 0 : 1);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View contentView;
        RecyclerView recyclerView;
        if (cachedViews.size() > 0) {
            contentView = cachedViews.remove(0);
            recyclerView = (RecyclerView) contentView.findViewById(R.id.message_face_page_item);
        } else {
            contentView = View.inflate(container.getContext(), R.layout.message_face_page, null);
            recyclerView = (RecyclerView) contentView.findViewById(R.id.message_face_page_item);
            recyclerView.setLayoutManager(new GridLayoutManager(container.getContext(), 4));
            recyclerView.setAdapter(new FaceItemAdapter());
        }
        container.addView(contentView);
        FaceItemAdapter adapter = (FaceItemAdapter) recyclerView.getAdapter();
        adapter.init(position, faceItems);
        adapter.notifyDataSetChanged();
        return contentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        cachedViews.add((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setOnFaceItemClickListener(FaceItemEventListener listener) {
        this.listener = listener;
    }


    class FaceItemAdapter extends RecyclerView.Adapter<FaceViewHolder> {

        private int position;

        private List items = new ArrayList();

        public void init(int position, List items) {
            this.position = position;
            this.items = items;
        }

        @Override
        public FaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_face_selection, null);
            FaceViewHolder faceViewHolder = new FaceViewHolder(view);


            return faceViewHolder;
        }

        @Override
        public void onBindViewHolder(FaceViewHolder holder, int position) {
            Object o = items.get(this.position * 8 + position);
            holder.itemView.setOnClickListener(v -> {
                // TODO: 2017/10/9 点击操作
                if (listener != null) {
                    AppLogger.w("点击了面孔条目:" + position);
                    listener.onFaceItemClicked(this.position, position, holder.itemView);
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                // TODO: 2017/10/9 长按弹出菜单提示
                if (listener != null) {
                    AppLogger.w("点击了面孔条目:" + position);
                    listener.onFaceItemLongClicked(this.position, position, holder.itemView);
                }
                return true;
            });

        }

        @Override
        public int getItemCount() {
            return Math.min(8, items.size() - 8 * position);
        }
    }

    class FaceViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_item_face_selection)
        public ImageViewTip icon;
        @BindView(R.id.text_item_face_selection)
        public TextView text;

        public FaceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface FaceItemEventListener {

        void onFaceItemClicked(int page_position, int position, View faceItem);

        void onFaceItemLongClicked(int page_position, int position, View faceItem);
    }
}
