/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cylan.jiafeigou.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.cylan.jiafeigou.R;

/**
 * This provides methods to help Activities load their UI.
 */
public class ActivityUtils {

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     */
    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int frameId) {
//        checkNotNull(fragmentManager);
//        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int containerId, int id) {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_down_in,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_left)
                .add(containerId, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public static void justPop(FragmentActivity activity) {
        if (activity != null && activity.getSupportFragmentManager() != null) {
            final int count = activity.getSupportFragmentManager().getBackStackEntryCount();
            if (count > 0) {
                activity.getSupportFragmentManager().popBackStack();
            }
        }
    }

    public static void addFragmentSlideInFromRight(FragmentManager fragmentManager, Fragment fragment, int containerId) {
        final String tag = fragment.getClass().getSimpleName();
        Fragment f = fragmentManager.findFragmentByTag(tag);
        if (f != null && f.isVisible()) {
            return;
        }
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_right_in,
                        R.anim.slide_out_left,
                        R.anim.slide_out_right,
                        R.anim.slide_out_right)
                .add(android.R.id.content, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }

}
