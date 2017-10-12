package com.cylan.jiafeigou.n.view.adapter;

import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.view.cam.item.FaceItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanzhendong on 2017/9/29.
 */

public class CamMessageFaceAdapter extends PagerAdapter {

    private List<FaceItem> faceItems = new ArrayList<>();

    private List<View> cachedViews = new ArrayList<>();
    private FaceItemEventListener listener;
    private List<FaceItem> preload = new ArrayList<>();

    public void setFaceItems(List<FaceItem> faceItems) {
        if (faceItems != null) {
            faceItems.clear();
        } else {
            this.faceItems = new ArrayList<>();
        }
        this.faceItems.addAll(preload);
        if (faceItems != null) {
            this.faceItems.addAll(faceItems);
        }
        notifyDataSetChanged();
    }

    public void appendFaceItems(List<FaceItem> faceItems) {
        if (this.faceItems != null && this.faceItems.size() > 0) {
            this.faceItems.addAll(faceItems);
        } else {
            setFaceItems(faceItems);
        }
    }

    @Override
    public int getCount() {
        return faceItems.size() / 6 + (faceItems.size() % 6 == 0 ? 0 : 1);
    }

    public int getTotalCount() {
        return faceItems.size();
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
            recyclerView.setLayoutManager(new GridLayoutManager(container.getContext(), 3));
            FastItemAdapter<FaceItem> adapter = new FastItemAdapter<>();
            recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    if (parent.getChildLayoutPosition(view) % 3 == 1) {
                        int pixelOffset = ContextUtils.getContext().getResources().getDimensionPixelOffset(R.dimen.y18);
                        outRect.left = pixelOffset;
                        outRect.right = pixelOffset;
                    }
                }
            });
            adapter.withOnClickListener((v, adapter1, item, position1) -> {
                // TODO: 2017/10/9 点击操作
                if (listener != null) {
                    FaceItem.FaceItemViewHolder viewHolder = (FaceItem.FaceItemViewHolder) recyclerView.getChildViewHolder(v);
                    AppLogger.w("点击了面孔条目:" + position);
                    listener.onFaceItemClicked(position, position1, viewHolder.itemView, viewHolder.getIcon());
                }
                return true;
            });
            adapter.withOnLongClickListener((v, adapter1, item, position1) -> {
                // TODO: 2017/10/9 长按弹出菜单提示
                if (listener != null) {
                    AppLogger.w("点击了面孔条目:" + position);
                    FaceItem.FaceItemViewHolder viewHolder = (FaceItem.FaceItemViewHolder) recyclerView.getChildViewHolder(v);
                    listener.onFaceItemLongClicked(position, position1, viewHolder.itemView, viewHolder.getIcon());
                }
                return true;
            });
            recyclerView.setAdapter(adapter);
        }
        container.addView(contentView);
        FastItemAdapter<FaceItem> adapter = (FastItemAdapter<FaceItem>) recyclerView.getAdapter();
//        adapter.init(position, faceItems);
        List<FaceItem> faceItems = this.faceItems.subList(6 * position, Math.min(6, this.faceItems.size() - 6 * position));
        adapter.set(faceItems);
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

    public FaceItem getGlobalItem(int page_position, int position) {
        int globalPosition = page_position * 6 + position;

        if (globalPosition > faceItems.size()) {
            return null;
        }
        return faceItems.get(globalPosition);
    }

    public void setPreloadFaceItems(List<FaceItem> list) {
        if (this.preload != null) {
            this.preload.clear();
        } else {
            this.preload = new ArrayList<>();
        }

        this.preload.addAll(list);
        setFaceItems(null);
    }

    public boolean hasPreloadFaceItems() {
        return preload != null && preload.size() > 0;
    }


//    class FaceItemAdapter extends RecyclerView.Adapter<FaceItem.FaceItemViewHolder> {
//
//        private int position;
//
//        private List items = new ArrayList();
//
//        public void init(int position, List items) {
//            this.position = position;
//            this.items = items;
//        }
//
//        @Override
//        public FaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = View.inflate(parent.getContext(), R.layout.item_face_selection, null);
//            FaceViewHolder faceViewHolder = new FaceViewHolder(view);
//
//
//            return faceViewHolder;
//        }
//
//        @Override
//        public void onBindViewHolder(FaceViewHolder holder, int position) {
//            Object o = items.get(this.position * 8 + position);
//
//            holder.itemView.setOnClickListener(v -> {
//                // TODO: 2017/10/9 点击操作
//                if (listener != null) {
//                    AppLogger.w("点击了面孔条目:" + position);
//                    listener.onFaceItemClicked(this.position, position, holder.itemView);
//                }
//            });
//
//            holder.itemView.setOnLongClickListener(v -> {
//                // TODO: 2017/10/9 长按弹出菜单提示
//                if (listener != null) {
//                    AppLogger.w("点击了面孔条目:" + position);
//                    listener.onFaceItemLongClicked(this.position, position, holder.itemView);
//                }
//                return true;
//            });
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return Math.min(8, items.size() - 8 * position);
//        }
//    }

//    class FaceViewHolder extends RecyclerView.ViewHolder {
//        @BindView(R.id.img_item_face_selection)
//        public ImageViewTip icon;
//        @BindView(R.id.text_item_face_selection)
//        public TextView text;
//
//        public FaceViewHolder(View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//    }

    public interface FaceItemEventListener {

        void onFaceItemClicked(int page_position, int position, View parent, View icon);

        void onFaceItemLongClicked(int page_position, int position, View parent, View icon);
    }
}
