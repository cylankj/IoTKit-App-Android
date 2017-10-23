package com.cylan.jiafeigou.n.view.adapter;

import android.graphics.Rect;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.view.cam.item.FaceItem;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.ListUtils;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.Collections;
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
            notifyDataSetChanged();
        } else {
            setFaceItems(faceItems);
        }
    }

    @Override
    public int getCount() {
        return faceItems.size() / JConstant.FACE_CNT_IN_PAGE + (faceItems.size() % JConstant.FACE_CNT_IN_PAGE == 0 ? 0 : 1);
    }

    public int getTotalCount() {
        return ListUtils.getSize(faceItems);
    }

    public List<FaceItem> getFaceItems() {
        return faceItems;
    }

    /**
     * @param index to setHasSelected =true, only one item is selected
     */
    public void updateSelectedItem(final int index) {
        final int cnt = ListUtils.getSize(faceItems);
        if (index < 0 || index > cnt - 1)
            return;
        for (int i = 0; i < cnt; i++) {
            faceItems.get(i).withSetSelected(index == i);
        }
        notifyDataSetChanged();
    }

    /**
     * {@link #updateSelectedItem(FaceItem)}
     */
    public void updateSelectedItem(FaceItem index) {
        if (index == null)
            return;
        for (FaceItem item : faceItems) {
            index.withSetSelected(item.equals(index));
        }
        notifyDataSetChanged();
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
            adapter.withSelectable(true);
            adapter.withMultiSelect(false);
            adapter.withSelectWithItemUpdate(true);
            adapter.withAllowDeselection(false);
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
                    AppLogger.d("点击了面孔条目 gPosition:" + (listener.getCurrentItem() * JConstant.FACE_CNT_IN_PAGE + position1) + "," + listener.getCurrentItem());
                    listener.onFaceItemClicked(position1, viewHolder.itemView, viewHolder.getIcon());
                    updateSelectedItem(listener.getCurrentItem() * JConstant.FACE_CNT_IN_PAGE + position1);
                }
                return true;
            });
            adapter.withOnLongClickListener((v, adapter1, item, position1) -> {
                // TODO: 2017/10/9 长按弹出菜单提示
                if (listener != null) {
                    AppLogger.w("点击了面孔条目:" + position1);
                    FaceItem.FaceItemViewHolder viewHolder = (FaceItem.FaceItemViewHolder) recyclerView.getChildViewHolder(v);
                    listener.onFaceItemLongClicked(position1, viewHolder.itemView, viewHolder.getIcon(), item.getFaceType());
                }
                return true;
            });
            recyclerView.setAdapter(adapter);
        }
        container.addView(contentView);
        FastItemAdapter<FaceItem> adapter = (FastItemAdapter<FaceItem>) recyclerView.getAdapter();
//        adapter.init(position, faceItems);
        List<FaceItem> faceItems = this.faceItems.subList(JConstant.FACE_CNT_IN_PAGE * position,
                JConstant.FACE_CNT_IN_PAGE * position + Math.min(JConstant.FACE_CNT_IN_PAGE, this.faceItems.size() - JConstant.FACE_CNT_IN_PAGE * position));
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
        int globalPosition = page_position * JConstant.FACE_CNT_IN_PAGE + position;

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

    public void sortByNewMessageVersion() {
        // TODO: 2017/10/16 先判断

        Collections.sort(faceItems, (item1, item2) -> (int) (item1.getVersion() - item2.getVersion()));
        notifyDataSetChanged();
    }

    public interface FaceItemEventListener {

        void onFaceItemClicked(int position, View parent, ImageView icon);

        void onFaceItemLongClicked(int position, View parent, ImageView icon, int faceType);

        int getCurrentItem();
    }
}
