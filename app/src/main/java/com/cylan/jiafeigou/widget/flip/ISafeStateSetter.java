package com.cylan.jiafeigou.widget.flip;

/**
 * Created by cylan-hunt on 16-12-23.
 */

public interface ISafeStateSetter {
    /**
     * 被动,刷新状态,从外部传进来
     *
     * @param state
     */
    void setState(boolean state);

    void setVisibility(boolean show);

    void setFlipListener(FlipImageView.OnFlipListener listener);

}
