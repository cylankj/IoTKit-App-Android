package com.cylan.jiafeigou.widget.sticky;


public class HeaderStickyAnimator extends BaseStickyHeaderAnimator {

    protected AnimatorBuilder mAnimatorBuilder;
    private float mBoundedTranslatedRatio;
    private boolean hasAnimatorBundles = false;

    @Override
    protected void onAnimatorReady() {
        super.onAnimatorReady();
        mAnimatorBuilder = getAnimatorBuilder();
        hasAnimatorBundles = (mAnimatorBuilder != null) && (mAnimatorBuilder.hasAnimatorBundles());
    }

    /**
     * Override if you want to load the animator builder
     */
    public AnimatorBuilder getAnimatorBuilder() {
        return null;
    }

    @Override
    public void onScroll(int scrolledY) {
        super.onScroll(scrolledY);

        mBoundedTranslatedRatio = clamp(getTranslationRatio(), 0f, 1f);

        if (hasAnimatorBundles) {
            mAnimatorBuilder.animateOnScroll(mBoundedTranslatedRatio, StickyCompat.getTranslationY(getHeader()));
        }

    }

    public float getBoundedTranslatedRatio() {
        return mBoundedTranslatedRatio;
    }
}