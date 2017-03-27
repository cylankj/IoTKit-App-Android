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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.cylan.jiafeigou.R;

/**
 * This provides methods to help Activities load their UI.
 */
public class ActivityUtils {

    /**
     * The {@code fragment} is added to the container view with msgId {@code frameId}. The operation is
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
                        R.anim.slide_down_out,
                        R.anim.slide_down_in,
                        R.anim.slide_down_out)
                .add(containerId, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    public static void addFragmentToActivity(@NonNull FragmentManager fragmentManager,
                                             @NonNull Fragment fragment, int containerId, boolean id) {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_down_in,
                        R.anim.slide_out_left,
                        R.anim.slide_in_left,
                        R.anim.slide_out_left)
                .add(containerId, fragment, fragment.getClass().getSimpleName())
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

    public static boolean addFragmentSlideInFromRight(FragmentManager fragmentManager, Fragment fragment, int containerId) {
        final String tag = fragment.getClass().getSimpleName();
        Fragment f = fragmentManager.findFragmentByTag(tag);
        if (f != null && f.isVisible()) {
            return false;
        }
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_right_in,
                        R.anim.slide_out_left,
                        R.anim.slide_out_right,
                        R.anim.slide_out_right)
                .add(containerId, fragment, tag)
                .addToBackStack(tag)
                .commit();
        return true;
    }

    public static boolean addFragmentSlideInFromRight(FragmentManager fragmentManager, Fragment fragment, int containerId, boolean noStack) {
        final String tag = fragment.getClass().getSimpleName();
        Fragment f = fragmentManager.findFragmentByTag(tag);
        if (f != null && f.isVisible()) {
            return false;
        }
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_right_in,
                        R.anim.slide_out_left,
                        R.anim.slide_out_right,
                        R.anim.slide_out_right)
                .add(containerId, fragment, tag)
                .commit();
        return true;
    }

    public static boolean addFragmentSlideInFromLeft(FragmentManager fragmentManager, Fragment fragment, int containerId) {
        final String tag = fragment.getClass().getSimpleName();
        Fragment f = fragmentManager.findFragmentByTag(tag);
        if (f != null && f.isVisible()) {
            return false;
        }
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_left,
                        R.anim.slide_out_right,
                        R.anim.slide_out_right)
                .add(containerId, fragment, tag)
                .addToBackStack(tag)
                .commit();
        return true;
    }

    public static boolean addFragmentSlideInFromLeft(FragmentManager fragmentManager, Fragment fragment, int containerId, boolean noStatck) {
        final String tag = fragment.getClass().getSimpleName();
        Fragment f = fragmentManager.findFragmentByTag(tag);
        if (f != null && f.isVisible()) {
            return false;
        }
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_left,
                        R.anim.slide_out_left,
                        R.anim.slide_out_right,
                        R.anim.slide_out_right)
                .add(containerId, fragment, tag)
                .commit();
        return true;
    }

    public static boolean isFragmentInTop(Activity activity, int fragmentLayoutId) {
        View rootView = activity.findViewById(android.R.id.content);
        return rootView != null && rootView.findViewById(fragmentLayoutId) != null;
    }

    /**
     * 用来加载fragment的方法。
     */
    public static void loadFragment(int id, FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                //如果需要动画，可以把动画添加进来
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * 用来加载fragment的方法。
     */
    public static void loadFragmentNoBackStack(int id, FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                //如果需要动画，可以把动画添加进来
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * 用来加载fragment的方法。
     */
    public static void loadFragmentNoAnimation(int id, FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .add(id, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * 用来加载fragment的方法。
     */
    public static void replaceFragment(int id, FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(id, fragment, fragment.getClass().getSimpleName())
                .commit();
    }

    /**
     * 用来加载fragment的方法。
     */
    public static void replaceFragmentNoAnimation(int id, FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(id, fragment, fragment.getClass().getSimpleName())
                .commit();
    }
}
