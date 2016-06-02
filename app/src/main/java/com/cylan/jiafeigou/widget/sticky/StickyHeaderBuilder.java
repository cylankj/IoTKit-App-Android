package com.cylan.jiafeigou.widget.sticky;

import android.content.Context;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * Created by hunt on 16-6-1.
 */

public abstract class StickyHeaderBuilder {

    protected final Context mContext;

    protected View mHeader;
    protected int mMinHeight;
    protected HeaderAnimator mAnimator;
    protected boolean mAllowTouchBehindHeader;

    protected StickyHeaderBuilder(final Context context) {
        mContext = context;
        mMinHeight = 0;
        mAllowTouchBehindHeader = false;
    }

    public abstract StickyHeader build();

//    public static ListViewBuilder stickTo(final ListView listView) {
//        return new ListViewBuilder(listView);
//    }

    public static RecyclerViewBuilder stickTo(final ViewGroup recyclerView) {
        StickyHeaderUtils.checkRecyclerView(recyclerView);
        return new RecyclerViewBuilder(recyclerView);
    }

    public static ScrollViewBuilder stickTo(final ScrollView scrollView) {
        return new ScrollViewBuilder(scrollView);
    }

    public static TargetBuilder stickTo(final Context context) {
        return new TargetBuilder(context);
    }

    public StickyHeaderBuilder setHeader(@IdRes final int idHeader, final ViewGroup view) {
        mHeader = view.findViewById(idHeader);
        return this;
    }

    public StickyHeaderBuilder setHeader(final View header) {
        mHeader = header;
        return this;
    }

    /**
     * Deprecated: use {@link #minHeightHeaderDim(int)}
     */
    @Deprecated
    public StickyHeaderBuilder minHeightHeaderRes(@DimenRes final int resDimension) {
        return minHeightHeaderDim(resDimension);
    }

    public StickyHeaderBuilder minHeightHeaderDim(@DimenRes final int resDimension) {
        mMinHeight = mContext.getResources().getDimensionPixelSize(resDimension);
        return this;
    }

    /**
     * Deprecated: use {@link #minHeightHeader(int)}
     */
    @Deprecated
    public StickyHeaderBuilder minHeightHeaderPixel(final int minHeight) {
        return minHeightHeader(minHeight);
    }

    public StickyHeaderBuilder minHeightHeader(final int minHeight) {
        mMinHeight = minHeight;
        return this;
    }

    public StickyHeaderBuilder animator(final HeaderAnimator animator) {
        mAnimator = animator;
        return this;
    }

    /**
     * Allows the touch of the views behind the StikkyHeader. by default is false
     *
     * @param allow true to allow the touch behind the StikkyHeader, false to allow only the scroll.
     */
    public StickyHeaderBuilder allowTouchBehindHeader(boolean allow) {
        mAllowTouchBehindHeader = allow;
        return this;
    }

//    public static class ListViewBuilder extends StickyHeaderBuilder {
//
//        private final ListView mListView;
//
//        protected ListViewBuilder(final ListView listView) {
//            super(listView.getContext());
//            mListView = listView;
//        }
//
//        @Override
//        public StickyHeaderListView build() {
//
//            //if the animator has not been set, the default one is used
//            if (mAnimator == null) {
//                mAnimator = new HeaderStikkyAnimator();
//            }
//
//            final StikkyHeaderListView stikkyHeaderListView = new StikkyHeaderListView(mContext, mListView, mHeader, mMinHeight, mAnimator);
//            stikkyHeaderListView.build(mAllowTouchBehindHeader);
//
//            return stikkyHeaderListView;
//        }
//    }

    public static class RecyclerViewBuilder extends StickyHeaderBuilder {

        private final RecyclerView mRecyclerView;

        protected RecyclerViewBuilder(final ViewGroup mRecyclerView) {
            super(mRecyclerView.getContext());
            this.mRecyclerView = (RecyclerView) mRecyclerView;
        }

        @Override
        public StickyHeaderRecyclerView build() {

            //if the animator has not been set, the default one is used
            if (mAnimator == null) {
                mAnimator = new HeaderStickyAnimator();
            }

            final StickyHeaderRecyclerView stickyHeaderRecyclerView = new StickyHeaderRecyclerView(mContext, mRecyclerView, mHeader, mMinHeight, mAnimator);
            stickyHeaderRecyclerView.build(mAllowTouchBehindHeader);

            return stickyHeaderRecyclerView;
        }

    }

    public static class ScrollViewBuilder extends StickyHeaderBuilder {

        private final ScrollView mScrollView;

        protected ScrollViewBuilder(final ScrollView scrollView) {
            super(scrollView.getContext());
            this.mScrollView = scrollView;
        }

        @Override
        public StickyHeaderScrollView build() {

            //if the animator has not been set, the default one is used
            if (mAnimator == null) {
                mAnimator = new HeaderStickyAnimator();
            }

            final StickyHeaderScrollView stikkyHeaderScrollView = new StickyHeaderScrollView(mContext, mScrollView, mHeader, mMinHeight, mAnimator);

            stikkyHeaderScrollView.build(mAllowTouchBehindHeader);

            return stikkyHeaderScrollView;
        }

    }

    public static class TargetBuilder extends StickyHeaderBuilder {

        private final Context mContext;

        protected TargetBuilder(final Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public StickyHeaderTarget build() {

            //if the animator has not been set, the default one is used
            if (mAnimator == null) {
                mAnimator = new HeaderStickyAnimator();
            }

            final StickyHeaderTarget stickyHeaderTarget = new StickyHeaderTarget(mContext, mHeader, mMinHeight, mAnimator);
            stickyHeaderTarget.build(mAllowTouchBehindHeader);

            return stickyHeaderTarget;
        }

    }
}
