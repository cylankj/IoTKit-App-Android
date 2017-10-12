package com.cylan.jiafeigou.utils;

import android.support.annotation.FloatRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanzhendong on 2017/10/11.
 */

public class AvatarRequest {
    private List<String> avatars = new ArrayList<>();

    private float gap;

    private AvatarRequest(List<String> avatars, float gap) {
        this.avatars.addAll(avatars);
        this.gap = gap;
    }

    public List<String> getAvatars() {
        return avatars;
    }

    public float getGap() {
        return gap;
    }

    public static class Builder {
        private List<String> avatarUrls = new ArrayList<>();
        private float gap = .5F;

        public Builder addAvatar(String avatarUrl) {
            if (avatarUrls.size() <= 5) {
                avatarUrls.add(avatarUrl);
            }
            return this;
        }

        public Builder setGap(@FloatRange(from = 0.f, to = 1.f) float gap) {
            this.gap = gap;
            return this;
        }

        public AvatarRequest build() {
            return new AvatarRequest(avatarUrls, gap);
        }

    }

}
